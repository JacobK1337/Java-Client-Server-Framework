package custom_implementations;

import java.io.Serializable;

public enum CustomMessageType implements Serializable {
    NOT_ASSIGNED,

    UPLOAD_FILE,
    DELETE_FILE,
    DOWNLOAD_FILE,
    CHANGE_DIRECTORY,

    //connection operations
    DISCONNECT,

    //verified request on data connection
    DATA_STREAM_VERIFIED,

    //server verification responses
    SERVER_OK,
    SERVER_ERROR,

    //upload file operations
    UPLOAD_ACCEPT,
    UPLOAD_REJECT,
    UPLOAD_DATA,
    UPLOAD_FINISHED
}
