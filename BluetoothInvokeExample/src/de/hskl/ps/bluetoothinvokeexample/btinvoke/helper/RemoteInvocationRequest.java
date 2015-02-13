package de.hskl.ps.bluetoothinvokeexample.btinvoke.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class RemoteInvocationRequest {

    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_METHOD_NAME = "method";
    private static final String JSON_KEY_PARAMS = "params";

    private long id_ = -1;
    private String methodName_ = null;
    private Object[] methodParams_ = null;

    public static JSONObject toJsonObject(long id, String methodName, Object[] methodParameters) throws JSONException {
        JSONObject j = new JSONObject();
        j.put(JSON_KEY_ID, id);
        j.put(JSON_KEY_METHOD_NAME, methodName);
        j.put(JSON_KEY_PARAMS, new JSONArray(methodParameters));

        return j;
    }
    
    public static RemoteInvocationRequest fromJSONString(String json) throws JSONException {
        JSONObject j = new JSONObject(json);
        
        final long id = j.getLong(JSON_KEY_ID);
        final String m = j.getString(JSON_KEY_METHOD_NAME);
        final JSONArray a = j.getJSONArray(JSON_KEY_PARAMS);
        // Get params from JSONArray
        final Object[] params = new Object[a.length()];
        for(int i = 0; i < a.length(); ++i) {
            params[i] = a.get(i);
        }

        return new RemoteInvocationRequest(id, m, params);
    }
    
    public JSONObject toJSONObject() throws JSONException {
        return RemoteInvocationRequest.toJsonObject(id_, methodName_, methodParams_);
    }

    public long id() {
        return id_;
    }

    public String methodName() {
        return methodName_;
    }

    public Object[] methodParams() {
        return methodParams_;
    }

    private RemoteInvocationRequest(long id, String methodName, Object[] methodParameters) {
        id_ = id;
        methodName_ = methodName;
        methodParams_ = methodParameters;
    }

}
