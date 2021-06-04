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

package com.openexchange.mail.json.compose.share.spi;

import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.Applicable;
import com.openexchange.mail.json.compose.ComposeContext;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.share.FileItem;
import com.openexchange.mail.json.compose.share.FileItems;
import com.openexchange.mail.json.compose.share.Item;
import com.openexchange.mail.json.compose.share.StorageQuota;
import com.openexchange.mail.json.compose.share.StoredAttachments;
import com.openexchange.mail.json.compose.share.StoredAttachmentsControl;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AttachmentStorage} - The storage for attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface AttachmentStorage extends Applicable {

    @Override
    default boolean applicableFor(ComposeRequest composeRequest) throws OXException {
        return applicableFor(composeRequest.getSession());
    }

    /**
     * Checks if this instance is applicable for specified session.
     *
     * @param session The session
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean applicableFor(Session session);

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Stores the attachments associated with specified compose context.
     *
     * @param sourceMessage The source message providing basic information
     * @param password The optional password to protect the attachments
     * @param expiry The optional expiration date
     * @param autoDelete <code>true</code> to have the files being cleansed provided that <code>expiry</code> is given; otherwise <code>false</code> to leave them
     * @param context The associated compose context; e.g. providing the attachments to store
     * @return The result for stored attachments
     * @throws OXException If attachments cannot be stored for any reason
     */
    StoredAttachmentsControl storeAttachments(ComposedMailMessage sourceMessage, String password, Date expiry, boolean autoDelete, ComposeContext context) throws OXException;

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if specified folder/location exists.
     *
     * @param folderId The folder identifier
     * @param session The session providing user information
     * @return <code>true</code> if such a folder/location exists; otherwise <code>false</code>
     * @throws OXException If existence check fails
     */
    boolean existsFolder(String folderId, ServerSession session) throws OXException;

    /**
     * Stores given attachments in a new location/folder.
     *
     * @param attachments The attachments to store
     * @param subject The current subject of the associated message
     * @param session The session providing user information
     * @return The result for the stored attachments
     * @throws OXException If attachment cannot be stored
     */
    StoredAttachments storeAttachments(List<MailPart> attachments, String subject, ServerSession session) throws OXException;

    /**
     * Stores given attachments in a new location/folder.
     *
     * @param attachments The attachments to store
     * @param subject The current subject of the associated message
     * @param session The session providing user information
     * @return The result for the stored attachments
     * @throws OXException If attachment cannot be stored
     */
    StoredAttachments storeAttachments(FileItems attachments, String subject, ServerSession session) throws OXException;

    /**
     * Appends given attachment to given location/folder.
     *
     * @param attachment The attachment to append
     * @param folderId The folder identifier
     * @param session The session providing user information
     * @return The appended item
     * @throws OXException If attachment cannot be appended
     */
    Item appendAttachment(MailPart attachment, String folderId, ServerSession session) throws OXException;

    /**
     * Appends given attachment to given location/folder.
     *
     * @param attachment The attachment to append
     * @param folderId The folder identifier
     * @param session The session providing user information
     * @return The appended item
     * @throws OXException If attachment cannot be appended
     */
    Item appendAttachment(FileItem attachment, String folderId, ServerSession session) throws OXException;

    /**
     * Retrieves the items residing in given location/folder.
     *
     * @param folderId The folder identifier
     * @param session The session providing user information
     * @return The items contained in folder
     * @throws OXException If items cannot be returned
     */
    List<Item> getAttachments(String folderId, ServerSession session) throws OXException;

    /**
     * Gets the denoted file item from given location/folder.
     *
     * @param attachmentId The identifier of the attachment to fetch
     * @param folderId The folder identifier
     * @param session  The session providing user information
     * @return The file item
     * @throws OXException If file item cannot be returned
     */
    FileItem getAttachment(String attachmentId, String folderId, ServerSession session) throws OXException;

    /**
     * Deletes specified attachment from denoted location/folder.
     *
     * @param attachmentId The identifier of attachment to delete
     * @param folderId The folder identifier
     * @param session The session providing user information
     * @throws OXException If deletion fails
     */
    void deleteAttachment(String attachmentId, String folderId, ServerSession session) throws OXException;

    /**
     * Deletes specified location/folder (and thus all currently contained attachments).
     *
     * @param folderId The folder identifier
     * @param session The session providing user information
     * @throws OXException If deletion fails
     */
    void deleteFolder(String folderId, ServerSession session) throws OXException;

    /**
     * Renames specified location/folder.
     *
     * @param subject The current subject of the associated message
     * @param folderId The folder identifier
     * @param session The session providing user information
     * @throws OXException If deletion fails
     */
    void renameFolder(String subject, String folderId, ServerSession session) throws OXException;

    /**
     * Creates the share target for the folder (for an anonymous user).
     *
     * @param sourceMessage The source message providing basic information
     * @param folderId The folder identifier
     * @param attachmentItems
     * @param folderItem
     * @param password The optional password to protect the attachments
     * @param expiry The optional expiration date
     * @param autoDelete <code>true</code> to have the files being cleansed provided that <code>expiry</code> is given; otherwise <code>false</code> to leave them
     * @param session The session providing user information
     * @param context The associated compose context; does not contain any attachments
     * @return The share target encapsulated in a StoredAttachmentsControl
     * @throws OXException If share target cannot be created
     */
    StoredAttachmentsControl createShareTarget(ComposedMailMessage sourceMessage, String folderId, Item folderItem, List<Item> attachmentItems, String password, Date expiry, boolean autoDelete, ServerSession session, ComposeContext contextToPass) throws OXException;

    /**
     * Gets the current storage quota and usage for eager over-quota checks during attachment upload.
     *
     * @param session The session providing user information
     * @return The quota information
     * @throws OXException If quota cannot be determined
     */
    StorageQuota getStorageQuota(ServerSession session) throws OXException;

}
