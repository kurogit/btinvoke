package de.hskl.ps.bluetoothinvokeexample;

import java.lang.reflect.Proxy;
import java.util.Calendar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;

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
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvocationHandler;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvoke;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTConnectionMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.exceptions.BTInvocationException;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.helper.RemoteInvocationResult;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.services.BTInvocationServerService_;
import de.hskl.ps.bluetoothinvokeexample.example.CollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ISleeper;

/**
 * Example activity which runs on the GUI device or Server device.
 * <p>
 * This activity demonstrates how to use BTInvoke on the server side. It shows a Textview for Statusmessages and three Buttons for running examples.<br>
 * Examples are:
 * <ul>
 * <il> <b>Collatz</b> Length which runs {@link CollatzLength#lengthOfHailstoneSequence(long)}.
 * <il> <b>Sleep</b> which runs {@link ISleeper#sleepForSecondsAndReturn(int, double)}.
 * <il> <b>Proxy</b> which runs a created proxy instance of {@link ICollatzLength}.
 * </ul>
 * <p>
 * Is using {@code @EActivity} from Android Annotations.
 * @author Patrick Schwartz
 * @date 2014
 */
@EActivity(R.layout.activity_gui)
public class GUIActivity extends Activity {
    
    /** Reference to the used {@code ListView}. */
    @ViewById(R.id.LIST_VIEW)
    ListView listView;

    /** {@code ArrayAdpater} for the {@code ListView}.  */
    private ArrayAdapter<String> logEntryAdapter_ = null;
    
    /** Reference to the local broadcast manager. */
    private LocalBroadcastManager broadcast_ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the service
        BTInvocationServerService_.intent(this).start();
        
        broadcast_ = LocalBroadcastManager.getInstance(this);
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
        
        // Register broadcast reciever
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTConnectionMessages.CONNECTION_STATUS_MESSAGE));
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvokeMessages.ACTION_STATUS_MESSAGE));
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvokeMessages.REMOTE_INVOCATION_RESULT));
    }

    @Override
    public void onPause() {
        super.onPause();

        broadcast_.unregisterReceiver(broadCastReciever_);
    }
    
    @AfterViews
    void afterViews() {
        logEntryAdapter_ = new ArrayAdapter<String>(this, R.layout.log_message);
        listView.setAdapter(logEntryAdapter_);
    }
    
    /**
     * Add entry to the shown log.
     * <p>
     * Guaranteed to be called on the UI thread through Android Annotations.
     * 
     * @param log The message to add to the log. Will be prepended with the current time.
     */
    @UiThread
    void addLogEntry(String log) {
        String timestamp = DateFormat.format("H:m:s", Calendar.getInstance()).toString();

        String msg = String.format("%s: %s", timestamp, log);

        logEntryAdapter_.add(msg);
        logEntryAdapter_.notifyDataSetChanged();
    }
    
    /**
     * Run the first example.
     * <p>
     * Will request a run of {@link ICollatzLength#lengthOfHailstoneSequence(long)} on the remote device.
     * <p>
     * A Collatz sequence is also called a Hailstone sequence.
     */
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
    
    /**
     * Run the second example.
     * <p>
     * Will request a run of {@link ISleeper#sleepForSecondsAndReturn(int, double)} on the remote device.
     */
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
    
    /**
     * Run the third example.
     * <p>
     * Will create a Proxy implementation of {@link ICollatzLength} using {@link Proxy} and {@link BTInvocationHandler}.
     * Has to run in the Background since it is a blocking call.
     * @see {@link BTInvocationHandler}
     */
    @Click(R.id.BUTTON_PROXY)
    @Background
    void proxyMethod() {
        ICollatzLength i = (ICollatzLength) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { ICollatzLength.class }, new BTInvocationHandler());
        long r = i.lengthOfHailstoneSequence(1000000);
        addLogEntry("Result: " + r);
    }
    
    /**
     * Broadcast receiver.
     * <p>
     * 
     * Receives the following Message types:<br>
     * <ul>
     * <li> {@link BTInvokeMessages#REMOTE_INVOCATION_Result}. The Message containing the result of a request.
     * <li> {@link BTConnectionMessages#CONNECTION_STATUS_MESSAGE}. For update the log with status messages.
     * <li> {@link BTInvokeMessages#ACTION_STATUS_MESSAGE}. For update the log with status messages.
     * </ul>
     */
    private final BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BTInvokeMessages.REMOTE_INVOCATION_RESULT)) {
                String recievedString = intent.getStringExtra(BTInvokeMessages.Extras.JSONSTRING);
                
                RemoteInvocationResult r = null;
                try {
                    r = RemoteInvocationResult.fromJSONString(recievedString);
                } catch(JSONException e) {
                }
                
                addLogEntry("Recieved following result: " + r.result());
            } else if(intent.getAction().equalsIgnoreCase(BTConnectionMessages.CONNECTION_STATUS_MESSAGE)) {
                String msg = BTConnectionMessages.turnIntentToHumanReadableString(intent);
                addLogEntry(msg);
            } else if(intent.getAction().equalsIgnoreCase(BTInvokeMessages.ACTION_STATUS_MESSAGE)) {
                String msg = BTInvokeMessages.turnIntentToHumanReadableString(intent);
                addLogEntry(msg);
            }

        }
    };

}
