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

package com.openexchange.file.storage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.openexchange.exception.OXException;
import com.openexchange.java.Functions.OXFunction;
import com.openexchange.session.Session;

/**
 * {@link SizeLimitedFileStorageAccountManager} - limits the amount of accounts a user can create for a {@link FileStorageService}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class SizeLimitedFileStorageAccountManager implements FileStorageAccountManager {

    private final OXFunction<Session, Integer, OXException> getMaxAccountsCallback;
    private final FileStorageAccountManager delegate;
    private final String serviceId;

    /**
     * Initializes a new {@link SizeLimitedFileStorageAccountManager}.
     *
     * @param serviceId The ID of the {@link FileStorageService} to limit
     * @param getMaxAccountsCallback The maximum of allowed accounts
     * @param delegate The delegate
     */
    public SizeLimitedFileStorageAccountManager(String serviceId, OXFunction<Session, Integer, OXException> getMaxAccountsCallback, FileStorageAccountManager delegate) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.getMaxAccountsCallback= Objects.requireNonNull(getMaxAccountsCallback, "getMaxAccountsCallback must not be null");
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    /**
     * Gets only accounts matching the given service identifier
     *
     * @param session The session
     * @param serviceId The identifier
     * @return The accounts with the given identifier
     * @throws OXException
     */
    private List<FileStorageAccount> getAccountsByService(Session session, String serviceId) throws OXException {
        return getAccounts(session).stream().filter(a -> a.getFileStorageService().getId().equals(serviceId)).collect(Collectors.toList());
    }

    @Override
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        int maxAllowedAccounts = i(getMaxAccountsCallback.apply(session));
        List<FileStorageAccount> existingAccounts = getAccountsByService(session, serviceId);
        if (maxAllowedAccounts == 0 || existingAccounts.size() < maxAllowedAccounts) {
            return delegate.addAccount(account, session);
        }
        throw FileStorageExceptionCodes.MAX_ACCOUNTS_EXCEEDED.create(I(maxAllowedAccounts), serviceId);
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        delegate.updateAccount(account, session);
    }

    @Override
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        delegate.deleteAccount(account, session);
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return delegate.getAccounts(session);
    }

    @Override
    public FileStorageAccount getAccount(String id, Session session) throws OXException {
        return delegate.getAccount(id, session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        delegate.cleanUp(secret, session);
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        delegate.removeUnrecoverableItems(secret, session);
    }

    @Override
    public void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException {
        delegate.migrateToNewSecret(oldSecret, newSecret, session);
    }

    @Override
    public boolean hasEncryptedItems(Session session) throws OXException {
        return delegate.hasEncryptedItems(session);
    }
}
