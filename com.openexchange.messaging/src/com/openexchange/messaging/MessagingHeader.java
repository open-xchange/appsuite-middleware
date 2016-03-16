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
