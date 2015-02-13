package de.hskl.ps.bluetoothinvokeexample.constants;

import android.content.Intent;


public final class BTInvokeMessages {
    public static final String REMOTE_INVOCATION = "REMOTE_INVOCATION";
    public static final String REMOTE_INVOCATION_RESULT = "REMOTE_INVOCATION_RESULT";
    
    public static final String ACTION_STATUS_MESSAGE = "ACTION_STATUS_MESSAGE";
    
    public static final class Status {
        public static final int NEW_INVOCATION_REQUEST = 1;
        public static final int RECIEVED_RESULT = 2;
        public static final int HANDLING_REQUEST = 3;
        public static final int METHOD_CALL_DONE = 4;
        
        private Status(){}
    }
    
    public static final class Errors {
        public static final int SENDING_STRING_FAILED = -1;
        public static final int READING_STRING_FAILED = -2;
        public static final int CALLING_METHOD_FAILED = -3;
        
        private Errors(){}
    }
    
    public static final class Extras {
        public static final String STATUS_TYPE = "TYPE";
        public static final String RESULT = "RESULT";
        public static final String JSONSTRING = "JSONSTRING";
        
        private Extras(){}
    }
    
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
