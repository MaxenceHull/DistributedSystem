package ResInterface;

public class TransactionAbortedException extends Exception{
    protected int xid = 0;

    public TransactionAbortedException (int xid, String msg)
    {
        super(msg);
        this.xid = xid;
    }

    public int getXId() {
        return this.xid;
    }
}
