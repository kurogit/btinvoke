package de.hskl.ps.bluetoothinvokeexample.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.api.BackgroundExecutor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import de.hskl.ps.bluetoothinvokeexample.blueooth.BluetoothGlobals;
import de.hskl.ps.bluetoothinvokeexample.blueooth.IBluetoothServerService;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EService
public class BtInvocationService extends Service implements IBluetoothServerService, InvocationHandler {

    public final class BluetoothInvocationBinder extends Binder{
        public BtInvocationService getService() {
            return BtInvocationService.this;
        }
    }

    private final String TAG = "BTInovationService";

    private final IBinder binder_ = new BluetoothInvocationBinder();

    private BluetoothAdapter adapter_ = null;
    private InputStream readStream_ = null;
    private OutputStream writeStream_ = null;

    @Override
    public IBinder onBind(Intent intent) {
        BetterLog.d(TAG, "Binding with Intent: %s", intent.toString());
        
        return binder_;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BetterLog.v(TAG, "onCreate");
        
        adapter_ = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop all threads
        BackgroundExecutor.cancelAll("accept_thread", true);
        BackgroundExecutor.cancelAll("read_thread", true);
    }

    @Background(id = "accept_thread")
    @Override
    public void acceptBluetoothConnection() {
        BetterLog.v(TAG, "acceptBluetoothConnection");
        BluetoothServerSocket bss = null;
        try {
            bss = adapter_.listenUsingRfcommWithServiceRecord(BluetoothGlobals.APP_BT_SERVICE_NAME, BluetoothGlobals.APP_BT_UUID);
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
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        // Turn method to Json
        // Write json string
        // read (Blocking!) result
        // turn result json to Object of returntype
        // return result
        return null;
    }
}
