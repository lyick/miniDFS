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
import java.util.concurrent.CyclicBarrier;


public class NameNode extends Thread implements Serializable{

    private static final long serialVersionUID = 1L;

    private FileMap fileMap;

    public void init(){
        if(!new File("dfs/namenode/map").exists()) {
            fileMap = new FileMap();
        }
        else load();
        Global.current_dir = fileMap.fileTree;
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
                case cd:
                    chgDir();break;
                case mkdir:
                    makeDir();break;
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

    public TreeNode dirToParse(){
        if(Global.file_path.charAt(0) == '/') {
            return fileMap.fileTree;
        }
        return Global.current_dir;
    }

    public void chgDir(){
        TreeNode tn = dirToParse().parsePath(Global.file_path);
        if(tn == null)
            System.out.println("No such directory.");
        else {
            if((tn = tn.findDir(Global.file_name)) != null)
                Global.current_dir = tn;
            else System.out.println("No such directory.");
        }
        unLock( Global.main_event[0]);
    }

    public void makeDir(){
        TreeNode tn = dirToParse().parsePath(Global.file_path);
        if(tn == null)
            System.out.println("Cannot make directory.No such path or directory.");
        else if(tn.findDir(Global.file_name) != null)
            System.out.println("Directory already exist.");
        else {
            TreeNode newDir = new TreeNode(Global.file_name);
            newDir.getDirs().put("..",tn);
            tn.getDirs().put(Global.file_name,newDir);
            System.out.println("Dir create success!");
        }
        unLock( Global.main_event[0]);

        updateData();
    }

    public void listFile() {
        System.out.printf(Global.OUTPUT_FORMAT,"Type","File_name","File_size");
        for(String dir : Global.current_dir.getDirs().keySet())
            System.out.printf(Global.OUTPUT_FORMAT,"Dir",dir,"");
        for(String file : Global.current_dir.getFiles().keySet())
            System.out.printf(Global.OUTPUT_FORMAT,"File",file,Global.current_dir.getFiles().get(file).getSize());
        unLock(Global.ls_event);
    }

    public void splitFile(){
        TreeNode tn = dirToParse().parsePath(Global.des_path);
        if(tn == null || tn.findDir(Global.file_name) == null) {
            System.out.println("Cannot upload file.No such path or directory.");
            for(int i = 0 ;i < 4 ; i++)
                unLock(Global.main_event[i]);
            return ;
        }
        tn = tn.findDir(Global.file_name);
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
            unLock(Global.data_event[m]);
        }
        MyFile myFile = new MyFile(upfile.getName(),new Long(file_size).toString(),blk,file_id);
//        fileMap.id_file.add(myFile);
        tn.getFiles().put(upfile.getName(),myFile);
        System.out.println("Upload success!");
        fileMap.last_id++;
        fileMap.lastServer++;
        updateData();

    }

    public void readFile(){
        TreeNode tn = dirToParse().parsePath(Global.file_path);
        MyFile myFile;

        if(tn == null)
        {
            System.out.println("Cannot read file.No such directory.");
            unLock(Global.read_event);
            return;
        } else if((myFile = tn.findFile(Global.file_name)) ==null ){
            System.out.println("Cannot read file.No such file.");
            unLock(Global.read_event);
            return;
        }
        Global.file_ID = myFile.getId();
        String read_first_blk = myFile.getBlocks()[0];
        int serverId = fileMap.block_datanode.get(read_first_blk).get(0);
        unLock(Global.data_event[serverId]);
    }

    public void fetchFile(){
        TreeNode tn = dirToParse().parsePath(Global.file_path);
        MyFile myFile;
        if(tn == null)
        {
            System.out.println("Cannot fetch file.No such file or directory.");
            unLock(Global.main_event[0]);
            return;
        } else if((myFile = tn.findFile(Global.file_name)) ==null ){
            System.out.println("Cannot fetch file.No such file or directory.");
            unLock(Global.main_event[0]);
            return;
        }
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
            System.out.println("Fetch success!");
            unLock(Global.main_event[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unLock(CyclicBarrier cb){
        try {
            cb.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

}
