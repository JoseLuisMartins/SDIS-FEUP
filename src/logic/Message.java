package logic;

//header
//<MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
//body

import java.io.File;

public class Message {

    private String message;
    private String messageHeader;
    private String messageBody;


    //header params
    private MessageType type;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo;
    //This field together with the FileId specifies a chunk in the file. The chunk numbers are integers and should be assigned sequentially starting at 0.
    // It is encoded as a sequence of ASCII characters corresponding to the decimal representation of that number, with the most significant digit first.
    // The length of this field is variable, but should not be larger than 6 chars. Therefore, each file can have at most one million chunks.
    // Given that each chunk is 64 KByte, this limits the size of the files to backup to 64 GByte.
    private String replicationDeg;
    //This field contains the desired replication degree of the chunk. This is a digit, thus allowing a replication degree of up to 9. It takes one byte, which is the ASCII code of that digit.






    //receives a message and parses it
    //not checking message integrity at the moment
    public Message(String msg) {
        this.message=msg;

        String[] splitedMsg = message.split(" +");

        this.type = MessageType.valueOf(splitedMsg[0]);

        this.version = splitedMsg[1];
        this.senderId = Integer.parseInt(splitedMsg[2]);
        this.fileId = splitedMsg[3];

        //parse header
        switch (type){
            case PUTCHUNK://PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
                this.chunkNo = Integer.parseInt(splitedMsg[4]);
                this.replicationDeg = splitedMsg[5];
                break;
            case GETCHUNK://GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            case CHUNK://CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
            case REMOVED://REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            case STORED://STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
                this.chunkNo = Integer.parseInt(splitedMsg[4]);
                break;
            case DELETE://DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
                break;
        }

        //parse body




    }



    //receives a message type and creates it
    public Message(MessageType type, String version, int senderId, String fileId, int chunkNo, String replicationDeg,String msgBody) {
        this.type = type;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.messageBody = msgBody;

        String crlf = Character.toString((char)13) + Character.toString((char)10);
        message = type.toString() + " " + version + " " + senderId + " " + fileId + " ";

        switch (type){
            case PUTCHUNK:
                message += chunkNo + " " + replicationDeg + " "  + crlf + crlf;
                message += " " + messageBody;
                break;
            case GETCHUNK:
            case CHUNK:
            case REMOVED:
            case STORED:
                message += chunkNo + " " + crlf + crlf;

                break;
            case DELETE:
                message += crlf + crlf;
                break;
        }


    }

    @Override
    public String toString() {

        String[] splitedMsg = message.split(" +");

        return "Message{" +
                "message='" + message + '\'' +
                ", messageHeader='" + messageHeader + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", type=" + type +
                ", version='" + version + '\'' +
                ", senderId=" + senderId +
                ", fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDeg='" + replicationDeg + '\'' +
                '}' + "splitLength-> " +  splitedMsg.length;
    }


}
