package de.hskl.ps.bluetoothinvokeexample.btinvoke.services;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTConnection;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTServerConnection;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTConnectionException;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * Service for BTInvoke on the server side.
 * <p>
 * Listens for the {@link BTInvokeMessages#REMOTE_INVOCATION} broadcast action. If the broadcast is
 * Received the JSON String will be send over Bluetooth to the client device. It then waits for the
 * answer of the client device and sends a {@link BTInvokeMessages#REMOTE_INVOCATION_RESULT}
 * broadcast containing the result.
 * <p>
 * Will also send several status message broadcast with the
 * {@link BTInvokeMessages#ACTION_STATUS_MESSAGE} action. Possible types are defined in
 * {@link BTInvokeMessages.Status} and {@link BTInvokeMessages.Errors}.
 * <p>
 * Uses {@link EService} from Android Annotations for starting background threads.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
@EService
public class BTInvokeServerService extends Service {

    private static final String TAG = "BTIServerService";

    /** The server connection */
    @Bean
    BTServerConnection connection_;

    /** Reference to the local broadcast manager */
    private LocalBroadcastManager broadcast_ = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Destroy service if the app closes.
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Dont allow binding.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BetterLog.v(TAG, "onCreate");

        broadcast_ = LocalBroadcastManager.getInstance(this);

        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvokeMessages.REMOTE_INVOCATION));

        connection_.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BetterLog.v(TAG, "onDestroy");

        broadcast_.unregisterReceiver(broadCastReciever_);
        connection_.destroy();
    }

    /**
     * Sends a String over Bluetooth and waits for an answer.
     * <p>
     * If the Answer is received send it in an broadcast using the
     * {@link BTInvokeMessages#REMOTE_INVOCATION_RESULT} action.
     * <p>
     * Has to run in background since {@link BTConnection#readString()} is blocking.
     * 
     * @param s
     *            The string to send.
     */
    @Background(id = "send_and_wait_thread", serial = "send_and_wait_thread")
    void sendStringAndWaitForAnswer(String s) {
        try {
            sendStatusMessage(BTInvokeMessages.Status.NEW_INVOCATION_REQUEST);

            // Send string
            connection_.writeString(s);

            // Wait for answer.
            String recievedString = connection_.readString();

            sendStatusMessage(BTInvokeMessages.Status.RECIEVED_RESULT);

            Intent intent = new Intent(BTInvokeMessages.REMOTE_INVOCATION_RESULT);
            intent.putExtra(BTInvokeMessages.Extras.JSONSTRING, recievedString);
            broadcast_.sendBroadcast(intent);

        } catch(BTConnectionException e) {
            BetterLog.e(TAG, e, "Reading or writing failed");

            // Send failed Message
            sendStatusMessage(BTInvokeMessages.Errors.SENDING_STRING_FAILED);
        }
    }

    /**
     * Send a status message as a broadcast.
     * 
     * @param type
     *            The type of Status message as defined by {@link BTInvokeMessages.Status} and
     *            {@link BTInvokeMessages.Errors}
     */
    private void sendStatusMessage(final int type) {
        Intent i = new Intent(BTInvokeMessages.ACTION_STATUS_MESSAGE);
        i.putExtra(BTInvokeMessages.Extras.STATUS_TYPE, type);
        broadcast_.sendBroadcast(i);
    }

    /** {@code BroadcastReceiver} listening for {@link BTInvokeMessages#REMOTE_INVOCATION} */
    private final BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BTInvokeMessages.REMOTE_INVOCATION)) {
                String jsonString = intent.getExtras().getString(BTInvokeMessages.Extras.JSONSTRING);

                sendStringAndWaitForAnswer(jsonString);
            }
        }
    };
}
