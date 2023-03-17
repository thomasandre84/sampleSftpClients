package samplesftpclients.util;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.UnsupportedEncodingException;

public class IdentityKey implements Identity {
    private JSch jsch;
    private KeyPair kpair;
    private String identity;

    public static IdentityKey createIdentity(JSch jsch, KeyPair keyPair, String identity) {
        return new IdentityKey(jsch, keyPair, identity);
    }

    private IdentityKey(JSch jsch, KeyPair kpair, String identity){
        this.jsch = jsch;
        this.kpair = kpair;
        this.identity = identity;
    }

    @Override
    public boolean setPassphrase(byte[] passphrase) throws JSchException {
        return kpair.decrypt(passphrase);
    }

    @Override
    public byte[] getPublicKeyBlob() {
        return kpair.getPublicKeyBlob();
    }

    @Override
    public byte[] getSignature(byte[] data) {
        return kpair.getSignature(data);
    }

    @Override
    public boolean decrypt() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getAlgName() {
        return "ssh-rsa";
    }

    @Override
    public String getName() {
        return identity;
    }

    @Override
    public boolean isEncrypted() {
        return kpair.isEncrypted();
    }

    @Override
    public void clear() {
        kpair.dispose();
        kpair = null;
    }

    public KeyPair getKeyPair() {
        return kpair;
    }
}
