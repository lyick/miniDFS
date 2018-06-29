package Node;

import Global.Global;
import Util.FileMap;
import Util.MyFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map.*;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;



public class NameNode extends Thread implements Serializable{

    private static final long serialVersionUID = 1L;

    private FileMap fileMap;

    public void init(){
        if(!new File("dfs/namenode/map").exists()) {
            fileMap = new FileMap();
        }
        else load();

    }

    @Override
    public void run() {
        while(true)
        {
            try {
               Global.name_event.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            switch (Global.cmd_type){
                case ls:
                    listFile();break;
                case put:
                    splitFile();break;
                case read:
                    readFile();break;
                case fetch:
                    fetchFile();break;
            }
        }
    }

    @Override
    public void start(){
        init();
        super.start();
    }

    public void updateData(){
       try {
           FileOutputStream fos = new FileOutputStream("dfs/namenode/map");
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(fileMap);
       } catch (Exception e){
            e.printStackTrace();
       }
    }

    public void load(){
        try{
            FileInputStream fis = new FileInputStream("dfs/namenode/map");
            ObjectInputStream ois = new ObjectInputStream(fis);
            fileMap = (FileMap)ois.readObject();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void listFile() {
        System.out.printf(Global.OUTPUT_FORMAT,"ID","file_name","file_size");
        Vector<MyFile> vec = fileMap.id_file;
        for(int i = 0;i < vec.size(); i++ ){
            System.out.printf(Global.OUTPUT_FORMAT,i,vec.get(i).getName(),vec.get(i).getSize());
        }
        try {
            Global.ls_event.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void splitFile(){
        File upfile = new File(Global.file_path);
        int id = fileMap.last_id;
        int off = id % 4;
        int block_counts = (int)(upfile.length() / Global.BLOCK_SIZE) + 1;

        updateData();
    }

    public void fetchFile(){

    }

    public void readFile(){

    }
}
