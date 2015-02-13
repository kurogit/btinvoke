package de.hskl.ps.bluetoothinvokeexample.btinvoke;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTInvocationException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationRequest;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

public class BTInvoke {
    
    private static final String TAG = BTInvoke.class.getSimpleName();
    
    private static long currentID = 0;
    
    /**
     * Execute the Method with the given name on a client device. This is an
     * asynchronous Operation. This Method will return immediately.
     * 
     * @param context An Android {@link Context}. Needed for sending a Message to the Service.
     * @param methodName Name of the Method to execute.
     * @param args Arguments of the Methods. In the current version only primitive Types + String are supported.
     * @return Id of the Operation. Can be used to find the correct Result.
     * @throws BTInvocationException 
     */
    public static long remoteExecute(Context context, String methodName, Object... args) throws BTInvocationException {
        long id = currentID++;
                
        JSONObject j = null;
        try {
            j = RemoteInvocationRequest.toJsonObject(id, methodName, args);
        } catch(JSONException e) {
            throw new BTInvocationException("Failed to convert to JSON", e);
        }
        
        String s = j.toString();
        BetterLog.d(TAG, "Created Json String: %s", s);
        
        LocalBroadcastManager b = LocalBroadcastManager.getInstance(context);
        
        // Send Message
        Intent intent = new Intent(BTInvokeMessages.REMOTE_INVOCATION);
        intent.putExtra(BTInvokeMessages.Extras.JSONSTRING, s);
        b.sendBroadcast(intent);
                
        return id;
    }

}
