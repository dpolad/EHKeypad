package brb.ehkeypad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.media.SoundPool;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


// TODO: Strong code polish / comment /refactor
// TODO: add other sounds
// TODO: move selection boxes for switches in a more intuitive position
// TODO: add selection box for each button
// TODO: correct BPM value
// TODO: add progress_bar bar for BPM ticks (already in place, only need to code it)
// TODO: changes colors and shapes
// TODO: there is a bug with long sounds, do a soundpool for each line


public class FragmentControl extends Fragment {

    class Sound {
        public String name;
        public int rid;
        public int soundid_buttons;
        public int[] soundid_switches =  new int[4];

        public Sound(String name, int rid) {
            this.name = name;
            this.rid = rid;
            this.soundid_buttons = -1;
            for(int i = 0; i < SWITCHES_LINES; i++) {
                this.soundid_switches[i] = -1;
            }
        }
    }

    public static final int SWITCHES_LINES = 4;
    public static final int BUTTONS_NUMBER = 8;
    public static final int SWITCHES_NUMBER = 8;
    public static final int MAX_CONCURRENT_SOUNDS = 4;


    public TextView bpm_text;

    // bpm progress_bar bar
    public ProgressBar progress_bar;

    // soundpool for piano keys
    public SoundPool sp_buttons;
    public SoundPool[] sp_switches =  new SoundPool[SWITCHES_LINES];

    //button views
    private Button[] keys = new Button[BUTTONS_NUMBER];

    // used to decide when it is possible to access UI element
    public boolean ready = false;

 /*   // sound played by switches
    private String switchSound_0 = "kick";
    private String switchSound_1 = "kick";
    private String switchSound_2 = "blabla";
    private String switchSound_3 = "blabla";
*/

    private RadioButton[] radios = new RadioButton[4];
    private RadioButton radio_off;

    private int current_radio = 0;

    //hashmap for switch sounds

    private ArrayList<Sound> sounds_array = new ArrayList<>();

    // byte taken from MainActivity, each bit is the position of a switch
    private byte[] switches_position = new byte[4];

    // each surfaces represents a switch
    private SurfaceView[][] squares= new SurfaceView[SWITCHES_LINES][SWITCHES_NUMBER];

    private TextView[] switches_sound_text= new TextView[4];
    // bpm value
    private long bpm = 30;

    //progress_bar of the bpm bar
    private int barprog;

    //current and last switch
    private int currentSwitch = 0;
    private int lastSwitch = 7;

    // index in array of Sounds sounds_array
    private int button_sound_index[] = new int[BUTTONS_NUMBER];


    // index in array of Sounds sounds_array
    private int switches_sound_index[] = new int[SWITCHES_LINES];

    //timer 1 used for sounds, timer2 for bar progress_bar
    Timer timer_bpm, timer_progress_bar;


    //task used to update the progressbar
    private ProgressTask pTask;


    public FragmentControl() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // instantiate sound pools
        sp_buttons = new SoundPool.Builder().setMaxStreams(MAX_CONCURRENT_SOUNDS).build();
        for(int i = 0; i < SWITCHES_LINES; i++) {
            sp_switches[i] = new SoundPool.Builder().setMaxStreams(1).build();
        }

