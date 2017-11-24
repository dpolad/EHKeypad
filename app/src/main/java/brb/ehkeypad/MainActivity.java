package brb.ehkeypad;
import android.content.Context;
import android.nfc.TagLostException;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import brb.ehkeypad.FragmentLog;

public class MainActivity extends AppCompatActivity {

    /* ST25DV constants */
    public static final byte MB_CTRL_DYN_EN = 0x01;
    public static final byte MB_CTRL_DYN_HOST_PUT_MSG = 0x02;
    public static final byte MB_CTRL_DYN_RF_PUT_MSG = 0x04;
    public static final byte MB_CTRL_DYN_HOST_MISS_MSG = 0x10;
    public static final byte MB_CTRL_DYN_RF_MISS_MSG = 0x20;
    public static final byte MB_CTRL_DYN_HOST_CURRENT_MSG = 0x40;
    public static final byte MB_CTRL_DYN_RF_CURRENT_MSG = (byte) 0x80;

    public static final String LOG_CAT_TAG = "NfcDemo";

    public static Context ctx;

    private NfcAdapter myNfcAdapter;
    private Tag nfcTag;
    private NfcV NFCV_Tag;

    private int myHandlerPeriod = 1;
    private Handler myHandler;

    private int k;

    private FragmentLog fl;
    private FragmentDebug fd;
    private FragmentControl fc;
    private byte previous_pressed_buttons = 0x00;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentLog(), "Log");
        adapter.addFragment(new FragmentDebug(), "Debug");
        adapter.addFragment(new FragmentControl(), "Control");
        viewPager.setAdapter(adapter);
        ctx = getApplicationContext();

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fl = (FragmentLog) adapter.getItem(0);
        fd = (FragmentDebug) adapter.getItem(1);
        fc = (FragmentControl) adapter.getItem(2);
        k = 0;

        /* Check if device has NFC */
        myNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (myNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!myNfcAdapter.isEnabled()) {
            finish();
            return;
        }

        /* Ok we have NFC */
        handleIntent(getIntent());


    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Log.i(LOG_CAT_TAG, "Detected new intent: " + action);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NFCV_Tag = NfcV.get(nfcTag);

            try {
                NFCV_Tag.connect();
         //       connectionTextView.setText("Connected!");
                fl.updateTextConnection("Connected!");
            } catch (IOException e) {
                e.printStackTrace();
                fl.updateTextConnection("Unable to Connect!");
                return;
            }


            /* Start periodic task */
            myHandler = new Handler();
            startRepeatingTask();
        }


    }

    private void update() {
     //   logTextView.setText("");

        byte[] response;    // Byte array to store responses
        byte[] cmd;         // Byte array used to send commands
        byte MB_CTRL_Dyn;   // Mailbox dynamic register
        byte msg_length;    // Mailbox message length


        cmd = new byte[] {
                (byte) 0x00,    //Flag
                (byte) 0xAD,    //Read configuration command
                (byte) 0x02,    //IC Mfg Code
                (byte) 0x0D};   //Pointer (0x0D = Mailbox dyn register)

        try {
            response = NFCV_Tag.transceive(cmd);
            if(response[0] == 0x00)
            {
                MB_CTRL_Dyn = response[1];
                fl.appendTextLog("OK!!!"+k++);
                fl.updateTextConnection("Connected!");
           /*     addText("MB_CTRL_Dyn:");
                addText("\tMB_EN: " + getBit(MB_CTRL_Dyn, MB_CTRL_DYN_EN));
                addText("\tHOST_PUT_MSG: " + getBit(MB_CTRL_Dyn, MB_CTRL_DYN_HOST_PUT_MSG));
                addText("\tRF_PUT_MSG: " + getBit(MB_CTRL_Dyn, MB_CTRL_DYN_RF_PUT_MSG));
                addText("\tHOST_MISS_MSG: " + getBit(MB_CTRL_Dyn, MB_CTRL_DYN_HOST_MISS_MSG));
                addText("\tRF_MISS_MSG: " + getBit(MB_CTRL_Dyn, MB_CTRL_DYN_RF_MISS_MSG));
                addText("\tHOST_CURRENT_MSG: " + getBit(MB_CTRL_Dyn, MB_CTRL_DYN_HOST_CURRENT_MSG));
                addText("\tRF_CURRENT_MSG: " + getBit(MB_CTRL_Dyn, MB_CTRL_DYN_RF_CURRENT_MSG));
*/
                if( getBit(MB_CTRL_Dyn, MB_CTRL_DYN_RF_MISS_MSG) || getBit(MB_CTRL_Dyn, MB_CTRL_DYN_HOST_PUT_MSG) ){
                    // Read message length
                    cmd = new byte[] {
                            (byte) 0x00,    //Flag
                            (byte) 0xAB,    //Read mailbox message length
                            (byte) 0x02};   //IC Mfg Code
                    response = NFCV_Tag.transceive(cmd);
                    if(response[0] == 0x00){
                        msg_length = response[1];

                        // Read message
                        cmd = new byte[] {
                                (byte) 0x00,    //Flag
                                (byte) 0xAC,    //Read mailbox message
                                (byte) 0x02,    //IC Mfg Code
                                (byte) 0x00,    //Read from first byte
                                msg_length};    //Read msg length bytes
                        response = NFCV_Tag.transceive(cmd);
                        fl.appendTextLog("Mailbox: " + bytesToHex(response));

                        /* If message correctly received */
                        if(response[0] == 0x00){
                            byte pressed_buttons = response[1];
                            fd.updateButtons(pressed_buttons);
                            fc.sendTriggers((byte)((pressed_buttons^previous_pressed_buttons)&pressed_buttons));
                            fc.updateButtons(pressed_buttons);
                            previous_pressed_buttons = pressed_buttons;
                            byte pressed_switches = response[2];
                            fd.updateSwitches(pressed_switches);
                            fc.sendSwitches(pressed_switches);
                            byte adc_msb = response[3];
                       //     byte adc_lsb = response[4];
                            int bpm_value = 0x000000FF&((int) adc_msb);
                            fd.updateBPMDebug(bpm_value);
                            fc.setBpm(bpm_value);



                        }

                    }else{
                        fl.appendTextLog("Error reading msg length");
                    }

                }
                else
                {
                    fl.appendTextLog("mailboxing error");
                }
            }else
            {
                fl.appendTextLog("Error in Mailbox dynamic reg: " + response[0]);
            }

        } catch (IOException e) {
            e.printStackTrace();
            fl.appendTextLog("IOEXCEPTION!!!");
        }


    }



    public static Boolean getBit(byte value, byte mask){
        return (value & mask) > 0;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    Runnable myStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                update();
            } finally {
                myHandler.postDelayed(myStatusChecker, myHandlerPeriod);
            }
        }
    };

    void startRepeatingTask() {
        myStatusChecker.run();
    }

    void stopRepeatingTask() {
        if(myHandler != null) myHandler.removeCallbacks(myStatusChecker);
    }



    // Adapter for the viewpager using FragmentPagerAdapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}