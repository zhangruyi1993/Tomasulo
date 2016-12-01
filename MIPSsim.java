import java.io.*;

/**
 * Created by nana on 2016/11/21.
 */
public class MIPSsim {
    public static void main(String[] args) {
        // write your code here
        File file = new File(args[0]);
        File output = new File(args[1]);
        if (file.exists()) {
            try {
                InputStream in = null;
                in = new FileInputStream(file);
                long tempbyte = 0;
                int address = 600;
                boolean exit = false;
                output.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(output));
                Tomasulo myTom = new Tomasulo();
                while ((tempbyte = in.read()) != -1) {
                    for (int i = 0; i < 3; i++) {
                        tempbyte = tempbyte * 256 + in.read();
                    }
                    String ins = longToString(tempbyte);
                    Instruction newIns = new Instruction(ins, address, exit);
                    exit = newIns.ex;
                    //put the instruction from input into the commands array
                    myTom.commands.put(address, newIns);
                    if(exit)
                        break;
                    address += 4;
                }
                myTom.simulate(out);
                in.close();
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally{
                //close
            }
        }
    }
    //converting the binary into string so it is easier to compare. Working well
    private static String longToString (long num) {
        String ins =  "";
        long base = Integer.MAX_VALUE;
        base = base + 1;
        while (base != 0) {
            if (num >= base) {
                num -= base;
                ins = ins.concat("1");
            } else {
                ins = ins.concat("0");
            }
            base /= 2;
        }
        return ins;
    }
}
