package com.example.p1apidemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;

import com.google.gson.Gson;

/*
Activity that handles Authentication with AMM. Request to authenticate is first sent to AMM, we
expect a login form as a response. We display the login form as a WebView.
Once form is sent back and if authentication is successful, resposne will be a redirect containing
acccess token
 */
public class AuthenticateActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";

    private WebView webView;
    ImplicitAuthenticator ia;
    Token token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().supportZoom();
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();

        //retrieve sharedPreferences since that is where we stored client id and target amm
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String amm_target = sharedPref.getString(getString(R.string.AMM_HOST_ADDRESS_PARAM), "");
        String clientID = sharedPref.getString(getString(R.string.client_id_param),"");
        String redirectUri = sharedPref.getString(getString(R.string.redirect_uri_param),"");

        ia = new ImplicitAuthenticator(clientID, amm_target, redirectUri);
        webView.setWebViewClient(new AuthenticateClient(ia.getRedirectUri()));

        webView.loadUrl(ia.getImplicitFlowUrl());

        Log.println(Log.INFO, TAG, "created webview: " + webView.getUrl());
    }

    @Override
    protected void onPause () {
        super.onPause();
    }

    @Override
    protected void onResume () {
        String action = getIntent().getStringExtra("action");

        if(!action.equals("refresh")){
            CookieManager.getInstance().removeAllCookie();
        }

        webView.loadUrl(ia.getImplicitFlowUrl());

        super.onResume();
    }

    protected void parseRedirect(String url){
        try{
            Token token = ia.parseSuccsessfulRedirect(url);
            Context context = getApplicationContext();
            SharedPreferences sharedPref = context.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            Gson gson = new Gson();
            String json = gson.toJson(token);
            editor.putString(getString(R.string.token_param), json);
            editor.commit();

            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);

        } catch (Exception e) {
            e.printStackTrace();

            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("status", "error");
            startActivity(i);
        }
    }

    /*
    Extend the normal webViewClient so that we can listen to redirect provided by AMM.
    Redirect url is set in AMM API Clients page
    */
    private class AuthenticateClient extends WebViewClient {
        private String redirect_url;

        public AuthenticateClient(String redirect_url) {
            super();
            this.redirect_url = redirect_url;
        }

        //overload of shouldOverrideUrlLoading() to accomodate for different android versions
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG,"intercepting request string = " + url);

            if(url.contains(redirect_url)){
                parseRedirect(url);
            }

            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url) {
            Log.println(Log.INFO, TAG,"intercepting url ");
            if(url.getUrl().toString().contains(redirect_url)){
                parseRedirect(url.getUrl().toString());
            }
            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }



    }
}