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

package com.openexchange.file.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.rdb.internal.CachingFileStorageAccountStorage;
import com.openexchange.session.Session;

/**
 * {@link RdbFileStorageAccountManager} - The default file storage account manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class RdbFileStorageAccountManager implements FileStorageAccountManager {

    /**
     * The file storage account storage cache.
     */
    private static final CachingFileStorageAccountStorage CACHE = CachingFileStorageAccountStorage.getInstance();

    /**
     * The identifier of associated file storage service.
     */
    private final String serviceId;

    /**
     * The file storage service.
     */
    private final FileStorageService service;

    /**
     * Initializes a new {@link RdbFileStorageAccountManager}.
     *
     * @param service The file storage service
     */
    public RdbFileStorageAccountManager(final FileStorageService service) {
        super();
        serviceId = service.getId();
        this.service = service;
    }

    /**
     * Gets the appropriate file storage account manager for specified account identifier and session.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The file storage account manager or <code>null</code>
     * @throws OXException If retrieval fails
     */
    public static RdbFileStorageAccountManager getAccountById(final String accountId, final Session session) throws OXException {
        try {
            final int id = Integer.parseInt(accountId);
            if (id < 0) {
                // Unsupported account identifier
                return null;
            }
            final FileStorageAccount account = CACHE.getAccount(id, session);
            return null == account ? null : new RdbFileStorageAccountManager(account.getFileStorageService());
        } catch (NumberFormatException e) {
            // Unsupported account identifier
            return null;
        }
    }

    @Override
    public FileStorageAccount getAccount(final String id, final Session session) throws OXException {
        try {
            return CACHE.getAccount(serviceId, Integer.parseInt(id), session);
        } catch (NumberFormatException e) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(id, serviceId, I(session.getUserId()), I(session.getContextId()));
        }
    }

    @Override
    public List<FileStorageAccount> getAccounts(final Session session) throws OXException {
        return CACHE.getAccounts(serviceId, session);
    }

    @Override
    public String addAccount(final FileStorageAccount account, final Session session) throws OXException {
        return String.valueOf(CACHE.addAccount(serviceId, account, session));
    }

    @Override
    public void deleteAccount(final FileStorageAccount account, final Session session) throws OXException {
        CACHE.deleteAccount(serviceId, account, session);
    }

    @Override
    public void updateAccount(final FileStorageAccount account, final Session session) throws OXException {
        CACHE.updateAccount(serviceId, account, session);
    }

    @Override
    public boolean hasEncryptedItems(final Session session) throws OXException {
        return CACHE.hasEncryptedItems(service, session);
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        CACHE.migrateToNewSecret(service, oldSecret, newSecret, session);
    }

    @Override
    public void cleanUp(final String secret, final Session session) throws OXException {
        CACHE.cleanUp(service, secret, session);
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        CACHE.removeUnrecoverableItems(service, secret, session);
    }

}
