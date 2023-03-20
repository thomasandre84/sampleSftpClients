package samplesftpclients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example that implements the Apache Mina SFTP Client.
 */
public class Mina extends AbstractSftpClient implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mina.class);
    private Mina(String username, String password, String host, int port) {
        super(username, password, host, port);
    }

    private Mina(String username, byte[] privateKey, String host, int port) {
        super(username, privateKey, host, port);
    }


    @Override
    public void close() throws Exception {

    }
}
