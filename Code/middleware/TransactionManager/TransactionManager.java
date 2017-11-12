package TransactionManager;


import ResInterface.InvalidTransactionException;
import ResInterface.TransactionAbortedException;
import LockManager.LockManager;
import LockManager.DeadlockException;

import java.util.HashSet;

public class TransactionManager {
    static LockManager lockManager = new LockManager();
    static int current_transaction_id = 0;
    public static HashSet<Integer> transactions = new HashSet<>();;


    public synchronized int start(){
        current_transaction_id += 1;
        transactions.add(current_transaction_id);
        return current_transaction_id;
    }

    public boolean commit(int transaction_id) throws InvalidTransactionException, TransactionAbortedException {
        synchronized (this.transactions){
            if(!transactions.contains(transaction_id))
                throw new InvalidTransactionException(transaction_id, "Transaction does not exist");
            transactions.remove(transaction_id);
        }
        return lockManager.UnlockAll(transaction_id);
    }

    public boolean lock(int transaction_id, String strData, int lockType) throws TransactionAbortedException, InvalidTransactionException {
        synchronized (this.transactions){
            if(!transactions.contains(transaction_id))
                throw new InvalidTransactionException(transaction_id, "Transaction does not exist");
        }
        boolean result;
        try {
            result = lockManager.Lock(transaction_id, strData, lockType);
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(transaction_id, "Deadlock state");
        }
        return result;
    }

    public void abort(int id){
        lockManager.UnlockAll(id);
        synchronized (this.transactions){
            transactions.remove(id);
        }
    }

    public static String getKeyCar( String location ) {
        String s = "car-" + location  ;
        return s.toLowerCase();
    }

    public static String getKeyCustomer( int customerID ) {
        String s = "customer-" + customerID;
        return s.toLowerCase();
    }

    public static String getKeyFlight( int flightNum ) {
        String s = "flight-" + flightNum;
        return s.toLowerCase();
    }

    public static String getKeyRoom( String location ) {
        String s = "room-" + location  ;
        return s.toLowerCase();
    }

    public boolean stillHasTransaction(){
        synchronized (this.transactions){
            return !transactions.isEmpty();
        }
    }


}
