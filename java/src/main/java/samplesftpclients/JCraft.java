package samplesftpclients;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samplesftpclients.util.EnvConfig;
import samplesftpclients.util.IdentityKey;
import samplesftpclients.util.LocalFilesUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * An example wrapper class for JCraft.
 */
public final class JCraft extends AbstractSftpClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCraft.class);

    private static final String CHANNEL = "sftp";

    private Session session;
    ChannelSftp channelSftp;
    private JSch jsch;

    private JCraft (){};

    public static JCraft passwordLogin(String username,String password,String host, int port) {
        JCraft jCraft = new JCraft();
        jCraft.username = username;
        jCraft.password = password;
        jCraft.host = host;
        jCraft.port = port;

        return jCraft;
    }

    public static JCraft keyLogin(String username, byte[] privateKey, String host, int port) {
        JCraft jCraft = new JCraft();
        jCraft.username = username;
        jCraft.privateKey = privateKey;
        jCraft.host=host;
        jCraft.port=port;

        return jCraft;
    }

    /**
     * Connect to the SFTP Server.
     */
    public void connect() {
        LOGGER.info("Connecting to the SFTP-Server: {}", host);
        jsch = new JSch();

        try {
            Properties config = new Properties();
            //config.put("StrictHostKeyChecking", "no");
            jsch.setKnownHosts(LocalFilesUtil.convertByteArrayToInputStream(LocalFilesUtil.getContentFromFile(Path.of(EnvConfig.KNOWN_HOSTS_PATH))));
            if (privateKey != null) {
                Identity identity = createIndentity(jsch, this.privateKey);
                jsch.addIdentity(identity, null);
                config.put("PreferredAuthentications", "publickey");
            }
            session = jsch.getSession(username, host, port);
            if (password != null) session.setPassword(password);

            session.setConfig(config);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel(CHANNEL);
            channelSftp.connect();
            LOGGER.info("Successfully logged in");
        } catch (JSchException e) {
            LOGGER.error("Cannot Login: {}", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Change Remote Directory
     *
     * @param folder
     */
    public void changeRemoteDir(String folder) {
        try {
            LOGGER.debug("Changing folder to: {}", folder);
            channelSftp.cd(folder);
            LOGGER.debug("Successfully Changed folder to: {}", folder);
        } catch (SftpException e) {
            LOGGER.error("Cannot Change Folder {}", e);
        }
    }

    /**
     * List all files in current Remote Directory.
     *
     * @return
     */
    public List<String> listCurrentRemoteDir() {
        List<String> dir = new ArrayList<>();
        try {
            LOGGER.debug("Listing current Directory");
            String curdir = channelSftp.pwd();
            Vector<ChannelSftp.LsEntry> dirv = channelSftp.ls(curdir);
            LOGGER.debug("Listed current Directory: {}", curdir);
            dirv.forEach(p -> {
                if (!p.getFilename().startsWith("."))
                    dir.add(p.getFilename());
            });
        } catch (SftpException e) {
            LOGGER.error("Cannot List Current Directory: {}", e);
        }
        return dir;
    }

    /**
     * List all files incl. path in Remote Directory.
     *
     * @param remoteFolder
     * @return
     */
    public List<String> listRemoteDir(String remoteFolder) {
        List<String> dir = new ArrayList<>();
        try {
            LOGGER.debug("Listing remote Directory: {}", remoteFolder);
            Vector<ChannelSftp.LsEntry> dirv = channelSftp.ls(remoteFolder);
            LOGGER.debug("Listed remote Directory: {}", remoteFolder);
            dirv.forEach(p -> {
                if (!p.getFilename().startsWith("."))
                    dir.add(remoteFolder + "/" + p.getFilename());
            });
        } catch (SftpException e) {
            LOGGER.error("Cannot List Remote Directory: {}", e);
        }
        return dir;
    }

    /**
     * Download all files in Current Directory to current local Directory.
     * Keep the filenames.
     */
    public void downloadAllFilesCurrentRemoteDir() {
        List<String> files = listCurrentRemoteDir();
        LOGGER.info("Staring to Download the following files: {}", files);
        files.forEach(f -> {
            downloadFile(f);
        });
        LOGGER.info("Download finished for files: {}", files);
    }

    /**
     * Will download the remote files to the current local directory
     * @param remoteDir
     */
    public void downloadAllFilesRemoteDir(String remoteDir) {
        List<String> files = listRemoteDir(remoteDir);
        LOGGER.info("Staring to Download the following files: {}", files);
        files.forEach(f -> {
                String[] dstFile = f.split("/"); // Here is the magic
                downloadFile(f, dstFile[dstFile.length-1]);
        });
        LOGGER.info("Download finished for files: {}", files);
    }

    /**
     * Download a file by name.
     * Might lead to an issue, if with filepath.
     *
     * @param fileName
     */
    public void downloadFile(String fileName){
        try {
            LOGGER.info("Download file: {}", fileName);
            channelSftp.get(fileName, fileName);
            LOGGER.info("Download finished for file: {}", fileName);
        } catch (SftpException e) {
            LOGGER.error("Cannot Download file: {}", e);
        }
    }

    /**
     * Download a file
     * @param fileName
     * @param localName
     */
    public void downloadFile(String fileName, String localName){
        try {
            LOGGER.info("Download file: {}", fileName);
            channelSftp.get(fileName, localName);
            LOGGER.info("Download finished for file: {}", fileName);
        } catch (SftpException e) {
            LOGGER.error("Cannot Download file: {}", e);
        }
    }

    public void changeLocalDir(String folder) {
        try {
            channelSftp.lcd(folder);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        LOGGER.info("Starting to logout");
        channelSftp.exit();
        session.disconnect();
        LOGGER.info("Logout done");
    }

    private static Identity createIndentity(JSch jsch, byte[] privateKey) throws JSchException {
        KeyPair keyPair = KeyPair.load(jsch, privateKey, null);
        return IdentityKey.createIdentity(jsch, keyPair, "example");
    }


    @Override
    public void close() throws IOException {
        disconnect();
    }
}
