package de.hskl.ps.bluetoothinvokeexample.btinvoke;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.MethodCallException;

public class BTInvokeMethodManager {
    
    private static BTInvokeMethodManager instance_ = null;
    
    public static BTInvokeMethodManager getInstance() {
        if(instance_ == null)
            instance_ = new BTInvokeMethodManager();
        
        return instance_;
    }
    
    private Map<Class<?>, Object> map = null;
    
    public void registerInterfaceAndImplementation(Class<?> interfaceClass, Object impl) {
        if(!interfaceClass.isInterface())
            return;
        
        map.put(interfaceClass, impl);
    }
    
    public Object callMethodFromJSON(String jsonString) throws MethodCallException {
        try {
            JSONObject j = new JSONObject(jsonString);
            
            String methodName = j.getString("method");
            JSONArray jsonParams = j.getJSONArray("params");
            
            Object[] params = new Object[jsonParams.length()];
            Class<?>[] paramTypes = new Class<?>[jsonParams.length()];
            
            for(int i = 0; i < jsonParams.length(); ++i) {
                JSONObject param = jsonParams.getJSONObject(i);
                
                paramTypes[i] = Class.forName(param.getString("type"));
                params[i] = param.get("value");
            }
            
            return callMethod(methodName, params);
            
        } catch(JSONException e) {
            throw new MethodCallException("Could not convert Json", e);
        } catch(ClassNotFoundException e) {
            throw new MethodCallException("Could not convert Json", e);
        }
    }
    
    public Object callMethod(String methodName, Object[] params) throws MethodCallException {
        Set<Class<?>> keys = map.keySet();
        
        Method method = null;
        Class<?> in = null;
        
        for(Class<?> i : keys) {
            Method[] methods = i.getDeclaredMethods();
            for(Method m : methods) {
                if(m.getName().equals(methodName)) {
                    method = m;
                    in = i;
                    break;
                }
            }
            if(in != null && method != null)
                break;
        }
                   
        Object impl = map.get(in);
        
        try {
            Object r = method.invoke(impl, params);
            return r;
        } catch(Exception e) {
            throw new MethodCallException("Unable to call the Method", e);
        } 
    }
    
    private BTInvokeMethodManager() {
        map = new HashMap<Class<?>, Object>();
    }
}
