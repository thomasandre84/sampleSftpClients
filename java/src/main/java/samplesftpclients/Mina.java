package samplesftpclients;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.RequiredServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samplesftpclients.util.EnvConfig;
import samplesftpclients.util.KnowHostsKeyVerifier;
import samplesftpclients.util.LocalFilesUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An example that implements the Apache Mina SFTP Client.
 */
public class Mina extends AbstractSftpClient implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mina.class);
    private static Duration TIMEOUT = Duration.ofMillis(1_000L);

    public static Mina passwordLogin(String username,String password,String host, int port) {
        return new Mina(username, password, host, port);
    }

    public static Mina keyLogin(String username, byte[] privateKey, String host, int port) {
        return new Mina(username, privateKey, host, port);
    }
    private Mina(String username, String password, String host, int port) {
        super(username, password, host, port);
    }

    private Mina(String username, byte[] privateKey, String host, int port) {
        super(username, privateKey, host, port);
    }

    SshClient sshClient;
    ClientSession sshSession;
    SftpClient sftpClient;

    // The underlying session will also be closed when the client is
    public void connect() throws IOException {
        sshClient = SshClient.setUpDefaultClient();
        sshClient.start();

        sshSession = sshClient.connect(username, host, port)
                .verify(TIMEOUT)
                .getClientSession();
        Collection<KeyPair> keyPairs = loadKeyPairs();
        KeyIdentityProvider keyIdentityProvider = buildKeyIdentityProvider(keyPairs);
        sshSession.setKeyIdentityProvider(keyIdentityProvider);

        verifyServerKey(sshSession);

        sshSession.auth().verify(TIMEOUT);

        sftpClient = SftpClientFactory.instance()
                .createSftpClient(sshSession);
    }

    private Collection<KeyPair> loadKeyPairs() throws IOException {
        try {
            KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();
            return loader.loadKeyPairs(null, null, null, new String(privateKey));

        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private void verifyServerKey(ClientSession sshSession) throws IOException {
        ServerKeyVerifier serverKeyVerifier = sshSession.getServerKeyVerifier();
        //KnownHostsServerKeyVerifier knownHostsServerKeyVerifier = new KnownHostsServerKeyVerifier(serverKeyVerifier, Path.of(EnvConfig.KNOWN_HOSTS_PATH));
        KnowHostsKeyVerifier knownHostsKeyVerifier = new KnowHostsKeyVerifier(LocalFilesUtil.getContentFromFile(Path.of(EnvConfig.KNOWN_HOSTS_PATH)));


        //RequiredServerKeyVerifier requiredServerKeyVerifier = new RequiredServerKeyVerifier(publicKey);
        sshSession.setServerKeyVerifier(knownHostsKeyVerifier);
        SocketAddress remoteAddress = new InetSocketAddress(host, port);

        List<ClientSession.ClientSessionEvent> events = Arrays.asList(ClientSession.ClientSessionEvent.WAIT_AUTH);
        sshSession.waitFor(events, TIMEOUT);
        PublicKey publicKey = sshSession.getServerKey();

        knownHostsKeyVerifier.verifyServerKey(sshSession, remoteAddress, publicKey);
    }

    private KeyIdentityProvider buildKeyIdentityProvider(Collection<KeyPair> keyPairs) {
        return KeyIdentityProvider.wrapKeyPairs(keyPairs);
    }

    public List<String> listCurrentRemoteDir() throws IOException {
        SftpClient.CloseableHandle handle = sftpClient.openDir(".");
        List<String> fileNames = new ArrayList<>();
        for (SftpClient.DirEntry dirEntry : sftpClient.listDir(handle)) {
            fileNames.add(dirEntry.getFilename());
        }
        return fileNames;
    }



    @Override
    public void close() throws Exception {
        sftpClient.close();
        sshSession.close();
        sshClient.close();
    }
}
