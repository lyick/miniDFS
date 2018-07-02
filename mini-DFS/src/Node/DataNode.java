package Node;

import Global.Global;
import Global.Operation;
import Util.Block;
import Util.FileHelper;

import java.util.concurrent.BrokenBarrierException;

public class DataNode extends Thread{

    private int id;

    public DataNode(int serverId){
        super();
        this.id = serverId;
    }

    @Override
    public void run(){
        while(true){
            try {
                Global.data_event[id].await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            if(Global.cmd_type == Operation.put && Global.blockServer.containsKey(id)){
                save();
            } else if (Global.cmd_type == Operation.read)
                read();
            else {
                try {
                    Global.main_event[id].await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void save(){
        for(Block block : Global.blockServer.get(id)){
            FileHelper.write("dfs/datanode"+id+"/"+block.getName(),block.getOffset());
        }
        try {
            Global.main_event[id].await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    public void read(){
        String path = "dfs/datanode"+id+"/";
        String filename = Global.file_ID+"-part-0";
        FileHelper.read(path+filename);
        try {
            Global.read_event.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

}
