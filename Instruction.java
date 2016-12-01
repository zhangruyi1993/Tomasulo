/**
 * Created by nana on 2016/11/21.
 */
public class Instruction {
    String function;
    int addr;
    //the following six are data needed
    int irs = Integer.MIN_VALUE;//also the base of lw and sw, they shouldn't be negative is assigned when initial
    int irt = Integer.MIN_VALUE;
    int ird = Integer.MIN_VALUE;
    int isa = Integer.MIN_VALUE;
    int ioffset = Integer.MAX_VALUE;  //signed left-shifted offset
    int imi = Integer.MAX_VALUE; //signed offset and imi
    int jumpAddr = Integer.MIN_VALUE; // only J instruction has this attribute
    boolean ex; // help tell if the break appeared or not
    String instru; //store the typable version of the instruction
    String simIns;

    //constructor from string
    Instruction(String ins, int address, boolean exit) {
        addr = address;
        //separate the instruction into three different types and disassembler individually
        if (exit) {
            instru = ins.concat(" ").concat(Integer.toString(address).concat(" 0"));
            ex = exit;
        } else {
            switch (ins.substring(0,6)) {
                case "000000" :
                    this.rDis(ins, address, exit);
                    break;
                case "000001" :
                    this.jDis(ins, address);
                    ex = exit;
                    break;
                default :
                    this.iDis(ins, address);
                    ex = exit;
                    break;
            }
        }
    }

