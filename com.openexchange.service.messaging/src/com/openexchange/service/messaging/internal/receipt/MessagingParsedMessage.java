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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.service.messaging.internal.receipt;

import java.util.UUID;

/**
 * {@link MessagingParsedMessage} - A contiguous message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingParsedMessage {

    private final String topic;

    private final UUID uuid;

    private final byte[] chunk;

    private final int chunkNumber;

    private boolean contiguous;

    private final int prefixCode;

    /**
     * Initializes a new {@link MessagingParsedMessage}.
     *
     * @param uuid The message UUID
     * @param prefixCode The prefix code
     * @param chunkNumber The chunk number
     * @param chunk The next data chunk
     */
    public MessagingParsedMessage(final UUID uuid, final int prefixCode, final int chunkNumber, final byte[] chunk) {
        super();
        this.uuid = uuid;
        this.prefixCode = prefixCode;
        topic = null;
        this.chunkNumber = chunkNumber;
        this.chunk = chunk;
    }

    /**
     * Initializes a new {@link MessagingParsedMessage}.
     *
     * @param uuid The message UUID
     * @param prefixCode The prefix code
     * @param topic The message topic
     * @param firstChunk The first data chunk
     */
    public MessagingParsedMessage(final UUID uuid, final int prefixCode, final String topic, final byte[] firstChunk) {
        super();
        this.uuid = uuid;
        this.prefixCode = prefixCode;
        this.topic = topic;
        this.chunkNumber = 1;
        this.chunk = firstChunk;
    }

    /**
     * Gets the message's UUID.
     *
     * @return The message's UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the topic
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the chunk
     *
     * @return The chunk
     */
    public byte[] getChunk() {
        return chunk;
    }

    /**
     * Gets the chunk number
     *
     * @return The chunk number
     */
    public int getChunkNumber() {
        return chunkNumber;
    }

    /**
     * Checks the contiguous flag.
     *
     * @return The contiguous flag.
     */
    public boolean isContiguous() {
        return contiguous;
    }

    /**
     * Sets the contiguous flag.
     *
     * @param contiguous The contiguous flag.
     */
    public void setContiguous(final boolean contiguous) {
        this.contiguous = contiguous;
    }

    /**
     * Gets the prefix code.
     *
     * @return The prefix code
     */
    public int getPrefixCode() {
        return prefixCode;
    }

}
