
package com.openexchange.realtime.xmpp.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.authentication.Cookie;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;

public class XMPPLoginRequest implements LoginRequest {

    private final String user;

    private final String password;

    private String host;

    private int port;

    public XMPPLoginRequest(String user, String password, String host, int port) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public String getUserAgent() {
        return "chat";
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getLogin() {
        return user;
    }

    @Override
    public Interface getInterface() {
        return Interface.HTTP_JSON;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return Collections.emptyMap();
    }

    @Override
    public String getHash() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public String getClientIP() {
        return "";
    }

    @Override
    public String getClient() {
        return "chat";
    }

    @Override
    public String getAuthId() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    @Override
    public String getClientToken() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getServerName() {
        return host;
    }

    @Override
    public int getServerPort() {
        return port;
    }

    @Override
    public String getHttpSessionID() {
        return null;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

}
