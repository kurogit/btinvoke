package de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions;

/**
 * Exception for errors while calling a method on the client device.
 * 
 * @author Patrick Schwartz
 * @date 2015
 *
 */
public class MethodCallException extends Exception {

    private static final long serialVersionUID = 5700037900409905815L;

    public MethodCallException(String what, Throwable t) {
        super(what, t);
    }
}
