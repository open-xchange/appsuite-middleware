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

import java.util.EnumMap;
import java.util.Map;

/**
 * {@link MessagingHeader} - A message header.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingHeader {

    /**
     * An enumeration of known headers.
     */
    public static enum KnownHeader {
        /**
         * Account name
         */
        ACCOUNT_NAME("X-Account-Name"),
        /**
         * Bcc
         */
        BCC("Bcc"),
        /**
         * Cc
         */
        CC("Cc"),
        /**
         * Content-Type
         */
        CONTENT_TYPE("Content-Type"),
        /**
         * Content-Disposition
         */
        CONTENT_DISPOSITION("Content-Disposition"),
        /**
         * Content-Transfer-Encoding
         */
        CONTENT_TRANSFER_ENCODING("Content-Transfer-Encoding"),
        /**
         * From
         */
        FROM("From"),
        /**
         * In-Reply-To
         */
        IN_REPLY_TO("In-Reply-To"),
        /**
         * Message-Id
         */
        MESSAGE_ID("Message-Id"),
        /**
         * MIME-Version
         */
        MIME_VERSION("MIME-Version"),
        /**
         * X-Priority
         */
        PRIORITY("X-Priority"),
        /**
         * References
         */
        REFERENCES("References"),
        /**
         * Disposition-Notification-To
         */
        DISPOSITION_NOTIFICATION_TO("Disposition-Notification-To"),
        /**
         * Reply-To
         */
        REPLY_TO("Reply-To"),
        /**
         * Date
         */
        SENT_DATE("Date"),
        /**
         * Subject
         */
        SUBJECT("Subject"),
        /**
         * To
         */
        TO("To"),
        /**
         * Date
         */
        DATE("Date"),
        /**
         * X-Message-Type
         */
        MESSAGE_TYPE("X-Message-Type"),
        /**
         * X-Mailer
         */
        MAILER("X-Mailer");

        private final String name;

        private KnownHeader(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private static final Map<KnownHeader, MessagingField> equivalenceMap = new EnumMap<KnownHeader, MessagingField>(KnownHeader.class);

        static {
            equivalenceMap.put(BCC, MessagingField.BCC);
            equivalenceMap.put(CC, MessagingField.CC);
            equivalenceMap.put(CONTENT_TYPE, MessagingField.CONTENT_TYPE);
            equivalenceMap.put(FROM, MessagingField.FROM);
            equivalenceMap.put(PRIORITY, MessagingField.PRIORITY);
            equivalenceMap.put(DISPOSITION_NOTIFICATION_TO, MessagingField.DISPOSITION_NOTIFICATION_TO);
            equivalenceMap.put(SENT_DATE, MessagingField.SENT_DATE);
            equivalenceMap.put(SUBJECT, MessagingField.SUBJECT);
            equivalenceMap.put(TO, MessagingField.TO);
            equivalenceMap.put(DATE, MessagingField.SENT_DATE);
            MessagingField.initHeaders();
        }

        /**
         * Maps a {@link MessagingHeader} to a {@link MessagingField}
         *
         * @return The {@link MessagingField} this field is associated with
         */
        public MessagingField getEquivalentField() {
            return equivalenceMap.get(this);
        }
    }

    /**
     * The header type.
     */
    public static enum HeaderType {
        /**
         * The header value is a usual string.
         */
        PLAIN,
        /**
         * The header value is a date.
         */
        DATE,
        /**
         * The header value is an address.
         */
        ADDRESS,
        /**
         * The header contains parameter.
         */
        PARAMETERIZED,

        ;
    }

    /**
     * Gets the header type.
     *
     * @return The header type
     */
    public HeaderType getHeaderType();

    /**
     * Gets the name.
     *
     * @return The name
     */
    public String getName();

    /**
     * Gets the value.
     *
     * @return The value
     */
    public String getValue();

}
