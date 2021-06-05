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
