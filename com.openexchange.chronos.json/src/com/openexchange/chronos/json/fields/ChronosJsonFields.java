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

package com.openexchange.chronos.json.fields;

import com.openexchange.chronos.Event;
import com.openexchange.chronos.json.converter.mapper.EventMapper;

/**
 * {@link ChronosJsonFields} contains all fields which are used by the {@link EventMapper}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ChronosJsonFields {

    /**
     * The id of the event. See {@link Event#getId()}
     */
    public static final String ID = "id";
    /**
     * The folder id of the event. See {@link Event#getFolderId()}
     */
    public static final String FOLDER = "folder";
    /**
     * The uid of the event. See {@link Event#getUid()}
     */
    public static final String UID = "uid";
    /**
     * The filename of the event. See {@link Event#getFilename()}
     */
    public static final String FILENAME = "filename";
    /**
     * The sequence of the event. See {@link Event#getSequence()}
     */
    public static final String SEQUENCE = "sequence";
    /**
     * The timestamp of the event. See {@link Event#getTimestamp()}
     */
    public static final String TIMESTAMP = "timestamp";
    /**
     * The creation date of the event. See {@link Event#getCreated()}
     */
    public static final String CREATED = "created";
    /**
     * The creator of the event. See {@link Event#getCreatedBy()}
     */
    public static final String CREATED_BY = "createdBy";
    /**
     * The lastModified timestamp of the event. See {@link Event#getLastModified()}
     */
    public static final String LAST_MODIFIED = "lastModified";
    /**
     * The last modifier of the event. See {@link Event#getModifiedBy()}
     */
    public static final String MODIFIED_BY = "modifiedBy";
    /**
     * The calendar user of the event. See {@link Event#getCalendarUser()}
     */
    public static final String CALENDAR_USER = "calendarUser";
    /**
     * The summary of the event. See {@link Event#getSummary()}
     */
    public static final String SUMMARY = "summary";
    /**
     * The location of the event. See {@link Event#getLocation()}
     */
    public static final String LOCATION = "location";
    /**
     * The description of the event. See {@link Event#getDescription()}
     */
    public static final String DESCRIPTION = "description";
    /**
     * The categories of the event. See {@link Event#getCategories()}
     */
    public static final String CATEGORIES = "categories";
    /**
     * The class of the event. See {@link Event#getClassification()}
     */
    public static final String CLASSIFICATION = "class";
    /**
     * The color of the event. See {@link Event#getColor()}
     */
    public static final String COLOR = "color";
    /**
     * The starting date of the event. See {@link Event#getStartDate()}
     */
    public static final String START_DATE = "startDate";
    /**
     * The end date of the event. See {@link Event#getEndDate()()}
     */
    public static final String END_DATE = "endDate";
    /**
     * The transparency of the event. See {@link Event#getTransp()}
     */
    public static final String TRANSP = "transp";
    /**
     * The seriesId of the event. See {@link Event#getSeriesId()}
     */
    public static final String SERIES_ID = "seriesId";
    /**
     * The recurrence rule of the event. See {@link Event#getRecurrenceRule()}
     */
    public static final String RECURRENCE_RULE = "rrule";
    /**
     * The recurrence id of the event. See {@link Event#getRecurrenceId()}
     */
    public static final String RECURRENCE_ID = "recurrenceId";
    /**
     * The recurrence dates of the event. See {@link Event#getRecurrenceDates()}
     */
    public static final String RECURRENCE_DATES = "recurrenceDates";
    /**
     * The change exception dates of the event. See {@link Event#getChangeExceptionDates()}
     */
    public static final String CHANGE_EXCEPTION_DATES = "changeExceptionDates";
    /**
     * The delete exception dates of the event. See {@link Event#getDeleteExceptionDates()}
     */
    public static final String DELETE_EXCEPTION_DATES = "deleteExceptionDates";
    /**
     * The status of the event. See {@link Event#getStatus()}
     */
    public static final String STATUS = "status";
    /**
     * The url of the event. See {@link Event#getUrl()}
     */
    public static final String URL = "url";
    /**
     * The organizer of the event. See {@link Event#getOrganizer()}
     */
    public static final String ORGANIZER = "organizer";
    /**
     * The optional comment a organizer can set.
     */
    public static final String COMMENT = "comment";
    /**
     * The geolocation of the event. See {@link Event#getGeo()}
     */
    public static final String GEO = "geo";
    /**
     * The privileges attendees have to modify the event. See {@link Event#getAttendeePrivileges()}
     */
    public static final String ATTENDEE_PRIVILEGES = "attendeePrivileges";

    /**
     *
     * {@link Geo} contains fields of the Geo json object. See {@link ChronosJsonFields#GEO}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public static final class Geo {

        /**
         * The geographic latitude of the geolocation.
         */
        public static final String LATITUDE = "lat";
        /**
         * The geographic longitude of the geolocation.
         */
        public static final String LONGITUDE = "long";
    }

    /**
     * The attendees of the event. See {@link Event#getAttendees()}
     */
    public static final String ATTENDEES = "attendees";

    /**
     *
     * {@link CalendarUser} contains fields of the CalendarUser json object. E.g. see {@link ChronosJsonFields#ORGANIZER}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public static class CalendarUser {

        /**
         * The uri of the calendar user. See {@link com.openexchange.chronos.CalendarUser#getUri()}
         */
        public static final String URI = "uri";
        /**
         * The common name of the calendar user. See {@link com.openexchange.chronos.CalendarUser#getCn()}
         */
        public static final String CN = "cn";
        /**
         * The email address of the calendar user. See {@link com.openexchange.chronos.CalendarUser#getEMail()}
         */
        public static final String EMAIL = "email";
        /**
         * The sent by of the calendar user. See {@link com.openexchange.chronos.CalendarUser#getSentBy()}
         */
        public static final String SENT_BY = "sentBy";
        /**
         * The entity of the calendar user. See {@link com.openexchange.chronos.CalendarUser#getEntity()}
         */
        public static final String ENTITY = "entity";
    }

    /**
     *
     * {@link Attendee} contains fields of the Attendee json object. See {@link ChronosJsonFields#ATTENDEES}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public static final class Attendee extends CalendarUser {

        /**
         * The type of the attendee. See {@link com.openexchange.chronos.Attendee#getCuType()}
         */
        public static final String CU_TYPE = "cuType";
        /**
         * The role of the attendee. See {@link com.openexchange.chronos.Attendee#getRole()}
         */
        public static final String ROLE = "role";
        /**
         * The participation status of the attendee. See {@link com.openexchange.chronos.Attendee#getPartStat()}
         */
        public static final String PARTICIPATION_STATUS = "partStat";
        /**
         * The comment of the attendee. See {@link com.openexchange.chronos.Attendee#getComment()}
         */
        public static final String COMMENT = "comment";
        /**
         * The rsvp of the attendee. See {@link com.openexchange.chronos.Attendee#getRsvp()}
         */
        public static final String RSVP = "rsvp";
        /**
         * The folder id of the attendee. See {@link com.openexchange.chronos.Attendee#getFolderId()}
         */
        public static final String FOLDER = "folder";
        /**
         * The member of the attendee. See {@link com.openexchange.chronos.Attendee#getMember()}
         */
        public static final String MEMBER = "member";
        /**
         * The contact of the attendee.
         */
        public static final String CONTACT = "contact";
        /**
         * The resource of the attendee.
         */
        public static final String RESOURCE = "resource";
        /**
         * The group of the attendee.
         */
        public static final String GROUP = "group";

    }

    /**
     * The attachments of the event. See {@link Event#getAttachments()}
     */
    public static final String ATTACHMENTS = "attachments";

    /**
     *
     * {@link Attachment} contains fields of the Attachment json object. See {@link ChronosJsonFields#ATTACHMENTS}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public static final class Attachment {

        /**
         * The filename of the attachment. See {@link com.openexchange.chronos.Attachment#getFilename()}
         */
        public static final String FILENAME = "filename";
        /**
         * The format type of the attachment. See {@link com.openexchange.chronos.Attachment#getFormatType()}
         */
        public static final String FORMAT_TYPE = "fmtType";
        /**
         * The size of the attachment. See {@link com.openexchange.chronos.Attachment#getSize()}
         */
        public static final String SIZE = "size";
        /**
         * The creation time of the attachment. See {@link com.openexchange.chronos.Attachment#getCreated()}
         */
        public static final String CREATED = "created";
        /**
         * The managed id of the attachment. See {@link com.openexchange.chronos.Attachment#getManagedId()}
         */
        public static final String MANAGED_ID = "managedId";

        /**
         * A uniform resource identifier to the attachment file. See {@link com.openexchange.chronos.Attachment#getUri()}
         */
        public static final String URI = "uri";

    }

    /**
     * The alarms of the event. See {@link Event#getAlarms()}
     */
    public static final String ALARMS = "alarms";

    /**
     *
     * {@link Alarm} contains fields of the Alarm json object. See {@link ChronosJsonFields#ALARMS}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public static final class Alarm {

        /**
         * The id of the alarm. See {@link com.openexchange.chronos.Alarm#getId()}
         */
        public static final String ID = "id";
        /**
         * The uid of the alarm. See {@link com.openexchange.chronos.Alarm#getUid()}
         */
        public static final String UID = "uid";
        /**
         * The acknowledged of the alarm. See {@link com.openexchange.chronos.Alarm#getRelatedTo()}
         */
        public static final String RELATED_TO = "relatedTo";
        /**
         * The acknowledged of the alarm. See {@link com.openexchange.chronos.Alarm#getAcknowledged()}
         */
        public static final String ACK = "acknowledged";
        /**
         * The action of the alarm. See {@link com.openexchange.chronos.Alarm#getAction()}
         */
        public static final String ACTION = "action";
        /**
         * The trigger of the alarm. See {@link com.openexchange.chronos.Alarm#getTrigger()}
         */
        public static final String TRIGGER = "trigger";

        /**
         *
         * {@link Trigger} contains fields of the Trigger json object. See {@link Alarm#TRIGGER}
         *
         * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
         * @since v7.10.0
         */
        public static final class Trigger {

            /**
             * The related of the trigger. See {@link com.openexchange.chronos.Trigger#getRelated()}
             */
            public static final String RELATED = "related";
            /**
             * The duration of the trigger. See {@link com.openexchange.chronos.Trigger#getDuration()}
             */
            public static final String DURATION = "duration";
            /**
             * The dateTime of the trigger. See {@link com.openexchange.chronos.Trigger#getDateTime()}
             */
            public static final String DATE_TIME = "dateTime";
        }

        /**
         * The attachments of the alarm. See {@link com.openexchange.chronos.Alarm#getAttachments()}
         */
        public static final String ATTACHMENTS = "attachments";
        /**
         * The attendees of the alarm. See {@link com.openexchange.chronos.Alarm#getAttendees()}
         */
        public static final String ATTENDEES = "attendees";
        /**
         * The summary of the alarm. See {@link com.openexchange.chronos.Alarm#getSummary()}
         */
        public static final String SUMMARY = "summary";
        /**
         * The description of the alarm. See {@link com.openexchange.chronos.Alarm#getDescription()}
         */
        public static final String DESCRIPTION = "description";
        /**
         * The extended properties of the alarm. See {@link com.openexchange.chronos.Alarm#getExtendedProperties()}
         */
        public static final String EXTENDED_PROPERTIES = "extendedProperties";
    }

    /**
     * The extended properties of the event. See {@link Event#getExtendedProperties()}
     */
    public static final String EXTENDED_PROPERTIES = "extendedProperties";

    /**
     * The flags of the event. See {@link Event#getFlags()}
     */
    public static final String FLAGS = "flags";

    /**
     *
     * {@link ExtendedProperty} contains fields of the ExtendedProperty json object. See {@link ChronosJsonFields#EXTENDED_PROPERTIES}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public static final class ExtendedProperty {

        /**
         * The name of the extended property. See {@link com.openexchange.chronos.ExtendedProperty#getName()}
         */
        public static final String NAME = "name";
        /**
         * The value of the extended property. See {@link com.openexchange.chronos.ExtendedProperty#getValue()}
         */
        public static final String VALUE = "value";

    }
}
