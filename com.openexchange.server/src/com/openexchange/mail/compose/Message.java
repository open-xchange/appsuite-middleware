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

package com.openexchange.mail.compose;

import java.util.List;
import com.openexchange.java.Strings;

/**
 * {@link Message} - Represents a message, which is being composed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface Message {

    /** The type of a message's (textual) content */
    public static enum ContentType {
    /**
     * The <code>"text/plain"</code> content type.
     */
    TEXT_PLAIN("text/plain"),
    /**
     * The <code>"text/html"</code> content type.
     */
    TEXT_HTML("text/html");

        private final String id;

        private ContentType(String id) {
            this.id = id;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the Content-Type for given identifier
         *
         * @param contentType The identifier to look-up
         * @return The associated Content-Type or <code>null</code>
         */
        public static ContentType contentTypeFor(String contentType) {
            if (Strings.isEmpty(contentType)) {
                return null;
            }

            String lk = contentType.trim();
            if ("ALTERNATIVE".equals(lk)) {
                // Old behavior for requesting to build a multipart/alternative message. Assume text/html in that case.
                return ContentType.TEXT_HTML;
            }
            for (ContentType ct : ContentType.values()) {
                if (lk.equalsIgnoreCase(ct.id)) {
                    return ct;
                }
            }
            return null;
        }
    }

    /** A message's priority */
    public static enum Priority {
        /**
         * The <code>"low"</code> priority.
         */
        LOW("low", 5),
        /**
         * The <code>"normal"</code> priority.
         */
        NORMAL("normal", 3),
        /**
         * The <code>"high"</code> priority.
         */
        HIGH("high", 1);

        private final String id;
        private final int level;

        private Priority(String id, int level) {
            this.id = id;
            this.level = level;
        }

        /**
         * Gets the level
         *
         * @return The level
         */
        public int getLevel() {
            return level;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the priority for given identifier
         *
         * @param priority The identifier to look-up
         * @return The associated priority or <code>null</code>
         */
        public static Priority priorityFor(String priority) {
            if (Strings.isEmpty(priority)) {
                return null;
            }

            String lk = priority.trim();
            for (Priority p : Priority.values()) {
                if (lk.equalsIgnoreCase(p.id)) {
                    return p;
                }
            }
            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the "From" address.
     *
     * @return The "From" address
     */
    Address getFrom();

    /**
     * Gets the "Sender" address.
     *
     * @return The "Sender" address
     */
    Address getSender();

    /**
     * Gets the "To" addresses.
     *
     * @return The "To" addresses (might be <code>null</code> or an empty list to signal no such recipient addresses)
     */
    List<Address> getTo();

    /**
     * Gets the "Cc" addresses.
     *
     * @return The "Cc" addresses (might be <code>null</code> or an empty list to signal no such recipient addresses)
     */
    List<Address> getCc();

    /**
     * Gets the "Bcc" addresses.
     *
     * @return The "Bcc" addresses (might be <code>null</code> or an empty list to signal no such recipient addresses)
     */
    List<Address> getBcc();

    /**
     * Gets the subject.
     *
     * @return The subject
     */
    String getSubject();

    /**
     * Gets the content.
     *
     * @return The content
     */
    String getContent();

    /**
     * Gets the content type.
     *
     * @return The content type
     */
    ContentType getContentType();

    /**
     * Indicates whether a read receipt is supposed to be requested.
     *
     * @return <code>true</code> to request a read receipt; otherwise <code>false</code>
     */
    boolean isRequestReadReceipt();

    /**
     * Gets the information whether attachments are supposed to be shared (via a link), rather than attaching them to the message.
     *
     * @return The shared attachments information
     * @see SharedAttachmentsInfo#DISABLED
     */
    SharedAttachmentsInfo getSharedAttachments();

    /**
     * Gets the attachment listing for this message.
     *
     * @return The attachments (might be <code>null</code> or an empty list to signal no attachments)
     */
    List<Attachment> getAttachments();

    /**
     * Gets the meta information for this message.
     *
     * @return The meta information
     */
    Meta getMeta();

    /**
     * Gets the security information for this message.
     *
     * @return The security information
     * @see Security#DISABLED
     */
    Security getSecurity();

    /**
     * Gets this message's priority.
     *
     * @return The priority
     */
    Priority getPriority();

}
