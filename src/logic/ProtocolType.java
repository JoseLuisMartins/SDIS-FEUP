package logic;

//Represents the type of protocol
public enum ProtocolType {
    BACKUP("BACKUP"),
    RESTORE("RESTORE"),
    DELETE("DELETE"),
    RECLAIM("RECLAIM"),
    RETRIEVE("RETRIEVE");

    private String name;

    ProtocolType(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
