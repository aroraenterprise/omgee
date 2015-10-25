package com.sajarora.omgee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sajarora.omgee.api.OmgeeApi;
import com.sajarora.omgee.api.OmgeeUser;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private EditText mEtUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (checkIsLoggedIn()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        initViews();
    }

    private boolean checkIsLoggedIn() {
        return MainApplication.getInstance().getUser() != null;
    }

    private void initViews() {
        mEtUsername = (EditText)findViewById(R.id.et_username);
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mEtUsername.getText().toString();
                if (username.isEmpty()) {
                    mEtUsername.setError("Please enter a username.");
                    return;
                }
                OmgeeApi.getInstance().service.login(new OmgeeUser(username),
                        new Callback<OmgeeUser>() {
                            @Override
                            public void success(OmgeeUser user, Response response) {
                                Toast.makeText(LoginActivity.this, "Logged in with: " +
                                        user.username, Toast.LENGTH_SHORT).show();
                                MainApplication.getInstance().setUser(user);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d(TAG, error.getMessage());
                                Toast.makeText(LoginActivity.this, "Failed to login.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
}
