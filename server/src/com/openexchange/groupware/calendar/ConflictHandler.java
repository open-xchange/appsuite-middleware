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

package com.openexchange.groupware.calendar;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ConflictHandler
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class ConflictHandler {
    
    private CalendarDataObject cdao;
    private SessionObject so;
    private boolean action = true;
    private int current_results;
    private User u;
    
    private Date rs;
    private Date re;
    
    public static final int MAX_CONFLICT_RESULTS = 999;
    
    public static final CalendarDataObject NO_CONFLICTS[] = new CalendarDataObject[0];
    
    private static final Log LOG = LogFactory.getLog(ConflictHandler.class);
    
    public ConflictHandler(final CalendarDataObject cdao, final SessionObject so, final boolean action) {
        this.cdao = cdao;
        this.so = so;
        this.action = action;
    }
    
    private final User getUser() {
    	if (null == u) {
    		u = UserStorage.getUser(so.getUserId(), so.getContext());
    	}
    	return u;
    }
    
    public CalendarDataObject[] getConflicts() throws OXException {
        if (cdao.getShownAs() == CalendarDataObject.FREE || !UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), so.getContext()).hasConflictHandling()) {
            return NO_CONFLICTS; // According to bug #5267 and modularisation concept
        } else if (!action && !cdao.containsStartDate() && !cdao.containsEndDate() && !cdao.containsParticipants() && !cdao.containsRecurrenceType()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Ignoring conflict checks because we detected an update and no start/end time, recurrence type or participants are changed!");
            }
            return NO_CONFLICTS;
        } else if (cdao.containsEndDate() && CalendarCommonCollection.checkMillisInThePast(cdao.getEndDate().getTime())) {
            return NO_CONFLICTS; // Past apps should never conflict
        } else if (!action && !cdao.containsShownAs() && (cdao.getShownAs() == CalendarDataObject.FREE)) {
            //if (cdao.getShownAs() == CalendarDataObject.FREE) {
            return NO_CONFLICTS; // According to bug #5267
            //}
        }
        if (!containsResources()) {
            if (!cdao.getIgnoreConflicts()) {
                if (cdao.getRecurrenceType() == 0) {
                    return prepareResolving(true);
                }
                return NO_CONFLICTS;
            }
            return NO_CONFLICTS;
        }
        rs = cdao.getStartDate();
        re = cdao.getEndDate();
        final CalendarDataObject[] resources = prepareResolving(false);
        if (resources.length > 0) {
            return resources;
        }
        if (!cdao.getIgnoreConflicts()) {
            return prepareResolving(true);
        }
        return NO_CONFLICTS;
    }
    
    private CalendarDataObject[] prepareResolving(final boolean request_participants) throws OXException {
        if (cdao.getRecurrenceType() == 0) {
            if (action) {
                if (request_participants) {
                    return resolveParticipantConflicts(cdao.getStartDate(), cdao.getEndDate());
                }
                return resolveResourceConflicts(cdao.getStartDate(), cdao.getEndDate());
            }
            if (request_participants) {
                return resolveParticipantConflicts(cdao.getStartDate(), cdao.getEndDate());
            }
            return resolveResourceConflicts(cdao.getStartDate(), cdao.getEndDate());
        }
        if (request_participants) {
            return NO_CONFLICTS;
        }
        CalendarRecurringCollection.fillDAO(cdao);
        RecurringResults rrs;
        if (rs == null || re == null) {
            rrs = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        } else {
            rrs = CalendarRecurringCollection.calculateRecurring(cdao, rs.getTime(), re.getTime(), 0);
        }
        CalendarDataObject multi[] = new CalendarDataObject[0];
        for (int a = 0; a < rrs.size(); a++) {
            final RecurringResult rs = rrs.getRecurringResult(a);
            final CalendarDataObject temp[] = resolveResourceConflicts(new Date(rs.getStart()), new Date(rs.getEnd()));
            multi = CalendarCommonCollection.copyAndExpandCalendarDataObjectArray(temp, multi);
            
        }
        return multi;
    }
    
    private CalendarDataObject[] resolveParticipantConflicts(final Date start, final Date end) throws OXException {
        final String sql_in = CalendarCommonCollection.getSQLInStringForParticipants(cdao.getUsers());
        if (sql_in == null) {
            return NO_CONFLICTS;
        }
        final CalendarSqlImp calendarsqlimp = CalendarSql.getCalendarSqlImplementation();
        Connection readcon = null;
        SearchIterator<?> si = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        PreparedStatement private_folder_information = null;
        boolean close_connection = true;
        try {
            readcon = DBPool.pickup(so.getContext());            
            long whole_day_start = CalendarCommonCollection.getUserTimeUTCDate(start, getUser().getTimeZone());
            long whole_day_end = CalendarCommonCollection.getUserTimeUTCDate(end, getUser().getTimeZone());
            if (whole_day_end <= whole_day_start) {
                whole_day_end = whole_day_start+CalendarRecurringCollection.MILLI_DAY;
            }
            prep = calendarsqlimp.getConflicts(so.getContext(), start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in, true);
            private_folder_information = calendarsqlimp.getConflicts(so.getContext(), start, end, null, null, readcon, sql_in, false);
            rs = calendarsqlimp.getResultSet(prep);
            si = new FreeBusyResults(rs, prep, so.getContext(), getUser().getId(), getUser().getGroups(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), so.getContext()), readcon, true, cdao.getUsers(), private_folder_information);
            ArrayList<CalendarDataObject> li = null;
            while (si.hasNext()) {
                final CalendarDataObject conflict_dao = (CalendarDataObject) si.next();
                if (conflict_dao != null && conflict_dao.containsStartDate() && conflict_dao.containsEndDate()) {
                    if (li == null) {
                        li = new ArrayList<CalendarDataObject>();
                    }
                    
                    if (!(!action && cdao.getObjectID() == conflict_dao.getObjectID())) { // Same id should never conflict if we are running an update
                        if (!conflict_dao.containsRecurrencePosition()) {
                            if (!CalendarCommonCollection.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                li.add(conflict_dao);
                                current_results++;
                            }
                        } else if (conflict_dao.getRecurrencePosition() > 0 && CalendarCommonCollection.inBetween(start.getTime(), end.getTime(), conflict_dao.getStartDate().getTime(), conflict_dao.getEndDate().getTime())) {
                            if (!CalendarCommonCollection.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
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
        } catch (final SearchIteratorException sie) {
            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, sie, 12);
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (close_connection && si != null) {
                try {
                    si.close();
                } catch (final SearchIteratorException sie) {
                    LOG.error("Error closing SearchIterator" ,sie);
                }
                CalendarCommonCollection.closeResultSet(rs);
                CalendarCommonCollection.closePreparedStatement(prep);
                CalendarCommonCollection.closePreparedStatement(private_folder_information);
            }
            if (close_connection && readcon != null) {
                try {
                    DBPool.push(so.getContext(), readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("error pushing readable connection" ,dbpe);
                }
            }
        }
    }
    
    private CalendarDataObject[] resolveResourceConflicts(final Date start, final Date end) throws OXException {
        final String sql_in = CalendarCommonCollection.getSQLInStringForResources(cdao.getParticipants());
        if (sql_in == null) {
            return NO_CONFLICTS;
        }
        final CalendarSqlImp calendarsqlimp = CalendarSql.getCalendarSqlImplementation();
        Connection readcon = null;
        SearchIterator<?> si = null;
        ResultSet rs = null;
        PreparedStatement prep  = null;
        PreparedStatement private_folder_information = null;
        boolean close_connection = true;
        try {
            readcon = DBPool.pickup(so.getContext());
            long whole_day_start = CalendarCommonCollection.getUserTimeUTCDate(start, getUser().getTimeZone());
            long whole_day_end = CalendarCommonCollection.getUserTimeUTCDate(end, getUser().getTimeZone());
            if (whole_day_end <= whole_day_start) {
                whole_day_end = whole_day_start+CalendarRecurringCollection.MILLI_DAY;
            }
            prep = calendarsqlimp.getResourceConflicts(so.getContext(), start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in);
            private_folder_information = calendarsqlimp.getResourceConflictsPrivateFolderInformation(so.getContext(), start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, sql_in);
            rs = calendarsqlimp.getResultSet(prep);
            si = new FreeBusyResults(rs, prep, so.getContext(), getUser().getId(), getUser().getGroups(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), so.getContext()), readcon, true, cdao.getParticipants(), private_folder_information);
            ArrayList<CalendarDataObject> li = null;
            while (si.hasNext()) {
                final CalendarDataObject conflict_dao = (CalendarDataObject)si.next();
                if (conflict_dao != null && conflict_dao.containsStartDate() && conflict_dao.containsEndDate()) {
                    if (li == null) {
                        li = new ArrayList<CalendarDataObject>();
                    }
                    
                    if (!(!action && cdao.getObjectID() == conflict_dao.getObjectID())) { // Same id should never conflict if we are running an update
                        if (!cdao.containsRecurrencePosition()) {
                            if (!CalendarCommonCollection.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                if (!conflict_dao.containsRecurrencePosition()) {
                                    conflict_dao.setHardConflict();
                                    li.add(conflict_dao);
                                    current_results++;
                                } else if (conflict_dao.getRecurrencePosition() > 0 && CalendarCommonCollection.inBetween(start.getTime(), end.getTime(), conflict_dao.getStartDate().getTime(), conflict_dao.getEndDate().getTime())) {
                                    if (!CalendarCommonCollection.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
                                        conflict_dao.setHardConflict();
                                        li.add(conflict_dao);
                                        current_results++;
                                    }
                                }
                            }
                        } else if (cdao.getRecurrencePosition() > 0 && CalendarCommonCollection.inBetween(start.getTime(), end.getTime(), conflict_dao.getStartDate().getTime(), conflict_dao.getEndDate().getTime())) {
                            if (!CalendarCommonCollection.checkMillisInThePast(conflict_dao.getEndDate().getTime())) {
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
        } catch (final SearchIteratorException sie) {
            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, sie, 13);
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (close_connection && si != null) {
                try {
                    si.close();
                } catch (final SearchIteratorException sie) {
                    LOG.error("Error closing SearchIterator" ,sie);
                }
                CalendarCommonCollection.closeResultSet(rs);
                CalendarCommonCollection.closePreparedStatement(prep);
                CalendarCommonCollection.closePreparedStatement(private_folder_information);
            }
            if (close_connection && readcon != null) {
                try {
                    DBPool.push(so.getContext(), readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("error pushing readable connection" ,dbpe);
                }
            }
        }
    }
    
    private final boolean containsResources() {
        return cdao.containsResources();
    }
    
}
