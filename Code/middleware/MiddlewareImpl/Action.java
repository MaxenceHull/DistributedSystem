package MiddlewareImpl;

import java.lang.reflect.Method;

public class Action {
    Method method;
    Object[] parameters;

    public Action(Method method, Object[] parameters){
        this.method = method;
        this.parameters = parameters;
    }

    public Object[] getParameters() {
        return parameters;
    }

}
