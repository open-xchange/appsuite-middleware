package com.openexchange.oidc.state;

public class DefaultLogoutRequestInfo implements LogoutRequestInfo {
    
    private String state;
    private String domainName;
    private String sessionId;
    
    public DefaultLogoutRequestInfo(String state, String domainName, String sessionId) {
        super();
        this.state = state;
        this.domainName = domainName;
        this.sessionId = sessionId;
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
    public String getSessionId() {
        return this.sessionId;
    }

}
