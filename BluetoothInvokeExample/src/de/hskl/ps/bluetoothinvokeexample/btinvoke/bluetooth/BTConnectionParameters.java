package de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth;

import java.util.UUID;

/**
 * Constants for the Bluetooth connection.
 * @author Patrick Schwartz
 * @date 2015
 */
public final class BTConnectionParameters {
    /** Bluetooth service name */
    public static final String APP_BT_SERVICE_NAME = "BluetoothInvocation";
    /** UUID for the connection. Needed on both sides. Was randomly generated */
    public static final UUID APP_BT_UUID = UUID.fromString("097bf674-4569-41ef-aa83-7d9d0e034cdf");
    
    private BTConnectionParameters(){}
}
