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

package com.openexchange.webdav.action;

/**
 * {@link WebdavOption} contains all known webdav options
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public enum WebdavOption {

    /**
     * Compliant class 1
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-18.1">RFC 4918 Section 18.1</a>
     */
    ONE("1"),

    /**
     * Compliant class 2
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-18.2">RFC 4918 Section 18.2</a>
     */
    TWO("2"),

    /**
     * Compliant class 3
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-18.3">RFC 4918 Section 18.3</a>
     */
    THREE("3"),

    /**
     * Header announcing the server can "provide an interoperable mechanism for handling discretionary
     * access control for content and metadata managed by WebDAV servers"
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-1">RFC 3744 Section 1</a>
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-7.2">RFC 3744 Section 7.2</a>
     */
    ACCESS_CONTROL("access-control"),

    /**
     * Header announcing that the server is capable "of accessing, managing, and sharing calendaring
     * and scheduling information based on the iCalendar format" over WebDAV
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-5">RFC 4791 Section 5</a>
     */
    CALENDAR_ACCESS("calendar-access"),

    /**
     * Header announcing that the server is capable "of accessing, managing, and sharing contact
     * information based on the vCard format." over WebDAV
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6352#section-6">RFC 6352 Section 6</a>
     */
    ADDRESSBOOK("addressbook"),

    /**
     * Header announcing that server is capable of an extended "MKCOL (Make Collection) method [that]
     * allow collections of arbitrary resourcetype to be created and to allow properties to be set
     * at the same time."
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5689#section-3.1">RFC 5689 Section 3.1</a>
     */
    EXTENDED_MKCOL("extended-mkcol"),

    /**
     * Header announcing that the server is capable "of performing scheduling operations with
     * iCalendar-based calendar components" over WebDAV
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-2">RFC 6638 Section 2</a>
     */
    CALENDAR_AUTO_SCHEDULE("calendar-auto-schedule"),

    /**
     * Header announcing that the server is capable "of exchanging and processing scheduling messages
     * based on the iCalendar Transport-Independent Interoperability Protocol (iTIP)" over WebDAV
     * 
     * @see <a href="https://tools.ietf.org/html/draft-desruisseaux-caldav-sched-04#section-3">Draft CalDAV scheduling Section 3</a>
     */
    CALENDAR_SCHEDULE("calendar-schedule"),

    /**
     * Header announcing that the server is capable to "enable[s] the sharing of calendars between
     * users on a CalDAV server"
     * 
     * @see <a href="https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-sharing.txt#L358">Draft CalDAV Sharing Section 5</a>
     */
    CALENDAR_SERVER_SHARING("calendarserver-sharing"),

    CALENDAR_PRINCIPAL_SEARCH("calendarserver-principal-search"),
    CALENDAR_PRINCIPAL_PROPERTY_SEARCH("calendarserver-principal-property-search"),
    CALENDAR_SERVER_SUBSCRIBED("calendarserver-subscribed"),

    /**
     * Header announcing that the server is capable of "sharing [of] resources between users on a WebDAV server."
     * 
     * @see <a href=https://tools.ietf.org/html/draft-pot-webdav-resource-sharing-04#section-4">Draft WebDAV resource sharing Section 4</a>
     */
    RESOURCE_SHARING("resource-sharing"),

    CALENDAR_MANAGED_ATTACHMENT("calendar-managed-attachments"),

    /**
     * Header announcing that the server is capable to "enables a client to mark events with an access classification (e.g., "private") so that
     * other calendar users have restricted rights to view the data in the calendar component." as CalDAV server
     * 
     * @see <a href="https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-privateevents.txt#L207">Draft CalDAV Private comment Section 4.2</a>
     */
    CALENDAR_SERVER_PRIVATE_EVENTS("calendarserver-private-events"),

    /**
     * Header announcing that the server is capable to "allow[s] calendar clients to split recurring events on the server in such a way as to preserve
     * the original per-attendee data, such as alarms and participation status" as CalDAV server
     * 
     * @see <a href="https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-recursplit.txt#L117">Draft CalDAV Recurring Split Section 3</a>
     */
    CALENDAR_SERVER_RECURRENCE_SPLIT("calendarserver-recurrence-split"),
    ;

    private final String name;

    /**
     * Initializes a new {@link WebdavOption}.
     */
    private WebdavOption(String name) {
        this.name = name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
