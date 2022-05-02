package file;

import java.io.Serializable;

public class FileInfo implements Serializable {

    public enum FileType{
        DIR,
        FILE
    }

    private final String fileName;
    private final long fileSize;
    private final FileType fileType;

    public FileInfo(String fileName, long fileSize, FileType fileType){
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public long getFileSize(){
        return fileSize;
    }

    public String getFileName(){
        return fileName;
    }

    public FileType getFileType(){
        return fileType;
    }
}