        // populate sound_array ArrayList
        try {
            fillMap();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

/*
        Integer[] kick =  new Integer[4];
        Integer[] jab =  new Integer[4];
        Integer[] asd =  new Integer[4];
        Integer[] lit_c_bass_hit_9 =  new Integer[4];
        Integer[] lit_c_synth_hit_5 =  new Integer[4];
        Integer[] lit_chicago =  new Integer[4];
        Integer[] mr2_c_bass_hit_06  =  new Integer[4];
        Integer[] mr2_c_bass_hit_08  =  new Integer[4];

        for(int i = 0; i <4; i++) {
            sp_switches[i]= new SoundPool.Builder().setMaxStreams(2).build();
            kick[i] = sp_switches[i].load(MainActivity.ctx, R.raw.kick, 1);
            jab[i] = sp_switches[i].load(MainActivity.ctx, R.raw.jab, 1);
            asd[i] = sp_switches[i].load(MainActivity.ctx, R.raw.asd, 1);
            lit_c_bass_hit_9[i] = sp_switches[i].load(MainActivity.ctx, R.raw.lit_c_bass_hit_9, 1);
            lit_c_synth_hit_5[i] = sp_switches[i].load(MainActivity.ctx, R.raw.lit_c_synth_hit_5, 1);
            lit_chicago[i] = sp_switches[i].load(MainActivity.ctx, R.raw.lit_chicago86_ride, 1);
            mr2_c_bass_hit_06[i] = sp_switches[i].load(MainActivity.ctx, R.raw.mr2_c_bass_hit_06, 1);
            mr2_c_bass_hit_08[i] = sp_switches[i].load(MainActivity.ctx, R.raw.mr2_c_bass_hit_08, 1);
        }

        soundMap.put("kick", kick);
        soundMap.put("jab", jab);
        soundMap.put("asd", asd);
        soundMap.put("lit_c_bass_hit_9", lit_c_bass_hit_9);
        soundMap.put("lit_c_synth_hit_5", lit_c_synth_hit_5);
        soundMap.put("lit_chicago", lit_chicago);
        soundMap.put("mr2_c_bass_hit_06", mr2_c_bass_hit_06);
        soundMap.put("mr2_c_bass_hit_08", mr2_c_bass_hit_08);
        arrayHash = new ArrayList<>(soundMap.keySet());
*/

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View layout = inflater.inflate(R.layout.control_tab, container, false);

        bpm_text = layout.findViewById(R.id.bpm_control_textview);


        radio_off = layout.findViewById(R.id.radio_off);
        radio_off.setChecked(true);

        radios[0] = layout.findViewById(R.id.radio_0);
        radios[1] = layout.findViewById(R.id.radio_1);
        radios[2] = layout.findViewById(R.id.radio_2);
        radios[3] = layout.findViewById(R.id.radio_3);


        radio_off.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radio_off.setChecked(true);
                radios[0].setChecked(false);
                radios[1].setChecked(false);
                radios[2].setChecked(false);
                radios[3].setChecked(false);
                current_radio = -1;
            }
        });

        radios[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radio_off.setChecked(false);
                radios[0].setChecked(true);
                radios[1].setChecked(false);
                radios[2].setChecked(false);
                radios[3].setChecked(false);
                current_radio = 0;
            }
        });

        radios[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radio_off.setChecked(false);
                radios[0].setChecked(false);
                radios[1].setChecked(true);
                radios[2].setChecked(false);
                radios[3].setChecked(false);
                current_radio = 1;
            }
        });

        radios[2].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radio_off.setChecked(false);
                radios[0].setChecked(false);
                radios[1].setChecked(false);
                radios[2].setChecked(true);
                radios[3].setChecked(false);
                current_radio = 2;
            }
        });

        radios[3].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radio_off.setChecked(false);
                radios[0].setChecked(false);
                radios[1].setChecked(false);
                radios[2].setChecked(false);
                radios[3].setChecked(true);
                current_radio = 3;
            }
        });


        progress_bar = layout.findViewById(R.id.progressBar8);
        progress_bar.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);



        //fill squares with blue color
        for(int i = 0; i < SWITCHES_NUMBER; i++){
            squares[0][i] = layout.findViewById(R.id.block0_0+i);
            squares[0][i].setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myBlue));
            squares[1][i] = layout.findViewById(R.id.block1_0+i);
            squares[1][i].setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myBlue));
            squares[2][i] = layout.findViewById(R.id.block2_0+i);
            squares[2][i].setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myBlue));
            squares[3][i] = layout.findViewById(R.id.block3_0+i);
            squares[3][i].setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myBlue));
        }


        // each spinner is for a line of switches, at the moment cannot make the code more elegant

        // layout find each line and set onlick to to throw a dialog ancd change
        // corresponding textview and sound associated to switch

        //layout.findViewById(MainActivity.ctx,R.id.);

        switches_sound_text[0] = layout.findViewById(R.id.switch_label_0);
        switches_sound_text[0].setText("None");
        switches_sound_text[1] = layout.findViewById(R.id.switch_label_1);
        switches_sound_text[1].setText("None");
        switches_sound_text[2] = layout.findViewById(R.id.switch_label_2);
        switches_sound_text[2].setText("None");
        switches_sound_text[3] = layout.findViewById(R.id.switch_label_3);
        switches_sound_text[3].setText("None");

        int[] switch_line_ids = {R.id.switch_line_0,R.id.switch_line_1,R.id.switch_line_2,R.id.switch_line_3};
        for(int i = 0; i < SWITCHES_LINES; i++) {
            LinearLayout l = layout.findViewById(switch_line_ids[i]);
            final int finalI = i;
            l.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform sound selection
                    Dialog dialog = onCreateDialogSingleChoice(false, finalI);
                    dialog.show();
                }
            });
        }



        for(int i = 0; i < BUTTONS_NUMBER; i++) {
            keys[i] = layout.findViewById(R.id.key0 + i);
            final int finalI = i;
            keys[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform sound selection
                    Dialog dialog = onCreateDialogSingleChoice(true,finalI);
                    dialog.show();
                }
            });
        }


        ready = true;
        return layout;
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
            timer_bpm = new Timer();
            timer_progress_bar = new Timer();
            timer_bpm.schedule(new PlayTask(),millis);



        } else {
            // if gets invisible -> cancel timers

            System.out.println("this fragment is now invisible");
            if(timer_bpm != null) timer_bpm.cancel();
            if(timer_progress_bar != null) timer_progress_bar.cancel();
        }
    }
    public void setBpm(long bpm) {
        this.bpm = (bpm+1)*3;
        if(ready) bpm_text.setText(String.valueOf(bpm+1));
    }
    public void sendButtonsTriggers(byte b) {
        for(int i = 0;i<8;i++){
            if(getBit(b,i)) sp_buttons.play(sounds_array.get(button_sound_index[i]).soundid_buttons,1,1,0,0,1);
        }


    }
    public void sendSwitches(byte pressed_switches) {
        if(current_radio >= 0) {
            switches_position[current_radio] = pressed_switches;
            //update square values
            for (int i = 0; i < 8; i++) {
                if (ready && i != lastSwitch) {
                    squareColor(squares[current_radio][i], getBit(switches_position[current_radio], i));
                }

            }
        }
    }
    class PlayTask extends TimerTask {
        public void run() {
            //main bpm timer code

            for(int i = 0; i < SWITCHES_LINES; i++) {
                squareColor(squares[i][lastSwitch], getBit(switches_position[i], lastSwitch));
                squareLightColor(squares[i][currentSwitch], getBit(switches_position[i], currentSwitch));
            }


            playSwitch();

            lastSwitch = currentSwitch;
            currentSwitch=(++currentSwitch)%8;
            long currentBpm = 60000/bpm;

            timer_bpm.schedule(new PlayTask(),currentBpm);

            progress_bar.setMax(100);
            if(lastSwitch == 0){
                barprog = 0;
                progress_bar.setProgress(0);

                if(pTask!=null)pTask.cancel();
                pTask = new ProgressTask();

                // set subtimer for progressBar update
                timer_progress_bar.scheduleAtFixedRate(pTask,0,(currentBpm*8)/100);
            }
        }
    }
    private void playSwitch() {

        for(int k = 0;k<SWITCHES_LINES;k++){
            int sound = sounds_array.get(switches_sound_index[k]).soundid_switches[k];
            if((sound != 0) && getBit(switches_position[k],currentSwitch)){
                sp_switches[k].play(sound,1,1,0,0,1);
            }
        }

    }
    class ProgressTask extends TimerTask {
        public void run() {
            progress_bar.setProgress(barprog++);
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
                else s.setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.colorPrimary));

            }
        });

    }
    private void keyColor(final Button k, final boolean b){
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(b) k.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R.color.colorPrimaryDark)));
                else k.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.ctx,R.color.colorPrimary)));

            }
        });

    }
    private void squareLightColor(final SurfaceView s, final boolean b){
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(b) s.setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.myLightRed));
                else s.setBackgroundColor(ContextCompat.getColor(MainActivity.ctx,R.color.colorPrimaryDark));

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

    public void fillMap() throws IllegalAccessException {
        Field[] fields=R.raw.class.getFields();
        sounds_array.add(new Sound("None",0));
        for(int count=0; count < fields.length; count++){
            sounds_array.add(new Sound(fields[count].getName(),fields[count].getInt(fields[count])));
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ready = false;
    }
    public Dialog onCreateDialogSingleChoice(final boolean is_button, final int switch_button_number) {

        //Initialize the Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Source of the data in the DIalog
        CharSequence[] array = new CharSequence[sounds_array.size()];
        for(int k = 0; k < sounds_array.size(); k++){
            array[k] = sounds_array.get(k).name;
            System.out.println(k+" "+array[k]);
        }
        int checked_item;
        if(is_button){
            checked_item = button_sound_index[switch_button_number];
        }
        else{
            checked_item = switches_sound_index[switch_button_number];

        }

        // Set the dialog title
        builder.setTitle("Select Sound")
        // Specify the list array, the items to be selected by default (null for none),
        // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(array, checked_item, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(is_button){
                            button_sound_index[switch_button_number] = which;
                            sounds_array.get(which).soundid_buttons=sp_buttons.load(MainActivity.ctx,sounds_array.get(which).rid,0);
                        }
                        else{
                            switches_sound_index[switch_button_number] = which;
                            System.out.println("which= "+which+" sw "+switch_button_number);
                            sounds_array.get(which).soundid_switches[switch_button_number]=
                                    sp_switches[switch_button_number].load(MainActivity.ctx,sounds_array.get(which).rid,0);
                            switches_sound_text[switch_button_number].setText(sounds_array.get(which).name);

                        }
                    }
                });
        return builder.create();
    }

}