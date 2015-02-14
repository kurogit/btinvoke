package de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth;

import java.io.IOException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.api.BackgroundExecutor;

import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * Handling a Bluetooth connection as a server.
 * <p>
 * Uses {@link EBean} from Android Annotation for the background threads. Has to be injected with
 * {@link Bean}.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
@EBean
public class BTServerConnection extends BTConnection {

    private static final String TAG = BTConnection.class.getSimpleName();

    private static final String THREAD_NAME = "connect_thread";

    /** Construct a new ServerConneciton Object. Context required for sending broadcasts */
    public BTServerConnection(Context c) {
        super(c);
    }

    /**
     * Accept a connection as a server. Has to be run in a background thread since accepting is a
     * blocking operation.
     */
    @Background(id = THREAD_NAME, serial = THREAD_NAME)
    public void acceptConnection() {

        if(status() == ConnectionStatus.ACCEPTING) {
            BetterLog.i(TAG, "Bluetooth is currently still accepting!");
            reportError(BTConnectionMessages.Errors.ALREADY_CONNECTING);
            return;
        }

        changeStatus(ConnectionStatus.ACCEPTING);
        BluetoothServerSocket bss = null;
        try {
            bss = adapter_.listenUsingRfcommWithServiceRecord(BTConnectionParameters.APP_BT_SERVICE_NAME, BTConnectionParameters.APP_BT_UUID);
        } catch(IOException e) {
            BetterLog.e(TAG, e, "Could not create BluetoothServerSocket");
            reportConnectionFailed();
            cancelConnection();
            return;
        }

        // Act as server and accept a connection.
        try {
            BetterLog.i(TAG, "Accepting Connections");
            socket_ = bss.accept();
        } catch(IOException e) {
            BetterLog.e(TAG, e, "Accepting Bluetooth connection failed");
            reportConnectionFailed();
            cancelConnection();
            return;
        }

        // Open input and output streams
        if(socket_ != null) {
            try {
                readStream_ = socket_.getInputStream();
                writeStream_ = socket_.getOutputStream();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Unable to open stream");
                reportConnectionFailed();
                // We can't do our work now
                cancelConnection();
                return;
            }
            BetterLog.i(TAG, "Succesfully established connection to %s", socket_.getRemoteDevice().getName());
            changeStatus(ConnectionStatus.CONNECTED);

            try {
                bss.close();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Unable to close BluetoothServerSocket");
                // This is weird, but no need to report it
            }
        }
    }

    @Override
    public void doConnect() {
        acceptConnection();
    }

    @Override
    protected void cancelThread() {
        BackgroundExecutor.cancelAll(THREAD_NAME, true);
    }

    /** Report a connection failure */
    private void reportConnectionFailed() {
        reportError(BTConnectionMessages.Errors.CONNECTING_FAILED);
    }
}
