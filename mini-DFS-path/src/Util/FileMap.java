package Util;

import Node.TreeNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class FileMap implements Serializable {
    private static final long serialVersionUID = 1L;

    public int last_id;

    public int lastServer;

//    public Vector<MyFile> id_file;

    public final TreeNode fileTree;

    public HashMap<String,List<Integer>> block_datanode;

    public FileMap(){
        last_id = 0;
        lastServer = 0;
//        id_file = new Vector<>();
        block_datanode = new HashMap<>();
        fileTree = new TreeNode("/");
    }


}
