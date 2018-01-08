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

package com.openexchange.chronos.json.converter.mapper;

import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

/**
 * {@link ColumnIDs}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ColumnIDs {

    /**
     * The column identifier denoting the object identifier of the event.
     */
    static final int ID = DataObject.OBJECT_ID;
    /**
     * The column identifier denoting the parent folder identifier representing the view on the event.
     */
    static final int FOLDER_ID = FolderChildObject.FOLDER_ID;
    /**
     * The column identifier denoting the universal identifier of the event.
     */
    static final int UID = Appointment.UID;
    /**
     * The column identifier denoting the filename of the event.
     */
    static final int FILENAME = Appointment.FILENAME;
    /**
     * The column identifier denoting the sequence number of the event.
     */
    static final int SEQUENCE = Appointment.SEQUENCE;
    /**
     * The column identifier denoting the creation date of the event.
     */
    static final int CREATED = FolderChildObject.CREATION_DATE;
    /**
     * The column identifier denoting the calendar user of the event's creator.
     */
    static final int CREATED_BY = FolderChildObject.CREATED_BY;
    /**
     * The column identifier denoting the last modification date of the event.
     */
    static final int LAST_MODIFIED = FolderChildObject.LAST_MODIFIED;
    /**
     * The column identifier denoting the calendar user who last modified the event.
     */
    static final int MODIFIED_BY = FolderChildObject.MODIFIED_BY;
    /**
     * The column identifier denoting the calendar user of the event.
     */
    static final int CALENDAR_USER = Appointment.PRINCIPAL;
    /**
     * The column identifier denoting the summary of the event.
     */
    static final int SUMMARY = Appointment.TITLE;
    /**
     * The column identifier denoting the location of the event.
     */
    static final int LOCATION = Appointment.LOCATION;
    /**
     * The column identifier denoting the description of the event.
     */
    static final int DESCRIPTION = Appointment.NOTE;
    /**
     * The column identifier denoting the categories of the event.
     */
    static final int CATEGORIES = CommonObject.CATEGORIES;
    /**
     * The column identifier denoting the classification of the event.
     */
    static final int CLASSIFICATION = 261;
    /**
     * The column identifier denoting the color of the event.
     */
    static final int COLOR = CommonObject.COLOR_LABEL;
    /**
     * The column identifier denoting the start date of the event.
     */
    static final int START_DATE = Appointment.START_DATE;
    /**
     * The column identifier denoting the end date of the event.
     */
    static final int END_DATE = Appointment.END_DATE;
    /**
     * The column identifier denoting the time transparency of the event.
     */
    static final int TRANSP = Appointment.SHOWN_AS;
    /**
     * The column identifier denoting the series identifier of the event.
     */
    static final int SERIES_ID = Appointment.RECURRENCE_ID;
    /**
     * The column identifier denoting the recurrence rule of the event.
     */
    static final int RECURRENCE_RULE = Appointment.RECURRENCE_TYPE;
    /**
     * The column identifier denoting the recurrence identifier of the event.
     */
    static final int RECURRENCE_ID = Appointment.RECURRENCE_DATE_POSITION;
    /**
     * The column identifier denoting the change exception dates of the event.
     */
    static final int CHANGE_EXCEPTION_DATES = Appointment.CHANGE_EXCEPTIONS;
    /**
     * The column identifier denoting the delete exception dates of the event.
     */
    static final int DELETE_EXCEPTION_DATES = Appointment.DELETE_EXCEPTIONS;
    /**
     * The column identifier denoting the status of the event.
     */
    static final int STATUS = 263;
    /**
     * The column identifier denoting the organizer of the event.
     */
    static final int ORGANIZER = Appointment.ORGANIZER;
    /**
     * The column identifier denoting the attendees of the event.
     */
    static final int ATTENDEES = Appointment.PARTICIPANTS;
    /**
     * The column identifier denoting the attachments of the event.
     */
    static final int ATTACHMENTS = 264;
    /**
     * The column identifier denoting the alarms of the event.
     */
    static final int ALARMS = Appointment.ALARM;
    /**
     * The column identifier for the extended properties of the event.
     */
    static final int EXTENDED_PROPERTIES = Appointment.EXTENDED_PROPERTIES;
    /**
     * The column identifier for the flags of the event.
     */
    static final int FLAGS = 265;

}
