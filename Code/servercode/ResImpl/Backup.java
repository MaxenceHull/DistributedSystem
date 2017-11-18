package ResImpl;

import java.io.*;

public class Backup {

    private String path;

    public Backup(String path){
        this.path = path;
    }

     public void save(RMHashtable hashtable){
         try {
             FileOutputStream fileOut =
                     new FileOutputStream(path);
             ObjectOutputStream out = new ObjectOutputStream(fileOut);
             out.writeObject(hashtable);
             out.close();
             fileOut.close();
             System.out.printf("Serialized data is saved in "+path);
         } catch (IOException i) {
             i.printStackTrace();
         }
    }

    public RMHashtable getBackup(){
        RMHashtable result = null;
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (RMHashtable) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        System.out.printf("Backup done from "+path);
        return result;
    }
}
