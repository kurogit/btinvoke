package de.hskl.ps.bluetoothinvokeexample.btinvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages.Result;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTInvocationException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationRequest;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationResult;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

/**
 * Allows calling a remote method as if it were local.
 * <p>
 * Implements {@link InvocationHandler} for use with
 * {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}.
 * <p>
 * <strong>Example:</strong><br>
 * If we have an Interface named {@code IExampleInterace} we can write the following code:<br>
 * 
 * <pre>
 * {@code
 * IExampleInterace e = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IExampleInterace.class}, new BTInvocationHandler(this);
 * }
 * </pre>
 * 
 * It is then possible to call any Method on that interface as if it were local.
 * <p>
 * <strong>Warning:</strong> This works by blocking the call until the
 * {@link BTInvokeMessages#REMOTE_INVOCATION_RESULT} arrives using a {@link BroadcastReceiver}. A
 * {@link BroadcastReceiver} is always called on the UI thread. If a Method on the created Proxy
 * object is also called from the UI thread, the broadcast containing the result can never be received. Therefore, methods
 * on an Proxy created with this {@code InvocationHandler} should never be called on the UI thread!
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public class BTInvocationHandler implements InvocationHandler {

    private static final String TAG = BTInvocationHandler.class.getSimpleName();
    
    /** Counter for IDs */
    private static long currentID = 0;
    
    /** ID of the request this InvocationHandler will send */
    private long id_ = -1;
    /** The result of the Method call */
    private Object result_ = null;
    
    /** A latch for blocking the Method call until the result arrives */
    private CountDownLatch latch_ = null;

    /** Reference to the local broadcast manager */
    private LocalBroadcastManager broadcast_ = null;

    /** BroadcastReciever listening for {@code BTInvokeMessages.REMOTE_INVOCATION_RESULT} */
    private BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(BTInvokeMessages.REMOTE_INVOCATION_RESULT)) {
                String recievedString = intent.getStringExtra(BTInvokeMessages.Extras.JSONSTRING);

                try {
                    RemoteInvocationResult r = RemoteInvocationResult.fromJSONString(recievedString);

                    result_ = r.result();
                    
                    // Release the block
                    latch_.countDown();

                } catch(JSONException e) {
                    BetterLog.e(TAG, e, "Converting result string from JSON failed");
                    result_ = Result.ERROR_RESULT;
                }
            }
        }
    };
    
    /**
     * Creates a {@code BTInvocationHandler} Object. The context is needed for sending broadcasts.
     */
    public BTInvocationHandler(Context c) {

        id_ = currentID++;
        
        latch_ = new CountDownLatch(1);

        broadcast_ = LocalBroadcastManager.getInstance(c);
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvokeMessages.REMOTE_INVOCATION_RESULT));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        // Turn method to json
        JSONObject j = RemoteInvocationRequest.toJsonObject(id_, method.getName(), params);

        BetterLog.d(TAG, "Created Json String: %s", j.toString());
        
        // send request to service
        Intent intent = new Intent(BTInvokeMessages.REMOTE_INVOCATION);
        intent.putExtra(BTInvokeMessages.Extras.JSONSTRING, j.toString());
        broadcast_.sendBroadcast(intent);

        // Wait for result. Timeout after 10 seconds.
        boolean latchResult = latch_.await(10, TimeUnit.SECONDS);
        
        // Prevent BroadcastReceiver from leaking
        broadcast_.unregisterReceiver(broadCastReciever_);

        // Check if Latch failed or timed out
        if(!latchResult)
            throw new BTInvocationException("Timeout on anwser");

        // Check if we received an Error
        if(result_.equals(Result.ERROR_RESULT)) {
            throw new BTInvocationException("Error on compute device. Check the logs on the compute device for more info.");
        }

        return result_;
    }

}
