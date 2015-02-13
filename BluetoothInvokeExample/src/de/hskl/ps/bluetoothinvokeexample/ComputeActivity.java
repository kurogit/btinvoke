package de.hskl.ps.bluetoothinvokeexample;

import java.util.Calendar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.example.CollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.DoubleSleeper;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ISleeper;
import de.hskl.ps.bluetoothinvokeexample.services.BTInvocationServerService_;
import de.hskl.ps.bluetoothinvokeexample.services.BTInvokeClientService_;
import de.hskl.ps.bluetoothinvokeexample.util.BetterLog;

@EActivity(R.layout.activity_compute)
public class ComputeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BTInvokeClientService_.intent(this).start();

        BTInvokeMethodManager.getInstance().registerInterfaceAndImplementation(ICollatzLength.class, new CollatzLength());
        BTInvokeMethodManager.getInstance().registerInterfaceAndImplementation(ISleeper.class, new DoubleSleeper());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isFinishing()) {
            BTInvocationServerService_.intent(this).stop();
        }
    }

    @ViewById(R.id.LIST_VIEW)
    ListView listView;

    private ArrayAdapter<String> logEntryAdapter_ = null;

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.BT_STATUS_MESSAGE));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReciever_);
    }

    @AfterViews
    void afterViews() {
        logEntryAdapter_ = new ArrayAdapter<String>(this, R.layout.log_message);
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
