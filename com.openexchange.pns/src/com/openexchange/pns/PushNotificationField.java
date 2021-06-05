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

package com.openexchange.pns;


/**
 * {@link PushNotificationField} - The well-known fields for a push notification.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum PushNotificationField {

    // -------------------------------- Generic fields --------------------------------
    /** The key providing the actual message to display; type is <code>java.lang.String</code> */
    MESSAGE("message"),
    /** The item identifier; type is <code>java.lang.String</code> */
    ID("id"),
    /** The folder identifier; type is <code>java.lang.String</code> */
    FOLDER("folder"),
    /** A listing of arguments; type is <code>java.util.List</code> */
    ARGS("args"),

    // -------------------------------- Mail-related fields --------------------------------
    /** The subject of a mail; type is <code>java.lang.String</code> */
    MAIL_SUBJECT("subject"),
    /** The sender's address of a mail; type is <code>java.lang.String</code> */
    MAIL_SENDER_EMAIL("email"),
    /** The sender's address of a mail; type is <code>java.lang.String</code> */
    MAIL_SENDER_PERSONAL("displayname"),
    /** The unread count; type is <code>java.lang.Integer</code> */
    MAIL_UNREAD("unread"),
    /** The key providing the mail path; type is <code>java.lang.String</code> */
    MAIL_PATH("cid"),
    /** The teaser for the mail text; type is <code>java.lang.String</code> */
    MAIL_TEASER("teaser"),

    // -------------------------------- Calendar-related fields ----------------------------
    /** The title of an appointment; type is <code>java.lang.String</code> */
    APPOINTMENT_TITLE("title"),
    /** The location of an appointment; type is <code>java.lang.String</code> */
    APPOINTMENT_LOCATION("location"),
    /** The start date of an appointment; type is <code>java.util.Date</code> */
    APPOINTMENT_START_DATE("start_date"),
    /** The end date of an appointment; type is <code>java.util.Date</code> */
    APPOINTMENT_END_DATE("end_date"),

    ;

    private final String id;

    private PushNotificationField(String id) {
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

}
