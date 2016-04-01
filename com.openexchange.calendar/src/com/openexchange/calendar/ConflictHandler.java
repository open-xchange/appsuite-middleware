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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarConfig;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * ConflictHandler
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class ConflictHandler {

    private final CalendarDataObject cdao;
    private final Session so;
    private boolean create = true;
    private int current_results;

    public static final int MAX_CONFLICT_RESULTS = 999;

    public static final CalendarDataObject NO_CONFLICTS[] = new CalendarDataObject[0];

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConflictHandler.class);
    private final CalendarDataObject edao;
    private final CalendarCollection recColl;

    public ConflictHandler(final CalendarDataObject cdao,final CalendarDataObject edao, final Session so, final boolean create) {
        this.cdao = cdao;
        this.edao = edao;
        this.so = so;
        this.create = create;
        this.recColl = new CalendarCollection();
    }

    public CalendarDataObject[] getConflicts() throws OXException {
        final Context ctx = Tools.getContext(so);
        if (isFree() || !UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx).hasConflictHandling()) {
            return NO_CONFLICTS; // According to bug #5267 and modularisation concept
        } else if (!create && !cdao.containsStartDate() && !cdao.containsEndDate() && !cdao.containsParticipants() && !cdao.containsRecurrenceType() && !cdao.containsShownAs()) {
            LOG.debug("Ignoring conflict checks because we detected an update and no start/end time, recurrence type or participants and shown as are changed!");
            return NO_CONFLICTS;
        } else if (cdao.isSingle() && cdao.getEndDate() != null && recColl.checkMillisInThePast(cdao.getEndDate().getTime())) {
            return NO_CONFLICTS; // Past single apps should never conflict
        } else if (cdao.isSequence() && recColl.checkMillisInThePast((cdao.getStartDate() != null ? recColl.getMaxUntilDate(cdao) : recColl.getMaxUntilDate(edao)).getTime())) {
            return NO_CONFLICTS; // Past series apps should never conflict
        } else if (!create && !cdao.containsShownAs() && isFree()) {
            //if (cdao.getShownAs() == CalendarDataObject.FREE) {
            return NO_CONFLICTS; // According to bug #5267
            //}
        }
        // So we'll make a conflict check for real. We'll need start and end time for that.
        if(edao != null && ! cdao.containsStartDate() ) { cdao.setStartDate(edao.getStartDate()); }
        if(edao != null && ! cdao.containsEndDate()) { cdao.setEndDate(edao.getEndDate()); }

        if (!containsResources()) {
        	if (cdao.getIgnoreConflicts()) {
        		return NO_CONFLICTS;
        	}

            return prepareResolving(true);
        }
        final CalendarDataObject[] resources = prepareResolving(false);
        if (resources.length > 0) {
            return resources;
        }
        if (!cdao.getIgnoreConflicts()) {
            return prepareResolving(true);
        }
        return NO_CONFLICTS;
    }

    private boolean isFree() {
        if (cdao.containsShownAs()) {
            return cdao.getShownAs() == Appointment.FREE;
        }
        if (edao != null && edao.getShownAs() == Appointment.FREE) {
            return true;
        }
        return false;
    }

    private CalendarDataObject[] prepareResolving(final boolean request_participants) throws OXException {
        /*
         * Using original method {@link #resolveResourceConflicts(Date, Date)}
         * for non series appointments.
         */
        Date start = cdao.getStartDate() != null ? cdao.getStartDate() : edao.getStartDate();
        Date end = cdao.getEndDate() != null ? cdao.getEndDate() : edao.getEndDate();
        if (cdao.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
            if (request_participants) {
                return resolveParticipantConflicts(start, end);
            }
            return resolveResourceConflicts(start, end);
        }
        if (request_participants) {
            return resolveParticipantsRecurring();
        }

        CalendarDataObject clone = cdao.clone();
        if (edao != null) {
            try (CalendarOperation co = new CalendarOperation()) {
                co.checkUpdateRecurring(clone, edao);
            }
        }
        RecurringResultsInterface results;
        if (edao == null || (0 == clone.getRecurrencePosition() && null == clone.getRecurrenceDatePosition() && recColl.detectTimeChange(clone, edao))) {
            results = recColl.calculateRecurringIgnoringExceptions(cdao, 0, 0, 0);
        } else {
            results = recColl.calculateRecurring(cdao, 0, 0, 0);
        }

        if (results == null || results.size() < 1) {
            LOG.debug("No occurrences for this appointment: " + cdao.toString());
            return new CalendarDataObject[]{};
        }

        final Date resultStart = new Date(results.getRecurringResult(0).getStart());
        final Date resultEnd = new Date(results.getRecurringResult(results.size() - 1).getEnd());
        final CalendarDataObject[] resultConflicts = resolveResourceConflicts(resultStart, resultEnd, results);
        // Results must be sorted afterwards because already existing series
        // appointments are returned in time reverse order by FreeBusyResults.
        // Because of 999 maximum number of conflicts the returned array may
        // contain a lot of appointments far in the future.
        Arrays.sort(resultConflicts, new Comparator<CalendarDataObject>() {
            @Override
            public int compare(final CalendarDataObject cdao1, final CalendarDataObject cdao2) {
                return cdao1.getStartDate().compareTo(cdao2.getStartDate());
            }
        });
        return resultConflicts;
	}

    private CalendarDataObject[] resolveParticipantsRecurring() throws OXException {
        long now = System.currentTimeMillis();
        final long limit = CalendarConfig.getSeriesConflictLimit() ? now + Constants.MILLI_YEAR : 0;
        final RecurringResultsInterface rresults = recColl.calculateRecurring(cdao, now, limit, 0);
        for (int i = 0; i < rresults.size(); i++) {
            final RecurringResultInterface recurringResult = rresults.getRecurringResult(i);
            final CalendarDataObject[] conflicts = resolveParticipantConflicts(new Date(recurringResult.getStart()), new Date(recurringResult.getEnd()));
            if (conflicts.length > 0) {
                return conflicts;
            }
        }

        return NO_CONFLICTS;
    }

    private CalendarDataObject[] resolveParticipantConflicts(final Date start, final Date end) throws OXException {
        final String sql_in = recColl.getSQLInStringForParticipants(getConflictUsers());
        if (sql_in == null) {
            return NO_CONFLICTS;
        }
        final CalendarSqlImp calendarsqlimp = CalendarSql.getCalendarSqlImplementation();
        Connection readcon = null;
        SearchIterator<CalendarDataObject> si = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        SearchIterator<List<Integer>> private_folder_information = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(so);
        final User user = Tools.getUser(so, ctx);
        try {
            readcon = DBPool.pickup(ctx);
            final long whole_day_start = cdao.getFullTime() ? start.getTime() : recColl.getUserTimeUTCDate(start, user.getTimeZone());
            long whole_day_end = cdao.getFullTime() ? end.getTime() : recColl.getUserTimeUTCDate(end, user.getTimeZone());
            if (whole_day_end <= whole_day_start) {
                whole_day_end = whole_day_end+Constants.MILLI_DAY;
            }
            prep = calendarsqlimp.getConflicts(ctx, start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in, true);
            private_folder_information = calendarsqlimp.getAllPrivateAppointmentAndFolderIdsForUser(ctx, user.getId(), readcon);
            rs = calendarsqlimp.getResultSet(prep);
            final long startTime = start.getTime();
            final long endTime = end.getTime();
            si = new FreeBusyResults(rs, prep, ctx, user.getId(), user.getGroups(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx), readcon, true, cdao.getUsers(), private_folder_information, calendarsqlimp, startTime, endTime);
            ArrayList<CalendarDataObject> li = null;
            while (si.hasNext()) {
                final CalendarDataObject conflict_dao = si.next();
                if (conflict_dao != null && conflict_dao.containsStartDate() && conflict_dao.containsEndDate()) {
                    if (li == null) {
                        li = new ArrayList<CalendarDataObject>();
                    }

                    if (shouldConflict(cdao, conflict_dao)) { // Same id should never conflict if we are running an update
                        if (!conflict_dao.containsRecurrencePosition()) {
                            if (!recColl.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                li.add(conflict_dao);
                                current_results++;
                            }
                        } else if (conflict_dao.getRecurrencePosition() > 0 && recColl.inBetween(startTime, endTime, conflict_dao.getStartDate().getTime(), conflict_dao.getEndDate().getTime())) {
                            if (!recColl.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                li.add(conflict_dao);
                                current_results++;
                            }
                        }
                        if (current_results == MAX_CONFLICT_RESULTS) {
                            break;
                        }
                    }
                }
            }
            si.close();
            close_connection = false;
            if (li != null) {
                final CalendarDataObject[] ret = new CalendarDataObject[li.size()];
                li.toArray(ret);
                return ret;
            }
            return NO_CONFLICTS;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            if (close_connection) {
                SearchIterators.close(si);
                recColl.closeResultSet(rs);
                recColl.closePreparedStatement(prep);
                SearchIterators.close(private_folder_information);
            }
            if (close_connection && readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    boolean shouldConflict(final CalendarDataObject cdao, final CalendarDataObject conflictDao) {
        if (create) {
            return true;
        }
        if (cdao.containsRecurrenceID() && cdao.getRecurrenceID() == conflictDao.getObjectID()) {
            return false;
        }
        if (conflictDao.containsRecurrenceID() && conflictDao.getRecurrenceID() == cdao.getObjectID()) {
            return false;
        }
        if (cdao.getObjectID() != conflictDao.getObjectID()) {
            return true;
        }
        return false;
    }

    private List<UserParticipant> getConflictUsers() {
        final List<UserParticipant> relevantUsers = new ArrayList<UserParticipant>();
        if (cdao.getUsers() == null) {
            return relevantUsers;
        }
        for (final UserParticipant user : cdao.getUsers()) {
            switch (user.getConfirm()) {
            case CalendarObject.ACCEPT:
                relevantUsers.add(user);
                break;
            case CalendarObject.NONE:
                if (CalendarConfig.getUndefinedStatusConflict()) {
                    relevantUsers.add(user);
                }
                break;
            case CalendarObject.DECLINE:
            case CalendarObject.TENTATIVE:
            default:
                break;
            }
        }

        return relevantUsers;
    }

    private CalendarDataObject[] resolveResourceConflicts(final Date start, final Date end) throws OXException {
        final String sql_in = recColl.getSQLInStringForResources(cdao.getParticipants());
        if (sql_in == null) {
            return NO_CONFLICTS;
        }
        final CalendarSqlImp calendarsqlimp = CalendarSql.getCalendarSqlImplementation();
        Connection readcon = null;
        SearchIterator<?> si = null;
        ResultSet rs = null;
        PreparedStatement prep  = null;
        SearchIterator<List<Integer>> private_folder_information = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(so);
        final User user = Tools.getUser(so, ctx);
        try {
            readcon = DBPool.pickup(ctx);
            final long whole_day_start = cdao.getFullTime() ? start.getTime() : recColl.getUserTimeUTCDate(start, user.getTimeZone());
            long whole_day_end = cdao.getFullTime() ? end.getTime() : recColl.getUserTimeUTCDate(end, user.getTimeZone());
            if (!cdao.getFullTime() || whole_day_end <= whole_day_start) {
                whole_day_end = whole_day_end+Constants.MILLI_DAY;
            }
            prep = calendarsqlimp.getResourceConflicts(ctx, start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in);
            private_folder_information = calendarsqlimp.getResourceConflictsPrivateFolderInformation(ctx, start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in);
            rs = calendarsqlimp.getResultSet(prep);
            final long startTime = start.getTime();
            final long endTime = end.getTime();
            si = new FreeBusyResults(rs, prep, ctx, user.getId(), user.getGroups(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx), readcon, true, cdao.getParticipants(), private_folder_information, calendarsqlimp, startTime, endTime);
            ArrayList<CalendarDataObject> li = null;
            while (si.hasNext()) {
                final CalendarDataObject conflict_dao = (CalendarDataObject)si.next();
                if (conflict_dao != null && conflict_dao.containsStartDate() && conflict_dao.containsEndDate()) {
                    if (li == null) {
                        li = new ArrayList<CalendarDataObject>();
                    }

                    if (!(!create && cdao.getObjectID() == conflict_dao.getObjectID())) { // Same id should never conflict if we are running an update
                        if (!cdao.containsRecurrencePosition()) {
                            if (!recColl.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                if (!conflict_dao.containsRecurrencePosition()) {
                                    conflict_dao.setHardConflict();
                                    li.add(conflict_dao);
                                    current_results++;
                                } else if (conflict_dao.getRecurrencePosition() > 0 && recColl.inBetween(start.getTime(), end.getTime(), conflict_dao.getStartDate().getTime(), conflict_dao.getEndDate().getTime())) {
                                    if (!recColl.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                        conflict_dao.setHardConflict();
                                        li.add(conflict_dao);
                                        current_results++;
                                    }
                                }
                            }
                        } else if (cdao.getRecurrencePosition() > 0 && recColl.inBetween(start.getTime(), end.getTime(), conflict_dao.getStartDate().getTime(), conflict_dao.getEndDate().getTime())) {
                            if (!recColl.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                conflict_dao.setHardConflict();
                                li.add(conflict_dao);
                                current_results++;
                            }
                        }
                        if (current_results == MAX_CONFLICT_RESULTS) {
                            break;
                        }
                    }
                }
            }
            si.close();
            close_connection = false;
            if (li != null) {
                final CalendarDataObject[] ret = new CalendarDataObject[li.size()];
                li.toArray(ret);
                return ret;
            }
            return NO_CONFLICTS;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            if (close_connection) {
                SearchIterators.close(si);
                recColl.closeResultSet(rs);
                recColl.closePreparedStatement(prep);
                SearchIterators.close(private_folder_information);
            }
            if (close_connection && readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    /**
     * This method searches for booking conflicts if a series appointment contains
     * a resource. Start and end date must be the start and the end of the series.
     * This method then fetches all appointments in that time frame that also book
     * the resource. The occurrences of the series appointment are then iterated
     * using the given RecurringResults for every possible conflict selected from
     * the database. All recurring dates of the series are then checked against
     * every possible conflict from the database and only conflicting time frames
     * are returned as resource booking conflicts. This method is a lot faster
     * that {@link #resolveResourceConflicts(Date, Date)}.
     * @param start Start date of the first occurrence of the series appointment.
     * @param end End date of the last occurence of the series apppointment.
     * @param results Recurring results of the series appointment.
     * @return Conflicting appointments that booked the resource in the same
     * time frame.
     * @throws OXException if some problem occurs.
     */
    private CalendarDataObject[] resolveResourceConflicts(final Date start, final Date end, final RecurringResultsInterface results) throws OXException {
        final String sql_in = recColl.getSQLInStringForResources(cdao.getParticipants());
        if (sql_in == null) {
            return NO_CONFLICTS;
        }
        final CalendarSqlImp calendarsqlimp = CalendarSql.getCalendarSqlImplementation();
        Connection readcon = null;
        SearchIterator<CalendarDataObject> si = null;
        ResultSet rs = null;
        PreparedStatement prep  = null;
        SearchIterator<List<Integer>> private_folder_information = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(so);
        final User user = Tools.getUser(so, ctx);
        try {
            readcon = DBPool.pickup(ctx);
            final long whole_day_start = cdao.getFullTime() ? start.getTime() : recColl.getUserTimeUTCDate(start, user.getTimeZone());
            long whole_day_end = cdao.getFullTime() ? end.getTime() : recColl.getUserTimeUTCDate(end, user.getTimeZone());
            if (!cdao.getFullTime() || whole_day_end <= whole_day_start) {
                whole_day_end = whole_day_end+Constants.MILLI_DAY;
            }
            prep = calendarsqlimp.getResourceConflicts(ctx, start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in);
            private_folder_information = calendarsqlimp.getResourceConflictsPrivateFolderInformation(ctx, start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in);
            rs = calendarsqlimp.getResultSet(prep);
            final long startTime = start.getTime();
            final long endTime = end.getTime();
            si = new FreeBusyResults(rs, prep, ctx, user.getId(), user.getGroups(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx), readcon, true, cdao.getParticipants(), private_folder_information, calendarsqlimp, startTime, endTime);
            final ArrayList<CalendarDataObject> li = new ArrayList<CalendarDataObject>();
            while (si.hasNext()) {
                final CalendarDataObject conflict_dao = si.next();
                if (conflict_dao == null || !conflict_dao.containsStartDate() || !conflict_dao.containsEndDate()) {
                    continue;
                }
                if (recColl.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                    continue;
                }
                if (!create && !shouldConflict(cdao, conflict_dao)) { // Same id should never conflict if we are running an update
                    continue;
                }
                for (int i = 0; i < results.size() && current_results < MAX_CONFLICT_RESULTS; i++) {
                    final RecurringResultInterface result = results.getRecurringResult(i);
                    final long rStart = result.getStart();
                    final long rEnd = result.getEnd();
                    if (recColl.inBetween(rStart, rEnd, conflict_dao.getStartDate().getTime(), conflict_dao.getEndDate().getTime())) {
                        conflict_dao.setHardConflict();
                        li.add(conflict_dao);
                        current_results++;
                    }
                }
                if (current_results >= MAX_CONFLICT_RESULTS) {
                    break;
                }
            }
            si.close();
            close_connection = false;
            if (0 != li.size()) {
                final CalendarDataObject[] ret = new CalendarDataObject[li.size()];
                li.toArray(ret);
                return ret;
            }
            return NO_CONFLICTS;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            if (close_connection) {
                SearchIterators.close(si);
                recColl.closeResultSet(rs);
                recColl.closePreparedStatement(prep);
                SearchIterators.close(private_folder_information);
            }
            if (close_connection && readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    private final boolean containsResources() {
        return cdao.containsResources();
    }
}
