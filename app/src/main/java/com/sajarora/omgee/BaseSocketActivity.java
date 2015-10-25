package com.sajarora.omgee;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by sajarora on 10/24/15.
 */
public class BaseSocketActivity extends Activity {
    private Socket mSocket;
    private static final String TAG = BaseSocketActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSocket();
        if (mSocket != null) {
            mSocket.connect();
        }
    }

    private void initSocket() {
        try {
            mSocket = IO.socket("http://45.55.172.186/");
        } catch (URISyntaxException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSocket != null)
            mSocket.disconnect();
    }

    public void sendJsonData(String event, JSONObject data){
        if (mSocket.connected()){
            mSocket.emit(event, data);
        }
    }

    public Socket getSocket(){
        return mSocket;
    }
}
