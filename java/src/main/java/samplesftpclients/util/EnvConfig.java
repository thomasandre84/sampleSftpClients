package samplesftpclients.util;

/**
 * Just for share. Do not do like this in a real application!
 */
public class EnvConfig {

    private EnvConfig(){}

    public static String USERNAME = System.getenv("SFTP_USERNAME");
    public static String PASSWORD = System.getenv("SFTP_PASSWORD");
    public static String HOST = System.getenv("SFTP_HOST");
    public static int PORT = Integer.parseInt(System.getenv("SFTP_PORT"));
    public static String REMOTE_DIR = System.getenv("SFTP_REMOTE_DIR");
    public static String LOCAL_DIR = System.getenv("LOCAL_DIR");

}
