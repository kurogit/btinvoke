package de.hskl.ps.bluetoothinvokeexample.fragments;

import java.util.Calendar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Fragment;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.hskl.ps.bluetoothinvokeexample.R;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvocationException;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.BTInvokeMethodManager;
import de.hskl.ps.bluetoothinvokeexample.btinvocation.MethodCallException;
import de.hskl.ps.bluetoothinvokeexample.example.CollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;
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
            
            
            Long l = (Long) BTInvokeMethodManager.getInstance().callMethodFromJSON(json);
            addLogEntry("Result is: " + l);
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
}