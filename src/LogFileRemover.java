import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

// > java -jar logrem.jar -path=/path/of/log/folders -maxSizeLimit=3 -logFileExtension=log,out

public class LogFileRemover {

    public static boolean isPresent(String[] extension, String filename) {
        boolean isPresent = false;
        int length = extension.length;
        int count = 0;
        String fileExtension = filename.split("\\.")[filename.split("\\.").length-1];

        for(int i=0; i<length; i++) {
            if(fileExtension.equalsIgnoreCase(extension[i])) {
                count++;
            }
        }
        if(count != 0) {

            isPresent = true;
            System.out.println();
        }
        return isPresent;
    }

    public static String fetchSize(String maxSizeFlag) {
        String size = maxSizeFlag.split("=")[maxSizeFlag.split("=").length-1];
        String format = Character.toString(size.charAt(size.length()-1));
        String sizeValue = size.substring(0, size.length()-1);
        String returnSize = "";

        if(format.equalsIgnoreCase("m")) {
            returnSize = String.valueOf(Double.valueOf(sizeValue)*1024);


        }
        else if(format.equalsIgnoreCase("k")) {
            returnSize = sizeValue;
        }
        else if(format.equalsIgnoreCase("g")) {
            returnSize = String.valueOf(Double.valueOf(sizeValue)*1024*1024);
        }

        return returnSize;
    }

    public static ArrayList<String> findLogs(String folderPath, String[] extensions) {
        ArrayList<String> filesWithDefinedExtensions = new ArrayList<>();
        String filename = "";
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                filename = listOfFiles[i].getName();
                if(isPresent(extensions, filename)) {
                    filesWithDefinedExtensions.add(filename);
                }
            }
        }
        return filesWithDefinedExtensions;
    }

    public static long totalDiskSpace() {
        File[] drivers = File.listRoots();
        long total = 0;

        for(int i=0; i< drivers.length; i++) {
            total = total + new File(drivers[i].toString()).getFreeSpace()/1024/1024;
        }
        return total;
    }

    public static String getSizeOfFile(String filepath) throws IOException {
        String sizeOfFile = "";
        File file = new File(filepath);
        Path path = Paths.get(filepath);
        long bytes = Files.size(path);
        sizeOfFile = String.format("%d", bytes / 1024);
        return sizeOfFile;
    }

    public static void deleteFile(String filepath) {
        File file = new File(filepath);
        file.delete();
    }

        public static int findCountOfAFile(String ex, ArrayList<String> filenames) {
            int count = 0;
            String extensionFromFile;
            for(int i=0; i<filenames.size(); i++) {
                extensionFromFile = filenames.get(i).split("\\.")[filenames.get(i).split("\\.").length-1];
                if(extensionFromFile.equalsIgnoreCase(ex)) {
                    count++;
                }
            }
            return count;
        }

        public static void main(String[] args) throws IOException {
        ArrayList<String> files;
        ArrayList<String> pathsWithFilenames = new ArrayList<>();
        ArrayList<String> deletedFiles = new ArrayList<>();

        int deleteCount = 0;
        int notDeleteCount = 0;
        int totalSize = 0;

        long diskSpaceBefore =  totalDiskSpace(); // in KB
        long diskSpaceAfter;


        String path_flag = args[0];
        String maxSizeLimit_flag = args[1];
        String logFileExtension_flag = args[2];

            if (path_flag.split("=")[0].equalsIgnoreCase("-path") && maxSizeLimit_flag.split("=")[0].equalsIgnoreCase("-maxSizeLimit") && logFileExtension_flag.split("=")[0].equalsIgnoreCase("-logFileExtension")) {
                String path = path_flag.split("=")[1];
                String maxSize = fetchSize(maxSizeLimit_flag);
                System.out.println(maxSize);
                String[] extensions = logFileExtension_flag.split("=")[1].split(",");
                files = findLogs(path, extensions);

                System.out.println("File names with specified extensions");
                System.out.println("====================================");
                for(int i=0; i<files.size(); i++) {
                    System.out.println((i+1) + ". " + files.get(i));
                }

                System.out.println();
                for(int i=0; i<extensions.length; i++) {
                    System.out.println("Total ." + extensions[i] +" files : " + findCountOfAFile(extensions[i], files) );
                }


                for (int i = 0; i < files.size(); i++) {
                    pathsWithFilenames.add(path + "/" + files.get(i));
                }

                int k=0;

                for (String p : pathsWithFilenames) {
                    if (Double.valueOf(getSizeOfFile(p)) >= Double.valueOf(maxSize)) {

                        deletedFiles.add(files.get(k));
                        deleteCount++;
                        totalSize = totalSize + Integer.valueOf(getSizeOfFile(path + "/" + files.get(k)));
                        deleteFile(p);
                    }
                    k++;
                }
                if(deletedFiles.size() != 0) {
                    System.out.println();
                    System.out.println("List of deleted files which are more than " + maxSize +" KB");
                    System.out.println("==============================================================");
                    for(String deletedFile : deletedFiles) {
                        System.out.println(deletedFile);
                    }

                    System.out.println();
                    System.out.println("==============================================================");

                    System.out.println( totalSize + " KB disk space released");
                    System.out.println("==============================================================");
                    System.out.println();
                    System.out.println("Total disk space before clearing logs : " + diskSpaceBefore + " MB");
                    diskSpaceAfter = totalDiskSpace();
                    System.out.println("Total disk space after clearing logs : " + diskSpaceAfter + " MB");


                } else {
                    System.out.println();
                    System.out.println("All files are less than " + maxSize + " KB");
                    System.out.println("No files are deleted");
                }

            } else {
                System.out.println("=============================================================================================================================");
                System.out.println("RUN FAILED");
                System.out.println("=============================================================================================================================");
                System.out.println();
                System.out.println("Please read the guide below for running the tool");
                System.out.println();
                System.out.println("To run the jar file you should have three things");
                System.out.println("\t1)\tpath [ folder path of the log files ]");
                System.out.println("\t2)\tmaxSizeLimit [ file size limit in kilobytes ]");
                System.out.println("\t3)\tlogFileExtension [ extensions of the files you want to delete. extensions should be separated by comma (\",\") ]");
                System.out.println();
                System.out.println("Example: java -jar logrem.jar -path=/path/of/log/folders -maxSizeLimit=3 -logFileExtension=log,out");
                System.out.println("=============================================================================================================================");

            }

    }
}