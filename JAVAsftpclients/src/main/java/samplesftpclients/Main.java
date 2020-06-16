package samplesftpclients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samplesftpclients.util.EnvConfig;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try (JCraft jCraft = new JCraft(EnvConfig.USERNAME, EnvConfig.PASSWORD, EnvConfig.HOST, EnvConfig.PORT)) {
            jCraft.connect();

        } catch (Exception e) {
            LOGGER.error("{}", e);
        }

    }
}
