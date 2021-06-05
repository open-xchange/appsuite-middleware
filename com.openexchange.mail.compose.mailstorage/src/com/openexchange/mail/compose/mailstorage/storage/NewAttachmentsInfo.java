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

package com.openexchange.mail.compose.mailstorage.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.MessageDescription;

/**
 * Extends {@link MessageInfo} to return a collection of newly added attachments.
 *
 * @see MessageInfo
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class NewAttachmentsInfo extends MessageInfo {

    private final List<UUID> newAttachmentIds;

    /**
     * Initializes a new {@link NewAttachmentsInfo}.
     *
     * @param newAttachmentIds The list of new attachment IDs
     * @param message The message description. It MUST contain all new attachments for that an ID is provided.
     * @param size
     * @param lastModified
     * @throws IllegalArgumentException If message or newAttachmentIds are <code>null</code> or
     *         if a provided attachment ID is not found the message's list of attachments
     */
    NewAttachmentsInfo(List<UUID> newAttachmentIds, MessageDescription message, long size, Date lastModified) {
        super(message, size, lastModified);
        this.newAttachmentIds = Objects.requireNonNull(newAttachmentIds);
        for (UUID id : newAttachmentIds) {
            if (!message.getAttachments().stream().anyMatch(a -> id.equals(a.getId()))) {
                throw new IllegalArgumentException("New attachment '" + UUIDs.getUnformattedString(id) + "' is missing in message!");
            }
        }
    }

    /**
     * Gets the identifiers of new attachments.
     *
     * @return The identifiers of new attachments
     */
    public List<UUID> getNewAttachmentIds() {
        return newAttachmentIds;
    }

    /**
     * Gets the attachment associated with given attachment identifier.
     *
     * @param id The attachment identifier to look-up
     * @return The associated attachment
     * @throws NoSuchElementException If there is no such attachment present
     */
    public Attachment getAttachment(UUID id) {
        Optional<Attachment> optionalAttachment = message.getAttachments().stream().filter(a -> id.equals(a.getId())).findFirst();
        if (optionalAttachment.isPresent()) {
            return optionalAttachment.get();
        }
        throw new NoSuchElementException("No such attachment present for identifier: " + UUIDs.getUnformattedString(id));
    }

    /**
     * Gets the listing of newly added attachments.
     *
     * @return The added attachment
     */
    public List<Attachment> getNewAttachments() {
        List<Attachment> retval = new ArrayList<>(newAttachmentIds.size());
        Set<UUID> idSet = new HashSet<>(newAttachmentIds);
        for (Attachment attachment : message.getAttachments()) {
            if (idSet.contains(attachment.getId())) {
                retval.add(attachment);
            }
        }
        return retval;
    }

}
