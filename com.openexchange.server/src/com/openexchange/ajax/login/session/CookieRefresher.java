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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.ajax.login.session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionServletInterceptor;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;


/**
 * {@link CookieRefresher} - Cares about refreshing session-associated cookies.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CookieRefresher implements SessionServletInterceptor {

    private static final String PARAM_COOKIE_REFRESH_TIMESTAMP = Session.PARAM_COOKIE_REFRESH_TIMESTAMP;
    private static final String PARAM_REFRESH_SESSION_COOKIE_FLAG = "as";

    private final LoginConfiguration conf;

    /**
     * Initializes a new {@link CookieRefresher}.
     */
    public CookieRefresher(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void intercept(Session session, HttpServletRequest req, HttpServletResponse resp) throws OXException {
        if (needsCookieRefresh(session)) {
            String hash = session.getHash();

            // Write secret+public cookie
            LoginServlet.writeSecretCookie(req, resp, session, hash, req.isSecure(), req.getServerName(), conf);

            // Refresh HTTP session, too
            req.getSession();
        } else if (conf.isSessiondAutoLogin()) {
            if (needsSessionCookieRefresh(session)) {
                String hash = session.getHash();

                // Check whether to write session cookie as well
                if (refreshSessionCookie(session.getSessionID(), LoginServlet.SESSION_PREFIX + hash, req)) {
                    LoginServlet.writeSessionCookie(resp, session, hash, req.isSecure(), req.getServerName());
                }
            }
        }
    }

    private boolean refreshSessionCookie(String sessionId, String expectedSessionCookieName, HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (null == cookies || 0 == cookies.length) {
            return false;
        }

        for (int i = cookies.length; i-- > 0;) {
            Cookie cookie = cookies[i];
            if (expectedSessionCookieName.equals(cookie.getName()) && sessionId.equals(cookie.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean needsCookieRefresh(Session session) {
        Long stamp = (Long) session.getParameter(PARAM_COOKIE_REFRESH_TIMESTAMP);
        if (null == stamp) {
            // No time stamp available, yet
            if (session instanceof PutIfAbsent) {
                ((PutIfAbsent) session).setParameterIfAbsent(PARAM_COOKIE_REFRESH_TIMESTAMP, createNewStamp());
            } else {
                session.setParameter(PARAM_COOKIE_REFRESH_TIMESTAMP, createNewStamp());
            }
            return false;
        }

        int intervalMillis = ((conf.getCookieExpiry() / 7) * 1000);
        long now = System.currentTimeMillis();
        if ((now - intervalMillis) > stamp.longValue()) {
            // Needs refresh
            synchronized (stamp) {
                Long check = (Long) session.getParameter(PARAM_COOKIE_REFRESH_TIMESTAMP);
                if (!stamp.equals(check)) {
                    // Concurrent update. Another thread already initiated cookie refresh
                    return false;
                }

                session.setParameter(PARAM_COOKIE_REFRESH_TIMESTAMP, createNewStamp());
                if (conf.isSessiondAutoLogin()) {
                    session.setParameter(PARAM_REFRESH_SESSION_COOKIE_FLAG, Boolean.TRUE);
                }
                return true;
            }
        }
        return false;
    }

    private Long createNewStamp() {
        // Explicitly use "new Long()" constructor!
        return new Long(System.currentTimeMillis());
    }

    private boolean needsSessionCookieRefresh(Session session) {
        Boolean flag = (Boolean) session.getParameter(PARAM_REFRESH_SESSION_COOKIE_FLAG);
        if ((null == flag) || (false == flag.booleanValue())) {
            return false;
        }

        session.setParameter(PARAM_REFRESH_SESSION_COOKIE_FLAG, null);
        if (session instanceof PutIfAbsent) {
            return (null == ((PutIfAbsent) session).setParameterIfAbsent(PARAM_REFRESH_SESSION_COOKIE_FLAG, Boolean.TRUE));
        }

        session.setParameter(PARAM_REFRESH_SESSION_COOKIE_FLAG, Boolean.TRUE);
        return true;
    }

}
