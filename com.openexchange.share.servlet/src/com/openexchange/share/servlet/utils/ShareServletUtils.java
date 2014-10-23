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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.share.servlet.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.share.ShareList;
import com.openexchange.share.servlet.auth.ShareLoginMethod;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link ShareServletUtils} - Utilities for Share Servlet layer.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class ShareServletUtils {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareServletUtils.class);

    /**
     * Initializes a new {@link ShareServletUtils}.
     */
    private ShareServletUtils() {
        super();
    }

    /**
     * Authenticates the request to the share and performs a guest login, sending an appropriate HTTP response in case of unauthorized
     * access.
     *
     * @param share The share
     * @param request The request
     * @param response The response
     * @param loginConfig The login configuration to use
     * @param tranzient <code>true</code> to mark the session as transient, <code>false</code>, otherwise
     * @return The login result, or <code>null</code> if not successful
     */
    public static LoginResult login(ShareList share, HttpServletRequest request, HttpServletResponse response, LoginConfiguration loginConfig, boolean tranzient) throws OXException, IOException {
        /*
         * parse login request
         */
        Context context = ShareServiceLookup.getService(UserService.class, true).getContext(share.getContextID());
        User user = ShareServiceLookup.getService(UserService.class, true).getUser(share.getGuest(), context);
        LoginRequestImpl loginRequest = LoginTools.parseLogin(request, user.getMail(), user.getUserPassword(), false,
            loginConfig.getDefaultClient(), loginConfig.isCookieForceHTTPS(), false);
        loginRequest.setTransient(tranzient);
        /*
         * login
         */
        ShareLoginMethod loginMethod = new ShareLoginMethod(share, context, user);
        Map<String, Object> properties = new HashMap<String, Object>();
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, properties, loginMethod);
        if (null == loginResult || null == loginResult.getSession()) {
            loginMethod.sendUnauthorized(request, response);
            return null;
        }
        LOG.debug("Successful login for share {} with guest user {} in context {}.", share.getToken(), share.getGuest(), share.getContextID());
        return loginResult;
    }

    /**
     * Performs a logout for the supplied session (if not <code>null</code>).
     *
     * @param session The session
     * @throws OXException
     */
    public static void logout(Session session) throws OXException {
        if (null != session) {
            LoginPerformer.getInstance().doLogout(session.getSessionID());
        }
    }

    private static final Pattern PATH_PATTERN = Pattern.compile("/+([a-f0-9]{32})(?:/+items(?:/+([0-9]+))?)?/*", Pattern.CASE_INSENSITIVE);

    /**
     * Extracts the token from a HTTP request's path info.
     *
     * @param pathInfo The path info
     * @return The token, or <code>null</code> if no token could be extracted
     */
    public static String extractToken(String pathInfo) {
        if (Strings.isEmpty(pathInfo)) {
            return null;
        }
        Matcher matcher = PATH_PATTERN.matcher(pathInfo);
        return matcher.matches() ? matcher.group(1) : null;
    }

    /**
     * Splits the supplied path by the separator char <code>/</code> into their components. Empty components are removed implicitly.
     *
     * @param pathInfo the path info to split
     * @return The splitted path
     */
    public static String[] splitPath(String pathInfo) {
        List<String> paths = Strings.splitAndTrim(pathInfo, "/");
        Iterator<String> iterator = paths.iterator();
        while (iterator.hasNext()) {
            if (Strings.isEmpty(iterator.next())) {
                iterator.remove();
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

}
