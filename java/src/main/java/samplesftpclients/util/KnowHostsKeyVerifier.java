package samplesftpclients.util;

import org.apache.sshd.client.config.hosts.KnownHostEntry;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class KnowHostsKeyVerifier implements ServerKeyVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnowHostsKeyVerifier.class);
    public static final String STRICT_CHECKING_OPTION = "StrictHostKeyChecking";
    public static final String KNOWN_HOSTS_FILE_OPTION = "UserKnownHostsFile";
    private final Collection<KnownHostEntry> knownHostEntries;

    public KnowHostsKeyVerifier(byte[] knownHosts) throws IOException {
        this.knownHostEntries = KnownHostEntry.readKnownHostEntries(byteArrayToBufferedReader(knownHosts));
    }
    private static BufferedReader byteArrayToBufferedReader(byte[] content) {
        var is = new ByteArrayInputStream(content);
        var bfReader = new BufferedReader(new InputStreamReader(is));
        return bfReader;
    }


    @Override
    public boolean verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey) {
        Collection<KnownHostsServerKeyVerifier.HostEntryPair> knownHosts = null;
        try {
            knownHosts = getKnownHosts(clientSession, this.knownHostEntries);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return acceptKnownHostEntries(clientSession, remoteAddress, serverKey, knownHosts);
    }

    private static Collection<KnownHostsServerKeyVerifier.HostEntryPair> getKnownHosts(ClientSession session, Collection<KnownHostEntry> entries) throws GeneralSecurityException, IOException {
        List<KnownHostsServerKeyVerifier.HostEntryPair> keys = new ArrayList<>(entries.size());
        PublicKeyEntryResolver resolver = getFallbackPublicKeyEntryResolver();
        for (KnownHostEntry entry : entries) {
            PublicKey key = resolveHostKey(session, entry, resolver);
            if (key != null) {
                keys.add(new KnownHostsServerKeyVerifier.HostEntryPair(entry, key));
            }
        }
        return keys;
    }
    private static PublicKeyEntryResolver getFallbackPublicKeyEntryResolver() {
        return PublicKeyEntryResolver.IGNORING;
    }

    private static PublicKey resolveHostKey(
            ClientSession session, KnownHostEntry entry, PublicKeyEntryResolver resolver)
            throws IOException, GeneralSecurityException {
        if (entry == null) {
            return null;
        }

        AuthorizedKeyEntry authEntry = ValidateUtils.checkNotNull(entry.getKeyEntry(), "No key extracted from %s", entry);
        PublicKey key = authEntry.resolvePublicKey(session, resolver);

        return key;
    }

    private static boolean acceptKnownHostEntries(
            ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey,
            Collection<KnownHostsServerKeyVerifier.HostEntryPair> knownHosts) {

        KnownHostsServerKeyVerifier.HostEntryPair match = findKnownHostEntry(clientSession, remoteAddress, knownHosts);
        if (match == null) {
            return false;
        }

        KnownHostEntry entry = match.getHostEntry();
        PublicKey expected = match.getServerKey();
        if (KeyUtils.compareKeys(expected, serverKey)) {
            return acceptKnownHostEntry(clientSession, remoteAddress, serverKey, entry);
        }


        return true;
    }

    private static KnownHostsServerKeyVerifier.HostEntryPair findKnownHostEntry(
            ClientSession clientSession, SocketAddress remoteAddress, Collection<KnownHostsServerKeyVerifier.HostEntryPair> knownHosts) {
        if (GenericUtils.isEmpty(knownHosts)) {
            return null;
        }

        Collection<SshdSocketAddress> candidates = resolveHostNetworkIdentities(clientSession, remoteAddress);
        boolean debugEnabled = LOGGER.isDebugEnabled();
        if (debugEnabled) {
            LOGGER.debug("findKnownHostEntry({})[{}] host network identities: {}",
                    clientSession, remoteAddress, candidates);
        }

        if (GenericUtils.isEmpty(candidates)) {
            return null;
        }

        for (KnownHostsServerKeyVerifier.HostEntryPair match : knownHosts) {
            KnownHostEntry entry = match.getHostEntry();
            for (SshdSocketAddress host : candidates) {
                try {
                    if (entry.isHostMatch(host.getHostName(), host.getPort())) {
                        if (debugEnabled) {
                            LOGGER.debug("findKnownHostEntry({})[{}] matched host={} for entry={}",
                                    clientSession, remoteAddress, host, entry);
                        }
                        return match;
                    }
                } catch (RuntimeException | Error e) {
                    LOGGER.warn("findKnownHostEntry({})[{}] failed ({}) to check host={} for entry={}: {}",
                            clientSession, remoteAddress, e.getClass().getSimpleName(),
                            host, entry.getConfigLine(), e.getMessage(), e);
                }
            }
        }

        return null; // no match found
    }

    private static Collection<SshdSocketAddress> resolveHostNetworkIdentities(
            ClientSession clientSession, SocketAddress remoteAddress) {
        /*
         * NOTE !!! we do not resolve the fully-qualified name to avoid long DNS timeouts. Instead we use the reported
         * peer address and the original connection target host
         */
        Collection<SshdSocketAddress> candidates = new TreeSet<>(SshdSocketAddress.BY_HOST_AND_PORT);
        candidates.add(SshdSocketAddress.toSshdSocketAddress(remoteAddress));
        SocketAddress connectAddress = clientSession.getConnectAddress();
        candidates.add(SshdSocketAddress.toSshdSocketAddress(connectAddress));
        return candidates;
    }

    private static boolean acceptKnownHostEntry(
            ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey, KnownHostEntry entry) {
        if (entry == null) { // not really expected, but manage it
            return false;
        }

        if ("revoked".equals(entry.getMarker())) {
            LOGGER.debug("acceptKnownHostEntry({})[{}] key={}-{} marked as {}",
                    clientSession, remoteAddress, KeyUtils.getKeyType(serverKey), KeyUtils.getFingerPrint(serverKey),
                    entry.getMarker());
            return false;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("acceptKnownHostEntry({})[{}] matched key={}-{}",
                    clientSession, remoteAddress, KeyUtils.getKeyType(serverKey), KeyUtils.getFingerPrint(serverKey));
        }
        return true;
    }


}
