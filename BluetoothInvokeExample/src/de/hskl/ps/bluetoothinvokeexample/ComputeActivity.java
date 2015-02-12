package de.hskl.ps.bluetoothinvokeexample;

import org.androidannotations.annotations.EActivity;

import de.hskl.ps.bluetoothinvokeexample.services.BTInvocationServerService_;
import de.hskl.ps.bluetoothinvokeexample.services.BTInvokeClientService_;
import android.app.Activity;
import android.os.Bundle;

@EActivity(R.layout.activity_compute)
public class ComputeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        BTInvokeClientService_.intent(this).start();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if(isFinishing()) {
            BTInvocationServerService_.intent(this).stop();
        }
    }
}
