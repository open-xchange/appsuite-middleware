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

package com.openexchange.login.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.session.Origin;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.user.User;

/**
 * Implements {@link AddSessionParameter}.
 */
public final class AddSessionParameterImpl implements AddSessionParameter {

    private final String userName;
    private final LoginRequest request;
    private final User user;
    private final Context ctx;
    private final List<SessionEnhancement> enhancements;
    private final String passwordOverride;

    /**
     * Initializes a new {@link AddSessionParameterImpl}.
     *
     * @param userName The user name
     * @param request The associated login request
     * @param user The resolved user
     * @param ctx The resolved context
     */
    public AddSessionParameterImpl(final String userName, final LoginRequest request, final User user, final Context ctx) {
        this(userName, request, user, ctx, null);
    }

    /**
     * Initializes a new {@link AddSessionParameterImpl}.
     *
     * @param userName The user name
     * @param request The associated login request
     * @param user The resolved user
     * @param ctx The resolved context
     * @param passwordOverride Override the login password with new value, or <code>null</code> if not applicable
     */
    public AddSessionParameterImpl(final String userName, final LoginRequest request, final User user, final Context ctx, String passwordOverride) {
        super();
        this.userName = userName;
        this.request = request;
        this.user = user;
        this.ctx = ctx;
        this.enhancements = Collections.synchronizedList(new ArrayList<SessionEnhancement>());
        this.passwordOverride = passwordOverride;
    }

    @Override
    public Origin getOrigin() {
        return Interface.originFor(request.getInterface());
    }

    @Override
    public String getClientIP() {
        return request.getClientIP();
    }

    @Override
    public Context getContext() {
        return ctx;
    }

    @Override
    public String getFullLogin() {
        return request.getLogin();
    }

    @Override
    public String getUserLoginInfo() {
        return userName;
    }

    @Override
    public String getPassword() {
        return null != passwordOverride ? passwordOverride : request.getPassword();
    }

    @Override
    public int getUserId() {
        return user.getId();
    }

    @Override
    public String getAuthId() {
        return request.getAuthId();
    }

    @Override
    public String getHash() {
        return request.getHash();
    }

    @Override
    public String getClient() {
        return request.getClient();
    }

    @Override
    public String getClientToken() {
        return request.getClientToken();
    }

    @Override
    public boolean isTransient() {
        return request.isTransient();
    }

    @Override
    public boolean isStaySignedIn() {
        return request.isStaySignedIn();
    }

    @Override
    public List<SessionEnhancement> getEnhancements() {
        return enhancements;
    }

    /**
     * Add a sessionEnhancement
     *
     * @param enhancement
     */
    public void addEnhancement(SessionEnhancement enhancement) {
        enhancements.add(enhancement);
    }

    @Override
    public String getUserAgent() {
        return request.getUserAgent();
    }
}
