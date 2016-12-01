import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by nana on 2016/11/22.
 */
public class Tomasulo {
    static int pc = 600;
    static int cycleNum = 0;
    static Queue<Instruction> IQ = new LinkedList<>();;
    static reservationStation RS = new reservationStation();;   //size 10
    static reorderBuffer ROB = new reorderBuffer();;       //size 6
    static branchTargetBuffer BTB = new branchTargetBuffer();;  //size 16
    static register[] Registers;    //size 32
    static int[] DataSegment;      //size 10

    static HashMap<Integer, Instruction> commands =  new HashMap<>();//commonds is hashmap so we can easily get the instruction from there adress
    static int btbid = 0;//use to keep track on the btb id
    static int robid = 0;//use to keep track on the rob id
    static boolean exit = false;
    static boolean mispre = false;
    static boolean isfull = false;

    //constructor
    Tomasulo() {
        //initial the register to be all zeros
        Registers = new register[32];
        for (int i = 0; i < 32; i++) {
            Registers[i] = new register();
        }
        //initial the memory to be all zeros
        DataSegment = new int[10];
        for(int i = 0; i < 10; i++) {
            DataSegment[i] = 0;
        }
    }
    //simulator
    void simulate(BufferedWriter out) {
        do {
            cycle();
            print(out);
        } while(!exit);
    }
    //do this functions in order per cycle
    void cycle() {
        commit();//System.out.println("ROB size is: " + ROB.queue.size());
        writeback();//System.out.println("ROB size is: " + ROB.queue.size());
        execute();//System.out.println("ROB size is: " + ROB.queue.size());
        issue();//System.out.println("ROB size is: " + ROB.queue.size());
        fetch();//System.out.println("ROB size is: " + ROB.queue.size());
        cycleNum++;
    }
    //print the tomasulo every cycle
    void print(BufferedWriter out) {
        try {
            //write cycle number
            out.write("Cycle <" + Integer.toString(cycleNum)  + ">:" + "\r\n");
            //write IQ
            out.write("IQ:" + "\r\n");
            for(Instruction ins : IQ) {
                out.write(ins.simIns + "\r\n");
            }
            //write RS
            out.write("RS:" + "\r\n");
            for(rsEntry rs : RS.queue) {
                out.write(rs.command + "\r\n");
            }
            //write ROB
            out.write("ROB:" + "\r\n");
            for(robEntry rob : ROB.queue) {
                out.write(rob.command + "\r\n");
            }
            //write RTB
            out.write("BTB:" + "\r\n");
            for(btbEntry btb : BTB.list) {
                out.write(btb.outcom + "\r\n");
            }
            //write Registers
            out.write("Registers:" + "\r\n");
            String temp = "R00:";int k = 0; temp += printRegisters(k); out.write(temp + "\r\n");
            temp = "R08:"; k = 8; temp += printRegisters(k); out.write(temp + "\r\n");
            temp = "R16:"; k = 16; temp += printRegisters(k); out.write(temp + "\r\n");
            temp = "R24:"; k = 24; temp += printRegisters(k); out.write(temp + "\r\n");
            //write Data Segment
            out.write("Data Segment:" + "\r\n");
            temp = "716:";
            for(int i = 0; i < 10; i++) {
                temp += "\t"  + DataSegment[i];
            }
            out.write(temp + "\r\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //print one line in register
    String printRegisters(int k) {
        String result = "";
        for(int i = k,j = 0;j < 8; i++, j++ ) {
            result += "\t" + Registers[i].value;
        }
        return result;
    }
    void commit() {
        if(ROB.queue.size() > 0) {
            robEntry rob = ROB.queue.peek(); //get the top robEntry of the rob table
            rsEntry rs = RS.getEntry(rob.Entry);
            if(rs != null && BTB.getEntry(rs.addr) == null && rs.Op.equals("J")) {
                btbEntry btb = new btbEntry(rs.addr, rs.A, ++btbid);
                btb.prediction = 1;
                btb.update();
                BTB.add(btb);
                mispre = true;
            }
            if (rob.ready) {
                if(rob.command.length() > 6 && rob.command.substring(1,6).equals("BREAK")){
                    exit = true;
                }
                //check branch
                if (rob.isBranch) {
                    if(rob.misPredict != 0) {
                        flush();
                        mispre = true;
                        //update the pc
                        if (rob.misPredict == -1) {
                            pc = rs.addr + 4;
                        } else if (rob.misPredict == 1) {
                            pc = rob.value;
                        }
                    }
                } else if (rob.command.substring(1, 3).equals("SW")) {
                    DataSegment[(rob.Destination - 716)/4] = rob.value;
                } else if(!rob.command.substring(1,4).equals("NOP") && !rob.command.substring(1,6).equals("BREAK")) {
                    Registers[rob.Destination].value = rob.value;
                    Registers[rob.Destination].Busy = false;
                    Registers[rob.Destination].Reorder = 0;
                }
                rob.busy = false;
                if(ROB.queue.size() == ROB.N) {
            		isfull = true;
            	}
                ROB.poll();
                RS.poll();
                robid--;
                for(register reg : Registers){
                    if(reg.Reorder > 0) {
                        reg.Reorder--;
                    }
                }
            }
        }
    }
    void writeback() {
    	if(mispre) {
    		return;
    	}
        //update the rob from the un-busy rs first, then broadcast it to the RS
        for(rsEntry rs : RS.queue) {
            robEntry rob = ROB.get(rs.Dest);
            //if this rob is already ready, break
            if(rob.ready) {
                continue;
            }
            if(!rs.Busy && !rs.done) {
                if(rs.Op.equals("LW") && rs.wait == 2) {
                	rob.value = DataSegment[(rs.A - 716)/4];
                    rs.wait = 0;
                    continue;
                }
                if(!rs.Op.equals("SW") && !rs.Op.equals("LW")) {
                    rs.done = true;
                }
                //update the rob
                switch (rs.Op) {
                    case "SW":
                        if(rs.checkVk()) {
                            rob.Destination = rs.A;
                            rob.value = rs.Vk;
                            rob.ready = true;
                            rs.done = true;
                        }
                        break;
                    case "LW" :
                        if(checkFormerSW(rs.addr)) {
                            rob.ready = true;
                            rs.done =true;
                        }
                        break;
                    default:
                        rob.value = rs.A;
                        rob.ready = true;
                        break;
                }
                //broadcast to the reservation station
                for(rsEntry waitRS : RS.queue) {
                	if(waitRS == rs) {
                		continue;
                	}
                    if(waitRS.Qj == rs.Dest) {
                        waitRS.Vj = rob.value;
                        waitRS.Qj = 0;
                        rs.writen = true;
                    }
                    if(waitRS.Qk == rs.Dest) {
                        waitRS.Vk = rob.value;
                        waitRS.Qk = 0;
                        rs.writen = true;
                    }
                }
            }
        }
    }
    //check if this LW instruction has any store in rob has the same address as him
    boolean checkFormerSW(int addr) {
        for(robEntry rob : ROB.queue) {
            rsEntry rs = RS.getEntry(rob.Entry);
            if(rs.A == addr && rs.Op.equals("SW"))  return false;
        }
        return true;
    }
    void execute() {
    	if(mispre) {
    		return;
    	}
        //loop every entry in reservation station to see if any one can be execute
        for(rsEntry rs : RS.queue) {
        	if(rs.writen) {
        		rs.writen = false;
        		continue;
        	}
            //check sw and lw if is delay for a cycle
            if(rs.wait == 1) {
                rs.wait = 2;
                break;
            }
            robEntry rob = ROB.get(rs.Dest);
            //only execute the busy rsEntry
            if(rs.Busy) {
                btbEntry btb = BTB.getEntry(rs.addr);
                if(btb == null && !rs.Op.equals("J") && BTB.size  + 1 < BTB.N && rob.isBranch) {
                    btb = new btbEntry(rs.addr, rs.addr + 4 + rs.A, ++btbid);
                    BTB.add(btb);
                }
                switch(rs.Op){
                    case "J":
                        if(btb == null) {
                            rob.misPredict = 1;
                            rob.value = rs.A;
                        }
                        rs.Busy = false;
                        rob.ready =true;
                        break;
                    case "BEQ":
                        if(rs.checkVj() && rs.checkVk()) {
                            btb = BTB.getEntry(rs.addr);
                            if(rs.Vj != rs.Vk && btb.prediction == 1) { //non-taken but predict taken
                                btb.prediction = 0;
                                rob.misPredict = -1;
                            } else if(rs.Vj == rs.Vk && btb.prediction == 0){//taken but predict non-taken
                                btb.prediction = 1;
                                rob.misPredict = 1;
                                pc = btb.target;
                            }
                            rs.Busy = false;
                            rob.ready =true;
                            rob.value = btb.target;
                        }
                        break;
                    case "BNE":
                        if(rs.checkVj() && rs.checkVk()) {
                            btb = BTB.getEntry(rs.addr);
                            if(rs.Vj == rs.Vk && btb.prediction == 1) { //non-taken but predict taken
                                btb.prediction = 0;
                                rob.misPredict = -1;
                            } else if(rs.Vj != rs.Vk && btb.prediction == 0){//taken but predict non-taken
                                btb.prediction = 1;
                                rob.misPredict = 1;
                                pc = btb.target;
                            }
                            rs.Busy = false;
                            rob.ready =true;
                            rob.value = btb.target;
                        }
                        break;
                    case "BLEZ":
                        if(rs.checkVj()) {
                            btb = BTB.getEntry(rs.addr);
                            if(rs.Vj > 0 && btb.prediction == 1) { //non-taken but predict taken
                                btb.prediction = 0;
                                rob.misPredict = -1;
                            } else if(rs.Vj <= 0 && btb.prediction == 0){//taken but predict non-taken
                                btb.prediction = 1;
                                rob.misPredict = 1;
                                pc = btb.target;
                            }
                            rs.Busy = false;
                            rob.ready =true;
                            rob.value = btb.target;
                        }
                        break;
                    case "BGTZ":
                        if(rs.checkVj()) {
                            btb = BTB.getEntry(rs.addr);
                            if(rs.Vj <= 0 && btb.prediction == 1) { //non-taken but predict taken
                                btb.prediction = 0;
                                rob.misPredict = -1;
                            } else if(rs.Vj > 0 && btb.prediction == 0){//taken but predict non-taken
                                btb.prediction = 1;
                                rob.misPredict = 1;
                                pc = btb.target;
                            }
                            rs.Busy = false;
                            rob.ready =true;
                            rob.value = btb.target;
                        }
                        break;
                    case "ADDI":
                    case "ADDIU":
                        if(rs.checkVj()) {
                            rs.A += rs.Vj;
                            rs.Busy = false;
                        }
                        break;
                    case "SLTI":
                        if(rs.checkVj()) {
                            if(rs.Vj < rs.Vk)   rs.A  = 1;
                            else    rs.A  = 0;
                            rs.Busy = false;
                        }
                        break;
                    case "LW":
                        if(rs.checkVj() && checkLWSW(rs)) {
                            rs.A += rs.Vj;
                            rs.Busy = false;
                        }
                        break;
                    case "SW":
                        if(rs.checkVj() && checkLWSW(rs)) {
                            rs.A += rs.Vj;
                            rs.Busy = false;
                        }
                        break;
                    case "BLTZ":
                        if(rs.checkVj()) {
                            btb = BTB.getEntry(rs.addr);
                            if(rs.Vj >= 0 && btb.prediction == 1) { //non-taken but predict taken
                                btb.prediction = 0;
                                rob.misPredict = -1;
                            } else if(rs.Vj < 0 && btb.prediction == 0){//taken but predict non-taken
                                btb.prediction = 1;
                                rob.misPredict = 1;
                                pc = btb.target;
                            }
                            rs.Busy = false;
                            rob.ready =true;
                            rob.value = btb.target;
                        }
                        break;
                    case "BGEZ":
                        if(rs.checkVj()) {
                            btb = BTB.getEntry(rs.addr);
                            if(rs.Vj < 0 && btb.prediction == 1) { //non-taken but predict taken
                                btb.prediction = 0;
                                rob.misPredict = -1;
                            } else if(rs.Vj >= 0 && btb.prediction == 0){//taken but predict non-taken
                                btb.prediction = 1;
                                rob.misPredict = 1;
                                pc = btb.target;
                            }
                            rs.Busy = false;
                            rob.ready =true;
                            rob.value = btb.target;
                        }
                        break;
                    case "SLL":
                        if(rs.checkVk()) {
                            rs.A = rs.Vk << rs.A;
                            rs.Busy = false;
                        }
                        break;
                    case "SLR":
                        if(rs.checkVk()) {
                            rs.A = rs.Vk >>> rs.A;
                            rs.Busy = false;
                        }
                        break;
                    case "SRA":
                        if(rs.checkVk()) {
                            rs.A = rs.Vk >> rs.A;
                            rs.Busy = false;
                        }
                        break;
                    case "ADD":
                    case "ADDU":
                        if(rs.checkVj() && rs.checkVk()) {
                            rs.A = rs.Vj + rs.Vk;
                            rs.Busy = false;
                        }
                        break;
                    case "SUB":
                    case "SUBU":
                        if(rs.checkVj() && rs.checkVk()) {
                            rs.A = rs.Vj - rs.Vk;
                            rs.Busy = false;
                        }
                        break;
                    case "AND":
                        if(rs.checkVj() && rs.checkVk()) {
                            rs.A = rs.Vj & rs.Vk;
                            rs.Busy = false;
                        }
                        break;
                    case "OR":
                        if(rs.checkVj() && rs.checkVk()) {
                            rs.A = rs.Vj | rs.Vk;
                            rs.Busy = false;
                        }
                        break;
                    case "XOR":
                        if(rs.checkVj() && rs.checkVk()) {
                            rs.A = rs.Vj ^ rs.Vk;
                            rs.Busy = false;
                        }
                        break;
                    case "NOR":
                        if(rs.checkVj() && rs.checkVk()) {
                            rs.A = ~(rs.Vj| rs.Vk);
                            rs.Busy = false;
                        }
                        break;
                    case "SLT":
                    case "SLTU":
                        if(rs.checkVj() && rs.checkVk()){
                            if(rs.Vj < rs.Vk){
                                rs.A = 1;
                            }else{
                                rs.A = 0;
                            }
                            rs.Busy = false;
                        }
                        break;
                }
                if(btb != null) {
                	btb.update();
                }
            }
        }
    }
    //when mispredict, flush
    void flush(){
        IQ.clear();
        ROB.queue.clear();
        ROB.size = 0;
        RS.queue.clear();
        RS.size = 0;
        robid = 1;
        for(register reg : Registers){
            if(reg.Reorder != -1) {
                reg.Reorder = -1;
            }
        }
    }
    boolean checkLWSW(rsEntry rs) {
        for(robEntry rob : ROB.queue) {
            if(rs.Dest == rob.Entry)
                return true;
            if(rob.command.substring(1,3).equals("SW") && rob.Destination == rs.Vj + rs.A)
                return false;
            if(rob.command.substring(1,3).equals("LW") && rob.value == rs.Vj + rs.A)
                return false;
        }
        return true;
    }
    //issue state
    void issue() {
    	if(mispre) {
    		return;
    	}
    	if(isfull) {
    		isfull = false;
    		return;
    	}
        //stop when there is nothing in the instruction queue
        if(IQ.size() == 0) {
            return;
        }
        //issue the instruction if RS and ROB is not full
        if(RS.size < RS.N && ROB.size < ROB.N) {
            Instruction ins = IQ.poll();       //the instruction needed to be added into the IQ
            rsEntry rs = new rsEntry(ins.addr,ins.simIns, ins.function, robid + 1);    //the rs that needed to be added into reservation station, busy is set when construct
            robEntry rob = new robEntry(ins, ++robid); //the rob that needed to be added into the reorder buffer, busy, ready set when construct
            ROB.add(rob);
            //if the instruction is NOP or BREAK, make it ready to be committed right after being issued
            if(ins.function.equals("NOP") | ins.function.equals("BREAK")){
                rob.ready = true;
                return;
            }
            //add them to the table
            RS.add(rs);
            //set wait for lw
            if (ins.function.equals("LW")) {
                rs.wait = 1;
            }
            //all the element that need to be considered adding to the rs and rob
            String funct = ins.function;
            int irs = ins.irs;
            int irt = ins.irt;
            int ird = ins.ird;
            int isa = ins.isa;
            int ioffset = ins.ioffset;
            int imi = ins.imi;
            //all common properties are set default or in the rs rob constructor, so we only need to consider the different part
            //check if rs should go to Vj, if yes set Vj, Qj in reservation station
            if(irs != Integer.MIN_VALUE) {
                if(Registers[ins.irs].Busy) {
                    int h = Registers[irs].Reorder;//getting the target register
                    robEntry thisRob = ROB.get(h); // if the this register is busy, then go to it's reorder buffer to see if the value is ready
                    if(thisRob.ready) {
                        rs.Vj = thisRob.value;
                        rs.Qj = 0;
                    } else {
                        rs.Qj = h;
                    }
                } else {
                    rs.Vj = Registers[ins.irs].value;
                    rs.Qj = 0;
                }
            }
            //check rt
            if(irt != Integer.MIN_VALUE && !funct.equals("LW") && !funct.equals("ADDI") && !funct.equals("ADDIU") && !funct.equals("SLTI")) {
                register reg = Registers[irt]; //target reg
                int h = reg.Reorder;         //target rob order
                if(reg.Busy) {
                    robEntry thisRob = ROB.get(h); // if the this register is busy, then go to it's reorder buffer to see if the value is ready
                    if(thisRob.ready) {
                        rs.Vk = thisRob.value;
                        rs.Qk = 0;
                    } else {
                        rs.Qk = h;
                    }
                } else {
                    rs.Vk = reg.value;
                    rs.Qk = 0;
                }
            } else if(funct.equals("LW") | funct.equals("ADDI") | funct.equals("ADDIU") | funct.equals("SLTI")) {
                rob.Destination = irt;
                register reg = Registers[irt];
                reg.Busy = true;
                reg.Reorder = rob.Entry;
            }
            //check rd set the rob des to register; set the Reorder, busy of register
            if(ird != Integer.MIN_VALUE) {
                rob.Destination = ird;
                register reg = Registers[ird];
                reg.Busy = true;
                reg.Reorder = rob.Entry;
            }
            //check sa
            if(isa != Integer.MIN_VALUE) {
                rs.Vj = isa;
                rs.Qj = 0;
            }
            //checck if ioffset, imi or jumpAddr exist to set A
            if(ioffset != Integer.MAX_VALUE) rs.A = ioffset;
            if(imi != Integer.MAX_VALUE) {
                    rs.A = imi;
            }
            if(ins.jumpAddr != Integer.MIN_VALUE) {
                rs.A = ins.jumpAddr;
            }
        }
    }
    // fetch state
    void fetch() {
    	if(mispre) {
    		mispre = false;
    		return;
    	}
        // if pc is greater then 716, stop fetching
        if (pc > 716) {
            return;
        }
        //fetch instruction from commands
        Instruction thisIns = commands.get(pc);//get the instruction that needed to be execute
        if (thisIns != null) {
            IQ.add(thisIns);
            String funct = thisIns.function;
            boolean isBranch = true;
            //check if this instruction is a branch, if it is calculate the target address
            switch (funct) {
                case "J":
                case "BEQ":
                case "BNE":
                case "BLEZ":
                case "BGTZ":
                case "BLTZ":
                case "BGEZ":
                    break;
                default:
                    isBranch = false;
                    break;
            }
            //check if this is a branch, if it is then keep checking if it is in the btb table, if not set pc to pc + 4
            if (isBranch) {
                btbEntry btb = BTB.getEntry(pc);
                if (btb != null && btb.prediction == 1) {
                    pc = btb.target;
                    return;
                }
                pc += 4;
            } else {
                pc += 4; // if this is not a branch then set pc to pc + 4
            }
        }
    }
}
