package management;

import file.Chunk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

// this class will be used to file management
public class FileManager {

    // save the chunk pass by argument
    public void saveChunk(Chunk ck) throws FileNotFoundException {

        createFolder();

        File file = new File("bin\\" + ck.toString());

        FileOutputStream fout = new FileOutputStream(file);

        try {
            fout.write(ck.getContent());
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete the chunk pass by argument
    public void deleteChunk(Chunk ck){


        deleteFile("bin\\" + ck.toString());

        // it is necessary to free space on disk

    }

    // restore a file
    public void restoreFile(ArrayList<Chunk> chunks, String fileName) throws IOException {
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
    public void deleteFile(String fileName){

        File file = new File(fileName);

        if(!file.exists()){
            System.out.println("The file " + fileName + "don't exist");
            return;
        }

        file.delete();
    }

    private void createFolder(){

        File dir = new File("bin\\");

        if(!dir.exists()){
            dir.mkdir();
        }
    }
}
