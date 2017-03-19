package logic;


import network.MulticastChannelWrapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static MulticastChannelWrapper mc= null;
    public static MulticastChannelWrapper mdb= null;
    public static MulticastChannelWrapper mdr= null;
    public static String version = null;
    public static int senderID = -1;



    // the fileId should include encrypted the fileName, modified date and owner(Peer Id)
    public String sha256(String fileName, long date, Integer idOwner){



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
}
