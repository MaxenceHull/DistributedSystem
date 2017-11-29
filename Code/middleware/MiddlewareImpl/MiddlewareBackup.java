package MiddlewareImpl;

import TransactionManager.TransactionManager;

import java.io.*;

public class MiddlewareBackup {


    public static void save(TransactionManager tm){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("MiddlewareBackup");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(tm);
            out.close();
            fileOut.close();
            //System.out.println("Serialized data is saved in MiddlewareBackup");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static TransactionManager getBackup(){
        TransactionManager result = null;
        try {
            FileInputStream fileIn = new FileInputStream("MiddlewareBackup");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (TransactionManager) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            System.out.println("No backup file");
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }

        return result;
    }
}
