package ResImpl;


import java.io.*;
import java.util.Hashtable;

public class Backup {

    private String path;
    private String pathMasterRecord = "MasterRecord";
    private String currentMasterRecord;

    public Backup(String path){

        this.path = path;
        this.pathMasterRecord = pathMasterRecord+"_"+path;
    }

     public void save(RMHashtable hashtable){
        String nextRecord = (currentMasterRecord.equals("A")) ? "B" : "A";
        try {
            FileOutputStream fileOut =
                     new FileOutputStream(path+"_"+nextRecord);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(hashtable);
            out.close();
            fileOut.close();
            //System.out.printf("Serialized data is saved in "+path+"_"+nextRecord);
        } catch (IOException i) {
             i.printStackTrace();
        }
        setPathMasterRecord(nextRecord);
        currentMasterRecord = nextRecord;

    }

    public void saveTransaction(Log log){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path+"_Log");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(log);
            out.close();
            fileOut.close();
            //System.out.println("Serialized data is saved in MiddlewareBackup");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public Log getBackupLog(){
        Log result = null;
        try {
            FileInputStream fileIn = new FileInputStream(path+"_Log");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (Log) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        //System.out.printf("Backup done from "+path);
        return result;
    }

    public RMHashtable getBackup(){
        RMHashtable result = null;
        currentMasterRecord = getMasterRecord();
        try {
            FileInputStream fileIn = new FileInputStream(path+"_"+currentMasterRecord);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (RMHashtable) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        //System.out.printf("Backup done from "+path);
        return result;
    }

    private String getMasterRecord(){
        String result = null;
        try {
            FileInputStream fileIn = new FileInputStream(pathMasterRecord);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (String) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        if(result == null){
            result = "A";
        }
        return result;
    }

    private void setPathMasterRecord(String newValue){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(pathMasterRecord);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(newValue);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
