package de.hskl.ps.bluetoothinvokeexample.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.api.BackgroundExecutor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTGlobals;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.IBTConnectionHandler;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EService
public class BTInvocationServerService extends Service implements IBTConnectionHandler {

    private final String TAG = "BTIServerService";   

    private BluetoothAdapter adapter_ = null;
    private BluetoothSocket socket_ = null;
    private InputStream readStream_ = null;
    private OutputStream writeStream_ = null;
    
    private LocalBroadcastManager broadcast_ = null;
    
    // IBTConnectionHandler interface
    private ConnectionStatus connectionStatus_ = ConnectionStatus.DISABLED;
    
    private BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
           if(intent.getAction().equals(BTInvocationMessages.REMOTE_EXECUTE)) {
               String jsonString = intent.getExtras().getString(BTInvokeExtras.JSONSTRING);
               
               sendStringAndWaitForAnswer(jsonString);
               
               BetterLog.d(TAG, "I am the service and i recieved: %s", jsonString);
           }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        BetterLog.v(TAG, "onCreate");
        
        adapter_ = BluetoothAdapter.getDefaultAdapter();
        
        broadcast_ = LocalBroadcastManager.getInstance(this);
        
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.REMOTE_EXECUTE));
        
        connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BetterLog.v(TAG, "onDestroy");
        
        // Stop all threads
        BackgroundExecutor.cancelAll("accept_thread", true);
        
        broadcast_.unregisterReceiver(broadCastReciever_);
    }

    @Override
    public void connect() {
        BetterLog.v(TAG, "connect");
        
        // Check if we are currently connecting
        if(connectionStatus_ == ConnectionStatus.CONNECTING) {
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
        
        acceptConnections();
    }
    
    @Background(id = "accept_thread", serial = "accept_thread")
    void acceptConnections() {
        connectionStatus_ = ConnectionStatus.CONNECTING;
        BluetoothServerSocket bss = null;
        try {
            bss = adapter_.listenUsingRfcommWithServiceRecord(BTGlobals.APP_BT_SERVICE_NAME, BTGlobals.APP_BT_UUID);
        } catch(IOException e) {
            final String msg = "Could not Create BluetoothServerSocket";
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
            BetterLog.i(TAG, "Succesfully established connection to %s", socket_.getRemoteDevice().getName());
            postStatusMessage("Established Connection to" + socket_.getRemoteDevice().getName());
            try {
                readStream_ = socket_.getInputStream();
            } catch(IOException e) {
                final String msg = "Unable to open Inputstream";
                BetterLog.e(TAG, e, msg);
                postStatusMessage(msg);
                // We cant do our work now
                cancelConnection();
                return;
            }
            try {
                writeStream_ = socket_.getOutputStream();
            } catch(IOException e) {
                final String msg = "Unable to open Outputstream";
                BetterLog.e(TAG, e, msg);
                postStatusMessage(msg);
                // We cant do our work now
                cancelConnection();
                return;
            }
            try {
                bss.close();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Unable to close BluetoothServerSocket");
                // This is weird, but no need to post it
            }
            
            connectionStatus_ = ConnectionStatus.CONNECTED;
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
                connectionStatus_ = ConnectionStatus.NOT_CONNECTED;
            }
        }
    }

    @Background(id = "read_thread")
    void readLoop() {
        byte[] buffer = new byte[1024];
        int bytes;

        while(true) {
            try {
                bytes = readStream_.read(buffer);
                String msg = new String(buffer.clone(), 0, bytes);
                BetterLog.d(TAG, "Succesfully read %d bytes : %s", bytes, msg);
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Exception while reading");
            }
        }
    }
    
    
    @Background(id="send_and_wait_thread", serial="send_and_wait_thread")
    void sendStringAndWaitForAnswer(String s) {
        try {
            writeStream_.write(s.getBytes());
            
            byte[] buffer = new byte[1024];
            int readByteCount = readStream_.read(buffer);
            String recievedString = new String(buffer, 0, readByteCount);
            
            Intent intent = new Intent(BTInvocationMessages.REMOTE_EXECUTE_RESULT);
            intent.putExtra(BTInvokeExtras.JSONSTRING, recievedString);
            broadcast_.sendBroadcast(intent);
            
        } catch(IOException e) {
           BetterLog.e(TAG, e, "");
           // TODO
        }
    }

    @Override
    public ConnectionStatus status() {
        return connectionStatus_;
    }

    public void postStatusMessage(String msg) {
        //TODO
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
