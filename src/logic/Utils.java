package logic;


import network.MulticastChannelWrapper;

import java.net.DatagramSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {
    public static MulticastChannelWrapper mc= null;
    public static MulticastChannelWrapper mdb= null;
    public static MulticastChannelWrapper mdr= null;
    public static String version = null;
    public static int peerID = -1;
    public static DatagramSocket peerSocket=null;
    public static String CHUNKS_FOLDER_NAME;
    public static Metadata metadata;

    //PUTCHUNK


    // the fileId should include encrypted the fileName, modified date and owner(Peer Id)
    public static String sha256(String fileName, long date, Integer idOwner){



        String text = new StringBuilder().append(fileName).append(date).append(idOwner).toString();

        MessageDigest mDigest = null;

        try {
            mDigest = MessageDigest.getInstance("SHA-256");

            byte[] result = mDigest.digest(text.getBytes());

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static void sleepRandomTime(int limit){
        Random rn = new Random();
        int time = rn.nextInt(limit);

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static  void sleepSpecificTime(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
