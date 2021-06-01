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

package com.openexchange.mail.compose;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * {@link CompositionSpaceService} - The composition space service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface CompositionSpaceService {

    /**
     * Gets the identifier for this composition space service.
     *
     * @return The service identifier
     */
    default String getServiceId() {
        return CompositionSpaceServiceFactory.DEFAULT_SERVICE_ID;
    }

    /**
     * Gets the collected warnings that occurred while using this composition space service.
     *
     * @return The warnings
     */
    Collection<OXException> getWarnings();

    /**
     * Transports the mail resulting from specified composition space.
     *
     * @param compositionSpaceId The identifier of the composition space describing the mail to transport
     * @param optionalUploadedAttachments The optional uploaded attachments that are streamed-through w/o being saved to attachment storage
     * @param mailSettings The user's mail settings
     * @param requestData The request data
     * @param warnings The optional collection to add possible warnings to
     * @param deleteAfterTransport Whether the composition space is supposed to be deleted after transport
     * @param clientToken The clients current token to edit the space
     * @return The path to the sent mail or <code>null</code>
     * @throws OXException If transport fails
     */
    MailPath transportCompositionSpace(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, UserSettingMail mailSettings, AJAXRequestData requestData, List<OXException> warnings, boolean deleteAfterTransport, ClientToken clientToken) throws OXException;

    /**
     * Saves given composition space to an appropriate draft mail.
     *
     * @param compositionSpaceId The identifier of the composition space to save as draft mail
     * @param optionalUploadedAttachments The optional uploaded attachments that are streamed-through w/o being saved to attachment storage
     * @param deleteAfterSave Whether the composition space is supposed to be deleted after saving as draft mail
     * @param clientToken The clients current token to edit the space
     * @return The path to the draft mail
     * @throws OXException If conversion fails
     */
    MailPath saveCompositionSpaceToDraftMail(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, boolean deleteAfterSave, ClientToken clientToken) throws OXException;

    /**
     * Opens a new composition space for composing a message according to given parameters.
     *
     * @return The opened composition space
     * @throws OXException If no composition space can be opened
     */
    CompositionSpace openCompositionSpace(OpenCompositionSpaceParameters parameters) throws OXException;

    /**
     * Closes specified composition space and drops all associated resources.
     *
     * @param compositionSpaceId The composition space identifier
     * @param hardDelete Whether associated resources are supposed to be permanently deleted or a backup should be moved to some sort of trash bin
     * @param clientToken The clients current token to edit the space
     * @return <code>true</code> if such a composition space has been successfully closed; otherwise <code>false</code>
     * @throws OXException If closing/deleting composition space fails
     */
    boolean closeCompositionSpace(UUID compositionSpaceId, boolean hardDelete, ClientToken clientToken) throws OXException;

    /**
     * Closes those composition spaces associated with given session, which are idle for longer than given max. idle time.
     * @param maxIdleTime The max. idle time in milliseconds
     *
     * @throws OXException If composition spaces cannot be closed/deleted
     */
    void closeExpiredCompositionSpaces(long maxIdleTimeMillis) throws OXException;

    /**
     * Gets the composition space associated with given identifier.
     *
     * @param compositionSpaceId The composition space identifier
     * @return The composition space
     * @throws OXException If composition space cannot be returned
     */
    CompositionSpace getCompositionSpace(UUID compositionSpaceId) throws OXException;

    /**
     * Gets the identifiers of all available composition spaces associated with given session.
     *
     * @param fields The fields to set in returned composition spaces
     * @return The composition space identifiers
     * @throws OXException If composition spaces cannot be returned
     */
    List<CompositionSpace> getCompositionSpaces(MessageField[] fields) throws OXException;

    /**
     * Updates the composition space associated with given identifier.
     *
     * @param compositionSpaceId The composition space identifier
     * @param messageDescription The message description providing the changes to apply
     * @param clientToken The clients current token to edit the space
     * @return The composition space
     * @throws OXException If composition space cannot be returned
     */
    CompositionSpace updateCompositionSpace(UUID compositionSpaceId, MessageDescription messageDescription, ClientToken clientToken) throws OXException;

    /**
     * Adds new attachments from upload to given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param attachmentId The identifier of the attachment to replace
     * @param uploadedAttachments The uploaded attachments
     * @param disposition The disposition
     * @param clientToken The clients current token to edit the space
     * @return The replaced attachments
     * @throws OXException If replacing the attachment fails
     */
    AttachmentResult replaceAttachmentInCompositionSpace(UUID compositionSpaceId, UUID attachmentId, StreamedUploadFileIterator uploadedAttachments, String disposition, ClientToken clientToken) throws OXException;

    /**
     * Adds new attachments from upload to given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param uploadedAttachments The uploaded attachments
     * @param disposition The disposition
     * @param clientToken The clients current token to edit the space
     * @return The added attachments
     * @throws OXException If adding the attachments fails
     */
    AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, String disposition, ClientToken clientToken) throws OXException;

    /**
     * Adds a new attachment to given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param attachment The attachment description
     * @param data The attachment's data
     * @param disposition The disposition
     * @param clientToken The clients current token to edit the space
     * @return The added attachment
     * @throws OXException If adding the attachment fails
     */
    AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, AttachmentDescription attachment, InputStream data, ClientToken clientToken) throws OXException;

    /**
     * Adds user's vCard as attachment to given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param clientToken The clients current token to edit the space
     * @return The added vCard attachment
     * @throws OXException If adding the vCard fails
     */
    AttachmentResult addVCardToCompositionSpace(UUID compositionSpaceId, ClientToken clientToken) throws OXException;

    /**
     * Adds given contact's vCard as attachment to given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param contactId The identifier of the contact
     * @param folderId The identifier of the folder in which the contact resides
     * @param clientToken The clients current token to edit the space
     * @return The added vCard attachment
     * @throws OXException If adding the vCard fails
     */
    AttachmentResult addContactVCardToCompositionSpace(UUID compositionSpaceId, String contactId, String folderId, ClientToken clientToken) throws OXException;

    /**
     * Adds the attachments from the original mail (e.g. on reply) to denoted composition space
     *
     * @param compositionSpaceId The composition space identifier
     * @return The added attachments
     * @param clientToken The clients current token to edit the space
     * @throws OXException If adding attachments fails
     */
    AttachmentResult addOriginalAttachmentsToCompositionSpace(UUID compositionSpaceId, ClientToken clientToken) throws OXException;

    /**
     * Deletes the specified attachment from given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param attachmentId The identifier of the attachment to delete
     * @param clientToken The clients current token to edit the space
     * @throws OXException If deletion fails
     */
    AttachmentResult deleteAttachment(UUID compositionSpaceId, UUID attachmentId, ClientToken clientToken) throws OXException;

    /**
     * Gets the specified attachment from given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param attachmentId The identifier of the attachment to delete
     * @return The attachment
     * @throws OXException If retrieval fails
     */
    AttachmentResult getAttachment(UUID compositionSpaceId, UUID attachmentId) throws OXException;

    /**
     * Gets the attachment upload limits for given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @returns The limits
     * @throws OXException If retrieval fails
     */
    UploadLimits getAttachmentUploadLimits(UUID compositionSpaceId) throws OXException;

}
