package logic;


import file.ChunkID;

public class ChunkState implements Comparable<ChunkState> {
    private ChunkID chunkID;
    private int deletePretencion;

    public ChunkState(ChunkID chunkID, int deletePretencion)  {
        this.chunkID = chunkID;
        this.deletePretencion = deletePretencion;
    }

    public ChunkID getChunkID() {
        return chunkID;
    }

    public int getDeletePretencion() {
        return deletePretencion;
    }

    @Override
    public int compareTo(ChunkState chunkState) {
        int compareChunk = ((ChunkState) chunkState).getDeletePretencion();

        //descending order
        return  compareChunk - this.deletePretencion;
    }




    @Override
    public String toString() {
        return "ChunkState{" +
                "chunkID=" + chunkID.toString() +
                ", deletePretencion=" + deletePretencion +
                '}' + '\n';
    }
}
