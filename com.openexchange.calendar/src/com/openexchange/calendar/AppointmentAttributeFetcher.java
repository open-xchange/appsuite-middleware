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

package com.openexchange.calendar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.search.SearchAttributeFetcher;

/**
 * {@link AppointmentAttributeFetcher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AppointmentAttributeFetcher implements SearchAttributeFetcher<Appointment> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttributeGetter.class);

    private static interface AttributeGetter {

        public Object getObject(Appointment candidate);
    }

    private static final Map<String, AttributeGetter> GETTERS;

    static {
        final Map<String, AttributeGetter> m = new HashMap<String, AttributeGetter>(25);

        m.put(AppointmentFields.FULL_TIME, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Boolean.valueOf(candidate.getFullTime());
            }
        });

        m.put(AppointmentFields.IGNORE_CONFLICTS, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Boolean.valueOf(candidate.getIgnoreConflicts());
            }
        });

        m.put(AppointmentFields.LOCATION, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getLocation();
            }
        });

        m.put(AppointmentFields.SHOW_AS, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getShownAs());
            }
        });

        m.put(AppointmentFields.TIMEZONE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getTimezone();
            }
        });

        /*-
         * Calendar Fields
         */

        m.put(CalendarFields.ALARM, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getAlarm());
            }
        });

        m.put(CalendarFields.CHANGE_EXCEPTIONS, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getChangeException();
            }
        });

        m.put(ParticipantsFields.CONFIRM_MESSAGE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getConfirmMessage();
            }
        });

        m.put(ParticipantsFields.CONFIRMATION, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getConfirm());
            }
        });

        m.put(CalendarFields.DAY_IN_MONTH, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getDayInMonth());
            }
        });

        m.put(CalendarFields.DAYS, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getDays());
            }
        });

        m.put(CalendarFields.DELETE_EXCEPTIONS, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getDeleteException();
            }
        });

        m.put(CalendarFields.END_DATE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getEndDate();
            }
        });

        m.put(CalendarFields.INTERVAL, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getInterval());
            }
        });

        m.put(CalendarFields.MONTH, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getMonth());
            }
        });

        m.put(CalendarFields.NOTE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getNote();
            }
        });

        m.put(CalendarFields.NOTIFICATION, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Boolean.valueOf(candidate.getNotification());
            }
        });

        m.put(CalendarFields.OCCURRENCES, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getOccurrence());
            }
        });

        m.put(CalendarFields.PARTICIPANTS, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getParticipants();
            }
        });

        m.put(CalendarFields.RECURRENCE_CALCULATOR, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getRecurrenceCalculator());
            }
        });

        m.put(CalendarFields.RECURRENCE_DATE_POSITION, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getRecurrenceDatePosition();
            }
        });

        m.put(CalendarFields.RECURRENCE_ID, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getRecurrenceID());
            }
        });

        m.put(CalendarFields.RECURRENCE_POSITION, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getRecurrencePosition());
            }
        });

        m.put(CalendarFields.RECURRENCE_START, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Long.valueOf(candidate.getRecurringStart());
            }
        });

        m.put(CalendarFields.RECURRENCE_TYPE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getRecurrenceType());
            }
        });

        m.put(CalendarFields.START_DATE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getStartDate();
            }
        });

        m.put(CalendarFields.TITLE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getTitle();
            }
        });

        m.put(CalendarFields.UNTIL, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getUntil();
            }
        });
        m.put(CalendarFields.USERS, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getUsers();
            }
        });

        /*-
         * Common fields
         */

        m.put(CommonFields.CATEGORIES, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getCategories();
            }
        });

        m.put(CommonFields.COLORLABEL, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getLabel());
            }
        });

        m.put(DataFields.CREATED_BY, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getCreatedBy());
            }
        });

        m.put(DataFields.CREATION_DATE, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getCreationDate();
            }
        });

        m.put(FolderChildFields.FOLDER_ID, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getParentFolderID());
            }
        });

        m.put(DataFields.ID, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getObjectID());
            }
        });

        m.put(DataFields.LAST_MODIFIED, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getLastModified();
            }
        });

        m.put(DataFields.MODIFIED_BY, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Integer.valueOf(candidate.getModifiedBy());
            }
        });

        m.put(CommonFields.PRIVATE_FLAG, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return Boolean.valueOf(candidate.getPrivateFlag());
            }
        });

        m.put(CommonFields.EXTENDED_PROPERTIES, new AttributeGetter() {

            @Override
            public Object getObject(final Appointment candidate) {
                return candidate.getExtendedProperties();
            }
        });

        GETTERS = Collections.unmodifiableMap(m);
    }

    private static final AppointmentAttributeFetcher instance = new AppointmentAttributeFetcher();

    /**
     * Gets the contact attribute fetcher instance.
     *
     * @return The contact attribute fetcher instance.
     */
    public static AppointmentAttributeFetcher getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link AppointmentAttributeFetcher}.
     */
    private AppointmentAttributeFetcher() {
        super();
    }

    @Override
    public <T> T getAttribute(final String attributeName, final Appointment candidate) {
        final AttributeGetter getter = GETTERS.get(attributeName);
        if (null == getter) {
            LOG.info("No getter for field: {}", attributeName);
            return null;
        }
        return (T) getter.getObject(candidate);
    }

}
