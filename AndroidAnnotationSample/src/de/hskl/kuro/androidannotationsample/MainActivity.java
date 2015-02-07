package de.hskl.kuro.androidannotationsample;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    @ViewById(R.id.myInput)
    protected EditText myInput;

    @ViewById(R.id.myTextView)
    protected TextView textView;

    @Click(R.id.myInput)
    protected void myButton() {
         String name = myInput.getText().toString();
         textView.setText("Hello "+name);
    }
}