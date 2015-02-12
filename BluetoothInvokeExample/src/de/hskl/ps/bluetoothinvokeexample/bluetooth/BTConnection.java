package de.hskl.ps.bluetoothinvokeexample.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.api.BackgroundExecutor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.R;
import de.hskl.ps.bluetoothinvokeexample.constants.BTGlobals;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * Class for handling a Bluetooth connection.
 * 
 * @author Patrick Schwartz
 *
 */
@EBean
public class BTConnection {

    /** Connection status */
    public enum Status {
        DISABLED, NOT_CONNECTED, ACCEPTING, CONNECTING, CONNECTED
    }

    private static final String TAG = BTConnection.class.getSimpleName();

    private Status status_ = Status.DISABLED;

    private BluetoothAdapter adapter_ = null;
    private BluetoothSocket socket_ = null;
    private InputStream readStream_ = null;
    private OutputStream writeStream_ = null;
    private byte[] readBuffer_ = null;
    
    
    private Context context_ = null;
    private LocalBroadcastManager broadcast_ = null;

    public BTConnection(Context c) {
        context_ = c;

        adapter_ = BluetoothAdapter.getDefaultAdapter();

        broadcast_ = LocalBroadcastManager.getInstance(c);

        if(adapter_.isEnabled())
            status_ = Status.NOT_CONNECTED;
        
        // Listen for BluetoothAdapter changes
        context_.registerReceiver(broadcastReciever_, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        
        readBuffer_ = new byte[512];
    }

    public void destroy() {
        cancelConnection();
        cancelThreads();
        context_.unregisterReceiver(broadcastReciever_);
    }

    /** Returns the current connection status */
    public Status status() {
        return status_;
    }
    
    public boolean isConnected() {
        return status_ == Status.CONNECTED;
    }
    
    public void writeString(String s) throws IOException {
        if(status_ != Status.CONNECTED) {
            final String msg = "Bluetooth ist not connected!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }
        
        try {
            writeStream_.write(s.getBytes());
        } catch(IOException e) {
            // socket is somehow closed
            cancelConnection();
            throw e;
        }
    }
    
    public String readString() throws IOException {
        if(status_ != Status.CONNECTED) {
            final String msg = "Bluetooth ist not connected!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return null;
        }
        
        try {            
            int bytes = readStream_.read(readBuffer_);
            BetterLog.d(null, "Succesfully read %d bytes", bytes);
            
            return new String(readBuffer_.clone(), 0, bytes);
        } catch(IOException e) {
            // socket is somehow closed
            cancelConnection();
            throw e;
        }
    }
    
    @Background(id = "accept_thread", serial = "accept_thread")
    public void acceptConnection() {
        
        if(status_ == Status.DISABLED) {
            final String msg = "Bluetooth is disabled!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }
        
        if(status_ == Status.ACCEPTING) {
            final String msg = "Bluetooth is currently still connecting!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }
        
        if(status_ == Status.CONNECTED) {
            final String msg = "There is already a connection present!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }
        
        status_ = Status.ACCEPTING;
        BluetoothServerSocket bss = null;
        try {
            bss = adapter_.listenUsingRfcommWithServiceRecord(BTGlobals.APP_BT_SERVICE_NAME, BTGlobals.APP_BT_UUID);
        } catch(IOException e) {
            final String msg = "Could not create BluetoothServerSocket";
            BetterLog.e(TAG, e, msg);
            postStatusMessage(msg);
            cancelConnection();
            return;
        }

        // Act as server and accept a connection.
        try {
            BetterLog.i(TAG, "Accepting Connections");
            postStatusMessage("Accepting Bluetooth Connections");
            socket_ = bss.accept();
        } catch(IOException e) {
            final String msg = "Accepting Bluetooth connection failed";
            BetterLog.e(TAG, e, msg);
            postStatusMessage(msg);
            cancelConnection();
            return;
        }

        // Open input and output streams
        if(socket_ != null) {
            try {
                readStream_ = socket_.getInputStream();
                writeStream_ = socket_.getOutputStream();
            } catch(IOException e) {
                final String msg = "Unable to open stream";
                BetterLog.e(TAG, e, msg);
                postStatusMessage(msg);
                // We cant do our work now
                cancelConnection();
                return;
            }
            BetterLog.i(TAG, "Succesfully established connection to %s", socket_.getRemoteDevice().getName());
            postStatusMessage("Established Connection to" + socket_.getRemoteDevice().getName());
            status_ = Status.CONNECTED;

            try {
                bss.close();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Unable to close BluetoothServerSocket");
                // This is weird, but no need to post it
            }
        }
    }

    @Background(id="connect_thread", serial="connect_thread")
    public void connectAsClient() {
        
        if(status_ == Status.DISABLED) {
            final String msg = "Bluetooth is disabled!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }
        
        if(status_ == Status.CONNECTING) {
            final String msg = "Bluetooth is currently still connecting!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }
        
        if(status_ == Status.CONNECTED) {
            final String msg = "There is already a connection present!";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
            return;
        }
        
        // Discovery is expensive and interferes with the connection.
        adapter_.cancelDiscovery();

        Set<BluetoothDevice> devices = adapter_.getBondedDevices();
        if(devices.isEmpty()) {
            final String msg = context_.getResources().getString(R.string.NO_BONDED_DEVICES);
            BetterLog.d(TAG, msg);
            postStatusMessage(msg);
            return;
        }

        for(BluetoothDevice device : devices) {
            status_ = Status.CONNECTING;
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
                BetterLog.e(TAG, e, "Unable to connect to %s", device.getName());
                continue;
            }
            
            if(socket_.isConnected()) {
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
                BetterLog.i(TAG, "Socket succesfully connected to %s", socket_.getRemoteDevice().getName());
                postStatusMessage("Established Connection to" + socket_.getRemoteDevice().getName());
                status_ = Status.CONNECTED;
                break;
            } else {
                status_ = Status.NOT_CONNECTED;
            }

        }
        
        if(status_ != Status.CONNECTED) {
            final String msg = "Was not able to connect to any device";
            BetterLog.i(TAG, msg);
            postStatusMessage(msg);
        }
    }
    
    private void postStatusMessage(String string) {
        Intent i = new Intent(BTInvocationMessages.BT_STATUS_MESSAGE);
        i.putExtra(BTInvokeExtras.BT_STATUS_MESSAGE, string);
        broadcast_.sendBroadcast(i);

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
            }
        }
        status_ = Status.NOT_CONNECTED;
    }
    
    private void cancelThreads() {
        BackgroundExecutor.cancelAll("accept_thread", true);
        BackgroundExecutor.cancelAll("connect_thread", true);
    }

    private final BroadcastReceiver broadcastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                
                // Bluetooth was turned off
                if(newState == BluetoothAdapter.STATE_OFF) {
                    BetterLog.i(TAG, "Bluetooth was turned off! Canceling threads and the connection.");
                    cancelThreads();
                    cancelConnection();
                    status_ = Status.DISABLED;
                } else if(newState == BluetoothAdapter.STATE_ON && status_ == Status.DISABLED) {
                    status_ = Status.NOT_CONNECTED;
                }
            }
        }
    };

}
