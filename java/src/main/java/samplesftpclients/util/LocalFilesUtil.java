package samplesftpclients.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LocalFilesUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFilesUtil.class);
    private LocalFilesUtil(){}

    public static List<String> getLocalFiles(String localDir) throws IOException {
        Path path = FileSystems.getDefault().getPath(localDir);
        List<String> localFiles = new ArrayList<>();

        Files.walk(path).forEach(p -> {
            if (!p.toString().equals(localDir)){
                String[] pathes = p.toString().split("/");
                if (pathes.length > 0)
                    localFiles.add(pathes[pathes.length -1]);
            }
        });
        return localFiles;
    }

    public static void deleteLocalFiles(String localDir) throws IOException {
        Path path = FileSystems.getDefault().getPath(localDir+"/");
        Files.walk(path).sorted((a, b) -> b.compareTo(a)).forEach((p -> {
            try {
                File f = p.toFile();
                if (f.isFile()) {
                    System.out.println("Deleting File: "+ f.getName() + " with size: "+ f.length() / 1024 + " kb");
                    Files.delete(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public static List<String> getFileNames(List<String> files) {
        List<String> fileNames = new ArrayList<>();
        for (String fqn : files){
            String[] tmp = fqn.split("/");
            fileNames.add(tmp[tmp.length-1]);
        }
        return fileNames;
    }

    public static byte[] getContentFromFile(Path filePath) throws IOException {
        LOGGER.info("Getting content from File: '{}'", filePath);
        return Files.readAllBytes(filePath);
    }

    public static InputStream convertByteArrayToInputStream(byte[] content) {
        return new ByteArrayInputStream(content);
    }
}
