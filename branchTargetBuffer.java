import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by nana on 2016/11/22.control the size the reservation station
 */
public class branchTargetBuffer{
    int  N = 16;
    int size;
    LinkedList<btbEntry> list;
    public branchTargetBuffer() {
        this.list = new LinkedList<btbEntry>();
        this.size = 0;
    }
    public boolean add(btbEntry r){
        if (size >= N) return false;
        list.add(r);
        size++;
        return true;
    }
    btbEntry getEntry(int pc) {
        for(btbEntry btb : list) {
            if(pc == btb.pc) {
                return btb;
            }
        }
        return null;
    }
}