    //R-Type Instruction dis
    void rDis(String ins, int address, boolean exit) {
        if (ins.equals("00000000000000000000000000000000")) {
            this.instru = "000000 00000 00000 00000 00000 000000 ".concat(Integer.toString(address)).concat(" NOP");
            this.simIns = "[".concat("NOP").concat("]");
            this.ex = exit;
            this.function = "NOP";
        } else {
            int rs = StringToInt(ins.substring(6, 11));
            int rt = StringToInt(ins.substring(11, 16));
            int rd = StringToInt(ins.substring(16, 21));
            int shamt = StringToInt(ins.substring(21, 26));
            String funct = " ";
            switch (ins.substring(26)) {
                //SLL, SRL AND SRA don't have ira, ioffset and jumpAddr, have function, irt, ird, isa
                case "000000":
                    this.function = "SLL"; this.irt = rt; this.ird = rd; this.isa = shamt;
                    funct = funct.concat("SLL R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rt)).concat(", ").concat(Integer.toString(shamt));
                    break;
                case "000010":
                    this.function = "SRL"; this.irt = rt; this.ird = rd; this.isa = shamt;
                    funct = funct.concat("SRL R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rt)).concat(", ").concat(Integer.toString(shamt));
                    break;
                case "000011":
                    this.function = "SRA"; this.irt = rt; this.ird = rd; this.isa = shamt;
                    funct = funct.concat("SRA R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rt)).concat(", ").concat(Integer.toString(shamt));
                    break;
                // Break is special, it has no propoties because nothing need to be done after this instruction
                case "001101":
                    this.function = "BREAK";
                    funct = funct.concat("BREAK");
                    exit = true;
                    break;
                //ADD, ADDU,SUB, SUBU, AND, OR, XOR, NOR, ALT, SLTU don't have isa, ioffset and jumpAddr, have function, irs, irt, ird
                case "100000":
                    this.function = "ADD"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("ADD R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "100001":
                    this.function = "ADDU"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("ADDU R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "100010":
                    this.function = "SUB"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("SUB R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "100011":
                    this.function = "SUBU"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("SUBU R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "100100":
                    this.function = "AND"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("AND R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "100101":
                    this.function = "OR"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("OR R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "100110":
                    this.function = "XOR"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("XOR R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "100111":
                    this.function = "NOR"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("NOR R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "101010":
                    this.function = "SLT"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("SLT R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                case "101011":
                    this.function = "SLTU"; this.irs = rs; this.irt = rt; this.ird = rd;
                    funct = funct.concat("SLTU R").concat(Integer.toString(rd)).concat(", R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt));
                    break;
                default:
                    break;
            }
            this.instru =  ins.substring(0, 6).concat(" ").concat(ins.substring(6, 11)).concat(" ")
                    .concat(ins.substring(11, 16)).concat(" ").concat(ins.substring(16, 21)).concat(" ")
                    .concat(ins.substring(21, 26)).concat(" ").concat(ins.substring(26)).concat(" ")
                    .concat(Integer.toString(address)).concat(funct);
            this.simIns = "[".concat(funct.trim()).concat("]");
            this.ex = exit;
        }
    }
    //J-Type Instruction dis
    void jDis(String ins, int address) {
        String funct = " ";
        int rs = StringToInt(ins.substring(6,11));
        int offset = 4 * StringToImi(ins.substring(16));
        this.irs = rs; this.ioffset = offset;
        if (ins.substring(11,16).equals("00000")) {
            this.function = "BLZT";
            funct = funct.concat("BLZT");
        } else if (ins.substring(11,16).equals("00001")){
            this.function = "BGEZ";
            funct = funct.concat("BGEZ");
        }
        this.instru = ins.substring(0, 6).concat(" ").concat(ins.substring(6,11)).concat(" ")
                .concat(ins.substring(11,16)).concat(" ").concat(ins.substring(16,21)).concat(" ")
                .concat(ins.substring(21,26)).concat(" ").concat(ins.substring(26)).concat(" ")
                .concat(Integer.toString(address)).concat(funct).concat(" R")
                .concat(Integer.toString(rs)).concat(" #").concat(Integer.toString(offset));
        this.simIns = "[".concat(funct.trim()).concat("]");
    }
    //I-Type Instruction dis
    void iDis(String ins, int address) {
        String funct = " ";
        int rs = StringToInt(ins.substring(6,11));
        int rt = StringToInt(ins.substring(11,16));
        int im = StringToImi(ins.substring(16));
        int offset = 4 * StringToInt(ins.substring(16));
        switch(ins.substring(0,6)) {
            //J is a special instruction , it only have function and jumpAddr property
            case "000010" :
                this.function = "J"; this.jumpAddr = 4*StringToInt(ins.substring(6));
                funct = funct.concat("J #").concat(Integer.toString(4*StringToInt(ins.substring(6))));
                break;
            //BEQ and BNE have similar property irs, irt, ioffset
            case "000100" :
                this.function = "BEQ"; this.irs = rs; this.irt = rt; this.ioffset = offset;
                funct = funct.concat("BEQ R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt)).concat(", #").concat(Integer.toString(offset));
                break;
            case "000101" :
                this.function = "BNE"; this.irs = rs; this.ioffset = offset;
                funct = funct.concat("BNE R").concat(Integer.toString(rs)).concat(", R").concat(Integer.toString(rt)).concat(", #").concat(Integer.toString(offset));
                break;
            //BLEZ and BGTZ have similar property irs, ioffset
            case "000110" :
                this.function = "BLEZ"; this.irs = rs; this.ioffset = offset;
                funct = funct.concat("BLEZ R").concat(Integer.toString(rs)).concat(", #").concat(Integer.toString(offset));
                break;
            case "000111" :
                this.function = "BGTZ"; this.irs = rs; this.irt = rt; this.ioffset = offset;
                funct = funct.concat("BGTZ R").concat(Integer.toString(rs)).concat(", #").concat(Integer.toString(offset));
                break;
            //ADDI, ADDIU and SLTI have similar property irs, irt, ioffset
            case "001000" :
                this.function = "ADDI"; this.irs = rs; this.irt = rt; this.imi = im;
                funct = funct.concat("ADDI R").concat(Integer.toString(rt)).concat(", R").concat(Integer.toString(rs)).concat(", #").concat(Integer.toString(im));
                break;
            case "001001" :
                this.function = "ADDIU"; this.irs = rs; this.irt = rt; this.imi = im;
                funct = funct.concat("ADDIU R").concat(Integer.toString(rt)).concat(", R").concat(Integer.toString(rs)).concat(", #").concat(Integer.toString(im));
                break;
            case "001010" :
                this.function = "SLTI"; this.irs = rs; this.irt = rt; this.imi = im;
                funct = funct.concat("SLTI R").concat(Integer.toString(rt)).concat(", R").concat(Integer.toString(rs)).concat(", #").concat(Integer.toString(im));
                break;
            //LW and SW have similar property irs, irt, ioffset
            case "100011" :
                this.function = "LW"; this.irs = rs; this.irt = rt; this.imi = im;
                funct = funct.concat("LW R").concat(Integer.toString(rt).concat(", ").concat(Integer.toString(im)).concat("(R").concat(Integer.toString(rs).concat(")")));
                break;
            case "101011" :
                this.function = "SW"; this.irs = rs; this.irt = rt; this.imi = im;
                funct = funct.concat("SW R").concat(Integer.toString(rt).concat(", ").concat(Integer.toString(im)).concat("(R").concat(Integer.toString(rs).concat(")")));
                break;
            default:
                break;
        }
        this.instru = ins.substring(0, 6).concat(" ").concat(ins.substring(6,11)).concat(" ")
                .concat(ins.substring(11,16)).concat(" ").concat(ins.substring(16,21)).concat(" ")
                .concat(ins.substring(21,26)).concat(" ").concat(ins.substring(26)).concat(" ")
                .concat(Integer.toString(address)).concat(funct);
        this.simIns = "[".concat(funct.trim()).concat("]");
    }

    //converting the string into int. Working well
    int StringToInt (String s) {
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            result *= 2;
            if (s.charAt(i) == '1') {
                result++;
            }
        }
        return result;
    }
    //converting the string into imi. Working well
    int StringToImi (String s) {
        int result = 0;
        boolean sign = false;
        if (s.charAt(0) == '1') {
            sign = true;
            String temp = "";
            int i;
            for (i = s.length() - 1; i >= 0; i--) {
                if (s.charAt(i) == '1') {
                    temp = "1".concat(temp);
                    i--;
                    break;
                }
                temp = "0".concat(temp);
            }
            for (i = i; i >= 0; i--) {
                if (s.charAt(i) == '0') {
                    temp = "1".concat(temp);
                } else {
                    temp = "0".concat(temp);
                }
            }
            s = temp;
        }
        for (int i = 0; i < s.length(); i++) {
            result *= 2;
            if (s.charAt(i) == '1') {
                result++;
            }
        }
        if (sign) {
            result = -result;
        }
        return result;
    }
}
