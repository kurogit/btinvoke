package de.hskl.ps.bluetoothinvokeexample.bluetooth.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * Class for handling a Bluetooth connection.
 * 
 * @author Patrick Schwartz
 *
 */
public abstract class BTConnection {

    private static final String TAG = BTConnection.class.getSimpleName();

    
    protected BluetoothAdapter adapter_ = null;
    protected BluetoothSocket socket_ = null;
    protected InputStream readStream_ = null;
    protected OutputStream writeStream_ = null;
    
    private ConnectionStatus status_ = ConnectionStatus.DISABLED;
    private byte[] readBuffer_ = null;
    
    
    private Context context_ = null;
    private LocalBroadcastManager broadcast_ = null;

    public BTConnection(Context c) {
        context_ = c;

        adapter_ = BluetoothAdapter.getDefaultAdapter();

        broadcast_ = LocalBroadcastManager.getInstance(c);

        if(adapter_.isEnabled())
            changeStatus(ConnectionStatus.NOT_CONNECTED);
        
        // Listen for BluetoothAdapter changes
        context_.registerReceiver(broadcastReciever_, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        
        readBuffer_ = new byte[512];
    }

    public void destroy() {
        cancelConnection();
        cancelThread();
        context_.unregisterReceiver(broadcastReciever_);
    }
    
    public void connect() {
        if(status() == ConnectionStatus.DISABLED) {
            BetterLog.i(TAG, "Bluetooth is disabled!");
            reportError(BTConnectionMessages.Errors.BLUETOOTH_DISABLED);
            return;
        }

        if(status() == ConnectionStatus.CONNECTED) {
            BetterLog.i(TAG, "There is already a connection present!");
            reportError(BTConnectionMessages.Errors.ALREADY_CONNECTED);
            return;
        }
        
        // Let subclass handle the rest
        doConnect();
    }
        
    public void writeString(String s) throws BTConnectionException {
        if(status_ != ConnectionStatus.CONNECTED) {
            BetterLog.i(TAG, "Bluetooth ist not connected!");
            reportError(BTConnectionMessages.Errors.NOT_CONNECTED);
            throw new BTConnectionException("Bluetooth is not connected!");
        }
        
        try {
            writeStream_.write(s.getBytes());
        } catch(IOException e) {
            // socket is somehow closed
            cancelConnection();
            throw new BTConnectionException("Socket was closed", e);
        }
    }
    
    public String readString() throws BTConnectionException {
        if(status_ != ConnectionStatus.CONNECTED) {
            BetterLog.i(TAG, "Bluetooth ist not connected!");
            reportError(BTConnectionMessages.Errors.NOT_CONNECTED);
            throw new BTConnectionException("Bluetooth not connected");
        }
        
        try {            
            int bytes = readStream_.read(readBuffer_);
            BetterLog.d(null, "Succesfully read %d bytes", bytes);
            
            return new String(readBuffer_.clone(), 0, bytes);
        } catch(IOException e) {
            // socket is somehow closed
            cancelConnection();
            throw new BTConnectionException("Socket was closed", e);
        }
    }
    
    public boolean isConnected() {
        return status_ == ConnectionStatus.CONNECTED;
    }
    
    protected ConnectionStatus status() {
        return status_;
    }
    protected void cancelConnection() {
        if(socket_ != null) {
            try {
                socket_.close();
            } catch(IOException e) {
                final String msg = "Unable to close Bluetoothsocket";
                BetterLog.e(TAG, e, msg);
            } finally {
                socket_ = null;
            }
        }
        changeStatus(ConnectionStatus.NOT_CONNECTED);
    }
    
    protected void changeStatus(ConnectionStatus newStatus) {
        Intent i = new Intent(BTConnectionMessages.CONNECTION_STATUS_MESSAGE);
        i.putExtra(BTConnectionMessages.EXTRA_TYPE, newStatus.ordinal());
        if(newStatus == ConnectionStatus.CONNECTED)
            i.putExtra(BTConnectionMessages.EXTRA_DEVICE, socket_.getRemoteDevice().getName());
        
        broadcast_.sendBroadcast(i);
        
        BetterLog.i(TAG, "Changed connection status. New status: %s", newStatus.toString());
        
        status_ = newStatus;
    }
    
    protected void reportError(final int errorType) {
        Intent i = new Intent(BTConnectionMessages.CONNECTION_STATUS_MESSAGE);
        i.putExtra(BTConnectionMessages.EXTRA_TYPE, errorType);
        
        broadcast_.sendBroadcast(i);
    }
        
    protected abstract void cancelThread();
    protected abstract void doConnect();
    
    private final BroadcastReceiver broadcastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                
                // Bluetooth was turned off
                if(newState == BluetoothAdapter.STATE_OFF) {
                    BetterLog.i(TAG, "Bluetooth was turned off! Canceling threads and the connection.");
                    cancelThread();
                    cancelConnection();
                    changeStatus(ConnectionStatus.DISABLED);
                } else if(newState == BluetoothAdapter.STATE_ON && status_ == ConnectionStatus.DISABLED) {
                    status_ = ConnectionStatus.NOT_CONNECTED;
                }
            }
        }
    };

}
