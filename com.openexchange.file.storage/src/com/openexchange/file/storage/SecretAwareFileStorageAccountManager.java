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

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.secret.SecretExceptionCodes;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;


/**
 * {@link SecretAwareFileStorageAccountManager} - An account manager that ensures a non-empty secret string when serving a
 * {@link #getAccounts(Session)} call.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SecretAwareFileStorageAccountManager implements FileStorageAccountManager {

    /**
     * Gets a new {@code SecretAwareFileStorageAccountManager} instance.
     *
     * @param manager The backing account manager
     * @return The secret-aware account manager or <code>null</code>
     */
    public static SecretAwareFileStorageAccountManager newInstanceFor(FileStorageAccountManager manager) {
        return null == manager ? null : new SecretAwareFileStorageAccountManager(manager);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    /** The delegate */
    private final FileStorageAccountManager manager;

    /**
     * Initializes a new {@link SecretAwareFileStorageAccountManager}.
     */
    protected SecretAwareFileStorageAccountManager(FileStorageAccountManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        return manager.addAccount(account, session);
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        manager.updateAccount(account, session);
    }

    @Override
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        manager.deleteAccount(account, session);
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        SecretService secretService = Services.getOptionalService(SecretService.class);
        if (null != secretService && Strings.isEmpty(secretService.getSecret(session))) {
            // The OAuth-based file storage needs a valid secret string for operation
            return Collections.emptyList();
        }
        try {
            return manager.getAccounts(session);
        } catch (OXException e) {
            if (!SecretExceptionCodes.EMPTY_SECRET.equals(e)) {
                throw e;
            }
            // The OAuth-based file storage needs a valid secret string for operation
            return Collections.emptyList();
        }
    }

    @Override
    public FileStorageAccount getAccount(String id, Session session) throws OXException {
        return manager.getAccount(id, session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        manager.cleanUp(secret, session);
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        manager.removeUnrecoverableItems(secret, session);
    }

    @Override
    public void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException {
        manager.migrateToNewSecret(oldSecret, newSecret, session);
    }

    @Override
    public boolean hasEncryptedItems(Session session) throws OXException {
        return manager.hasEncryptedItems(session);
    }

}
