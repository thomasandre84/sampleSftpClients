package samplesftpclients;

import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JCraftTest {

    static JCraft jCraft;
    private static String username = System.getenv("SFTP_USERNAME");
    private static String password = System.getenv("SFTP_PASSWORD");
    private static String host = System.getenv("SFTP_HOST");
    private static int port = Integer.parseInt(System.getenv("SFTP_PORT"));
    private static String remoteDir = System.getenv("SFTP_REMOTE_DIR");
    private static String localDir = System.getenv("LOCAL_DIR");

    @BeforeAll
    void setUp() {
        jCraft = new JCraft(username, password, host, port);
    }

    @AfterAll
    void disconnect() throws Exception {
        jCraft.disconnect();
    }

    @Test
    @Order(1)
    void connect() {
        jCraft.connect();
        assertTrue(jCraft.channel.isConnected());
        assertTrue(jCraft.channelSftp.isConnected());
    }

    @Test
    @Order(2)
    void changeRemoteDir() throws Exception {
        jCraft.changeRemoteDir(remoteDir);
        assertEquals(remoteDir, jCraft.channelSftp.pwd());
    }

    @Test
    @Order(3)
    void listCurrentRemoteDir() {
        List<String> files = jCraft.listCurrentRemoteDir();
        assertFalse(files.isEmpty());
        files.forEach(f -> assertFalse(f.startsWith("/")));
    }

    @Test
    @Order(4)
    void listRemoteDir() {
        List<String> files = jCraft.listRemoteDir(remoteDir);
        assertFalse(files.isEmpty());
        files.forEach(f -> assertTrue(f.startsWith(remoteDir)));
    }

    @Test
    @Order(5)
    void downloadAllFilesCurrentRemoteDir() throws Exception {
        jCraft.changeLocalDir(localDir);
        jCraft.downloadAllFilesCurrentRemoteDir();

        List<String> files = jCraft.listCurrentRemoteDir();
        List<String> localFiles = LocalFilesUtil.getLocalFiles(localDir);
        // Every remote file is in the local files - expecting, nothing has changed during download
        files.forEach(f -> assertTrue(localFiles.contains(f)));

        // cleanup
        LocalFilesUtil.deleteLocalFiles(localDir);
    }

    @Test
    @Order(6)
    void downloadAllFilesRemoteDir() throws Exception {
        jCraft.changeRemoteDir("/"); // let's get to the root
        jCraft.downloadAllFilesRemoteDir(remoteDir);
        
        List<String> files = jCraft.listRemoteDir(remoteDir); // Here they have FQN
        List<String> localFiles = LocalFilesUtil.getLocalFiles(localDir);
        List<String> fileNames = LocalFilesUtil.getFileNames(files);
        // Every remote file is in the local files - expecting, nothing has changed during download
        fileNames.forEach(f -> assertTrue(localFiles.contains(f)));

        // cleanup
        LocalFilesUtil.deleteLocalFiles(localDir);
    }


}