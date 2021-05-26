package com.example.p1apidemo;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

public class AmmRequestConstructor {


    private String AMM_TARGET;
    private String GET_PROTOCOL = "http";
    private String GET_GATEWAYS_ENDPOINT = "api/v1/systems";
    private String GET_GROUPS_ENDPOINT = "api/v1/systems/groups";
    private String ACCESS_TOKEN_PARAM = "access_token";
    private String HEARBEAT_PARAM = "heartbeat";
    private String GROUPID_PARAM = "groupid";
    private String PLATFORM_PARAM = "platforms";
    private String GATEWAY_PARAM = "gateways";
    private String CELLULARS_PARAM = "cellulars";

    Token token;

    public String getAMM_TARGET() {
        return AMM_TARGET;
    }
    public void setAMM_TARGET(String AMM_TARGET) {
        this.AMM_TARGET = AMM_TARGET;
    }
    public Token getToken() {
        return token;
    }
    public void setToken(Token token) {
        this.token = token;
    }


    public AmmRequestConstructor(String target, Token token){
        AMM_TARGET = target;
        this.token = token;
    }

    public String getAllGateways(){
        return getGateways(null, null, "", "");
    }

    public String getAllGroups(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(GET_PROTOCOL)
                .authority(AMM_TARGET)
                .appendEncodedPath(GET_GROUPS_ENDPOINT)
                .appendQueryParameter(ACCESS_TOKEN_PARAM, token.getAccess_token());

        String url = builder.build().toString();

        return url;
    }

    public String getGateways(List<String> cellulars, List<String> platforms, String heartbeat, String groupid) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(GET_PROTOCOL)
                .authority(AMM_TARGET)
                .appendEncodedPath(GET_GATEWAYS_ENDPOINT)
                .appendQueryParameter(ACCESS_TOKEN_PARAM, token.getAccess_token());
        
        if(!isNullorEmpty(heartbeat)){
            builder.appendQueryParameter(HEARBEAT_PARAM, heartbeat);
        }

        if(!isNullorEmpty(groupid)){
            builder.appendQueryParameter(GROUPID_PARAM, groupid);
        }

        if(!isNullorEmpty(platforms)){
            builder.appendQueryParameter(PLATFORM_PARAM, TextUtils.join(",", platforms));
        }

        if(!isNullorEmpty(cellulars)){
            builder.appendQueryParameter(CELLULARS_PARAM, TextUtils.join(",", cellulars));
        }


        String url = builder.build().toString();

        return url;
    }

    public String getLatestMapStats(String uid) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(GET_PROTOCOL)
                .authority(AMM_TARGET)
                .appendEncodedPath(GET_GATEWAYS_ENDPOINT)
                .appendEncodedPath(uid)
                .appendEncodedPath("data")
                .appendQueryParameter(ACCESS_TOKEN_PARAM, token.getAccess_token())
                .appendQueryParameter("ids", "ReportIdleTime,GPS Location-latitude,GPS Location-longitude,GPS FixDimension");

        return builder.build().toString();
    }

    public String getHistoricalStats(String uid, String dataid) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(GET_PROTOCOL)
                .authority(AMM_TARGET)
                .appendEncodedPath(GET_GATEWAYS_ENDPOINT)
                .appendEncodedPath("data")
                .appendEncodedPath("raw")
                .appendQueryParameter(ACCESS_TOKEN_PARAM, token.getAccess_token())
                .appendQueryParameter("targetid", uid)
                .appendQueryParameter("dataid", dataid );

        return builder.build().toString();
    }

    public String getLogout(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(GET_PROTOCOL)
                .authority(AMM_TARGET)
                .appendEncodedPath("api/oauth/expire")
                .appendQueryParameter(ACCESS_TOKEN_PARAM, token.getAccess_token());

        return builder.build().toString();
    }

    private boolean isNullorEmpty(String s) {
        return s==null || s.isEmpty();
    }

    private boolean isNullorEmpty(List<String> list) {
        return list == null || list.isEmpty();
    }
}
