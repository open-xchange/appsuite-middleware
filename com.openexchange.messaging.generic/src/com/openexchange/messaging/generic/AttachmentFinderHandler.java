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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.messaging.generic;

import java.util.Collection;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;

/**
 * {@link AttachmentFinderHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AttachmentFinderHandler implements MessageHandler {

    private final String sequenceId;

    private MessagingPart messagingPart;

    /**
     * Initializes a new {@link AttachmentFinderHandler}.
     *
     * @param sequenceId The sequence identifier of the attachment to find
     */
    public AttachmentFinderHandler(final String sequenceId) {
        super();
        this.sequenceId = sequenceId;
    }

    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        return true;
    }

    @Override
    public boolean handleHeaders(final Map<String, Collection<MessagingHeader>> headers) throws OXException {
        return true;
    }

    @Override
    public void handleMessageEnd(final MessagingMessage message) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean handleMultipart(final MultipartContent multipart) throws OXException {
        return true;
    }

    @Override
    public boolean handleNestedMessage(final MessagingMessage message) throws OXException {
        new MessageParser().parseMessage(message, this);
        return (null == messagingPart);
    }

    @Override
    public boolean handlePart(final MessagingPart part, final boolean isInline) throws OXException {
        if (sequenceId.equals(part.getSectionId())) {
            messagingPart = part;
            return false;
        }
        return true;
    }

    @Override
    public boolean handleReceivedDate(final long receivedDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSystemFlags(final int flags) throws OXException {
        return true;
    }

    @Override
    public boolean handleUserFlags(final Collection<String> userFlags) throws OXException {
        return true;
    }

    /**
     * Gets the part.
     *
     * @return The found part or <code>null</code>
     */
    public MessagingPart getMessagingPart() {
        return messagingPart;
    }

}
