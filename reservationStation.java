import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by nana on 2016/11/22.control the size the reservation station
 */
public class reservationStation {
    int  N = 10;
    int size;
    Queue<rsEntry> queue;
    public reservationStation() {
        this.queue = new LinkedList<rsEntry>();
        this.size = 0;
    }
    public boolean add(rsEntry r){
        if (size >= N) return false;
        queue.add(r);
        size++;
        return true;
    }
    rsEntry getEntry(int Dest) {
        for(rsEntry rs : queue) {
            if(rs.Dest == Dest)    return rs;
        }
        return null;
    }
    void poll() {
        queue.poll();
        size--;
        for(rsEntry rs : queue) {
            rs.Dest--;
            if(rs.Qj > 0) {
                rs.Qj--;
            }
            if(rs.Qk > 0) {
                rs.Qk--;
            }
        }
    }
}
