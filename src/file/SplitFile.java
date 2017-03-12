package file;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SplitFile {

    private File file;
    private ArrayList<Chunk> chunksList;

    public SplitFile(File file) throws IOException {

        this.file = file;
        this.chunksList = new ArrayList<Chunk>();

        split();
    }

    private void split() throws IOException {

        byte[] data = loadFile(this.file);
        splitData(data);
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
        int x = 64000;

        int chunkNo = 0;
        // all the chunks should be with the 64KByte, except the last one that could store less.
        for (int i = 0; i < data.length - x + 1; i += x) {
            Chunk chunk = new Chunk("teste", chunkNo, Arrays.copyOfRange(data, i, i + x), 3);
            chunksList.add(chunk);
            chunkNo++;
        }


        if (data.length % x != 0) {
            Chunk ck = new Chunk("teste", chunkNo, Arrays.copyOfRange(data, data.length - data.length % x, data.length), 3);
            chunksList.add(ck);
        }
    }

    public ArrayList<Chunk> getChunksList() {
        return chunksList;
    }
}
