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
 * {@link MessageHandler} - This interface declares the <code>handleXXX</code> methods which are invoked by the
 * {@link MessageParser} instance on certain parts of a message.
 * <p>
 * Each methods returns a boolean value which indicates whether the underlying {@link MessageParser} instance should continue or quit
 * message parsing after method invocation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessageHandler {

    /**
     * Handles specified message headers.
     *
     * @param headers The message headers
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling headers fails
     */
    boolean handleHeaders(Map<String, Collection<MessagingHeader>> headers) throws OXException;

    /**
     * Handles specified color label.
     *
     * @param colorLabel The message's color label
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling color label fails
     */
    boolean handleColorLabel(int colorLabel) throws OXException;

    /**
     * Handles message's system flags (//SEEN, //ANSWERED, ...).
     *
     * @param flags The message's system flags
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling system flags fails
     */
    boolean handleSystemFlags(int flags) throws OXException;

    /**
     * Handle message's user flags.
     *
     * @param userFlags The message's user flags
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling user flags fails
     */
    boolean handleUserFlags(Collection<String> userFlags) throws OXException;

    /**
     * Handle message's received date.
     *
     * @param receivedDate The received date's number of milliseconds since January 1, 1970, 00:00:00 GMT
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling received date fails
     */
    boolean handleReceivedDate(long receivedDate) throws OXException;

    /**
     * Handles specified messaging part.
     *
     * @param part The messaging part
     * @param isInline <code>true</code> if part is considered to be inline; otherwise <code>false</code>
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling part fails
     */
    boolean handlePart(MessagingPart part, boolean isInline) throws OXException;

    /**
     * Handles specified multipart content.
     *
     * @param multipart The multipart content
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling multipart fails
     */
    boolean handleMultipart(MultipartContent multipart) throws OXException;

    /**
     * Handles a nested message (<code>message/rfc822</code>)
     *
     * @param message The nested message
     * @return <code>true</code> to continue processing; otherwise <code>false</code> to quit
     * @throws OXException If handling nested message fails
     */
    boolean handleNestedMessage(MessagingMessage message) throws OXException;

    /**
     * Performs some optional finishing operations
     *
     * @param message The message whose end has been reached
     * @throws OXException If handling message end fails
     */
    void handleMessageEnd(MessagingMessage message) throws OXException;

}
