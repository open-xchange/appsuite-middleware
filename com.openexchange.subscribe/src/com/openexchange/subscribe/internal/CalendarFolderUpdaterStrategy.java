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

package com.openexchange.subscribe.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.search.Order;
import com.openexchange.subscribe.TargetFolderSession;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link CalendarFolderUpdaterStrategy}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CalendarFolderUpdaterStrategy implements FolderUpdaterStrategy<CalendarDataObject> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarFolderUpdaterStrategy.class);

    private static final int SQL_INTERFACE = 1;

    private static final int TARGET = 2;

    private static final int[] COMPARISON_COLUMNS = {
        Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.TITLE, Appointment.START_DATE, Appointment.END_DATE, Appointment.UID, Appointment.NOTE, Appointment.LAST_MODIFIED, Appointment.SEQUENCE };

    @Override
    public int calculateSimilarityScore(final CalendarDataObject original, final CalendarDataObject candidate, final Object session) throws OXException {
        int score = 0;
        // A score of 10 is sufficient for a match
        // If the UID is the same we can assume it is the same event. Please note the UID is assumed to have been saved with contextid and folder-id as prefix here
        final String candidatesUID = getPrefixForUID(original) + candidate.getUid();
        if ((isset(original.getUid()) || isset(candidate.getUid())) && eq(original.getUid(), candidatesUID)) {
            score += 10;
        }
        if ((isset(original.getTitle()) || isset(candidate.getTitle())) && eq(original.getTitle(), candidate.getTitle())) {
            score += 5;
        }
        if ((isset(original.getNote()) || isset(candidate.getNote())) && eq(original.getNote(), candidate.getNote())) {
            score += 3;
        }
        if (original.getStartDate() != null && candidate.getStartDate() != null && eq(original.getStartDate(), candidate.getStartDate())) {
            score += 3;
        }
        if (original.getEndDate() != null && candidate.getEndDate() != null && eq(original.getEndDate(), candidate.getEndDate())) {
            score += 3;
        }

        return score;
    }

    private boolean isset(final String s) {
        return s == null || s.length() > 0;
    }

    protected boolean eq(final Object o1, final Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public void closeSession(final Object session) throws OXException {

    }

    @Override
    public Collection<CalendarDataObject> getData(final TargetFolderDefinition target, final Object session) throws OXException {
        final CalendarSql calendarSql = (CalendarSql) getFromSession(SQL_INTERFACE, session);

        final int folderId = target.getFolderIdAsInt();
        // get all the appointments, from the beginning of time (1970) till the end of time
        final Date startDate = new Date(0);
        final Date endDate = new Date(Long.MAX_VALUE);
        SearchIterator<Appointment> appointmentsInFolder;
        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        try {
            appointmentsInFolder = calendarSql.getAppointmentsBetweenInFolder(folderId, COMPARISON_COLUMNS, startDate, endDate, 0, Order.ASCENDING);

            while (appointmentsInFolder.hasNext()) {
                retval.add((CalendarDataObject) appointmentsInFolder.next());
            }
        } catch (final SQLException e) {
            LOG.error("", e);
        }

        return retval;
    }

    @Override
    public int getThreshold(final Object session) throws OXException {
        return 9;
    }

    @Override
    public boolean handles(final FolderObject folder) {
        return folder.getModule() == FolderObject.CALENDAR;
    }

    @Override
    public void save(final CalendarDataObject newElement, final Object session, Collection<OXException> errors) throws OXException {
        final CalendarSql calendarSql = (CalendarSql) getFromSession(SQL_INTERFACE, session);
        final TargetFolderDefinition target = (TargetFolderDefinition) getFromSession(TARGET, session);
        newElement.setParentFolderID(target.getFolderIdAsInt());
        newElement.setContext(target.getContext());
        addPrefixToUID(newElement);
        calendarSql.insertAppointmentObject(newElement);
    }

    private Object getFromSession(final int key, final Object session) {
        return ((Map<Integer, Object>) session).get(key);
    }

    @Override
    public Object startSession(final TargetFolderDefinition target) throws OXException {
        final Map<Integer, Object> userInfo = new HashMap<Integer, Object>();
        CalendarSql calendarSql = new CalendarSql(new TargetFolderSession(target));
        userInfo.put(SQL_INTERFACE, calendarSql);
        userInfo.put(TARGET, target);
        // Clear Folder

        try {
            calendarSql.deleteAppointmentsInFolder(target.getFolderIdAsInt());
        } catch (SQLException e) {

        }


        return userInfo;
    }

    @Override
    public void update(final CalendarDataObject original, final CalendarDataObject update, final Object session) throws OXException {
        final CalendarSql calendarSql = (CalendarSql) getFromSession(SQL_INTERFACE, session);

        update.setParentFolderID(original.getParentFolderID());
        update.setObjectID(original.getObjectID());
        update.setLastModified(original.getLastModified());
        update.setContext(original.getContext());
        addPrefixToUID(update);

        calendarSql.updateAppointmentObject(update, original.getParentFolderID(), original.getLastModified());
    }

    private void addPrefixToUID (final CalendarDataObject cdo){
                cdo.setUid(getPrefixForUID(cdo) + cdo.getUid());
    }

    private String getPrefixForUID (final CalendarDataObject cdo){
        if (null != cdo.getUid()){
            if (! cdo.getUid().equals("")){
                    return Integer.toString(cdo.getContextID() + cdo.getParentFolderID());
            }
        }
        return "";
    }
}
