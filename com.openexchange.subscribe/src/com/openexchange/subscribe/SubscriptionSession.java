package com.openexchange.subscribe;

import com.openexchange.session.Session;


public class SubscriptionSession implements Session {
    private Subscription subscription;
    public SubscriptionSession(Subscription subscription){
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

    public Object getParameter(String name) {
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

    public void removeUploadedFileOnly(String id) {
        throw new UnsupportedOperationException();
    }

    public void setParameter(String name, Object value) {
        throw new UnsupportedOperationException();        
    }

}
