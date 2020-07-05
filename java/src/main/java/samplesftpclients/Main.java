package samplesftpclients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samplesftpclients.util.EnvConfig;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try (JCraft jCraft = new JCraft()) {
            jCraft.login(EnvConfig.HOST, EnvConfig.PORT, EnvConfig.USERNAME, EnvConfig.PASSWORD);
            LOGGER.info("{}", jCraft.listCurrentRemoteDir());
            jCraft.changeRemoteDir(EnvConfig.REMOTE_DIR);
            jCraft.listCurrentRemoteDir();
            jCraft.downloadAllFilesCurrentRemoteDir();

        } catch (Exception e) {
            LOGGER.error("{}", e);
        }

    }
}
