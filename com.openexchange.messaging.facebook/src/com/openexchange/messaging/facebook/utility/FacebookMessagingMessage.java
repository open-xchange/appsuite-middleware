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

package com.openexchange.messaging.facebook.utility;

import com.openexchange.messaging.generic.internet.MimeMessagingMessage;

/**
 * {@link FacebookMessagingMessage} - Extends {@link MimeMessagingMessage} by facebook user identifier.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookMessagingMessage extends MimeMessagingMessage {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5314172807807461367L;

    private long fromUserId;

    private final StringBuilder messageText;

    private String toString;

    // private long postId;

    /**
     * Initializes a new {@link FacebookMessagingMessage}.
     */
    public FacebookMessagingMessage() {
        super();
        messageText = new StringBuilder(256);
        toString = "[no content]";
    }

    /**
     * Appends specified text.
     * <p>
     * Text should already be HTML-escaped: '<' ==> &amp;lt
     * 
     * @param textContent The text to append
     */
    public void appendTextContent(final String textContent) {
        messageText.append(textContent);
    }

    /**
     * Gets the message text.
     * 
     * @return The message text
     */
    public CharSequence getMessageText() {
        return messageText;
    }

    /**
     * Gets the message text length.
     * 
     * @return The message text length
     */
    public int getMessageTextLength() {
        return messageText.length();
    }

    /**
     * Sets the toString() text.
     * 
     * @param toString The toString() text
     */
    public void setToString(String toString) {
        this.toString = toString;
    }

    @Override
    public String toString() {
        return toString;
    }

    /**
     * Gets the <i>"From"</i> user identifier.
     * 
     * @return The <i>"From"</i> user identifier
     */
    public long getFromUserId() {
        return fromUserId;
    }

    /**
     * Sets the <i>"From"</i> user identifier.
     * 
     * @param userId The <i>"From"</i> user identifier to set
     */
    public void setFromUserId(final long userId) {
        this.fromUserId = userId;
    }

    /**
     * // * Gets the post identifier. // * // * @return The post identifier //
     */
    // public long getPostId() {
    // return postId;
    // }
    //
    // /**
    // * Sets the post identifier.
    // *
    // * @param postId The post identifier to set
    // */
    // public void setPostId(final long postId) {
    // this.postId = postId;
    // }

}
