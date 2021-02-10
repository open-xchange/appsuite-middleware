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
