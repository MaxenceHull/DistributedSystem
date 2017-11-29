package TransactionManager;


import MiddlewareImpl.MiddlewareManagerImpl;
import ResInterface.InvalidTransactionException;
import ResInterface.TransactionAbortedException;
import LockManager.LockManager;
import LockManager.DeadlockException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionManager implements Serializable {
    private LockManager lockManager = new LockManager();
    int current_transaction_id = 0;
    public HashSet<Integer> transactions = new HashSet<>();
    public ConcurrentHashMap<Integer, Long> clientTime = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, HashMap<Integer, Boolean>> votes = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, HashMap<Integer, Boolean>> decisions = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, Boolean> hasCommitted = new ConcurrentHashMap<>();


    public synchronized int start(){
        current_transaction_id += 1;
        transactions.add(current_transaction_id);
        hasCommitted.put(current_transaction_id, false);
        HashMap<Integer, Boolean> rm = new HashMap<>();
        rm.put(MiddlewareManagerImpl.RM_ROOM, null);
        rm.put(MiddlewareManagerImpl.RM_FLIGHT, null);
        rm.put(MiddlewareManagerImpl.RM_CAR, null);
        votes.put(current_transaction_id, rm);
        rm = new HashMap<>();
        rm.put(MiddlewareManagerImpl.RM_ROOM, null);
        rm.put(MiddlewareManagerImpl.RM_FLIGHT, null);
        rm.put(MiddlewareManagerImpl.RM_CAR, null);
        decisions.put(current_transaction_id, rm);
        return current_transaction_id;
    }

    public boolean commit(int transaction_id) throws InvalidTransactionException, TransactionAbortedException {
        synchronized (this.transactions){
            if(!transactions.contains(transaction_id))
                throw new InvalidTransactionException(transaction_id, "Transaction does not exist");
            removeTransaction(transaction_id);
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
        if(lockManager.UnlockAll(id)) {
            removeTransaction(id);
        }else {
            System.out.println("Unlock impossible on transaction "+id);
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

    private void removeTransaction(int idTransaction){
        synchronized (this.transactions){
            transactions.remove(idTransaction);
            clientTime.remove(idTransaction);
            votes.remove(idTransaction);
            hasCommitted.remove(idTransaction);
        }
    }


}
