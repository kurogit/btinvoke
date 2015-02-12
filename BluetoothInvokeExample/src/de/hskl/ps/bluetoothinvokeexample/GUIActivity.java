package de.hskl.ps.bluetoothinvokeexample;

import org.androidannotations.annotations.EActivity;

import de.hskl.ps.bluetoothinvokeexample.services.BTInvocationServerService_;
import android.app.Activity;
import android.os.Bundle;

@EActivity(R.layout.activity_gui)
public class GUIActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        BTInvocationServerService_.intent(this).start();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if(isFinishing()) {
            BTInvocationServerService_.intent(this).stop();
        }
    }
}
