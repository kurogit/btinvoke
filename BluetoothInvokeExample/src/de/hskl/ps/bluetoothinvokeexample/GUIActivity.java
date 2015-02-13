package de.hskl.ps.bluetoothinvokeexample;

import java.lang.reflect.Proxy;
import java.util.Calendar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
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
import de.hskl.ps.bluetoothinvokeexample.bluetooth.BTConnectionMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvocationException;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvocationHandler;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvoke;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvocationMessages;
import de.hskl.ps.bluetoothinvokeexample.constants.BTInvokeExtras;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;
import de.hskl.ps.bluetoothinvokeexample.services.BTInvocationServerService_;

@EActivity(R.layout.activity_gui)
public class GUIActivity extends Activity {

    @ViewById(R.id.LIST_VIEW)
    ListView listView;

    private ArrayAdapter<String> logEntryAdapter_ = null;

    private final BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BTInvocationMessages.REMOTE_INVOCATION_RESULT)) {
                String recievedString = intent.getStringExtra(BTInvokeExtras.JSONSTRING);
                addLogEntry("Recived following string:\n" + recievedString);
            } else if(intent.getAction().equalsIgnoreCase(BTConnectionMessages.CONNECTION_STATUS_MESSAGE)) {
                String msg = BTConnectionMessages.turnIntentToHumanReadableString(intent);
                addLogEntry(msg);
            }

        }
    };

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

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReciever_, new IntentFilter(BTConnectionMessages.CONNECTION_STATUS_MESSAGE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReciever_, new IntentFilter(BTInvocationMessages.REMOTE_INVOCATION_RESULT));
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

    @Click(R.id.BUTTON_COLLATZ)
    void runHailstoneExample() {
        final String exampleMethod = "lengthOfHailstoneSequence";
        final long exampleArg = 1000000;

        addLogEntry(String.format("Sending new Request: %s (%d)", exampleMethod, exampleArg));

        try {
            BTInvoke.remoteExecute(this, exampleMethod, exampleArg);
        } catch(BTInvocationException e) {
            addLogEntry("Invocation failed");
            return;
        }
    }

    @Click(R.id.BUTTON_SLEEP)
    void runSleepExample() {
        final String exampleMethod = "sleepForSecondsAndReturn";
        final int exampleArg1 = 3;
        final double exampleArg2 = 4.20;

        addLogEntry(String.format("Sending new Request: %s(%d, %f)", exampleMethod, exampleArg1, exampleArg2));
        try {
            BTInvoke.remoteExecute(this, exampleMethod, exampleArg1, exampleArg2);
        } catch(BTInvocationException e) {
            addLogEntry("Invocation failed");
            return;
        }
    }

    @Click(R.id.BUTTON_PROXY)
    @Background
    void proxyMethod() {
        ICollatzLength i = (ICollatzLength) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { ICollatzLength.class }, new BTInvocationHandler());
        long r = i.lengthOfHailstoneSequence(1000000);
        addLogEntry("Result: " + r);
    }

}
