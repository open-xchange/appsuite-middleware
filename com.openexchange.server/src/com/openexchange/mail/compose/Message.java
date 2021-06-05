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

package com.openexchange.mail.compose;

import java.util.List;
import java.util.Map;
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
        TEXT_PLAIN("text/plain", false),
        /**
         * The <code>"text/html"</code> content type.
         */
        TEXT_HTML("text/html", true),
        /**
         * The <code>"multipart/alternative"</code> content type.
         */
        MULTIPART_ALTERNATIVE("multipart/alternative", true);

        private final String id;
        private final boolean impliesHtml;

        private ContentType(String id, boolean impliesHtml) {
            this.id = id;
            this.impliesHtml = impliesHtml;
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
         * Whether this content type implies HTML content.
         *
         * @return <code>true</code> if HTML content is implied; otherwise <code>false</code>
         */
        public boolean isImpliesHtml() {
            return impliesHtml;
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
            if ("alternative".equalsIgnoreCase(lk)) {
                // Old behavior for requesting to build a multipart/alternative message.
                return ContentType.MULTIPART_ALTERNATIVE;
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

        /**
         * Gets the priority for given identifier
         *
         * @param level The level to look-up
         * @return The associated priority or <code>null</code>
         */
        public static Priority priorityForLevel(int level) {
            for (Priority p : Priority.values()) {
                if (level == p.level) {
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
     * Gets the "Reply-To" address.
     *
     * @return The "Reply-To" address
     */
    Address getReplyTo();

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
     * Gets the optional custom headers.
     *
     * @return The custom headers or <code>null</code>
     */
    Map<String, String> getCustomHeaders();

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

    /**
     * Checks if the content of this message is stored encrypted.
     *
     * @return <code>true</code> if encrypted; otherwise <code>false</code>
     */
    boolean isContentEncrypted();

}
