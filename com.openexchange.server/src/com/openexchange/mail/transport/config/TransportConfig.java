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

package com.openexchange.mail.transport.config;

import static com.openexchange.session.Sessions.isGuest;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.UrlInfo;
import com.openexchange.mail.config.ConfiguredServer;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.MailAccounts;
import com.openexchange.mailaccount.TransportAccount;
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
     * @throws OXException If user-specific transport configuration cannot be determined
     */
    public static final <C extends TransportConfig> C getTransportConfig(C transportConfig, Session session, int accountId) throws OXException {
        /*
         * Fetch mail account
         */
        int userId = session.getUserId();
        int contextId = session.getContextId();
        TransportAccount transportAccount = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true).getTransportAccount(accountId, userId, contextId);
        transportConfig.account = transportAccount;
        transportConfig.accountId = accountId;
        transportConfig.session = session;
        fillLoginAndPassword(transportConfig, session, getUser(session).getLoginInfo(), transportAccount, false);

        UrlInfo urlInfo = TransportConfig.getTransportServerURL(transportAccount, userId, contextId);
        String serverURL = urlInfo.getServerURL();
        if (serverURL == null) {
            if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getTransportServerSource(userId, contextId, isGuest(session)))) {
                throw MailConfigException.create(new StringBuilder(128).append("Property \"").append("com.openexchange.mail.transportServer").append("\" not set in mail properties").toString());
            }
            throw MailConfigException.create(new StringBuilder(128).append("Cannot determine transport server URL for user ").append(userId).append(" in context ").append(contextId).toString());
        }

        transportConfig.parseServerURL(urlInfo);
        transportConfig.doCustomParsing(transportAccount, session);
        return transportConfig;
    }

    /**
     * Gets the transport server URL appropriate to configured transport server source.
     *
     * @param transportAccount The mail account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The appropriate transport server URL or <code>null</code>
     * @throws OXException If URL information cannot be returned
     */
    public static UrlInfo getTransportServerURL(TransportAccount transportAccount, int userId, int contextId) throws OXException {
        if (!transportAccount.isDefaultAccount()) {
            return new UrlInfo(transportAccount.generateTransportServerURL(), transportAccount.isTransportStartTls());
        }
        if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getTransportServerSource(userId, contextId, MailAccounts.isGuestAccount(transportAccount)))) {
            ConfiguredServer server = MailProperties.getInstance().getTransportServer(userId, contextId);
            if (server == null) {
                throw MailConfigException.create(new StringBuilder(128).append("Property \"").append("com.openexchange.mail.transportServer").append("\" not set in mail properties").toString());
            }
            return new UrlInfo(server.getUrlString(true), MailProperties.getInstance().isTransportStartTls(userId, contextId));
        }
        return new UrlInfo(transportAccount.generateTransportServerURL(), transportAccount.isTransportStartTls());
    }

    /**
     * Gets the transport server URL appropriate to configured login type
     *
     * @param session The user session
     * @param accountId The account ID
     * @return The appropriate transport server URL or <code>null</code>
     * @throws OXException If transport server URL cannot be returned
     */
    public static UrlInfo getTransportServerURL(Session session, int accountId) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        if (MailAccount.DEFAULT_ID == accountId && ServerSource.GLOBAL.equals(MailProperties.getInstance().getTransportServerSource(userId, contextId, isGuest(session)))) {
            ConfiguredServer server = MailProperties.getInstance().getTransportServer(userId, contextId);
            if (server == null) {

                throw MailConfigException.create(new StringBuilder(128).append("Property \"").append("com.openexchange.mail.transportServer").append("\" not set in mail properties").toString());
            }
            return new UrlInfo(server.getUrlString(true), MailProperties.getInstance().isTransportStartTls(userId, contextId));
        }

        MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        return getTransportServerURL(storage.getTransportAccount(accountId, session.getUserId(), session.getContextId()), session.getUserId(), session.getContextId());
    }

    @Override
    public IMailProperties getMailProperties() {
        return null;
    }

    @Override
    public void setMailProperties(IMailProperties mailProperties) {
        // Nothing to do
    }

    /**
     * Gets the transport properties for this transport configuration.
     *
     * @return The transport properties for this transport configuration
     */
    public abstract ITransportProperties getTransportProperties();

    /**
     * Sets the transport properties for this transport configuration.
     *
     * @param transportProperties The transport properties for this transport configuration
     */
    public abstract void setTransportProperties(ITransportProperties transportProperties);
}
