package Node;

import Global.Global;
import Util.Block;
import Util.FileMap;
import Util.MyFile;

import java.io.*;
import java.util.ArrayList;
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
        System.out.printf("%-10s%-20s%-10s%n","ID","file_name","file_size");
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
        long file_size = upfile.length();
        int file_id = fileMap.last_id;
        int begin_server = fileMap.lastServer;
        int block_counts = (int)(file_size / Global.BLOCK_SIZE) + 1;
        String[] blk = new String[block_counts];
        for(int i = 0; i < block_counts ; i++)
            for(int j = 0; j < 3 ; j++){
                int assign_server = (begin_server + i + j) % 4;
                String blkName = file_id +"-part-"+i;
                blk[i] = blkName;
                Block block = new Block(blkName, Global.BLOCK_SIZE * i );
                if(Global.blockServer.containsKey(assign_server))
                {
                    Global.blockServer.get(assign_server).add(block);
                } else {
                    ArrayList<Block> blocks = new ArrayList<>();
                    blocks.add(block);
                    Global.blockServer.put(assign_server,blocks);
                }

                if(fileMap.block_datanode.containsKey(blkName))
                {
                    fileMap.block_datanode.get(blkName).add(assign_server);
                } else {
                    ArrayList<Integer> servers = new ArrayList<>();
                    servers.add(assign_server);
                    fileMap.block_datanode.put(blkName,servers);
                }
            }
        for(int m = 0; m < 4 ; m++) {
            try {
                Global.data_event[m].await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        MyFile myFile = new MyFile(Global.file_path,new Long(file_size).toString(),blk);
        fileMap.id_file.add(myFile);
        fileMap.last_id++;
        fileMap.lastServer++;
        updateData();

    }

    public void readFile(){
        MyFile myFile = fileMap.id_file.get(Global.file_ID);
        String read_first_blk = myFile.getBlocks()[0];
        int serverId = fileMap.block_datanode.get(read_first_blk).get(0);
        try {
            Global.data_event[serverId].await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void fetchFile(){
        MyFile myFile = fileMap.id_file.get(Global.file_ID);
        File output = new File(Global.save_path+"/"+myFile.getName());
        int serverId;
        byte[] b = new byte[Global.BLOCK_SIZE];
        int size;
        try (FileOutputStream fos = new FileOutputStream(output);){
            for(String blk : myFile.getBlocks()){
                serverId = fileMap.block_datanode.get(blk).get(0);
                FileInputStream fis = new FileInputStream(new File("dfs/datanode"+serverId+"/"+blk));
                if((size = fis.read(b))!=-1)
                    fos.write(b,0,size);
                fis.close();
            }
            Global.main_event[0].await();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
