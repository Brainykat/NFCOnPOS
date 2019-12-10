package com.brainykat.kariuki.nfconpos;

import android.app.DialogFragment;
import android.content.Context;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

public class NFCReadFragment extends DialogFragment {

    public static final String TAG = NFCReadFragment.class.getSimpleName();

    public static NFCReadFragment newInstance() {

        return new NFCReadFragment();
    }

    private TextView mTvMessage;
    private Listener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_read, container, false);
        initViews(view);
        Log.e("Thuku","View created");
        return view;
    }

    private void initViews(View view) {
        mTvMessage = (TextView) view.findViewById(R.id.tv_message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("Thuku","Its Attached");
        mListener = (MainActivity)context;
        mListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    public String[] onNfcDetected(MifareClassic ndef) {
        Log.e("Thuku","Tusome sasa");
        return readFromNFC(ndef);
    }

    private String[] readFromNFC(MifareClassic mfc) {
        byte[] data;
        try {
            // 5.1) Connect to card
            mfc.connect();
            boolean auth = false;
            // 5.2) and get the number of sectors this card has..and loop thru these sectors
            int secCount = mfc.getSectorCount();
            int bCount = 0;
            int bIndex = 0;
            String[] infoData = new String[secCount];
            for(int j = 0; j < secCount; j++){
                // 6.1) authenticate the sector
                auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
                if(auth){
                    //Log.i(TAG, "Authentication Success " + String.valueOf(j));
                    // 6.2) In each sector - get the block count
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = 0;
                    final StringBuilder ms = new StringBuilder();
                    for(int i = 0; i < bCount; i++){
                        bIndex = mfc.sectorToBlock(j);
                        data = mfc.readBlock(bIndex);
                        ms.append(bytesToHex(data));
                        bIndex++;
                    }
                    //Log.i(TAG, HEXToString(ms.toString()));
                    infoData[j] =HEXToString(ms.toString());
                }else{ // Authentication failed - Handle it
                    infoData[j]="Authentication Failed ` "+ String.valueOf(j);
                    //Log.i(TAG, "Authentication Failed " + String.valueOf(j));
                }
            }
            return infoData;
        }catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            String[] yesNo = new String[1];
            return yesNo;
        }
        finally {
            if (mfc != null) {
                try {
                    mfc.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
    }
    // End of method
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
    public static String HEXToString(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
}
