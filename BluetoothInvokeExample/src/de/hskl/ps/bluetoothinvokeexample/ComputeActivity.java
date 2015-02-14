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
import android.widget.TextView;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.bluetooth.BTConnectionMessages;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.services.BTInvokeClientService;
import de.hskl.ps.bluetoothinvokeexample.btinvoke.services.BTInvokeClientService_;
import de.hskl.ps.bluetoothinvokeexample.example.CollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.DoubleSleeper;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ISleeper;

/**
 * Example activity which runs on the compute or client device.
 * <p>
 * This activity demonstrates how to use BTInvoke on the client side. It shows a {@link TextView}
 * for status messages and a Button for connecting.<br>
 * <p>
 * Is using {@code @EActivity} from Android Annotations.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
@EActivity(R.layout.activity_compute)
public class ComputeActivity extends Activity {

    /** Reference to the used {@code ListView}. */
    @ViewById(R.id.LIST_VIEW)
    ListView listView;

    /** {@code ArrayAdpater} for the {@code ListView}. */
    private ArrayAdapter<String> logEntryAdapter_ = null;

    /** Reference to the local broadcast manager. */
    private LocalBroadcastManager broadcast_ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        broadcast_ = LocalBroadcastManager.getInstance(this);

        BTInvokeClientService_.intent(this).start();

        // Register callable interfaces
        BTInvokeMethodManager.getInstance().registerInterfaceAndImplementation(ICollatzLength.class, new CollatzLength());
        BTInvokeMethodManager.getInstance().registerInterfaceAndImplementation(ISleeper.class, new DoubleSleeper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isFinishing()) {
            BTInvokeClientService_.intent(this).stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTConnectionMessages.CONNECTION_STATUS_MESSAGE));
        broadcast_.registerReceiver(broadCastReciever_, new IntentFilter(BTInvokeMessages.ACTION_STATUS_MESSAGE));
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
     * @param log
     *            The message to add to the log. Will be prepended with the current time.
     */
    @UiThread
    void addLogEntry(String log) {
        String timestamp = DateFormat.format("H:m:s", Calendar.getInstance()).toString();

        String msg = String.format("%s: %s", timestamp, log);

        logEntryAdapter_.add(msg);
        logEntryAdapter_.notifyDataSetChanged();
    }

    /**
     * Try to connect to a server device.
     */
    @Click(R.id.BUTTON_CONNECT)
    void onConnectClicked() {
        // Send connect intent to Service
        Intent i = new Intent(this, BTInvokeClientService_.class);
        i.setAction(BTInvokeClientService.ACTION_CONNECT);
        startService(i);
    }

    /**
     * Used broadcast receiver.
     * <p>
     * 
     * Receives the following Message types:<br>
     * <ul>
     * <li> {@link BTConnectionMessages#CONNECTION_STATUS_MESSAGE}. For update the log with status
     * messages.
     * <li> {@link BTInvokeMessages#ACTION_STATUS_MESSAGE}. For update the log with status messages.
     * </ul>
     */
    private final BroadcastReceiver broadCastReciever_ = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(BTConnectionMessages.CONNECTION_STATUS_MESSAGE)) {
                String msg = BTConnectionMessages.turnIntentToHumanReadableString(intent);
                addLogEntry(msg);
            } else if(intent.getAction().equalsIgnoreCase(BTInvokeMessages.ACTION_STATUS_MESSAGE)) {
                String msg = BTInvokeMessages.turnIntentToHumanReadableString(intent);
                addLogEntry(msg);
            }

        }
    };
}
