package de.hskl.ps.bluetoothinvokeexample;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;
import android.app.Activity;
import android.content.Intent;

@EActivity(R.layout.activity_start)
public class StartActivity extends Activity {
    
    private static final String TAG = StartActivity.class.getSimpleName();
    
    @Click(R.id.BUTTON_COMPUTE_DEVICE)
    void computeDeviceChosen() {
        BetterLog.i(TAG, "User chose compute device");
    }
    
    @Click(R.id.BUTTON_GUI_DEVICE)
    void guiDeviceChosen() {
        BetterLog.i(TAG, "User chose gui device");
        
        GUIActivity_.intent(this).start();
    }
}
