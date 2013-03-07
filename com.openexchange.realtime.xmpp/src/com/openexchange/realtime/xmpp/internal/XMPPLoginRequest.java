/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */


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
