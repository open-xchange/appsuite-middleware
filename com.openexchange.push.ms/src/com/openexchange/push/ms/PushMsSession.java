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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.push.ms;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.push.ms.osgi.PushMsActivator;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;


/**
 * {@link PushMsSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PushMsSession implements PutIfAbsent {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(PushMsSession.class);

    /**
     * Deserializes a session from a map that was previously wrapped via {@link PushMsSession#wrap}.
     *
     * @param map The map containing the wrapped session
     * @return A new session instance representing the wrapped session
     */
    public static PushMsSession unwrap(Map<String, Serializable> map) {
        PushMsSession session = new PushMsSession();
        session.loginName = (String) map.get("__loginName");
        session.password = unobfuscate((String) map.get("__password"));
        Integer intObject = (Integer) map.get("__contextId");
        session.contextId = null != intObject ? intObject.intValue() : 0;
        intObject = (Integer) map.get("__userId");
        session.userId = null != intObject ? intObject.intValue() : 0;
        session.sessionId = (String) map.get("__sessionId");
        session.secret = (String) map.get("__secret");
        session.login = (String) map.get("__login");
        session.randomToken = (String) map.get("__randomToken");
        session.localIp = (String) map.get("__localIp");
        session.authId = (String) map.get("__authId");
        session.hash = (String) map.get("__hash");
        session.client = (String) map.get("__client");
        session.userLogin = (String) map.get("__userLogin");
        Boolean booleanObject = (Boolean) map.get("__tranzient");
        session.tranzient = null != booleanObject ? booleanObject.booleanValue() : false;
        Serializable capabilities = map.get("__paramCapabilities");
        if (null != capabilities) {
            session.setParameter(PARAM_CAPABILITIES, capabilities);
        }
        Serializable alternativeId = map.get("__paramAlternativeId");
        if (null != alternativeId) {
            session.setParameter(PARAM_ALTERNATIVE_ID, alternativeId);
        }
        return session;
    }

    /**
     * Serializes a session to a POJO map.
     *
     * @param session The session to wrap
     * @return The wrapped session
     */
    public static Map<String, Serializable> wrap(Session session) {
        Map<String, Serializable> map = new LinkedHashMap<String, Serializable>(24);
        map.put("__loginName", session.getLoginName());
        map.put("__password", obfuscate(session.getPassword()));
        map.put("__contextId", Integer.valueOf(session.getContextId()));
        map.put("__userId", Integer.valueOf(session.getUserId()));
        map.put("__sessionId", session.getSessionID());
        map.put("__secret", session.getSecret());
        map.put("__login", session.getLogin());
        map.put("__randomToken", session.getRandomToken());
        map.put("__localIp", session.getLocalIp());
        map.put("__authId", session.getAuthId());
        map.put("__hash", session.getHash());
        map.put("__client", session.getClient());
        map.put("__userLogin", session.getUserlogin());
        map.put("__tranzient", Boolean.valueOf(session.isTransient()));
        Object capabilities = session.getParameter(PARAM_CAPABILITIES);
        if (null != capabilities && Serializable.class.isInstance(capabilities)) {
            map.put("__paramCapabilities", (Serializable) capabilities);
        }
        Object alternativeId = session.getParameter(PARAM_ALTERNATIVE_ID);
        if (null != alternativeId && Serializable.class.isInstance(alternativeId)) {
            map.put("__paramAlternativeId", (Serializable) alternativeId);
        }
        return map;
    }

    private String loginName;
    private String password;
    private int contextId;
    private int userId;
    private String sessionId;
    private String secret;
    private String login;
    private String randomToken;
    private String localIp;
    private String authId;
    private String hash;
    private String client;
    private String userLogin;
    private boolean tranzient;
    private final ConcurrentMap<String, Object> parameters;

    /**
     * Initializes a new {@link PushMsSession}.
     */
    private PushMsSession() {
        super();
        this.parameters = new ConcurrentHashMap<String, Object>();
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public String getLocalIp() {
        return localIp;
    }

    @Override
    public void setLocalIp(String ip) {
        localIp = ip;
    }

    @Override
    public String getLoginName() {
        return loginName;
    }

    @Override
    public boolean containsParameter(String name) {
        return parameters.containsKey(name);
    }

    @Override
    public Object getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getRandomToken() {
        return randomToken;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public String getSessionID() {
        return sessionId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getUserlogin() {
        return userLogin;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    @Override
    public String getAuthId() {
        return authId;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public boolean isTransient() {
        return tranzient;
    }

    @Override
    public Object setParameterIfAbsent(String name, Object value) {
        return parameters.putIfAbsent(name, value);
    }

    private static String obfuscate(String string) {
        if (Strings.isEmpty(string)) {
            return string;
        }
        CryptoService cryptoService = Services.getService(CryptoService.class);
        if (null == cryptoService) {
            LOG.error("Unable to obfuscate without crypto service");
            return string;
        }
        try {
            return cryptoService.encrypt(string, PushMsActivator.INIT_REF.get().getObfuscationKey());
        } catch (OXException e) {
            LOG.error("Error obfuscating string", e);
            return string;
        }
    }

    private static String unobfuscate(final String string) {
        if (Strings.isEmpty(string)) {
            return string;
        }
        CryptoService cryptoService = Services.getService(CryptoService.class);
        if (null == cryptoService) {
            LOG.error("Unable to unobfuscate without crypto service");
            return string;
        }
        try {
            return cryptoService.decrypt(string, PushMsActivator.INIT_REF.get().getObfuscationKey());
        } catch (OXException e) {
            LOG.error("Error unobfuscate string", e);
            return string;
        }
    }

}
