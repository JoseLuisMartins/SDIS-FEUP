package logic;

//header
//<MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
//body

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import network.MulticastChannelWrapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
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
                break;
        }


        this.messageHeader = messageFields[HEADER].toString().getBytes(StandardCharsets.US_ASCII);

        //parse body

        this.messageBody = messageFields[BODY].toString().getBytes(StandardCharsets.US_ASCII);

    }



    //receives a message type and creates it
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
                sb.append(chunkNo).append(SEPARATOR).append(SEPARATOR).append(CRLF).append(CRLF);
                break;
            case DELETE:
                sb.append(CRLF).append(CRLF);
                break;
        }

        this.messageHeader = sb.toString().getBytes(StandardCharsets.US_ASCII);

        //body
        if(type == MessageType.CHUNK || type == MessageType.PUTCHUNK)
            sb.append(new String(msgBody,StandardCharsets.US_ASCII));


        this.message = sb.toString().getBytes(StandardCharsets.US_ASCII);

    }

    public byte[] getMessage() {
        return message;
    }

    public void send(MulticastChannelWrapper channel) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, channel.getAddress(), channel.getPort());
        Utils.peerSocket.send(sendPacket);
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
