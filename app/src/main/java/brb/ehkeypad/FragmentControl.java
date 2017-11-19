package brb.ehkeypad;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class FragmentControl extends Fragment {

    public TextView bpm_text;

    // bpm progress bar
    public ProgressBar progress;

    // soundpool for piano keys
    public SoundPool sp;

    //soundpool for switches
    public SoundPool[] array_sp = new SoundPool[8];

    //button views
    private Button[] keys = new Button[8];

    // used to decide when it is possible to access UI element
    public boolean ready = false;

    // sound played by switches
    private String switchSound = "kick";

    // can be moved to local variables
    private int[] array_kick = new int[8];
    private int[] array_jab = new int[8];


    // notes for piano -> TODO: move to a better structure
    private int piano_a;
    private int piano_b;
    private int piano_bb;
    private int piano_c;
    private int piano_d;
    private int piano_e;
    private int piano_f;
    private int piano_g;

    //hashmap for switch sounds
    private HashMap<String, int[]> soundMap = new HashMap<String, int[]>();

    // byte taken from MainActivity, each bit is the position of a switch
    private byte switches;

    // each surfaces represents a switch
    private SurfaceView[] squares= new SurfaceView[8];

    // bpm value
    private long bpm = 30;

    //progress of the bpm bar
    private int barprog;

    //current and last switch
    private int currentSwitch = 0;
    private int lastSwitch = 7;



    //timer 1 used for sounds, timer2 for bar progress
    Timer t1,t2;

    //may be moved to local
    private long currentBpm;

    //unknown at the moment
    private ProgressTask pTask;


    public FragmentControl() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // instantiate sound pools: array of soundpool is for switches, while sp is for buttons
        AudioAttributes att = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

        sp = new SoundPool.Builder().setAudioAttributes(att).build();
        piano_a = sp.load(MainActivity.ctx,R.raw.piano_a,1);
        piano_b = sp.load(MainActivity.ctx,R.raw.piano_b,1);
        piano_bb = sp.load(MainActivity.ctx,R.raw.piano_bb,1);
        piano_c = sp.load(MainActivity.ctx,R.raw.piano_c,1);
        piano_d = sp.load(MainActivity.ctx,R.raw.piano_d,1);
        piano_e = sp.load(MainActivity.ctx,R.raw.piano_e,1);
        piano_f = sp.load(MainActivity.ctx,R.raw.piano_f,1);
        piano_g = sp.load(MainActivity.ctx,R.raw.piano_g,1);



        for (int i = 0; i < 8; i++){
            // load each soundpool with kick
            array_sp[i] = new SoundPool.Builder().setAudioAttributes(att).build();

            array_kick[i] = array_sp[i].load(MainActivity.ctx,R.raw.kick,1);
            array_jab[i] = array_sp[i].load(MainActivity.ctx,R.raw.jab,1);

            soundMap.put("kick",array_kick);
            soundMap.put("jab",array_jab);

        }



        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View ll = inflater.inflate(R.layout.control_tab, container, false);

        // set UI objects views
        bpm_text = ll.findViewById(R.id.bpm_control_textview);
        progress = ll.findViewById(R.id.progressBar8);
        for(int i = 0; i < 8; i++){
            squares[i] = ll.findViewById(R.id.block1+i);
            squares[i].setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myBlue));

            keys[i] = ll.findViewById(R.id.key0+i);
        }
        progress.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);

        Spinner spinner = ll.findViewById(R.id.key_spinner);
//      Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.ctx,R.array.piano_array, android.R.layout.simple_spinner_item);
//      Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//      Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                   // Toast.makeText(getContext(), item.toString(),Toast.LENGTH_SHORT).show();
                    switchSound=item.toString();
                    System.out.println(switchSound);
                }
                //Toast.makeText(getContext(), "Selected",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub

            }
        });

        ready = true;

        return ll;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // this code is triggered when the control page changes state from visible to invisible

        // if gets visible -> set timers for audio playback
        if (isVisibleToUser) {
            System.out.println("this fragment is now visible");
            long millis = 60000/bpm;
            System.out.println(millis);
            t1 = new Timer();
            t2 = new Timer();
            t1.schedule(new PlayTask(),millis);



        } else {
            // if gets invisible -> cancel timers

            System.out.println("this fragment is now invisible");
            if(t1 != null) t1.cancel();
            if(t2 != null) t2.cancel();
        }
    }

    public void setBpm(long bpm) {
        this.bpm = (bpm+1)*3;
        if(ready) bpm_text.setText(String.valueOf(bpm+1));
    }

    public void sendTriggers(byte b) {
        if(getBit(b,0)) sp.play(piano_a,1,1,0,0,1);
        if(getBit(b,1)) sp.play(piano_bb,1,1,0,0,1);
        if(getBit(b,2)) sp.play(piano_b,1,1,0,0,1);
        if(getBit(b,3)) sp.play(piano_c,1,1,0,0,1);
        if(getBit(b,4)) sp.play(piano_d,1,1,0,0,1);
        if(getBit(b,5)) sp.play(piano_e,1,1,0,0,1);
        if(getBit(b,6)) sp.play(piano_f,1,1,0,0,1);
        if(getBit(b,7)) sp.play(piano_g,1,1,0,0,1);


    }

    public void sendSwitches(byte pressed_switches) {
        switches = pressed_switches;
        //update square values
        for(int i = 0; i < 8; i++){
            if(ready && i!=lastSwitch){
                squareColor(squares[i],getBit(switches,i));
            }

        }
    }

    class PlayTask extends TimerTask {
        public void run() {
            //main bpm timer code

            squareColor(squares[lastSwitch],getBit(switches,lastSwitch));

            squareLightColor(squares[currentSwitch],getBit(switches,currentSwitch));

            if(getBit(switches,currentSwitch)){
                playSwitch(currentSwitch);
            }
            lastSwitch = currentSwitch;
            currentSwitch=(++currentSwitch)%8;
            currentBpm = 60000/bpm;

            t1.schedule(new PlayTask(),currentBpm);

            progress.setMax(100);
            if(lastSwitch == 0){
                barprog = 0;
                progress.setProgress(0);
                if(pTask!=null)pTask.cancel();
                pTask = new ProgressTask();

                // set subtimer for progressBar update
                t2.scheduleAtFixedRate(pTask,0,(currentBpm*8)/100);
            }
        }
    }

    private void playSwitch(int currentSwitch) {

        int[] sound = soundMap.get(switchSound);
        if(sound != null){
            array_sp[currentSwitch].play(sound[currentSwitch],1,1,0,0,1);
        }

    }

    class ProgressTask extends TimerTask {
        public void run() {
            progress.setProgress(barprog++);
        }
    }
    public boolean getBit(byte b, int position){


        return ((b >> position) & 1) == 1;
    }

    private void squareColor(final SurfaceView s, final boolean b){
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(b) s.setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myRed));
                else s.setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myBlue));

            }
        });

    }


    private void keyColor(final Button k, final boolean b){
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(b) k.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myBlue)));
                else k.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R. color.myRed)));

            }
        });

    }

    private void squareLightColor(final SurfaceView s, final boolean b){
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(b) s.setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myLightRed));
                else s.setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myLightBlue));

            }
        });
    }

    public void updateButtons(byte b){

        if(ready) {
            for(int i = 0; i < 8; i++){
                keyColor(keys[i],getBit(b,i));
            }
        }


    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ready = false;
    }

}