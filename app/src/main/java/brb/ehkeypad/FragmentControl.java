package brb.ehkeypad;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class FragmentControl extends Fragment {

    public TextView bpm_text;
    public ProgressBar progress;
    public SoundPool sp,sp2;
    public SoundPool[] array_sp = new SoundPool[8];

    public boolean ready = false;

    private int kick;
    private int[] array_kick = new int[8];
    private int piano_a;
    private int piano_b;
    private int piano_bb;
    private int piano_c;
    private int piano_d;
    private int piano_e;
    private int piano_f;
    private int piano_g;

    private byte switches;


    private long bpm = 30;
    private int barprog;
    private int currentSwitch = 0;


    Timer t1,t2;
    private long currentBpm;
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

        // load each soundpool with kick
        for (int i = 0; i < 8; i++){
            array_sp[i] = new SoundPool.Builder().setAudioAttributes(att).build();
            array_kick[i] = array_sp[i].load(MainActivity.ctx,R.raw.kick,1);
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
        progress = ll.findViewById(R.id.progressBar);
        progress.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
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

    }

    class PlayTask extends TimerTask {
        public void run() {
            //main bpm timer code

            System.out.println("Time's up!");
            //sp2.play(jab,1,1,0,0,1);
            if(getBit(switches,currentSwitch)){
                array_sp[currentSwitch].play(array_kick[currentSwitch],1,1,0,0,1);

            }
            currentSwitch=(++currentSwitch)%8;
            currentBpm = 60000/bpm;
            t1.schedule(new PlayTask(),currentBpm);
            barprog = 0;
        //    progress.setMax(currentBpm);
            progress.setProgress(0);
            if(pTask!=null)pTask.cancel();
            pTask = new ProgressTask();

            // set subtimer for progressBar update
            t2.scheduleAtFixedRate(pTask,0,currentBpm/30);
        }
    }
    class ProgressTask extends TimerTask {
        public void run() {
            progress.setProgress(barprog+=(currentBpm/30.0));
        }
    }
    public boolean getBit(byte b, int position)
    {
        return ((b >> position) & 1) == 1;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ready = false;
    }

}