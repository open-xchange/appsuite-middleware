package com.openexchange.oidc.state;


public interface LogoutRequestInfo {
    
    /**
     * The state of the client, that triggered the request.
     * 
     * @return The state of the client. Never <code>null</code>
     */
    String getState();
    
    /**
     * The domain name, the request is coming from.
     * 
     * @return The domain name of the client. Never <code>null</code>
     */
    String getDomainName();
    
    String getSessionId();
}
