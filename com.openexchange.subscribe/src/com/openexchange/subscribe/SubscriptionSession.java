package com.openexchange.subscribe;

import com.openexchange.session.Session;


public class SubscriptionSession implements Session {
    private final Subscription subscription;
    public SubscriptionSession(final Subscription subscription){
        this.subscription = subscription;
    }
    
    //IMPLEMENTED:
    public int getContextId() {
        return subscription.getContext().getContextId();
    }
    
    public int getUserId() {
        return subscription.getUserId();
    }
    
    //NOT IMPLEMENTED AT ALL:
    public String getLocalIp() {
        throw new UnsupportedOperationException();
    }

    public String getLogin() {
        throw new UnsupportedOperationException();
    }

    public String getLoginName() {
        throw new UnsupportedOperationException();
    }

    public Object getParameter(final String name) {
        throw new UnsupportedOperationException();
    }

    public boolean containsParameter(final String name) {
        throw new UnsupportedOperationException();
    }

    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    public String getRandomToken() {
        throw new UnsupportedOperationException();
    }

    public String getSecret() {
        throw new UnsupportedOperationException();
    }

    public String getSessionID() {
        throw new UnsupportedOperationException();
    }

    public String getUserlogin() {
        throw new UnsupportedOperationException();
    }

    public void removeRandomToken() {
        throw new UnsupportedOperationException();
    }

    public void removeUploadedFileOnly(final String id) {
        throw new UnsupportedOperationException();
    }

    public void setParameter(final String name, final Object value) {
        throw new UnsupportedOperationException();        
    }

}
