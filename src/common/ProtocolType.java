package common;

import java.io.Serializable;

//Represents the type of protocol
public enum ProtocolType {
    BACKUP,
    RESTORE,
    DELETE,
    RECLAIM,
    STATE;

}
