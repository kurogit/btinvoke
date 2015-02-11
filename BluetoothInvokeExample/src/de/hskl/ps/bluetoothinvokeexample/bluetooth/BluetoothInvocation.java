package de.hskl.ps.bluetoothinvokeexample.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.api.BackgroundExecutor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTGlobals;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EBean
public class BluetoothInvocation {
    
    private static final String TAG = BluetoothInvocation.class.getSimpleName();
    
    @RootContext
    Context context_;
    
    private BluetoothAdapter adapter_ = null;
    
    private InputStream readStream_ = null;
    private OutputStream writeStream_ = null;
    
    
    public BluetoothInvocation() {
        adapter_ = BluetoothAdapter.getDefaultAdapter();
        if(adapter_ == null) {
            BetterLog.e(TAG, "Bluetooth is not avaiable");
        }
        
        if(!adapter_.isEnabled()) {
            BetterLog.e(TAG, "Bluetooth is not enabled!");
        }
    }
    
    public void destroy() {
        BackgroundExecutor.cancelAll("accept_thread", true);
    }
    
    @Background(id = "accept_thread")
    public void acceptBluetoothConnection() {
        BetterLog.v(TAG, "acceptBluetoothConnection");
        BluetoothServerSocket bss = null;
        try {
            bss = adapter_.listenUsingRfcommWithServiceRecord(BTGlobals.APP_BT_SERVICE_NAME, BTGlobals.APP_BT_UUID);
        } catch(IOException e) {
            BetterLog.e(TAG, e, "Could not Create BluetoothServerSocket");
            return;
        }

        // Act as server and accept a connection.
        BluetoothSocket socket = null;
        try {
            BetterLog.i(TAG, "Accepting Connections");
            socket = bss.accept();
        } catch(IOException e) {
            BetterLog.e(TAG, e, "Accepting Bluetooth connection failed");
            return;
        }

        // Open input and outputstreams
        if(socket != null) {
            BetterLog.i(TAG, "Succesfully established connection to %s", socket.getRemoteDevice().getName());
            try {
                readStream_ = socket.getInputStream();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Unable to open Inputstream");
            }
            try {
                writeStream_ = socket.getOutputStream();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Unable to open Outputstream");
            }
            try {
                bss.close();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Unable to close BluetoothServerSocket");
            }
        }
    }
}
