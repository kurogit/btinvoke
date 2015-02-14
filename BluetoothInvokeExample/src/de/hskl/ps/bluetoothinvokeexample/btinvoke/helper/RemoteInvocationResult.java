package de.hskl.ps.bluetoothinvokeexample.btinvoke.helper;

import org.json.JSONException;
import org.json.JSONObject;

import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages.Result;

/**
 * Helper class representing a remote invocation result.
 * <p>
 * The class can convert to and from JSON and exists to allow a better access to the components of a
 * remote invocation result.
 * <p>
 * A remote invocation result has two components:<br>
 * <ul>
 * <li><b>ID</b>. The id of the Request. Used to match a received result to a request.
 * <li><b>result</b>. The result of the remote method or {@link Result#ERROR_RESULT} if an
 * Error happened which still allowed to send an answer.
 * </ul>
 * <p>
 * A remote invocation result has the following JSON form:<br>
 * {@code "id" : theId, "result" : theResult} }
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public final class RemoteInvocationResult {

    /** JSON key of the ID */
    private static final String JSON_KEY_ID = "id";
    /** JSON key of the result */
    private static final String JSON_KEY_RESULT = "result";

    private long id_ = -1;
    private Object result_ = null;

    /**
     * Convert the two components to a {@code JSONObject}.
     * 
     * @param id
     *            ID of the request.
     * @param result
     *            The result
     * @return A {@link JSONObject} containing the result in the correct form.
     * @throws JSONException
     *             If the conversion to JSON failed.
     */
    public static JSONObject toJSONObject(long id, Object result) throws JSONException {
        JSONObject j = new JSONObject();
        j.put(JSON_KEY_ID, id);
        j.put(JSON_KEY_RESULT, result);

        return j;
    }

    /**
     * Create a {@code RemoteInvocationResult} Object from a JSON String.
     * 
     * @param json
     *            The JSON String.
     * @return A {@link RemoteInvocationResult} Object converted from the JSON String.
     * @throws JSONException
     *             If the conversion from JSON failed.
     */
    public static RemoteInvocationResult fromJSONString(String json) throws JSONException {
        JSONObject j = new JSONObject(json);
        long id = j.getLong(JSON_KEY_ID);
        Object result = j.get(JSON_KEY_RESULT);

        return new RemoteInvocationResult(id, result);
    }

    /** Constructor taking the two components */
    public RemoteInvocationResult(long id, Object result) {
        id_ = id;
        result_ = result;
    }

    /** Return the ID */
    public long id() {
        return id_;
    }

    /** Return the result */
    public Object result() {
        return result_;
    }
}
