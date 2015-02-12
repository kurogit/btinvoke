package de.hskl.ps.bluetoothinvokeexample.fragments;

import java.util.Calendar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.hskl.ps.bluetoothinvokeexample.R;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.example.CollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.DoubleSleeper;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ISleeper;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EFragment(R.layout.fragment_compute)
public class ComputeFragment extends Fragment {
    
    @ViewById(R.id.LIST_VIEW)
    ListView listView;
    
    private ArrayAdapter<String> logEntryAdapter_ = null;
    
    private ICollatzLength localCalculation_ = null;
    
    public ComputeFragment() {
        localCalculation_ = new CollatzLength();
        
        BTInvokeMethodManager.getInstance().registerInterfaceAndImplementation(ICollatzLength.class, localCalculation_);
        BTInvokeMethodManager.getInstance().registerInterfaceAndImplementation(ISleeper.class, new DoubleSleeper());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.BT_STATUS_MESSAGE));
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadCastReciever_);
    }
    
    @AfterViews
    void afterViews() {
        logEntryAdapter_ = new ArrayAdapter<String>(getActivity(), R.layout.log_message);
        listView.setAdapter(logEntryAdapter_);
    }
    
    @UiThread
    void addLogEntry(String log) {
        String timestamp = DateFormat.format("H:m:s", Calendar.getInstance()).toString();

        String msg = String.format("%s: %s", timestamp, log);

        logEntryAdapter_.add(msg);
        logEntryAdapter_.notifyDataSetChanged();
    }
    
    @Click(R.id.BUTTON_CONNECT)
    void onConnectClicked() {
                
        try {
            String json = "{\"id\":0,\"method\":\"lengthOfHailstoneSequence\",\"params\":[{\"type\":\"java.lang.Integer\",\"value\":1000}]}";
            
            
            Object r = BTInvokeMethodManager.getInstance().callMethodFromJSON(json);
            addLogEntry("Result is: " + r);
        } catch(MethodCallException e) {
            BetterLog.e("bla", e, "Could not call method!");
        }
    }
    
    @Background
    @Click(R.id.BUTTON_LOCAL)
    void onLocalExecute() {
        final int exampleValue = 1000000000;

        addLogEntry("Executing localy");
        addLogEntry("Calculating Lengths of HailstoneSequences with startValue: " + exampleValue);
        long length = localCalculation_.lengthOfHailstoneSequence(exampleValue);
        addLogEntry("Calculating length done: " + length);
    }
    
    private final BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(BTInvocationMessages.BT_STATUS_MESSAGE)) {
                String msg = intent.getStringExtra(BTInvokeExtras.BT_STATUS_MESSAGE);
                addLogEntry(msg);
            }

        }
    };
}