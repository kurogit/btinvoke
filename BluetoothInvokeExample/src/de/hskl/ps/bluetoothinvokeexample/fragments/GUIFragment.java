package de.hskl.ps.bluetoothinvokeexample.fragments;

import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Locale;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.Post;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.hskl.ps.bluetoothinvokeexample.R;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvocationException;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvocationHandler;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvoke;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.example.CollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EFragment(R.layout.fragment_log)
public class GUIFragment extends Fragment {

    @ViewById(R.id.LIST_VIEW)
    ListView listView;

    private ArrayAdapter<String> logEntryAdapter_ = null;

    private ICollatzLength localCalculation_ = null;

    private final BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BTInvocationMessages.REMOTE_EXECUTE_RESULT)) {
                String recievedString = intent.getStringExtra(BTInvokeExtras.JSONSTRING);
                addLogEntry("Recived following string:\n" + recievedString);
            } else if(intent.getAction().equalsIgnoreCase(BTInvocationMessages.BT_STATUS_MESSAGE)) {
                String msg = intent.getStringExtra(BTInvokeExtras.BT_STATUS_MESSAGE);
                addLogEntry(msg);
            }

        }
    };

    public GUIFragment() {
        localCalculation_ = new CollatzLength();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.BT_STATUS_MESSAGE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.REMOTE_EXECUTE_RESULT));
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

    @Click(R.id.BUTTON_REMOTE)
    void onRemoteExecute() {
        //final String exampleMethod = "lengthOfHailstoneSequence";
        //final long exampleArg = 1000000;
        
/*        final String exampleMethod = "sleepForSecondsAndReturn";
        final int exampleArg1 = 3;
        final double exampleArg2 = 4.20;
        
        final String msg = String.format("Sending new Request: %s (%d, %f)", exampleMethod, exampleArg1, exampleArg2);
        addLogEntry(msg);
        
        try {
            BTInvoke.remoteExecute(getActivity(), exampleMethod, exampleArg1, exampleArg2);
        } catch(BTInvocationException e) {
            return;
        }*/
        
        test();
    }
    
    @Background
    void test() {
        ICollatzLength i = (ICollatzLength) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ICollatzLength.class}, new BTInvocationHandler());
        long r = i.lengthOfHailstoneSequence(1000000);
        addLogEntry("Result: " + r);
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
}