package com.stollmann.tiov2sample;


import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;


import com.stollmann.shared.STTrace;
import com.stollmann.shared.STUtil;
import com.stollmann.terminalIO.TIOManager;
import com.stollmann.terminalIO.TIOPeripheral;
import com.stollmann.terminalIO.TIOPeripheralCallback;


import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.SystemClock;
import android.view.Window;
import android.widget.ToggleButton;


import java.math.BigDecimal;
import java.math.RoundingMode;


public class CalorieActivity extends DemoBase implements TIOPeripheralCallback {

    private static final int RSSI_INTERVAL = 1670;
    private static final int NUM_PACKAGES = 5;

    private TIOPeripheral _peripheral;
    private Handler _rssiHandler = new Handler();
    private Runnable _rssiRunnable;
    private ToggleButton _connectionButton;


    //******************************************************************************
    // Activity overrides
    //******************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        STTrace.method("onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie);
        // Show the Up button in the action bar.
        //setupActionBar();

        this.connectViews();
        this.connectPeripheral();
        this.updateUIState();
        this.displayVersionNumber();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
	/*private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}*/


    @Override
    protected void onResume() {
        STTrace.method("onResume");

        if (this._peripheral.isConnected()) {
            this.startRSSITimer();
            //this._localCreditsTextView.setText(Integer.toString(this._peripheral.getLocalUARTCreditsCount()));
            //this._remoteCreditsTextView.setText(Integer.toString(this._peripheral.getRemoteUARTCreditsCount()));
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        STTrace.method("onPause");

        this.stopRSSITimer();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        STTrace.method("onDestroy");

        this._peripheral.setListener(null);

        super.onDestroy();
    }


    //******************************************************************************
    // UI event handlers
    //******************************************************************************

    public void onConnectButtonPressed() {
        STTrace.method("onConnectButtonPressed");

        this._peripheral.connect();
        this.updateUIState();

        while (this._peripheral.isConnecting()){
            //_connectionButton.setText(R.string.connecting);
        }
        _connectionButton.setTextOff(getString(R.string.toggle_off));

    }

    public void onDisconnectButtonPressed() {
        STTrace.method("onDisconnectButtonPressed");

        this.stopRSSITimer();
        this._peripheral.disconnect();
        _connectionButton.setTextOn(getString(R.string.toggle_on));
    }

    //******************************************************************************
    // TIOPeripheralCallback implementation
    //******************************************************************************

    @Override
    public void tioPeripheralDidConnect(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralDidConnect");

        this.updateUIState();
        this.startRSSITimer();

        if (!this._peripheral.shallBeSaved())
        {
            // save if connected for the first time
            this._peripheral.setShallBeSaved(true);
            TIOManager.sharedInstance().savePeripherals();
        }
    }

    @Override
    public void tioPeripheralDidFailToConnect(TIOPeripheral peripheral, String errorMessage) {
        STTrace.method("tioPeripheralDidFailToConnect", errorMessage);

        this.updateUIState();

        if (errorMessage.length() > 0) {
            STUtil.showErrorAlert("Failed to connect with error message: " + errorMessage, this);
        }
    }

    @Override
    public void tioPeripheralDidDisconnect(TIOPeripheral peripheral, String errorMessage) {
        STTrace.method("tioPeripheralDidDisconnect", errorMessage);

        this.stopRSSITimer();
        this.updateUIState();

        if (errorMessage.length() > 0) {
            STUtil.showErrorAlert("Disconnected with error message: " + errorMessage, this);
        }
    }

    CalorieMeasurement measurement = new CalorieMeasurement();

    @Override
    public void tioPeripheralDidReceiveUARTData(TIOPeripheral peripheral, byte[] data) {
        STTrace.method("tioPeripheralDidReceiveUARTData", STTrace.byteArrayToString(data));

        //Log.i("info","Received " + data.length + " bytes");
        try {

            DataCommand newData = new DataCommand(data);

            for (int i=0; i<4;i++) {
                measurement.CaloriesCalc(newData.Channel1[i]);
            }


        } catch (Exception ex) {
            STTrace.exception(ex);
        }
    }

    @Override
    public void tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral peripheral, int bytesWritten) {
        STTrace.method("tioPeripheralDidWriteNumberOfUARTBytes", Integer.toString(bytesWritten));

    }

