package de.hskl.ps.bluetoothinvokeexample.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeJSONKeys;

public class RemoteExectutionRequest {
    private static final String ID = "ID";
    private static final String METHOD_NAME = "METHOD_NAME";
    private static final String METHOD_PARAMS = "METHOD_PARAMS";

    private long id_ = -1;
    private String methodName_ = null;
    private Object[] methodParams_ = null;

    public static RemoteExectutionRequest fromBundle(Bundle b) {
        long id = b.getLong(ID);
        String methodName = b.getString(METHOD_NAME);
        Object[] methodParams = (Object[]) b.get(METHOD_PARAMS);

        return new RemoteExectutionRequest(id, methodName, methodParams);
    }
    
    public static JSONObject toJsonObject(long id, String methodName, Object[] methodParameters) throws JSONException {
        JSONObject j = new JSONObject();
        j.put(BTInvokeJSONKeys.ID, id);
        j.put(BTInvokeJSONKeys.METHOD_NAME, methodName);
        j.put(BTInvokeJSONKeys.PARAMETERS, new JSONArray(methodParameters));
        
        return j;
    }

    public RemoteExectutionRequest(long id, String methodName, Object[] methodParameters) {
        id_ = id;
        methodName_ = methodName;
        methodParams_ = methodParameters;
    }

    public void writeToBundle(Bundle b) {
        b.putLong(ID, id_);
        b.putString(METHOD_NAME, methodName_);
        b.putSerializable(METHOD_PARAMS, methodParams_);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject j = new JSONObject();
        j.put(BTInvokeJSONKeys.ID, id_);
        j.put(BTInvokeJSONKeys.METHOD_NAME, methodName_);
        j.put(BTInvokeJSONKeys.PARAMETERS, new JSONArray(methodParams_));
        
        return j;
    }

    public long getId() {
        return id_;
    }

    public String getMethodName() {
        return methodName_;
    }

    public Object[] getMethodParams() {
        return methodParams_;
    }

}
