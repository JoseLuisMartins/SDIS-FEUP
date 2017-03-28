package management;

import file.Chunk;
import file.ChunkID;
import logic.Metadata;
import logic.Utils;
import sun.misc.IOUtils;

import java.io.*;
import java.util.ArrayList;

import static logic.Utils.CHUNKS_FOLDER_NAME;
import static logic.Utils.metadata;

// this class will be used to file management
public class FileManager {


    // save the chunk pass by argument
    public static void saveChunk(Chunk ck) {


        createFolder(CHUNKS_FOLDER_NAME);

        StringBuilder path = new StringBuilder().append(CHUNKS_FOLDER_NAME).append("/").append(ck.getId().getFileID());

        createFolder(path.toString());

        path.append("/").append(ck.getId().getChunkID());

        File file = new File(path.toString());

        try {
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(ck.getContent());
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete the chunk pass by argument
    public static void deleteFileChunks(String fileID){
        StringBuilder path = new StringBuilder().append(CHUNKS_FOLDER_NAME).append("/").append(fileID);
        File folder = new File(path.toString());

        String[]entries = folder.list();
        for(String s: entries){
            File currentFile = new File(folder.getPath(),s);
            currentFile.delete();
        }

        folder.delete();//delete the folder
    }

    public static void deleteChunk(ChunkID chunkId){
        StringBuilder path = new StringBuilder().append(CHUNKS_FOLDER_NAME).append("/").append(chunkId.getFileID());
        File folder = new File(path.toString());

        path.append("/").append(chunkId.getChunkID());
        File chunkFile = new File(path.toString());
        chunkFile.delete();

        String[]entries = folder.list();

        if(entries.length == 0)//if it's the only chunk of that file, eliminate the folder as well
            folder.delete();
    }

    public static byte[]  loadChunk(ChunkID chunkId){

        StringBuilder path = new StringBuilder().append(CHUNKS_FOLDER_NAME).append("/").append(chunkId.getFileID()).append("/").append(chunkId.getChunkID());
        File file = new File(path.toString());


        byte[] bytesArray = new byte[(int) file.length()];

        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytesArray); //read file into bytes[]
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytesArray;

    }

    public static boolean hasFileChunks(String fileID){

        StringBuilder path = new StringBuilder().append(CHUNKS_FOLDER_NAME).append("/").append(fileID);
        File dir = new File(path.toString());

        return dir.exists();
    }

    public static boolean hasChunk(ChunkID chunkID){

        StringBuilder path = new StringBuilder().append(CHUNKS_FOLDER_NAME).append("/").append(chunkID.getFileID()).append("/").append(chunkID.getChunkID());
        File f = new File(path.toString());

        return f.exists();
    }


    // restore a file
    public static void restoreFile(ArrayList<Chunk> chunks, String fileName) throws IOException {
        // Assuming that the chunks already came ordered

        File file = new File(fileName);
        FileOutputStream out = new FileOutputStream(file);

        for(int i = 0 ;  i< chunks.size() ; i++){
            out.write(chunks.get(i).getContent());
        }
        out.flush();
        out.close();

    }


    private static void createFolder(String folderName){

        File dir = new File(folderName);

        if(!dir.exists()){
            dir.mkdir();
        }
    }


    public static  void saveMetadata(){
        File f =new File("metadata" + Utils.peerID);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(metadata);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadMetadata(){


        try {
            File myStatsFile = new File("metadata" + Utils.peerID);


            if(!myStatsFile.exists()){
                myStatsFile.createNewFile();
                Utils.metadata = new Metadata();
                saveMetadata();
            }else{
                FileInputStream fin = new FileInputStream(myStatsFile);
                ObjectInputStream in = new ObjectInputStream(fin);
                Utils.metadata = (Metadata) in.readObject();
                fin.close();
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static long getSizeOfFolder(File folder) {
        long length = 0;

        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getSizeOfFolder(files[i]);
            }
        }
        return length;
    }
    

    public static int getUsedSpace(){
        return 0;
    }


}
