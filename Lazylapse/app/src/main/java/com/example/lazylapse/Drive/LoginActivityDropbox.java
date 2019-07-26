package com.example.lazylapse.Drive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dropbox.core.android.Auth;
import com.example.lazylapse.App;
import com.example.lazylapse.Interface.Controller;
import com.example.lazylapse.R;

/**
 * Activity prompted when starting the app without access token saved in sharedPreference in order
 * to obtain one.
 *
 * authored by Valdio Veliu on https://www.sitepoint.com/adding-the-dropbox-api-to-an-android-app/
 */
public class LoginActivityDropbox extends AppCompatActivity {
    /**
     * Called when the activity is started, and display the layout, and set the onClick method of
     * the button in the activty to open a login window either on navigator or the local dropbox app
     * if installed (for some reason it only works with the app installed).
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button SignInButton = (Button) findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Auth.startOAuth2Authentication(App.getContext(), getString(R.string.APP_KEY));
            }
        });
    }

    /**
     * onResume is called when we return the activity to the foreground after having it in the
     * background (when you go back to the app after authentication's complete). This will call
     * {@link #getAccessToken()}, which will, if the user is authenticated, store the token in
     * shared preferences.
     */
    @Override
    protected void onResume() {
        super.onResume();
        try{
            getAccessToken();
        }catch(Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    /**
     * if the user is authenticated, store the token in shared preferences.
     */
    public void getAccessToken() {
        String accessToken = Auth.getOAuth2Token(); //generate Access Token
        Toast.makeText(this,accessToken,Toast.LENGTH_LONG).show();
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("com.example.valdio.dropboxintegration", Context.MODE_PRIVATE);
            prefs.edit().putString("access-token", accessToken).apply();

            //Proceed to MainActivity
            Intent intent = new Intent(LoginActivityDropbox.this, Controller.class);
            startActivity(intent);
        }
    }
}