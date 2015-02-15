package de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth;

import java.io.IOException;
import java.util.Set;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.api.BackgroundExecutor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * Handling a Bluetooth connection as a client.
 * <p>
 * For this to work the device running the server has to be paired and bonded already.
 * <p>
 * Uses {@link EBean} from Android Annotation for the background threads. Has to be injected with {@link Bean}.
 * @author Patrick Schwartz
 * @date 2015
 */
@EBean
public class BTClientConnection extends BTConnection {

    private static final String TAG = BTConnection.class.getSimpleName();

    private static final String THREAD_NAME = "connect_thread";
    
    /** Construct a new BTClientConnection object. Context required for sending broadcasts */
    public BTClientConnection(Context c) {
        super(c);
    }
    
    /**
     * Try to connect to a bonded device. Has to be run in a background thread since connecting is a
     * blocking operation.
     */
    @Background(id = THREAD_NAME, serial = THREAD_NAME)
    void connectAsClient() {

        if(status() == ConnectionStatus.CONNECTING) {
            BetterLog.i(TAG, "Bluetooth is currently still connecting!");
            reportError(BTConnectionMessages.Errors.ALREADY_CONNECTING);
            return;
        }

        // Discovery is expensive and interferes with the connection.
        adapter_.cancelDiscovery();

        Set<BluetoothDevice> devices = adapter_.getBondedDevices();
        if(devices.isEmpty()) {
            BetterLog.d(TAG, "No bonded Devices!");
            reportError(BTConnectionMessages.Errors.NO_BONDED_DEVICES);
            return;
        }

        for(BluetoothDevice device : devices) {
            changeStatus(ConnectionStatus.CONNECTING);
            try {
                socket_ = device.createRfcommSocketToServiceRecord(BTConnectionParameters.APP_BT_UUID);
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
                changeStatus(ConnectionStatus.CONNECTED);
                break;
            } else {
                changeStatus(ConnectionStatus.NOT_CONNECTED);
            }

        }

        if(status() != ConnectionStatus.CONNECTED) {
            BetterLog.i(TAG, "Was not able to connect to any device");
            reportError(BTConnectionMessages.Errors.CONNECTING_FAILED);
        }
    }

    @Override
    protected void doConnect() {
        connectAsClient();
    }

    @Override
    protected void cancelThread() {
        BackgroundExecutor.cancelAll(THREAD_NAME, true);
    }
}
