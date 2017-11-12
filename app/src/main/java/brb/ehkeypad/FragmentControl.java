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
    public SoundPool sp;

    public boolean ready = false;

    private int jab;
    private int pianoa;

    private int bpm = 30;
    private int barprog;


    Timer t1,t2;
    private int currentBpm;
    private ProgressTask pTask;


    public FragmentControl() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        AudioAttributes att = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        sp = new SoundPool.Builder().setAudioAttributes(att).build();
        jab = sp.load(MainActivity.ctx,R.raw.jab,1);
        pianoa = sp.load(MainActivity.ctx,R.raw.piano_a,1);

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View ll = inflater.inflate(R.layout.control_tab, container, false);

        bpm_text = ll.findViewById(R.id.bpm_control_textview);
        progress = ll.findViewById(R.id.progressBar);
        progress.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
        ready = true;
        return ll;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            System.out.println("this fragment is now visible");
            long millis = 60000/bpm;
            System.out.println(millis);
            t1 = new Timer();
            t2 = new Timer();
            t1.schedule(new PlayTask(),millis);



        } else {
            System.out.println("this fragment is now invisible");
            if(t1 != null) t1.cancel();
            if(t2 != null) t2.cancel();
        }
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    //    if(ready) bpm_text.setText(bpm);
    }

    public void sendTriggers(byte b) {
        if(getBit(b,0)) sp.play(pianoa,1,1,0,0,1);
        if(getBit(b,1)) sp.play(pianoa,1,1,0,0,1);
        if(getBit(b,2)) sp.play(pianoa,1,1,0,0,1);
        if(getBit(b,3)) sp.play(pianoa,1,1,0,0,1);
        if(getBit(b,4)) sp.play(pianoa,1,1,0,0,1);
        if(getBit(b,5)) sp.play(pianoa,1,1,0,0,1);
        if(getBit(b,6)) sp.play(pianoa,1,1,0,0,1);
        if(getBit(b,7)) sp.play(pianoa,1,1,0,0,1);


    }

    class PlayTask extends TimerTask {
        public void run() {
            System.out.println("Time's up!");
            sp.play(jab,1,1,0,0,1);
            currentBpm = 60000/bpm;
            t1.schedule(new PlayTask(),currentBpm);
            barprog = 0;
            progress.setMax(currentBpm);
            progress.setProgress(0);
            if(pTask!=null)pTask.cancel();
            pTask = new ProgressTask();
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
        if (((b >> position) & 1) == 1) return true;
        else return false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ready = false;
    }

}