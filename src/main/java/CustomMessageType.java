import java.io.Serializable;

public enum CustomMessageType implements Serializable {
    //client's requests
    UPLOAD_FILE,
    DELETE_FILE,
    DOWNLOAD_FILE,
    CHANGE_DIRECTORY,
    DISCONNECT,

    //server responses
    REQUEST_ACCEPT,
    REQUEST_REJECT,

    //server messages
    SERVER_MSG,

    //data stream headers
    DOWNLOAD_DATA,
    UPLOAD_DATA;
}