    @Override
    public void tioPeripheralUARTWriteBufferEmpty(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralUARTWriteBufferEmpty");

    }

    @Override
    public void tioPeripheralDidUpdateAdvertisement(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralDidUpdateAdvertisement");

        // display peripheral properties
        //this._mainTitleTextView.setText(this._peripheral.getName() + "  " + this._peripheral.getAddress());
        //this._subTitleTextView.setText(this._peripheral.getAdvertisementDisplayString());
    }

    @Override
    public void tioPeripheralDidUpdateRSSI(TIOPeripheral peripheral, int rssi) {
        STTrace.method("tioPeripheralDidUpdateRSSI", Integer.toString(rssi));

        //this._rssiTextView.setText(Integer.toString(rssi));
    }

    @Override
    public void tioPeripheralDidUpdateLocalUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
        STTrace.method("tioPeripheralDidUpdateLocalUARTCreditsCount", Integer.toString(creditsCount));

        //this._localCreditsTextView.setText(Integer.toString(creditsCount));
    }

    @Override
    public void tioPeripheralDidUpdateRemoteUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
        STTrace.method("tioPeripheralDidUpdateRemoteUARTCreditsCount", Integer.toString(creditsCount));

        //this._remoteCreditsTextView.setText(Integer.toString(creditsCount));
    }


    //******************************************************************************
    // Internal methods
    //******************************************************************************

    private void connectViews() {
        STTrace.method("connectViews");

        this._connectionButton = (ToggleButton)this.findViewById(R.id.toggleConnect);

        _connectionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && !_peripheral.isConnected() &&!_peripheral.isConnecting()){
                    onConnectButtonPressed();
                } else if(_peripheral.isConnected()){
                    onDisconnectButtonPressed();
                }
            }
        });

    }

    private void connectPeripheral() {
        STTrace.method("connectPeripheral");

        // extract peripheral id (address) from intent
        Intent intent = this.getIntent();
        String address = intent.getStringExtra(TIOV2Sample.PERIPHERAL_ID_NAME);

        // retrieve peripheral instance from TIOManager
        this._peripheral = TIOManager.sharedInstance().findPeripheralByAddress(address);

        // register callback
        this._peripheral.setListener(this);

    }


    private void startRSSITimer() {
        STTrace.method("startRSSITimer");

        if (this._rssiRunnable == null) {
            this._rssiRunnable = new Runnable() {
                @Override
                public void run() {
                    CalorieActivity.this._peripheral.readRSSI();
                    CalorieActivity.this._rssiHandler.postDelayed(CalorieActivity.this._rssiRunnable, CalorieActivity.RSSI_INTERVAL);
                }
            };
        }
        this._peripheral.readRSSI();
        this._rssiHandler.postDelayed(this._rssiRunnable, CalorieActivity.RSSI_INTERVAL);
    }

    private void stopRSSITimer() {
        STTrace.method("stopRSSITimer");

        this._rssiHandler.removeCallbacks(this._rssiRunnable);
    }

    private void updateUIState() {
        STTrace.method("updateUIState");

    }

    private void displayVersionNumber() {
        PackageInfo packageInfo;
        String version = "";
        try {
            packageInfo = this.getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (Exception ex) {
            STTrace.exception(ex);
        }
        this.setTitle(this.getTitle() + " " + version);
    }

    public void getDataClick(View v){
        String textToSend = "cmd2";
        try {
            byte[] data = textToSend.getBytes("CP-1252");
            this._peripheral.writeUARTData(data);
        } catch (Exception ex) {
            STTrace.exception(ex);
        }
    }

}

