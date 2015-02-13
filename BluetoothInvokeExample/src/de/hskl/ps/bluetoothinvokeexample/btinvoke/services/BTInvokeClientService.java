package de.hskl.ps.bluetoothinvokeexample.btinvoke.services;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTClientConnection;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTConnectionMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.ConnectionStatus;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTConnectionException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationRequest;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationResult;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeError;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * 
 * Responsibility: 1. Receive Strings over Bluetooth 2.
 * 
 * @author Patrick Schwartz
 *
 */
@EService
public class BTInvokeClientService extends Service {

    public static final String ACTION_CONNECT = "ACTION_CONNECT";

    /** Tag for Logging */
    private static final String TAG = BTInvokeClientService.class.getSimpleName();

    private LocalBroadcastManager broadcast_ = null;

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
                result = BTInvokeError.ERROR_RESULT;
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
    
    private void sendStatusMessage(final int type) {
        Intent i =new Intent(BTInvokeMessages.ACTION_STATUS_MESSAGE);
        i.putExtra(BTInvokeMessages.Extras.STATUS_TYPE, type);
        
        broadcast_.sendBroadcast(i);
    }
    
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
