package management;

//the role of this class is to manage the disk
public class DiskManager {

    private int maxSize = 700000;
    private int occupiedSpace;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getOccupiedSpace() {
        return occupiedSpace;
    }

    public int getFreeSpace(){
        return maxSize - occupiedSpace;
    }

    //define strategy : upload all the information on the beguin?

    // the server can reclaim disk space  on peers
    public void reclaimDisk(){

    }
}
