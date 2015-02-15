package de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions;

/**
 * Exception for errors while preparing to call the method.
 * <p>
 * Exception is thrown during processing on the server device.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public class BTInvocationException extends Exception {

    private static final long serialVersionUID = 2864705623960965359L;

    public BTInvocationException(String what) {
        super(what);
    }

    public BTInvocationException(String what, Throwable e) {
        super(what, e);
    }
}
