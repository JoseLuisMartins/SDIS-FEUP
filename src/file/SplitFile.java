package file;


import logic.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SplitFile {

    private File file;
    private ArrayList<Chunk> chunksList;
    private String fileId;

    public SplitFile(File file) throws IOException {

        this.file = file;
        this.chunksList = new ArrayList<Chunk>();


        split();
        debug();
    }

    private void split() throws IOException {

        byte[] data = loadFile(this.file);
        this.fileId = Utils.sha256(this.file);
        splitData(data);


    }

    public String getFileId() {
        return fileId;
    }

    // loads the data of the file to an array of bytes
    private byte[] loadFile(File file) throws IOException {

        FileInputStream inputStream = new FileInputStream(file);

        byte[] fileData = new byte[(int) file.length()];

        inputStream.read(fileData);
        inputStream.close();

        return fileData;
    }

    // divides all the data in chunks
    private void splitData(byte[] data){
        // For debug is better a small number, but the real number should be 64000
        int x = 64000;//64000


        int chunkNo = 0;
        // all the chunks should be with the 64KByte, except the last one that could store less.
        for (int i = 0; i < data.length - x + 1; i += x) {
            Chunk chunk = new Chunk(this.fileId, chunkNo, Arrays.copyOfRange(data, i, i + x));
            chunksList.add(chunk);
            chunkNo++;
        }


        if (data.length % x != 0) {
            Chunk ck = new Chunk(this.fileId, chunkNo, Arrays.copyOfRange(data, data.length - data.length % x, data.length));
            chunksList.add(ck);
        }else{
            chunkNo++;
            Chunk ck1 = new Chunk(this.fileId, chunkNo, new byte[0]);
            chunksList.add(ck1);
        }

    }

    public ArrayList<Chunk> getChunksList() {
        return chunksList;
    }

    private void debug(){
        System.out.println("SIZE: " + getChunksList().size());
        System.out.println("File: " + file.getName());
        System.out.println("Path: " + file.getAbsolutePath());
        System.out.println("File ID: " + fileId);
        System.out.println("-------------------------------------");
    }
}
