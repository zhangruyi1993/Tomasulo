/**
 * Created by nana on 2016/11/22.
 */
public class btbEntry {
    int id;
    int pc;
    int target;
    int prediction = 0;
    String outcom = "[Entry ";

    btbEntry(int pc, int targetAddr, int id) {
        this.id = id;
        this.pc = pc;
        this.target = targetAddr;
        outcom += id + "]<" + pc + "," + targetAddr + "," + prediction + ">";
    }
    void update() {
    	outcom ="[Entry " + id + "]<" + pc + "," + target + "," + prediction + ">"; 
    }
}
