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

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
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

    private int maxClientCount = -1;
    private final UserService userService;

    public LastLoginRecorder(ConfigurationService confService, UserService userService) {
        super();
        this.userService = userService;
        readConfiguration(confService);
    }

    private void readConfiguration(ConfigurationService confService) {
        maxClientCount = confService.getIntProperty("com.openexchange.user.maxClientCount", -1);
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
        if (!isWhitelistedClient(client) && maxClientCount > 0) {
            int count = 0;
            for (String origKey : user.getAttributes().keySet()) {
                if (origKey.startsWith("client:") && ++count > maxClientCount) {
                    LOG.warn("Login of client {} for login {} (Context: {}, User: {}) will not be recorded in the database.", client, login, context.getContextId(), user.getId());
                }
            }
        }
        updateLastLogin(userService, client, user, context);
    }

    /**
     * Updates the last-accessed time stamp for given user's client.
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
        userService.setAttribute(null, "client:" + client, String.valueOf(System.currentTimeMillis()), origUser.getId(), context, false);
    }

    private static boolean isWhitelistedClient(String client) {
        for (Interface iface : Interface.values()) {
            if (iface.toString().equals(client)) {
                return true;
            }
        }
        for (String known : KNOWN_CLIENTS) {
            if (known.equals(client)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleLogout(final LoginResult logout) {
        // Nothing to to.
    }

    private static final String[] KNOWN_CLIENTS = {
        "com.openexchange.ox.gui.dhtml",     // OX6 frontend
        "open-xchange-appsuite",             // AppSuite frontend
        "com.openexchange.mobileapp",        // Mobile Web Interface
        "OpenXchange.HTTPClient.OXAddIn",    // Outlook OXtender2 AddIn
        "OpenXchange.HTTPClient.OXNotifier", // OXNotifier
        "com.open-xchange.updater.olox1",    // Outlook Updater 1
        "com.open-xchange.updater.olox2"     // Outlook Updater 2
    };
}
