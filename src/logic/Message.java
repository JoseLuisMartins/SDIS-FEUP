package logic;

//header
//<MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
//body

import network.MulticastChannelWrapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Message {

    public static final char CR = 0xD;
    public static final char LF = 0xA;
    public static final String CRLF = "" + CR + LF;
    public static final int HEADER = 0;
    public static final int BODY = 1;
    public static final char SEPARATOR =  ' ';

    private byte[] message;
    private byte[] messageHeader;
    private byte[] messageBody;


    //header params
    private MessageType type;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replicationDeg;




    //receives a message and parses it
    //not checking message integrity at the moment
    public Message(byte[] msg) {

        this.message=msg;
        String msgString = new String(msg,StandardCharsets.US_ASCII);

        String[] messageFields = msgString.split(CRLF + CRLF );


        String[] headerFields = messageFields[HEADER].split(" +");

        this.type = MessageType.valueOf(headerFields[0]);
        this.message = msg;
        this.version = headerFields[1];
        this.senderId = Integer.parseInt(headerFields[2]);
        this.fileId = headerFields[3];

        //parse header
        switch (type){
            case PUTCHUNK://PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
                this.chunkNo = Integer.parseInt(headerFields[4]);
                this.replicationDeg = Integer.parseInt(headerFields[5]);
                break;
            case GETCHUNK://GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            case CHUNK://CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
            case REMOVED://REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            case STORED://STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
                this.chunkNo = Integer.parseInt(headerFields[4]);
                break;
            case DELETE://DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
            case DELETED_CONFIRMATION:
                break;
        }


        this.messageHeader = messageFields[HEADER].toString().getBytes(StandardCharsets.US_ASCII);

        //parse body
        if(type == MessageType.CHUNK || type == MessageType.PUTCHUNK) {

            int bodyPos = messageHeader.length + 4; // 13 ,10,13,10
            this.messageBody = Arrays.copyOfRange(msg,bodyPos,msg.length);
        }


    }


    //receives a message type and creates it
    //TODO refractor-> make method for each type
    public Message(MessageType type, String version, int senderId, String fileId, int chunkNo, int replicationDeg,byte[] msgBody) {
        this.type = type;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.messageBody = msgBody;


        StringBuilder sb = new StringBuilder().append(type.toString()).append(SEPARATOR).append(version).append(SEPARATOR).append(senderId).append(SEPARATOR).append(fileId).append(SEPARATOR);


        //header
        switch (type){
            case PUTCHUNK:
                sb.append(chunkNo).append(SEPARATOR).append(replicationDeg).append(SEPARATOR).append(CRLF).append(CRLF);
                break;
            case GETCHUNK:
            case CHUNK:
            case REMOVED:
            case STORED:
                sb.append(chunkNo).append(SEPARATOR).append(CRLF).append(CRLF);
                break;
            case DELETE:
            case DELETED_CONFIRMATION:
                sb.append(CRLF).append(CRLF);
                break;
        }

        this.messageHeader = sb.toString().getBytes(StandardCharsets.US_ASCII);


        //body
        if((type == MessageType.CHUNK && msgBody != null) || type == MessageType.PUTCHUNK) {
            this.message = new byte[this.messageHeader.length + this.messageBody.length];
            System.arraycopy(this.messageHeader, 0, this.message, 0, this.messageHeader.length);
            System.arraycopy(this.messageBody, 0, this.message, this.messageHeader.length, this.messageBody.length);
        }else
            this.message = this.messageHeader;


    }


    public Message(MessageType type, String version, int senderId,String fileId) {
        this(type, version, senderId, fileId, -1, -1,null);
    }

    public Message(MessageType type, String version, int senderId,String fileId, int chunkNo) {
        this(type, version, senderId, fileId, chunkNo, -1,null);
    }

    public Message(MessageType type, String version, int senderId,String fileId, int chunkNo,byte[] msgBody) {
        this(type, version, senderId, fileId, chunkNo, -1,msgBody);
    }



    public byte[] getMessage() {
        return message;
    }


    public MessageType getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }

    public void send(MulticastChannelWrapper channel)  {
        try {
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, channel.getAddress(), channel.getPort());
            Utils.peerSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public String toString() {

        String str= new String(message);
        return "Message{" +
                "message='" + str + '\'' +
                ", messageHeader='" + messageHeader + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", type=" + type +
                ", version='" + version + '\'' +
                ", senderId=" + senderId +
                ", fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDeg='" + replicationDeg + '\'' +
                '}';
    }


}
