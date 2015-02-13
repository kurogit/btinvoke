package de.hskl.ps.bluetoothinvokeexample.btinvocation;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.helper.RemoteExectutionRequest;
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
            j = RemoteExectutionRequest.toJsonObject(id, methodName, args);
        } catch(JSONException e) {
            throw new BTInvocationException("Failed to convert to JSON", e);
        }
        
        String s = j.toString();
        BetterLog.d(TAG, "Created Json String: %s", s);
        
        
        // Send Message
        Intent intent = new Intent(BTInvocationMessages.REMOTE_EXECUTE);
        intent.putExtra(BTInvokeExtras.JSONSTRING, s);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        
        return id;
    }

}
