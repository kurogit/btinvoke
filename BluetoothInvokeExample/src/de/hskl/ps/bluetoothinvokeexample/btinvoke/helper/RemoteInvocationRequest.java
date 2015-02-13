package de.hskl.ps.bluetoothinvokeexample.btinvoke.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class representing a remote invocation request.
 * <p>
 * The class can convert to and from JSON and exists to allow a better access to the components of a
 * remote invocation request.
 * <p>
 * A remote invocation request has three components:<br>
 * <ul>
 * <li><b>ID</b>. The id of the Request. Used to match a received result to a request.
 * <li><b>Method name</b>. The name of the method that should be called on the client side.
 * <li><b>Parameters</b>. The parameters for the method.
 * </ul>
 * <p>
 * A remote invocation request has the following JSON form:<br>
 * {@code { "id" : theId, "method" : "theMethodName", "params" : [ArrayOfParameters] } }
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public final class RemoteInvocationRequest {

    /** JSON key of the ID */
    private static final String JSON_KEY_ID = "id";
    /** JSON key of the method name */
    private static final String JSON_KEY_METHOD_NAME = "method";
    /** JSON key of the method parameters */
    private static final String JSON_KEY_PARAMS = "params";

    private long id_ = -1;
    private String methodName_ = null;
    private Object[] methodParams_ = null;

    /**
     * Convert the three components to a {@code JSONObject}.
     * 
     * @param id
     *            ID of the request.
     * @param methodName
     *            Method name.
     * @param methodParameters
     *            Method parameters.
     * @return A {@link JSONObject} containing the request in the correct form.
     * @throws JSONException
     *             If the conversion to JSON failed.
     */
    public static JSONObject toJsonObject(long id, String methodName, Object[] methodParameters) throws JSONException {
        JSONObject j = new JSONObject();
        j.put(JSON_KEY_ID, id);
        j.put(JSON_KEY_METHOD_NAME, methodName);
        j.put(JSON_KEY_PARAMS, new JSONArray(methodParameters));

        return j;
    }

    /**
     * Create a {@code RemoteInvocationRequest} Object from a JSON String.
     * 
     * @param json
     *            The JSON String.
     * @return A {@link RemoteInvocationRequest} Object converted from the JSON String.
     * @throws JSONException
     *             If the conversion from JSON failed.
     */
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

    /**
     * Convert this instance to a {@code org.json.JSONObject}.
     * <p>
     * Calls {@link RemoteInvocationRequest#toJsonObject(long, String, Object[])}.
     * 
     * @return A {@link org.json.JSONObject} containing the three components.
     * @throws JSONException
     *             If the Conversion to JSON failed.
     */
    public JSONObject toJSONObject() throws JSONException {
        return RemoteInvocationRequest.toJsonObject(id_, methodName_, methodParams_);
    }

    /** Return the ID */
    public long id() {
        return id_;
    }

    /** Return the method name */
    public String methodName() {
        return methodName_;
    }

    /** Return the method parameters */
    public Object[] methodParams() {
        return methodParams_;
    }

    /** Constructor taking the three components */
    private RemoteInvocationRequest(long id, String methodName, Object[] methodParameters) {
        id_ = id;
        methodName_ = methodName;
        methodParams_ = methodParameters;
    }

}
