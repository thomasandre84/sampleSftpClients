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
        Libs lib = Libs.JCRAFT; // default
        if (args.length > 0) {
            lib = Libs.valueOf(args[0]);
        }
        if (args.length > 1) {
            useKey = Boolean.parseBoolean(args[1]);
        }

        switch (lib) {
            case JCRAFT:
                useJCraft(useKey);
                break;
            case MINA:
                useMina(useKey);
                break;

        }


    }

    private static void useJCraft(boolean useKey){
        try (JCraft jCraft = useKey ?
                JCraft.keyLogin(EnvConfig.USERNAME, LocalFilesUtil.getContentFromFile(Path.of(EnvConfig.PRIVATE_KEY_PATH)), EnvConfig.HOST, EnvConfig.PORT) :
                JCraft.passwordLogin(EnvConfig.USERNAME, EnvConfig.PASSWORD, EnvConfig.HOST, EnvConfig.PORT)) {
            jCraft.connect();
            LOGGER.info("List Current Remote Directory: '{}'", jCraft.listCurrentRemoteDir());
            //jCraft.changeRemoteDir(EnvConfig.REMOTE_DIR);
            //jCraft.listCurrentRemoteDir();
            //jCraft.downloadAllFilesCurrentRemoteDir();

        } catch (Exception e) {
            LOGGER.error("Error: ", e);
        }
    }

    private static void useMina(boolean useKey) {
        try (Mina mina= useKey ?
                Mina.keyLogin(EnvConfig.USERNAME, LocalFilesUtil.getContentFromFile(Path.of(EnvConfig.PRIVATE_KEY_PATH)), EnvConfig.HOST, EnvConfig.PORT) :
                Mina.passwordLogin(EnvConfig.USERNAME, EnvConfig.PASSWORD, EnvConfig.HOST, EnvConfig.PORT)) {
            LOGGER.info("Trying to connect");
            mina.connect();
            LOGGER.info("Connected");
            //mina.listCurrentRemoteDir();
            LOGGER.info("List Current Remote Directory: '{}'", mina.listCurrentRemoteDir());
        } catch (Exception e) {
            LOGGER.error("Error: ",e);
        }
    }

    enum Libs {
        JCRAFT,
        MINA
    }
}
