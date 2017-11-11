package brb.ehkeypad;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class FragmentLog extends Fragment {

    public TextView logTextView;
    public TextView connectionTextView;
    private List<String> errorLog = new ArrayList<String>();

    public boolean ready = false;

    public FragmentLog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ll = inflater.inflate(R.layout.log_tab, container, false);
        ready = true;
        connectionTextView = ll.findViewById(R.id.connection_textview);
        logTextView = ll.findViewById(R.id.log_textview);
        return ll;
    }

    public void updateTextLog(String s) {

        if (ready) {
            logTextView.setText(s);
        }
    }
    public void appendTextLog(String s) {

        if (ready) {
            addToLog(s,30);
        }
    }
    public void updateTextConnection(String s){

        if(ready) {
            connectionTextView.setText(s);
        }

    }

    public void addToLog(String str,int max) {
        if (str.length() > 0) {
            errorLog.add(str) ;
        }
        // remove the first line if log is too large
        if (errorLog.size() >= max) {
            errorLog.remove(0);
        }
        updateLog();
    }
    private void updateLog() {
        String log = "";
        for (String str : errorLog) {
            log += str + "\n";
        }
        logTextView.setText(log);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ready = false;
    }
}
