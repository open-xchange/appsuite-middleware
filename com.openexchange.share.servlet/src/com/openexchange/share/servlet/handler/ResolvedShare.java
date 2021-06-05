/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.user.User;

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
