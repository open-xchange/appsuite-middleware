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

package com.openexchange.file.storage.webdav;

import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.webdav.exception.WebdavExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link CapabilityAwareAccountManager} - a {@link FileStorageAccountManager} which checks if the user has the appropriate capability to add or update an account.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class CapabilityAwareAccountManager implements FileStorageAccountManager {

    private final String cap;
    private final String id;
    private final CapabilityService capService;
    private final FileStorageAccountManager delegate;
    
    /**
     * Initializes a new {@link CapabilityAwareAccountManager}.
     * 
     * @param cap The capability to check
     * @param id The id of the file storage
     * @param capService The {@link CapabilityService}
     * @param delegate The {@link FileStorageAccountManager} which performs the operations if allowed.
     */
    public CapabilityAwareAccountManager(@NonNull String cap, @NonNull String id, @NonNull CapabilityService capService, @NonNull FileStorageAccountManager delegate) {
        super();
        this.cap = cap;
        this.capService = capService;
        this.id = id;
        this.delegate = delegate;
    }
    
    private void checkCapability(Session session) throws OXException {
        if (false == capService.getCapabilities(session).contains(cap)) {
            throw WebdavExceptionCodes.MISSING_CAPABILITY.create(id);
        }
    }
    
    @Override
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        checkCapability(session);
        return delegate.addAccount(account, session);
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        checkCapability(session);
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
