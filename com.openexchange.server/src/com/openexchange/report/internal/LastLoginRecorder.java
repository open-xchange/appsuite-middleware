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

package com.openexchange.report.internal;

import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.Client;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.user.UserService;

/**
 * {@link LastLoginRecorder} records the last login of a user in their attributes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LastLoginRecorder implements LoginHandlerService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LastLoginRecorder.class);

    private final int maxClientCount;
    private final UserService userService;
    private final Set<String> whiteList;

    /**
     * Initializes a new {@link LastLoginRecorder}.
     *
     * @param configService The configuration service to use
     * @param userService The user service to use
     */
    public LastLoginRecorder(ConfigurationService configService, UserService userService) {
        super();
        this.userService = userService;
        maxClientCount = configService.getIntProperty("com.openexchange.user.maxClientCount", -1);

        ImmutableSet.Builder<String> whiteList = ImmutableSet.builder();
        for (Interface iface : Interface.values()) {
            whiteList.add(iface.toString());
        }

        String[] knownClients = {
            Client.OX6_UI.getClientId(),                   // OX6 frontend
            Client.APPSUITE_UI.getClientId(),              // AppSuite frontend
            Client.MOBILE_APP.getClientId(),               // Mobile Web Interface
            Client.OUTLOOK_OXTENDER2_ADDIN.getClientId(),  // Outlook OXtender2 AddIn
            Client.OXNOTIFIER.getClientId(),               // OXNotifier
            Client.OUTLOOK_UPDATER1.getClientId(),         // Outlook Updater 1
            Client.OUTLOOK_UPDATER2.getClientId()          // Outlook Updater 2
        };
        for (String knownClient : knownClients) {
            whiteList.add(knownClient);
        }
        this.whiteList = whiteList.build();
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        final LoginRequest request = login.getRequest();

        // Determine client
        String client = request.getClient();
        if (null == client) {
            Interface interfaze = request.getInterface();
            client = null == interfaze ? null : interfaze.toString();
        }
        if (null == client) {
            return;
        }

        Context context = login.getContext();
        User user = login.getUser();
        if (maxClientCount > 0 && !isWhitelistedClient(client)) {
            int count = 0;
            for (String origKey : user.getAttributes().keySet()) {
                if (origKey.startsWith("client:") && ++count > maxClientCount) {
                    LOG.warn("Login of client {} for login {} (Context: {}, User: {}) will not be recorded in the database.", client, login, Integer.valueOf(context.getContextId()), Integer.valueOf(user.getId()));
                    return;
                }
            }
        }
        updateLastLogin(userService, client, user, context);
    }

    /**
     * Updates the last-accessed time stamp for given user's client.
     *
     * @param userService UserService to update the user attributes.
     * @param client The client identifier
     * @param origUser The associated user
     * @param context The context
     * @throws OXException If update fails for any reason
     */
    static void updateLastLogin(UserService userService, String client, User origUser, Context context) throws OXException {
        if (context.isReadOnly()) {
            return;
        }

        // Set attribute and add current time stamp
        try {
            userService.setAttribute(null, "client:" + client, String.valueOf(System.currentTimeMillis()), origUser.getId(), context, false);
        } catch (OXException e) {
            if (!UserExceptionCode.CONCURRENT_ATTRIBUTES_UPDATE.equals(e)) {
                throw e;
            }
            // Ignore. Another thread updated in the meantime
        }
    }

    private boolean isWhitelistedClient(String client) {
        return null != client && whiteList.contains(client);
    }

    @Override
    public void handleLogout(final LoginResult logout) {
        // Nothing to to.
    }

}
