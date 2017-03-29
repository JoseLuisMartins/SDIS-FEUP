package logic;


import common.ProtocolType;
import network.MulticastChannelWrapper;

import java.io.File;
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
    public static String sha256(File f){

        String text = new StringBuilder().append(f.getName()).append(f.lastModified()).append(Utils.peerID).toString();

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

    public static boolean validClientArgs(String[] args){

        if(args.length == 4){
            if(!args[1].equals(ProtocolType.BACKUP.toString())) {
                return false;
            }
            else if (!args[2].matches("^[\\w,\\s-]+\\.[A-Za-z]{3}")){
                return false;
            }
            else if(args[3].matches("\\D")){
                return false;
            }

            return true;
        }else if(args.length == 3) {
            if(args[1].equals(ProtocolType.DELETE.toString()) || args[1].equals(ProtocolType.RESTORE.toString())){
                if (args[2].matches("^[\\w,\\s-]+\\.[A-Za-z]{3}")){
                    return true;
                }else return false;
            }
            else if(args[1].equals(ProtocolType.RECLAIM.toString())) {
                if (args[2].matches("\\d")) {
                    return true;
                } else return false;
            }
            else return false;
        }else if(args.length == 2){
            if(args[1].equals(ProtocolType.STATE.toString())){
                return true;
            }
            else return false;
        }

        return false;

    }


    public static boolean validServiceArgs(String[] args){

        if(args.length == 9){

            //ex: java TestApp 1.0 1 myServer  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224
            if(!args[0].matches("\\d+\\.\\d+")) {
                return false;
            } else if(!args[1].matches("\\d+")){
                return false;
            }else if(!args[2].matches("[a-zA-Z]+")){
                return false;
            }

            for(int i = 3; i < args.length ; i++){
                String[] parts = args[i].split("\\.");

                if(parts.length != 4) return false;

                if(parts[0].matches("\\D+"))return false;


                if(!(Integer.parseInt(parts[0]) >= 224 && Integer.parseInt(parts[0]) <= 255)) return false;

                for(int j = 1 ; j < parts.length ; j++ ){
                    if(parts[j].matches("\\D+"))return false;

                    if(!(Integer.parseInt(parts[j]) >= 0 && Integer.parseInt(parts[j]) <= 255)){
                        return false;
                    }
                }
                i++;

                if(args[i].matches("\\D+"))return false;
            }

            return true;
        }

        return false;
    }
}

