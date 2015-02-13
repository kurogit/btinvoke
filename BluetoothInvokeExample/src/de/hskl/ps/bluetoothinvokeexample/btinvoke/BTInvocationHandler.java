package de.hskl.ps.bluetoothinvokeexample.btinvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTInvocationException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationRequest;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationResult;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

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
            if(intent.getAction().equalsIgnoreCase(BTInvokeMessages.REMOTE_INVOCATION_RESULT)) {
                String recievedString = intent.getStringExtra(BTInvokeMessages.Extras.JSONSTRING);

                try {
                    RemoteInvocationResult r = RemoteInvocationResult.fromJSONString(recievedString);
                    
                    result_ = r.result();
                    
                    latch_.countDown();

                } catch(JSONException e) {
                    BetterLog.e(TAG, e, "Converting result string from JSON failed");
                    result_ = BTInvokeError.ERROR_RESULT;
                }
            }
        }
    };

    public BTInvocationHandler() {
        id_ = currentID++;
        
        latch_ = new CountDownLatch(1);

        broadcast_ = LocalBroadcastManager.getInstance(context_);
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvokeMessages.REMOTE_INVOCATION_RESULT));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        // Turn method to json
        JSONObject j = RemoteInvocationRequest.toJsonObject(id_, method.getName(), params);

        BetterLog.d(TAG, "Created Json String: %s", j.toString());
        // send to service
        Intent intent = new Intent(BTInvokeMessages.REMOTE_INVOCATION);
        intent.putExtra(BTInvokeMessages.Extras.JSONSTRING, j.toString());
        broadcast_.sendBroadcast(intent);

        // wait for result
        boolean latchResult = latch_.await(10, TimeUnit.SECONDS);

        broadcast_.unregisterReceiver(broadCastReciever_);
        
        // Check if Latch failed or timed out
        if(!latchResult)
            throw new BTInvocationException("Timeout on anwser");
        
        // Check if we received an Error
        if(result_.equals(BTInvokeError.ERROR_RESULT)) {
            throw new BTInvocationException("Error on compute device. Check the logs on the compute device for more info.");
        }

        return result_;
    }

}
