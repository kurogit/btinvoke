package de.hskl.ps.bluetoothinvokeexample;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import de.hskl.ps.bluetoothinvokeexample.services.BtInvocationService;
import de.hskl.ps.bluetoothinvokeexample.services.BtInvocationService_;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

public class GUIActivity extends Activity {

    private ListFragment listFragment_ = null;
    private ArrayAdapter<String> logEntryAdapter_ = null;

    private BtInvocationService service_ = null;

    private final ServiceConnection serviceConnection_ = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            service_ = ((BtInvocationService.BluetoothInvocationBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service_ = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compute);
        if(savedInstanceState == null) {
            listFragment_ = new ListFragment();

            getFragmentManager().beginTransaction().add(R.id.container, listFragment_).commit();
        }

        logEntryAdapter_ = new ArrayAdapter<String>(this, R.layout.log_message);
        listFragment_.setListAdapter(logEntryAdapter_);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // Remember to use the generated version here
        Intent intent = BtInvocationService_.intent(this).get();
        
        BetterLog.i("wad", "Trying to bind service");
        bindService(intent, serviceConnection_, Context.BIND_AUTO_CREATE);
        BetterLog.i("wad", "After bindService");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        unbindService(serviceConnection_);
    }
}
