package de.hskl.ps.bluetoothinvokeexample.btinvocation;

public class BTInvocationException extends Exception {

    private static final long serialVersionUID = 2864705623960965359L;
    
    public BTInvocationException(String what) {
        super(what);
    }
    
    public BTInvocationException(Throwable t) {
        super(t);
    }
}
