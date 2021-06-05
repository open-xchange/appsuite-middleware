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

package com.openexchange.messaging;

import java.io.Serializable;
import java.util.Collection;
import com.openexchange.exception.OXException;

/**
 * {@link MessagingMessage} - A message.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingMessage extends MessagingPart, MessagingContent, Serializable {

    /*-
     * ------------------- Flags ------------------------------
     */

    /**
     * This message has been answered. This flag is set by clients to indicate that this message has been answered to.
     *
     * @value 1
     */
    public static final int FLAG_ANSWERED = 1;

    /**
     * This message is marked deleted. Clients set this flag to mark a message as deleted. The expunge operation on a folder removes all
     * messages in that folder that are marked for deletion.
     *
     * @value 2
     */
    public static final int FLAG_DELETED = 1 << 1;

    /**
     * This message is a draft. This flag is set by clients to indicate that the message is a draft message.
     *
     * @value 4
     */
    public static final int FLAG_DRAFT = 1 << 2;

    /**
     * This message is flagged. No semantic is defined for this flag. Clients alter this flag.
     *
     * @value 8
     */
    public static final int FLAG_FLAGGED = 1 << 3;

    /**
     * This message is recent. Folder implementations set this flag to indicate that this message is new to this folder, that is, it has
     * arrived since the last time this folder was opened.
     * <p>
     * Clients cannot alter this flag.
     *
     * @value 16
     */
    public static final int FLAG_RECENT = 1 << 4;

    /**
     * This message is seen. This flag is implicitly set by the implementation when the this Message's content is returned to the client in
     * some form.
     *
     * @value 32
     */
    public static final int FLAG_SEEN = 1 << 5;

    /**
     * A special flag that indicates that this folder supports user defined flags.
     * <p>
     * Clients cannot alter this flag.
     *
     * @value 64
     */
    public static final int FLAG_USER = 1 << 6;

    /**
     * Virtual Spam flag
     *
     * @value 128
     */
    public static final int FLAG_SPAM = 1 << 7;

    /**
     * Virtual forwarded flag that marks this message as being forwarded.
     *
     * @value 256
     */
    public static final int FLAG_FORWARDED = 1 << 8;

    /**
     * Virtual read acknowledgment flag that marks this message as being notified for delivery.
     *
     * @value 512
     */
    public static final int FLAG_READ_ACK = 1 << 9;

    /*-
     * ------------------- User Flags ------------------------------
     */

    /**
     * The value of virtual forwarded flag.
     *
     * @value $Forwarded
     */
    public static final String USER_FORWARDED = "$Forwarded";

    /**
     * The value of virtual read acknowledgment flag.
     *
     * @value $MDNSent
     */
    public static final String USER_READ_ACK = "$MDNSent";

    /**
     * Gets the folder full name.
     *
     * @return The folder full name or <code>null</code> if not available
     */
    public String getFolder();

    /**
     * Gets the color label.
     *
     * @return The color label
     * @throws OXException If color label cannot be returned
     */
    public int getColorLabel() throws OXException;

    /**
     * Gets the flag bitmask.
     *
     * @return The flag bitmask
     * @throws OXException If flag bitmask cannot be returned
     */
    public int getFlags() throws OXException;

    /**
     * Gets the received date (storage's internal time stamp).
     *
     * @return The received date or <code>-1</code> if not available
     */
    public long getReceivedDate();

    /**
     * Gets the user flags.
     *
     * @return The user flags or <code>null</code> if none available
     * @throws OXException If user flags cannot be returned
     */
    public Collection<String> getUserFlags() throws OXException;

    /**
     * Get the thread level of this message.
     *
     * @return The thread level of this message
     */
    public int getThreadLevel();

    /**
     * Gets the identifier.
     *
     * @return The identifier or <code>null</code> if not available
     */
    public String getId();

    /**
     * Gets the URL to use as a picture for this message. Typically represents the source or author.
     */
    public String getPicture();

    /**
     * Gets the URL associated with this message, if possible.
     * <p>
     * This is useful for RSS messages as they contain links to their origin messages or feeds.
     *
     * @return Gets the URL associated with this message.
     * @throws OXException If no URL can be returned.
     */
    public String getUrl() throws OXException;

}
