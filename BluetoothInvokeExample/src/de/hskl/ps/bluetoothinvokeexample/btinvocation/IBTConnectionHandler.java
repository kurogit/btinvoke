package de.hskl.ps.bluetoothinvokeexample.btinvocation;

public interface IBTConnectionHandler {
    public enum ConnectionStatus {
        DISABLED, NOT_CONNECTED, CONNECTING, CONNECTED
    }
    
    public ConnectionStatus status();
    
    public void connect();
}
