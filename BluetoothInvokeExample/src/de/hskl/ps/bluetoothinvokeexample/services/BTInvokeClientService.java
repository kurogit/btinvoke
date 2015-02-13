package de.hskl.ps.bluetoothinvokeexample.services;

import java.io.IOException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.bluetooth.BTConnection;
import de.hskl.ps.bluetoothinvokeexample.bluetooth.BTConnection.Status;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeErrorValues;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeJSONKeys;
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
    BTConnection connection_;

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

        connection_.connectAsClient();
        readLoop();
    }

    @Background(id = "read_thread", serial = "read_thread")
    void readLoop() {
        while(true) {
            if(connection_.status() != Status.CONNECTED) {
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
            
            long id = -1;
            String methodName = null;
            Object[] params = null;
            try {
                JSONObject r = new JSONObject(recievedString);
                id = r.getInt(BTInvokeJSONKeys.ID);
                methodName = r.getString(BTInvokeJSONKeys.METHOD_NAME);
                JSONArray a = r.getJSONArray(BTInvokeJSONKeys.PARAMETERS);
                params = new Object[a.length()];
                for(int i = 0; i < a.length(); ++i) {
                    params[i] = a.get(i);
                }
            } catch(JSONException e) {
                BetterLog.e(TAG, e, "Exception while converting incoming JSON");
                // We cant get to the id and method anymore.
                continue;
            }

            Object result = null;
            try {
                result = BTInvokeMethodManager.getInstance().callMethod(methodName, params);
            } catch(MethodCallException e) {
                BetterLog.e(TAG, e, "Exception while calling method");
                result = BTInvokeErrorValues.ERROR_RESULT;
            }

            // Create result string
            JSONObject j = new JSONObject();
            try {
                j.put(BTInvokeJSONKeys.ID, id);
                j.put(BTInvokeJSONKeys.RESULT, result);
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
}
