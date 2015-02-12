package de.hskl.ps.bluetoothinvokeexample.btinvocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

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
        // Turn to JSON
        JSONObject j = new JSONObject();
        try {
            j.put("id", id);
            j.put("method", methodName);
            
            JSONArray params = new JSONArray();
            for(int i = 0; i < args.length; ++i) {
                JSONObject param = new JSONObject();
                param.put("value", args[i]);
                param.put("type", args[i].getClass().getName());
                params.put(param);
            }
            j.put("params", params);
            
        } catch(JSONException e) {
            throw new BTInvocationException("Failed to create JSONObject.");
        }
        String s = j.toString();
        BetterLog.d(TAG, "Created Json String: %s", s);
        
        // Send Message
        Intent intent = new Intent(BTInvocationMessages.REMOTE_EXECUTE);
        intent.putExtra(BTInvokeExtras.JSONSTRING, j.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        
        return id;
    }

}
