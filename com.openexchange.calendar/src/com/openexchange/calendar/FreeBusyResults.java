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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;


/**
 * FreeBusyResults
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class FreeBusyResults implements SearchIterator<CalendarDataObject> {
    
    public final static int MAX_SHOW_USER_PARTICIPANTS = 5;

    private final List<AbstractOXException> warnings;
    private final ResultSet rs;
    private final Connection con;
    private final Context c;
    private final int uid;
    private int seq = -1;
    private int sa;
    private int ft;
    private int oid;
    private int pflag;
    private int owner;
    private int fid;
    private String title;
    private RecurringResultsInterface rrs;
    private boolean has_next;
    private final boolean show_details;
    private final PreparedStatement prep;
    private CalendarFolderObject cfo;
    private final Participant conflict_objects[];
    private final long range_start;
    private final long range_end;
    
    private int last_up_oid;
    private UserParticipant last_up[];
    private int last_p_oid;
    private Participant last_p[];
    
    private List<CalendarDataObject> al = new ArrayList<CalendarDataObject>(16);
    private int counter;
    
    private PrivateFolderInformationObject[] private_folder_array;
    private final PreparedStatement private_folder_information;

    private final CalendarSqlImp calendarsqlimp;

    private CalendarCollection recColl;
    
    private static final Log LOG = LogFactory.getLog(FreeBusyResults.class);
    
    /*public FreeBusyResults(final ResultSet rs, final PreparedStatement prep, final Context c, final Connection con, final long range_start, final long range_end) throws OXException {
    	this.warnings =  new ArrayList<AbstractOXException>(2);
    	this.rs = rs;
        this.prep = prep;
        this.con = con;
        this.c = c;
        this.range_start = range_start;
        this.range_end = range_end;
        rsNext();
        preFill();
    }*/
    
    public FreeBusyResults(final ResultSet rs, final PreparedStatement prep, final Context c, final int uid, final int groups[], final UserConfiguration uc, final Connection con, final boolean show_details, final Participant conflict_objects[], final PreparedStatement private_folder_information, final CalendarSqlImp calendarsqlimp) throws OXException {
    	this(rs, prep, c, uid, groups, uc, con, show_details, conflict_objects, private_folder_information, calendarsqlimp, 0, 0);
    }

    public FreeBusyResults(final ResultSet rs, final PreparedStatement prep, final Context c, final int uid, final int groups[], final UserConfiguration uc, final Connection con, final boolean show_details, final Participant conflict_objects[], final PreparedStatement private_folder_information, final CalendarSqlImp calendarsqlimp, final long range_start, final long range_end) throws OXException {
    	this.warnings =  new ArrayList<AbstractOXException>(2);
    	this.rs = rs;
        this.prep = prep;
        this.con = con;
        this.c = c;
        this.uid = uid;
        this.show_details = show_details;
        this.conflict_objects = conflict_objects;
        this.calendarsqlimp = calendarsqlimp;
        this.private_folder_information = private_folder_information;
        this.range_start = range_start;
        this.range_end = range_end;
        this.recColl = new CalendarCollection();
        if (show_details) {
            preFillPermissionArray(groups, uc);
            preFillPrivateFolderInformation();
        }
        rsNext();
        preFill();
    }
    
    private final void preFill() throws OXException {
        try {
            while (myhasNext()) {
                final CalendarDataObject o;
                try {
                    o = mynext();
                } catch (final SearchIteratorException sie) {
                    throw new OXException(sie);
                }
                if (o != null) {
                    al.add(o);
                }
            }
        } finally {
            myclose();
        }
    }
    
    public final CalendarDataObject next() {
        return al.get(counter++);
    }
    
    public final CalendarDataObject mynext() throws SearchIteratorException, OXException {
        CalendarDataObject cdao = null;
        if (seq >= 0 && rrs != null) {
            final RecurringResultInterface rr = rrs.getRecurringResult(seq);
            return recurringDAO(rr);
        }
        try {
            if (has_next) {
                cdao = new CalendarDataObject();
                oid = rs.getInt(1);
                cdao.setObjectID(oid);
                final java.util.Date s = rs.getTimestamp(2);
                cdao.setStartDate(s);
                final java.util.Date e = rs.getTimestamp(3);
                cdao.setEndDate(e);
                ft = rs.getInt(4);
                sa = rs.getInt(5);
                cdao.setShownAs(sa);
                title = rs.getString(6);
                fid = rs.getInt(7);
                pflag = rs.getInt(8);
                owner = rs.getInt(9);
                cdao.setCreatedBy(owner);
                final int recid = rs.getInt(10);
                if (!rs.wasNull() && recid == oid) {
                    cdao.setRecurrenceCalculator(rs.getInt(11));
                    cdao.setRecurrence(rs.getString(12));
                    recColl.fillDAO(cdao);
                    cdao.setDelExceptions(rs.getString(13));
                    cdao.setExceptions(rs.getString(14));
                    cdao.setTimezone(rs.getString(15));
                    cdao.setRecurrenceID(recid);
                    if (recColl.fillDAO(cdao)) {
                        try {
                            rrs = recColl.calculateRecurring(cdao, range_start, range_end, 0);
                        } catch (OXException x) {
                            LOG.error("Can not load appointment '"+cdao.getTitle()+"' with id "+cdao.getObjectID()+":"+cdao.getContextID()+" due to invalid recurrence pattern", x);
                            recColl.recoverForInvalidPattern(cdao);
                            seq = -1;
                            rsNext();
                            return cdao;
                        }
                        seq = rrs.size()-1;
                        if (seq >= 0) {
                            final RecurringResultInterface rr = rrs.getRecurringResult(seq);
                            rsNext();
                            return recurringDAO(rr);
                        }
                        rsNext();
                        return null;
                    }

                }
            }
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch(final Exception e) {
            LOG.error("FreeBusyResults calculation problem with oid "+oid+" / "+cdao == null ? "" : cdao.toString() , e);
            throw new SearchIteratorException(SearchIteratorException.SearchIteratorCode.CALCULATION_ERROR, com.openexchange.groupware.EnumComponent.APPOINTMENT, oid, e);
        }
        if (ft != 0 && cdao != null) {
            cdao.setFullTime(true);
        }
        fillDetails(cdao);
        rsNext();
        return cdao;
    }
    
    public int size() {
        throw new UnsupportedOperationException("Mehtod size() not implemented");
    }
    
    public boolean hasSize() {
        return false;
    }

    public void addWarning(final AbstractOXException warning) {
		warnings.add(warning);
	}

	public AbstractOXException[] getWarnings() {
		return warnings.isEmpty() ? null : warnings.toArray(new AbstractOXException[warnings.size()]);
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}
    
    private final CalendarDataObject recurringDAO(final RecurringResultInterface rr) throws OXException {
        if (rr != null) {
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setShownAs(sa);
            cdao.setStartDate(new Date(rr.getStart()));
            cdao.setEndDate(new Date(rr.getEnd()));
            if (ft != 0) {
                cdao.setFullTime(true);
            }
            cdao.setRecurrencePosition(rr.getPosition());
            cdao.setObjectID(oid);
            cdao.setCreatedBy(owner);
            fillDetails(cdao);
            seq--;
            return cdao;
        }
        rsNext();
        return null;
    }

    private void fillDetails(final CalendarDataObject cdao) throws OXException {
        if (show_details) {
            if(checkPermissions()) {
                cdao.setTitle(title);
            }
            final Participants ret = resolveConflictingUserParticipants();
            cdao.setParticipants(ret.getList());
        }
    }
    
    public boolean hasNext() {
        if (!al.isEmpty() && counter < al.size()) {
            return true;
        }
        return false;
    }
    
    public final boolean myhasNext() {
        if (seq >= 0) {
            return true;
        }
        return has_next;
    }
    
    private void rsNext() {
        if (rs != null) {
            try {
                has_next = rs.next();
            } catch (final SQLException sqle) {
                has_next = false;
                LOG.error("Error while getting next result set", sqle);
            }
        }
    }
    
    public void close() {
        al = null;
        title = null;
        rrs = null;
    }
    
    public final void myclose() {
        recColl.closeResultSet(rs);
        recColl.closePreparedStatement(prep);
        recColl.closePreparedStatement(private_folder_information);
        if (con != null) {
            DBPool.push(c, con);
        }
    }
    
    private final boolean checkPermissions() {
        if ((pflag == 0 && isVisible()) || (pflag == 1 && owner == uid)) {
            return true;
        }
        return false;
    }
    
    private final boolean isVisible() {
        if (cfo == null) {
            return false;
        }
        if (fid > 0) {
            if ( cfo.canReadAllInPublicFolder(fid) ) {
                return true;
            } else if (owner == uid && cfo.canReadOwnInPublicFolder(fid)) {
                return true;
            }
        } else {
            for (int a = 0, size = private_folder_array.length; a < size; a++) {
                PrivateFolderInformationObject pfio = private_folder_array[a];
                if (pfio.compareObjectId(oid)) {
                    int o = pfio.getParticipant();
                    int p = pfio.getPrivateFolder();
                    if (cfo.canReadAllInPrivateFolder(p)) {
                        return true;
                    } else if (cfo.canReadAllInSharedFolder(p)) {
                        return true;
                    } else if (o == uid) {
                        if (cfo.canReadOwnInPrivateFolder(p)) {
                            return true;
                        } else if (cfo.canReadOwnInSharedFolder(p)) {
                            return true;
                        }
                    } 
                }
            }
        }
        return false;
    }
    
    private final void preFillPermissionArray(final int groups[], final UserConfiguration uc) throws OXException {
        final Connection readcon = null;
        try {
            cfo = recColl.getAllVisibleAndReadableFolderObject(uid, groups, c, uc, readcon);
        } catch (final OXException ex) {
            throw new OXException(ex);
        } catch (final DBPoolingException ex) {
            throw new OXException(ex);
        } catch (final SearchIteratorException ex) {
            throw new OXException(ex);
        } catch (final SQLException ex) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, ex);
        } finally {
            if (readcon != null) {
                DBPool.closeReaderSilent(c, readcon);
            }
        }
    }
    
    private final Participants resolveConflictingUserParticipants() throws OXException {
        final Participants p = new Participants();
        int counter = 0;
        if (conflict_objects instanceof UserParticipant[]) {
            try {
                final CalendarDataObject temp = new CalendarDataObject();
                temp.setContext(c);
                temp.setObjectID(oid);
                UserParticipant op[] = null;
                if (oid == last_up_oid) {
                    op = last_up;
                } else {
                    op = CalendarSql.getCalendarSqlImplementation().getUserParticipants(temp, con, uid).getUsers();
                    last_up_oid = oid;
                    last_up = op;
                }
                if (op != null && op.length > 0) {
                    final  UserParticipant up[] = (UserParticipant[])conflict_objects;
                    Set<Integer> upIds = new HashSet<Integer>(up.length);
                    for(int a = 0, size = up.length; a < size; a++) { upIds.add(up[a].getIdentifier()); }

                    for (int b = 0; b < op.length; b++) {
                        if(upIds.contains(op[b].getIdentifier())) {
                            p.add(op[b]);
                            counter++;
                            if (counter >= MAX_SHOW_USER_PARTICIPANTS) {
                                break;
                            }
                        }
                    }

                }
            } catch(final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle, new Object[0]);
            }
        } else {
            try {
                final CalendarDataObject temp = new CalendarDataObject();
                temp.setContext(c);
                temp.setObjectID(oid);
                Participant op[] = null;
                if (oid == last_p_oid) {
                    op = last_p;
                } else {
                    op = CalendarSql.getCalendarSqlImplementation().getParticipants(temp, con).getList();
                    last_p_oid = oid;
                    last_p = op;
                }
                if (op != null && op.length > 1) {
                    for (int a = 0; a < conflict_objects.length; a++) {
                        for (int b = 0; b < op.length; b++) {
                            if (conflict_objects[a].getType() == Participant.RESOURCE && conflict_objects[a].getIdentifier() == op[b].getIdentifier()) {
                                p.add(op[b]);
                                counter++;
                                if (counter >= MAX_SHOW_USER_PARTICIPANTS) {
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch(final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle, new Object[0]);
            }
        }
        return p;
    }
    
    private final void preFillPrivateFolderInformation() {

        List<PrivateFolderInformationObject> list = new ArrayList<PrivateFolderInformationObject>(16);

        int object_id = 0;
        int pfid = 0;
        int uid = 0;
        PreparedStatement shared_folder_info = null;
        try {
            

            ResultSet rs = private_folder_information.executeQuery();
            while (rs.next()) {
                object_id = rs.getInt(1);
                pfid = rs.getInt(2);
                uid = rs.getInt(3);
                if (!rs.wasNull()) {
                    final PrivateFolderInformationObject pfio = new PrivateFolderInformationObject(object_id, pfid, uid);
                    list.add(pfio);
                }
            }
            rs.close();
            // Add shared folders and check if there are such shared folders
            if (! cfo.getSharedFolderList().isEmpty()) {
                shared_folder_info = calendarsqlimp.getSharedAppointmentFolderQuery(c, cfo, con);
    
                rs = shared_folder_info.executeQuery();
                while (rs.next()) {
                    object_id = rs.getInt(1);
                    pfid = rs.getInt(2);
                    uid = rs.getInt(3);
                    if (!rs.wasNull()) {
                        final PrivateFolderInformationObject pfio = new PrivateFolderInformationObject(object_id, pfid, uid);
                        list.add(pfio);
                    }
                }
                rs.close();
            }
        } catch(final SQLException sqle) {
            LOG.error(sqle.getMessage(), sqle);
        } finally {
            if(shared_folder_info != null) {
                try {
                    shared_folder_info.close();
                } catch (final SQLException e) {
                	LOG.error(e.getMessage(), e);
                }
            }
        }
        private_folder_array = list.toArray(new PrivateFolderInformationObject[list.size()]);
    }
    
}
