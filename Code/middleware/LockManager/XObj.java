package LockManager;

import java.io.Serializable;

public class XObj implements Serializable
{
    protected int xid = 0;
    
    XObj() {
        super();
        this.xid = 0;
    }
    
    XObj(int xid) {
        super();
        
        if (xid > 0) {
            this.xid = xid;
        } else {
            this.xid = 0;
        }
    }
    
    public String toString() {
        String outString = new String(this.getClass() + "::xid(" + this.xid + ")");
        return outString;
    }
    
    public int getXId() {
        return this.xid;
    }
    
    public int hashCode() {
        return this.xid;
    }
    
    public boolean equals(Object xobj) {
        if (xobj == null) return false;
        
        if (xobj instanceof XObj) {
            int _id = ((XObj)xobj).getXId();
            if (this.xid == _id) {
                return true;
            }
        }
        return false;
    }
    
    public Object clone() {
        try {
            XObj xobj = (XObj)super.clone();
            xobj.SetXId(this.xid);
            return xobj;
        } catch (CloneNotSupportedException clonenotsupported) {
            return null;
        }
    }
    
    public int key() {
        return this.xid;
    }

    // Used by clone.
    public void SetXId(int xid) {
        if (xid > 0) {
            this.xid = xid;
        }
        return;
    }
}
