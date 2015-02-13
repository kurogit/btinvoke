package de.hskl.ps.bluetoothinvokeexample.bluetooth;

public final class BTConnectionMessages {

    public static final String CONNECTION_STATUS_MESSAGE = "CONNECTION_STATUS_MESSAGE";
    
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    
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
}
