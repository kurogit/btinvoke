package de.hskl.ps.bluetoothinvokeexample;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EActivity(R.layout.activity_start)
public class StartActivity extends Activity {
    
    private static final String TAG = StartActivity.class.getSimpleName();
    
    private BluetoothAdapter adapter_ = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        adapter_ = BluetoothAdapter.getDefaultAdapter();
        
        if(!adapter_.isEnabled()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }
    
    @Click(R.id.BUTTON_COMPUTE_DEVICE)
    void computeDeviceChosen() {
        BetterLog.i(TAG, "User chose compute device");
        
        if(!checkBluetoothEnabled())
            return;
        
        ComputeActivity_.intent(this).start();
    }
    
    @Click(R.id.BUTTON_GUI_DEVICE)
    void guiDeviceChosen() {
        BetterLog.i(TAG, "User chose gui device");
        
        if(!checkBluetoothEnabled())
            return;
        
        GUIActivity_.intent(this).start();
    }
    
    private boolean checkBluetoothEnabled() {
        if(!adapter_.isEnabled()) {
            Toast.makeText(this, "Enable bluetooth first!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
