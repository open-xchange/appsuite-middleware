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

package com.openexchange.mail.compose.mailstorage.association;

import java.util.List;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.mail.compose.mailstorage.storage.MessageInfo;

/**
 * Encapsulates certain metadata about a composition space and its according
 * most recent draft message.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class DraftMetadata {

    /**
     * Creates draft metadata from specified message description.
     *
     * @param messageDesc The message description
     * @return The newly created draft metadata
     */
    public static DraftMetadata fromMessage(MessageDescription messageDesc, long draftSize) {
        return new DraftMetadata(draftSize, AttachmentMetadata.fromMessage(messageDesc), messageDesc.getSharedAttachmentsInfo(), messageDesc.getSecurity());
    }

    /**
     * Creates draft metadata from specified message info.
     *
     * @param messageInfo The message info
     * @return The newly created draft metadata
     */
    public static DraftMetadata fromMessageInfo(MessageInfo messageInfo) {
        MessageDescription messageDesc = messageInfo.getMessage();
        return new DraftMetadata(messageInfo.getSize(), AttachmentMetadata.fromMessage(messageDesc), messageDesc.getSharedAttachmentsInfo(), messageDesc.getSecurity());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final long size;
    private final SharedAttachmentsInfo sharedAttachmentsInfo;
    private final Security security;
    private final List<AttachmentMetadata> attachments;

    /**
     * Initializes a new {@link DraftMetadata}.
     */
    private DraftMetadata(long size, List<AttachmentMetadata> attachments, SharedAttachmentsInfo sharedAttachmentsInfo, Security security) {
        super();
        this.size = size;
        this.sharedAttachmentsInfo = sharedAttachmentsInfo;
        this.security = security;
        this.attachments = attachments;
    }

    /**
     * Gets the size in bytes.
     *
     * @return The size or <code>-1</code>
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the shared attachments information.
     *
     * @return The shared attachments information
     */
    public SharedAttachmentsInfo getSharedAttachmentsInfo() {
        return sharedAttachmentsInfo;
    }

    /**
     * Gets the security information.
     *
     * @return The security information
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Gets the attachments.
     *
     * @return The attachments
     */
    public List<AttachmentMetadata> getAttachments() {
        return attachments;
    }

}
