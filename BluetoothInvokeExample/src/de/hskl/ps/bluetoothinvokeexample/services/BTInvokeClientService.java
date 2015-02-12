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

            try {
                String recievedString = connection_.readString();

                JSONObject r = new JSONObject(recievedString);

                int id = r.getInt(BTInvokeJSONKeys.ID);
                String methodName = r.getString(BTInvokeJSONKeys.METHOD_NAME);
                JSONArray a = r.getJSONArray(BTInvokeJSONKeys.PARAMETERS);
                Object[] params = new Object[a.length()];
                for(int i = 0; i < a.length(); ++i) {
                    params[i] = a.get(i);
                }

                Object result = BTInvokeMethodManager.getInstance().callMethod(methodName, params);

                JSONObject j = new JSONObject();
                j.put(BTInvokeJSONKeys.ID, id);
                j.put(BTInvokeJSONKeys.RESULT, result);

                connection_.writeString(j.toString());

            } catch(IOException e) {
                BetterLog.e(TAG, e, "Exception while reading");
            } catch(MethodCallException e) {
                BetterLog.e(TAG, e, "Exception while calling the Method");
            } catch(JSONException e) {
                BetterLog.e(TAG, e, "Exception while converting JSON");
            }
        }
    }
}
