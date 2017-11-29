package ResImpl;

import java.io.Serializable;
import java.util.Hashtable;

public class Log implements Serializable{
    public Hashtable<Integer, RMHashtable> writtenItemsByTrx = new Hashtable<>();
    public Hashtable<Integer, Boolean> hasCommited = new Hashtable<>();
}
