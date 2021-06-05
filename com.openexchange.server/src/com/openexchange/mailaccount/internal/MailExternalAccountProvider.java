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

package com.openexchange.mailaccount.internal;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.InternetDomainName;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.DefaultExternalAccount;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.java.Strings;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthAccountStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class MailExternalAccountProvider implements ExternalAccountProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailExternalAccountProvider}.
     * 
     * @param services The service lookup-up instance
     */
    public MailExternalAccountProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public @NonNull ExternalAccountModule getModule() {
        return ExternalAccountModule.MAIL;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(contextId)) {
            addMailAccount(contextId, mailAccount, list);
        }
        return list;
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(contextId)) {
            addMailAccount(contextId, providerId, mailAccount, list);
        }
        return list;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(userId, contextId)) {
            addMailAccount(contextId, mailAccount, list);
        }
        return list;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(userId, contextId)) {
            addMailAccount(contextId, providerId, mailAccount, list);
        }
        return list;
    }

    @Override
    public boolean delete(int id, int contextId, int userId) throws OXException {
        return getMailAccountStorageService().deleteMailAccount(id, ImmutableMap.of(), userId, contextId);
    }

    @Override
    public boolean delete(int id, int contextId, int userId, Connection connection) throws OXException {
        return getMailAccountStorageService().deleteMailAccount(id, ImmutableMap.of(), userId, contextId, false, connection);
    }

    ////////////////////////////////////////////// HELPERS ////////////////////////////////////////

    /**
     * Converts the specified mail account to an {@link ExternalAccount} and adds it to the list
     * 
     * @param mailAccount the {@link MailAccount} to convert and add
     * @param list The list to add it to
     * @throws OXException if an error is occurred
     */
    private void addMailAccount(int contextId, MailAccount mailAccount, List<ExternalAccount> list) throws OXException {
        addMailAccount(contextId, null, mailAccount, list);
    }

    /**
     * Converts the specified mail account to an {@link ExternalAccount} and adds it to the list
     * 
     * @param mailAccount the {@link MailAccount} to convert and add
     * @param providerId The provider identifier
     * @param list The list to add it to
     * @throws OXException if an error is occurred
     */
    private void addMailAccount(int contextId, String providerId, MailAccount mailAccount, List<ExternalAccount> list) throws OXException {
        int unifiedId = getUnifiedInboxManagement().getUnifiedINBOXAccountID(mailAccount.getUserId(), contextId);
        if (mailAccount.getId() == 0 || mailAccount.getId() == unifiedId) {
            // Skip primary account and unified inbox
            return;
        }
        String pid = extractProviderId(mailAccount, contextId);
        if (Strings.isNotEmpty(providerId) && false == pid.equals(providerId)) {
            return;
        }
        list.add(new DefaultExternalAccount(mailAccount.getId(), contextId, mailAccount.getUserId(), pid, ExternalAccountModule.MAIL));
    }

    /**
     * Extracts the provider identifier.
     * 
     * @param mailAccount The mail account from which to extract the account identifier
     * @return The account identifier which is the account's configured mail server, either
     *         as an IP address or as the top domain.
     * @throws OXException
     */
    private String extractProviderId(MailAccount mailAccount, int contextId) throws OXException {
        if (mailAccount.getMailOAuthId() > 0) {
            OAuthAccount account = getOAuthAccountStorage().getAccount(contextId, mailAccount.getUserId(), mailAccount.getMailOAuthId());
            return account.getAPI().getServiceId();
        }
        String address = mailAccount.getMailServer();
        if (InetAddressUtils.isIPv4Address(address) || InetAddressUtils.isIPv6Address(address)) {
            return address;
        }
        return InternetDomainName.from(address).topDomainUnderRegistrySuffix().toString();
    }

    /**
     * Returns the {@link MailAccountStorageService}
     *
     * @return the {@link MailAccountStorageService}
     * @throws OXException if the service is absent
     */
    private MailAccountStorageService getMailAccountStorageService() throws OXException {
        return ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
    }

    /**
     * Returns the {@link OAuthAccountStorage}
     *
     * @return the {@link OAuthAccountStorage}
     * @throws OXException if the service is absent
     */
    private OAuthAccountStorage getOAuthAccountStorage() throws OXException {
        return services.getServiceSafe(OAuthAccountStorage.class);
    }

    private UnifiedInboxManagement getUnifiedInboxManagement() {
        return ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);

    }
}
