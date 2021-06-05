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
     * The conferences of the event.
     */
    CONFERENCES,
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
