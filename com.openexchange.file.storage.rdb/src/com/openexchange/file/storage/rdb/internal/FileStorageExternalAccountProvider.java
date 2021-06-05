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

package com.openexchange.file.storage.rdb.internal;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.DefaultExternalAccount;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.file.storage.AdministrativeFileStorageAccount;
import com.openexchange.file.storage.AdministrativeFileStorageAccountStorage;

/**
 * {@link FileStorageExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class FileStorageExternalAccountProvider implements ExternalAccountProvider {

    private final AdministrativeFileStorageAccountStorage adminAccountStorage;

    /**
     * Initializes a new {@link FileStorageExternalAccountProvider}.
     * 
     * @param adminAccountStorage The {@link FileStorageExternalAccountProvider}
     */
    public FileStorageExternalAccountProvider(AdministrativeFileStorageAccountStorage adminAccountStorage) {
        super();
        this.adminAccountStorage = adminAccountStorage;
    }

    @Override
    public @NonNull ExternalAccountModule getModule() {
        return ExternalAccountModule.DRIVE;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        return parseAccounts(adminAccountStorage.getAccounts(contextId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        return parseAccounts(adminAccountStorage.getAccounts(contextId, userId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        return parseAccounts(adminAccountStorage.getAccounts(contextId, userId, providerId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        return parseAccounts(adminAccountStorage.getAccounts(contextId, providerId));
    }

    @Override
    public boolean delete(int id, int contextId, int userId) throws OXException {
        return adminAccountStorage.deleteAccount(contextId, userId, id);
    }

    @Override
    public boolean delete(int id, int contextId, int userId, Connection connection) throws OXException {
        return adminAccountStorage.deleteAccount(contextId, userId, id, connection);
    }

    /**
     * Parses the {@link AdministrativeFileStorageAccount}s to {@link ExternalAccount}s
     *
     * @param list The accounts
     * @return The parsed accounts
     */
    private List<ExternalAccount> parseAccounts(List<AdministrativeFileStorageAccount> list) {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (AdministrativeFileStorageAccount account : list) {
            accounts.add(new DefaultExternalAccount(account.getId(), account.getContextId(), account.getUserId(), account.getServiceId(), getModule()));
        }
        return accounts;
    }
}
