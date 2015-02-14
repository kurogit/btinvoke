package de.hskl.ps.bluetoothinvokeexample.btinvoke.services;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages.Result;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTClientConnection;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTConnection;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTConnectionMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.ConnectionStatus;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTConnectionException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationRequest;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationResult;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * Service for BTInvoke on the client side.
 * <p>
 * Will not immediately try to connect. Use {@link Activity#startService(Intent)} with an
 * {@link Intent} containing the {@link BTInvokeClientService#ACTION_CONNECT} action. Then it
 * listens for the broadcast with the {@link BTConnectionMessages#CONNECTION_STATUS_MESSAGE} action
 * of type {@link ConnectionStatus#CONNECTED}. Then starts reading String from the Bluetooth
 * connection inside a loop. If it successfully reads a send String it will convert it to a
 * {@link RemoteInvocationRequest} and try to call the method. This can only work if the method was
 * registered using {@link BTInvokeMethodManager#registerInterfaceAndImplementation(Class, Object)}.
 * Tries to send a result back even if the call failed. Unfortunately there are still some
 * situations where no result can be send back to the server side.
 * <p>
 * Will send several status message broadcast with the
 * {@link BTInvokeMessages#ACTION_STATUS_MESSAGE} action. Possible types are defined in
 * {@link BTInvokeMessages.Status} and {@link BTInvokeMessages.Errors}.
 * <p>
 * Uses {@link EService} from Android Annotations for starting background threads.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
@EService
public class BTInvokeClientService extends Service {

    public static final String ACTION_CONNECT = "ACTION_CONNECT";

    /** Tag for Logging */
    private static final String TAG = BTInvokeClientService.class.getSimpleName();

    /** Reference to the local broadcast manager */
    private LocalBroadcastManager broadcast_ = null;

    /** Client connection */
    @Bean
    BTClientConnection connection_;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if(action != null && action.equalsIgnoreCase(ACTION_CONNECT)) {
                connection_.connect();
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        broadcast_ = LocalBroadcastManager.getInstance(this);

        // We need to listen to the connected message before we can start the loop
        broadcast_.registerReceiver(broadcastReciever_, new IntentFilter(BTConnectionMessages.CONNECTION_STATUS_MESSAGE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        broadcast_.unregisterReceiver(broadcastReciever_);

        connection_.destroy();
    }

    /**
     * Start the read loop.
     * <p>
     * If a String is received over Bluetooth, it will converted to a
     * {@link RemoteInvocationRequest}. Then the Method defined in that request will be called and
     * the result send back over Bluetooth.
     * <p>
     * Has to run in background since {@link BTConnection#readString()} is blocking.
     */
    @Background(id = "read_thread", serial = "read_thread")
    void readLoop() {
        while(connection_.isConnected()) {
            String recievedString = null;
            try {
                recievedString = connection_.readString();
            } catch(BTConnectionException e) {
                BetterLog.e(TAG, e, "Exception while reading");
                // If reading fails we can't really send an answer
                sendStatusMessage(BTInvokeMessages.Errors.READING_STRING_FAILED);
                continue;
            }

            RemoteInvocationRequest r = null;
            try {
                r = RemoteInvocationRequest.fromJSONString(recievedString);
            } catch(JSONException e) {
                BetterLog.e(TAG, e, "Exception while converting incoming JSON");
                // We cant get to the id and method anymore.
                sendStatusMessage(BTInvokeMessages.Errors.READING_STRING_FAILED);
                continue;
            }

            sendStatusMessage(BTInvokeMessages.Status.HANDLING_REQUEST);

            Object result = null;
            try {
                result = BTInvokeMethodManager.getInstance().callMethod(r.methodName(), r.methodParams());
            } catch(MethodCallException e) {
                BetterLog.e(TAG, e, "Exception while calling method");
                result = Result.ERROR_RESULT;
                sendStatusMessage(BTInvokeMessages.Errors.CALLING_METHOD_FAILED);
            }

            sendStatusMessage(BTInvokeMessages.Status.METHOD_CALL_DONE);

            // Create result string
            JSONObject j = null;
            try {
                j = RemoteInvocationResult.toJSONObject(r.id(), result);
            } catch(JSONException e) {
                BetterLog.e(TAG, e, "Exception while creating answer JSON");
                // If this fails we can't do anything
                sendStatusMessage(BTInvokeMessages.Errors.SENDING_STRING_FAILED);
                continue;
            }

            try {
                connection_.writeString(j.toString());
            } catch(BTConnectionException e) {
                BetterLog.e(TAG, e, "Exception while writing");
                // If writing does not work we can't send the answer.
                sendStatusMessage(BTInvokeMessages.Errors.SENDING_STRING_FAILED);
                continue;
            }

        }
    }

    /**
     * Send a status message broadcast.
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

    /**
     * {@code BroadcastReceiver} listening for {@link BTConnectionMessages#CONNECTION_STATUS_MESSAGE}
     * of type {@link ConnectionStatus#CONNECTED}
     */
    private final BroadcastReceiver broadcastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BTConnectionMessages.CONNECTION_STATUS_MESSAGE)) {

                int type = intent.getIntExtra(BTConnectionMessages.EXTRA_TYPE, -1);
                if(type == ConnectionStatus.CONNECTED.ordinal()) {
                    // We are connected. Start the readLoop.
                    readLoop();
                }
            }
        }
    };
}
