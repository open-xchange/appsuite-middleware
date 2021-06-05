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

package com.openexchange.file.storage.mail;

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.session.Session;


/**
 * {@link MailDriveFileStorageAccountManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailDriveFileStorageAccountManager implements FileStorageAccountManager {

    private static final Object DEFAULT_ID = MailDriveConstants.ACCOUNT_ID;

    private final MailDriveFileStorageService service;

    /**
     * Initializes a new {@link MailDriveFileStorageAccountManager}.
     */
    public MailDriveFileStorageAccountManager(MailDriveFileStorageService service) {
        super();
        this.service = service;
    }

    @Override
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        return "";
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        // Nope
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return service.hasMailDriveAccess(session) ? Arrays.<FileStorageAccount> asList(new MailDriveFileStorageAccount(service, session)) : Collections.<FileStorageAccount> emptyList();
    }

    @Override
    public FileStorageAccount getAccount(String id, Session session) throws OXException {
        if (DEFAULT_ID.equals(id)) {
            return new MailDriveFileStorageAccount(service, session);
        }
        throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(id, MailDriveConstants.ID, I(session.getUserId()), I(session.getContextId()));
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean hasEncryptedItems(Session session) throws OXException {
        return false;
    }

}
