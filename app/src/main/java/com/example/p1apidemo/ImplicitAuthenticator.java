package com.example.p1apidemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.session.MediaSession;
import android.net.Uri;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
/*
Class that provides methods to allow implicit authentication
 */
public class ImplicitAuthenticator {
    private String clientID;
    private final String GRANT_TYPE = "implicit";
    private final String RESPONSE_TYPE = "token";
    private String AMM_TARGET;
    private final String IMPLICIT_ENDPOINT = "api\\oauth\\authorize";
    private String redirect_uri;
    private final String ACCESS_TOKEN_KEY = "access_token";
    private final String TOKEN_TYPE_KEY = "token_type";
    private final String EXPIRY_KEY = "expires_in";
    private final String SCOPE_KEY = "scope";

    public String getRedirectUri() {
        return redirect_uri;
    }


    public ImplicitAuthenticator(String clientID, String target, String redirect_uri){
        AMM_TARGET = target;
        this.clientID = clientID;
        this.redirect_uri = redirect_uri;
    }

    public String getImplicitFlowUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(AMM_TARGET)
                .appendEncodedPath(IMPLICIT_ENDPOINT)
                .appendQueryParameter("grant_type", GRANT_TYPE)
                .appendQueryParameter("response_type", RESPONSE_TYPE)
                .appendQueryParameter("client_id", clientID);
        String url = builder.build().toString();

        return url;

    }

    //builds a token from implicit flow redirect url
    public Token parseSuccsessfulRedirect(String url) throws Exception{
        URL url1 = new URL(url);
        Map<String, String> attributes = new HashMap<>();
        String fragment = url1.getRef();
        String[] kvPair = fragment.split("&");
        for(String kvpair : kvPair) {
            String key = kvpair.split("=")[0];
            String value = kvpair.split("=")[1];
            attributes.put(key, value);
        }

        String tokenType = attributes.get(TOKEN_TYPE_KEY);
        String tokenId = attributes.get(ACCESS_TOKEN_KEY);
        String expiry = attributes.get(EXPIRY_KEY);
        String scope = attributes.get(SCOPE_KEY);

        return new Token(tokenId, tokenType, expiry, scope);

    }


    public boolean isSuccessfulRedirect(String url) {
        return url.contains("error");
    }


}
