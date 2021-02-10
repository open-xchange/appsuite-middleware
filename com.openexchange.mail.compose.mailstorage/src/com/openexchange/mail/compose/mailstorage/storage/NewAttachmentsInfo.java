/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
