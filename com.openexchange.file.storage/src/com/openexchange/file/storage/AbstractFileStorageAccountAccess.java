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

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AbstractFileStorageAccountAccess} - The abstract account access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFileStorageAccountAccess implements FileStorageAccountAccess {

    protected boolean connected = false;
    protected final Session session;
    protected final String accountId;
    protected final FileStorageServiceFactory factory;

    /**
     * Initializes a new {@link AbstractFileStorageAccountAccess}.
     *
     * @param factory The factory
     * @param session The session
     * @param accountId The account identifier
     */
    protected AbstractFileStorageAccountAccess(final FileStorageServiceFactory factory, final Session session, final String accountId) {
        super();
        this.session = session;
        this.accountId = accountId;
        this.factory = factory;
    }

    @Override
    public void connect() throws OXException {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void close() {
        connected = false;
    }

    @Override
    public boolean ping() throws OXException {
        return true;
    }

    @Override
    public boolean cacheable() {
        return true;
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        return factory.getFileAccess(session, accountId);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        return factory.getFolderAccess(session, accountId);
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolderAccess().getRootFolder();
    }

    @Override
    public FileStorageService getService() {
        return factory.getFileStorageService();
    }

}
