package de.hskl.ps.bluetoothinvokeexample.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTGlobals;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.IBTConnectionHandler;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * 
 * Responsibility:
 * 1. Receive Strings over Bluetooth
 * 2. 
 *  
 * @author Patrick Schwartz
 *
 */
@EService
public class BTInvokeClientService extends Service implements IBTConnectionHandler {

    /** Tag for Logging */
    private static final String TAG = BTInvokeClientService.class.getSimpleName();
    
    LocalBroadcastManager broadcast_ = null;
    
    private BluetoothAdapter adapter_ = null;
    private BluetoothSocket socket_ = null;
    private InputStream readStream_ = null;
    private OutputStream writeStream_ = null;

    private ConnectionStatus status_ = ConnectionStatus.DISABLED;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        broadcast_ = LocalBroadcastManager.getInstance(this);
        
        adapter_ = BluetoothAdapter.getDefaultAdapter();

        if(adapter_.isEnabled())
            status_ = ConnectionStatus.NOT_CONNECTED;

        connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        cancelConnection();
    }
    
    @Override
    public ConnectionStatus status() {
        return status_;
    }

    @Override
    public void connect() {
        // Check if we are currently connecting
        if(status_ == ConnectionStatus.CONNECTING) {
            final String msg = "We are already connecting";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }

        // Check if we are already connected
        if(socket_ != null && socket_.isConnected()) {
            final String msg = "We are already connected to a remote device!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }

        connectToDevice();
    }

    @Background(id="connect_thread")
    void connectToDevice() {
        BetterLog.i(TAG, "connectBluetooth");
        
        // Discovery is expensive and interferes with the connection.
        adapter_.cancelDiscovery();

        Set<BluetoothDevice> devices = adapter_.getBondedDevices();
        if(devices.isEmpty()) {
            BetterLog.d(TAG, "No bonded devices!");
            return;
        }

        for(BluetoothDevice device : devices) {
            status_ = ConnectionStatus.CONNECTING;
            try {
                socket_ = device.createRfcommSocketToServiceRecord(BTGlobals.APP_BT_UUID);
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Failed to create socket!");
                continue;
            }

            try {
                BetterLog.d(TAG, "Trying to connect to %s", device.getName());
                socket_.connect();
            } catch(IOException e) {
                BetterLog.e(TAG, "Unable to connect to %s", device.getName());
                continue;
            }
            
            // Successfully connected
            if(socket_.isConnected()) {
                BetterLog.i(TAG, "Socket succesfully connected to %s", socket_.getRemoteDevice().getName());
                try {
                    readStream_ = socket_.getInputStream();
                    writeStream_ = socket_.getOutputStream();
                } catch(IOException e) {
                    BetterLog.e(TAG, e, "Unable to get Streams!");
                    // Our connection is useless now
                    cancelConnection();
                    continue;
                }                
                // All good
                status_ = ConnectionStatus.CONNECTED;
                break;
            } else {
                status_ = ConnectionStatus.NOT_CONNECTED;
            }

        }
        readLoop();
    }
    
    @Background(id="read_thread")
    protected void readLoop() {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        
        while(true) {
            try {
                bytes = readStream_.read(buffer);
                BetterLog.d(TAG, "Succesfully read %d bytes", bytes);
                String recievedString = new String(buffer.clone(), 0, bytes);
                
                Object result = BTInvokeMethodManager.getInstance().callMethodFromJSON(recievedString);
                
                String s = "Result: " + result;
                
                writeStream_.write(s.getBytes());
                
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Exception while reading");
            } catch(MethodCallException e) {
                BetterLog.e(TAG, e, "Exception while calling the Method");
            }
        }
    }
    
    private void cancelConnection() {
        if(socket_ != null) {
            try {
                socket_.close();
            } catch(IOException e) {
                final String msg = "Unable to close Bluetoothsocket";
                BetterLog.e(TAG, e, msg);
            } finally {
                socket_ = null;
                status_ = ConnectionStatus.NOT_CONNECTED;
            }
        }
    }
    
    private void postStatusMessage(String msg) {
        // TODO Auto-generated method stub

    }

}
