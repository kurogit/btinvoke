package de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions;

public class BTConnectionException extends Exception {
    public BTConnectionException(String what) {
        super(what);
    }
    
    public BTConnectionException(String what, Throwable t) {
        super(what, t);
    }
}
