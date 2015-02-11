package de.hskl.ps.bluetoothinvokeexample.btinvocation;

public interface IBTInvocationStatusMessageWriter {
    void setStatusMessageListener(IBTInvocationStatusMessageListener l);
    void postStatusMessage(String msg);
}
