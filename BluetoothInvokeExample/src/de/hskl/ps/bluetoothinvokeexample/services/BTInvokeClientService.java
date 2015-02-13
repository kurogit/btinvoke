package de.hskl.ps.bluetoothinvokeexample.services;

import java.io.IOException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.bluetooth.BTClientConnection;
import de.hskl.ps.bluetoothinvokeexample.bluetooth.ConnectionStatus;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeErrorValues;
import de.hskl.ps.bluetoothinvokeexample.helper.RemoteInvocationRequest;
import de.hskl.ps.bluetoothinvokeexample.helper.RemoteInvocationResult;
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

    /** Tag for Logging */
    private static final String TAG = BTInvokeClientService.class.getSimpleName();

    private LocalBroadcastManager broadcast_ = null;

    @Bean
    BTClientConnection connection_;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

        connection_.connect();
        readLoop();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        connection_.destroy();
    }

    @Background(id = "read_thread", serial = "read_thread")
    void readLoop() {
        while(true) {
            if(connection_.status() != ConnectionStatus.CONNECTED) {
                // FIXME: Bad!
                continue;
            }

            String recievedString = null;
            try {
                recievedString = connection_.readString();
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Exception while reading");
                // If reading fails we can't really send an answer
                continue;
            }

            RemoteInvocationRequest r = null;
            try {
                r = RemoteInvocationRequest.fromJSONString(recievedString);
            } catch(JSONException e) {
                BetterLog.e(TAG, e, "Exception while converting incoming JSON");
                // We cant get to the id and method anymore.
                continue;
            }

            Object result = null;
            try {
                result = BTInvokeMethodManager.getInstance().callMethod(r.methodName(), r.methodParams());
            } catch(MethodCallException e) {
                BetterLog.e(TAG, e, "Exception while calling method");
                result = BTInvokeErrorValues.ERROR_RESULT;
            }

            // Create result string
            JSONObject j = null;
            try {
                j = RemoteInvocationResult.toJSONObject(r.id(), result);
            } catch(JSONException e1) {
                BetterLog.e(TAG, "Exception while creating answer JSON");
                // If this fails we can't do anything
                continue;
            }

            try {
                connection_.writeString(j.toString());
            } catch(IOException e) {
                BetterLog.e(TAG, e, "Exception while writing");
                // If writing does not work we can't send the answer.
                continue;
            }

        }
    }
    
    private final BroadcastReceiver broadcastReciever_ = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
        }
    };
}
