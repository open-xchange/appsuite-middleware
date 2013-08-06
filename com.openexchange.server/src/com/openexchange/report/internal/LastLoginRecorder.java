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

package com.openexchange.report.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.UserService;

/**
 * {@link LastLoginRecorder} records the last login of a user in its user attributes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LastLoginRecorder implements LoginHandlerService {

    private static volatile Integer maxClientCount;
    private static int maxClientCount() {
        Integer tmp = maxClientCount;
        if (null == tmp) {
            synchronized (LastLoginRecorder.class) {
                tmp = maxClientCount;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? -1 : service.getIntProperty("com.openexchange.user.maxClientCount", -1));
                    maxClientCount = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * Initializes a new {@link LastLoginRecorder}.
     */
    public LastLoginRecorder() {
        super();
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        final LoginRequest request = login.getRequest();
        // Determine client
        String client;
        if (null != request.getClient()) {
            client = request.getClient();
        } else if (null != request.getInterface()) {
            client = request.getInterface().toString();
        } else {
            return;
        }
        updateLastLogin(client, login.getUser(), login.getContext());
    }

    /**
     * Updates the last-accessed time stamp for given user's client.
     *
     * @param client The client identifier
     * @param origUser The associated user
     * @param context The context
     * @throws OXException If update fails for any reason
     */
    static void updateLastLogin(final String client, final User origUser, final Context context) throws OXException {
        // Set attribute
        final String key = "client:" + client;
        if (context.isReadOnly()) {
            return;
        }
        // Retrieve existing ones
        final Map<String, Set<String>> attributes;
        {
            final int maxClientCount = maxClientCount();
            if (maxClientCount > 0) {
                final Map<String, Set<String>> origAttributes = origUser.getAttributes();
                int count = 0;
                for (final String origKey : origAttributes.keySet()) {
                    if (origKey.startsWith("client:") && ++count > maxClientCount) {
                        throw UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.create(Integer.valueOf(context.getContextId()), Integer.valueOf(origUser.getId()));
                    }
                }
                attributes = new HashMap<String, Set<String>>(origAttributes);
            } else {
                attributes = new HashMap<String, Set<String>>(origUser.getAttributes());
            }
        }
        // Add current time stamp
        attributes.put(key, new HashSet<String>(Arrays.asList(Long.toString(System.currentTimeMillis()))));
        final UserImpl newUser = new UserImpl();
        newUser.setId(origUser.getId());
        newUser.setAttributes(attributes);
        UserService service;
        try {
            service = ServerServiceRegistry.getInstance().getService(UserService.class, true);
            service.updateUser(newUser, context);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void handleLogout(final LoginResult logout) {
        // Nothing to to.
    }
}
