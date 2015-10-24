package com.sajarora.omgee;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    LinearLayout mContentFrame;

    private static final String PREFERENCES_FILE = "mymaterialapp_settings";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;
    private BandClient mBandClient;

    private static final int NAVIGATION_DASHBOARD = 0;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setUpToolbar();

        mProgress = (ProgressBar) findViewById(R.id.progress);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);

        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(this, PREF_USER_LEARNED_DRAWER, "false"));

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        setUpNavDrawer();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mContentFrame = (LinearLayout) findViewById(R.id.nav_contentframe);

        setUpBand();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_dashboard:
                        mCurrentSelectedPosition = NAVIGATION_DASHBOARD;
                }
                goToFragment(NAVIGATION_DASHBOARD);
                return true;
            }
        });

    }

    private void goToFragment(int navigationDashboard) {
        Snackbar.make(mContentFrame, "Dashboard", Snackbar.LENGTH_SHORT).show();
        getFragmentManager().beginTransaction().replace(R.id.nav_content,
                DashboardFragment.getInstance()).commit();
    }

    private void setUpBand() {
        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
        new ConnectToBand().execute(pairedBands);
    }

    public BandClient getBand(){
        return mBandClient;
    }

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
                        Snackbar.make(mContentFrame, "No paired bands.", Snackbar.LENGTH_SHORT).show();
                        return false;
                    }
                } catch (InterruptedException | BandException ex) {
                    Log.d(TAG, ex.getMessage());
                    Snackbar.make(mContentFrame, "Failed to pair with band.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mContentFrame, "No paired bands.", Snackbar.LENGTH_SHORT).show();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            mProgress.setVisibility(View.GONE);
            if (result)
                goToFragment(NAVIGATION_DASHBOARD);
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION, 0);
        Menu menu = mNavigationView.getMenu();
        menu.getItem(mCurrentSelectedPosition).setChecked(true);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setActionBar(mToolbar);
        }
    }

    private void setUpNavDrawer() {
        if (mToolbar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.ic_action_menu);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (!mUserLearnedDrawer) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            mUserLearnedDrawer = true;
            saveSharedSetting(this, PREF_USER_LEARNED_DRAWER, "true");
        }

    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }
}
