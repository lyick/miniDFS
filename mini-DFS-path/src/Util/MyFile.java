package Util;

import java.io.Serializable;

public class MyFile implements Serializable{
    private static final long serialVersionUID = 1L;

    final private String name;

    final private String size;

    final private String[] blocks;

    final private int id;

    public MyFile(String filename, String filesize ,String[] blk, int id){
        this.name = filename;
        this.size = filesize;
        blocks = blk;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String[] getBlocks() {
        return blocks;
    }

    public int getId() {
        return id;
    }
}
