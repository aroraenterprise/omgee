package com.sajarora.omgee;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

/**
 * Created by sajarora on 10/24/15.
 */
public class DashboardFragment extends Fragment implements View.OnClickListener, HeartRateConsentListener {

    private static final String TAG = DashboardFragment.class.getSimpleName();
    private MainActivity mActivity;
    private TextView mTextBandInfo;
    private View mContentView;
    private TextView mTextHeartRate;

    public static Fragment getInstance() {
        return new DashboardFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActivity = (MainActivity)getActivity();
        initViews();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mTextBandInfo = (TextView)mContentView.findViewById(R.id.txt_band_info);
        mTextHeartRate = (TextView)mContentView.findViewById(R.id.txt_heart_rate);
        mContentView.findViewById(R.id.btn_start).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_stop).setOnClickListener(this);
        return mContentView;
    }

    private void initViews() {
        String fwVersion = null;
        String hwVersion = null;
        try {
            fwVersion = mActivity.getBand().getFirmwareVersion().await();
            hwVersion = mActivity.getBand().getHardwareVersion().await();
            mTextBandInfo.setText("Software Version: " + fwVersion +
                    " - Hardware Version: " + hwVersion);
        } catch (InterruptedException | BandException ex) {
            Log.d(TAG, ex.getMessage());
            Snackbar.make(mContentView, "Failed to get band version.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void subscribeToHeartRateSensor(){
        if(mActivity.getBand().getSensorManager().getCurrentHeartRateConsent() !=
                UserConsent.GRANTED) {
            // user hasn’t consented, request consent
            // the calling class is an Activity and implements
            // HeartRateConsentListener
            mActivity.getBand().getSensorManager().requestHeartRateConsent(getActivity(),
                    this);
        }
    }

    // create a heart rate event listener
    BandHeartRateEventListener heartRateListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent event) {
            mTextHeartRate.setText(event.toString());
        }
    };

    public void startHeartRateMonitor(){
        try {
            subscribeToHeartRateSensor();
            //register the listener
            mActivity.getBand().getSensorManager().registerHeartRateEventListener(
                    heartRateListener);
        } catch (BandException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void stopHeartRateMonitor() {
        super.onDetach();
        try {
            // unregister the listener
            mActivity.getBand().getSensorManager().unregisterHeartRateEventListener(heartRateListener);
        } catch(BandIOException ex) {
            // handle BandException
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                startHeartRateMonitor();
                return;
            case R.id.btn_stop:
                stopHeartRateMonitor();
                return;
        }
    }

    @Override
    public void userAccepted(boolean b) {

    }
}
