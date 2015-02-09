package de.hskl.ps.bluetoothinvokeexample.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import de.hskl.ps.bluetoothinvokeexample.R;

public class PlaceholderFragment extends ListFragment {
    
    private ArrayAdapter<String> logEntryAdapter_ = null;
    
    public PlaceholderFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        logEntryAdapter_ = new ArrayAdapter<String>(getActivity(), R.layout.log_message);
    }
    
}