package de.hskl.ps.bluetoothinvokeexample.btinvoke.helper;

import org.json.JSONException;
import org.json.JSONObject;

public final class RemoteInvocationResult {
    
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_RESULT = "result";
    
    private long id_ = -1;
    private Object result_ = null;
    
    public static JSONObject toJSONObject(long id, Object result) throws JSONException {
        JSONObject j = new JSONObject();
        j.put(JSON_KEY_ID, id);
        j.put(JSON_KEY_RESULT, result);
        
        return j;
    }
    
    public static RemoteInvocationResult fromJSONString(String json) throws JSONException {
        JSONObject j = new JSONObject(json);
        long id = j.getLong(JSON_KEY_ID);
        Object result = j.get(JSON_KEY_RESULT);
        
        return new RemoteInvocationResult(id, result);
    }
    
    public RemoteInvocationResult(long id, Object result) {
        id_ = id;
        result_ = result;
    }
    
    public long id() {
        return id_;
    }
    
    public Object result() {
        return result_;
    }
}
