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

package com.openexchange.oauth.impl.internal;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.DefaultExternalAccount;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.oauth.AdministrativeOAuthAccount;
import com.openexchange.oauth.AdministrativeOAuthAccountStorage;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OAuthExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class OAuthExternalAccountProvider implements ExternalAccountProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link OAuthExternalAccountProvider}.
     * 
     * @param services The service lookup-up instance
     */
    public OAuthExternalAccountProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public @NonNull ExternalAccountModule getModule() {
        return ExternalAccountModule.OAUTH;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId, userId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId, userId, providerId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId, providerId));
    }

    @Override
    public boolean delete(int id, int contextId, int userId) throws OXException {
        return getStorage().deleteAccount(contextId, userId, id);
    }

    @Override
    public boolean delete(int id, int contextId, int userId, Connection connection) throws OXException {
        return getStorage().deleteAccount(contextId, userId, id, connection);
    }

    /**
     * Parses the specified oauth accounts to external accounts
     *
     * @param list the list of the accounts to parse
     * @return The external accounts
     */
    private List<ExternalAccount> parseAccounts(List<AdministrativeOAuthAccount> list) {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (AdministrativeOAuthAccount account : list) {
            accounts.add(new DefaultExternalAccount(account.getId(), account.getContextId(), account.getUserId(), account.getServiceId(), getModule()));
        }
        return accounts;
    }

    /**
     * Returns the {@link AdministrativeOAuthAccountStorage}
     *
     * @return the {@link AdministrativeOAuthAccountStorage}
     * @throws OXException if the service is absent
     */
    private AdministrativeOAuthAccountStorage getStorage() throws OXException {
        return services.getServiceSafe(AdministrativeOAuthAccountStorage.class);
    }
}
