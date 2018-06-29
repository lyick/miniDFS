package Global;

import java.util.concurrent.CyclicBarrier;

public class Global {
    public final static String OUTPUT_FORMAT = "%-10d%-20s%-10s%n";
    public final static int BLOCK_SIZE = 1024 * 1024 * 2;
    public static boolean cmd_flag;
    public static Operation cmd_type;
    public static String file_path;
    public static String save_path;
    public static int file_ID = 0;
    public static CyclicBarrier name_event = new CyclicBarrier(2);
    public static CyclicBarrier ls_event = new CyclicBarrier(2);
    public static CyclicBarrier read_event = new CyclicBarrier(2);
    public static CyclicBarrier[] main_event = new CyclicBarrier[4];
    public static CyclicBarrier[] data_event = new CyclicBarrier[4];
    public final static int SERVER_NUMBER = 4;

    static{
        for(CyclicBarrier cb : main_event){
            cb = new CyclicBarrier(2);
        }

        for(CyclicBarrier cb : data_event){
            cb = new CyclicBarrier(2);
        }
    }

}
