package de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth;

/**
 * Bluetooth Connection status
 * 
 * @author Patrick Schwartz
 * @date 2015
 *
 */
public enum ConnectionStatus {
    /** The Bluetooth adapter is disabled. */
    DISABLED,
    /** No connection present */
    NOT_CONNECTED,
    /** A BTServerConnection is currently accepting connections. */
    ACCEPTING,
    /** A BTClientConnection is currently connecting. */
    CONNECTING,
    /** Connection was established */
    CONNECTED
}