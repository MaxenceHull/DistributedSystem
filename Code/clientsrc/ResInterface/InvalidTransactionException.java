package ResInterface;

public class InvalidTransactionException extends Exception{
    protected int xid = 0;

    public InvalidTransactionException (int xid, String msg)
    {
        super(msg);
        this.xid = xid;
    }

    public int getXId() {
        return this.xid;
    }
}
