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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.session.Session;

/**
 * {@link FileStorageAccountStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public interface FileStorageAccountStorage {

    /**
     * Gets the denoted account.
     *
     * @param serviceId The service identifier
     * @param id The account ID
     * @param session The session
     * @return The account
     * @throws OXException If returning account fails
     */
    public FileStorageAccount getAccount(String serviceId, int id, Session session) throws OXException;

    /**
     * Gets all accounts associated with specified service and given user.
     *
     * @param serviceId The service ID
     * @param session The session
     * @return All accounts associated with specified service and given user
     * @throws OXException If accounts cannot be returned
     */
    public List<FileStorageAccount> getAccounts(String serviceId, Session session) throws OXException;

    /**
     * Adds given account.
     *
     * @param serviceId The service identifier
     * @param account The account
     * @param session The session
     * @return The identifier of the newly created account
     * @throws OXException If insertion fails
     */
    public int addAccount(String serviceId, FileStorageAccount account, Session session) throws OXException;

    /**
     * Deletes denoted account.
     *
     * @param serviceId The service identifier
     * @param account The account
     * @param session The session
     * @throws OXException If deletion fails
     */
    public void deleteAccount(String serviceId, FileStorageAccount account, Session session) throws OXException;

    /**
     * Updates given account.
     *
     * @param serviceId The service identifier
     * @param account The account
     * @param session The session
     * @throws OXException If update fails
     */
    public void updateAccount(String serviceId, FileStorageAccount account, Session session) throws OXException;

}
