package de.hskl.ps.bluetoothinvokeexample.btinvoke;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.MethodCallException;

/**
 * Handles the with BTInvoke callable methods.
 * <p>
 * Before a method can be invoked with BTInvoke a Interface containing the Method and a Class
 * implementing the Interface have to be registered.
 * <p>
 * <b>Limitations:</b><br>
 * <ul>
 * <li>A method that should be callable can not be overloaded in the interface or implementing
 * class.
 * <li>Will also not work correctly if multiple interfaces with methods with same names were
 * registered.
 * <li>Only Methods with Parameters and return types of primitives types + String are supported.
 * </ul>
 * <p>
 * This class is a Singleton.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public class BTInvokeMethodManager {

    /** Map containing the callable Interfaces and their implementation */
    private Map<Class<?>, Object> map = null;

    /** The Singleton instance */
    private static BTInvokeMethodManager instance_ = null;

    /**
     * Get the instance of this class.
     * 
     * @return The instance of this class.
     */
    public static BTInvokeMethodManager getInstance() {
        if(instance_ == null)
            instance_ = new BTInvokeMethodManager();

        return instance_;
    }

    /**
     * Register a interface and a instance of a Class implementing it.
     * 
     * @param interfaceClass
     *            The interface.
     * @param impl
     *            An instance of a Class implementing the interface.
     */
    public void registerInterfaceAndImplementation(Class<?> interfaceClass, Object impl) {
        if(!interfaceClass.isInterface() || !interfaceClass.isInstance(impl))
            return;

        map.put(interfaceClass, impl);
    }

    /**
     * Call a method in on of the registered Interfaces.
     * <p>
     * This Method will search the registered Interfaces for a method with the specified name and
     * call the implementation of it. It will call the first method it finds with the name
     * regardless if it has the correct Parameters or not.
     * 
     * @param methodName
     *            Name of the method to call.
     * @param params
     *            The Parameters for the method.
     * @return Result of the method.
     * @throws MethodCallException
     *             If the Method was not callable. Reasons this can happen include things such as:<br>
     *             <ul>
     *             <li> Method was not found. <li> Incorrect number of parameters. <li> Incorrect
     *             type of Parameters.
     *             </ul>
     */
    public Object callMethod(String methodName, Object[] params) throws MethodCallException {
        Set<Class<?>> keys = map.keySet();

        Method method = null;
        Class<?> in = null;

        // Search the method in the interfaces.
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

        // Get the implementation.
        Object impl = map.get(in);

        try {
            // Call the Method.
            Object r = method.invoke(impl, params);
            return r;
        } catch(Exception e) {
            throw new MethodCallException("Unable to call the Method", e);
        }
    }

    /** Constructor taking no Parameters */
    private BTInvokeMethodManager() {
        map = new HashMap<Class<?>, Object>();
    }
}
