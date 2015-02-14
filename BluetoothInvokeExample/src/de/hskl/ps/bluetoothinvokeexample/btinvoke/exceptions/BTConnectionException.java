package de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions;

/**
 * Exception for errors during the connection process.
 * 
 * @author Patrick Schwartz
 */
public class BTConnectionException extends Exception {

    private static final long serialVersionUID = -2513028870604807218L;

    public BTConnectionException(String what) {
        super(what);
    }

    public BTConnectionException(String what, Throwable t) {
        super(what, t);
    }
}
