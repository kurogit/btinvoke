package de.hskl.ps.bluetoothinvokeexample.btinvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeErrorValues;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeJSONKeys;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

@EBean
public class BTInvocationHandler implements InvocationHandler {

    private static final String TAG = BTInvocationHandler.class.getSimpleName();

    private static long currentID = 0;

    @RootContext
    Context context_;

    private long id_ = -1;
    private Object result_ = null;

    private CountDownLatch latch_ = null;

    private LocalBroadcastManager broadcast_ = null;

    private BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(BTInvocationMessages.REMOTE_EXECUTE_RESULT)) {
                String recievedString = intent.getStringExtra(BTInvokeExtras.JSONSTRING);

                try {
                    JSONObject j = new JSONObject(recievedString);
                    long id = j.getLong(BTInvokeJSONKeys.ID);
                    
                    if(id != id_) {
                        // This is not the result of our original request.
                        return;
                    }
                    
                    result_ = j.get(BTInvokeJSONKeys.RESULT);
                    latch_.countDown();

                } catch(JSONException e) {
                }
            }
        }
    };

    public BTInvocationHandler() {
        id_ = currentID++;
        
        latch_ = new CountDownLatch(1);

        broadcast_ = LocalBroadcastManager.getInstance(context_);
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.REMOTE_EXECUTE_RESULT));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Turn method to json
        JSONObject j = new JSONObject();
        j.put("id", currentID++);
        j.put("method", method.getName());
        j.put("params", new JSONArray(args));

        BetterLog.d(TAG, "Created Json String: %s", j.toString());
        // send to service
        Intent intent = new Intent(BTInvocationMessages.REMOTE_EXECUTE);
        intent.putExtra(BTInvokeExtras.JSONSTRING, j.toString());
        broadcast_.sendBroadcast(intent);

        // wait for result
        boolean latchResult = latch_.await(10, TimeUnit.SECONDS);

        broadcast_.unregisterReceiver(broadCastReciever_);
        
        // Check if Latch failed or timed out
        if(!latchResult)
            throw new BTInvocationException("Timeout on anwser");
        
        // Check if we received an Error
        if(result_.equals(BTInvokeErrorValues.ERROR_RESULT)) {
            throw new BTInvocationException("Error on compute device. Check the logs on the compute device for more info.");
        }

        return result_;
    }

}
