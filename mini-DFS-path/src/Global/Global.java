package Global;

import Node.TreeNode;
import Util.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;

public class Global {
    public final static String OUTPUT_FORMAT = "%-10s%-24s%-12s%n";
    public final static int BLOCK_SIZE = 1024 * 1024 * 2;
    public static boolean cmd_flag;
    public static Operation cmd_type;
    public static String file_path;
    public static String file_name;
    public static String save_path;
    public static String des_path;
    public static int file_ID = 0;
    public static CyclicBarrier name_event = new CyclicBarrier(2);
    public static CyclicBarrier ls_event = new CyclicBarrier(2);
    public static CyclicBarrier read_event = new CyclicBarrier(2);
    public static CyclicBarrier[] main_event = new CyclicBarrier[4];
    public static CyclicBarrier[] data_event = new CyclicBarrier[4];
    public static HashMap<Integer, ArrayList<Block>> blockServer = new HashMap<>();
    public final static int SERVER_NUMBER = 4;
    public static TreeNode current_dir;

    public static void init(){
        for(int i =0;i < 4 ;i ++){
            main_event[i] = new CyclicBarrier(2);
        }

        for(int i =0;i < 4 ;i ++){
            data_event[i] = new CyclicBarrier(2);
        }
    }

}
