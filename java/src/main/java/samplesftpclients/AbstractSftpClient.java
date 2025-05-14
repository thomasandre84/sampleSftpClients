package samplesftpclients;

import java.nio.file.Path;

public abstract class AbstractSftpClient {
    protected String username;
    protected String password;
    protected byte[] privateKey;
    protected Path privateKeyPath;
    protected String host;
    protected int port;
    protected Path knownHostsPath;

    protected AbstractSftpClient(){}

    protected AbstractSftpClient(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public AbstractSftpClient(String username, byte[] privateKey, String host, int port) {
        this.username = username;
        this.privateKey = privateKey;
        this.host = host;
        this.port = port;
    }

    public AbstractSftpClient(String username, Path privateKeyPath, String host, int port) {
        this.username = username;
        this.privateKeyPath = privateKeyPath;
        this.host = host;
        this.port = port;
    }
}
