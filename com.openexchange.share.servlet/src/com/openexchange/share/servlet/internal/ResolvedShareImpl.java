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

package com.openexchange.share.servlet.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.share.servlet.handler.ResolvedShare;

/**
 * {@link ResolvedShareImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ResolvedShareImpl implements ResolvedShare {

    private final Share share;
    private final Session session;
    private final User user;
    private final Context context;
    private final LoginConfiguration loginConfig;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    /**
     * Initializes a new {@link ResolvedShareImpl}.
     *
     * @param share The share
     * @param loginResult The login result
     * @param loginConfig The login config
     * @param request The request
     * @param response The response
     */
    public ResolvedShareImpl(Share share, LoginResult loginResult, LoginConfiguration loginConfig, HttpServletRequest request, HttpServletResponse response) {
        this(share, loginResult.getSession(), loginResult.getUser(), loginResult.getContext(), loginConfig, request, response);
    }

    /**
     * Initializes a new {@link ResolvedShareImpl}.
     *
     * @param share The share
     * @param session The session
     * @param user The user
     * @param context The context
     * @param loginConfig The login config
     * @param request The request
     * @param response The response
     */
    public ResolvedShareImpl(Share share, Session session, User user, Context context, LoginConfiguration loginConfig, HttpServletRequest request, HttpServletResponse response) {
        super();
        this.share = share;
        this.session = session;
        this.user = user;
        this.context = context;
        this.loginConfig = loginConfig;
        this.request = request;
        this.response = response;
    }

    @Override
    public Share getShare() {
        return share;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public LoginConfiguration getLoginConfig() {
        return loginConfig;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

}