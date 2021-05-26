package com.example.p1apidemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv= (TextView) findViewById(R.id.mainText);
        tv.setTextSize(18);
        tv.setPadding(26,80,26,0);
        tv.setText("DISCLAIMER: This is a Sierra Wireless AMM API Demo Application. It is meant to be used as a resource for those wishing to use AMM API. It is NOT an official product");

        Context context = getApplicationContext();

        //sets target AMM and Client ID, this can be set in res/values/strings.xml
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.AMM_HOST_ADDRESS_PARAM), getString(R.string.AMM_HOST_ADDRESS));
        editor.putString(getString(R.string.client_id_param), getString(R.string.client_id));
        editor.putString(getString(R.string.redirect_uri_param), getString(R.string.redirect_uri));

        editor.apply();
    }

    public void clickLogin(View view) {

        Log.i("main", "clicked Login button");

        Intent i = new Intent(this, AuthenticateActivity.class);
        i.putExtra("action", "login");
        startActivity(i);

    }

    @Override
    protected void onResume() {
        String status  = getIntent().getStringExtra("status");
        if (status!=null && status.contains("error")){
            Log.i("MAIN", "error getting access token");
        }
        super.onResume();
    }
}
