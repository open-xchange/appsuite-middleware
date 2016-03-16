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

package com.openexchange.share.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;

/**
 * {@link ResolvedShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ResolvedShare {

    private final AccessShareRequest shareRequest;
    private final Session session;
    private final User user;
    private final Context context;
    private final LoginConfiguration loginConfig;
    private final LoginResult loginResult;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    /**
     * Initializes a new {@link ResolvedShare}.
     *
     * @param shareRequest The share request
     * @param session The session
     * @param user The user
     * @param context The context
     * @param loginResult The login result
     * @param loginConfig The login config
     * @param request The request
     * @param response The response
     */
    public ResolvedShare(AccessShareRequest shareRequest, LoginResult loginResult, LoginConfiguration loginConfig, HttpServletRequest request, HttpServletResponse response) {
        super();
        this.shareRequest = shareRequest;
        this.session = loginResult.getSession();
        this.user = loginResult.getUser();
        this.context = loginResult.getContext();
        this.loginResult = loginResult;
        this.loginConfig = loginConfig;
        this.request = request;
        this.response = response;
    }

    /**
     * Gets the established session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the associated user
     *
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the associated user
     *
     * @return The user
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the associated login result
     *
     * @return The login result
     */
    public LoginResult getLoginResult() {
        return loginResult;
    }

    /**
     * Gets the login configuration derived from the associated share
     *
     * @return The login configuration
     */
    public LoginConfiguration getLoginConfig() {
        return loginConfig;
    }

    /**
     * Gets the associated HTTP request
     *
     * @return The HTTP request
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Gets the associated HTTP response
     *
     * @return The HTTP response
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    public AccessShareRequest getShareRequest() {
        return shareRequest;
    }

    @Override
    public String toString() {
        return "ResolvedShare [context=" + context + ", user=" + user + ", shareRequest=" + shareRequest + "]";
    }

}
