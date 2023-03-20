package samplesftpclients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samplesftpclients.util.EnvConfig;
import samplesftpclients.util.LocalFilesUtil;

import java.nio.file.Path;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        boolean useKey = false;
        if (args.length > 0) {
            useKey = Boolean.parseBoolean(args[0]);
        }
        try (JCraft jCraft = useKey ?
                JCraft.keyLogin(EnvConfig.USERNAME, LocalFilesUtil.getContentFromFile(Path.of(EnvConfig.PRIVATE_KEY_PATH)), EnvConfig.HOST, EnvConfig.PORT) :
                JCraft.passwordLogin(EnvConfig.USERNAME, EnvConfig.PASSWORD, EnvConfig.HOST, EnvConfig.PORT)) {
            jCraft.connect();
            LOGGER.info("{}", jCraft.listCurrentRemoteDir());
            //jCraft.changeRemoteDir(EnvConfig.REMOTE_DIR);
            //jCraft.listCurrentRemoteDir();
            //jCraft.downloadAllFilesCurrentRemoteDir();

        } catch (Exception e) {
            LOGGER.error("Error: ", e);
        }

    }
}
