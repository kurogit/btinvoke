package de.hskl.ps.bluetoothinvokeexample.btinvocation;

public class MethodCallException extends Exception {

    private static final long serialVersionUID = 5700037900409905815L;

    public MethodCallException(String what) {
        super(what);
    }

    public MethodCallException(Throwable t) {
        super(t);
    }
    
    public MethodCallException(String what, Throwable t) {
        super(what, t);
    }
}
