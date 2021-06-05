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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FileStorageAccountManager} - An account manager for a certain file storage service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public interface FileStorageAccountManager extends FileStorageConstants {

    /**
     * Adds a new account.
     *
     * @param account The account to add
     * @param session The session providing needed user data
     * @return The identifier of the newly created account
     * @throws OXException If insertion fails
     */
    String addAccount(FileStorageAccount account, Session session) throws OXException;

    /**
     * Updates an existing account.
     *
     * @param account The account providing the identifier and the data to update
     * @param session The session providing needed user data
     * @throws OXException If update fails
     */
    void updateAccount(FileStorageAccount account, Session session) throws OXException;

    /**
     * Deletes an existing account.
     *
     * @param account The account to delete
     * @param session The session providing needed user data
     * @throws OXException If deletion fails
     */
    void deleteAccount(FileStorageAccount account, Session session) throws OXException;

    /**
     * Gets all service's accounts associated with session user.
     *
     * @param session The session providing needed user data
     * @return All accounts associated with session user.
     * @throws OXException If listing fails
     */
    List<FileStorageAccount> getAccounts(Session session) throws OXException;

    /**
     * Gets an existing file storage account.
     *
     * @param id The identifier
     * @param session The session providing needed user data
     * @return The file storage account.
     * @throws OXException If retrieval fails
     */
    FileStorageAccount getAccount(String id, Session session) throws OXException;

    /**
     * Cleans-up accounts that could no more be decrypted with given secret
     *
     * @param secret The current secret
     * @param session The session providing user information
     * @throws OXException If operation fails
     */
    void cleanUp(String secret, Session session) throws OXException;

    void removeUnrecoverableItems(String secret, Session session) throws OXException;
    
    /**
     * Migrates all encrypted strings from an old secret to a new one.
     *
     * @param oldSecret The old secret for decrypting stored secret strings
     * @param newSecret The new secret used for encrypting the secret strings
     * @param session The session providing needed user data
     * @throws OXException If migrate operation fails for any reason
     */
    void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException;

    /**
     * Tests for encrypted items.
     *
     * @param session The session providing needed user data
     * @return <code>true</code> if encrypted items are available; otherwise <code>false</code>
     * @throws OXException If test for encrypted items fails
     */
    boolean hasEncryptedItems(Session session) throws OXException;

}
