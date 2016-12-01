/**
 * Created by nana on 2016/11/22.
 */
public class rsEntry {
    //set by default
    boolean Busy = true;
    //need to set by code, Vk and Qk are initialed with -1
    int Vj;
    int Vk = -1;
    int Qj = -1;
    int Qk = -1;
    int A;
    //set in constructor
    int addr;
    int Dest;
    String command;
    String Op;

    //to keep SW and LW in the execute state for one more cycle
    int wait = 0;

    //only  need to write back once
    boolean done = false;
   
    boolean writen = false;
    
    rsEntry(int add, String ins,String funct, int dest) {
        addr = add;
        Dest = dest;
        Op = funct;
        command = ins;
    }
    boolean checkVj(){
        if(Qj == 0) {
            return true;
        }
        return false;
    }
    boolean checkVk(){
        if(Qk == 0) {
            return true;
        }
        return false;
    }
}
