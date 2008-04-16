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
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * FreeBusyResults
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class FreeBusyResults implements SearchIterator {
    
    public final static int MAX_SHOW_USER_PARTICIPANTS = 5;
    
    private final ResultSet rs;
    private final Connection con;
    private final Context c;
    private int uid;
    private int seq = -1;
    private int sa;
    private int ft;
    private int oid;
    private int pflag;
    private int owner;
    private int fid;
    private String title;
    private RecurringResults rrs;
    private boolean has_next;
    private boolean show_details;
    private final PreparedStatement prep;
    private CalendarFolderObject cfo;
    private Participant conflict_objects[];
    private long range_start;
    private long range_end;
    
    private int last_up_oid;
    private UserParticipant last_up[] = null;
    private int last_p_oid;
    private Participant last_p[] = null;
    
    private ArrayList<Object> al = new ArrayList<Object>(16);
    private int counter;
    
    private ArrayList<PrivateFolderInformationObject> private_folder_array;
    private PreparedStatement private_folder_information;
    
    private static final Log LOG = LogFactory.getLog(FreeBusyResults.class);
    
    public FreeBusyResults(ResultSet rs, PreparedStatement prep, Context c, Connection con, long range_start, long range_end) throws OXException {
        this.rs = rs;
        this.prep = prep;
        this.con = con;
        this.c = c;
        this.range_start = range_start;
        this.range_end = range_end;
        rsNext();
        preFill();
    }
    
    public FreeBusyResults(ResultSet rs, PreparedStatement prep, Context c, int uid, int groups[], UserConfiguration uc, Connection con, boolean show_details, Participant conflict_objects[], PreparedStatement private_folder_information) throws OXException {
        this.rs = rs;
        this.prep = prep;
        this.con = con;
        this.c = c;
        this.uid = uid;
        this.show_details = show_details;
        this.conflict_objects = conflict_objects;
        this.private_folder_information = private_folder_information;
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
                Object o = null;
                try {
                    o = mynext();
                } catch (SearchIteratorException sie) {
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
    
    public final Object next() {
        return al.get(counter++);
    }
    
    public final Object mynext() throws SearchIteratorException, OXException {
        CalendarDataObject cdao = null;
        if (seq >= 0 && rrs != null) {
            final RecurringResult rr = rrs.getRecurringResult(seq);
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
                final int recid = rs.getInt(10);
                if (!rs.wasNull() && recid == oid) {
                    cdao.setRecurrenceCalculator(rs.getInt(11));
                    cdao.setRecurrence(rs.getString(12));
                    cdao.setDelExceptions(rs.getString(13));
                    cdao.setExceptions(rs.getString(14));
                    cdao.setTimezone(rs.getString(15));
                    cdao.setRecurrenceID(recid);
                    if (CalendarRecurringCollection.fillDAO(cdao)) {
                        rrs = CalendarRecurringCollection.calculateRecurring(cdao, range_start, range_end, 0);
                        seq = rrs.size()-1;
                        if (seq >= 0) {
                            final RecurringResult rr = rrs.getRecurringResult(seq);
                            rsNext();
                            return recurringDAO(rr);
                        }
                        rsNext();
                        return null;
                    }
                }
            }
        } catch (SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch(Exception e) {
            LOG.error("FreeBusyResults calculation problem with oid "+oid+" / "+cdao == null ? "" : cdao.toString());
            throw new SearchIteratorException(SearchIteratorException.SearchIteratorCode.CALCULATION_ERROR, com.openexchange.groupware.EnumComponent.APPOINTMENT, oid, e);
        }
        if (ft != 0 && cdao != null) {
            cdao.setFullTime(true);
        }
        if (show_details && checkPermissions() && cdao != null) {
            cdao.setTitle(title);
        }
        rsNext();
        return cdao;
    }
    
    public int size() {
        throw new UnsupportedOperationException("Mehtod size() not implemented");
    }
    
    public boolean hasSize() {
        return false;
    }
    
    private final CalendarDataObject recurringDAO(final RecurringResult rr) throws OXException {
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
            if (show_details && checkPermissions()) {
                cdao.setTitle(title);
                final Participants ret = resolveConflictingUserParticipants();
                cdao.setParticipants(ret.getList());
            }
            seq--;
            return cdao;
        }
        rsNext();
        return null;
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
            } catch (SQLException sqle) {
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
        CalendarCommonCollection.closeResultSet(rs);
        CalendarCommonCollection.closePreparedStatement(prep);
        CalendarCommonCollection.closePreparedStatement(private_folder_information);
        if (con != null) {
            try {
                DBPool.push(c, con);
            } catch (DBPoolingException dbpe) {
                LOG.error(CalendarSql.ERROR_PUSHING_DATABASE, dbpe);
            }
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
            if (Arrays.binarySearch(cfo.getPublicReadableAll(), fid) >= 0) {
                return true;
            } else if (Arrays.binarySearch(cfo.getPublicReadableAll(), fid) >= 0 && owner == uid) {
                return true;
            }
        } else {
            int p = 0;
            int o = 0;
            boolean perm = false;
            for (int a = 0; a < private_folder_array.size(); a++) {
                final PrivateFolderInformationObject pfio = private_folder_array.get(a);
                if (pfio.compareObjectId(oid)) {
                    p = pfio.getPrivateFolder();
                    o = pfio.getParticipant();
                    if (Arrays.binarySearch(cfo.getPrivateReadableAll(), p) >= 0) {
                        perm = true;
                        break;
                    } else if (Arrays.binarySearch(cfo.getSharedReadableAll(), p) >= 0) {
                        perm = true;
                        break;
                    } else if (Arrays.binarySearch(cfo.getPrivateReadableOwn(), p) >= 0 && o == uid) {
                        perm = true;
                        break;
                    } else if (Arrays.binarySearch(cfo.getSharedReadableOwn(), p) >= 0 && o == uid) {
                        perm = true;
                        break;
                    }
                }
            }
            return perm;
        }
        return false;
    }
    
    private final void preFillPermissionArray(final int groups[], final UserConfiguration uc) throws OXException {
        Connection readcon = null;
        try {
            cfo = CalendarCommonCollection.getAllVisibleAndReadableFolderObject(uid, groups, c, uc, readcon);
        } catch (OXException ex) {
            throw new OXException(ex);
        } catch (DBPoolingException ex) {
            throw new OXException(ex);
        } catch (SearchIteratorException ex) {
            throw new OXException(ex);
        } catch (SQLException ex) {
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
                if (oid != last_up_oid)  {
                    op = CalendarSql.getCalendarSqlImplementation().getUserParticipants(temp, con, uid).getUsers();
                    last_up_oid = oid;
                    last_up = op;
                } else {
                    op = last_up;
                }
                if (op != null && op.length > 1) {
                    final  UserParticipant up[] = (UserParticipant[])conflict_objects;
                    for (int a = 0; a < up.length; a++) {
                        for (int b = 0; b < op.length; b++) {
                            if (up[a].getIdentifier() == op[b].getIdentifier()) {
                                p.add(op[b]);
                                counter++;
                                if (counter >= MAX_SHOW_USER_PARTICIPANTS) {
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR);
            }
        } else {
            try {
                final CalendarDataObject temp = new CalendarDataObject();
                temp.setContext(c);
                temp.setObjectID(oid);
                Participant op[] = null;
                if (oid != last_p_oid) {
                    op = CalendarSql.getCalendarSqlImplementation().getParticipants(temp, con).getList();
                    last_p_oid = oid;
                    last_p = op;
                } else {
                    op = last_p;
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
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR);
            }
        }
        return p;
    }
    
    private final void preFillPrivateFolderInformation() {
        private_folder_array = new ArrayList<PrivateFolderInformationObject>(16);
        int object_id = 0;
        int pfid = 0;
        int uid = 0;
        try {
            ResultSet rs = private_folder_information.executeQuery();
            while (rs.next()) {
                object_id = rs.getInt(1);
                pfid = rs.getInt(2);
                uid = rs.getInt(3);
                if (!rs.wasNull()) {
                    final PrivateFolderInformationObject pfio = new PrivateFolderInformationObject(object_id, pfid, uid);
                    private_folder_array.add(pfio);
                }
            }
        } catch(SQLException sqle) {
            LOG.error(sqle.getMessage(), sqle);
        }
    }
    
}
