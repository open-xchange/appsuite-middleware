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

package com.openexchange.chronos;

/**
 * {@link EventField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum EventField {

    /**
     * The object identifier of the event.
     */
    ID,
    /**
     * The parent folder identifier representing the view on the event.
     */
    FOLDER_ID,
    /**
     * The universal identifier of the event.
     */
    UID,
    /**
     * The relationship between this and other events.
     */
    RELATED_TO,
    /**
     * The filename of the event.
     */
    FILENAME,
    /**
     * The sequence number of the event.
     */
    SEQUENCE,
    /**
     * The timestamp of the event.
     */
    TIMESTAMP,
    /**
     * The creation date of the event.
     */
    CREATED,
    /**
     * The calendar user that initially created the event.
     */
    CREATED_BY,
    /**
     * The last modification date of the event.
     */
    LAST_MODIFIED,
    /**
     * The calendar user that performed the last modification of the event.
     */
    MODIFIED_BY,
    /**
     * The calendar user of the event.
     */
    CALENDAR_USER,
    /**
     * The summary of the event.
     */
    SUMMARY,
    /**
     * The location of the event.
     */
    LOCATION,
    /**
     * The description of the event.
     */
    DESCRIPTION,
    /**
     * The categories of the event.
     */
    CATEGORIES,
    /**
     * The classification of the event.
     */
    CLASSIFICATION,
    /**
     * The color of the event.
     */
    COLOR,
    /**
     * The uniform resource locator (URL) of the event.
     */
    URL,
    /**
     * The global position of the event.
     */
    GEO,
    /**
     * The privileges attendees have to modify the event.
     */
    ATTENDEE_PRIVILEGES,
    /**
     * The start date of the event.
     */
    START_DATE,
    /**
     * The end date of the event.
     */
    END_DATE,
    /**
     * The time transparency of the event.
     */
    TRANSP,
    /**
     * The series identifier of the event.
     */
    SERIES_ID,
    /**
     * The recurrence rule of the event.
     */
    RECURRENCE_RULE,
    /**
     * The recurrence identifier of the event.
     */
    RECURRENCE_ID,
    /**
     * The recurrence dates of the event.
     */
    RECURRENCE_DATES,
    /**
     * The change exception dates of the event.
     */
    CHANGE_EXCEPTION_DATES,
    /**
     * The delete exception dates of the event.
     */
    DELETE_EXCEPTION_DATES,
    /**
     * The status of the event.
     */
    STATUS,
    /**
     * The organizer of the event.
     */
    ORGANIZER,
    /**
     * The attendees of the event.
     */
    ATTENDEES,
    /**
     * The attachments of the event.
     */
    ATTACHMENTS,
    /**
     * The alarms of the event.
     */
    ALARMS,
    /**
     * Extended properties of the event.
     */
    EXTENDED_PROPERTIES,
    /**
     * Flags of the event.
     */
    FLAGS,
    ;
}
