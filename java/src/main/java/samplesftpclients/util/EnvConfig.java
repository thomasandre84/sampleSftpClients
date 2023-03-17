package samplesftpclients.util;

import java.util.Optional;

/**
 * Just for share. Do not do like this in a real application!
 */
public class EnvConfig {

    private EnvConfig(){}

    public static String USERNAME = Optional.ofNullable(System.getenv("SFTP_USERNAME")).orElse("user");
    public static String PASSWORD = System.getenv("SFTP_PASSWORD");
    public static String HOST = Optional.ofNullable(System.getenv("SFTP_HOST")).orElse("localhost");
    public static Integer PORT = Integer.parseInt(Optional.ofNullable(System.getenv("SFTP_PORT")).orElse("22"));
    public static String REMOTE_DIR = Optional.ofNullable(System.getenv("SFTP_REMOTE_DIR")).orElse("tmp");
    public static String LOCAL_DIR = Optional.ofNullable(System.getenv("LOCAL_DIR")).orElse("/tmp");
    public static String PRIVATE_KEY_PATH = Optional.ofNullable(System.getenv("SFTP_PRIVATE_KEY_PATH")).orElse(System.getProperty("user.home") + "/.ssh/id_rsa");
    public static String KNOWN_HOSTS_PATH = Optional.ofNullable(System.getenv("SFTP_KNOWN_HOSTS_PATH")).orElse(System.getProperty("user.home") + "/.ssh/known_hosts");
}
