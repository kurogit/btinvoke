package de.hskl.ps.bluetoothinvokeexample.bluetooth.copy;

import android.content.Intent;

public final class BTConnectionMessages {

    public static final String CONNECTION_STATUS_MESSAGE = "CONNECTION_STATUS_MESSAGE";

    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";

    private static final int DEFAULT_TYPE = -255;

    private BTConnectionMessages() {
    }

    public final static class Errors {
        public final static int NOT_CONNECTED = -1;
        public static final int BLUETOOTH_DISABLED = -2;
        public static final int ALREADY_CONNECTING = -3;
        public static final int ALREADY_CONNECTED = -4;
        public static final int CONNECTING_FAILED = -5;
        public static final int NO_BONDED_DEVICES = -6;

        private Errors() {
        }
    }

    public static final String turnIntentToHumanReadableString(Intent i) {
        // Only handle CONNECTION_STATUS_MESSAGE intents.
        if(!i.getAction().equalsIgnoreCase(CONNECTION_STATUS_MESSAGE))
            return "";

        int type = i.getIntExtra(EXTRA_TYPE, DEFAULT_TYPE);

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
