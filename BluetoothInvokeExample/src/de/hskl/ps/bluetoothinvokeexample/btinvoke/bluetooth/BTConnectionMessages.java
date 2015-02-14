package de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth;

import android.content.Intent;

/**
 * Constants for various connection status messages. Used in broadcasts.
 * <p>
 * Errors are defined in this class. Messages for direct status are the
 * {@link ConnectionStatus#ordinal()} values of the {@link ConnectionStatus} enum.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public final class BTConnectionMessages {

    /**
     * Action for a connection status message.
     * <p>
     * An intent with this action will always contain the extra
     * {@link BTConnectionMessages#EXTRA_TYPE}.
     */
    public static final String CONNECTION_STATUS_MESSAGE = "CONNECTION_STATUS_MESSAGE";

    /** Extra for the status message type */
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    /** Extra for the device name on connection */
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";

    private BTConnectionMessages() {
    }

    /** Errors */
    public final static class Errors {
        /** Bluetooth is not connected */
        public static final int NOT_CONNECTED = -1;
        /** Bluetooth is disabled */
        public static final int BLUETOOTH_DISABLED = -2;
        /** A connection is already being established */
        public static final int ALREADY_CONNECTING = -3;
        /** A connection is already present */
        public static final int ALREADY_CONNECTED = -4;
        /** The connecting failed */
        public static final int CONNECTING_FAILED = -5;
        /** There are no bonded devices */
        public static final int NO_BONDED_DEVICES = -6;

        private Errors() {
        }
    }

    /**
     * Turns an intent with the {@link BTConnectionMessages#CONNECTION_STATUS_MESSAGE} action to a
     * String containing a readable representation of the status message.
     * 
     * @param i The intent.
     * @return A readable representation of the status message.
     */
    public static final String turnIntentToHumanReadableString(Intent i) {
        // Only handle CONNECTION_STATUS_MESSAGE intents.
        if(!i.getAction().equalsIgnoreCase(CONNECTION_STATUS_MESSAGE))
            return "";

        int type = i.getIntExtra(EXTRA_TYPE, -255);

        String m = "Bluetooth: ";
        // Switch does not work here
        if(type == ConnectionStatus.DISABLED.ordinal()) {
            m += "Bluetooth disabled";
        } else if(type == ConnectionStatus.NOT_CONNECTED.ordinal()) {
            m += "Not connected";
        } else if(type == ConnectionStatus.ACCEPTING.ordinal()) {
            m += "Accepting connection";
        } else if(type == ConnectionStatus.CONNECTING.ordinal()) {
            m += "Currently connecting";
        } else if(type == ConnectionStatus.CONNECTED.ordinal()) {
            String deviceName = i.getStringExtra(EXTRA_DEVICE);
            m += "Connection established to " + deviceName;
        }
        // It is an Error
        switch (type) {
        case Errors.NOT_CONNECTED: {
            m += "No connection present!";
            break;
        }
        case Errors.BLUETOOTH_DISABLED: {
            m += "Bluetooth is disabled!";
            break;
        }
        case Errors.ALREADY_CONNECTING: {
            m += "Already trying to connect!";
            break;
        }
        case Errors.ALREADY_CONNECTED: {
            m += "Already connected!";
            break;
        }
        case Errors.CONNECTING_FAILED: {
            m += "The connection failed!";
            break;
        }
        case Errors.NO_BONDED_DEVICES: {
            m += "There are no bonded Devices. This App requires an already bonded device!";
            break;
        }
        }
        return m;
    }
}
