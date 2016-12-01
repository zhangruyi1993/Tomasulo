/**
 * Created by nana on 2016/11/22.
 */
public class robEntry {
    // set when construct
    int Entry;
    String command;
    boolean isBranch = false;
    // set by default
    boolean busy = true;
    boolean ready = false;
    // need to be set in code
    int Destination = -1;
    int value;
    int misPredict = 0; //0 for predict right, -1 for non-taken when predict taken, 1 for taken when predict non-taken

    robEntry(Instruction ins, int entry) {
        Entry = entry;
        command = ins.simIns;
        String funct = ins.function;
        if(funct.equals("J") | funct.equals("BEQ") | funct.equals("BNE") | funct.equals("BLEZ")
                | funct.equals("BGTZ") | funct.equals("BLTZ") | funct.equals("BGEZ")) {
            isBranch = true;
        }
    }
}