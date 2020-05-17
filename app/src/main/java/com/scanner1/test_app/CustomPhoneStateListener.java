package com.scanner1.test_app;

import android.content.Context;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

import androidx.annotation.RequiresApi;

import java.util.List;

public class CustomPhoneStateListener extends PhoneStateListener {

    private MainActivity main;

    public CustomPhoneStateListener(MainActivity mainActivity) {
        main = mainActivity;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        main.updateNeighboursList();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo) {
        super.onCellInfoChanged(cellInfo);
        main.updateNeighboursList();
    }
}
