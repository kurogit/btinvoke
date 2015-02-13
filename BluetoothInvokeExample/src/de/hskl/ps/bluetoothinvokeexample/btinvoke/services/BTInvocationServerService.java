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
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTServerConnection;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTConnectionException;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EService
public class BTInvocationServerService extends Service {

    private final String TAG = "BTIServerService";

    @Bean
    BTServerConnection connection_;

    private LocalBroadcastManager broadcast_ = null;
    
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

    @Background(id = "send_and_wait_thread", serial = "send_and_wait_thread")
    void sendStringAndWaitForAnswer(String s) {        
        try {
            sendStatusMessage(BTInvokeMessages.Status.NEW_INVOCATION_REQUEST);
            
            connection_.writeString(s);

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
    
    private void sendStatusMessage(final int type) {
        Intent i = new Intent(BTInvokeMessages.ACTION_STATUS_MESSAGE);
        i.putExtra(BTInvokeMessages.Extras.STATUS_TYPE, type);
        broadcast_.sendBroadcast(i);
    }
    
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
