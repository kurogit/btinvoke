package de.hskl.ps.bluetoothinvokeexample.fragments;

import java.util.Calendar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.hskl.ps.bluetoothinvokeexample.R;
import de.hskl.ps.bluetoothinvokeexample.example.CollatzLength;
import de.hskl.ps.bluetoothinvokeexample.example.ICollatzLength;

@EFragment(R.layout.fragment_log)
public class BTExampleFragment extends Fragment {

    @ViewById(R.id.LIST_VIEW)
    ListView listView;

    private ArrayAdapter<String> logEntryAdapter_ = null;

    private ICollatzLength localCalculation_ = null;
    private ICollatzLength remoteCalculation_ = null;

    public BTExampleFragment() {
        localCalculation_ = new CollatzLength();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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