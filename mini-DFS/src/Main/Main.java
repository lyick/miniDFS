package Main;

import Global.Global;
import Global.Operation;
import Node.NameNode;



import java.io.File;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;

public class Main {

    public void init(){
        //文件夹创建
        File dfsdir = new File("dfs");
        if(!dfsdir.exists())
            dfsdir.mkdir();
        File namedir = new File("dfs/namenode");
        if(!namedir.exists())
        namedir.mkdir();
        File datadir;
        for(int i = 0;i < 4; i++) {
            datadir = new File("dfs/datanode" + i);
            if(!datadir.exists())
                datadir.mkdir();
        }

        //线程创建开启
        NameNode nameNode = new NameNode();
        nameNode.start();
        //
    }

    public void start() throws BrokenBarrierException, InterruptedException {
        Scanner scan = new Scanner(System.in);
        while(true){
            System.out.print("Mini-DFS > ");
            Global.cmd_flag = processCmd(scan.nextLine());
            if(Global.cmd_flag) {
                Global.name_event.await();
                if (Global.cmd_type == Operation.quit)
                    System.exit(0);
                else if (Global.cmd_type == Operation.put){
                    for(int i=0;i<Global.SERVER_NUMBER;i++)
                        Global.main_event[i].await();
                }
                else if (Global.cmd_type == Operation.read)
                    Global.read_event.await();

                else if (Global.cmd_type == Operation.ls)
                    Global.ls_event.await();

                else if (Global.cmd_type == Operation.fetch)
                    for(int i=0;i<Global.SERVER_NUMBER;i++)
                        Global.main_event[i].await();
                else
                    continue;


            }
//            System.out.println("pass");
        }
    }

    public boolean processCmd(String s){
        String[] cmds = s.split(" ");
        if(cmds[0].equals("quit"))
            if(cmds.length!=1) {
                System.out.println("quit Usage: quit");
                return false;
            }
            else{
                Global.cmd_type = Operation.quit;
            }
        else if(cmds[0].equals("ls")){
            if(cmds.length > 1) {
                System.out.println("ls Usage: ls");
                return false;
            }
            else{
                Global.cmd_type = Operation.ls;
            }
        }
        else if(cmds[0].equals("put")){
            if(cmds.length != 2) {
                System.out.println("put Usage: put file_path");
                return false;
            }
            else {
                if(!new File(cmds[1]).exists()){
                    System.out.println("File not exist.Please check your file_path");
                    return false;
                }
                else {
                    Global.cmd_type = Operation.put;
                    Global.file_path = cmds[1];
                }
            }
        }
        else if(cmds[0].equals("read")){
            if(cmds.length!=2){
                System.out.println("read Usage: read file_id");
                return false;
            }
            else{
                try{
                    Global.file_ID = Integer.parseInt(cmds[1]);
                }catch (NumberFormatException e){
                    System.out.println("file_id must be integer.");
                    return false;
                }
                Global.cmd_type = Operation.read;
                return true;

            }

        }
        else if(cmds[0].equals("fetch")){
            if(cmds.length!=3){
                System.out.println("fetch Usage: fetch file_id save_path");
                return false;
            }
            else{
                if(!new File(cmds[2]).isDirectory()){
                    System.out.println("Invalid save_path.");
                    return false;
                }
                try{
                    Global.file_ID = Integer.parseInt(cmds[1]);
                }catch (NumberFormatException e){
                    System.out.println("file_id must be integer.");
                    return false;
                }
                Global.cmd_type = Operation.fetch;
                Global.save_path = cmds[2];
            }
        }
        else{
            System.out.println("Command not found.Please use put|ls|fetch|read|quit.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
        Main main = new Main();
        main.init();
        main.start();
    }
}
