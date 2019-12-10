package com.brainykat.kariuki.nfconpos;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.basewin.log.LogUtil;
import com.basewin.services.ServiceManager;

public class MainActivity extends AppCompatActivity implements Listener {
    public static final String TAG = MainActivity.class.getSimpleName();
    private NfcAdapter mNfcAdapter;
    private NFCReadFragment mNfcReadFragment;
    Button MyButton;
    TextView FullNames,MilkNumber,ET_ColPoint;
    //CardBinder cardService;
    private boolean isDialogDisplayed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ServiceManager.getInstence().init(this);
        LogUtil.openLog();
        MyButton = findViewById(R.id.button);
        FullNames = findViewById(R.id.textView);
        MilkNumber = findViewById(R.id.textViewMilkNo);
        ET_ColPoint = findViewById(R.id.textViewCol);
        //card();
        initNFC();
        MyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReadFragment();
            }
        });
    }
    private void showReadFragment() {

        mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance();
        }
        mNfcReadFragment.show(getFragmentManager(),NFCReadFragment.TAG);

    }
    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            snackThem("Adapter good");
        } else {
            snackThemRed("Please Enable NFC");
        }
    }
    //Read Card for Farmer Info
    @Override
    protected void onNewIntent(Intent intent) {
        snackThemRed("Manji Maji");
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            showReadFragment();
            MifareClassic mdef = MifareClassic.get(tag);

            //mNfcReadFragment = (NFCReadFragment)getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
            String[] customerData = mNfcReadFragment.onNfcDetected(mdef);

                    if (customerData.length == 16 && customerData[6].trim().length() > 0 && !customerData[8].contains("Authentication Failed ` ")) {
                        if (customerData[6] != null || !customerData[6].trim().isEmpty()) {
                            FullNames.setText(customerData[6].substring(0, 16) + customerData[7].substring(0, customerData[7].indexOf('`')));
                            MilkNumber.setText(customerData[8].substring(0, customerData[8].indexOf('`')));
                            ET_ColPoint.setText(customerData[15].substring(0, customerData[15].indexOf('`')));
                            //setControlsFocusable(false);
                            //cardRead = true;
                            //btn_enableDisable(true);
                        } else {
                            snackThem("Card Data Corrupt");
                        }
                    } else {
                        snackThem("Card Invalid or Broken");
                    }


        } else {
            Log.i("TAG", "Invalid Tag Signature");
            snackThem("Invalid Tag Signature");
        }
    }
    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {
        isDialogDisplayed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void snackThem(String msg) {
        View v = findViewById(R.id.cordinator);
        Snackbar snackbar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.GREEN);
        snackbar.show();
    }
    private void snackThemRed(String msg) {
        View v = findViewById(R.id.cordinator);
        Snackbar snackbar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.RED);
        snackbar.show();
    }
}
