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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;

/**
 * {@link IMailFolderStorageEnhanced}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailFolderStorageEnhanced extends IMailFolderStorage {

    /**
     * Expunge all messages marked as \Deleted and moves them to trash folder.
     *
     * @param fullName The folder full name
     * @throws OXException If expunge operation fails
     */
    public void expungeFolder(String fullName) throws OXException;

    /**
     * Expunge all messages marked as \Deleted.
     *
     * @param fullName The folder full name
     * @param hardDelete <code>true</code> to perform hard-delete; otherwise messages are moved to trash folder
     * @throws OXException If expunge operation fails
     */
    public void expungeFolder(String fullName, boolean hardDelete) throws OXException;

    /**
     * Gets the number of unread mails for specified folder.
     *
     * @param fullName The folder's full name
     * @return The unread counter
     * @throws OXException If an error occurs
     */
    int getUnreadCounter(String fullName) throws OXException;

    /**
     * Gets the number of new mails for specified folder (since last access to folder).
     *
     * @param fullName The folder's full name
     * @return The new counter
     * @throws OXException If an error occurs
     */
    int getNewCounter(String fullName) throws OXException;

    /**
     * Gets the total number of mails for specified folder.
     *
     * @param fullName The folder's full name
     * @return The total counter
     * @throws OXException If an error occurs
     */
    int getTotalCounter(String fullName) throws OXException;

}
