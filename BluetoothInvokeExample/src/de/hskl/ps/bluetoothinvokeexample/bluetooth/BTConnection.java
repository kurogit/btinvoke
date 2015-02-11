package de.hskl.ps.bluetoothinvokeexample.bluetooth;

import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public class BTConnection {
    
    public enum Status {
        DISABLED,
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED
    }
    
    private static final String TAG = BTConnection.class.getSimpleName();
    
    private Context context_ = null;
    private BluetoothAdapter adapter_ = null;
    private Status status_ = Status.NOT_CONNECTED;
    
    public BTConnection(Context context) {
        context_ = context;
        
        adapter_ = BluetoothAdapter.getDefaultAdapter();
        
        if(adapter_ == null) {
            BetterLog.e(TAG, "No Bluetooth Available!");
            return;
        }
        
        if(!adapter_.isEnabled()) {
            BetterLog.i(TAG, "Bluetooth is disabled. Trying to enable");
            
            status_ = Status.DISABLED;
            
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context_.startActivity(intent);
        } else {
            // Bluetooth already enabled
            status_ = Status.NOT_CONNECTED;
        }
    }
    
    public Status status() {
        return status_;
    }
    
    public void acceptConnection() {
        
    }
}
