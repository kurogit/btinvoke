package de.hskl.ps.bluetoothinvokeexample.services;

import java.io.IOException;

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
import de.hskl.ps.bluetoothinvokeexample.bluetooth.BTConnection;
import de.hskl.ps.bluetoothinvokeexample.bluetooth.BTServerConnection;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
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

        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.REMOTE_INVOCATION));

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
            connection_.writeString(s);

            String recievedString = connection_.readString();

            Intent intent = new Intent(BTInvocationMessages.REMOTE_INVOCATION_RESULT);
            intent.putExtra(BTInvokeExtras.JSONSTRING, recievedString);
            broadcast_.sendBroadcast(intent);

        } catch(IOException e) {
            BetterLog.e(TAG, e, "Writing or reading from socket failed");
        }
    }

    private final BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BTInvocationMessages.REMOTE_INVOCATION)) {
                String jsonString = intent.getExtras().getString(BTInvokeExtras.JSONSTRING);
                
                if(!connection_.isConnected()) {
                    BetterLog.i(TAG, "No connection!");
                    return;
                }
                
                sendStringAndWaitForAnswer(jsonString);
            }
        }
    };
}
