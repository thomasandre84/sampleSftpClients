package samplesftpclients;

import org.junit.jupiter.api.*;
import samplesftpclients.util.EnvConfig;
import samplesftpclients.util.LocalFilesUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JCraftTest {

    static JCraft jCraft;


    @BeforeAll
    void setUp() {
        jCraft = new JCraft(EnvConfig.USERNAME, EnvConfig.PASSWORD, EnvConfig.HOST, EnvConfig.PORT);
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
        jCraft.changeRemoteDir(EnvConfig.REMOTE_DIR);
        assertEquals(EnvConfig.REMOTE_DIR, jCraft.channelSftp.pwd());
    }

    @Test
    @Order(3)
    void listCurrentRemoteDir() {
        List<String> files = jCraft.listCurrentRemoteDir();
        assertFalse(files.isEmpty()); // really make sure, the folder has a file
        files.forEach(f -> assertFalse(f.startsWith("/")));
    }

    @Test
    @Order(4)
    void listRemoteDir() {
        List<String> files = jCraft.listRemoteDir(EnvConfig.REMOTE_DIR);
        assertFalse(files.isEmpty()); // really make sure, the folder has a file
        files.forEach(f -> assertTrue(f.startsWith(EnvConfig.REMOTE_DIR)));
    }

    @Test
    @Order(5)
    void downloadAllFilesCurrentRemoteDir() throws Exception {
        jCraft.changeLocalDir(EnvConfig.LOCAL_DIR);
        jCraft.downloadAllFilesCurrentRemoteDir();

        List<String> files = jCraft.listCurrentRemoteDir();
        List<String> localFiles = LocalFilesUtil.getLocalFiles(EnvConfig.LOCAL_DIR);
        // Every remote file is in the local files - expecting, nothing has changed during download
        files.forEach(f -> assertTrue(localFiles.contains(f)));

        // cleanup
        LocalFilesUtil.deleteLocalFiles(EnvConfig.LOCAL_DIR);
    }

    @Test
    @Order(6)
    void downloadAllFilesRemoteDir() throws Exception {
        jCraft.changeRemoteDir("/"); // let's get to the root
        jCraft.downloadAllFilesRemoteDir(EnvConfig.REMOTE_DIR);
        
        List<String> files = jCraft.listRemoteDir(EnvConfig.REMOTE_DIR); // Here they have FQN
        List<String> localFiles = LocalFilesUtil.getLocalFiles(EnvConfig.LOCAL_DIR);
        List<String> fileNames = LocalFilesUtil.getFileNames(files);
        // Every remote file is in the local files - expecting, nothing has changed during download
        fileNames.forEach(f -> assertTrue(localFiles.contains(f)));

        // cleanup
        LocalFilesUtil.deleteLocalFiles(EnvConfig.LOCAL_DIR);
    }


}