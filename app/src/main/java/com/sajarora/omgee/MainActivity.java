package com.sajarora.omgee;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseSocketActivity implements View.OnClickListener, HeartRateConsentListener, IBandCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SOCKET_USERNAME = "username";
    private static final String SOCKET_HEARTRATE = "heartrate";
    private static final String SOCKET_TIMESTAMP = "timestamp";
    private static final String SOCKET_SENSOR_HEARTRATE = "sensorHeartrate";
    private Toolbar mToolbar;

    private BandClient mBandClient;

    private ProgressBar mProgress;
    private RelativeLayout mLLDashboard;
    private TextView mTextBandInfo;
    private TextView mTextHeartRate;
    private MyBandHeartRateListener mBandListener;
    private JSONObject mJsonData;
    private boolean isMonitoring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //set up toolbar
        setUpToolbar();

        //init all the views
        initViews();

        //enable band
        setUpBand();
    }

    /**
     * Initializes all views by finding them in the xml and assigning them to a
     * variable
     */
    private void initViews() {
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mLLDashboard = (RelativeLayout) findViewById(R.id.rl_dashboard);
        //make dashboard invisible until band is ready
        mLLDashboard.setVisibility(View.GONE);
        mTextBandInfo = (TextView)findViewById(R.id.txt_band_info);
        mTextHeartRate = (TextView)findViewById(R.id.txt_heart_rate);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
    }

    /**
     * Microsoft band: find all connected
     */
    private void setUpBand() {
        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
        new ConnectToBand().execute(pairedBands);
    }

    public BandClient getBand(){
        return mBandClient;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                startHRMonitor();
                return;
            case R.id.btn_stop:
                stopHRMonitor();
                return;
        }
    }

    // create a heart rate event listener -> subscribes to events
    private class MyBandHeartRateListener implements BandHeartRateEventListener {
        private final IBandCallbacks mCallbacks;

        public MyBandHeartRateListener(IBandCallbacks callbacks){
            this.mCallbacks = callbacks;
        }

        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent event) {
            // do work on heart rate changed (i.e., update UI)
            Log.d(TAG, "HR: " + event.getHeartRate());
            mCallbacks.updateHR(event.getHeartRate(), event.getTimestamp());
        }
    };

    //Stops the HR monitoring
    private void stopHRMonitor() {
        isMonitoring = false;
        mTextBandInfo.setText("HR Monitoring Stopped.");
        try {
            // unregister the listener
            mBandClient.getSensorManager().unregisterHeartRateEventListener(mBandListener);
            mBandListener = null;
        } catch(BandIOException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    //Starts HR monitoring
    private void startHRMonitor() {
        if (getSocket() == null){
            Snackbar.make(mLLDashboard, "Backend connection not open.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        isMonitoring = true;
        mJsonData = new JSONObject();

        mBandListener = new MyBandHeartRateListener(this);
        checkForConsent();
        mTextBandInfo.setText("HR Monitoring Started...");
        try {
            // register the listener
            mBandClient.getSensorManager().registerHeartRateEventListener(
                    mBandListener);
        } catch (BandException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    @Override
    public void updateHR(final float heartRate, long timestamp) {
        if (!isMonitoring)
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextHeartRate.setText("HR: " + heartRate);
            }
        });
        try {
            mJsonData.put(SOCKET_USERNAME, MainApplication.getInstance().getUser().username);
            mJsonData.put(SOCKET_HEARTRATE, heartRate);
            mJsonData.put(SOCKET_TIMESTAMP, timestamp);
            sendJsonData(SOCKET_SENSOR_HEARTRATE, mJsonData);
        } catch(JSONException ex){
            Log.d(TAG, ex.getMessage());
        }
    }

    private void checkForConsent() {
        if(mBandClient.getSensorManager().getCurrentHeartRateConsent() !=
                UserConsent.GRANTED) {
            // user hasn’t consented, request consent
            // the calling class is an Activity and implements
            // HeartRateConsentListener
            mBandClient.getSensorManager().requestHeartRateConsent(this,
                    this);
        }
    }

    @Override
    public void userAccepted(boolean b) {
        Snackbar.make(mLLDashboard, "HR monitoring is turned on.", Snackbar.LENGTH_SHORT).show();
    }

    //Make a connection to one specific band (right now it is just the first band)
    private class ConnectToBand extends AsyncTask<BandInfo, Void, Boolean> {

        @Override
        protected Boolean doInBackground(BandInfo... pairedBands) {
            if (pairedBands.length > 0) {
                mBandClient = BandClientManager.getInstance().create(MainActivity.this, pairedBands[0]);
                BandPendingResult<ConnectionState> pendingResult =
                        mBandClient.connect();
                try {
                    ConnectionState state = pendingResult.await();
                    if (state == ConnectionState.CONNECTED) {
                        return true;
                    } else {
                        Snackbar.make(mLLDashboard, "No paired bands.", Snackbar.LENGTH_SHORT).show();
                        return false;
                    }
                } catch (InterruptedException | BandException ex) {
                    Log.d(TAG, ex.getMessage());
                    Snackbar.make(mLLDashboard, "Failed to pair with band.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mLLDashboard, "No paired bands.", Snackbar.LENGTH_SHORT).show();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            mProgress.setVisibility(View.GONE);
            if (result){
                mLLDashboard.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                MainApplication.getInstance().setUser(null);
                startActivity(new Intent(this , LoginActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setActionBar(mToolbar);
        }
    }
}
