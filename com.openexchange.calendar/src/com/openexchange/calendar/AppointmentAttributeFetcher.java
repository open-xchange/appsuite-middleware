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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.search.SearchAttributeFetcher;

/**
 * {@link AppointmentAttributeFetcher}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AppointmentAttributeFetcher implements SearchAttributeFetcher<AppointmentObject> {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AttributeGetter.class);

    private static interface AttributeGetter {

        public Object getObject(AppointmentObject candidate);
    }

    private static final Map<String, AttributeGetter> GETTERS;

    static {
        final Map<String, AttributeGetter> m = new HashMap<String, AttributeGetter>(25);

        m.put(AppointmentFields.FULL_TIME, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Boolean.valueOf(candidate.getFullTime());
            }
        });

        m.put(AppointmentFields.IGNORE_CONFLICTS, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Boolean.valueOf(candidate.getIgnoreConflicts());
            }
        });

        m.put(AppointmentFields.LOCATION, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getLocation();
            }
        });

        m.put(AppointmentFields.SHOW_AS, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getShownAs());
            }
        });

        m.put(AppointmentFields.TIMEZONE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getTimezone();
            }
        });

        /*-
         * Calendar Fields
         */

        m.put(AppointmentFields.ALARM, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getAlarm());
            }
        });

        m.put(AppointmentFields.CHANGE_EXCEPTIONS, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getChangeException();
            }
        });

        m.put(AppointmentFields.CONFIRM_MESSAGE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getConfirmMessage();
            }
        });

        m.put(AppointmentFields.CONFIRMATION, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getConfirm());
            }
        });

        m.put(AppointmentFields.DAY_IN_MONTH, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getDayInMonth());
            }
        });

        m.put(AppointmentFields.DAYS, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getDays());
            }
        });

        m.put(AppointmentFields.DELETE_EXCEPTIONS, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getDeleteException();
            }
        });

        m.put(AppointmentFields.END_DATE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getEndDate();
            }
        });

        m.put(AppointmentFields.INTERVAL, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getInterval());
            }
        });

        m.put(AppointmentFields.MONTH, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getMonth());
            }
        });

        m.put(AppointmentFields.NOTE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getNote();
            }
        });

        m.put(AppointmentFields.NOTIFICATION, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Boolean.valueOf(candidate.getNotification());
            }
        });

        m.put(AppointmentFields.OCCURRENCES, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getOccurrence());
            }
        });

        m.put(AppointmentFields.PARTICIPANTS, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getParticipants();
            }
        });

        m.put(AppointmentFields.RECURRENCE_CALCULATOR, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getRecurrenceCalculator());
            }
        });

        m.put(AppointmentFields.RECURRENCE_DATE_POSITION, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getRecurrenceDatePosition();
            }
        });

        m.put(AppointmentFields.RECURRENCE_ID, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getRecurrenceID());
            }
        });

        m.put(AppointmentFields.RECURRENCE_POSITION, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getRecurrencePosition());
            }
        });

        m.put(AppointmentFields.RECURRENCE_START, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Long.valueOf(candidate.getRecurringStart());
            }
        });

        m.put(AppointmentFields.RECURRENCE_TYPE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getRecurrenceType());
            }
        });

        m.put(AppointmentFields.START_DATE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getStartDate();
            }
        });

        m.put(AppointmentFields.TITLE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getTitle();
            }
        });

        m.put(AppointmentFields.UNTIL, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getUntil();
            }
        });
        m.put(AppointmentFields.USERS, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getUsers();
            }
        });

        /*-
         * Common fields
         */

        m.put(AppointmentFields.CATEGORIES, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getCategories();
            }
        });

        m.put(AppointmentFields.COLORLABEL, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getLabel());
            }
        });

        m.put(AppointmentFields.CREATED_BY, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getCreatedBy());
            }
        });

        m.put(AppointmentFields.CREATION_DATE, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getCreationDate();
            }
        });

        m.put(AppointmentFields.FOLDER_ID, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getParentFolderID());
            }
        });

        m.put(AppointmentFields.ID, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getObjectID());
            }
        });

        m.put(AppointmentFields.LAST_MODIFIED, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return candidate.getLastModified();
            }
        });

        m.put(AppointmentFields.MODIFIED_BY, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Integer.valueOf(candidate.getModifiedBy());
            }
        });

        m.put(AppointmentFields.PRIVATE_FLAG, new AttributeGetter() {

            public Object getObject(final AppointmentObject candidate) {
                return Boolean.valueOf(candidate.getPrivateFlag());
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

    public <T> T getAttribute(final String attributeName, final AppointmentObject candidate) {
        final AttributeGetter getter = GETTERS.get(attributeName);
        if (null == getter) {
            if (LOG.isInfoEnabled()) {
                LOG.info("No getter for field: " + attributeName);
            }
            return null;
        }
        return (T) getter.getObject(candidate);
    }

}
