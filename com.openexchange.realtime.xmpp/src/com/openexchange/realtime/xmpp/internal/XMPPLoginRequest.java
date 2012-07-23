
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

    private String user;

    private String password;

    public XMPPLoginRequest(String user, String password) {
        this.user = user;
        this.password = password;
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

}
