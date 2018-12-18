package com.openexchange.oidc.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.session.Origin;
import com.openexchange.session.Session;


public class MockedSession implements Session {

    Map<String, Object> parameterMap;


    public MockedSession() {
        super();
        this.parameterMap = new HashMap<>();
    }

    @Override
    public int getContextId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getLocalIp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLocalIp(String ip) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getLoginName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsParameter(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object getParameter(String name) {
        return this.parameterMap.get(name);
    }

    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRandomToken() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSecret() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSessionID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getUserId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getUserlogin() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLogin() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setParameter(String name, Object value) {
        this.parameterMap.put(name, value);
    }

    @Override
    public String getAuthId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHash() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setHash(String hash) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getClient() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setClient(String client) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isTransient() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<String> getParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Origin getOrigin() {
        return null;
    }

}
