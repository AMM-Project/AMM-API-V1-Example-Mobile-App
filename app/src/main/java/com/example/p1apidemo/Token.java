package com.example.p1apidemo;

public class Token {
    private String access_token;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getExpirationDuration() {
        return expirationDuration;
    }

    public void setExpirationDuration(String expirationDuration) {
        this.expirationDuration = expirationDuration;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    private String token_type;
    private long creationTime;
    private String expirationDuration;
    private String scope;

    public Token(String token_id, String token_type, String expirationDuration, String scope){
        this.access_token = token_id;
        this.token_type = token_type;
        this.creationTime = System.currentTimeMillis();
        this.expirationDuration = expirationDuration;
        this.scope = scope;
    }
}
