package de.hskl.ps.bluetoothinvokeexample.bluetooth;

public class BTConnectionException extends Exception {
    public BTConnectionException(String what) {
        super(what);
    }
    
    public BTConnectionException(String what, Throwable t) {
        super(what, t);
    }
}