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

package com.openexchange.pop3.storage;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;

/**
 * {@link POP3Storage} - Storage for messages from a POP3 account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface POP3Storage {
    
    /**
     * Connects this POP3 storage.
     *
     * @throws OXException If establishing a connection for this storage fails
     */
    default public void connect() throws OXException {
        connect(false);
    }

    /**
     * Connects this POP3 storage.
     *
     * @param enableDebug Whether to enabled debug logging
     * @throws OXException If establishing a connection for this storage fails
     */
    public void connect(boolean enableDebug) throws OXException;

    /**
     * Closes this storage and releases occupied resources.
     *
     * @throws OXException If closing the storage fails
     */
    public void close() throws OXException;

    /**
     * Gets possible warnings.
     *
     * @return Possible warnings.
     */
    public Collection<OXException> getWarnings();

    /**
     * Convenience method to obtain folder's number of unread messages in a fast way; meaning no default folder check is performed.
     *
     * @throws OXException If returning the unread count fails
     */
    public int getUnreadMessagesCount(final String fullname) throws OXException;

    /**
     * Releases all used resources prior to closing this storage.
     */
    public void releaseResources();

    /**
     * Synchronizes this storage with actual POP3 account.
     * <p>
     * Throws an <b><code>AlreadyLockedException</code></b> if another thread currently performs a sync attempt.
     * <p>
     * Tries to establish a connection to actual POP3 account, invokes {@link POP3StorageConnectCounter#incrementCounter()
     * incrementCounter()}, fetches all contained messages, synchronizes them with the ones hold in this storage, and finally invokes
     * {@link POP3StorageConnectCounter#decrementCounter() decrementCounter()}.
     *
     * @param expunge Whether to expunge messages from actual POP3 account after their retrieval
     * @param lastAccessed The last-accessed time stamp or <code>null</code> in case of first access
     * @throws OXException If synchronizing messages fails
     * @throws AlreadyLockedException If another thread currently performs a sync attempt
     */
    public void syncMessages(boolean expunge, Long lastAccessed) throws OXException;

    /**
     * Drops resources for associated user.
     *
     * @throws OXException If operation fails
     */
    public void drop() throws OXException;

    /**
     * Gets the appropriate {@link IMailFolderStorage} implementation that is considered as the main entry point to a user's mailbox.
     *
     * @return The appropriate {@link IMailFolderStorage} implementation
     * @throws OXException If connection is not established
     */
    public IMailFolderStorage getFolderStorage() throws OXException;

    /**
     * Gets the appropriate {@link IMailMessageStorage} implementation that provides necessary message-related operations/methods.
     *
     * @return The appropriate {@link IMailMessageStorage} implementation
     * @throws OXException If connection is not established
     */
    public IMailMessageStorage getMessageStorage() throws OXException;

    /**
     * Gets the UIDL map.
     *
     * @return The UIDL map
     * @throws OXException If UIDL map cannot be returned
     */
    public POP3StorageUIDLMap getUIDLMap() throws OXException;

    /**
     * Gets the trash container (containing UIDLS of permanently deleted POP3 messages)
     *
     * @return The trash container
     * @throws OXException If trash container cannot be returned
     */
    public POP3StorageTrashContainer getTrashContainer() throws OXException;

}
