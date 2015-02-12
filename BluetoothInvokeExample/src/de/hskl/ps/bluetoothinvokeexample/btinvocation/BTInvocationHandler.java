package de.hskl.ps.bluetoothinvokeexample.btinvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BTInvocationHandler implements InvocationHandler {
    
    private static final String TAG = BTInvocationHandler.class.getSimpleName();
    
    private static long currentID = 0;
    
    private BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            
        }
    };
    
    public BTInvocationHandler(Context context) {
       
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
        // wait for result
        // turn result from json to ?
        // return
        
        
        return null;
    }

}
