package com.openexchange.oidc.state;


public class DefaultLogoutRequestInfo implements LogoutRequestInfo {
    
    private String state;
    private String domainName;
    private String idToken;
    
    public DefaultLogoutRequestInfo(String state, String domainName, String idToken) {
        super();
        this.state = state;
        this.domainName = domainName;
        this.idToken = idToken;
    }

    @Override
    public String getState() {
        return this.state;
    }

    @Override
    public String getDomainName() {
        return this.domainName;
    }

    @Override
    public String getIDToken() {
        return this.idToken;
    }

}
