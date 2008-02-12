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

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ExternalGroupParticipant;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.ResourceGroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderException.Code;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.OXFolderAccess;


/**
 * CalendarSql
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

class CalendarMySQL implements CalendarSqlImp {
    
    private static final String PDM_AND_PD_FID = " AND pd.fid = ";
    private static final String select = "SELECT intfield01, timestampfield01, timestampfield02, field01 FROM prg_dates ";
    private static final String FREE_BUSY_SELECT = "SELECT intfield01, timestampfield01, timestampfield02, intfield07, intfield06, field01, fid, pflag, created_from, intfield02, intfield04, field06, field07, field08, timezone FROM prg_dates ";
    private static final String RANGE_SELECT = "SELECT intfield01, timestampfield01, timestampfield02, intfield02, intfield04, field06, field07, field08, timezone FROM prg_dates ";
    private static final String ORDER_BY = " ORDER BY pd.timestampfield01";
    private static final String ORDER_BY_TS1 = " ORDER BY timestampfield01";
    private static final String JOIN_DATES = " pd JOIN prg_dates_members pdm ON pd.intfield01 = pdm.object_id AND pd.cid = ";
    private static final String JOIN_PARTICIPANTS = " pd JOIN prg_date_rights pdr ON pd.intfield01 = pdr.object_id AND pd.cid = ";
    private static final String WHERE = " WHERE";
    private static final String PDM_MEMBER_UID_IS = " AND pdm.member_uid = ";
    private static final String PDM_MEMBER_UID_IN = " AND pdm.member_uid IN ";
    private static final String PDM_CID_IS = " AND pdm.cid = ";
    private static final String PDM_PFID_IS = " AND pdm.pfid = ";
    private static final String PDM_AND = " AND ";
    private static final String PDM_ORDER_BY = " ORDER BY ";
    private static final String PDM_GROUP_BY_PD_INTFIELD01 = " GROUP BY pd.intfield01 ";
    private static final String PDM_GROUP_BY_INTFIELD01 = " GROUP BY intfield01";
    private static final String PD_FID_IS_NULL = " AND pd.fid = 0 ";
    private static final String PD_CREATED_FROM_IS = " AND pd.created_from = ";
    private static final String DATES_IDENTIFIER_IS = " AND intfield01 = ";
    private static final String PARTICIPANTS_IDENTIFIER_IS = " AND object_id = ";
    private static final String PARTICIPANTS_IDENTIFIER_IN = " AND object_id IN ";
    private static final String UNION = " UNION ";

    private static final Log LOG = LogFactory.getLog(CalendarMySQL.class);
    
    
    public final PreparedStatement getAllAppointmentsForUser(final Context c, final int uid, final int groups[], final UserConfiguration uc, final java.util.Date d1, final java.util.Date d2, final String select, final Connection readcon, final java.util.Date since, final int orderBy, final String orderDir) throws OXException, SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        
        if (since != null) {
            sb.append(PDM_AND);
            getSince(sb);
        }
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(uid);
//        sb.append(" AND pdm.confirm != ");
//        sb.append(com.openexchange.groupware.container.CalendarObject.DECLINE);
        
        CalendarCommonCollection.getVisibleFolderSQLInString(sb, uid, groups, c, uc, readcon);
        
        if (CalendarCommonCollection.getFieldName(orderBy) != null && orderDir != null) {
            sb.append(PDM_ORDER_BY);
            sb.append(CalendarCommonCollection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
        } else {
            sb.append(ORDER_BY);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        int a = 1;
        
        pst.setTimestamp(a++, new Timestamp(d2.getTime()));
        pst.setTimestamp(a++, new Timestamp(d1.getTime()));
        
        if (since != null) {
            pst.setLong(a++, since.getTime());
        }
        return pst;
    }
    
    public final PreparedStatement getConflicts(final Context c, final java.util.Date d1, final java.util.Date d2, final java.util.Date d3, final java.util.Date d4, final Connection readcon, final String member_sql_in, final boolean free_busy_select) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        if (free_busy_select) {
            sb.append(FREE_BUSY_SELECT);
        } else {
            sb.append("SELECT pdm.object_id, pdm.pfid, pdm.member_uid FROM prg_dates ");
        }
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getConflictRange(sb);
        sb.append(PDM_MEMBER_UID_IN);
        sb.append(member_sql_in);
        sb.append(" AND pd.intfield06 != ");
        sb.append(AppointmentObject.FREE);
        sb.append(" AND pdm.confirm != ");
        sb.append(com.openexchange.groupware.container.CalendarObject.DECLINE);
        
        if (free_busy_select) {
            sb.append(UNION);
            sb.append(FREE_BUSY_SELECT);
            sb.append(JOIN_DATES);
            sb.append(c.getContextId());
            sb.append(PDM_CID_IS);
            sb.append(c.getContextId());
            sb.append(WHERE);
            getConflictRangeFullTime(sb);
            sb.append(PDM_MEMBER_UID_IN);
            sb.append(member_sql_in);
            sb.append(" AND pd.intfield06 != ");
            sb.append(AppointmentObject.FREE);
            sb.append(" AND pdm.confirm != ");
            sb.append(com.openexchange.groupware.container.CalendarObject.DECLINE);
        } else {
            sb.append(PDM_GROUP_BY_PD_INTFIELD01);
            sb.append(ORDER_BY);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        if (free_busy_select && d3 != null && d4 != null) {
            pst.setTimestamp(3, new Timestamp(d4.getTime()));
            pst.setTimestamp(4, new Timestamp(d3.getTime()));
        }
        return pst;
    }
    
    public final PreparedStatement getResourceConflictsPrivateFolderInformation(final Context c, final java.util.Date d1, final java.util.Date d2, final java.util.Date d3, final java.util.Date d4, final Connection readcon, final String resource_sql_in) throws SQLException {
        final StringBuilder sb = new StringBuilder(184);
        sb.append("SELECT pdm.object_id, pdm.pfid, pdm.member_uid FROM prg_dates ");
        sb.append(JOIN_PARTICIPANTS);
        sb.append(c.getContextId());
        sb.append(" AND pdr.cid = ");
        sb.append(c.getContextId());
        sb.append(" JOIN prg_dates_members pdm ON pd.intfield01 = pdm.object_id AND pdm.cid = ");
        sb.append(c.getContextId());
        sb.append(WHERE);
        getConflictRangeFullTime(sb);
        sb.append(" AND pdr.id IN ");
        sb.append(resource_sql_in);
        sb.append(" AND pdr.type = ");
        sb.append(Participant.RESOURCE);
        sb.append(" AND pd.intfield06 != ");
        sb.append(AppointmentObject.FREE);
        
        sb.append(UNION);
        
        sb.append("SELECT pdm.object_id, pdm.pfid, pdm.member_uid FROM prg_dates ");
        sb.append(JOIN_PARTICIPANTS);
        sb.append(c.getContextId());
        sb.append(" AND pdr.cid = ");
        sb.append(c.getContextId());
        sb.append(" JOIN prg_dates_members pdm ON pd.intfield01 = pdm.object_id AND pdm.cid = ");
        sb.append(c.getContextId());
        sb.append(WHERE);
        getConflictRangeFullTime(sb);
        sb.append(" AND pdr.id IN ");
        sb.append(resource_sql_in);
        sb.append(" AND pdr.type = ");
        sb.append(Participant.RESOURCE);
        sb.append(" AND pd.intfield06 != ");
        sb.append(AppointmentObject.FREE);
        
        sb.append(PDM_GROUP_BY_INTFIELD01);
        //sb.append(ORDER_BY_TS1);
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        pst.setTimestamp(3, new Timestamp(d4.getTime()));
        pst.setTimestamp(4, new Timestamp(d3.getTime()));
        return pst;
    }
    
    public final PreparedStatement getResourceConflicts(final Context c, final java.util.Date d1, final java.util.Date d2, final java.util.Date d3, final java.util.Date d4, final Connection readcon, final String resource_sql_in) throws SQLException {
        final StringBuilder sb = new StringBuilder(184);
        sb.append(FREE_BUSY_SELECT);
        sb.append(JOIN_PARTICIPANTS);
        sb.append(c.getContextId());
        sb.append(" AND pdr.cid = ");
        sb.append(c.getContextId());
        sb.append(WHERE);
        getConflictRange(sb);
        sb.append(" AND pdr.id IN ");
        sb.append(resource_sql_in);
        sb.append(" AND pdr.type = ");
        sb.append(Participant.RESOURCE);
        sb.append(" AND pd.intfield06 != ");
        sb.append(AppointmentObject.FREE);
        
        sb.append(UNION);
        
        sb.append(FREE_BUSY_SELECT);
        sb.append(JOIN_PARTICIPANTS);
        sb.append(c.getContextId());
        sb.append(" AND pdr.cid = ");
        sb.append(c.getContextId());
        sb.append(WHERE);
        getConflictRangeFullTime(sb);
        sb.append(" AND pdr.id IN ");
        sb.append(resource_sql_in);
        sb.append(" AND pdr.type = ");
        sb.append(Participant.RESOURCE);
        sb.append(" AND pd.intfield06 != ");
        sb.append(AppointmentObject.FREE);
        
        
        sb.append(PDM_GROUP_BY_INTFIELD01);
        sb.append(ORDER_BY_TS1);
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        pst.setTimestamp(3, new Timestamp(d4.getTime()));
        pst.setTimestamp(4, new Timestamp(d3.getTime()));
        return pst;
    }
    
    public final PreparedStatement getFreeBusy(final int uid, final Context c, final java.util.Date d1, final java.util.Date d2, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(FREE_BUSY_SELECT);
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(uid);
        sb.append(" AND pdm.confirm != ");
        sb.append(com.openexchange.groupware.container.CalendarObject.DECLINE);
        sb.append(PDM_GROUP_BY_PD_INTFIELD01);
        sb.append(ORDER_BY);
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }
    
    public final PreparedStatement getResourceFreeBusy(final int uid, final Context c, final java.util.Date d1, final java.util.Date d2, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(FREE_BUSY_SELECT);
        sb.append(JOIN_PARTICIPANTS);
        sb.append(c.getContextId());
        sb.append(" AND pdr.cid = ");
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        sb.append(" AND pdr.id = ");
        sb.append(uid);
        sb.append(" AND pdr.type = ");
        sb.append(Participant.RESOURCE);
        sb.append(PDM_GROUP_BY_PD_INTFIELD01);
        sb.append(ORDER_BY);
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }
    
    
    public PreparedStatement getActiveAppointments(final Context c, final int uid, final java.util.Date d1, final java.util.Date d2, final String select, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        sb.append(" AND member_uid = ");
        sb.append(uid);
        sb.append(" AND confirm != ");
        sb.append(com.openexchange.groupware.container.CalendarObject.DECLINE);
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }
    
    public final boolean[] getUserActiveAppointmentsRangeSQL(final Context c, final int uid, final int groups[], final UserConfiguration uc, final java.util.Date d1, final java.util.Date d2, final Connection readcon) throws SQLException, OXException {
        final StringBuilder sb = new StringBuilder(64);
        final long start = d1.getTime();
        final long end = d2.getTime();
        final int size = (int)((end-start)/CalendarRecurringCollection.MILLI_DAY);
        final boolean activeDates[] = new boolean[size];
        sb.append(RANGE_SELECT);
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        sb.append(" AND member_uid = ");
        sb.append(uid);
        sb.append(" AND confirm != ");
        sb.append(com.openexchange.groupware.container.CalendarObject.DECLINE);
        
        CalendarCommonCollection.getVisibleFolderSQLInString(sb, uid, groups, c, uc, readcon);
        
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        
        final ResultSet rs = getResultSet(pst);
        try {
            CalendarDataObject cdao = null;
            while (rs.next()) {
                cdao = new CalendarDataObject();
                final int oid = rs.getInt(1);
                final java.util.Date s = rs.getTimestamp(2);
                final java.util.Date e = rs.getTimestamp(3);
                final  int rec = rs.getInt(4);
                if (!rs.wasNull() && oid == rec) {
                    cdao.setStartDate(s);
                    cdao.setEndDate(e);
                    cdao.setRecurrenceCalculator(rs.getInt(5));
                    cdao.setRecurrence(rs.getString(6));
                    cdao.setDelExceptions(rs.getString(7));
                    cdao.setExceptions(rs.getString(8));
                    cdao.setTimezone(rs.getString(9));
                    if (CalendarRecurringCollection.fillDAO(cdao)) {
                        final RecurringResults rrs = CalendarRecurringCollection.calculateRecurring(cdao, start, end, 0);
                        for (int a = 0; a < rrs.size(); a++) {
                            final RecurringResult rr = rrs.getRecurringResult(a);
                            fillActiveDates(start, rr.getStart(), rr.getEnd(), activeDates);
                        }
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(StringCollection.convertArraytoString(new Object[] { "SKIP calculation for recurring appointment oid:uid:context " ,oid,CalendarOperation.COLON,uid,CalendarOperation.COLON,c.getContextId() }));
                        }
                    }
                } else {
                    fillActiveDates(start, s.getTime(), e.getTime(), activeDates);
                }
            }
            //CalendarCommonCollection.debugActiveDates (start, end, activeDates); // TODO:  Make configurable or uncomment in runtime edition
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(pst);
        }
        return activeDates;
    }
    
    private final void fillActiveDates(final long start, long s, final long e, final boolean activeDates[]) {
        if (start > s) {
            s = start;
        }
        
        int start_pos = 0;
        final int ll = (int)(e-s);
        int len = (int)(ll/CalendarRecurringCollection.MILLI_DAY);
        if (ll != 0 && ll % CalendarRecurringCollection.MILLI_DAY == 0) {
            len--;
        }
        
        if (s >= start) {
            start_pos = (int)((s-start)/CalendarRecurringCollection.MILLI_DAY);
            if (start_pos > activeDates.length) {
                return;
            }
        }
        for (int a = start_pos; a <= start_pos+len; a++) {
            if (a >= activeDates.length) {
                return;
            }
            activeDates[a] = true;
        }
    }
    
    public final PreparedStatement getPublicFolderRangeSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date d1, final java.util.Date d2, final String select, final boolean readall, final Connection readcon, final int orderBy, final String orderDir) throws SQLException {
        final StringBuilder sb = new StringBuilder(32);
        sb.append(parseSelect(select));
        sb.append(" pd ");
        sb.append(WHERE);
        getRange(sb);
        sb.append(" AND pd.cid = ");
        sb.append(c.getContextId());
        sb.append(PDM_AND_PD_FID);
        sb.append(fid);
        if (!readall) {
            sb.append(PD_CREATED_FROM_IS);
            sb.append(uid);
        }
        if (CalendarCommonCollection.getFieldName(orderBy) != null && orderDir != null) {
            sb.append(PDM_ORDER_BY);
            sb.append(CalendarCommonCollection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
        } else {
            sb.append(ORDER_BY);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }
    
    public final PreparedStatement getPrivateFolderRangeSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date d1, final java.util.Date d2, final String select, final boolean readall, final Connection readcon, final int orderBy, final String orderDir) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        sb.append(PD_FID_IS_NULL);
        sb.append(PDM_PFID_IS);
        sb.append(fid);
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(uid);
        if (!readall) {
            sb.append(PD_CREATED_FROM_IS);
            sb.append(uid);
        }
        if (CalendarCommonCollection.getFieldName(orderBy) != null && orderDir != null) {
            sb.append(PDM_ORDER_BY);
            sb.append(CalendarCommonCollection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
        } else {
            sb.append(ORDER_BY);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }
    
    public final PreparedStatement getSharedFolderRangeSQL(final Context c, final int uid, final int shared_folder_owner, final int groups[], final int fid, final java.util.Date d1, final java.util.Date d2, final String select, final boolean readall, final Connection readcon, final int orderBy, final String orderDir) throws SQLException {
        final StringBuilder sb = new StringBuilder(32);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        sb.append(PD_FID_IS_NULL);
        sb.append(" AND pd.pflag = 0 ");
        sb.append(PDM_PFID_IS);
        sb.append(fid);
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(shared_folder_owner);
        if (!readall) {
            sb.append(PD_CREATED_FROM_IS);
            sb.append(uid);
        }
        if (CalendarCommonCollection.getFieldName(orderBy) != null && orderDir != null) {
            sb.append(PDM_ORDER_BY);
            sb.append(CalendarCommonCollection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
        } else {
            sb.append(ORDER_BY);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }
    
    public final PreparedStatement getPrivateFolderModifiedSinceSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date since, final String select, final boolean readall, final Connection readcon, final java.util.Date d1, final java.util.Date d2) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getSince(sb);
        if (d1 != null && d2 != null) {
            sb.append(PDM_AND);
            getRange(sb);
        }
        sb.append(PD_FID_IS_NULL);
        sb.append(PDM_PFID_IS);
        sb.append(fid);
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(uid);
        if (!readall) {
            sb.append(PD_CREATED_FROM_IS);
            sb.append(uid);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setLong(1, since.getTime());
        if (d1 != null && d2 != null) {
            pst.setTimestamp(2, new Timestamp(d2.getTime()));
            pst.setTimestamp(3, new Timestamp(d1.getTime()));
        }
        return pst;
    }
    
    public final PreparedStatement getSharedFolderModifiedSinceSQL(final Context c, final int uid, final int shared_folder_owner, final int groups[], final int fid, final java.util.Date since, final String select, final boolean readall, final Connection readcon, final java.util.Date d1, final java.util.Date d2) throws SQLException {
        final StringBuilder sb = new StringBuilder(32);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getSince(sb);
        if (d1 != null && d2 != null) {
            sb.append(PDM_AND);
            getRange(sb);
        }
        sb.append(PD_FID_IS_NULL);
        sb.append(" AND pd.pflag = 0 ");
        sb.append(PDM_PFID_IS);
        sb.append(fid);
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(shared_folder_owner);
        if (!readall) {
            sb.append(PD_CREATED_FROM_IS);
            sb.append(uid);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setLong(1, since.getTime());
        if (d1 != null && d2 != null) {
            pst.setTimestamp(2, new Timestamp(d2.getTime()));
            pst.setTimestamp(3, new Timestamp(d1.getTime()));
        }
        return pst;
    }
    
    public final PreparedStatement getPublicFolderModifiedSinceSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date since, final String select, final boolean readall, final Connection readcon, final java.util.Date d1, final java.util.Date d2) throws SQLException {
        final StringBuilder sb = new StringBuilder(48);
        sb.append(parseSelect(select));
        sb.append(" pd WHERE pd.cid = ");
        sb.append(c.getContextId());
        sb.append(PDM_AND);
        getSince(sb);
        if (d1 != null && d2 != null) {
            sb.append(PDM_AND);
            getRange(sb);
        }
        sb.append(PDM_AND_PD_FID);
        sb.append(fid);
        if (!readall) {
            sb.append(PD_CREATED_FROM_IS);
            sb.append(uid);
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setLong(1, since.getTime());
        if (d1 != null && d2 != null) {
            pst.setTimestamp(2, new Timestamp(d2.getTime()));
            pst.setTimestamp(3, new Timestamp(d1.getTime()));
        }
        return pst;
    }
    
    public final PreparedStatement getPrivateFolderDeletedSinceSQL(final Context c, final int uid, final int fid, final java.util.Date d1, final String select, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(parseSelect(select));
        sb.append(" pd JOIN del_dates_members pdm ON pd.intfield01 = pdm.object_id AND pd.cid = ");
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getSince(sb);
        sb.append(" AND pd.fid = 0");
        sb.append(PDM_PFID_IS);
        sb.append(fid);
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(uid);
        sb.toString();
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setLong(1, d1.getTime());
        return pst;
    }
    
    public final PreparedStatement getSharedFolderDeletedSinceSQL(final Context c, final int uid, final int shared_folder_owner, final int fid, final java.util.Date d1, final String select, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(parseSelect(select));
        sb.append(" pd JOIN del_dates_members pdm ON pd.intfield01 = pdm.object_id AND pd.cid = ");
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getSince(sb);
        sb.append(" AND pd.fid = 0");
        sb.append(PDM_PFID_IS);
        sb.append(fid);
        sb.append(PDM_MEMBER_UID_IS);
        sb.append(shared_folder_owner);
        sb.toString();
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setLong(1, d1.getTime());
        return pst;
    }
    
    public final PreparedStatement getPublicFolderDeletedSinceSQL(final Context c, final int uid, final int fid, final java.util.Date d1, final String select, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(96);
        sb.append(parseSelect(select));
        sb.append(" pd JOIN del_dates_members pdm ON pd.intfield01 = pdm.object_id AND pd.cid = ");
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getSince(sb);
        sb.append(PDM_AND_PD_FID);
        sb.append(fid);
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setLong(1, d1.getTime());
        return pst;
    }
    
    public final String getObjectsByidSQL(final int oids[][], final int cid, final String select) {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(parseSelect(select));
        sb.append(" pd WHERE");
        sb.append(" pd.cid = ");
        sb.append(cid);
        sb.append(PDM_AND);
        sb.append(" intfield01 IN ");
        sb.append(StringCollection.getSqlInString(oids));
        return sb.toString();
    }
    
    public PreparedStatement getPrivateFolderObjects(final int fid, final Context c, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("SELECT intfield01, created_from FROM prg_dates ");
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        sb.append(" pd.fid = 0 ");
        sb.append(PDM_PFID_IS);
        sb.append(fid);
        return getPreparedStatement(readcon, sb.toString());
    }
    
    public PreparedStatement getPublicFolderObjects(final int fid, final Context c, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("SELECT intfield01, created_from FROM prg_dates pd");
        sb.append(WHERE);
        sb.append(" pd.cid = ");
        sb.append(c.getContextId());
        sb.append(PDM_AND_PD_FID);
        sb.append(fid);
        return getPreparedStatement(readcon, sb.toString());
    }
    
    public boolean checkIfFolderContainsForeignObjects(final int uid, final int fid, final Context c, final Connection readcon, final int foldertype) throws SQLException {
        final StringBuilder sb = new StringBuilder();
        if (foldertype == FolderObject.PRIVATE) {
            sb.append("SELECT intfield01 FROM prg_dates ");
            sb.append(JOIN_DATES);
            sb.append(c.getContextId());
            sb.append(PDM_CID_IS);
            sb.append(c.getContextId());
            sb.append(WHERE);
            sb.append(" pd.created_from != ");
            sb.append(uid);
            sb.append(PD_FID_IS_NULL);
            sb.append(PDM_PFID_IS);
            sb.append(fid);
        } else if (foldertype == FolderObject.PUBLIC) {
            sb.append("SELECT intfield01 FROM prg_dates pd");
            sb.append(WHERE);
            sb.append(" pd.created_from != ");
            sb.append(uid);
            sb.append(PDM_AND_PD_FID);
            sb.append(fid);
        } else {
            throw new SQLException("Unknown type detected!");
        }
        final PreparedStatement prep = getPreparedStatement(readcon, sb.toString());
        final ResultSet rs = getResultSet(prep);
        boolean ret = true;
        try {
            if (!rs.next()) {
                ret = false;
            }
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(prep);
        }
        return ret;
    }
    
    public boolean checkIfFolderIsEmpty(final int uid, final int fid, final Context c, final Connection readcon, final int foldertype) throws SQLException {
        final StringBuilder sb = new StringBuilder();
        if (foldertype == FolderObject.PRIVATE) {
            sb.append("SELECT intfield01 FROM prg_dates ");
            sb.append(JOIN_DATES);
            sb.append(c.getContextId());
            sb.append(PDM_CID_IS);
            sb.append(c.getContextId());
            sb.append(WHERE);
            sb.append(" pd.fid = 0 ");
            sb.append(PDM_PFID_IS);
            sb.append(fid);
        } else if (foldertype == FolderObject.PUBLIC) {
            sb.append("SELECT intfield01 FROM prg_dates pd");
            sb.append(WHERE);
            sb.append(" pd.fid = ");
            sb.append(fid);
        } else {
            throw new SQLException("Unknown type detected!");
        }
        final PreparedStatement prep = getPreparedStatement(readcon, sb.toString());
        final ResultSet rs = getResultSet(prep);
        boolean ret = true;
        try {
            if (rs.next()) {
                ret = false;
            }
        } finally {
            CalendarCommonCollection.closePreparedStatement(prep);
            CalendarCommonCollection.closeResultSet(rs);
        }
        return ret;
    }
    
    private final void getRange(final StringBuilder sb) {
        sb.append(" pd.timestampfield01 <= ? AND pd.timestampfield02 > ?");
    }
    
    private final void getConflictRange(final StringBuilder sb) {
        sb.append(" pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
    }
    
    private final void getConflictRangeFullTime(final StringBuilder sb) {
        sb.append(" intfield07 = 1 AND pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
    }
    
    private final void getSince(final StringBuilder sb) {
        sb.append(" pd.changing_date >= ?");
    }
    
    private final String parseSelect(final String select) {
        if (select != null) {
            return select;
        }
        return CalendarMySQL.select;
    }
    
    public final PreparedStatement getSearchQuery(final String select, final int uid, final int groups[], final UserConfiguration uc, final int orderBy, final String orderDir, final AppointmentSearchObject searchobject, final Context c, final Connection readcon, final CalendarFolderObject cfo) throws SQLException, OXException {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        
        java.util.Date range[] = searchobject.getRange();
        
        if (range != null && range[0] != null && range[1] != null) {
            range = searchobject.getRange();
            getRange(sb);
            sb.append(PDM_AND);
        }
        
        if (searchobject.getFolder() > 0) {
            sb.append(" ((pd.fid = 0");
            sb.append(PDM_PFID_IS);
            sb.append(searchobject.getFolder());
            sb.append(PDM_MEMBER_UID_IS);
            sb.append(uid);
            sb.append(") OR (");
            sb.append("pd.fid = ");
            sb.append(searchobject.getFolder());
            sb.append(PDM_MEMBER_UID_IS);
            sb.append(uid);
            sb.append("))");
        } else {
            
            
            sb.append(" pdm.member_uid = ");
            sb.append(uid);
            
            if (cfo != null) {
                final Object private_read_all[] = cfo.getPrivateReadableAll();
                final Object private_read_own[] = cfo.getPrivateReadableOwn();
                final Object public_read_all[] = cfo.getPublicReadableAll();
                final Object public_read_own[] = cfo.getPublicReadableOwn();
                
                boolean private_query = false;
                boolean public_query = false;
                if (private_read_all.length > 0) {
                    sb.append(" AND (pdm.pfid IN ");
                    sb.append(StringCollection.getSqlInString(private_read_all));
                    private_query = true;
                }
                
                if (private_read_own.length > 0) {
                    if (!private_query) {
                        sb.append(PD_CREATED_FROM_IS);
                    } else {
                        sb.append("OR pd.created_from = ");
                    }
                    sb.append(uid);
                    sb.append(" AND (pdm.pfid IN ");
                    sb.append(StringCollection.getSqlInString(private_read_own));
                    private_query = true;
                }
                
                
                if (public_read_all.length > 0) {
                    if (private_query) {
                        sb.append(" OR pd.fid IN ");
                        sb.append(StringCollection.getSqlInString(public_read_all));
                        public_query = true;
                    } else {
                        sb.append(" AND pd.fid IN ");
                        sb.append(StringCollection.getSqlInString(public_read_all));
                        public_query = true;
                    }
                }
                
                if (public_read_own.length > 0) {
                    if (!private_query && !public_query) {
                        sb.append(" AND pd.fid IN ");
                        sb.append(StringCollection.getSqlInString(public_read_own));
                        sb.append(PD_CREATED_FROM_IS);
                        sb.append(uid);
                    } else {
                        sb.append(" OR pd.fid IN ");
                        sb.append(StringCollection.getSqlInString(public_read_own));
                        sb.append(PD_CREATED_FROM_IS);
                        sb.append(uid);
                    }
                }
                
                if (private_query) {
                    sb.append(')');
                }
            }
        }
        
        
        String pattern = searchobject.getPattern();
        if (pattern != null) {
            sb.append(PDM_AND);
            sb.append(CalendarCommonCollection.getFieldName(AppointmentObject.TITLE));
            sb.append(" LIKE ?");
            pattern = pattern.trim();
            if (pattern.charAt(0) != '*') {
                pattern = CalendarOperation.PERCENT+pattern;
            } else {
                pattern = '%'+pattern.substring(1, pattern.length());
            }
            if (!pattern.endsWith("*")) {
                pattern = pattern+CalendarOperation.PERCENT;
            } else {
                pattern = pattern.substring(0, pattern.length()-1)+CalendarOperation.PERCENT;
            }
            
        }
        
        sb.append(PDM_ORDER_BY);
        String orderby = CalendarCommonCollection.getFieldName(orderBy);
        if (orderby == null) {
            orderby = CalendarCommonCollection.getFieldName(AppointmentObject.START_DATE);
        }
        sb.append(orderby);
        if (orderDir != null) {
            sb.append(' ');
            sb.append(orderDir);
        }
        
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        
        int x = 1;
        
        if (range != null && range[0] != null && range[1] != null) {
            pst.setTimestamp(x++, new Timestamp(range[1].getTime()));
            pst.setTimestamp(x++, new Timestamp(range[0].getTime()));
        }
        
        if (pattern != null) {
            pst.setString(x++, pattern);
        }
        
        // TODO: This should be rewritten to be more flexible and to cover all expectations
        return pst;
    }
    
    public final ResultSet getResultSet(final PreparedStatement pst) throws SQLException {
        return pst.executeQuery();
    }
    
    public final PreparedStatement getPreparedStatement(final Connection readcon, final String sql) throws SQLException {
        return readcon.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }
    
    public final String loadAppointment(final int oid, final Context c) throws SQLException {
        final StringBuilder sb = new StringBuilder(384);
        sb.append("SELECT creating_date, created_from, changing_date, changed_from, fid, pflag, timestampfield01, timestampfield02, timezone, ").append(
                " intfield02, intfield03, field01, field02, intfield06, intfield08, field04, intfield07, field09, intfield04, intfield05, field06, field07, field08 FROM prg_dates  WHERE cid = ");
        sb.append(c.getContextId());
        sb.append(DATES_IDENTIFIER_IS);
        sb.append(oid);
        return sb.toString();
        
    }
    
    public final CalendarDataObject[] insertAppointment(final CalendarDataObject cdao, final Connection writecon, final Session so) throws DataTruncation, SQLException, LdapException, Exception {
        int i = 1;
        PreparedStatement pst = null;
        try {
            pst = writecon.prepareStatement("insert into prg_dates (creating_date, created_from, changing_date, changed_from,"+
                    "fid, pflag, cid, timestampfield01, timestampfield02, timezone, intfield01, intfield03, intfield06, intfield07, intfield08, "+
                    "field01, field02, field04, field09, intfield02, intfield04, intfield05, field06, field07, field08) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            cdao.setObjectID(IDGenerator.getId(cdao.getContext(), Types.APPOINTMENT, writecon));
            
            pst.setTimestamp(i++, cdao.getCreatingDate());
            if (!cdao.containsLastModified()) {
                cdao.setLastModified(cdao.getCreatingDate());
            }
            pst.setInt(i++, cdao.getCreatedBy());
            pst.setLong(i++, cdao.getChangingDate().getTime());
            pst.setInt(i++, cdao.getModifiedBy());
            
            if (cdao.getFolderType() == FolderObject.PRIVATE || cdao.getFolderType() == FolderObject.SHARED) {
                pst.setInt(i++, 0);
            } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                pst.setInt(i++, cdao.getGlobalFolderID());
            } else {
                throw new OXCalendarException(OXCalendarException.Code.NOT_YET_SUPPORTED);
            }
            pst.setInt(i++, cdao.getPrivateflag());
            pst.setInt(i++, cdao.getContextID());
            pst.setTimestamp(i++, new java.sql.Timestamp(cdao.getStartDate().getTime()));
            pst.setTimestamp(i++, new java.sql.Timestamp(cdao.getEndDate().getTime()));
            pst.setString(i++, cdao.getTimezone());
            pst.setInt(i++, cdao.getObjectID());
            pst.setInt(i++, cdao.getLabel());
            pst.setInt(i++, cdao.getShownAs());
            pst.setInt(i++, cdao.getFulltime());
            pst.setInt(i++, cdao.getNumberOfAttachments());
            pst.setString(i++, cdao.getTitle());
            if (cdao.containsLocation()) {
                pst.setString(i++, cdao.getLocation());
            } else {
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }
            if (cdao.containsNote()) {
                pst.setString(i++, cdao.getNote());
            } else {
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }
            if (cdao.containsCategories()) {
                pst.setString(i++, cdao.getCategories());
            } else {
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }
            
            if (!cdao.isSequence(true)) {
                pst.setNull(i++, java.sql.Types.INTEGER);
                pst.setNull(i++, java.sql.Types.INTEGER);
                pst.setNull(i++, java.sql.Types.INTEGER);
                pst.setNull(i++, java.sql.Types.VARCHAR);
                pst.setNull(i++, java.sql.Types.VARCHAR);
                pst.setNull(i++, java.sql.Types.VARCHAR);
            } else {
                if (!cdao.containsRecurrenceID()) {
                    pst.setInt(i++, cdao.getObjectID());
                    cdao.setRecurrenceID(cdao.getObjectID());
                } else {
                    pst.setInt(i++, cdao.getRecurrenceID());
                    cdao.setRecurrenceID(cdao.getRecurrenceID());
                }
                pst.setInt(i++, cdao.getRecurrenceCalculator());
                pst.setInt(i++, cdao.getRecurrencePosition());
                pst.setString(i++, cdao.getRecurrence());
                if (cdao.getDelExceptions() != null) {
                    pst.setString(i++, cdao.getDelExceptions());
                } else {
                    pst.setNull(i++, java.sql.Types.VARCHAR);
                }
                if (cdao.getExceptions() != null) {
                    pst.setString(i++, cdao.getExceptions());
                } else {
                    pst.setNull(i++, java.sql.Types.VARCHAR);
                }
            }
            
            insertParticipants(cdao, writecon);
            insertUserParticipants(cdao, writecon, so.getUserId());
            pst.executeUpdate();
        } finally {
            CalendarCommonCollection.closePreparedStatement(pst);
        }
        writecon.commit();
        cdao.setParentFolderID(cdao.getActionFolder());
        CalendarCommonCollection.triggerEvent(so, CalendarOperation.INSERT, cdao);
        return null;
    }
    
    private final void insertParticipants(final CalendarDataObject cdao, final Connection writecon) throws SQLException, LdapException, OXCalendarException {
        final Participant p[] = cdao.getParticipants();
        Arrays.sort(p);
        if (p != null) {
            PreparedStatement pi = null;
            try {
                pi = writecon.prepareStatement("insert into prg_date_rights (object_id, cid, id, type, dn, ma) values (?, ?, ?, ?, ?, ?)");
                int lastid = -1;
                int lasttype = -1;
                for (int a = 0; a < p.length; a++) {
                    if (p[a].getIdentifier() == 0 && p[a].getType() == Participant.EXTERNAL_USER && p[a].getEmailAddress() != null) {
                        ExternalUserParticipant eup = new ExternalUserParticipant(p[a].getEmailAddress());
                        eup.setIdentifier(p[a].getEmailAddress().hashCode());
                        eup.setDisplayName(p[a].getDisplayName());
                        p[a] = eup;
                    }
                    if (!(lastid == p[a].getIdentifier() && lasttype == p[a].getType())) {
                        lastid = p[a].getIdentifier();
                        lasttype = p[a].getType();
                        pi.setInt(1, cdao.getObjectID());
                        pi.setInt(2, cdao.getContextID());
                        pi.setInt(3, p[a].getIdentifier());
                        pi.setInt(4, p[a].getType());
                        if (p[a].getDisplayName() == null) {
                            pi.setNull(5, java.sql.Types.VARCHAR);
                        } else {
                            pi.setString(5, p[a].getDisplayName());
                        }
                        if (p[a].getEmailAddress() == null) {
                            if (p[a].getIdentifier() > 0) {
                                pi.setNull(6, java.sql.Types.VARCHAR);
                            } else if (p[a].getType() == Participant.GROUP && p[a].getIdentifier() == 0) {
                                pi.setNull(6, 0);
                            } else {
                                throw new OXCalendarException(OXCalendarException.Code.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD);
                            }
                        } else {
                            pi.setString(6, p[a].getEmailAddress());
                        }
                        pi.addBatch();
                    }
                }
                pi.executeBatch();
            } finally {
                CalendarCommonCollection.closePreparedStatement(pi);
            }
        }
    }
        
    private final void insertUserParticipants(final CalendarDataObject cdao, final Connection writecon, final int uid) throws SQLException, Exception {
        final UserParticipant up[] = cdao.getUsers();
        Arrays.sort(up);
        if (up != null && up.length > 0) {
            PreparedStatement pi = null;
            try {
                pi = writecon.prepareStatement("insert into prg_dates_members (object_id, member_uid, pfid, confirm, reason, reminder, cid) values (?, ?, ?, ?, ?, ?, ?)");
                int lastid = -1;
                final OXFolderAccess access = new OXFolderAccess(cdao.getContext());
                for (int a = 0; a < up.length; a++) {
                    if (lastid != up[a].getIdentifier()) {
                        lastid = up[a].getIdentifier();
                        pi.setInt(1, cdao.getObjectID());
                        pi.setInt(2, up[a].getIdentifier());
                        
                        if (cdao.getFolderType() == FolderObject.PRIVATE ) {
                            if (cdao.getEffectiveFolderId() == 0) {
                                final int pfid = access.getDefaultFolder(up[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(up[a].getIdentifier(), cdao.getContext()));
                                pi.setInt(3, pfid);
                                up[a].setPersonalFolderId(pfid);
                                if (up[a].getIdentifier() == uid) {
                                    cdao.setActionFolder(pfid);
                                }
                            } else {
                                if (up[a].getIdentifier() == uid) {
                                    pi.setInt(3, cdao.getEffectiveFolderId());
                                    up[a].setPersonalFolderId(cdao.getEffectiveFolderId());
                                    if (cdao.getActionFolder() == 0) {
                                        cdao.setActionFolder(cdao.getEffectiveFolderId());
                                    }
                                } else {
                                    final int pfid = access.getDefaultFolder(up[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                    //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(up[a].getIdentifier(), cdao.getContext()));
                                    pi.setInt(3, pfid);
                                    up[a].setPersonalFolderId(pfid);
                                }
                            }
                        } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                            pi.setNull(3, java.sql.Types.INTEGER);
                        } else if (cdao.getFolderType() == FolderObject.SHARED) {
                            if (cdao.getSharedFolderOwner() != 0) {
                                if (up[a].getIdentifier() == cdao.getSharedFolderOwner()) {
                                    if (cdao.getGlobalFolderID() == 0) {
                                        final int pfid = access.getDefaultFolder(cdao.getSharedFolderOwner(), FolderObject.CALENDAR).getObjectID();
                                        //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(cdao.getSharedFolderOwner(), cdao.getContext()));
                                        pi.setInt(3, pfid);
                                        up[a].setPersonalFolderId(pfid);
                                        if (up[a].getIdentifier() == uid) {
                                            cdao.setActionFolder(pfid);
                                        }
                                    } else {
                                        pi.setInt(3, cdao.getGlobalFolderID());
                                        up[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                    }
                                } else {
                                    final int pfid = access.getDefaultFolder(up[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                    //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(up[a].getIdentifier(), cdao.getContext()));
                                    pi.setInt(3, pfid);
                                    up[a].setPersonalFolderId(pfid);
                                }
                            } else {
                                throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.FOLDER_TYPE_UNRESOLVEABLE);
                        }
                        pi.setInt(4, up[a].getConfirm());
                        if (cdao.containsAlarm() && up[a].getIdentifier() == uid) {
                            up[a].setAlarmMinutes(cdao.getAlarm());
                        } else {
                            if (!up[a].containsAlarm()) {
                                up[a].setAlarmMinutes(-1);
                            }
                        }
                        if (up[a].containsConfirmMessage() && up[a].getConfirmMessage() != null) {
                            pi.setString(5, up[a].getConfirmMessage());
                        } else {
                            pi.setNull(5, java.sql.Types.VARCHAR);
                        }
                        if (up[a].getAlarmMinutes() >= 0) {
                            pi.setInt(6, up[a].getAlarmMinutes());
                        } else {
                            pi.setNull(6, java.sql.Types.INTEGER);
                        }
                        if (up[a].getAlarmMinutes() >= 0 && up[a].getIdentifier() == uid) {
                            long la = up[a].getAlarmMinutes() * 60000L;
                            changeReminder(cdao.getObjectID(), uid, cdao.getEffectiveFolderId(), cdao.getContext(), cdao.isSequence(true), cdao.getEndDate(), new java.util.Date(cdao.getStartDate().getTime()-la), CalendarOperation.INSERT);
                        }
                        pi.setInt(7, cdao.getContextID());
                        CalendarCommonCollection.checkUserParticipantObject(up[a], cdao.getFolderType());
                        pi.addBatch();
                    }
                }
                pi.executeBatch();
            } finally {
                CalendarCommonCollection.closePreparedStatement(pi);
            }
        } else {
            throw new OXMandatoryFieldException(Component.APPOINTMENT, 1000011, "UserParticipant is empty!");
        }
    }
    
    public final void getParticipantsSQLIn(final ArrayList al, final Connection readcon, final int cid, final String sqlin) throws SQLException {
        
        final Statement stmt = readcon.createStatement();
        final StringBuilder query = new StringBuilder(128);
        query.append("SELECT object_id, id, type, dn, ma from prg_date_rights WHERE cid = ");
        query.append(cid);
        query.append(PARTICIPANTS_IDENTIFIER_IN);
        query.append(sqlin);
        query.append(" ORDER BY object_id ASC");
        final ResultSet rs = stmt.executeQuery(query.toString());
        int last_oid = -1;
        Participants participants = null;
        CalendarDataObject cdao = null;
        Participant participant = null;
        try {
            while (rs.next()) {
                final int oid = rs.getInt(1);
                if (last_oid != oid) {
                    if (participants != null) {
                        if (cdao != null) {
                            cdao.setParticipants(participants.getList());
                        }
                    }
                    participants = new Participants();
                    last_oid = oid;
                    cdao = CalendarCommonCollection.getDAOFromList(al, oid);
                }
                
                final int id = rs.getInt(2);
                final int type = rs.getInt(3);
                if (type == Participant.USER) {
                    participant = new UserParticipant(id);
                } else if (type == Participant.GROUP) {
                    participant = new GroupParticipant(id);
                } else if (type == Participant.RESOURCE) {
                    participant = new ResourceParticipant(id);
                    cdao.setContainsResources(true);
                } else if (type == Participant.RESOURCEGROUP) {
                    participant = new ResourceGroupParticipant(id);
                } else if (type == Participant.EXTERNAL_USER) {
                    String temp = rs.getString(4);
                    if (rs.wasNull()) {
                        temp = null;
                    }
                    String temp2 = rs.getString(5);
                    if (!rs.wasNull()) {
                        participant = new ExternalUserParticipant(temp2);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    } else {
                        participant = null;
                    }
                } else if (type == Participant.EXTERNAL_GROUP) {
                    String temp = rs.getString(4);
                    if (rs.wasNull()) {
                        temp = null;
                    }
                    String temp2 = rs.getString(5);
                    if (!rs.wasNull()) {
                        participant = new ExternalGroupParticipant(temp2);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    } else {
                        participant = null;
                    }
                } else {
                    LOG.warn("Unknown type detected for Participant :"+type);
                }
                if (participant != null) {
                    participants.add(participant);
                }
            }
            if (cdao != null && cdao.getObjectID() == last_oid) {
                cdao.setParticipants(participants.getList());
            }
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closeStatement(stmt);
        }
    }
    
    public final Participants getParticipants(final CalendarDataObject cdao, final Connection readcon) throws SQLException {
        final Participants participants = new Participants();
        final Statement stmt = readcon.createStatement();
        final StringBuilder query = new StringBuilder(128);
        query.append("SELECT id, type, dn, ma from prg_date_rights WHERE cid = ");
        query.append(cdao.getContextID());
        query.append(PARTICIPANTS_IDENTIFIER_IS);
        query.append(cdao.getObjectID());
        final ResultSet rs = stmt.executeQuery(query.toString());
        try {
            while (rs.next()) {
                Participant participant = null;
                final int id = rs.getInt(1);
                final int type = rs.getInt(2);
                if (type == Participant.USER) {
                    participant = new UserParticipant(id);
                } else if (type == Participant.GROUP) {
                    participant = new GroupParticipant(id);
                } else if (type == Participant.RESOURCE) {
                    participant = new ResourceParticipant(id);
                    cdao.setContainsResources(true);
                } else if (type == Participant.RESOURCEGROUP) {
                    participant = new ResourceGroupParticipant(id);
                } else if (type == Participant.EXTERNAL_USER) {
                    String temp = rs.getString(3);
                    if (rs.wasNull()) {
                        temp = null;
                    }
                    String temp2 = rs.getString(4);
                    if (!rs.wasNull()) {
                        participant = new ExternalUserParticipant(temp2);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    } else {
                        participant = null;
                    }
                } else if (type == Participant.EXTERNAL_GROUP) {
                    String temp = rs.getString(3);
                    if (rs.wasNull()) {
                        temp = null;
                    }
                    String temp2 = rs.getString(4);
                    if (!rs.wasNull()) {
                        participant = new ExternalGroupParticipant(temp2);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    } else {
                        participant = null;
                    }
                } else {
                    LOG.warn("Unknown type detected for Participant :"+type);
                }
                if (participant != null) {
                    participants.add(participant);
                }
            }
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closeStatement(stmt);
        }
        return participants;
    }
    
    public final void getUserParticipantsSQLIn(final ArrayList al, final Connection readcon, final int cid, final int uid, final String sqlin) throws SQLException, OXException {
        final Statement stmt = readcon.createStatement();
        final StringBuilder query = new StringBuilder(140);
        query.append("SELECT object_id, member_uid, confirm, reason, pfid, reminder from prg_dates_members WHERE cid = ");
        query.append(cid);
        query.append(PARTICIPANTS_IDENTIFIER_IN);
        query.append(sqlin);
        query.append(" ORDER BY object_id");
        final ResultSet rs = stmt.executeQuery(query.toString());
        String temp = null;
        int last_oid = -1;
        UserParticipant up = null;
        Participants participants = null;
        CalendarDataObject cdao = null;
        try {
            while (rs.next()) {
                final int oid = rs.getInt(1);
                if (last_oid != oid) {
                    if (participants != null) {
                        participants.add(up);
                        if (cdao != null) {
                            cdao.setUsers(participants.getUsers());
                        }
                    }
                    participants = new Participants();
                    last_oid = oid;
                    cdao = CalendarCommonCollection.getDAOFromList(al, oid);
                }
                final int tuid = rs.getInt(2);
                up = new UserParticipant(tuid);
                up.setConfirm(rs.getInt(3));
                temp = rs.getString(4);
                if (!rs.wasNull()) {
                    up.setConfirmMessage(temp);
                }
                final int pfid = rs.getInt(5);
                
                if (!rs.wasNull()) {
                    if (pfid < 1) {
                        LOG.error(StringCollection.convertArraytoString(new Object[] { "ERROR: getUserParticipantsSQLIn oid:uid ", Integer.valueOf(uid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(cdao.getObjectID()) }));
                    }
                    if (cdao.getFolderType() == FolderObject.PRIVATE) {
                        if  (uid == tuid) {
                            cdao.setGlobalFolderID(pfid);
                            cdao.setPrivateFolderID(pfid);
                        }
                        up.setPersonalFolderId(pfid);
                    }  else if (cdao.getFolderType() == FolderObject.SHARED) {
                        if (cdao.getSharedFolderOwner() != 0) {
                            if (cdao.getSharedFolderOwner() == tuid) {
                                cdao.setGlobalFolderID(pfid);
                                cdao.setPrivateFolderID(pfid);
                                up.setPersonalFolderId(pfid);
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                        }
                    } else if (uid == tuid) {
                        if (!cdao.containsParentFolderID()) {
                            cdao.setGlobalFolderID(pfid);
                        }
                        cdao.setPrivateFolderID(pfid);
                    } else {
                        cdao.setActionFolder(pfid);
                    }
                }
                
                final int alarm = rs.getInt(6);
                if (!rs.wasNull()) {
                    up.setAlarmMinutes(alarm);
                    if (up.getIdentifier() == uid && up.getAlarmMinutes() >= 0) {
                        cdao.setAlarm(up.getAlarmMinutes());
                    }
                }
                if (participants != null) {
                    participants.add(up);
                }
            }
            if (cdao != null && cdao.getObjectID() == last_oid) {
                cdao.setUsers(participants.getUsers());
            }
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closeStatement(stmt);
        }
    }
    
    public final Participants getUserParticipants(final CalendarDataObject cdao, final Connection readcon, final int uid) throws SQLException, OXException {
        final Participants participants = new Participants();
        final Statement stmt = readcon.createStatement();
        final StringBuilder query = new StringBuilder(140);
        query.append("SELECT member_uid, confirm, reason, pfid, reminder from prg_dates_members WHERE cid = ");
        query.append(cdao.getContextID());
        query.append(PARTICIPANTS_IDENTIFIER_IS);
        query.append(cdao.getObjectID());
        final ResultSet rs = stmt.executeQuery(query.toString());
        String temp = null;
        try {
            while (rs.next()) {
                final int tuid = rs.getInt(1);
                final UserParticipant up = new UserParticipant(tuid);
                up.setConfirm(rs.getInt(2));
                temp = rs.getString(3);
                if (!rs.wasNull()) {
                    up.setConfirmMessage(temp);
                }
                final int pfid = rs.getInt(4);
                if (!rs.wasNull()) {
                    if (pfid < 1) {
                        LOG.error(StringCollection.convertArraytoString(new Object[] { "ERROR: getUserParticipants oid:uid ",Integer.valueOf(uid),Character.valueOf(CalendarOperation.COLON),Integer.valueOf(cdao.getObjectID()) }));
                    }
                    if (cdao.getFolderType() == FolderObject.PRIVATE) {
                        if  (uid == tuid) {
                            cdao.setGlobalFolderID(pfid);
                            cdao.setPrivateFolderID(pfid);
                        }
                        up.setPersonalFolderId(pfid);
                    }  else if (cdao.getFolderType() == FolderObject.SHARED) {
                        if (cdao.getSharedFolderOwner() != 0) {
                            if (cdao.getSharedFolderOwner() == tuid) {
                                cdao.setGlobalFolderID(pfid);
                                cdao.setPrivateFolderID(pfid);
                                up.setPersonalFolderId(pfid);
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                        }
                    } else if (uid == tuid) {
                        cdao.setGlobalFolderID(pfid);
                        cdao.setPrivateFolderID(pfid);
                    } else {
                        cdao.setActionFolder(pfid);
                    }
                }
                final int alarm = rs.getInt(5);
                if (!rs.wasNull()) {
                    up.setAlarmMinutes(alarm);
                    if (up.containsAlarm() && up.getAlarmMinutes() >= 0 && up.getIdentifier() == uid) {
                        cdao.setAlarm(up.getAlarmMinutes());
                    }
                }
                participants.add(up);
            }
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closeStatement(stmt);
        }
        return participants;
    }
    
    public final CalendarDataObject loadObjectForUpdate(final CalendarDataObject cdao, final Session so, Context ctx, final int inFolder) throws SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException  {
        final CalendarOperation co = new CalendarOperation();
        Connection readcon = null;
        CalendarDataObject edao = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            readcon = DBPool.pickup(ctx);
            int action_folder = inFolder;
            if (cdao.containsParentFolderID()) {
                action_folder = cdao.getParentFolderID();
            }
            prep = getPreparedStatement(readcon, loadAppointment(cdao.getObjectID(), cdao.getContext()));
            rs = getResultSet(prep);
            edao = co.loadAppointment(rs, cdao.getObjectID(), inFolder, this, readcon, so, ctx, CalendarOperation.UPDATE, action_folder);
        } catch(final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch(final OXPermissionException oxpe ) {
            throw oxpe;
        } catch(final OXObjectNotFoundException oxonfe) {
            throw oxonfe;
        } catch(final OXException oxe) {
            throw oxe;
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(prep);
            if (readcon != null) {
                try {
                    DBPool.push(ctx, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("DBPoolingException:updateAppointment (push)", dbpe);
                }
            }
        }
        return edao;
    }
    
    
    public final CalendarDataObject[] updateAppointment(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final Session so, Context ctx, final int inFolder, final java.util.Date clientLastModified) throws SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException, OXConcurrentModificationException {
        return updateAppointment(cdao, edao, writecon, so, ctx, inFolder, clientLastModified, true);
    }
    
    final CalendarDataObject[] updateAppointment(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified, final boolean clientLastModifiedCheck) throws DataTruncation, SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException, OXConcurrentModificationException {
        
        final CalendarOperation co = new CalendarOperation();
        
        if (cdao.getFolderMove() && cdao.getFolderType() == FolderObject.PUBLIC && edao.getPrivateFlag()) {
            throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.PRIVATE_MOVE_TO_PUBLIC));
        }
        
        CalendarCommonCollection.detectFolderMoveAction(cdao, edao);
        
        if (clientLastModified == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        } else if (edao.getLastModified() == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        }
        
        if (clientLastModifiedCheck && edao.getLastModified().getTime() > clientLastModified.getTime()) {
            throw new OXConcurrentModificationException(Component.APPOINTMENT, OXConcurrentModificationException.ConcurrentModificationCode.CONCURRENT_MODIFICATION);
        }
        
        final int rec_action = co.checkUpdateRecurring(cdao, edao);
        if (edao.containsRecurrencePosition() && edao.getRecurrencePosition() > 0) {
            if (cdao.getFolderMove()) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_MOVE_EXCEPTION);
            }
            if (edao.containsPrivateFlag() && cdao.containsPrivateFlag() && edao.getPrivateflag() != cdao.getPrivateflag()) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_PRIVATE_FLAG);
            }
        }
        
        CalendarDataObject clone = null;
        
        if (rec_action == CalendarRecurringCollection.CHANGE_RECURRING_TYPE) {
            final ArrayList<Integer> exceptions = getExceptionList(null, ctx, edao.getRecurrenceID());
            if (exceptions != null && exceptions.size() > 0) {
                final Object oids[] = exceptions.toArray();
                deleteAllRecurringExceptions(StringCollection.getSqlInString(oids), so, writecon);
                for (int a = 0; a < exceptions.size(); a++) {
                    triggerDeleteEvent(exceptions.get(a).intValue(), inFolder, so, ctx, null, null);
                }
            }
        } else if (rec_action == CalendarRecurringCollection.RECURRING_EXCEPTION_DELETE) {
            final ArrayList<Integer> exceptions = getExceptionList(null, ctx, edao.getRecurrenceID());
            if (exceptions != null && exceptions.size() > 0) {
                final Object oids[] = exceptions.toArray();
                deleteAllRecurringExceptions(StringCollection.getSqlInString(oids), so, writecon);
                for (int a = 0; a < exceptions.size(); a++) {
                    triggerDeleteEvent(exceptions.get(a).intValue(), inFolder, so, ctx, null, null);
                }
            }
            CalendarCommonCollection.purgeExceptionFieldsFromObject(cdao);
        } else if (rec_action == CalendarRecurringCollection.RECURRING_CREATE_EXCEPTION) {
            // Because the GUI only sends changed fields, we have to create a merged object
            // from cdao and edao and then we force an insert!
            if (edao.containsPrivateFlag() && cdao.containsPrivateFlag() && edao.getPrivateflag() != cdao.getPrivateflag()) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_PRIVATE_FLAG);
            }
            clone = CalendarRecurringCollection.cloneObjectForRecurringException(cdao, edao);
            try {
                cdao.setRecurrenceCalculator(edao.getRecurrenceCalculator());
                if (cdao.containsAlarm()) {
                    if (cdao.containsUserParticipants() && cdao.getUsers() != null) {
                        CalendarCommonCollection.checkAndModifyAlarm(cdao, cdao.getUsers(), so.getUserId(), edao.getUsers());
                    } else {
                        CalendarCommonCollection.checkAndModifyAlarm(cdao, edao.getUsers(), so.getUserId(), edao.getUsers());
                    }
                    cdao.removeAlarm();
                }
                insertAppointment(clone, writecon, so);
                CalendarCommonCollection.removeFieldsFromObject(cdao);
                // no update here
                cdao.setParticipants(edao.getParticipants());
                cdao.setUsers(edao.getUsers());
                cdao.setRecurrence(edao.getRecurrence());
                cdao.setLastModified(clone.getLastModified());
            } catch( final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR);
            } catch(final LdapException ldape) {
                throw new OXException(ldape);
            } catch(final OXCalendarException oxce) {
                throw oxce;
            } catch(final OXException oxe) {
                throw oxe;
            } catch(final Exception ex) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, ex, Integer.valueOf(2));
            }
        }
        
        final int ucols[] = new int[26];
        int uc = CalendarOperation.fillUpdateArray(cdao, edao, ucols);
        boolean cup = false;
        if (uc > 0 || CalendarCommonCollection.check(cdao.getUsers(), edao.getUsers())) {
            
            ucols[uc++] = AppointmentObject.LAST_MODIFIED;
            ucols[uc++] = AppointmentObject.MODIFIED_BY;
            
            final StringBuilder update = new StringBuilder();
            update.append("UPDATE prg_dates pd ");
            for (int a  = 0; a < uc; a++) {
                if (a != 0) {
                    update.append(", ");
                    update.append(CalendarCommonCollection.getFieldName(ucols[a]));
                    update.append(" = ?");
                } else {
                    update.append("SET ");
                    update.append(CalendarCommonCollection.getFieldName(ucols[a]));
                    update.append(" = ?");
                }
            }
            
            update.append(" WHERE cid = ");
            update.append(cdao.getContextID());
            update.append(DATES_IDENTIFIER_IS);
            update.append(cdao.getObjectID());
            update.append(" AND changing_date <= ");
            if (clientLastModifiedCheck) {
                update.append(clientLastModified.getTime());
            } else {
                update.append(System.currentTimeMillis());
            }
            
            PreparedStatement pst = null;
            
            try {
                
                pst = writecon.prepareStatement(update.toString());
                
                for (int a = 0; a < uc; a++) {
                    switch (ucols[a]) {
                        case AppointmentObject.TITLE:
                            pst.setString(a+1, cdao.getTitle());
                            break;
                        case AppointmentObject.START_DATE:
                            pst.setTimestamp(a+1, new java.sql.Timestamp(cdao.getStartDate().getTime()));
                            break;
                        case AppointmentObject.END_DATE:
                            pst.setTimestamp(a+1, new java.sql.Timestamp(cdao.getEndDate().getTime()));
                            break;
                        case AppointmentObject.SHOWN_AS:
                            pst.setInt(a+1, cdao.getShownAs());
                            break;
                        case AppointmentObject.LOCATION:
                            if (cdao.getLocation() != null) {
                                pst.setString(a+1, cdao.getLocation());
                            } else {
                                pst.setNull(a+1, java.sql.Types.VARCHAR);
                            }
                            break;
                        case AppointmentObject.NOTE:
                            if (cdao.getNote() != null) {
                                pst.setString(a+1, cdao.getNote());
                            } else {
                                pst.setNull(a+1, java.sql.Types.VARCHAR);
                            }
                            break;
                        case AppointmentObject.CATEGORIES:
                            if (cdao.getCategories() != null) {
                                pst.setString(a+1, cdao.getCategories());
                            } else {
                                pst.setNull(a+1, java.sql.Types.VARCHAR);
                            }
                            break;
                        case AppointmentObject.FULL_TIME:
                            pst.setInt(a+1, cdao.getFulltime());
                            break;
                        case AppointmentObject.COLOR_LABEL:
                            pst.setInt(a+1, cdao.getLabel());
                            break;
                        case AppointmentObject.MODIFIED_BY:
                            pst.setInt(a+1, cdao.getModifiedBy());
                            break;
                        case AppointmentObject.LAST_MODIFIED:
                            if (!cdao.containsLastModified()) {
                                final Timestamp t = new Timestamp(System.currentTimeMillis());
                                pst.setLong(a+1, t.getTime());
                                cdao.setLastModified(t);
                            } else {
                                pst.setLong(a+1, cdao.getLastModified().getTime());
                            }
                            break;
                        case AppointmentObject.PRIVATE_FLAG:
                            pst.setInt(a+1, cdao.getPrivateflag());
                            break;
                        case AppointmentObject.FOLDER_ID:
                            if (cdao.getFolderType() == FolderObject.PRIVATE || cdao.getFolderType() == FolderObject.SHARED) {
                                pst.setInt(a+1, 0);
                            } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                                pst.setInt(a+1, cdao.getGlobalFolderID());
                            } else {
                                throw new OXCalendarException(OXCalendarException.Code.NOT_YET_SUPPORTED);
                            }
                            break;
                        case AppointmentObject.RECURRENCE_TYPE:
                            pst.setString(a+1, cdao.getRecurrence());
                            break;
                        case AppointmentObject.RECURRENCE_ID:
                            pst.setInt(a+1, cdao.getRecurrenceID());
                            break;
                        case AppointmentObject.DELETE_EXCEPTIONS:
                            pst.setString(a+1, cdao.getDelExceptions());
                            break;
                        case AppointmentObject.CHANGE_EXCEPTIONS:
                            pst.setString(a+1, cdao.getExceptions());
                            break;
                        case AppointmentObject.RECURRENCE_CALCULATOR:
                            pst.setInt(a+1, cdao.getRecurrenceCalculator());
                            break;
                        case AppointmentObject.RECURRENCE_POSITION:
                            pst.setInt(a+1, cdao.getRecurrencePosition());
                            break;
                        case AppointmentObject.NUMBER_OF_ATTACHMENTS:
                            pst.setInt(a+1, cdao.getNumberOfAttachments());
                            break;
                        case CalendarDataObject.TIMEZONE:
                            pst.setString(a+1, cdao.getTimezone());
                            break;
                        default:
                            throw new SQLException("Error: Calendar: Update: Mapping for " + ucols[a] + " not implemented!");
                    }
                }
                updateParticipants(cdao, edao, so.getUserId(), so.getContextId(), writecon, cup);
                final int ret = pst.executeUpdate();
                if (ret == 0) {
                    throw new OXConcurrentModificationException(Component.APPOINTMENT, OXConcurrentModificationException.ConcurrentModificationCode.CONCURRENT_MODIFICATION);
                }
            } finally {
                CalendarCommonCollection.closePreparedStatement(pst);
            }
            
        }
        cdao.setParentFolderID(cdao.getActionFolder());
        
        boolean solo_reminder = CalendarCommonCollection.checkForSoloReminderUpdate(cdao, uc, cup);
        CalendarCommonCollection.checkAndRemovePastReminders(cdao, edao);
        if (!solo_reminder) {
            CalendarCommonCollection.triggerEvent(so, CalendarOperation.UPDATE, cdao);
        }
        if (clone != null) {
            cdao.setObjectID(clone.getObjectID());
            cdao.setLastModified(clone.getLastModified());
        }
        return null;
    }
    
    private final void updateParticipants(final CalendarDataObject cdao, final CalendarDataObject edao, final int uid, final int cid, final Connection writecon, boolean cup) throws SQLException, OXException, LdapException {
        final Participant participants[] = cdao.getParticipants();
        UserParticipant users[] = cdao.getUsers();
        
        if (users == null && cdao.getFolderMoveAction() != CalendarOperation.NO_MOVE_ACTION) {
            users = edao.getUsers();
            CalendarOperation.fillUserParticipants(cdao);
        }
        
        final Participant old_participants[] = edao.getParticipants();
        final UserParticipant old_users[] = edao.getUsers();
        
        int check_up = old_users.length;
        
        Participant new_participants[] = null;
        Participant deleted_participants[] = null;
        
        UserParticipant new_userparticipants[] = null;
        UserParticipant modified_userparticipants[] = null;
        UserParticipant deleted_userparticipants[] = null;
        
        final Participants deleted = new Participants();
        final Participants new_deleted = new Participants();
        
        if (participants != null && !Arrays.equals(participants, old_participants)) {
            Arrays.sort(participants);
            Arrays.sort(old_participants);
            new_participants = CalendarOperation.getNewParticipants(participants, old_participants);
            deleted_participants = CalendarOperation.getDeletedParticipants(old_participants, participants);
        }
        
        final boolean time_change = CalendarCommonCollection.detectTimeChange(cdao, edao);
        
        if (time_change && users == null) {
            users = edao.getUsers();
        }
        
        if (users != null) {
            Arrays.sort(users);
            Arrays.sort(old_users);
            final Participants p[] = CalendarOperation.getModifiedUserParticipants(users, old_users, edao.getCreatedBy(), uid, time_change, cdao);
            if (p[0] != null) {
                new_userparticipants = p[0].getUsers();
                if (new_userparticipants != null) {
                    check_up += new_userparticipants.length;
                }
            }
            if (p[1] != null) {
                modified_userparticipants = p[1].getUsers();
            }
            deleted_userparticipants = CalendarOperation.getDeletedUserParticipants(old_users, users, uid);
            if (deleted_userparticipants != null) {
                check_up -= deleted_userparticipants.length;
            }
        }
        
        modified_userparticipants = CalendarCommonCollection.checkAndModifyAlarm(cdao, modified_userparticipants, uid, edao.getUsers());
        
        
        if (check_up < 1) {
            throw new OXCalendarException(OXCalendarException.Code.UPDATE_WITHOUT_PARTICIPANTS);
        }
        
        if (new_participants != null && new_participants.length > 0) {
            cup = true;
            PreparedStatement dr = null;
            try {
                dr = writecon.prepareStatement("insert into prg_date_rights (object_id, cid, id, type, dn, ma) values (?, ?, ?, ?, ?, ?)");
                Arrays.sort(new_participants);
                int lastid = -1;
                int lasttype = -1;
                for (int a = 0; a < new_participants.length; a++) {
                    if (new_participants[a].getIdentifier() == 0 && new_participants[a].getType() == Participant.EXTERNAL_USER && new_participants[a].getEmailAddress() != null) {
                        ExternalUserParticipant eup = new ExternalUserParticipant(new_participants[a].getEmailAddress());
                        eup.setIdentifier(new_participants[a].getEmailAddress().hashCode());
                        eup.setDisplayName(new_participants[a].getDisplayName());
                        new_participants[a] = eup;
                    }
                    if (!(lastid == new_participants[a].getIdentifier() && lasttype == new_participants[a].getType())) {
                        lastid = new_participants[a].getIdentifier();
                        lasttype = new_participants[a].getType();
                        dr.setInt(1, cdao.getObjectID());
                        dr.setInt(2, cid);
                        dr.setInt(3, new_participants[a].getIdentifier());
                        dr.setInt(4, new_participants[a].getType());
                        if (new_participants[a].getDisplayName() == null) {
                            dr.setNull(5, java.sql.Types.VARCHAR);
                        } else {
                            dr.setString(5, new_participants[a].getDisplayName());
                        }
                        if (new_participants[a].getEmailAddress() == null) {
                            if (new_participants[a].getIdentifier() == 0) {
                                throw new OXCalendarException(OXCalendarException.Code.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD);
                            }
                            dr.setNull(6, java.sql.Types.VARCHAR);
                        } else {
                            dr.setString(6, new_participants[a].getEmailAddress());
                        }
                        dr.addBatch();
                    }
                }
                dr.executeBatch();
            } finally {
                CalendarCommonCollection.closePreparedStatement(dr);
            }
        }
        
        if (deleted_participants != null && deleted_participants.length > 0) {
            cup = true;
            PreparedStatement pd = null;
            PreparedStatement pde = null;
            try {
                pd = writecon.prepareStatement("delete from prg_date_rights WHERE object_id = ? AND cid = ? AND id = ? AND type = ?");
                for (int a = 0; a < deleted_participants.length; a++) {
                    if (deleted_participants[a].getType() != Participant.EXTERNAL_USER && deleted_participants[a].getType() != Participant.EXTERNAL_GROUP) {
                        pd.setInt(1, cdao.getObjectID());
                        pd.setInt(2, cid);
                        pd.setInt(3, deleted_participants[a].getIdentifier());
                        pd.setInt(4, deleted_participants[a].getType());
                        pd.addBatch();
                    } else {
                        if (pde == null) {
                            pde = writecon.prepareStatement("delete from prg_date_rights WHERE object_id = ? AND cid = ? AND type = ? AND ma LIKE ?");
                        }
                        pde.setInt(1, cdao.getObjectID());
                        pde.setInt(2, cid);
                        pde.setInt(3, deleted_participants[a].getType());
                        pde.setString(4, deleted_participants[a].getEmailAddress());
                        pde.addBatch();
                    }
                }
                pd.executeBatch();
                if (pde != null) {
                    pde.executeBatch();
                    CalendarCommonCollection.closePreparedStatement(pde);
                }
            } finally {
                CalendarCommonCollection.closePreparedStatement(pd);
            }
        }
        
        if (new_userparticipants != null && new_userparticipants.length > 0) {
            cup = true;
            PreparedStatement pi = null;
            try {
                pi = writecon.prepareStatement("insert into prg_dates_members (object_id, member_uid, confirm, reason, pfid, reminder, cid) values (?, ?, ?, ?, ?, ?, ?)");
                Arrays.sort(new_userparticipants);
                int lastid = -1;
                final OXFolderAccess access = new OXFolderAccess(cdao.getContext());
                for (int a = 0; a < new_userparticipants.length; a++) {
                    if (lastid != new_userparticipants[a].getIdentifier()) {
                        lastid = new_userparticipants[a].getIdentifier();
                        pi.setInt(1, cdao.getObjectID());
                        pi.setInt(2, new_userparticipants[a].getIdentifier());
                        if (uid == new_userparticipants[a].getIdentifier()) {
                            if (new_userparticipants[a].getConfirm() == 0) {
                                pi.setInt(3, 1); // AUTO CONFIRM CREATOR
                            } else {
                                pi.setInt(3, new_userparticipants[a].getConfirm());
                            }
                        } else {
                            pi.setInt(3, new_userparticipants[a].getConfirm());
                        }
                        if (new_userparticipants[a].getConfirmMessage() != null) {
                            pi.setString(4, new_userparticipants[a].getConfirmMessage());
                        } else {
                            pi.setNull(4, java.sql.Types.VARCHAR);
                        }
                        
                        if (edao.getFolderType() == FolderObject.PRIVATE) {
                            if (new_userparticipants[a].getIdentifier() == uid) {
                                if (cdao.getGlobalFolderID() != 0) {
                                    pi.setInt(5, cdao.getGlobalFolderID());
                                    new_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                } else {
                                    try {
                                        final int pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(new_userparticipants[a].getIdentifier(), cdao.getContext()));
                                        pi.setInt(5, pfid);
                                        new_userparticipants[a].setPersonalFolderId(pfid);
                                    } catch (final Exception fe) {
                                        throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(3));
                                    }
                                }
                            } else {
                                try {
                                    int pfid = cdao.getGlobalFolderID();
                                    if (pfid == 0) {
                                        pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                    }
                                    //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(new_userparticipants[a].getIdentifier(), cdao.getContext()));
                                    pi.setInt(5, pfid);
                                    new_userparticipants[a].setPersonalFolderId(pfid);
                                } catch(final Exception fe) {
                                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(4));
                                }
                            }
                        } else if (edao.getFolderType() == FolderObject.PUBLIC) {
                            pi.setNull(5, java.sql.Types.INTEGER);
                        } else if (edao.getFolderType() == FolderObject.SHARED) {
                            if (edao.getSharedFolderOwner() != 0) {
                                if (edao.getSharedFolderOwner() == new_userparticipants[a].getIdentifier()) {
                                    if (cdao.getGlobalFolderID() != 0) {
                                        pi.setInt(5, cdao.getGlobalFolderID());
                                        new_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                    } else {
                                        try {
                                            final int pfid = access.getDefaultFolder(edao.getSharedFolderOwner(), FolderObject.CALENDAR).getObjectID();
                                            //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(edao.getSharedFolderOwner(), cdao.getContext()));
                                            pi.setInt(5, pfid);
                                            new_userparticipants[a].setPersonalFolderId(pfid);
                                        } catch (final Exception fe) {
                                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(5));
                                        }
                                    }
                                } else {
                                    if (cdao.getGlobalFolderID() != 0) {
                                        pi.setInt(5, cdao.getGlobalFolderID());
                                        new_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                    } else {
                                        try {
                                            final int pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                            //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(new_userparticipants[a].getIdentifier(), cdao.getContext()));
                                            pi.setInt(5, pfid);
                                            new_userparticipants[a].setPersonalFolderId(pfid);
                                        } catch (final Exception fe) {
                                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(3));
                                        }
                                    }
                                }
                            } else {
                                throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.FOLDER_TYPE_UNRESOLVEABLE);
                        }
                        
                        if (new_userparticipants[a].getAlarmMinutes() >= 0 && new_userparticipants[a].containsAlarm()) {
                            pi.setInt(6, new_userparticipants[a].getAlarmMinutes());
                            long la = new_userparticipants[a].getAlarmMinutes() * 60000L;
                            java.util.Date calc_date = null;
                            java.util.Date end_date = null;
                            if (cdao.containsStartDate()) {
                                calc_date = cdao.getStartDate();
                            } else {
                                calc_date = edao.getStartDate();
                            }
                            if (cdao.containsEndDate()) {
                                end_date = cdao.getEndDate();
                            } else {
                                end_date = edao.getEndDate();
                            }
                            changeReminder(cdao.getObjectID(), uid, cdao.getEffectiveFolderId(), cdao.getContext(), cdao.isSequence(true), end_date, new java.util.Date(calc_date.getTime()-la), CalendarOperation.INSERT);
                        } else {
                            pi.setNull(6, java.sql.Types.INTEGER);
                        }
                        
                        pi.setInt(7, cid);
                        CalendarCommonCollection.checkUserParticipantObject(new_userparticipants[a], cdao.getFolderType());
                        pi.addBatch();
                        if (checkForDeletedParticipants(new_userparticipants[a].getIdentifier(), cdao.getContextID(), cdao.getObjectID(), cdao.getContext())) {
                            deleted.add(new_userparticipants[a]);
                        }
                    }
                }
                pi.executeBatch();
            } finally {
                CalendarCommonCollection.closePreparedStatement(pi);
            }
        }
        
        if (modified_userparticipants != null && modified_userparticipants.length > 0) {
            cup = true;
            PreparedStatement pu = null;
            try {
                pu = writecon.prepareStatement("update prg_dates_members SET confirm = ?, reason = ?, pfid = ?, reminder = ? WHERE object_id = ? AND cid = ? and member_uid = ?");
                final OXFolderAccess access = new OXFolderAccess(cdao.getContext());
                for (int a = 0; a < modified_userparticipants.length; a++) {
                    // TODO: Enhance this and add a condition for lastid
                    pu.setInt(1, modified_userparticipants[a].getConfirm());
                    if (modified_userparticipants[a].getConfirmMessage() != null) {
                        pu.setString(2, modified_userparticipants[a].getConfirmMessage());
                    } else {
                        pu.setNull(2, java.sql.Types.VARCHAR);
                    }
                    if (modified_userparticipants[a].getIdentifier() == uid) {
                        if (cdao.getFolderType() == FolderObject.PRIVATE) {
                            if (cdao.getGlobalFolderID() != 0) {
                                pu.setInt(3, cdao.getGlobalFolderID());
                                modified_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                            } else {
                                try {
                                    int pfid = 0;
                                    if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                        pfid = modified_userparticipants[a].getPersonalFolderId();
                                    } else {
                                        pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        //pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(modified_userparticipants[a].getIdentifier(), cdao.getContext()));
                                        modified_userparticipants[a].setPersonalFolderId(pfid);
                                    }
                                    pu.setInt(3, pfid);
                                } catch (final Exception fe) {
                                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(6));
                                }
                            }
                        } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                            pu.setNull(3, java.sql.Types.INTEGER);
                        } else if (cdao.getFolderType() == FolderObject.SHARED) {
                            if (modified_userparticipants[a].getIdentifier() == uid && uid == cdao.getSharedFolderOwner()) {
                                if (cdao.getGlobalFolderID() != 0) {
                                    pu.setInt(3, cdao.getGlobalFolderID());
                                    modified_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                } else {
                                    try {
                                        int pfid = 0;
                                        if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                            pfid = modified_userparticipants[a].getPersonalFolderId();
                                        } else {
                                            pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                            //pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(modified_userparticipants[a].getIdentifier(), cdao.getContext()));
                                            modified_userparticipants[a].setPersonalFolderId(pfid);
                                        }
                                        pu.setInt(3, pfid);
                                    } catch (final Exception fe) {
                                        throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(6));
                                    }
                                }
                            } else {
                                try {
                                    int pfid = 0;
                                    if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                        pfid = modified_userparticipants[a].getPersonalFolderId();
                                    } else {
                                        pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        //pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(modified_userparticipants[a].getIdentifier(), cdao.getContext()));
                                        modified_userparticipants[a].setPersonalFolderId(pfid);
                                    }
                                    pu.setInt(3, pfid);
                                } catch (final Exception fe) {
                                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(6));
                                }
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.FOLDER_TYPE_UNRESOLVEABLE);
                        }
                    } else {
                        if (cdao.getFolderType() == FolderObject.PRIVATE) {
                            int pfid = 0;
                            if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                pfid = modified_userparticipants[a].getPersonalFolderId();
                            } else {
                                pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                modified_userparticipants[a].setPersonalFolderId(pfid);
                            }
                            pu.setInt(3, pfid);
                        } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                            pu.setNull(3, java.sql.Types.INTEGER);
                        } else if (cdao.getFolderType() == FolderObject.SHARED) {
                            if (edao.getSharedFolderOwner() != 0) {
                                if (edao.getSharedFolderOwner() == modified_userparticipants[a].getIdentifier()) {
                                    if (cdao.getGlobalFolderID() != 0) {
                                        pu.setInt(3, cdao.getGlobalFolderID());
                                        modified_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                    } else {
                                        if (cdao.getActionFolder() == 0) {
                                            try {
                                                final int pfid = access.getDefaultFolder(edao.getSharedFolderOwner(), FolderObject.CALENDAR).getObjectID();
                                                //final int pfid = Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(edao.getSharedFolderOwner(), cdao.getContext()));
                                                pu.setInt(3, pfid);
                                                modified_userparticipants[a].setPersonalFolderId(pfid);
                                            } catch (final Exception fe) {
                                                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(7));
                                            }
                                        } else {
                                            pu.setInt(3, cdao.getActionFolder());
                                            modified_userparticipants[a].setPersonalFolderId(cdao.getActionFolder());
                                        }
                                    }
                                }
                            } else {
                                throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.FOLDER_TYPE_UNRESOLVEABLE);
                        }
                    }
                    if (modified_userparticipants[a].getAlarmMinutes() >= 0 && modified_userparticipants[a].containsAlarm()) {
                        pu.setInt(4, modified_userparticipants[a].getAlarmMinutes());
                        java.util.Date calc_date = null;
                        java.util.Date end_date = null;
                        if (cdao.containsStartDate()) {
                            calc_date = cdao.getStartDate();
                        } else {
                            calc_date = edao.getStartDate();
                        }
                        if (cdao.containsEndDate()) {
                            end_date = cdao.getEndDate();
                        } else {
                            end_date = edao.getEndDate();
                        }
                        int folder_id = modified_userparticipants[a].getPersonalFolderId();
                        if (folder_id <= 0) {
                            folder_id = cdao.getEffectiveFolderId();
                        }
                        long la = modified_userparticipants[a].getAlarmMinutes() * 60000L;
                        java.util.Date reminder = new java.util.Date(calc_date.getTime()-la);
                        changeReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), folder_id, cdao.getContext(), cdao.isSequence(true), end_date, reminder, CalendarOperation.UPDATE);
                    } else {
                        pu.setNull(4, java.sql.Types.INTEGER);
                        changeReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), -1, cdao.getContext(), cdao.isSequence(true), null, null, CalendarOperation.DELETE);
                    }
                    
                    pu.setInt(5, cdao.getObjectID());
                    pu.setInt(6, cid);
                    pu.setInt(7, modified_userparticipants[a].getIdentifier());
                    CalendarCommonCollection.checkUserParticipantObject(modified_userparticipants[a], cdao.getFolderType());
                    pu.addBatch();
                }
                pu.executeBatch();
            } finally {
                CalendarCommonCollection.closePreparedStatement(pu);
            }
        }
        
        if (deleted_userparticipants != null && deleted_userparticipants.length > 0) {
            cup = true;
            PreparedStatement pd = null;
            try {
                pd = writecon.prepareStatement("delete from prg_dates_members WHERE object_id = ? AND cid = ? AND member_uid LIKE ?");
                for (int a = 0; a < deleted_userparticipants.length; a++) {
                    pd.setInt(1, cdao.getObjectID());
                    pd.setInt(2, cid);
                    pd.setInt(3, deleted_userparticipants[a].getIdentifier());
                    pd.addBatch();
                    java.util.Date calc_date = null;
                    java.util.Date end_date = null;
                    if (cdao.containsStartDate()) {
                        calc_date = cdao.getStartDate();
                    } else {
                        calc_date = edao.getStartDate();
                    }
                    if (cdao.containsEndDate()) {
                        end_date = cdao.getEndDate();
                    } else {
                        end_date = edao.getEndDate();
                    }
                    changeReminder(cdao.getObjectID(), uid, -1, cdao.getContext(), cdao.isSequence(true), end_date, new java.util.Date(calc_date.getTime()+deleted_userparticipants[a].getAlarmMinutes()), CalendarOperation.DELETE);
                    new_deleted.add(deleted_userparticipants[a]);
                }
                pd.executeBatch();
            } finally {
                CalendarCommonCollection.closePreparedStatement(pd);
            }
        }
        
        boolean del_master_update = false;
        final UserParticipant newdel_up[] = new_deleted.getUsers();
        
        if (newdel_up != null && newdel_up.length > 0) {
            if (!checkForDeletedMasterObject(cdao.getObjectID(), cid, cdao.getContext())) {
                cup = true;
                PreparedStatement pidm = null;
                try {
                    pidm = writecon.prepareStatement("insert into del_dates (creating_date, created_from, changing_date, changed_from, fid, intfield01, cid, pflag) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    pidm.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    pidm.setInt(2, uid);
                    pidm.setLong(3, System.currentTimeMillis());
                    pidm.setInt(4, uid);
                    pidm.setInt(5, cdao.getGlobalFolderID());
                    pidm.setInt(6, cdao.getObjectID());
                    pidm.setInt(7, cid);
                    pidm.setInt(8, cdao.getPrivateflag());
                    pidm.addBatch();
                    pidm.executeBatch();
                } finally {
                    CalendarCommonCollection.closePreparedStatement(pidm);
                }
            }
            PreparedStatement pid = null;
            try {
                pid = writecon.prepareStatement("insert into del_dates_members (object_id, member_uid, pfid, cid, confirm) values (?, ?, ?, ?, ?)");
                for (int a = 0; a < newdel_up.length; a++) {
                    pid.setInt(1, cdao.getObjectID());
                    pid.setInt(2, newdel_up[a].getIdentifier());
                    if (cdao.getGlobalFolderID() != 0) {
                        pid.setNull(3, java.sql.Types.INTEGER);
                    } else {
                        pid.setInt(3, newdel_up[a].getPersonalFolderId());
                    }
                    pid.setInt(4, cid);
                    if (newdel_up[a].containsConfirm()) {
                        pid.setInt(5,newdel_up[a].getConfirm());
                    } else {
                        pid.setNull(5, java.sql.Types.INTEGER);
                    }
                    pid.addBatch();
                }
                pid.executeBatch();
                del_master_update = true;
            } finally {
                CalendarCommonCollection.closePreparedStatement(pid);
            }
        }
        
        final UserParticipant del_up[] = deleted.getUsers();
        
        if (del_up != null && del_up.length > 0) {
            cup = true;
            PreparedStatement pdd = null;
            try {
                pdd = writecon.prepareStatement("delete from del_dates_members WHERE object_id = ? AND cid = ? AND member_uid LIKE ?");
                for (int a = 0; a < del_up.length; a++) {
                    pdd.setInt(1, cdao.getObjectID());
                    pdd.setInt(2, cid);
                    pdd.setInt(3, del_up[a].getIdentifier());
                    pdd.addBatch();
                }
                pdd.executeBatch();
                del_master_update = true;
            } finally {
                CalendarCommonCollection.closePreparedStatement(pdd);
            }
            
            if (new_deleted.getUsers() != null && new_deleted.getUsers().length > 0 && checkIfMasterIsOrphaned(cdao.getObjectID(), cid, cdao.getContext())) {
                PreparedStatement ddd = null;
                try {
                    ddd = writecon.prepareStatement("delete from del_dates WHERE intfield01 = ? AND cid = ?");
                    ddd.setInt(1, cdao.getObjectID());
                    ddd.setInt(2, cid);
                    ddd.addBatch();
                    ddd.executeBatch();
                    del_master_update = false;
                } finally {
                    CalendarCommonCollection.closePreparedStatement(ddd);
                }
            }
        }
        
        if (del_master_update) {
            PreparedStatement ddu = null;
            try {
                ddu = writecon.prepareStatement("update del_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?");
                ddu.setLong(1, System.currentTimeMillis());
                ddu.setInt(2, uid);
                ddu.setInt(3, cdao.getObjectID());
                ddu.setInt(4, cid);
                ddu.addBatch();
                ddu.executeBatch();
            } finally {
                CalendarCommonCollection.closePreparedStatement(ddu);
            }
        }
        
        CalendarCommonCollection.fillEventInformation(cdao, edao, edao.getUsers(), new_userparticipants, deleted_userparticipants, edao.getParticipants(), new_participants, deleted_participants);
        
    }
    
    public final void setUserConfirmation(final int oid, final int uid, final int confirm, final String confirm_message, final Session so, Context ctx) throws OXException {
        Connection writecon = null;
        int changes[];
        PreparedStatement pu = null;
        PreparedStatement mo = null;
        try  {
            final int fid = CalendarCommonCollection.resolveFolderIDForUser(oid, uid, ctx);
            writecon = DBPool.pickupWriteable(ctx);
            writecon.setAutoCommit(false);
            pu = writecon.prepareStatement("update prg_dates_members SET confirm = ?, reason = ? WHERE object_id = ? AND cid = ? and member_uid = ?");
            pu.setInt(1, confirm);
            if (confirm_message != null) {
                pu.setString(2, confirm_message);
            } else {
                pu.setNull(2, java.sql.Types.VARCHAR);
            }
            pu.setInt(3, oid);
            pu.setInt(4, so.getContextId());
            pu.setInt(5, uid);
            pu.addBatch();
            changes = pu.executeBatch();
            if (changes[0] == 1) {
                mo = writecon.prepareStatement("update prg_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?");
                mo.setLong(1, System.currentTimeMillis());
                mo.setInt(2, uid);
                mo.setInt(3, oid);
                mo.setInt(4, so.getContextId());
                mo.addBatch();
                mo.executeBatch();
                final AppointmentObject ao = new AppointmentObject();
                ao.setObjectID(oid);
                if (fid != -1) {
                    ao.setParentFolderID(fid);
                    CalendarCommonCollection.triggerEvent(so, CalendarOperation.UPDATE, ao);
                } else {
                    LOG.warn(StringCollection.convertArraytoString(new Object[] { "Unable to resolve folder id for user:oid:context",Integer.valueOf(uid),Integer.valueOf(oid),Integer.valueOf(so.getContextId())}));
                }
            } else if (changes[0] == 0) {
                LOG.warn(StringCollection.convertArraytoString(new Object[] { "Object not found: setUserConfirmation: prg_dates_members object_id = ",Integer.valueOf(oid) , " cid = ",Integer.valueOf(so.getContextId()) , " uid = ",Integer.valueOf(uid) }));
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.Component.APPOINTMENT, "Unable to set User Confirmation");
            } else {
                LOG.warn(StringCollection.convertArraytoString(new Object[] { "Result of setUserConfirmation was ",Integer.valueOf(changes[0]),". Check prg_dates_members object_id = ",Integer.valueOf(oid) , " cid = ",Integer.valueOf(so.getContextId()) , " uid = ",Integer.valueOf(uid) }));
            }
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch(final SQLException sqle) {
            if (writecon != null) {
                try {
                    writecon.rollback();
                } catch (final SQLException rb) {
                    LOG.error("setUserConfirmation (writecon) error while rollback ", rb);
                }
            }
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } finally {
            CalendarCommonCollection.closePreparedStatement(pu);
            CalendarCommonCollection.closePreparedStatement(mo);
            if (writecon != null) {
                try {
                    writecon.setAutoCommit(true);
                } catch (final SQLException sqle) {
                    LOG.error("setUserConfirmation (writecon) error while setAutoCommit(true) ", sqle);
                }
                DBPool.closeWriterSilent(ctx, writecon);
            }
        }
    }
    
    public final long attachmentAction(final int oid, final int uid, final Context c, final boolean action) throws OXException {
        Connection readcon = null, writecon = null;
        int changes[];
        PreparedStatement pst = null;
        int number_of_attachments = 0;
        ResultSet rs = null;
        PreparedStatement prep = null;
        long last_modified = 0L;
        try  {
            readcon = DBPool.pickup(c);
            final StringBuilder sb = new StringBuilder(96);
            sb.append("SELECT intfield08 FROM prg_dates WHERE intfield01 = ");
            sb.append(oid);
            sb.append(" AND cid = ");
            sb.append(c.getContextId());
            prep = getPreparedStatement(readcon, sb.toString());
            rs = getResultSet(prep);
            if (rs.next()) {
                number_of_attachments = rs.getInt(1);
            } else {
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.Component.APPOINTMENT, "Unable to handle attachment action");
            }
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch(final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(prep);
            if (readcon != null) {
                DBPool.closeReaderSilent(c, readcon);
            }
        }
        
        if (action) {
            number_of_attachments++;
        } else {
            number_of_attachments--;
            if (number_of_attachments < 0) {
                LOG.warn(StringCollection.convertArraytoString(new Object[] { "Object seems to be corrupted: attachmentAction:",Boolean.valueOf(action)," oid:cid:uid ",Integer.valueOf(oid) , Character.valueOf(CalendarOperation.COLON),Integer.valueOf(c.getContextId()) , Character.valueOf(CalendarOperation.COLON),Integer.valueOf(uid) }));
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.Component.APPOINTMENT, "Unable to processed attachment action. Got no attachments");
            }
        }
        
        try  {
            writecon = DBPool.pickupWriteable(c);
            writecon.setAutoCommit(false);
            pst = writecon.prepareStatement("update prg_dates SET changing_date = ?, changed_from = ?, intfield08 = ? WHERE intfield01 = ? AND cid = ?");
            last_modified = System.currentTimeMillis();
            pst.setLong(1, last_modified);
            pst.setInt(2, uid);
            pst.setInt(3, number_of_attachments);
            pst.setInt(4, oid);
            pst.setInt(5, c.getContextId());
            pst.addBatch();
            changes = pst.executeBatch();
            if (changes[0] == 0) {
                LOG.warn(StringCollection.convertArraytoString(new Object[] { "Object not found: attachmentAction: oid:cid:uid ",Integer.valueOf(oid) , Character.valueOf(CalendarOperation.COLON),Integer.valueOf(c.getContextId()) , Character.valueOf(CalendarOperation.COLON),Integer.valueOf(uid) } ));
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.Component.APPOINTMENT, "Unable to processed attachment action (update).");
            }
            LOG.warn(StringCollection.convertArraytoString(new Object[] { "Result of attachmentAction was ",Integer.valueOf(changes[0]),". Check prg_dates oid:cid:uid ",Integer.valueOf(oid) , Character.valueOf(CalendarOperation.COLON),Integer.valueOf(c.getContextId()) , Character.valueOf(CalendarOperation.COLON),Integer.valueOf(uid) } ));
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch(final SQLException sqle) {
            if (writecon != null) {
                try {
                    writecon.rollback();
                } catch (final SQLException rb) {
                    LOG.error("attachmentAction (writecon) error while rollback ", rb);
                }
            }
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } finally {
            CalendarCommonCollection.closePreparedStatement(pst);
            if (writecon != null) {
                try {
                    writecon.setAutoCommit(true);
                } catch (final SQLException sqle) {
                    LOG.error("attachmentAction (writecon) error while setAutoCommit(true) ", sqle);
                }
                DBPool.closeWriterSilent(c, writecon);
            }
        }
        return last_modified;
    }
    
    private final boolean checkIfMasterIsOrphaned(final int oid, final int cid, final Context context) throws OXException, SQLException {
        Connection readcon = null;
        boolean ret = false;
        try {
            readcon  = DBPool.pickup(context);
            final PreparedStatement pst = readcon.prepareStatement("SELECT object_id from del_dates_members WHERE object_id = ? AND cid = ?");
            pst.setInt(1, oid);
            pst.setInt(2, cid);
            final ResultSet rs = getResultSet(pst);
            try {
                ret = rs.next();
            } finally {
                CalendarCommonCollection.closeResultSet(rs);
            }
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (readcon != null) {
                try {
                    DBPool.push(context, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("DBPoolingException:checkIfMasterIsOrphaned (push)", dbpe);
                }
            }
        }
        return ret;
    }
    
    private final boolean checkForDeletedMasterObject(final int oid, final int cid, final Context context) throws OXException, SQLException {
        Connection readcon = null;
        boolean ret = false;
        try {
            readcon  = DBPool.pickup(context);
            final PreparedStatement pst = readcon.prepareStatement("SELECT intfield01 from del_dates WHERE intfield01 = ? AND cid = ?");
            pst.setInt(1, oid);
            pst.setInt(2, cid);
            final ResultSet rs = getResultSet(pst);
            
            try {
                ret = rs.next();
            } finally {
                CalendarCommonCollection.closeResultSet(rs);
                CalendarCommonCollection.closePreparedStatement(pst);
            }
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (readcon != null) {
                try {
                    DBPool.push(context, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("DBPoolingException:checkForDeletedMasterObject (push)", dbpe);
                }
            }
        }
        return ret;
    }
    
    private final boolean checkForDeletedParticipants(final int uid, final int cid, final int oid, final Context context) throws OXException, SQLException {
        Connection readcon = null;
        boolean ret = false;
        try {
            readcon  = DBPool.pickup(context);
            final PreparedStatement pst = readcon.prepareStatement("SELECT object_id from del_dates_members WHERE object_id = ? AND cid = ? AND member_uid = ?");
            pst.setInt(1, oid);
            pst.setInt(2, cid);
            pst.setInt(3, uid);
            final ResultSet rs = getResultSet(pst);
            try {
                ret = rs.next();
            } finally {
                CalendarCommonCollection.closeResultSet(rs);
                CalendarCommonCollection.closePreparedStatement(pst);
            }
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (readcon != null) {
                try {
                    DBPool.push(context, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("DBPoolingException:checkForDeletedParticipants (push)", dbpe);
                }
            }
        }
        return ret;
    }
    
    private final boolean checkIfUserIstheOnlyParticipant(final int cid, final int oid, final Connection readcon) throws SQLException {
        final PreparedStatement pst = readcon.prepareStatement("SELECT object_id from prg_dates_members WHERE object_id = ? AND cid = ?");
        pst.setInt(1, oid);
        pst.setInt(2, cid);
        final ResultSet rs = getResultSet(pst);
        int mc = 0;
        try {
            while (rs.next()) {
                mc++;
                if (mc > 1) {
                    break;
                }
            }
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(pst);
        }
        if (mc == 1) {
            return true;
        }
        return false;
    }
    
    private final void deleteOnlyOneParticipantInPrivateFolder(final int oid, final int cid, final int uid, final int fid, final Context c, final Connection writecon, final Session so) throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
        final PreparedStatement pd = writecon.prepareStatement("delete from prg_dates_members WHERE object_id = ? AND cid = ? AND member_uid LIKE ?");
        pd.setInt(1, oid);
        pd.setInt(2, cid);
        pd.setInt(3, uid);
        pd.addBatch();
        changeReminder(oid, uid, -1, c, false, null, null, CalendarOperation.DELETE);
        pd.executeBatch();
        boolean master_del_update = true;
        final PreparedStatement pdr = writecon.prepareStatement("delete from prg_date_rights WHERE object_id = ? AND cid = ? AND id = ? AND type = ?");
        pdr.setInt(1, oid);
        pdr.setInt(2, cid);
        pdr.setInt(3, uid);
        pdr.setInt(4, Participant.USER);
        pdr.addBatch();
        pdr.executeBatch();
        if (!checkForDeletedMasterObject(oid, cid, c)) {
            final PreparedStatement pidm = writecon.prepareStatement(
                    "insert into del_dates (creating_date, created_from, changing_date, changed_from, fid, intfield01, cid, pflag) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            pidm.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pidm.setInt(2, uid);
            pidm.setLong(3, System.currentTimeMillis());
            pidm.setInt(4, 0);
            pidm.setInt(5, fid);
            pidm.setInt(6, oid);
            pidm.setInt(7, cid);
            pidm.setInt(8, 0);
            pidm.addBatch();
            pidm.executeBatch();
            master_del_update = false;
            CalendarCommonCollection.closePreparedStatement(pidm);
        }
        CalendarCommonCollection.closePreparedStatement(pd);
        CalendarCommonCollection.closePreparedStatement(pdr);
        
        final PreparedStatement pid = writecon.prepareStatement("insert into del_dates_members (object_id, member_uid, pfid, cid, confirm) values (?, ?, ?, ?, ?)");
        pid.setInt(1, oid);
        pid.setInt(2, uid);
        pid.setInt(3, fid);
        pid.setInt(4, cid);
        pid.setInt(5, 0);
        pid.addBatch();
        pid.executeBatch();
        CalendarCommonCollection.closePreparedStatement(pid);
        
        final PreparedStatement ma = writecon.prepareStatement("update prg_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?");
        ma.setLong(1, System.currentTimeMillis());
        ma.setInt(2, uid);
        ma.setInt(3, oid);
        ma.setInt(4, cid);
        ma.addBatch();
        ma.executeBatch();
        CalendarCommonCollection.closePreparedStatement(ma);
        
        if (master_del_update) {
            final PreparedStatement ddu = writecon.prepareStatement("update del_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?");
            ddu.setLong(1, System.currentTimeMillis());
            ddu.setInt(2, uid);
            ddu.setInt(3, oid);
            ddu.setInt(4, cid);
            ddu.addBatch();
            ddu.executeBatch();
            CalendarCommonCollection.closePreparedStatement(ddu);
        }
        final AppointmentObject ao = new AppointmentObject();
        ao.setObjectID(oid);
        ao.setParentFolderID(fid);
        CalendarCommonCollection.triggerEvent(so, CalendarOperation.UPDATE, ao);
        changeReminder(oid, uid, fid, c, false, null, null, CalendarOperation.DELETE);
    }
    
    
    
    private final void changeReminder(final int oid, final int uid, final int fid, final Context c, final boolean sequence, final java.util.Date end_date, final java.util.Date reminder_date, final int action) throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
        final ReminderSQLInterface rsql = new ReminderHandler(c);
        if (action != CalendarOperation.DELETE) {
            if (!CalendarCommonCollection.isInThePast(end_date)) {
                final ReminderObject ro = new ReminderObject();
                ro.setUser(uid);
                ro.setTargetId(oid);
                ro.setModule(Types.APPOINTMENT);
                ro.setRecurrenceAppointment(sequence);
                ro.setDate(reminder_date);
                ro.setFolder(""+fid);
                if (!rsql.existsReminder(oid, uid, Types.APPOINTMENT)) {
                    rsql.insertReminder(ro);
                } else {
                    rsql.updateReminder(ro);
                }
            }
        } else {
            if (rsql.existsReminder(oid, uid, Types.APPOINTMENT)) {
                rsql.deleteReminder(oid, uid, Types.APPOINTMENT);
            }
        }
    }
    
    
    public final void deleteAppointment(final int uid, final CalendarDataObject cdao, final Connection writecon, final Session so, Context ctx, final int inFolder, final java.util.Date clientLastModified) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException, OXConcurrentModificationException {
        Connection readcon = null;
        CalendarDataObject edao = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            readcon = DBPool.pickup(ctx);
            final CalendarOperation co = new CalendarOperation();
            prep = getPreparedStatement(readcon, loadAppointment(cdao.getObjectID(), cdao.getContext()));
            rs = getResultSet(prep);
            edao = co.loadAppointment(rs, cdao.getObjectID(), inFolder, this, readcon, so, ctx, CalendarOperation.DELETE, inFolder);
        } catch(final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch(final OXPermissionException oxpe ) {
            throw oxpe;
        } catch(final OXException oxe) {
            throw oxe;
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(prep);
            if (readcon != null) {
                try {
                    DBPool.push(ctx, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("DBPoolingException:deleteAppointment (push)", dbpe);
                }
            }
        }
        if (clientLastModified == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        } else if (edao != null && edao.getLastModified() == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        }
        
        if (edao != null && edao.getLastModified().getTime() > clientLastModified.getTime()) {
            throw new OXConcurrentModificationException(Component.APPOINTMENT, OXConcurrentModificationException.ConcurrentModificationCode.CONCURRENT_MODIFICATION);
        }
        
        if (edao != null) {
            deleteSingleAppointment(cdao.getContextID(), cdao.getObjectID(), uid, edao.getCreatedBy(), inFolder, null, writecon, edao.getFolderType(), so, ctx, CalendarRecurringCollection.getRecurringAppointmentDeleteAction(cdao, edao), cdao, edao, clientLastModified);
        }
        
    }
    
    public void deleteAppointmentsInFolder(final Session so, Context ctx, final ResultSet rs, final Connection readcon, final Connection writecon, final int foldertype, final int fid) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException {
        while (rs.next()) {
            final int oid = rs.getInt(1);
            final int owner = rs.getInt(2);
            deleteSingleAppointment(so.getContextId(), oid, so.getUserId(), owner, fid, readcon, writecon, foldertype, so, ctx, CalendarRecurringCollection.RECURRING_NO_ACTION, null, null, null);
        }
    }
    
    private final void deleteSingleAppointment(final int cid, int oid, final int uid, final int owner, final int fid, Connection readcon, final Connection writecon, final int foldertype, final Session so, Context ctx, final int recurring_action, final CalendarDataObject cdao, final CalendarDataObject edao, final java.util.Date clientLastModified) throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
        
        if (foldertype == FolderObject.PRIVATE && uid != owner) {
            boolean close_read = false;
            try {
                if (readcon == null) {
                    readcon = DBPool.pickup(ctx);
                    close_read = true;
                }
                if (!checkIfUserIstheOnlyParticipant(cid, oid, readcon) && recurring_action != CalendarRecurringCollection.RECURRING_VIRTUAL_ACTION) {
                    if (close_read && readcon != null) {
                        DBPool.push(ctx, readcon);
                        close_read = false;
                    }
                    deleteOnlyOneParticipantInPrivateFolder(oid, cid, uid, fid, new ContextImpl(cid), writecon, so);
                    return;
                }
                if (recurring_action == CalendarRecurringCollection.RECURRING_VIRTUAL_ACTION) {
                    // Ceate an exception first, remove the user as participant and then return
                    if (!checkIfUserIstheOnlyParticipant(cid, oid, readcon)) {
                        
                        edao.setRecurrencePosition(cdao.getRecurrencePosition());
                        edao.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
                        CalendarRecurringCollection.setRecurrencePositionOrDateInDAO(edao);
                        
                        CalendarDataObject temp = (CalendarDataObject) edao.clone();
                        final RecurringResults rss = CalendarRecurringCollection.calculateRecurring(temp, 0, 0, edao.getRecurrencePosition());
                        if (rss != null) {
                            RecurringResult rs = rss.getRecurringResult(0);
                            if (rss != null) {
                                edao.setStartDate(new Date(rs.getStart()));
                                edao.setEndDate(new Date(rs.getEnd()));
                                
                            }
                        }                        
                        
                        final java.util.Date deleted_exceptions[] = edao.getDeleteException();
                        final java.util.Date changed_exceptions[] = edao.getChangeException();
                        final java.util.Date calculated_exception = edao.getRecurrenceDatePosition();
                        edao.removeDeleteExceptions();
                        edao.removeChangeExceptions();
                        CalendarCommonCollection.removeParticipant(edao, uid);
                        CalendarCommonCollection.removeUserParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        edao.setRecurrenceID(edao.getObjectID());
                        edao.removeObjectID();
                        try {
                            insertAppointment(edao, writecon, so);
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch(final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(8));
                        }
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setDeleteExceptions(CalendarCommonCollection.removeException(deleted_exceptions, calculated_exception));
                        update.setChangeExceptions(new java.util.Date[] { calculated_exception });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false); // MAIN OBJECT
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch(final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(9));
                        }
                    } else {
                        createSingleVirtualDeleteException(cdao, edao, writecon, oid, uid, fid, so, ctx, clientLastModified);
                    }
                } else if (recurring_action == CalendarRecurringCollection.RECURRING_EXCEPTION_ACTION) {
                    if (!checkIfUserIstheOnlyParticipant(cid, oid, readcon)) {
                        if (close_read && readcon != null) {
                            DBPool.push(ctx, readcon);
                            close_read = false;
                        }
                        // remove participant (update)
                        CalendarCommonCollection.removeParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setChangeExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            updateAppointment(edao, ldao, writecon, so, ctx, fid, clientLastModified, false); // EXCEPTION OBJECT
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false); // MAIN OBJECT
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch(final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(10));
                        }
                    } else {
                        // remove this real existing exception and update main object
                        removeSingleExistingException(edao, writecon, so);
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setChangeExceptions(CalendarCommonCollection.removeException(edao.getDeleteException(), edao.getRecurrenceDatePosition()));
                        update.setDeleteExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false); // MAIN OBJECT
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch(final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(11));
                        }
                    }
                }
                return;
            } catch(final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(final OXException oxe) {
                throw oxe;
            } catch(final DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } finally {
                if (close_read && readcon != null) {
                    try {
                        DBPool.push(ctx, readcon);
                    } catch (final DBPoolingException dbpe) {
                        LOG.error("DBPoolingException:deleteSingleAppointment (push) ", dbpe);
                    }
                }
            }
        }
        
        if (recurring_action == CalendarRecurringCollection.RECURRING_VIRTUAL_ACTION) {
            // this is an update with a new delete_exception
            if (edao != null) {
                createSingleVirtualDeleteException(cdao, edao, writecon, oid, uid, fid, so, ctx, clientLastModified);
            } else {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_UNEXPECTED_DELETE_STATE, Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(-1));
            }
            return;
        }  else if (recurring_action == CalendarRecurringCollection.RECURRING_FULL_DELETE) {
            final ArrayList<Integer> al = getExceptionList(readcon, ctx, edao.getRecurrenceID());
            if (al != null && al.size() > 0) {
                final Object oids[] = al.toArray();
                deleteAllRecurringExceptions(StringCollection.getSqlInString(oids), so, writecon);
                for (int a = 0; a < al.size(); a++) {
                    triggerDeleteEvent(al.get(a).intValue(), fid, so, ctx, null, readcon);
                }
            }
            oid = edao.getRecurrenceID();
        }
        
        PreparedStatement d_dates = null;
        PreparedStatement d_members = null;
        PreparedStatement copy_members = null;
        PreparedStatement copy_rights = null;
        PreparedStatement copy_dates = null;
        PreparedStatement del_dates = null;
        PreparedStatement del_members = null;
        PreparedStatement del_rights = null;
        PreparedStatement update = null;
        
        if (edao != null && edao.getRecurrenceID() > 0 && !cdao.containsRecurrenceID()) {
            cdao.setRecurrenceID(edao.getRecurrenceID());
        }
        
        long modified = 0;
        
        try {
            StringBuilder delete_statement = new StringBuilder(128);
            delete_statement.append("delete from del_dates WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(DATES_IDENTIFIER_IS);
            delete_statement.append(oid);
            d_dates = writecon.prepareStatement(delete_statement.toString());
            d_dates.addBatch();
            d_dates.executeBatch();
            delete_statement = new StringBuilder(128);
            delete_statement.append("delete from del_dates_members WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(PARTICIPANTS_IDENTIFIER_IS);
            delete_statement.append(oid);
            d_members = writecon.prepareStatement(delete_statement.toString());
            d_members.addBatch();
            d_members.executeBatch();
            
            delete_statement = new StringBuilder(128);
            delete_statement.append("INSERT INTO del_dates_members SELECT * FROM prg_dates_members WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(PARTICIPANTS_IDENTIFIER_IS);
            delete_statement.append(oid);
            copy_members= writecon.prepareStatement(delete_statement.toString());
            copy_members.addBatch();
            
            delete_statement = new StringBuilder(128);
            delete_statement.append("INSERT INTO del_date_rights SELECT * FROM prg_date_rights WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(PARTICIPANTS_IDENTIFIER_IS);
            delete_statement.append(oid);
            copy_rights = writecon.prepareStatement(delete_statement.toString());
            copy_rights.addBatch();
            
            
            delete_statement = new StringBuilder(128);
            delete_statement.append("INSERT INTO del_dates SELECT * FROM prg_dates WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(DATES_IDENTIFIER_IS);
            delete_statement.append(oid);
            copy_dates= writecon.prepareStatement(delete_statement.toString());
            copy_dates.addBatch();
            
            delete_statement = new StringBuilder(128);
            delete_statement.append("delete from prg_dates WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(DATES_IDENTIFIER_IS);
            delete_statement.append(oid);
            del_dates = writecon.prepareStatement(delete_statement.toString());
            del_dates.addBatch();
            
            
            delete_statement = new StringBuilder(128);
            delete_statement.append("delete from prg_dates_members WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(PARTICIPANTS_IDENTIFIER_IS);
            delete_statement.append(oid);
            del_members = writecon.prepareStatement(delete_statement.toString());
            del_members.addBatch();
            delete_statement = new StringBuilder(128);
            delete_statement.append("delete from prg_date_rights WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(PARTICIPANTS_IDENTIFIER_IS);
            delete_statement.append(oid);
            del_rights = writecon.prepareStatement(delete_statement.toString());
            del_rights.addBatch();
            
            modified = System.currentTimeMillis();
            
            delete_statement = new StringBuilder(128);
            delete_statement.append("UPDATE del_dates SET changing_date = ?, changed_from = ? WHERE cid = ");
            delete_statement.append(cid);
            delete_statement.append(DATES_IDENTIFIER_IS);
            delete_statement.append(oid);
            update = writecon.prepareStatement(delete_statement.toString());
            update.setLong(1, modified);
            update.setInt(2, uid);
            update.addBatch();
            
            copy_members.executeBatch();
            copy_rights.executeBatch();
            copy_dates.executeBatch();
            
            del_dates.executeBatch();
            del_members.executeBatch();
            del_rights.executeBatch();
            update.executeBatch();
            
        } finally {
            CalendarCommonCollection.closePreparedStatement(d_dates);
            CalendarCommonCollection.closePreparedStatement(d_members);
            CalendarCommonCollection.closePreparedStatement(copy_members);
            CalendarCommonCollection.closePreparedStatement(copy_rights);
            CalendarCommonCollection.closePreparedStatement(copy_dates);
            CalendarCommonCollection.closePreparedStatement(del_dates);
            CalendarCommonCollection.closePreparedStatement(del_members);
            CalendarCommonCollection.closePreparedStatement(del_rights);
            CalendarCommonCollection.closePreparedStatement(update);
        }
        
        if (edao != null) {
            edao.setModifiedBy(uid);
            edao.setChangingDate(new Timestamp(modified));
            triggerDeleteEvent(oid, fid, so, ctx, edao, readcon);
        } else {
            triggerDeleteEvent(oid, fid, so, ctx, null, readcon);
        }
    }
    
    private final void triggerDeleteEvent(final int oid, final int fid, final Session so, Context ctx, final CalendarDataObject edao, final Connection readcon) throws OXException, SQLException {
        CalendarDataObject ao = null;
        if (edao != null) {
            ao = (CalendarDataObject)edao.clone();
        } else {
            ao = new CalendarDataObject();
        }
        ao.setObjectID(oid);
        ao.setParentFolderID(fid);
        CalendarCommonCollection.triggerEvent(so, CalendarOperation.DELETE, ao);
        //deleteAllReminderEntries(edao, oid, fid, so, readcon);
        final ReminderSQLInterface rsql = new ReminderHandler(ctx);
        try {
        	rsql.deleteReminder(oid, Types.APPOINTMENT);
        } catch(AbstractOXException oxe) {
        	// this is wanted if Code = Code.NOT_FOUND
        	if (oxe.getDetailNumber() != Code.NOT_FOUND.getDetailNumber()) {
        		throw new OXException(oxe);
        	}
        }
    }
    
    
    private final void createSingleVirtualDeleteException(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final int oid, final int uid, final int fid, final Session so, Context ctx, final java.util.Date clientLastModified) throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
        final CalendarDataObject udao = new CalendarDataObject();
        udao.setObjectID(oid);
        udao.setContext(ctx);
        java.util.Date de = null;
        if (!cdao.containsRecurrenceDatePosition()) {
            final long del = CalendarRecurringCollection.getLongByPosition(edao, cdao.getRecurrencePosition());
            if (del != 0) {
                de = new java.util.Date(del);
            }
        } else {
            de = cdao.getRecurrenceDatePosition();
        }
        if (de != null) {
            udao.setDeleteExceptions(new java.util.Date[] { de });
            try {
                final CalendarDataObject ldao = loadObjectForUpdate(udao, so, ctx, fid);
                updateAppointment(udao, ldao, writecon, so, ctx, fid, clientLastModified);
            } catch (final OXException oxe) {
                throw oxe;
            } catch (final LdapException lde) {
                throw new OXException(lde);
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_UNEXPECTED_DELETE_STATE, Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(cdao.getRecurrencePosition()));
        }
    }
    
    private final void removeSingleExistingException(final CalendarDataObject edao, final Connection writecon, final Session so)  throws SQLException, OXException {
        PreparedStatement del_dates = null;
        PreparedStatement del_members = null;
        PreparedStatement del_rights = null;
        try {
            final StringBuilder del_dates_ps = new StringBuilder(128);
            del_dates_ps.append("delete from prg_dates WHERE cid = ");
            del_dates_ps.append(so.getContextId());
            del_dates_ps.append(DATES_IDENTIFIER_IS);
            del_dates_ps.append(edao.getObjectID());
            del_dates = writecon.prepareStatement(del_dates_ps.toString());
            del_dates.addBatch();
            final StringBuilder del_members_ps = new StringBuilder(128);
            del_members_ps.append("delete from prg_dates_members WHERE cid = ");
            del_members_ps.append(so.getContextId());
            del_members_ps.append(PARTICIPANTS_IDENTIFIER_IS);
            del_members_ps.append(edao.getObjectID());
            del_members = writecon.prepareStatement(del_members_ps.toString());
            del_members.addBatch();
            final StringBuilder del_rights_ps = new StringBuilder(128);
            del_rights_ps.append("delete from prg_date_rights WHERE cid = ");
            del_rights_ps.append(so.getContextId());
            del_rights_ps.append(PARTICIPANTS_IDENTIFIER_IS);
            del_rights_ps.append(edao.getObjectID());
            del_rights = writecon.prepareStatement(del_rights_ps.toString());
            del_rights.addBatch();
            del_dates.executeBatch();
            del_members.executeBatch();
            del_rights.executeBatch();
        } finally {
            CalendarCommonCollection.closePreparedStatement(del_dates);
            CalendarCommonCollection.closePreparedStatement(del_members);
            CalendarCommonCollection.closePreparedStatement(del_rights);
        }
    }
    
    
    final ArrayList<Integer> getExceptionList(Connection readcon, final Context c, final int rec_id) throws OXException {
        boolean close_read = false;
        ArrayList<Integer> al = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if (readcon == null) {
                readcon = DBPool.pickup(c);
                close_read = true;
            }
            al = new ArrayList<Integer>(8);
            final StringBuilder query = new StringBuilder(128);
            query.append("select intfield01 FROM prg_dates pd WHERE intfield02 = ");
            query.append(rec_id);
            query.append(" AND cid = ");
            query.append(c.getContextId());
            query.append(" AND intfield01 != intfield02 AND intfield05 > 0");
            prep = getPreparedStatement(readcon, query.toString());
            rs = getResultSet(prep);
            while (rs.next()) {
                al.add(Integer.valueOf(rs.getInt(1)));
            }
        } catch(final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(prep);
            if (close_read && readcon != null) {
                try {
                    DBPool.push(c, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("DBPoolingException:deleteExceptions (push) ", dbpe);
                }
            }
        }
        return al;
    }
    
    private final void deleteAllRecurringExceptions(final String inoids, final Session so, final Connection writecon)  throws SQLException, OXException {
        PreparedStatement del_rights = null;
        PreparedStatement del_members = null;
        PreparedStatement del_dates = null;
        try {
            final StringBuilder del_rights_ps = new StringBuilder(128);
            del_rights_ps.append("delete from prg_date_rights WHERE cid = ");
            del_rights_ps.append(so.getContextId());
            del_rights_ps.append(" AND object_id IN ");
            del_rights_ps.append(inoids);
            del_rights = writecon.prepareStatement(del_rights_ps.toString());
            del_rights.addBatch();
            final StringBuilder del_members_ps = new StringBuilder(128);
            del_members_ps.append("delete from prg_dates_members WHERE cid = ");
            del_members_ps.append(so.getContextId());
            del_members_ps.append(" AND object_id IN ");
            del_members_ps.append(inoids);
            del_members = writecon.prepareStatement(del_members_ps.toString());
            del_members.executeBatch();
            final StringBuilder del_dates_ps = new StringBuilder(128);
            del_dates_ps.append("delete FROM prg_dates  WHERE cid = ");
            del_dates_ps.append(so.getContextId());
            del_dates_ps.append(" AND intfield01 IN ");
            del_dates_ps.append(inoids);
            del_dates = writecon.prepareStatement(del_dates_ps.toString());
            del_dates.addBatch();
            del_rights.executeBatch();
            del_members.executeBatch();
            del_dates.executeBatch();
        } finally {
            CalendarCommonCollection.closePreparedStatement(del_rights);
            CalendarCommonCollection.closePreparedStatement(del_members);
            CalendarCommonCollection.closePreparedStatement(del_dates);
        }
    }
    
    private final void deleteAllReminderEntries(CalendarDataObject edao, int oid, int inFolder, Session so, Context ctx, Connection readcon) throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
        UserParticipant up[] = null;
        boolean close_read = false;
        if (edao == null) {
            PreparedStatement prep = null;
            ResultSet rs = null;
            try {
                if (readcon == null) {
                    readcon = DBPool.pickup(ctx);
                    close_read = true;
                }
                final CalendarOperation co = new CalendarOperation();
                prep = getPreparedStatement(readcon, loadAppointment(oid, ctx));
                rs = getResultSet(prep);
                edao = co.loadAppointment(rs, oid, inFolder, this, readcon, so, ctx, CalendarOperation.DELETE, inFolder, false); // No permission checks at all!
            } catch(final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(final OXPermissionException oxpe ) {
                throw oxpe;
            } catch(final OXException oxe) {
                throw oxe;
            } catch(final DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } finally {
                CalendarCommonCollection.closeResultSet(rs);
                CalendarCommonCollection.closePreparedStatement(prep);
                if (close_read && readcon != null) {
                    try {
                        DBPool.push(ctx, readcon);
                    } catch (final DBPoolingException dbpe) {
                        LOG.error("DBPoolingException:deleteAppointment (push)", dbpe);
                    }
                }
            }
        }
        up = edao.getUsers();
        for (int a = 0; a < up.length; a++) {
            int uid = up[a].getIdentifier();
            int fid = 0;
            if (up[a].getPersonalFolderId() > 0) {
                fid = up[a].getPersonalFolderId();
            } else {
                fid = edao.getEffectiveFolderId();
            }
            if (uid > 0 && fid > 0) {
                changeReminder(oid, uid, fid, ctx, edao.isSequence(), null, null, CalendarOperation.DELETE);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(StringCollection.convertArraytoString(new Object[] { "Reminder object will neither be checked nor deleted -> oid:uid:fid ", oid,":",uid,":",fid }));
                }
            }
        }
    }
    
    
}
