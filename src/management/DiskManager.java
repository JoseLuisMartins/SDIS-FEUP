package management;

//the role of this class is to manage the disk
public class DiskManager {

    private int maxSize = 700000;


    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    // the server can reclaim disk space  on peers
    public void reclaimDisk(){

    }
}
