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
            System.out.println("Serialized data is saved in MiddlewareBackup");
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
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        if(result != null){
            System.out.println("Backup done from MiddlewareBackup: ");
            System.out.println(result.transactions.toString());
            System.out.println(result.actions.toString());
            System.out.println(result.isRollback.toString());
            System.out.println(result.clientTime.toString());
        }

        return result;
    }
}
