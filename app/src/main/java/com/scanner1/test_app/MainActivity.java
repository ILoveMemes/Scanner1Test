package com.scanner1.test_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView neighbours;
    final private ArrayList<String> neighboursInfo = new ArrayList<>();
    private ArrayAdapter<String> neighboursAdapter;

    static public int UNAVAILABLE = 2147483647;

    private String mStr(int num) {
        if (num == UNAVAILABLE) {
            return "unavailable";
        }
        else {
            return Integer.toString(num);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("MissingPermission")
    public void updateNeighboursList() {
        neighboursInfo.clear();
        TelephonyManager tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        List<CellInfo> list = tManager.getAllCellInfo();
        if (list != null) {
            for (CellInfo info: list) {
                boolean gotInfo = false;
                String infoStr = "";
                if (info instanceof CellInfoGsm) {
                    boolean registered = info.isRegistered();
                    CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm)info).getCellSignalStrength();
                    double dbm = (double)cellSignalStrengthGsm.getDbm();
                    CellIdentityGsm cellIdentityGsm = ((CellInfoGsm)info).getCellIdentity();
                    int lac = cellIdentityGsm.getLac();
                    int cid = cellIdentityGsm.getCid();
                    infoStr = "GSM ";
                    if (registered) {
                        infoStr += "registered";
                    }
                    infoStr += " (" + dbm + " dBm)\n";
                    infoStr += "LAC: " + mStr(lac) + ";\nCID: " + mStr(cid);
                    gotInfo = true;
                }
                if (info instanceof CellInfoLte) {
                    boolean registered = info.isRegistered();
                    CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte)info).getCellSignalStrength();
                    double dbm = (double)cellSignalStrengthLte.getDbm();
                    CellIdentityLte cellIdentityLte = ((CellInfoLte) info).getCellIdentity();
                    int ci = cellIdentityLte.getCi();
                    int pci = cellIdentityLte.getPci();
                    int tac = cellIdentityLte.getTac();
                    infoStr = "LTE ";
                    if (registered) {
                        infoStr += "registered";
                    }
                    infoStr += " (" + dbm + " dBm)\n";
                    infoStr += "CI: " + mStr(ci) + ";\nPCI: " + mStr(pci) + ";\nTAC: " + mStr(tac);
                    gotInfo = true;
                }
                if (info instanceof CellInfoCdma) {
                    boolean registered = info.isRegistered();
                    CellSignalStrengthCdma cellSignalStrengthCdma = ((CellInfoCdma)info).getCellSignalStrength();
                    double dbm = (double)cellSignalStrengthCdma.getDbm();
                    CellIdentityCdma cellIdentityCdma = ((CellInfoCdma) info).getCellIdentity();
                    int bsid = cellIdentityCdma.getBasestationId();
                    int nid = cellIdentityCdma.getNetworkId();
                    int sid = cellIdentityCdma.getSystemId();
                    infoStr = "CDMA ";
                    if (registered) {
                        infoStr += "registered";
                    }
                    infoStr += " (" + dbm + " dBm)\n";
                    infoStr += "BSID: " + mStr(bsid) + ";\nNID: " + mStr(nid) + ";\nSID: " + mStr(sid);
                    gotInfo = true;
                }
                if (gotInfo) {
                    neighboursInfo.add(infoStr);
                }
            }
        }

        neighboursAdapter.notifyDataSetChanged();
    }

    private final int PERMISSION_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        neighbours = findViewById(R.id.lvCellInfo);
        neighboursAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, neighboursInfo);
        neighbours.setAdapter(neighboursAdapter);

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {


            int requestCode = 0;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE
                    },
                    PERMISSION_CODE
            );

        }



        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        telephonyManager.listen(new CustomPhoneStateListener(this), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ChangeServingBaseStation changeServingBSAction = new ChangeServingBaseStation(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Trying to change base station...", Snackbar.LENGTH_LONG)
                        .setAction("Action", changeServingBSAction).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return super.onOptionsItemSelected(item);

    }
}
