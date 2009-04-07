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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.transport.config;

import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link TransportConfig} - The user-specific transport configuration
 * <p>
 * Provides access to global transport properties.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class TransportConfig extends MailConfig {

    /**
     * Default constructor
     */
    protected TransportConfig() {
        super();
    }

    /**
     * Gets the user-specific transport configuration
     * 
     * @param clazz The transport configuration type
     * @param transportConfig A newly created {@link TransportConfig transport configuration}
     * @param session The session providing needed user data
     * @param accountId The mail account ID
     * @return The user-specific transport configuration
     * @throws MailException If user-specific transport configuration cannot be determined
     */
    public static final <C extends TransportConfig> C getTransportConfig(final Class<? extends C> clazz, final C transportConfig, final Session session, final int accountId) throws MailException {
        /*
         * Fetch mail account
         */
        final MailAccount mailAccount;
        try {
            final MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            if (accountId == MailAccount.DEFAULT_ID) {
                mailAccount = storage.getDefaultMailAccount(session.getUserId(), session.getContextId());
            } else {
                mailAccount = storage.getMailAccount(accountId, session.getUserId(), session.getContextId());
            }
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final MailAccountException e) {
            throw new MailException(e);
        }
        transportConfig.accountId = accountId;
        fillLoginAndPassword(
            transportConfig,
            session.getPassword(),
            UserStorage.getStorageUser(session.getUserId(), session.getContextId()).getLoginInfo(),
            mailAccount);
        String serverURL = TransportConfig.getTransportServerURL(mailAccount);
        if (serverURL == null) {
            if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getTransportServerSource())) {
                throw new MailConfigException(
                    new StringBuilder(128).append("Property \"").append("com.openexchange.mail.transportServer").append(
                        "\" not set in mail properties").toString());
            }
            throw new MailConfigException(new StringBuilder(128).append("Cannot determine transport server URL for user ").append(
                session.getUserId()).append(" in context ").append(session.getContextId()).toString());
        }
        {
            /*
             * Remove ending '/' character
             */
            final int lastPos = serverURL.length() - 1;
            if (serverURL.charAt(lastPos) == '/') {
                serverURL = serverURL.substring(0, lastPos);
            }
        }
        transportConfig.parseServerURL(serverURL);
        return transportConfig;
    }

    /**
     * Gets the transport server URL appropriate to to configured transport server source.
     * 
     * @param mailAccount The mail account
     * @return The appropriate transport server URL or <code>null</code>
     */
    public static String getTransportServerURL(final MailAccount mailAccount) {
        if (!mailAccount.isDefaultAccount()) {
            return mailAccount.getTransportServerURL();
        }
        if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getTransportServerSource())) {
            return MailProperties.getInstance().getTransportServer();
        }
        return mailAccount.getTransportServerURL();
    }

    /**
     * Gets the transport server URL appropriate to configured login type
     * 
     * @param session The user session
     * @param accountId The account ID
     * @return The appropriate transport server URL or <code>null</code>
     * @throws MailException If transport server URL cannot be returned
     */
    public static String getTransportServerURL(final Session session, final int accountId) throws MailException {
        try {
            final MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            return getTransportServerURL(storage.getMailAccount(accountId, session.getUserId(), session.getContextId()));
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final MailAccountException e) {
            throw new MailException(e);
        }
    }

}
