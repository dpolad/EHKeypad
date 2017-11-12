package brb.ehkeypad;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import static java.lang.Math.abs;


public class FragmentDebug extends Fragment {

    public Button btn1;
    public Button btn2;
    public Button btn3;
    public Button btn4;
    public Button btn5;
    public Button btn6;
    public Button btn7;
    public Button btn8;

    public Switch switch1;
    public Switch switch2;
    public Switch switch3;
    public Switch switch4;
    public Switch switch5;
    public Switch switch6;
    public Switch switch7;
    public Switch switch8;

    public SeekBar seekBar1;

    public TextView bpm;


    public boolean ready = false;

    public FragmentDebug() {
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
        View ll = inflater.inflate(R.layout.debug_tab, container, false);


        btn1 = ll.findViewById(R.id.btn1);
        btn2 = ll.findViewById(R.id.btn2);
        btn3 = ll.findViewById(R.id.btn3);
        btn4 = ll.findViewById(R.id.btn4);
        btn5 = ll.findViewById(R.id.btn5);
        btn6 = ll.findViewById(R.id.btn6);
        btn7 = ll.findViewById(R.id.btn7);
        btn8 = ll.findViewById(R.id.btn8);

        switch1 = ll.findViewById(R.id.switch1);
        switch2 = ll.findViewById(R.id.switch2);
        switch3 = ll.findViewById(R.id.switch3);
        switch4 = ll.findViewById(R.id.switch4);
        switch5 = ll.findViewById(R.id.switch5);
        switch6 = ll.findViewById(R.id.switch6);
        switch7 = ll.findViewById(R.id.switch7);
        switch8 = ll.findViewById(R.id.switch8);

        seekBar1 = ll.findViewById(R.id.seekBar1);
        seekBar1.setMax(255);

        bpm = ll.findViewById(R.id.bpm_textview);

        ready = true;

        return ll;
    }

    public void updateButtons(byte b){

        if(ready) {
            if((getBit(b,0))) btn1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
            if((getBit(b,1))) btn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
            if((getBit(b,2))) btn3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
            if((getBit(b,3))) btn4.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn4.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
            if((getBit(b,4))) btn5.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn5.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
            if((getBit(b,5))) btn6.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn6.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
            if((getBit(b,6))) btn7.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn7.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
            if((getBit(b,7))) btn8.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
            else btn8.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));
        }


    }
    public void updateSwitches(byte b){

        if(ready) {
            /* switch1.setChecked(getBit(b,0));
            switch2.setChecked(getBit(b,1));
            switch3.setChecked(getBit(b,2));
            switch4.setChecked(getBit(b,3));
            switch5.setChecked(getBit(b,4));
            switch6.setChecked(getBit(b,5));
            switch7.setChecked(getBit(b,6));
            switch8.setChecked(getBit(b,7));
            */
            if(switch1.isChecked() != getBit(b,0)) switch1.toggle();
            if(switch2.isChecked() != getBit(b,1)) switch2.toggle();
            if(switch3.isChecked() != getBit(b,2)) switch3.toggle();
            if(switch4.isChecked() != getBit(b,3)) switch4.toggle();
            if(switch5.isChecked() != getBit(b,4)) switch5.toggle();
            if(switch6.isChecked() != getBit(b,5)) switch6.toggle();
            if(switch7.isChecked() != getBit(b,6)) switch7.toggle();
            if(switch8.isChecked() != getBit(b,7)) switch8.toggle();
        }

    }


    public void updateBPMDebug(int value ){

        if(ready) {
       //     System.out.println(value);
            seekBar1.setProgress(value);
            bpm.setText(String.valueOf(value));
        }

    }

    public boolean getBit(byte b, int position)
    {
        if (((b >> position) & 1) == 1) return true;
        else return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ready = false;
    }

}