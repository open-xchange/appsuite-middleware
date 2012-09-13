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

package com.openexchange.login.internal;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginRequest;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.Parameterized;

/**
 * Implements {@link AddSessionParameter}.
 */
final class AddSessionParameterImpl implements AddSessionParameter, Parameterized {

    private final String username;
    private final LoginRequest request;
    private final User user;
    private final Context ctx;
    private final Map<String, Object> parameters;

    AddSessionParameterImpl(final String username, final LoginRequest request, final User user, final Context ctx) {
        super();
        parameters = new HashMap<String, Object>(4);
        this.username = username;
        this.request = request;
        this.user = user;
        this.ctx = ctx;
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
        return username;
    }

    @Override
    public String getPassword() {
        return request.getPassword();
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

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getParameter(String name) {
        try {
            return (V) parameters.get(name);
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public void setParameter(String name, Object value) {
        if (null == name) {
            parameters.remove(name);
        } else {
            parameters.put(name, value);
        }
    }

    @Override
    public Object removeParameter(String name) {
        return parameters.remove(name);
    }
}
