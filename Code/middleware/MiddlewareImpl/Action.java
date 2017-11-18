package MiddlewareImpl;

import java.io.Serializable;

public class Action implements Serializable{
    String method;
    Object[] parameters;

    public Action(String method, Object[] parameters){
        this.method = method;
        this.parameters = parameters;
    }


}
