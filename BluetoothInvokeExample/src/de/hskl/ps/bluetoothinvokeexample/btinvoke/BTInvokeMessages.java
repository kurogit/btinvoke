package de.hskl.ps.bluetoothinvokeexample.btinvoke;

import android.content.Intent;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.services.BTInvokeClientService;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.services.BTInvokeServerService;

/**
 * Constants for various BTInvoke status messages. Used in broadcasts.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public final class BTInvokeMessages {
    /** 
     * Intent action for a remote invocation request.
     * <p>
     * Intent contains the Extra {@link BTInvokeMessages.Extras#JSONSTRING}. 
     */
    public static final String REMOTE_INVOCATION = "REMOTE_INVOCATION";
    /** 
     * Intent action for a remote invocation result.
     * <p>
     * Intent contains the Extra {@link BTInvokeMessages.Extras#JSONSTRING}. 
     */
    public static final String REMOTE_INVOCATION_RESULT = "REMOTE_INVOCATION_RESULT";
    
    /**
     * Intent action for a BTInvoke status message. 
     * <p>
     * Used by {@link BTInvokeServerService} and {@link BTInvokeClientService}.
     * Will always contain the Extra {@link BTInvokeMessages.Extras#STATUS_TYPE}.
     */
    public static final String ACTION_STATUS_MESSAGE = "ACTION_STATUS_MESSAGE";
    
    /** Status messages */
    public static final class Status {
        /** A new request was received on the server side. */
        public static final int NEW_INVOCATION_REQUEST = 1;
        /** A result was recieved from the client side. */
        public static final int RECIEVED_RESULT = 2;
        /** The client side is currently handling a request. */
        public static final int HANDLING_REQUEST = 3;
        /** The client side finished calling the method */
        public static final int METHOD_CALL_DONE = 4;
        
        private Status(){}
    }
    
    /** Error messages */
    public static final class Errors {
        /** Failed to send the String over Bluetooth */
        public static final int SENDING_STRING_FAILED = -1;
        /** Failed to read String over Bluetooth */
        public static final int READING_STRING_FAILED = -2;
        /** Failed to call the Method on the client side */
        public static final int CALLING_METHOD_FAILED = -3;
        
        private Errors(){}
    }
    
    /** Keys for Extras in the messages */
    public static final class Extras {
        /** The type of status message */
        public static final String STATUS_TYPE = "TYPE";
        /** The JSON String */
        public static final String JSONSTRING = "JSONSTRING";
        
        private Extras(){}
    }
    
    /** Special values for the result */
    public static final class Result {
        /** There was an error on the client side */
        public static final String ERROR_RESULT = "error";

        private Result(){}
    }
    
    /**
     * Turns an intent with the {@link BTInvokeMessages#ACTION_STATUS_MESSAGE} action to a
     * String containing a readable representation of the status message.
     * 
     * @param i The intent.
     * @return A readable representation of the status message.
     */
    public static final String turnIntentToHumanReadableString(Intent i) {
        if(!i.getAction().equalsIgnoreCase(ACTION_STATUS_MESSAGE)) {
            return "";
        }
        
        String m = "Invocation: ";
        
        final int type = i.getIntExtra(Extras.STATUS_TYPE, 0);
        switch(type) {
        case Status.NEW_INVOCATION_REQUEST: {
            m += "New invocation request recieved. Sending String ...";
            break;
        }
        case Status.RECIEVED_RESULT: {
            m += "Recieved result from remote device.";
            break;
        }
        case Status.HANDLING_REQUEST: {
            m += "Recieved request. Trying to call the Method.";
            break;
        }
        case Status.METHOD_CALL_DONE: {
            m += "Method call done. Sending back result";
            break;
        }
        case Errors.SENDING_STRING_FAILED: {
            m += "Sending to the connected device failed!";
            break;
        }
        case Errors.READING_STRING_FAILED: {
            m += "Reading from the remote device failed!";
            break;
        }
        case Errors.CALLING_METHOD_FAILED: {
            m += "Calling the requested Method failed!";
            break;
        }
        }
        
        return m;
    }

    private BTInvokeMessages(){}
}
