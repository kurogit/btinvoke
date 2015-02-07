package de.hskl.kuro.bluetoothchataa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity {

    @ViewById(R.id.EDIT_TEXT)
    protected EditText editText_;

    @ViewById(R.id.TEXT)
    protected TextView text_;

    private BluetoothAdapter adapter_ = null;

    private static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_ENABLE_BT = 1;

    private static final String SERVICE_NAME = "Bluetoothtest";
    private static final UUID UUID_SECURE = UUID.fromString("fce8c61f-44e1-45d8-ba9a-093312caf56c");

    private InputStream readStream_ = null;
    private OutputStream writeStream_ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter_ = BluetoothAdapter.getDefaultAdapter();

        if(adapter_ == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Enable Bluetooth
        if(!adapter_.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth is enabled. Use it
            Toast.makeText(this, "Bluetoosh is enabled. Click Connect now!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case REQUEST_ENABLE_BT: {
            // When the request to enable Bluetooth returns
            if(resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetoosh is enabled. Click COnnect now!", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occurred
                // Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Bluetooth was not enabled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        }
    }
    
    @UiThread
    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
    @UiThread
    protected void addText(String text) {
        text_.append(text + "\n");
    }
    
    @Click(R.id.BUTTON_SEND)
    protected void sendMessage() {
        byte[] text = editText_.getText().toString().getBytes();
        try {
            writeStream_.write(text);
            Log.d(TAG, "Succesfully written " + text.length + " bytes");
        } catch(IOException e) {
            showToast("Exception while writing");
            Log.e(TAG, "Exception while writing", e);
        }
    }
    
    @Background(id = "accept_thread")
    protected void acceptBluetoothConnection() {
        Log.i(TAG, "acceptBluetoothConnection");
        BluetoothServerSocket bss = null;
        try {
            bss = adapter_.listenUsingRfcommWithServiceRecord(SERVICE_NAME, UUID_SECURE);
        } catch(IOException e) {
            Log.e(TAG, "Could not Create BluetoothServerSocket");
            showToast("Could not Create BluetoothServerSocket");
            // finish();
            return;
        }

        BluetoothSocket socket = null;
        try {
            Log.d(TAG, "Blocking call to accept");
            socket = bss.accept();
            Log.d(TAG, "Blocking call to accept done!");
        } catch(IOException e) {
            Log.e(TAG, "Accepting failed");
            return;
        }

        if(socket != null) {
            Log.d(TAG, "Succesfully established connection to " + socket.getRemoteDevice().getName());
            try {
                readStream_ = socket.getInputStream();
            } catch(IOException e) {
                showToast("Unable to open Inputstream");
                Log.e(TAG, "", e);
            }
            try {
                writeStream_ = socket.getOutputStream();
            } catch(IOException e) {
                showToast("Unable to open Outputstream");
                Log.e(TAG, "", e);
            }
            try {
                bss.close();
            } catch(IOException e) {
                Log.e(TAG, "Unable to close BluetoothServerSocket");
            }
            
            readLoop();
        }
    }

    @Background(id = "connect_thread")
    protected void connectBluetooth() {
        Log.i(TAG, "connectBluetooth");

        adapter_.cancelDiscovery();

        BluetoothSocket socket = null;

        Set<BluetoothDevice> devices = adapter_.getBondedDevices();
        if(devices.isEmpty()) {
            Log.d(TAG, "No bonded devices!");
        }

        for(BluetoothDevice device : devices) {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID_SECURE);
            } catch(IOException e) {
                Log.e(TAG, "Failed to create socket!", e);
                finish();
            }

            try {
                Log.d(TAG, "Trying to connect to " + device.getName());
                socket.connect();
            } catch(IOException e) {
                Log.e(TAG, "Unable to connect to " + device.getName());
                showToast("Unable to connect to " + device.getName());
                try {
                    socket.close();
                } catch(IOException e1) {
                    Log.e(TAG, "Unable to close socket");
                }
            }
            if(socket.isConnected())
                break;
        }
        if(socket.isConnected()) {
            Log.i(TAG, "Socket succesfully connected to " + socket.getRemoteDevice().getName());
            showToast("Connected to " + socket.getRemoteDevice().getName());
            try {
                readStream_ = socket.getInputStream();
            } catch(IOException e) {
                showToast("Unable to open Inputstream");
                Log.e(TAG, "", e);
            }
            try {
                writeStream_ = socket.getOutputStream();
            } catch(IOException e) {
                showToast("Unable to open Outputstream");
                Log.e(TAG, "", e);
            }
            
            readLoop();
        }
    }

    @Background(id="read_thread")
    protected void readLoop() {
        byte[] buffer = new byte[1024];
        int bytes;
        
        while(true) {
            try {
                bytes = readStream_.read(buffer);
                Log.d(TAG, "Succesfully read " + bytes + " bytes");
                String msg = new String(buffer.clone(), 0, bytes);
                addText(msg);
            } catch(IOException e) {
                showToast("Exception while reading");
                Log.e(TAG, "Exception while reading", e);
            }
        }
    }
    
    @OptionsItem(R.id.MENU_ITEM_CONNECT)
    protected void menuConnect() {
        connectBluetooth();
    }
    
    @OptionsItem(R.id.MENU_ITEM_ACCEPT)
    protected void menuAccept() {
        acceptBluetoothConnection();
    }
    
}