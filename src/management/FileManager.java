package management;

import file.Chunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static logic.Utils.CHUNKS_FOLDER_NAME;

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
    public static void deleteChunk(Chunk ck){


        StringBuilder path = new StringBuilder().append(CHUNKS_FOLDER_NAME).append(ck.toString());
        deleteFile(path.toString());

        // it is necessary to free space on disk

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

    // delete a file with the name pass by argument
    public static void deleteFile(String fileName){

        File file = new File(fileName);

        if(!file.exists()){
            System.out.println("The file don't exist!");
            return;
        }

        file.delete();
    }

    private static void createFolder(String folderName){

        File dir = new File(folderName);

        if(!dir.exists()){
            dir.mkdir();
        }
    }
}
