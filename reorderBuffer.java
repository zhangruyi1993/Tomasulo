import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by nana on 2016/11/22.control the size the reservation station
 */
public class reorderBuffer{
    int  N = 6;
    int size;
    Queue<robEntry> queue;
    public reorderBuffer() {
        this.queue = new LinkedList<robEntry>();
        this.size = 0;
    }
    public boolean add(robEntry r){
        if (size >= N) return false;
        queue.add(r);
        size++;
        return true;
    }
    robEntry get(int h) {
        Queue<robEntry> temp = new LinkedList<>(queue);
        for(int i = 0; i < queue.size(); i ++) {
            robEntry tempEntry = temp.poll();
            if(h == tempEntry.Entry) {
                return tempEntry;
            }
        }
        return null;
    }
    void poll() {
        size--;
        queue.poll();
        for(robEntry rob : queue) {
            rob.Entry--;
        }
    }
}
