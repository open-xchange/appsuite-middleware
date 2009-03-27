package com.openexchange.publish.microformats.internal;

import com.openexchange.publish.Path;
import com.openexchange.session.Session;


public class PathSession implements Session {

    private Path path;
    
    
    public PathSession(Path path) {
        this.path = path;
    }
    
    public int getContextId() {
        return path.getContextId();
    }

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

    public int getUserId() {
        return path.getOwnerId();
    }

    public String getUserlogin() {
        throw new UnsupportedOperationException();
    }

    public void removeRandomToken() {
        throw new UnsupportedOperationException();
    }

    public void setParameter(String name, Object value) {
        throw new UnsupportedOperationException();
    }

}
