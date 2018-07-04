package Node;

import Global.Global;
import Util.FileMap;
import Util.MyFile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

public class TreeNode implements Serializable{
    private static final long serialVersionUID = 1L;

    private final String name;

    private final HashMap<String,TreeNode> dirs;

    private final HashMap<String,MyFile> files;

    public TreeNode(String name){
        this.name = name;
        files = new HashMap<>();
        dirs = new HashMap<>();
        dirs.put(".",this);
    }

    public String getName() {
        return name;
    }

    public HashMap<String, MyFile> getFiles() {
        return files;
    }

    public HashMap<String, TreeNode> getDirs() {
        return dirs;
    }

    public MyFile findFile(String path){
        if(files.containsKey(path))
            return files.get(path);
        return null;
    }

    public TreeNode findDir(String path){
        if(dirs.containsKey(path))
            return dirs.get(path);
        return null;
    }

    public TreeNode parsePath(String path){
        String[] paths = path.split("/");
        TreeNode res = this;
        if(paths.length == 0)
            Global.file_name = ".";
        else Global.file_name = paths[paths.length-1];
        //@TODO 这里有bug
        if(paths.length < 2)
            return this;
        int i;
        if(paths[0].equals(""))
            i=1;
        else i = 0;
        for(; i < paths.length - 1 && res != null; i++)
        {
            res = res.findDir(paths[i]);
        }

        return res;
    }

}
