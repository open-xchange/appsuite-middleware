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

package com.openexchange.sessiond.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.services.Services;
import com.openexchange.sessionstorage.StoredSession;

/**
 * {@link Obfuscator}
 *
 * Utility class to wrap/unwrap sessions before/after putting/getting them from the session storage.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Obfuscator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessionImpl.class));
    private static final String[] WRAPPED_PARMETERS = { Session.PARAM_ALTERNATIVE_ID, Session.PARAM_CAPABILITIES };

    private final String obfuscationKey;

    /**
     * Initializes a new {@link Obfuscator}.
     *
     * @param obfuscationKey The key used to (un)obfuscate session passwords
     */
    public Obfuscator(String obfuscationKey) {
        super();
        this.obfuscationKey = obfuscationKey;
    }

    /**
     * Wraps a session before putting it to the storage.
     *
     * @param session The session
     * @return the wrapped session
     */
    public Session wrap(Session session) {
        if (null == session) {
            return null;
        }
        Map<String, Object> parameters = new HashMap<String, Object>(2);
        for (String param : WRAPPED_PARMETERS) {
            if (session.containsParameter(param)) {
                parameters.put(param, session.getParameter(param));
            }
        }
        return new StoredSession(session.getSessionID(), session.getLoginName(), obfuscate(session.getPassword()), session.getContextId(),
            session.getUserId(), session.getSecret(), session.getLogin(), session.getRandomToken(), session.getLocalIp(),
            session.getAuthId(), session.getHash(), session.getClient(), parameters);
    }

    /**
     * Unwraps a session after getting it from the storage.
     *
     * @param session The session
     * @return The unwrapped session
     */
    public Session unwrap(Session session) {
        if (null == session) {
            return null;
        }
        SessionImpl sessionImpl = new SessionImpl(session.getUserId(), session.getLoginName(), unobfuscate(session.getPassword()), session.getContextId(),
            session.getSessionID(), session.getSecret(), session.getRandomToken(), session.getLocalIp(), session.getLogin(),
            session.getAuthId(), session.getHash(), session.getClient(), false);
        for (String param : WRAPPED_PARMETERS) {
            if (session.containsParameter(param)) {
                sessionImpl.setParameter(param, session.getParameter(param));
            }
        }
        return sessionImpl;
    }

    private String obfuscate(final String string) {
        if (isEmpty(string)) {
            return string;
        }
        try {
            return Services.getService(CryptoService.class).encrypt(string, obfuscationKey);
        } catch (final OXException e) {
            LOG.error("Could not obfuscate string", e);
            return string;
        }
    }

    private String unobfuscate(final String string) {
        if (isEmpty(string)) {
            return string;
        }
        try {
            return Services.getService(CryptoService.class).decrypt(string, obfuscationKey);
        } catch (final OXException e) {
            LOG.error("Could not unobfuscate string", e);
            return string;
        }
    }

    private static boolean isEmpty(final String str) {
        if (null == str) {
            return true;
        }
        final int length = str.length();
        boolean empty = true;
        for (int i = 0; empty && i < length; i++) {
            empty = isWhitespace(str.charAt(i));
        }
        return empty;
    }

    /**
     * High speed test for whitespace!  Faster than the java one (from some testing).
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    private static boolean isWhitespace(final char c) {
        switch (c) {
            case 9:  //'unicode: 0009
            case 10: //'unicode: 000A'
            case 11: //'unicode: 000B'
            case 12: //'unicode: 000C'
            case 13: //'unicode: 000D'
            case 28: //'unicode: 001C'
            case 29: //'unicode: 001D'
            case 30: //'unicode: 001E'
            case 31: //'unicode: 001F'
            case ' ': // Space
                //case Character.SPACE_SEPARATOR:
                //case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
            default:
                return false;
        }
    }

}
