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

package com.openexchange.report.internal;

import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.Client;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Streams;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.User;
import com.openexchange.user.UserExceptionCode;
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

        String[] knownClients = { Client.OX6_UI.getClientId(),                   // OX6 frontend
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

        LockService lockService = ServerServiceRegistry.getInstance().getService(LockService.class);
        if (null == lockService) {
            doUpdateLastLogin(userService, client, origUser, context);
            return;
        }

        boolean acquired = false;
        AccessControl accessControl = lockService.getAccessControlFor("lastloginrecorder", 1, origUser.getId(), context.getContextId());
        try {
            acquired = accessControl.tryAcquireGrant();
            if (false == acquired) {
                // Release manually and null'ify
                accessControl.release(false);
                accessControl = null;
            } else {
                // Grant acquired...
                doUpdateLastLogin(userService, client, origUser, context);
            }
        } finally {
            Streams.close(accessControl);
        }
    }

    private static void doUpdateLastLogin(UserService userService, String client, User origUser, Context context) throws OXException {
        // Set attribute and add current time stamp
        try {
            userService.setAttribute(null, new StringBuilder("client:").append(client).toString(), Long.toString(System.currentTimeMillis()), origUser.getId(), context, false);
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
