package samplesftpclients;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * An example wrapper class for JCraft.
 */
public final class JCraft {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCraft.class);

    private static final String CHANNEL = "sftp";

    private final String username;
    private final String password;
    private final String host;
    private final int port;

    private Session session;
    Channel channel;
    ChannelSftp channelSftp;
    private JSch jsch;

    /**
     * Constructor.
     *
     * @param username
     * @param password
     * @param host
     * @param port
     */
    public JCraft(String username,String password,String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * Connect to the SFTP Server.
     */
    public void connect() {
        LOGGER.info("Connecting to the SFTP-Server: {}", host);
        jsch = new JSch();
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel(CHANNEL);
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            LOGGER.info("Successfully logger in");
        } catch (JSchException e) {
            LOGGER.error("Cannot Login: {}", e);
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
        channel.disconnect();
        session.disconnect();
        LOGGER.info("Logout done");
    }


    public static void main(String[] args) {
        if (args.length < 4){
            System.out.println("Provide the required args, username, password, host and port");
        }

        JCraft jCraft = new JCraft(args[0], args[1], args[2], Integer.parseInt(args[3]));
        jCraft.connect();
        //jCraft.changeLocalDir("/tmp"); // only required if target is without path
        List<String> files = jCraft.listRemoteDir("/vdfs-delivery/tkadt/DATA"); // Not good
        files.forEach(System.out::println);
        files.forEach(f -> jCraft.downloadFile(f, "/tmp/test3"));  // Works when the dest filename is named
        jCraft.changeRemoteDir("/vdfs-delivery/tkadt/DATA");
        // and get the filename, without
        //files.forEach(f -> jCraft.downloadFile("/tmp", f, "test"));

        jCraft.disconnect();

    }

}
