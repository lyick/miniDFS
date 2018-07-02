package Util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class FileMap implements Serializable {

    public int last_id;

    public int lastServer;

    public Vector<MyFile> id_file;

    public HashMap<String,List<Integer>> block_datanode;

    public FileMap(){
        last_id = 0;
        lastServer = 0;
        id_file = new Vector<>();
        block_datanode = new HashMap<>();
    }


}
