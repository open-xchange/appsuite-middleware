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

package com.openexchange.mail.attachment.storage;

import java.io.InputStream;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.session.Session;

/**
 * {@link MailAttachmentStorage} - Storage for mail attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public interface MailAttachmentStorage {

    /**
     * Performs required preparations in order to store attachments
     *
     * @param folderName The name of the folder holding the stored attachment (only applicable if storage supports folders visible to user)
     * @param checkForExpiredAttachments <code>true</code> to check for expired mail attachments; otherwise <code>false</code>
     * @param timeToLive The time-to-live for stored attachments
     * @param session The associated session
     * @throws OXException If preparations fail
     */
    void prepareStorage(String folderName, boolean checkForExpiredAttachments, long timeToLive, Session session) throws OXException;

    /**
     * Stores specified mail attachment into storage
     *
     * @param attachment The attachment to store
     * @param op The store operation
     * @param storeProps Additional properties for the store operation
     * @param session The associated session
     * @return The identifier of the stored attachment
     * @throws OXException If store operation fails
     */
    String storeAttachment(MailPart attachment, StoreOperation op, Map<String, Object> storeProps, Session session) throws OXException;

    /**
     * Gets the mail attachment denoted by given identifier
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @return The mail attachment
     * @throws OXException If mail attachment cannot be returned
     */
    MailPart getAttachment(String id, Session session) throws OXException;

    /**
     * Gets the information for the mail attachment denoted by given identifier
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @return The mail attachment information
     * @throws OXException If mail attachment cannot be returned
     */
    MailAttachmentInfo getAttachmentInfo(String id, Session session) throws OXException;

    /**
     * Gets the input stream of the mail attachment denoted by given identifier
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @return The mail attachment stream
     * @throws OXException If mail attachment cannot be returned
     */
    InputStream getAttachmentStream(String id, Session session) throws OXException;

    /**
     * Removes the mail attachment denoted by given identifier from storage
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @throws OXException If mail attachment cannot be removed
     */
    void removeAttachment(String id, Session session) throws OXException;

    /**
     * Discards the the mail attachment denoted by given identifier and associated URI's resource as well
     *
     * @param id The identifier of the attachment held in storage
     * @param uriInformation The optional URI information
     * @param session The associated session
     * @throws OXException If discard operation fails
     */
    void discard(String id, DownloadUri uriInformation, Session session) throws OXException;

}
