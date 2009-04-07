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

import static com.openexchange.groupware.EnumComponent.APPOINTMENT;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.Types;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.groupware.calendar.CalendarCallbacks;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.MBoolean;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
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
import com.openexchange.groupware.reminder.ReminderException;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderException.Code;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link CalendarMySQL} - The MySQL implementation of {@link CalendarSqlImp}.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class CalendarMySQL implements CalendarSqlImp {

    private static final String SELECT_ALL_PRIVATE_FOLDERS_IN_WHICH_A_USER_IS_A_PARTICIPANT = "SELECT object_id, pfid, member_uid FROM prg_dates_members WHERE member_uid = ? and cid = ?";

    private static final String PDM_AND_PD_FID = " AND pd.fid = ";

    private static final String select = "SELECT intfield01, timestampfield01, timestampfield02, field01 FROM prg_dates ";

    private static final String FREE_BUSY_SELECT = "SELECT intfield01, timestampfield01, timestampfield02, intfield07, intfield06, field01, fid, pflag, created_from, intfield02, intfield04, field06, field07, field08, timezone FROM prg_dates ";

    private static final String RANGE_SELECT = "SELECT intfield01, timestampfield01, timestampfield02, intfield02, intfield04, field06, field07, field08, timezone, intfield07 FROM prg_dates ";

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

    private static CalendarCollection collection = new CalendarCollection();

    private static interface StatementFiller {
        void fillStatement(PreparedStatement stmt, int pos, CalendarDataObject cdao) throws OXCalendarException,
                SQLException;
    }

    private static final Map<Integer, StatementFiller> STATEMENT_FILLERS;

    static {
        STATEMENT_FILLERS = new HashMap<Integer, StatementFiller>(22);
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.TITLE), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setString(pos, cdao.getTitle());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.START_DATE), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setTimestamp(pos, new java.sql.Timestamp(cdao.getStartDate().getTime()));
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.END_DATE), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setTimestamp(pos, new java.sql.Timestamp(cdao.getEndDate().getTime()));
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.SHOWN_AS), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, cdao.getShownAs());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.LOCATION), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                if (cdao.getLocation() == null) {
                    stmt.setNull(pos, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos, cdao.getLocation());
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.NOTE), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                if (cdao.getNote() == null) {
                    stmt.setNull(pos, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos, cdao.getNote());
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.CATEGORIES), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                if (cdao.getCategories() == null) {
                    stmt.setNull(pos, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos, cdao.getCategories());
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.FULL_TIME), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, cdao.getFulltime());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.COLOR_LABEL), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, cdao.getLabel());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.MODIFIED_BY), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                if (cdao.containsModifiedBy()) {
                    stmt.setInt(pos, cdao.getModifiedBy());
                } else {
                    throw new OXCalendarException(OXCalendarException.Code.MODIFIED_BY_MISSING);
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.LAST_MODIFIED), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                if (cdao.containsLastModified()) {
                    stmt.setLong(pos, cdao.getLastModified().getTime());
                } else {
                    final Timestamp t = new Timestamp(System.currentTimeMillis());
                    stmt.setLong(pos, t.getTime());
                    cdao.setLastModified(t);
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.PRIVATE_FLAG), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, cdao.getPrivateflag());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.FOLDER_ID), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                if (cdao.getFolderType() == FolderObject.PRIVATE || cdao.getFolderType() == FolderObject.SHARED) {
                    stmt.setInt(pos, 0);
                } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                    stmt.setInt(pos, cdao.getGlobalFolderID());
                } else {
                    throw new OXCalendarException(OXCalendarException.Code.NOT_YET_SUPPORTED);
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.RECURRENCE_TYPE), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setString(pos, cdao.getRecurrence());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.RECURRENCE_ID), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setInt(pos, cdao.getRecurrenceID());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.DELETE_EXCEPTIONS), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setString(pos, cdao.getDelExceptions());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.CHANGE_EXCEPTIONS), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setString(pos, cdao.getExceptions());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.RECURRENCE_CALCULATOR), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setInt(pos, cdao.getRecurrenceCalculator());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.RECURRENCE_POSITION), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setInt(pos, cdao.getRecurrencePosition());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.NUMBER_OF_ATTACHMENTS), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setInt(pos, cdao.getNumberOfAttachments());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(AppointmentObject.TIMEZONE), new StatementFiller() {
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXCalendarException, SQLException {
                stmt.setString(pos, cdao.getTimezoneFallbackUTC());
            }
        });
    }
    
    public CalendarMySQL() {
    }

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
        // sb.append(" AND pdm.confirm != ");
        // sb.append(com.openexchange.groupware.container.CalendarObject.DECLINE);

        collection.getVisibleFolderSQLInString(sb, uid, groups, c, uc, readcon);

        if (collection.getFieldName(orderBy) == null || orderDir == null) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(collection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
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

    public final PreparedStatement getAllPrivateAppointmentAndFolderIdsForUser(final Context c, final int id, final Connection readcon) throws SQLException {
        final PreparedStatement stmt = readcon.prepareStatement(SELECT_ALL_PRIVATE_FOLDERS_IN_WHICH_A_USER_IS_A_PARTICIPANT);
        stmt.setInt(1, id);
        stmt.setInt(2, c.getContextId());
        return stmt;
    }

    public PreparedStatement getSharedAppointmentFolderQuery(final Context c, final CalendarFolderObject cfo, final Connection readcon) throws SQLException {
        final StringBuilder sb = new StringBuilder("SELECT object_id, pfid, member_uid FROM prg_dates_members WHERE cid = ? AND pfid IN (");
        for(final Object o : cfo.getSharedFolderList()) {
            sb.append(o).append(',');
        }
        sb.setCharAt(sb.length()-1, ')');
        final PreparedStatement stmt = readcon.prepareStatement(sb.toString());
        stmt.setInt(1, c.getContextId());
        return stmt;

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
        // sb.append(ORDER_BY_TS1);
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
        final int size = (int) ((end - start) / Constants.MILLI_DAY);
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

        collection.getVisibleFolderSQLInString(sb, uid, groups, c, uc, readcon);

        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = null;
        try {
            pst.setTimestamp(1, new Timestamp(d2.getTime()));
            pst.setTimestamp(2, new Timestamp(d1.getTime()));
    
            rs = getResultSet(pst);

            CalendarDataObject cdao = null;
            while (rs.next()) {
                cdao = new CalendarDataObject();
                final int oid = rs.getInt(1);
                final java.util.Date s = rs.getTimestamp(2);
                final java.util.Date e = rs.getTimestamp(3);
                final int rec = rs.getInt(4);
                if (!rs.wasNull() && oid == rec) {
                    cdao.setStartDate(s);
                    cdao.setEndDate(e);
                    cdao.setRecurrenceCalculator(rs.getInt(5));
                    cdao.setRecurrence(rs.getString(6));
                    collection.fillDAO(cdao);
                    cdao.setDelExceptions(rs.getString(7));
                    cdao.setExceptions(rs.getString(8));
                    cdao.setTimezone(rs.getString(9));
                    cdao.setFullTime(rs.getInt(10) > 0);
                    try {
                        if (collection.fillDAO(cdao)) {
                            final RecurringResultsInterface rrs = collection.calculateRecurring(cdao, start, end, 0);
                            final TimeZone zone = Tools.getTimeZone(cdao.getTimezoneFallbackUTC());
                            for (int a = 0; a < rrs.size(); a++) {
                                final RecurringResultInterface rr = rrs.getRecurringResult(a);
                                fillActiveDates(start, rr.getStart(), rr.getEnd(), activeDates, collection.exceedsHourOfDay(rr.getStart(), zone));
                            }
                        } else {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(StringCollection.convertArraytoString(new Object[] { "SKIP calculation for recurring appointment oid:uid:context ", Integer.valueOf(oid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(uid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(c.getContextId()) }));
                            }
                        }
                    } catch (final OXException x) {
                        LOG.error("Can not calculate invalid recurrence pattern for appointment "+oid+":"+c.getContextId(),x);
                    }
                } else {
                    fillActiveDates(start, s.getTime(), e.getTime(), activeDates, collection.exceedsHourOfDay(s.getTime(), Tools.getTimeZone(rs.getString(9))));
                }
            }
            // collection.debugActiveDates (start, end,
            // activeDates); // TODO: Make configurable or uncomment in runtime
            // edition
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(pst);
        }
        return activeDates;
    }

    private final void fillActiveDates(final long start, long s, final long e, final boolean activeDates[], final boolean exceedsHourOfDay) {
        if (start > s) {
            s = start;
        }

        int start_pos = 0;
        final int ll = (int) (e - s);
        int len = (int) (ll / Constants.MILLI_DAY);
        if (ll != 0 && ll % Constants.MILLI_DAY == 0) {
            len--;
        }

        if (s >= start) {
            final long startDiff = (s - start);
            start_pos = (int) (startDiff / Constants.MILLI_DAY);
            if (exceedsHourOfDay) {
                start_pos++;
            }
            if (start_pos > activeDates.length) {
                return;
            }
        }
        final int length = start_pos + len;
        for (int a = start_pos; a <= length; a++) {
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
        if (collection.getFieldName(orderBy) == null || orderDir == null) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(collection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
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
        if (collection.getFieldName(orderBy) == null || orderDir == null) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(collection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
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
        if (collection.getFieldName(orderBy) == null || orderDir == null) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(collection.getFieldName(orderBy));
            sb.append(' ');
            sb.append(orderDir);
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

    public final PreparedStatement getSharedFolderModifiedSinceSQL(final Context c, final int uid, final int shared_folder_owner, final int groups[], final int fid, final java.util.Date since, final String select, final boolean readall, final Connection readcon, final java.util.Date d1, final java.util.Date d2, final boolean includePrivateFlag) throws SQLException {
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
        if (includePrivateFlag) {
            sb.append(" AND pd.pflag = 0 ");
        }
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
        ResultSet rs = null;
        boolean ret = true;
        try {
            rs = getResultSet(prep);

            if (!rs.next()) {
                ret = false;
            }
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(prep);
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
        ResultSet rs = null;
        boolean ret = true;
        try {
            rs = getResultSet(prep);
            if (rs.next()) {
                ret = false;
            }
        } finally {
            collection.closePreparedStatement(prep);
            collection.closeResultSet(rs);
        }
        return ret;
    }

    private static final void getRange(final StringBuilder sb) {
        sb.append(" pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
    }

    private static final void getConflictRange(final StringBuilder sb) {
        sb.append(" intfield07 = 0 AND pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
    }

    private static final void getConflictRangeFullTime(final StringBuilder sb) {
        sb.append(" intfield07 = 1 AND pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
    }

    private static final void getSince(final StringBuilder sb) {
        sb.append(" pd.changing_date >= ?");
    }

    private static final String parseSelect(final String select) {
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
                final Set<Integer> private_read_all = cfo.getPrivateReadableAll();
                final Set<Integer> private_read_own = cfo.getPrivateReadableOwn();
                final Set<Integer> public_read_all = cfo.getPublicReadableAll();
                final Set<Integer> public_read_own = cfo.getPublicReadableOwn();

                boolean private_query = false;
                boolean public_query = false;
                if (!private_read_all.isEmpty()) {
                    sb.append(" AND (pdm.pfid IN ");
                    sb.append(StringCollection.getSqlInString(private_read_all));
                    private_query = true;
                }

                if (!private_read_own.isEmpty()) {
                    if (private_query) {
                        sb.append(" OR pd.created_from = ");
                    } else {
                        sb.append(PD_CREATED_FROM_IS);
                    }
                    sb.append(uid);
                    sb.append(" AND (pdm.pfid IN ");
                    sb.append(StringCollection.getSqlInString(private_read_own));
                    private_query = true;
                }

                if (!public_read_all.isEmpty()) {
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

                if (!public_read_own.isEmpty()) {
                    if (private_query || public_query) {
                        sb.append(" OR pd.fid IN ");
                        sb.append(StringCollection.getSqlInString(public_read_own));
                        sb.append(PD_CREATED_FROM_IS);
                        sb.append(uid);
                    } else {
                        sb.append(" AND pd.fid IN ");
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
            sb.append(collection.getFieldName(AppointmentObject.TITLE));
            sb.append(" LIKE ?");
            pattern = StringCollection.prepareForSearch(pattern);
        }

        sb.append(PDM_ORDER_BY);
        String orderby = collection.getFieldName(orderBy);
        if (orderby == null) {
            orderby = collection.getFieldName(AppointmentObject.START_DATE);
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

        // TODO: This should be rewritten to be more flexible and to cover all
        // expectations
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
        sb.append("SELECT creating_date, created_from, changing_date, changed_from, fid, pflag, timestampfield01, timestampfield02, timezone, ").append(" intfield02, intfield03, field01, field02, intfield06, intfield08, field04, intfield07, field09, intfield04, intfield05, field06, field07, field08 FROM prg_dates  WHERE cid = ");
        sb.append(c.getContextId());
        sb.append(DATES_IDENTIFIER_IS);
        sb.append(oid);
        return sb.toString();

    }

    public final CalendarDataObject[] insertAppointment(final CalendarDataObject cdao, final Connection writecon, final Session so) throws DataTruncation, SQLException, LdapException, OXException {
    	return insertAppointment0(cdao, writecon, so, true);
    }

    private final CalendarDataObject[] insertAppointment0(final CalendarDataObject cdao, final Connection writecon, final Session so, final boolean notify) throws DataTruncation, SQLException, LdapException, OXException {
        int i = 1;
        PreparedStatement pst = null;
        try {
            pst = writecon.prepareStatement("insert into prg_dates (creating_date, created_from, changing_date, changed_from," + "fid, pflag, cid, timestampfield01, timestampfield02, timezone, intfield01, intfield03, intfield06, intfield07, intfield08, " + "field01, field02, field04, field09, intfield02, intfield04, intfield05, field06, field07, field08) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            cdao.setObjectID(IDGenerator.getId(cdao.getContext(), Types.APPOINTMENT, writecon));

            pst.setTimestamp(i++, new Timestamp(cdao.getCreationDate().getTime()));
            if (!cdao.containsLastModified()) {
                cdao.setLastModified(cdao.getCreationDate());
            }
            pst.setInt(i++, cdao.getCreatedBy());
            pst.setLong(i++, cdao.getLastModified().getTime());
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
            pst.setTimestamp(i++, new Timestamp(cdao.getStartDate().getTime()));
            pst.setTimestamp(i++, new Timestamp(cdao.getEndDate().getTime()));
            pst.setString(i++, cdao.getTimezoneFallbackUTC());
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

            if (cdao.isSequence(true)) {
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
                if (cdao.getDelExceptions() == null) {
                    pst.setNull(i++, java.sql.Types.VARCHAR);
                } else {
                    pst.setString(i++, cdao.getDelExceptions());
                }
                if (cdao.getExceptions() == null) {
                    pst.setNull(i++, java.sql.Types.VARCHAR);
                } else {
                    pst.setString(i++, cdao.getExceptions());
                }
            } else {
                pst.setNull(i++, java.sql.Types.INTEGER);
                pst.setNull(i++, java.sql.Types.INTEGER);
                pst.setNull(i++, java.sql.Types.INTEGER);
                pst.setNull(i++, java.sql.Types.VARCHAR);
                pst.setNull(i++, java.sql.Types.VARCHAR);
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }

            insertParticipants(cdao, writecon);
            insertUserParticipants(cdao, writecon, so.getUserId());
            pst.executeUpdate();
        } finally {
            collection.closePreparedStatement(pst);
        }
        writecon.commit();
        cdao.setParentFolderID(cdao.getActionFolder());
        if (notify) {
			collection.triggerEvent(so, CalendarOperation.INSERT, cdao);
		}
		return null;
    }

    private static final String SQL_INSERT_PARTICIPANT = "INSERT INTO prg_date_rights (object_id, cid, id, type, dn, ma) VALUES (?, ?, ?, ?, ?, ?)";

    private final void insertParticipants(final CalendarDataObject cdao, final Connection writecon)
            throws SQLException, OXCalendarException {
        final Participant participants[] = cdao.getParticipants();
        Arrays.sort(participants);
        if (participants != null) {
            PreparedStatement pi = null;
            try {
                pi = writecon.prepareStatement(SQL_INSERT_PARTICIPANT);
                final Set<Integer> knownExternalIds = createExternalIdentifierSet(participants);
                int lastid = -1;
                int lasttype = -1;
                for (int a = 0; a < participants.length; a++) {
                    if (participants[a].getIdentifier() == 0 && participants[a].getType() == Participant.EXTERNAL_USER
                            && participants[a].getEmailAddress() != null) {
                        final ExternalUserParticipant external = new ExternalUserParticipant(participants[a]
                                .getEmailAddress());
                        /*
                         * Determine an unique identifier
                         */
                        Integer identifier = Integer.valueOf(external.getEmailAddress().hashCode());
                        while (knownExternalIds.contains(identifier)) {
                            identifier = Integer.valueOf(identifier.intValue() + 1);

                        }
                        /*
                         * Add to known identifiers
                         */
                        knownExternalIds.add(identifier);
                        external.setIdentifier(identifier.intValue());
                        external.setDisplayName(participants[a].getDisplayName());
                        participants[a] = external;
                    }
                    final Participant participant = participants[a];
                    /*
                     * Don't insert a participant twice...
                     */
                    if (lastid != participant.getIdentifier() || lasttype != participant.getType()) {
                        lastid = participant.getIdentifier();
                        lasttype = participant.getType();
                        pi.setInt(1, cdao.getObjectID());
                        pi.setInt(2, cdao.getContextID());
                        pi.setInt(3, participant.getIdentifier());
                        pi.setInt(4, participant.getType());
                        if (participant.getDisplayName() == null) {
                            pi.setNull(5, java.sql.Types.VARCHAR);
                        } else {
                            pi.setString(5, participant.getDisplayName());
                        }
                        if (participant.getEmailAddress() == null) {
                            if (participant.getIdentifier() > 0) {
                                pi.setNull(6, java.sql.Types.VARCHAR);
                            } else if ((Participant.GROUP == participant.getType() || Participant.RESOURCE == participant
                                    .getType())
                                    && participant.getIdentifier() == 0) {
                                pi.setNull(6, 0);
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Missing mandatory email address in participant "
                                            + participant.getClass().getSimpleName(), new Throwable());
                                }
                                throw new OXCalendarException(
                                        OXCalendarException.Code.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD);
                            }
                        } else {
                            pi.setString(6, participant.getEmailAddress());
                        }
                        pi.addBatch();
                    }
                }
                pi.executeBatch();
            } finally {
                collection.closePreparedStatement(pi);
            }
        }
    }

    private static final String SQL_INSERT_USER = "INSERT INTO prg_dates_members (object_id, member_uid, pfid, confirm, reason, reminder, cid) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final void insertUserParticipants(final CalendarDataObject cdao, final Connection writecon, final int uid) throws SQLException, OXException {
        final UserParticipant users[] = cdao.getUsers();
        Arrays.sort(users);
        if (users != null && users.length > 0) {
            PreparedStatement stmt = null;
            try {
                stmt = writecon.prepareStatement(SQL_INSERT_USER);
                int lastid = -1;
                final OXFolderAccess access = new OXFolderAccess(cdao.getContext());
                final int objectId = cdao.getObjectID();
                if (!FolderObject.isValidFolderType(cdao.getFolderType())) {
                    final int folderId = cdao.getEffectiveFolderId();
                    if (0 < folderId) {
                        cdao.setFolderType(access.getFolderType(folderId, uid));
                    }
                }
                final int folderType = cdao.getFolderType();
                for (final UserParticipant user : users) {
                    if (lastid != user.getIdentifier()) {
                        lastid = user.getIdentifier();
                        stmt.setInt(1, objectId);
                        stmt.setInt(2, user.getIdentifier());

                        if (FolderObject.PRIVATE == folderType) {
                            if (cdao.getEffectiveFolderId() == 0) {
                                final int pfid = access.getDefaultFolder(user.getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                // final int pfid =
                                // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(upa.getIdentifier(),
                                // cdao.getContext()));
                                stmt.setInt(3, pfid);
                                user.setPersonalFolderId(pfid);
                                if (user.getIdentifier() == uid) {
                                    cdao.setActionFolder(pfid);
                                }
                            } else {
                                if (user.getIdentifier() == uid) {
                                    stmt.setInt(3, cdao.getEffectiveFolderId());
                                    user.setPersonalFolderId(cdao.getEffectiveFolderId());
                                    if (cdao.getActionFolder() == 0) {
                                        cdao.setActionFolder(cdao.getEffectiveFolderId());
                                    }
                                } else {
                                    // Prefer the personal folder ID if present in UserParticipant instance
                                    final int personalFolderId = user.getPersonalFolderId();
                                    final int pfid = personalFolderId > 0 ? personalFolderId : access.getDefaultFolder(
                                            user.getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                    // final int pfid =
                                    // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(upa.getIdentifier(),
                                    // cdao.getContext()));
                                    stmt.setInt(3, pfid);
                                    user.setPersonalFolderId(pfid);
                                }
                            }
                        } else if (FolderObject.PUBLIC == folderType) {
                            stmt.setNull(3, java.sql.Types.INTEGER);
                        } else if (FolderObject.SHARED == folderType) {
                            if (cdao.getSharedFolderOwner() == 0) {
                                throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                            }
                            if (user.getIdentifier() == cdao.getSharedFolderOwner()) {
                                if (cdao.getGlobalFolderID() == 0) {
                                    final int pfid = access.getDefaultFolder(cdao.getSharedFolderOwner(), FolderObject.CALENDAR).getObjectID();
                                    // final int pfid =
                                    // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(cdao.getSharedFolderOwner(),
                                    // cdao.getContext()));
                                    stmt.setInt(3, pfid);
                                    user.setPersonalFolderId(pfid);
                                    if (user.getIdentifier() == uid) {
                                        cdao.setActionFolder(pfid);
                                    }
                                } else {
                                    stmt.setInt(3, cdao.getGlobalFolderID());
                                    user.setPersonalFolderId(cdao.getGlobalFolderID());
                                }
                            } else {
                                final int pfid = access.getDefaultFolder(user.getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                // final int pfid =
                                // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(upa.getIdentifier(),
                                // cdao.getContext()));
                                stmt.setInt(3, pfid);
                                user.setPersonalFolderId(pfid);
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.FOLDER_TYPE_UNRESOLVEABLE);
                        }
                        stmt.setInt(4, user.getConfirm());
                        if (cdao.containsAlarm() && user.getIdentifier() == uid) {
                            user.setAlarmMinutes(cdao.getAlarm());
                        } else {
                            if (!user.containsAlarm()) {
                                user.setAlarmMinutes(-1);
                            }
                        }
                        if (user.containsConfirmMessage() && user.getConfirmMessage() != null) {
                            stmt.setString(5, user.getConfirmMessage());
                        } else {
                            stmt.setNull(5, java.sql.Types.VARCHAR);
                        }
                        if (user.getAlarmMinutes() >= 0) {
                            stmt.setInt(6, user.getAlarmMinutes());
                        } else {
                            stmt.setNull(6, java.sql.Types.INTEGER);
                        }
                        if (user.getAlarmMinutes() >= 0 && user.getIdentifier() == uid) {
                            final long la = user.getAlarmMinutes() * 60000L;
                            changeReminder(cdao.getObjectID(), uid, cdao.getEffectiveFolderId(), cdao.getContext(), cdao.isSequence(true), cdao.getEndDate(), new java.util.Date(cdao.getStartDate().getTime() - la), CalendarOperation.INSERT, false);
                        }
                        stmt.setInt(7, cdao.getContextID());
                        collection.checkUserParticipantObject(user, folderType);
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            } finally {
                collection.closePreparedStatement(stmt);
            }
        } else {
            throw new OXMandatoryFieldException(EnumComponent.APPOINTMENT, 1000011, "UserParticipant is empty!");
        }
    }

    public final void getParticipantsSQLIn(final List<CalendarDataObject> list, final Connection readcon, final int cid, final String sqlin) throws SQLException {

        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final StringBuilder query = new StringBuilder(128);
            query.append("SELECT object_id, id, type, dn, ma FROM prg_date_rights WHERE cid = ");
            query.append(cid);
            query.append(PARTICIPANTS_IDENTIFIER_IN);
            query.append(sqlin);
            query.append(" ORDER BY object_id ASC");
            rs = stmt.executeQuery(query.toString());
            final Map<Integer, CalendarDataObject> map;
            {
                final int size = list.size();
                map = new HashMap<Integer, CalendarDataObject>(size);
                for (int i = 0; i < size; i++) {
                    final CalendarDataObject cdo = list.get(i);
                    map.put(Integer.valueOf(cdo.getObjectID()), cdo);
                }
            }
            int last_oid = -1;
            Participants participants = null;
            CalendarDataObject cdao = null;
            Participant participant = null;
            while (rs.next()) {
                final int oid = rs.getInt(1);
                if (last_oid != oid) {
                    if (participants != null && cdao != null) {
                        cdao.setParticipants(participants.getList());
                    }
                    participants = new Participants();
                    last_oid = oid;
                    cdao = map.get(Integer.valueOf(oid));
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
                    final String temp2 = rs.getString(5);
                    if (rs.wasNull()) {
                        participant = null;
                    } else {
                        participant = new ExternalUserParticipant(temp2);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    }
                } else if (type == Participant.EXTERNAL_GROUP) {
                    String temp = rs.getString(4);
                    if (rs.wasNull()) {
                        temp = null;
                    }
                    final String temp2 = rs.getString(5);
                    if (rs.wasNull()) {
                        participant = null;
                    } else {
                        participant = new ExternalGroupParticipant(temp2);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    }
                } else {
                    LOG.warn("Unknown type detected for Participant :" + type);
                }
                if (participant != null) {
                    participants.add(participant);
                }
            }
            if (cdao != null && cdao.getObjectID() == last_oid) {
                cdao.setParticipants(participants.getList());
            }
        } finally {
            collection.closeResultSet(rs);
            collection.closeStatement(stmt);
        }
    }

    public final Participants getParticipants(final CalendarDataObject cdao, final Connection readcon) throws SQLException {
        final Participants participants = new Participants();
        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final StringBuilder query = new StringBuilder(128);
            query.append("SELECT id, type, dn, ma from prg_date_rights WHERE cid = ");
            query.append(cdao.getContextID());
            query.append(PARTICIPANTS_IDENTIFIER_IS);
            query.append(cdao.getObjectID());
            rs = stmt.executeQuery(query.toString());
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
                    final String temp2 = rs.getString(4);
                    if (!rs.wasNull()) {
                        participant = new ExternalUserParticipant(temp2);
                        ((ExternalUserParticipant) participant).setIdentifier(id);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    }
                } else if (type == Participant.EXTERNAL_GROUP) {
                    String temp = rs.getString(3);
                    if (rs.wasNull()) {
                        temp = null;
                    }
                    final String temp2 = rs.getString(4);
                    if (!rs.wasNull()) {
                        participant = new ExternalGroupParticipant(temp2);
                        if (temp != null) {
                            participant.setDisplayName(temp);
                        }
                    }
                } else {
                    LOG.warn("Unknown type detected for Participant :" + type);
                }
                if (participant != null) {
                    participants.add(participant);
                }
            }
        } finally {
            collection.closeResultSet(rs);
            collection.closeStatement(stmt);
        }
        return participants;
    }

    public final void getUserParticipantsSQLIn(final List<CalendarDataObject> list, final Connection readcon, final int cid, final int uid, final String sqlin) throws SQLException, OXException {
        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final StringBuilder query = new StringBuilder(140);
            query.append("SELECT object_id, member_uid, confirm, reason, pfid, reminder from prg_dates_members WHERE cid = ");
            query.append(cid);
            query.append(PARTICIPANTS_IDENTIFIER_IN);
            query.append(sqlin);
            query.append(" ORDER BY object_id");
            rs = stmt.executeQuery(query.toString());
            final Map<Integer, CalendarDataObject> map;
            {
                final int size = list.size();
                map = new HashMap<Integer, CalendarDataObject>(size);
                for (int i = 0; i < size; i++) {
                    final CalendarDataObject cdo = list.get(i);
                    map.put(Integer.valueOf(cdo.getObjectID()), cdo);
                }
            }
            String temp = null;
            int last_oid = -1;
            UserParticipant up = null;
            Participants participants = null;
            CalendarDataObject cdao = null;

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
                    cdao = map.get(Integer.valueOf(oid));
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
                        if (uid == tuid) {
                            cdao.setGlobalFolderID(pfid);
                            cdao.setPrivateFolderID(pfid);
                        }
                        up.setPersonalFolderId(pfid);
                    } else if (cdao.getFolderType() == FolderObject.SHARED) {
                        if (cdao.getSharedFolderOwner() == 0) {
                            throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                        }
                        if (cdao.getSharedFolderOwner() == tuid) {
                            cdao.setGlobalFolderID(pfid);
                            cdao.setPrivateFolderID(pfid);
                            up.setPersonalFolderId(pfid);
                        } else {
                            up.setPersonalFolderId(pfid);
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
            collection.closeResultSet(rs);
            collection.closeStatement(stmt);
        }
    }

    public final Participants getUserParticipants(final CalendarDataObject cdao, final Connection readcon, final int uid) throws SQLException, OXException {
        final Participants participants = new Participants();
        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final StringBuilder query = new StringBuilder(140);
            query.append("SELECT member_uid, confirm, reason, pfid, reminder from prg_dates_members WHERE cid = ");
            query.append(cdao.getContextID());
            query.append(PARTICIPANTS_IDENTIFIER_IS);
            query.append(cdao.getObjectID());
            rs = stmt.executeQuery(query.toString());
            String temp = null;

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
                        LOG.error(StringCollection.convertArraytoString(new Object[] { "ERROR: getUserParticipants oid:uid ", Integer.valueOf(uid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(cdao.getObjectID()) }));
                    }
                    if (cdao.getFolderType() == FolderObject.PRIVATE) {
                        if (uid == tuid) {
                            cdao.setGlobalFolderID(pfid);
                            cdao.setPrivateFolderID(pfid);
                        }
                        up.setPersonalFolderId(pfid);
                    } else if (cdao.getFolderType() == FolderObject.SHARED) {
                        if (cdao.getSharedFolderOwner() == 0) {
                            throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                        }
                        if (cdao.getSharedFolderOwner() == tuid) {
                            // we read an appointment in a shared folder and the
                            // folder owner is participant.
                            cdao.setGlobalFolderID(pfid);
                            cdao.setPrivateFolderID(pfid);
                        } else if (cdao.getPrivateFolderID() == 0) {
                            // we move into a shared folder. then folder type is
                            // shared but shared folder owner is not participant.
                            if (uid == tuid) {
                                cdao.setGlobalFolderID(pfid);
                                cdao.setPrivateFolderID(pfid);
                            } else {
                                cdao.setActionFolder(pfid);
                            }
                        }
                        up.setPersonalFolderId(pfid);
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
            collection.closeResultSet(rs);
            collection.closeStatement(stmt);
        }
        return participants;
    }

    public final CalendarDataObject loadObjectForUpdate(final CalendarDataObject cdao, final Session so, final Context ctx, final int inFolder) throws SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException {
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
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch (final OXPermissionException oxpe) {
            throw oxpe;
        } catch (final OXObjectNotFoundException oxonfe) {
            throw oxonfe;
        } catch (final OXException oxe) {
            throw oxe;
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(prep);
            if (readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
        return edao;
    }

    public final CalendarDataObject[] updateAppointment(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified) throws SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException, OXConcurrentModificationException {
        return updateAppointment(cdao, edao, writecon, so, ctx, inFolder, clientLastModified, true, false);
    }

    private final CalendarDataObject[] updateAppointment(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified, final boolean clientLastModifiedCheck, final boolean skipParticipants) throws DataTruncation, SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException, OXConcurrentModificationException {

        final CalendarOperation co = new CalendarOperation();

        if (cdao.getFolderMove() && cdao.getFolderType() == FolderObject.PUBLIC && edao.getPrivateFlag()) {
            throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.PRIVATE_MOVE_TO_PUBLIC));
        }

        collection.detectFolderMoveAction(cdao, edao);

        if (clientLastModified == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        } else if (edao.getLastModified() == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        }

        if (clientLastModifiedCheck && edao.getLastModified().getTime() > clientLastModified.getTime()) {
            throw new OXConcurrentModificationException(EnumComponent.APPOINTMENT, OXConcurrentModificationException.ConcurrentModificationCode.CONCURRENT_MODIFICATION);
        }

        final int rec_action = co.checkUpdateRecurring(cdao, edao);
        if (edao.containsRecurrencePosition() && edao.getRecurrencePosition() > 0) {
            /*
             * edao denotes a change exception
             */
            if (cdao.getFolderMove()) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_MOVE_EXCEPTION);
            }
            if (edao.containsPrivateFlag() && cdao.containsPrivateFlag() && edao.getPrivateflag() != cdao.getPrivateflag()) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_PRIVATE_FLAG);
            }
            if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) {
                if (edao.getRecurrencePosition() != cdao.getRecurrencePosition()) {
                    throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_POSITION_CHANGE);
                }
            } else {
                cdao.setRecurrencePosition(edao.getRecurrencePosition());
            }
            /*-
             * TODO: Uncomment?
             * 
             * Check if the change exception to create is moved to a date which is already covered by one of recurrence's occurrences. This
             * is needed since MS Outlook is not able to display two occurrences on one day
            if (CalendarRecurringCollection.isOccurrenceDate(
                cdao.getStartDate().getTime(),
                edao.getStartDate().getTime(),
                collection.getAppointmentByID(edao.getRecurrenceID(), so),
                collection.getChangeExceptionDatesByRecurrence(edao.getRecurrenceID(), so))) {
                LOG.warn(new StringBuilder("There is another appointment of series \"").append(
                    Tools.getAppointmentTitle(edao.getRecurrenceID(), ctx)).append("\" on day ").append(
                    Tools.getUTCDateFormat(CalendarRecurringCollection.normalizeLong(cdao.getStartDate().getTime()))).toString());
            }
            */
        }

        CalendarDataObject clone = null;

        final boolean changeMasterTime = checkRecurrenceMasterTimeUpdate(cdao, edao);

        //Reset all exceptions (change and delete)
        if(changeMasterTime) {
            cdao.setExceptions(null);
            cdao.setDelExceptions(null);
        }

        if (rec_action == collection.CHANGE_RECURRING_TYPE || changeMasterTime) {
            if (edao.getRecurrenceID() > 0 && edao.getObjectID() != edao.getRecurrenceID()) {
                throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_TYPE_CHANGE, new Object[0]);
            }
            final List<Integer> exceptions = getExceptionList(null, ctx, edao.getRecurrenceID());
            if (exceptions != null && !exceptions.isEmpty()) {
                final Integer oids[] = exceptions.toArray(new Integer[exceptions.size()]);
                if (oids.length > 0) {
                    deleteAllRecurringExceptions(oids, so, writecon, false);
                    for (int a = 0; a < exceptions.size(); a++) {
                        triggerDeleteEvent(exceptions.get(a).intValue(), inFolder, so, ctx, null);
                    }
                }
            }
            // Fake a series deletion for MS Outlook
            backupAppointment(writecon, so.getContextId(), edao.getObjectID(), so.getUserId());
        } else if (rec_action == collection.RECURRING_EXCEPTION_DELETE) {
            final List<Integer> exceptions = getExceptionList(null, ctx, edao.getRecurrenceID());
            if (exceptions != null && !exceptions.isEmpty()) {
                final Integer oids[] = exceptions.toArray(new Integer[exceptions.size()]);
                if (oids.length > 0) {
                    deleteAllRecurringExceptions(oids, so, writecon);
                    for (int a = 0; a < exceptions.size(); a++) {
                        triggerDeleteEvent(exceptions.get(a).intValue(), inFolder, so, ctx, null);
                    }
                }
            }
            collection.purgeExceptionFieldsFromObject(cdao);
        } else if (rec_action == collection.RECURRING_EXCEPTION_DELETE_EXISTING) {
        	final Date[] deleteExceptions = cdao.getDeleteException();
			final List<Integer> deleteExceptionPositions = new ArrayList<Integer>();
			{
				/*
				 * Get corresponding positions in recurring appointment whose
				 * change exception shall be turned to a delete exception
				 */
			    final int[] positions = collection.getDatesPositions(deleteExceptions, edao);
			    for (final int position : positions) {
			        deleteExceptionPositions.add(Integer.valueOf(position));
                }
			}
			if (!deleteExceptionPositions.isEmpty()) {
				final Integer[] objectIDs2Delete;
				final List<Long> dates;
				{
					final Object positions[] = deleteExceptionPositions.toArray();
					dates = new ArrayList<Long>(positions.length);
					final List<Integer> objectIDs = getDeletedExceptionList(null, ctx, edao.getRecurrenceID(), StringCollection.getSqlInString(positions), dates);
					objectIDs2Delete = objectIDs.toArray(new Integer[objectIDs.size()]);
				}
				if (objectIDs2Delete.length > 0) {
					final CalendarSql calendarSql = new CalendarSql(so);
					for (int i = 0; i < objectIDs2Delete.length; i++) {
						final int objectID2Delete = objectIDs2Delete[i].intValue();
						final CalendarDataObject toDelete = calendarSql.getObjectById(objectID2Delete, inFolder);
						deleteAppointment(writecon, so.getContextId(), objectID2Delete, so.getUserId());
						triggerDeleteEvent(objectID2Delete, inFolder, so, ctx, toDelete);
					}
				}
				// Remove deleted change exceptions from list
				if (!dates.isEmpty()) {
					Date[] cdates = collection.removeException(edao.getChangeException(), dates
							.remove(0).longValue());
					while (!dates.isEmpty()) {
						cdates = collection.removeException(cdates, dates.remove(0).longValue());
					}
					cdao.setChangeExceptions(cdates);
				}
			}
        } else if (rec_action == collection.RECURRING_CREATE_EXCEPTION) {
            // Because the GUI only sends changed fields, we have to create a
            // merged object
            // from cdao and edao and then we force an insert!
            if (edao.containsPrivateFlag() && cdao.containsPrivateFlag() && edao.getPrivateflag() != cdao.getPrivateflag()) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_PRIVATE_FLAG);
            }
            /*
             * Create a clone for the "new" change exception
             */
            clone = collection.cloneObjectForRecurringException(cdao, edao, so.getUserId());
            /*-
             * TODO: Uncomment?
             * Check if the change exception to create is moved to a date which is already covered by one of recurrence's occurrences. This
             * is needed since MS Outlook is not able to display two occurrences on one day
            if (CalendarRecurringCollection.isOccurrenceDate(
                clone.getStartDate().getTime(),
                clone.getRecurrenceDatePosition().getTime(),
                edao,
                collection.getChangeExceptionDatesByRecurrence(edao.getRecurrenceID(), so))) {
                LOG.warn(new StringBuilder("There is another appointment of series \"").append(edao.getTitle()).append("\" on day ").append(
                    Tools.getUTCDateFormat(CalendarRecurringCollection.normalizeLong(clone.getStartDate().getTime()))).toString());
            }
            */
            try {
                cdao.setRecurrenceCalculator(edao.getRecurrenceCalculator());
                if (cdao.containsAlarm()) {
                    final UserParticipant[] users;
                    if (cdao.containsUserParticipants() && cdao.getUsers() != null) {
                        users = collection.checkAndModifyAlarm(cdao, cdao.getUsers(), so.getUserId(), edao.getUsers());
                    } else {
                        users = collection.checkAndModifyAlarm(cdao, edao.getUsers(), so.getUserId(), edao.getUsers());
                    }
                    clone.setUsers(users);
                    clone.setAlarm(cdao.getAlarm());
                    cdao.removeAlarm();
                }
                final long lastModified = System.currentTimeMillis();
                clone.setCreationDate(new Date(lastModified));
                clone.setLastModified(new Date(lastModified));
                clone.setNumberOfAttachments(0);
                clone.setNumberOfLinks(0);
                // Insert without triggering event
                insertAppointment0(clone, writecon, so, false);
                // Trigger NEW event for new (user) participants only
            	// Remove recurring information from change exception
            	clone.setRecurrenceType(CalendarObject.NO_RECURRENCE);
            	clone.removeInterval();
            	clone.removeOccurrence();
            	clone.removeUntil();
            	clone.removeDeleteExceptions();
            	clone.removeChangeExceptions();
            	{
					// Clone users
					final UserParticipant[] cloneUsers = clone.getUsers();
					final UserParticipant[] newCloneUsers = new UserParticipant[cloneUsers.length];
					for (int i = 0; i < cloneUsers.length; i++) {
					    newCloneUsers[i] = (UserParticipant) cloneUsers[i].clone();
					}
					clone.setUsers(newCloneUsers);
					// Clone participants
					final Participant[] cloneParticipants = clone.getParticipants();
					final Participant[] newCloneParticipants = new Participant[cloneParticipants.length];
					for (int i = 0; i < cloneParticipants.length; i++) {
					    newCloneParticipants[i] = cloneParticipants[i].getClone();
					}
					clone.setParticipants(newCloneParticipants);
				}
				{
					// Create asymmetric set difference for users
					final Set<UserParticipant> diffUser = new HashSet<UserParticipant>(Arrays.asList(clone.getUsers()));
					// Mark every user participant to ignore notification
					for (final UserParticipant cur : diffUser) {
						cur.setIgnoreNotification(true);
					}
					// Except for the new ones
					diffUser.removeAll(Arrays.asList(edao.getUsers()));
					for (final UserParticipant cur : diffUser) {
						cur.setIgnoreNotification(false);
					}
				}
				{
					// Create asymmetric set difference for participants
					final Set<Participant> diffParticipants = new HashSet<Participant>(Arrays.asList(clone
							.getParticipants()));
					// Mark every participant to ignore notification
					for (final Participant cur : diffParticipants) {
						cur.setIgnoreNotification(true);
					}
					// Except for the new ones
					diffParticipants.removeAll(Arrays.asList(edao.getParticipants()));
					for (final Participant cur : diffParticipants) {
						cur.setIgnoreNotification(false);
					}
				}

                // Trigger NEW event for newly added users/participants
                collection.triggerEvent(so, CalendarOperation.INSERT, clone);

                // Proceed with update
                collection.removeFieldsFromObject(cdao);
                // no update here
                cdao.setParticipants(edao.getParticipants());
                cdao.setUsers(edao.getUsers());
                cdao.setRecurrence(edao.getRecurrence());
                cdao.setLastModified(clone.getLastModified());
            } catch (final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle, new Object[0]);
            } catch (final LdapException ldape) {
                throw new OXException(ldape);
            } catch (final OXCalendarException oxce) {
                throw oxce;
            } catch (final OXException oxe) {
                throw oxe;
            } catch (final Exception ex) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, ex, Integer.valueOf(2));
            }
        }

        /*
         * Check that any specified exception date is contained in recurring appointment's range
         */
        if ((cdao.containsDeleteExceptions() || cdao.containsChangeExceptions()) && edao.getRecurrenceID() > 0) {
            final CalendarDataObject tdao;
            if (edao.getObjectID() == edao.getRecurrenceID()) {
                // edao already denotes main appointment
                tdao = edao;
            } else {
                // Load main appointment for recurring calculation
                tdao = new CalendarSql(so).getObjectById(edao.getRecurrenceID(), inFolder);
            }
            if (!collection.checkIfDatesOccurInRecurrence(collection.mergeExceptionDates(
                    cdao.getDeleteException(), cdao.getChangeException()), tdao)) {
                throw new OXCalendarException(OXCalendarException.Code.FOREIGN_EXCEPTION_DATE);
            }
        }

        final int ucols[] = new int[26];
        int uc = CalendarOperation.fillUpdateArray(cdao, edao, ucols);
        final MBoolean cup = new MBoolean(false);
        if (uc > 0 || collection.check(cdao.getUsers(), edao.getUsers())) {

            ucols[uc++] = AppointmentObject.LAST_MODIFIED;
            ucols[uc++] = AppointmentObject.MODIFIED_BY;

            final StringBuilder update = new StringBuilder();
            update.append("UPDATE prg_dates pd SET ");
            update.append(collection.getFieldName(ucols[0]));
            update.append(" = ?");
            for (int a = 1; a < uc; a++) {
                update.append(", ");
                update.append(collection.getFieldName(ucols[a]));
                update.append(" = ?");
            }

            update.append(" WHERE cid = ");
            update.append(cdao.getContextID());
            update.append(DATES_IDENTIFIER_IS);
            update.append(cdao.getObjectID());
            // very fast tests fail if check is done against System.currentTimeMillis().
            if (clientLastModifiedCheck) {
                update.append(" AND changing_date <= ");
                update.append(clientLastModified.getTime());
            }

            PreparedStatement pst = null;

            try {

                pst = writecon.prepareStatement(update.toString());

                for (int a = 0; a < uc; a++) {
                    final StatementFiller statementFiller = STATEMENT_FILLERS.get(Integer.valueOf(ucols[a]));
                    if (null == statementFiller) {
                        throw new SQLException("Error: Calendar: Update: Mapping for " + ucols[a] + " not implemented!");
                    }
                    statementFiller.fillStatement(pst, a + 1, cdao);
                }
                if(!skipParticipants) {
                    updateParticipants(cdao, edao, so.getUserId(), so.getContextId(), writecon, cup);
                }
                final int ret = pst.executeUpdate();
                if (ret == 0) {
                    throw new OXConcurrentModificationException(EnumComponent.APPOINTMENT, OXConcurrentModificationException.ConcurrentModificationCode.CONCURRENT_MODIFICATION);
                }
                if (edao.isException()) {
                    // Update last-modified of master
                    updateLastModified(edao.getRecurrenceID(), ctx.getContextId(), so.getUserId(), cdao.getLastModified().getTime(), writecon);
                }
            } finally {
                collection.closePreparedStatement(pst);
            }

        }
        cdao.setParentFolderID(cdao.getActionFolder());

        if (cdao.getFolderMove()) {
            /*
             * Fake a deletion on MOVE operation for MS Outlook prior to
             * performing actual UPDATE
             */
            backupAppointment(writecon, so.getContextId(), cdao.getObjectID(), so.getUserId());
            
            /*
             * Update reminders' folder ID on move operation
             */
            final ReminderSQLInterface reminderInterface = new ReminderHandler(ctx);
            final SearchIterator<?> it = reminderInterface.listReminder(cdao.getObjectID());
            final List<ReminderObject> toUpdate = new ArrayList<ReminderObject>();
            try {
                while (it.hasNext()) {
                    toUpdate.add((ReminderObject) it.next());
                }
            } catch (final SearchIteratorException e) {
                LOG.error("Reminder update failed", e);
            } finally {
                try {
                    it.close();
                } catch (final SearchIteratorException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            for (final ReminderObject reminder : toUpdate) {
                reminder.setFolder(cdao.getParentFolderID());
                reminderInterface.updateReminder(reminder);
            }
        }

        final boolean solo_reminder = collection.checkForSoloReminderUpdate(cdao, ucols, cup);
        collection.checkAndRemovePastReminders(cdao, edao);
        if (!solo_reminder) {
            collection.triggerModificationEvent(so, edao, cdao);
        }
        if(rec_action == collection.RECURRING_CREATE_EXCEPTION) {
            try {
                CalendarCallbacks.getInstance().createdChangeExceptionInRecurringAppointment(cdao, clone,inFolder, so);
                
            } catch (final OXException x) {
                throw x;
            } catch (final AbstractOXException e) {
                throw new OXCalendarException(e);
            }
        }
        if (clone != null) {
            cdao.setObjectID(clone.getObjectID());
            cdao.setLastModified(clone.getLastModified());
        }
        /*
         * Check if last occurrence(s) of a recurring appointment was deleted
         */
        if (clientLastModifiedCheck
                && (cdao.containsDeleteExceptions() || cdao.containsChangeExceptions())
                && (!cdao.containsChangeExceptions() || cdao.getChangeException() == null || cdao.getChangeException().length <= 0)
                && (cdao.containsDeleteExceptions() && cdao.getDeleteException() != null && cdao.getDeleteException().length > 0)) {
            /*
             * No change exception exists for this recurring appointment;
             * further checking needed
             */
            final CalendarDataObject main;
            if (edao.getRecurrencePosition() > 0 || edao.getRecurrenceDatePosition() != null) {
                /*
                 * Update single appointment; load main appointment first
                 */
                main = new CalendarSql(so).getObjectById(edao.getRecurrenceID(), inFolder);
            } else {
                /*
                 * Main appointment already loaded
                 */
                main = edao;
            }
            final RecurringResultsInterface rresults = collection.calculateRecurring(main, 0, 0, 0,
                collection.MAXTC, true);
            /*
             * Check if every possible occurrence is covered by a delete exception
             */
            if (rresults.size() <= cdao.getDeleteException().length) {
                /*
                 * Commit current transaction
                 */
                if (!writecon.getAutoCommit()) {
                    writecon.commit();
                }
                /*
                 * Delete whole recurring appointment since its last occurrence
                 * has been deleted through previous transaction
                 */
                deleteSingleAppointment(main.getContextID(), main.getObjectID(), main.getCreatedBy(), main
                        .getCreatedBy(), inFolder, null, writecon, main.getFolderType(), so, ctx,
                        collection.RECURRING_NO_ACTION, main, main, clientLastModified);
            }

        }
        return null;
    }

    /**
     * Checks, if the start or end date of the whole sequence has been changed.
     * @param newObject new CalendarDataObject
     * @param currencObject old CalendarDataObject
     * @return
     */
    private boolean checkRecurrenceMasterTimeUpdate(final CalendarDataObject newObject, final CalendarDataObject currentObject) {
        //Is sequence?
        if (!currentObject.containsRecurrenceType() || currentObject.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE) {
            return false;
        }
        
        //Is Exception
        if(newObject.containsRecurrencePosition() && newObject.getRecurrencePosition() != 0) {
            return false;
        }
        if(newObject.containsRecurrenceDatePosition()) {
            return false;
        }

        final Date newStart = newObject.getStartDate();
        final Date newEnd = newObject.getEndDate();

        //No new dates
        if(newStart == null && newEnd == null) {
            return false;
        }

        //New start date
        if(newStart != null && !newStart.equals(currentObject.getStartDate())) {
            return true;
        }

        //New end date
        if(newEnd != null && !newEnd.equals(currentObject.getEndDate())) {
            return true;
        }

        return false;
    }

    private final void updateParticipants(final CalendarDataObject cdao, final CalendarDataObject edao, final int uid, final int cid, final Connection writecon, final MBoolean cup) throws SQLException, OXException, LdapException {
        final Participant[] participants = cdao.getParticipants();
        UserParticipant[] users = cdao.getUsers();

        if (users == null && cdao.getFolderMoveAction() != CalendarOperation.NO_MOVE_ACTION) {
            users = edao.getUsers();
            CalendarOperation.fillUserParticipants(cdao);
        }

        final Participant[] old_participants = edao.getParticipants();
        final UserParticipant[] old_users = edao.getUsers();

        /*
         * Check if updated appointment has the private flag set. If so check if
         * either the updated appointment specifies more than one appointment in
         * participant information (value is different from null) or the
         * storage version specifies more than one appointment in participant
         * information
         */
        if ((cdao.containsPrivateFlag() ? cdao.getPrivateFlag() : edao.getPrivateFlag())
                && ((participants == null ? old_participants.length > 1 : participants.length > 1) || (users == null ? old_users.length > 1
                        : users.length > 1))) {
            /*
             * Updated appointment has private flag set but contains more than
             * one participant
             */
            throw new OXCalendarException(OXCalendarException.Code.PRIVATE_FLAG_AND_PARTICIPANTS, new Object[0]);
        }

        int check_up = old_users.length;

        Participant[] new_participants = null;
        Participant[] deleted_participants = null;

        UserParticipant[] new_userparticipants = null;
        UserParticipant[] modified_userparticipants = null;
        UserParticipant[] deleted_userparticipants = null;

        final Participants deleted = new Participants();
        final Participants new_deleted = new Participants();

        if (participants != null && !Arrays.equals(participants, old_participants)) {
            Arrays.sort(participants);
            Arrays.sort(old_participants);
            new_participants = CalendarOperation.getNewParticipants(participants, old_participants);
            deleted_participants = CalendarOperation.getDeletedParticipants(old_participants, participants);
        }

        final boolean time_change = collection.detectTimeChange(cdao, edao);

        if (time_change && users == null) {
            users = edao.getUsers();
        }

        if (users != null) {
            Arrays.sort(users);
            Arrays.sort(old_users);
            final Participants p[] = CalendarOperation.getModifiedUserParticipants(users, old_users, edao.getCreatedBy(), uid, cdao.getSharedFolderOwner(), time_change, cdao);
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
                /*
                 * TODO: Check that appointment's owner is not removed as participant
                 */
//                for (final UserParticipant userParticipant : deleted_userparticipants) {
//                    if (userParticipant.getIdentifier() == edao.getCreatedBy()) {
//                        /*
//                         * Deny to remove owner from participants
//                         */
//                        throw new OXCalendarException(OXCalendarException.Code.OWNER_REMOVAL_EXCEPTION);
//                    }
//                }
                check_up -= deleted_userparticipants.length;
            }
        }
        final boolean onlyAlarmChange = modified_userparticipants == null;
        modified_userparticipants = collection.checkAndModifyAlarm(cdao, modified_userparticipants, uid, edao.getUsers());

        if (check_up < 1) {
            throw new OXCalendarException(OXCalendarException.Code.UPDATE_WITHOUT_PARTICIPANTS);
        }

        if (new_participants != null && new_participants.length > 0) {
            final Set<Integer> knownExternalIds = createExternalIdentifierSet(old_participants);
            cup.setMBoolean(true);
            PreparedStatement dr = null;
            try {
                dr = writecon.prepareStatement("insert into prg_date_rights (object_id, cid, id, type, dn, ma) values (?, ?, ?, ?, ?, ?)");
                Arrays.sort(new_participants);
                int lastid = -1;
                int lasttype = -1;
                for (int a = 0; a < new_participants.length; a++) {
                    if (new_participants[a].getIdentifier() == 0 && new_participants[a].getType() == Participant.EXTERNAL_USER && new_participants[a].getEmailAddress() != null) {
                        final ExternalUserParticipant eup = new ExternalUserParticipant(new_participants[a]
                                .getEmailAddress());
                        /*
                         * Determine an unique identifier
                         */
                        Integer identifier = Integer.valueOf(new_participants[a].getEmailAddress().hashCode());
                        while (knownExternalIds.contains(identifier)) {
                            identifier = Integer.valueOf(identifier.intValue() + 1);

                        }
                        /*
                         * Add to known identifiers
                         */
                        knownExternalIds.add(identifier);
                        eup.setIdentifier(identifier.intValue());
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
                            if (Participant.GROUP == new_participants[a].getType() ? new_participants[a].getIdentifier() < 0 : new_participants[a].getIdentifier() <= 0) {
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
                collection.closePreparedStatement(dr);
            }
        }

        if (deleted_participants != null && deleted_participants.length > 0) {
            cup.setMBoolean(true);
            PreparedStatement pd = null;
            PreparedStatement pde = null;
            try {
                pd = writecon.prepareStatement("delete from prg_date_rights WHERE object_id = ? AND cid = ? AND id = ? AND type = ?");
                for (int a = 0; a < deleted_participants.length; a++) {
                    if (deleted_participants[a].getType() == Participant.EXTERNAL_USER || deleted_participants[a].getType() == Participant.EXTERNAL_GROUP) {
                        if (pde == null) {
                            pde = writecon.prepareStatement("delete from prg_date_rights WHERE object_id = ? AND cid = ? AND type = ? AND ma LIKE ?");
                        }
                        pde.setInt(1, cdao.getObjectID());
                        pde.setInt(2, cid);
                        pde.setInt(3, deleted_participants[a].getType());
                        pde.setString(4, deleted_participants[a].getEmailAddress());
                        pde.addBatch();
                    } else {
                        pd.setInt(1, cdao.getObjectID());
                        pd.setInt(2, cid);
                        pd.setInt(3, deleted_participants[a].getIdentifier());
                        pd.setInt(4, deleted_participants[a].getType());
                        pd.addBatch();
                    }
                }
                pd.executeBatch();
                if (pde != null) {
                    pde.executeBatch();
                    collection.closePreparedStatement(pde);
                }
            } finally {
                collection.closePreparedStatement(pd);
            }
        }

        if (new_userparticipants != null && new_userparticipants.length > 0) {
            cup.setMBoolean(true);
            PreparedStatement pi = null;
            try {
                pi = writecon.prepareStatement("insert into prg_dates_members (object_id, member_uid, confirm, reason, pfid, reminder, cid) values (?, ?, ?, ?, ?, ?, ?)");
                Arrays.sort(new_userparticipants);
                int lastid = -1;
                final OXFolderAccess access = new OXFolderAccess(cdao.getContext());
                if (!FolderObject.isValidFolderType(edao.getFolderType())) {
                    final int folderId = edao.getEffectiveFolderId();
                    if (0 < folderId) {
                        edao.setFolderType(access.getFolderType(folderId, uid));
                    }
                }
                final int folderType = edao.getFolderType();
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
                        if (new_userparticipants[a].getConfirmMessage() == null) {
                            pi.setNull(4, java.sql.Types.VARCHAR);
                        } else {
                            pi.setString(4, new_userparticipants[a].getConfirmMessage());
                        }

                        if (FolderObject.PRIVATE == folderType) {
                            if (new_userparticipants[a].getIdentifier() == uid) {
                                if (cdao.getGlobalFolderID() == 0) {
                                    try {
                                        final int pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        // final int pfid =
                                        // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(new_userparticipants[a].getIdentifier(),
                                        // cdao.getContext()));
                                        pi.setInt(5, pfid);
                                        new_userparticipants[a].setPersonalFolderId(pfid);
                                    } catch (final Exception fe) {
                                        throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(3));
                                    }
                                } else {
                                    pi.setInt(5, cdao.getGlobalFolderID());
                                    new_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                }
                            } else {
                                try {
                                    final int pfid;
                                    if (cdao.getFolderMove()) {
                                        if (cdao.getFolderType() == FolderObject.PUBLIC) {
                                            // A move into a public folder: Set folder ID to zero since folder ID is then kept in calendar object itself
                                            pfid = 0;
                                        } else if (cdao.getFolderType() == FolderObject.SHARED) {
                                            // A move into shared folder
                                            if (new_userparticipants[a].getIdentifier() == cdao.getSharedFolderOwner()) {
                                                // A move into a shared folder and current participant denotes the shared folder's owner: Set folder ID to action folder
                                                pfid = cdao.getActionFolder();
                                            } else {
                                                // Non-folder-owner
                                                pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                            }
                                        } else {
                                            // A move into another private folder: Set to default folder ID for non-folder-owner
                                            pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        }
                                    } else {
                                        // always set the folder to the private folder of the user participant in private calendar folders.
                                        pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                    }
                                    if (pfid == 0) {
                                        pi.setNull(5, java.sql.Types.INTEGER);
                                    } else {
                                        pi.setInt(5, pfid);
                                    }
                                    new_userparticipants[a].setPersonalFolderId(pfid);
                                } catch (final Exception fe) {
                                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(4));
                                }
                            }
                        } else if (FolderObject.PUBLIC == folderType) {
                            pi.setNull(5, java.sql.Types.INTEGER);
                        } else if (FolderObject.SHARED == folderType) {
                            if (edao.getSharedFolderOwner() == 0) {
                                throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                            }
                            if (edao.getSharedFolderOwner() == new_userparticipants[a].getIdentifier()) {
                                if (cdao.getGlobalFolderID() == 0) {
                                    try {
                                        final int pfid = access.getDefaultFolder(edao.getSharedFolderOwner(), FolderObject.CALENDAR).getObjectID();
                                        // final int pfid =
                                        // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(edao.getSharedFolderOwner(),
                                        // cdao.getContext()));
                                        pi.setInt(5, pfid);
                                        new_userparticipants[a].setPersonalFolderId(pfid);
                                    } catch (final Exception fe) {
                                        throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(5));
                                    }
                                } else {
                                    pi.setInt(5, cdao.getGlobalFolderID());
                                    new_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                }
                            } else {
                                try {
                                    final int pfid;
                                    if (cdao.getFolderMove()) {
                                        if (cdao.getFolderType() == FolderObject.PUBLIC) {
                                            // A move into a public folder: Set folder ID to zero since folder ID is then kept in calendar object itself
                                            pfid = 0;
                                        } else if (cdao.getFolderType() == FolderObject.SHARED) {
                                            // A move into shared folder
                                            if (new_userparticipants[a].getIdentifier() == cdao.getSharedFolderOwner()) {
                                                // A move into a shared folder and current participant denotes the shared folder's owner: Set folder ID to action folder
                                                pfid = cdao.getActionFolder();
                                            } else {
                                                // Non-folder-owner
                                                pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                            }
                                        } else {
                                            // A move into another private folder: Set to default folder ID for non-folder-owner
                                            pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        }
                                    } else {
                                        // always set the folder to the private folder of the user participant in private calendar folders.
                                        pfid = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                    }
                                    if (pfid == 0) {
                                        pi.setNull(5, java.sql.Types.INTEGER);
                                    } else {
                                        pi.setInt(5, pfid);
                                    }
                                    new_userparticipants[a].setPersonalFolderId(pfid);
                                } catch (final Exception fe) {
                                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(3));
                                }
                            }
                        } else {
                            throw new OXCalendarException(OXCalendarException.Code.FOLDER_TYPE_UNRESOLVEABLE);
                        }

                        if (new_userparticipants[a].getAlarmMinutes() >= 0 && new_userparticipants[a].containsAlarm()) {
                            pi.setInt(6, new_userparticipants[a].getAlarmMinutes());
                            final long la = new_userparticipants[a].getAlarmMinutes() * 60000L;
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
                            final boolean isSequence = cdao.isSequence(true);
                            changeReminder(cdao.getObjectID(), uid, cdao.getEffectiveFolderId(), cdao.getContext(), isSequence, end_date, new java.util.Date(calc_date.getTime() - la), CalendarOperation.INSERT, (isSequence ? checkRecurrenceChange(cdao, edao) : false));
                        } else {
                            pi.setNull(6, java.sql.Types.INTEGER);
                        }

                        pi.setInt(7, cid);
                        collection.checkUserParticipantObject(new_userparticipants[a], cdao.getFolderType());
                        pi.addBatch();
                        if (checkForDeletedParticipants(new_userparticipants[a].getIdentifier(), cdao.getContextID(), cdao.getObjectID(), cdao.getContext())) {
                            deleted.add(new_userparticipants[a]);
                        }
                    }
                }
                pi.executeBatch();
            } finally {
                collection.closePreparedStatement(pi);
            }
        }

        if (modified_userparticipants != null && modified_userparticipants.length > 0) {
            cup.setMBoolean(!onlyAlarmChange);
            PreparedStatement pu = null;
            try {
                pu = writecon.prepareStatement("update prg_dates_members SET confirm = ?, reason = ?, pfid = ?, reminder = ? WHERE object_id = ? AND cid = ? and member_uid = ?");
                final OXFolderAccess access = new OXFolderAccess(cdao.getContext());
                if (!FolderObject.isValidFolderType(cdao.getFolderType())) {
                    final int folderId = cdao.getEffectiveFolderId();
                    if (0 < folderId) {
                        cdao.setFolderType(access.getFolderType(folderId, uid));
                    }
                }
                final int folderType = cdao.getFolderType();
                for (int a = 0; a < modified_userparticipants.length; a++) {
                    // TODO: Enhance this and add a condition for lastid
                    pu.setInt(1, modified_userparticipants[a].getConfirm());
                    if (modified_userparticipants[a].getConfirmMessage() == null) {
                        pu.setNull(2, java.sql.Types.VARCHAR);
                    } else {
                        pu.setString(2, modified_userparticipants[a].getConfirmMessage());
                    }
                    if (modified_userparticipants[a].getIdentifier() == uid) {
                        if (FolderObject.PRIVATE == folderType) {
                            if (cdao.getGlobalFolderID() == 0) {
                                try {
                                    int pfid = 0;
                                    if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                        pfid = modified_userparticipants[a].getPersonalFolderId();
                                    } else {
                                        pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        // pfid =
                                        // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(modified_userparticipants[a].getIdentifier(),
                                        // cdao.getContext()));
                                        modified_userparticipants[a].setPersonalFolderId(pfid);
                                    }
                                    pu.setInt(3, pfid);
                                } catch (final Exception fe) {
                                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(6));
                                }
                            } else {
                                pu.setInt(3, cdao.getGlobalFolderID());
                                modified_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                            }
                        } else if (FolderObject.PUBLIC == folderType) {
                            pu.setNull(3, java.sql.Types.INTEGER);
                        } else if (FolderObject.SHARED == folderType) {
                            if (modified_userparticipants[a].getIdentifier() == uid && uid == cdao.getSharedFolderOwner()) {
                                if (cdao.getGlobalFolderID() == 0) {
                                    try {
                                        int pfid = 0;
                                        if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                            pfid = modified_userparticipants[a].getPersonalFolderId();
                                        } else {
                                            pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                            // pfid =
                                            // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(modified_userparticipants[a].getIdentifier(),
                                            // cdao.getContext()));
                                            modified_userparticipants[a].setPersonalFolderId(pfid);
                                        }
                                        pu.setInt(3, pfid);
                                    } catch (final Exception fe) {
                                        throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(6));
                                    }
                                } else {
                                    pu.setInt(3, cdao.getGlobalFolderID());
                                    modified_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                }
                            } else {
                                try {
                                    int pfid = 0;
                                    if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                        pfid = modified_userparticipants[a].getPersonalFolderId();
                                    } else {
                                        pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                        // pfid =
                                        // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(modified_userparticipants[a].getIdentifier(),
                                        // cdao.getContext()));
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
                        if (FolderObject.PRIVATE == folderType) {
                            int pfid = 0;
                            if (modified_userparticipants[a].getPersonalFolderId() > 0) {
                                pfid = modified_userparticipants[a].getPersonalFolderId();
                            } else {
                                pfid = access.getDefaultFolder(modified_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                modified_userparticipants[a].setPersonalFolderId(pfid);
                            }
                            pu.setInt(3, pfid);
                        } else if (FolderObject.PUBLIC == folderType) {
                            pu.setNull(3, java.sql.Types.INTEGER);
                        } else if (FolderObject.SHARED == folderType) {
                            if (edao.getSharedFolderOwner() == 0) {
                                throw new OXCalendarException(OXCalendarException.Code.NO_SHARED_FOLDER_OWNER);
                            }
                            if (edao.getSharedFolderOwner() == modified_userparticipants[a].getIdentifier()) {
                                if (cdao.getGlobalFolderID() == 0) {
                                    if (cdao.getActionFolder() == 0) {
                                        try {
                                            final int pfid = access.getDefaultFolder(edao.getSharedFolderOwner(), FolderObject.CALENDAR).getObjectID();
                                            // final int pfid =
                                            // Integer.valueOf(OXFolderTools.getCalendarDefaultFolder(edao.getSharedFolderOwner(),
                                            // cdao.getContext()));
                                            pu.setInt(3, pfid);
                                            modified_userparticipants[a].setPersonalFolderId(pfid);
                                        } catch (final Exception fe) {
                                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, fe, Integer.valueOf(7));
                                        }
                                    } else {
                                        pu.setInt(3, cdao.getActionFolder());
                                        modified_userparticipants[a].setPersonalFolderId(cdao.getActionFolder());
                                    }
                                } else {
                                    pu.setInt(3, cdao.getGlobalFolderID());
                                    modified_userparticipants[a].setPersonalFolderId(cdao.getGlobalFolderID());
                                }
                            } else {
                                pu.setInt(3, modified_userparticipants[a].getPersonalFolderId());
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
                        final long la = modified_userparticipants[a].getAlarmMinutes() * 60000L;
                        final java.util.Date reminder = new java.util.Date(calc_date.getTime() - la);
                        final boolean isSequence = cdao.isSequence(true);
                        changeReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), folder_id, cdao.getContext(), isSequence, end_date, reminder, CalendarOperation.UPDATE, isSequence ? checkRecurrenceChange(cdao, edao) : false);
                    } else {
                        pu.setNull(4, java.sql.Types.INTEGER);
                        deleteReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), cdao.getContext());
                        //changeReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), -1, cdao.getContext(), cdao.isSequence(true), null, null, CalendarOperation.DELETE, false);
                    }

                    pu.setInt(5, cdao.getObjectID());
                    pu.setInt(6, cid);
                    pu.setInt(7, modified_userparticipants[a].getIdentifier());
                    collection.checkUserParticipantObject(modified_userparticipants[a], folderType);
                    pu.addBatch();
                }
                pu.executeBatch();
            } finally {
                collection.closePreparedStatement(pu);
            }
        }

        if (deleted_userparticipants != null && deleted_userparticipants.length > 0) {
            cup.setMBoolean(true);
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
                    deleteReminder(cdao.getObjectID(), uid, cdao.getContext());
                    //changeReminder(cdao.getObjectID(), uid, -1, cdao.getContext(), cdao.isSequence(true), end_date, new java.util.Date(calc_date.getTime() + deleted_userparticipants[a].getAlarmMinutes()), CalendarOperation.DELETE, false);
                    new_deleted.add(deleted_userparticipants[a]);
                }
                pd.executeBatch();
            } finally {
                collection.closePreparedStatement(pd);
            }
        }

        boolean del_master_update = false;
        final UserParticipant newdel_up[] = new_deleted.getUsers();

        if (newdel_up != null && newdel_up.length > 0) {
            if (!checkForDeletedMasterObject(cdao.getObjectID(), cid, cdao.getContext())) {
                cup.setMBoolean(true);
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
                    collection.closePreparedStatement(pidm);
                }
            }
            PreparedStatement cleanStatement = null;
            PreparedStatement pid = null;
            try {
                cleanStatement = writecon.prepareStatement("DELETE FROM del_dates_members WHERE object_id = ? AND member_uid = ? AND cid = ?");
                pid = writecon.prepareStatement("insert into del_dates_members (object_id, member_uid, pfid, cid, confirm) values (?, ?, ?, ?, ?)");
                for (int a = 0; a < newdel_up.length; a++) {
                    cleanStatement.setInt(1, cdao.getObjectID());
                    pid.setInt(1, cdao.getObjectID());
                    cleanStatement.setInt(2, newdel_up[a].getIdentifier());
                    pid.setInt(2, newdel_up[a].getIdentifier());
                    if (cdao.getGlobalFolderID() == 0) {
                        pid.setInt(3, newdel_up[a].getPersonalFolderId());
                    } else {
                        pid.setNull(3, java.sql.Types.INTEGER);
                    }
                    cleanStatement.setInt(3, cid);
                    pid.setInt(4, cid);
                    if (newdel_up[a].containsConfirm()) {
                        pid.setInt(5, newdel_up[a].getConfirm());
                    } else {
                        pid.setNull(5, java.sql.Types.INTEGER);
                    }
                    cleanStatement.addBatch();
                    pid.addBatch();
                }
                cleanStatement.executeBatch();
                pid.executeBatch();
                del_master_update = true;
            } finally {
                collection.closeStatement(cleanStatement);
                collection.closePreparedStatement(pid);
            }
        }

        final UserParticipant del_up[] = deleted.getUsers();

        if (del_up != null && del_up.length > 0) {
            cup.setMBoolean(true);
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
                collection.closePreparedStatement(pdd);
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
                    collection.closePreparedStatement(ddd);
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
                collection.closePreparedStatement(ddu);
            }
        }

        collection.fillEventInformation(cdao, edao, edao.getUsers(), new_userparticipants, deleted_userparticipants, edao.getParticipants(), new_participants, deleted_participants);

    }

    /**
     * Gathers all identifiers of external participants contained in specified
     * array of {@link Participant} objects whose identifier is different from
     * zero.
     *
     * @param participants
     *            The array of {@link Participant} objects.
     * @return All identifiers of external participants as a {@link Set}.
     */
    private static Set<Integer> createExternalIdentifierSet(final Participant[] participants) {
        final Set<Integer> retval = new HashSet<Integer>(participants.length >> 1);
        for (int i = 0; i < participants.length; i++) {
            if (participants[i].getType() == Participant.EXTERNAL_USER && participants[i].getIdentifier() != 0) {
                retval.add(Integer.valueOf(participants[i].getIdentifier()));
            }
        }
        return retval;
    }

    private static final String SQL_CONFIRM = "UPDATE prg_dates_members SET confirm = ?, reason = ? WHERE object_id = ? AND cid = ? and member_uid = ?";

    private static final String SQL_CONFIRM2 = "UPDATE prg_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?";

    public final Date setUserConfirmation(final int oid, final int uid, final int confirm, final String message, final Session so, final Context ctx) throws OXException {
        Connection writecon = null;
        PreparedStatement pu = null;
        PreparedStatement mo = null;
        final Date changeTimestamp = new Date();
        try {
            writecon = DBPool.pickupWriteable(ctx);
            writecon.setAutoCommit(false);
            pu = writecon.prepareStatement(SQL_CONFIRM);
            pu.setInt(1, confirm);
            if (message == null) {
                pu.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pu.setString(2, message);
            }
            pu.setInt(3, oid);
            pu.setInt(4, so.getContextId());
            pu.setInt(5, uid);
            final int changes = pu.executeUpdate();
            if (changes == 1) {
                mo = writecon.prepareStatement(SQL_CONFIRM2);
                mo.setLong(1, changeTimestamp.getTime());
                mo.setInt(2, uid);
                mo.setInt(3, oid);
                mo.setInt(4, so.getContextId());
                mo.executeUpdate();
            } else if (changes == 0) {
                final OXObjectNotFoundException e = new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND,
                    APPOINTMENT, "Object: " + oid + ", Context: " + so.getContextId() + ", User: " + uid);
                LOG.error(e.getMessage(), e);
                throw e;
            } else {
                LOG.warn(StringCollection.convertArraytoString(new Object[] { "Result of setUserConfirmation was ",
                        Integer.valueOf(changes), ". Check prg_dates_members object_id = ", Integer.valueOf(oid),
                        " cid = ", Integer.valueOf(so.getContextId()), " uid = ", Integer.valueOf(uid) }));
            }
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch (final SQLException sqle) {
            if (writecon != null) {
                try {
                    writecon.rollback();
                } catch (final SQLException rb) {
                    LOG.error("setUserConfirmation (writecon) error while rollback ", rb);
                }
            }
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } finally {
            collection.closePreparedStatement(pu);
            collection.closePreparedStatement(mo);
            if (writecon != null) {
                try {
                    writecon.setAutoCommit(true);
                } catch (final SQLException sqle) {
                    LOG.error("setUserConfirmation (writecon) error while setAutoCommit(true) ", sqle);
                }
                DBPool.closeWriterSilent(ctx, writecon);
            }
        }
        /*
         * Trigger event after changes are committed
         */
        final int fid = collection.resolveFolderIDForUser(oid, uid, ctx);
        if (fid == -1) {
            LOG.warn(StringCollection.convertArraytoString(new Object[] {
                    "Confirmation event could not be triggered: Unable to resolve folder id for user:oid:context",
                    Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(so.getContextId()) }));
            return changeTimestamp;
        }
        final CalendarDataObject cdao;
        try {
            cdao = new CalendarSql(so).getObjectById(oid, fid);
        } catch (final SQLException e) {
            LOG.warn("Confirmation event could not be triggered", new OXCalendarException(
                    OXCalendarException.Code.CALENDAR_SQL_ERROR, e));
            return changeTimestamp;
        }
        cdao.setParentFolderID(fid);
        collection.triggerEvent(so, getConfirmAction(confirm), cdao);
        return changeTimestamp;
    }

    private static final int getConfirmAction(final int confirm) {
        if (CalendarObject.ACCEPT == confirm) {
            return CalendarOperation.CONFIRM_ACCEPTED;
        }
        if (CalendarObject.DECLINE == confirm) {
            return CalendarOperation.CONFIRM_DELINED;
        }
        if (CalendarObject.TENTATIVE == confirm) {
            return CalendarOperation.CONFIRM_TENTATIVELY_ACCEPTED;
        }
        return CalendarObject.NONE;
    }

    public final long attachmentAction(final int oid, final int uid, final Context c, final boolean action) throws OXException {
        Connection readcon = null, writecon = null;
        int changes[];
        PreparedStatement pst = null;
        int number_of_attachments = 0;
        ResultSet rs = null;
        PreparedStatement prep = null;
        long last_modified = 0L;
        try {
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
                LOG.error("Object Not Found: " + "Unable to handle attachment action", new Throwable());
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.EnumComponent.APPOINTMENT, "");
            }
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(prep);
            if (readcon != null) {
                DBPool.closeReaderSilent(c, readcon);
            }
        }

        if (action) {
            number_of_attachments++;
        } else {
            number_of_attachments--;
            if (number_of_attachments < 0) {
                LOG.error(StringCollection.convertArraytoString(new Object[] { "Object seems to be corrupted: attachmentAction:", Boolean.valueOf(action), " oid:cid:uid ", Integer.valueOf(oid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(c.getContextId()), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(uid) }), new Throwable());
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.EnumComponent.APPOINTMENT, "");
            }
        }

        try {
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
                LOG.error(StringCollection.convertArraytoString(new Object[] { "Object not found: attachmentAction: oid:cid:uid ", Integer.valueOf(oid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(c.getContextId()), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(uid) }), new Throwable());
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.EnumComponent.APPOINTMENT, "");
            }
            LOG.warn(StringCollection.convertArraytoString(new Object[] { "Result of attachmentAction was ", Integer.valueOf(changes[0]), ". Check prg_dates oid:cid:uid ", Integer.valueOf(oid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(c.getContextId()), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(uid) }));
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch (final SQLException sqle) {
            if (writecon != null) {
                try {
                    writecon.rollback();
                } catch (final SQLException rb) {
                    LOG.error("attachmentAction (writecon) error while rollback ", rb);
                }
            }
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } finally {
            collection.closePreparedStatement(pst);
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
            readcon = DBPool.pickup(context);
            final PreparedStatement pst = readcon.prepareStatement("SELECT object_id from del_dates_members WHERE object_id = ? AND cid = ?");
            ResultSet rs = null;
            try {
                pst.setInt(1, oid);
                pst.setInt(2, cid);
                rs = getResultSet(pst);
                ret = rs.next();
            } finally {
                collection.closeResultSet(rs);
            }
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (readcon != null) {
                DBPool.push(context, readcon);
            }
        }
        return ret;
    }

    private static final String SQL_CHECK_DEL_MASTER = "SELECT intfield01 FROM del_dates WHERE intfield01 = ? AND cid = ?";

    /**
     * Checks if an entry can be found in backup table for specified identifier
     *
     * @param oid The master's object ID
     * @param cid The master's context ID
     * @param context The context
     * @return <code>true</code> if an entry can be found in backup table for specified identifier; otherwise <code>false</code>
     * @throws OXException If an OX error occurs
     * @throws SQLException If a SQL error occurs
     */
    private final boolean checkForDeletedMasterObject(final int oid, final int cid, final Context context) throws OXException, SQLException {
        Connection readcon = null;
        boolean ret = false;
        try {
            readcon = DBPool.pickup(context);
            final PreparedStatement pst = readcon.prepareStatement(SQL_CHECK_DEL_MASTER);
            ResultSet rs = null;
            try {
                pst.setInt(1, oid);
                pst.setInt(2, cid);
                rs = getResultSet(pst);
                ret = rs.next();
            } finally {
                collection.closeResultSet(rs);
                collection.closePreparedStatement(pst);
            }
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (readcon != null) {
                DBPool.push(context, readcon);
            }
        }
        return ret;
    }

    private static final String SQL_CHECK_DEL_PART = "SELECT object_id FROM del_dates_members WHERE object_id = ? AND cid = ? AND member_uid = ?";

    /**
     * Checks if specified participant is contained in participants' backup table.
     *
     * @param uid The participant's identifier
     * @param cid The context ID
     * @param oid The corresponding appointment's ID
     * @param context The context
     * @return <code>true</code> if specified participant is contained in participants' backup table; otherwise <code>false</code>
     * @throws OXException If an OX error occurs
     * @throws SQLException If a SQL error occurs
     */
    private final boolean checkForDeletedParticipants(final int uid, final int cid, final int oid, final Context context) throws OXException, SQLException {
        Connection readcon = null;
        boolean ret = false;
        try {
            readcon = DBPool.pickup(context);
            final PreparedStatement pst = readcon.prepareStatement(SQL_CHECK_DEL_PART);
            ResultSet rs = null;
            try {
                pst.setInt(1, oid);
                pst.setInt(2, cid);
                pst.setInt(3, uid);
                rs = getResultSet(pst);
                ret = rs.next();
            } finally {
                collection.closeResultSet(rs);
                collection.closePreparedStatement(pst);
            }
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (readcon != null) {
                DBPool.push(context, readcon);
            }
        }
        return ret;
    }

    private final boolean checkIfUserIstheOnlyParticipant(final int cid, final int oid, final Connection readcon) throws SQLException {
        final PreparedStatement pst = readcon.prepareStatement("SELECT object_id from prg_dates_members WHERE object_id = ? AND cid = ?");
        ResultSet rs = null;
        int mc = 0;
        try {
            pst.setInt(1, oid);
            pst.setInt(2, cid);
            rs = getResultSet(pst);
            while (rs.next()) {
                mc++;
                if (mc > 1) {
                    break;
                }
            }
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(pst);
        }
        return (mc == 1);
    }

    private final long deleteOnlyOneParticipantInPrivateFolder(final int oid, final int cid, final int uid,
            final int fid, final Context c, final Connection writecon, final Session so) throws SQLException,
            OXMandatoryFieldException, OXConflictException, OXException {
        final long lastModified = System.currentTimeMillis();
        final PreparedStatement pd = writecon
                .prepareStatement("delete from prg_dates_members WHERE object_id = ? AND cid = ? AND member_uid LIKE ?");
        try {
            pd.setInt(1, oid);
            pd.setInt(2, cid);
            pd.setInt(3, uid);
            pd.addBatch();
            deleteReminder(oid, uid, c);
            // changeReminder(oid, uid, -1, c, false, null, null,
            // CalendarOperation.DELETE, false);
            pd.executeBatch();
        } finally {
            collection.closePreparedStatement(pd);
        }
        boolean master_del_update = true;
        final PreparedStatement pdr = writecon
                .prepareStatement("delete from prg_date_rights WHERE object_id = ? AND cid = ? AND id = ? AND type = ?");
        try {
            pdr.setInt(1, oid);
            pdr.setInt(2, cid);
            pdr.setInt(3, uid);
            pdr.setInt(4, Participant.USER);
            pdr.executeUpdate();
            if (!checkForDeletedMasterObject(oid, cid, c)) {
                final PreparedStatement pidm = writecon
                        .prepareStatement("insert into del_dates (creating_date, created_from, changing_date, changed_from, fid, intfield01, cid, pflag) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                try {
                    pidm.setTimestamp(1, new Timestamp(lastModified));
                    pidm.setInt(2, uid);
                    pidm.setLong(3, lastModified);
                    pidm.setInt(4, 0);
                    pidm.setInt(5, fid);
                    pidm.setInt(6, oid);
                    pidm.setInt(7, cid);
                    pidm.setInt(8, 0);
                    pidm.executeUpdate();
                    master_del_update = false;
                } finally {
                    collection.closePreparedStatement(pidm);
                }
            }
        } finally {
            collection.closePreparedStatement(pdr);
        }

        final PreparedStatement prepid = writecon
                .prepareStatement("DELETE FROM del_dates_members WHERE cid = ? AND object_id = ? AND member_uid = ?");
        try {
            prepid.setInt(1, cid);
            prepid.setInt(2, oid);
            prepid.setInt(3, uid);
            prepid.executeUpdate();
        } finally {
            collection.closePreparedStatement(prepid);
        }

        final PreparedStatement pid = writecon
                .prepareStatement("insert into del_dates_members (object_id, member_uid, pfid, cid, confirm) values (?, ?, ?, ?, ?)");
        try {
            pid.setInt(1, oid);
            pid.setInt(2, uid);
            pid.setInt(3, fid);
            pid.setInt(4, cid);
            pid.setInt(5, 0);
            pid.executeUpdate();
        } finally {
            collection.closePreparedStatement(pid);
        }

        final PreparedStatement ma = writecon
                .prepareStatement("update prg_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?");
        try {
            ma.setLong(1, lastModified);
            ma.setInt(2, uid);
            ma.setInt(3, oid);
            ma.setInt(4, cid);
            ma.executeUpdate();
        } finally {
            collection.closePreparedStatement(ma);
        }

        if (master_del_update) {
            final PreparedStatement ddu = writecon
                    .prepareStatement("update del_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?");
            try {
                ddu.setLong(1, lastModified);
                ddu.setInt(2, uid);
                ddu.setInt(3, oid);
                ddu.setInt(4, cid);
                ddu.executeUpdate();
            } finally {
                collection.closePreparedStatement(ddu);
            }
        }
        final AppointmentObject ao = new AppointmentObject();
        ao.setObjectID(oid);
        ao.setParentFolderID(fid);
        collection.triggerEvent(so, CalendarOperation.UPDATE, ao);
        deleteReminder(oid, uid, c);
        // changeReminder(oid, uid, fid, c, false, null, null,
        // CalendarOperation.DELETE, false);
        return lastModified;
    }

    private static final String SQL_UPDATE_LAST_MODIFIED = "UPDATE prg_dates AS pd SET "
            + collection.getFieldName(AppointmentObject.LAST_MODIFIED) + " = ?, "
            + collection.getFieldName(AppointmentObject.MODIFIED_BY) + " = ? WHERE cid = ? AND "
            + collection.getFieldName(AppointmentObject.OBJECT_ID) + " = ? AND "
            + collection.getFieldName(AppointmentObject.LAST_MODIFIED) + " <= ?";

    private static void updateLastModified(final int oid, final int cid, final int uid, final long lastModified, final Connection writecon)
            throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writecon.prepareStatement(SQL_UPDATE_LAST_MODIFIED);
            int pos = 1;
            stmt.setLong(pos++, lastModified); // LAST_MODIFIED
            stmt.setInt(pos++, uid); // MODIFIED_BY
            stmt.setInt(pos++, cid); // Context
            stmt.setInt(pos++, oid); // OBJECT_ID
            stmt.setLong(pos++, lastModified); // LAST_MODIFIED
            stmt.executeUpdate();
        } finally {
            collection.closePreparedStatement(stmt);
        }
    }

    private static final String SQL_SELECT_WHOLE_RECURRENCE = "SELECT "
			+ collection.getFieldName(AppointmentObject.OBJECT_ID) + " FROM prg_dates WHERE cid = ? AND "
			+ collection.getFieldName(AppointmentObject.RECURRENCE_ID) + " = ? ORDER BY "
			+ collection.getFieldName(AppointmentObject.OBJECT_ID);

	private final void deleteOnlyOneRecurringParticipantInPrivateFolder(final int recurrenceId, final int cid,
			final int uid, final int fid, final Context c, final Connection writecon, final Session so)
			throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
		/*
		 * Get all object IDs belonging to specified recurrence ID
		 */
		final Set<Integer> objectIDs;
		{
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = writecon.prepareStatement(SQL_SELECT_WHOLE_RECURRENCE);
				stmt.setInt(1, cid);
				stmt.setInt(2, recurrenceId);
				rs = stmt.executeQuery();
				objectIDs = new HashSet<Integer>(8);
				while (rs.next()) {
					objectIDs.add(Integer.valueOf(rs.getInt(1)));
				}
			} finally {
				collection.closeResultSet(rs);
				collection.closePreparedStatement(stmt);
			}
		}
		for (final Integer objectId : objectIDs) {
			deleteOnlyOneParticipantInPrivateFolder(objectId.intValue(), cid, uid, fid, c, writecon, so);
		}
	}

    /**
     * Deletes the reminder entry for specified appointment and user/participant
     *
     * @param oid The apointment's object ID
     * @param uid The user's/participant's ID
     * @param c The context
     * @throws OXMandatoryFieldException If deleting reminder fails
     * @throws OXConflictException If deleting reminder fails
     * @throws OXException If deleting reminder fails
     */
    private static final void deleteReminder(final int oid, final int uid, final Context c) throws OXMandatoryFieldException, OXConflictException, OXException {
        changeReminder(oid, uid, -1, c, false, null, null, CalendarOperation.DELETE, false);
    }

    private static final void changeReminder(final int oid, final int uid, final int fid, final Context c, final boolean sequence, final java.util.Date end_date, final java.util.Date reminder_date, final int action, final boolean recurrenceChange) throws OXMandatoryFieldException, OXConflictException, OXException {
        final ReminderSQLInterface rsql = new ReminderHandler(c);
        if (action == CalendarOperation.DELETE || action == CalendarOperation.UPDATE && collection.isInThePast(end_date)) {
        //if (action == CalendarOperation.DELETE) {
            if (rsql.existsReminder(oid, uid, Types.APPOINTMENT)) {
                try {
                    rsql.deleteReminder(oid, uid, Types.APPOINTMENT);
                } catch (final ReminderException exc) {
                    if (ReminderException.Code.NOT_FOUND.getDetailNumber() == exc.getDetailNumber()) {
                        LOG.debug("Reminder was not found for deletion", exc);
                    } else {
                        throw exc;
                    }
                }
            }
        } else {
            if (!collection.isInThePast(end_date)) {
                final ReminderObject ro = new ReminderObject();
                ro.setUser(uid);
                ro.setTargetId(oid);
                ro.setModule(Types.APPOINTMENT);
                ro.setRecurrenceAppointment(sequence);
                ro.setDate(reminder_date);
                ro.setFolder(fid);
                if (rsql.existsReminder(oid, uid, Types.APPOINTMENT)) {
                    if (sequence && !recurrenceChange) {
                        /*
                         * A recurring appointment's reminder update whose
                         * recurrence pattern has not changed; verify that no
                         * already verified reminder appears again through
                         * comparing storage's reminder date with the one that
                         * shall be written to storage. If storage's reminder
                         * date is greater than or equal to specified reminder,
                         * leave unchanged.
                         */
                        if (rsql.loadReminder(oid, uid, Types.APPOINTMENT).getDate().getTime() < reminder_date
                                .getTime()) {
                            rsql.updateReminder(ro);
                        } else if (LOG.isDebugEnabled()) {
                            LOG.debug("No recurrence change! Leave corresponding reminder unchanged");
                        }
                    } else {
                        rsql.updateReminder(ro);
                    }
                } else {
                    rsql.insertReminder(ro);
                }
            }
        }
    }

    /**
     * Checks if specified current calendar data object contains recurrence time
     * and/or type changes compared to storage's calendar data object
     *
     * @param cdao
     *            The current calendar data object
     * @param edao
     *            The storage's calendar data object
     * @return <code>true</code> if specified current calendar data object
     *         contains recurrence time and/or type changes compared to
     *         storage's calendar data object; otherwise <code>false</code>
     */
    private static final boolean checkRecurrenceChange(final CalendarDataObject cdao, final CalendarDataObject edao) {
        /*
         * Recurrence pattern has changed
         */
        if (cdao.getRecurrence() != null && !cdao.getRecurrence().equals(edao.getRecurrence())) {
            return true;
        }
        /*
         * Recurrence start has changed
         */
        if (cdao.containsStartDate() && cdao.getStartDate() != null && !cdao.getStartDate().equals(edao.getStartDate())) {
            return true;
        }
        /*
         * Recurrence end has changed
         */
        if (cdao.containsEndDate() && cdao.getEndDate() != null && !cdao.getEndDate().equals(edao.getEndDate())) {
            return true;
        }
        /*
         * No recurrence time and/or type change
         */
        return false;
    }

    public final void deleteAppointment(final int uid, final CalendarDataObject cdao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException, OXConcurrentModificationException {
        final Connection readcon;
        try {
            readcon = DBPool.pickup(ctx);
        } catch (final DBPoolingException e) {
            throw new OXException(e);
        }
        final CalendarDataObject edao;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            final CalendarOperation co = new CalendarOperation();
            prep = getPreparedStatement(readcon, loadAppointment(cdao.getObjectID(), cdao.getContext()));
            rs = getResultSet(prep);
            edao = co.loadAppointment(rs, cdao.getObjectID(), inFolder, this, readcon, so, ctx, CalendarOperation.DELETE, inFolder);
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch (final OXPermissionException oxpe) {
            throw oxpe;
        } catch (final OXException oxe) {
            throw oxe;
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(prep);
            DBPool.push(ctx, readcon);
        }
        if (clientLastModified == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        } else if (edao.getLastModified() == null) {
            throw new OXCalendarException(OXCalendarException.Code.LAST_MODIFIED_IS_NULL);
        }

        if (edao.getLastModified().getTime() > clientLastModified.getTime()) {
            throw new OXConcurrentModificationException(EnumComponent.APPOINTMENT, OXConcurrentModificationException.ConcurrentModificationCode.CONCURRENT_MODIFICATION);
        }

        deleteSingleAppointment(cdao.getContextID(), cdao.getObjectID(), uid, edao.getCreatedBy(), inFolder, null, writecon, edao.getFolderType(), so, ctx, collection.getRecurringAppointmentDeleteAction(cdao, edao), cdao, edao, clientLastModified);

        if ((cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0)
                || (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null)) {
            final CalendarDataObject mdao;
            /*
             * Check if a change exception has been deleted
             */
            final int empty;
            final boolean isChangeException = (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() != edao.getObjectID());
            if (isChangeException) {
                /*
                 * A change exception; load real appointment
                 */
                mdao = new CalendarSql(so).getObjectById(edao.getRecurrenceID(), inFolder);
                empty = 0;
            } else {
                mdao = edao;
                empty = 1;
            }

            /*
             * Delete of a single appointment: delete exception
             */
            final RecurringResultsInterface rresults = collection.calculateRecurring(mdao, 0, 0, 0);
            if (rresults.size() == empty
                    && (mdao.getChangeException() == null || (isChangeException ? mdao.getChangeException().length == 1
                            : mdao.getChangeException().length == 0))) {
                /*
                 * Commit current transaction
                 */
                if (!writecon.getAutoCommit()) {
                    writecon.commit();
                }
                /*
                 * Delete whole recurring appointment since its last occurrence
                 * has been deleted through previous transaction
                 */
                deleteSingleAppointment(mdao.getContextID(), mdao.getObjectID(), mdao.getCreatedBy(), mdao
                        .getCreatedBy(), inFolder, null, writecon, mdao.getFolderType(), so, ctx,
                        collection.RECURRING_NO_ACTION, mdao, mdao, clientLastModified);
            }
        }
    }

    public void deleteAppointmentsInFolder(final Session so, final Context ctx, final ResultSet rs, final Connection readcon, final Connection writecon, final int foldertype, final int fid) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException {
        while (rs.next()) {
            final int oid = rs.getInt(1);
            final int owner = rs.getInt(2);
            deleteSingleAppointment(so.getContextId(), oid, so.getUserId(), owner, fid, readcon, writecon, foldertype, so, ctx, collection.RECURRING_NO_ACTION, null, null, null);
        }
    }

    /**
     * @param cid context identifier.
     * @param oid appointment identifier.
     * @param uid user that is doing the operation.
     * @param owner user that _created_ the appointment.
     * @param fid folder identifier.
     * @param foldertype any of PRIVATE, PUBLIC or SHARED.
     */
    private final void deleteSingleAppointment(final int cid, int oid, int uid, final int owner, final int fid, Connection readcon, final Connection writecon, final int foldertype, final Session so, final Context ctx, final int recurring_action, final CalendarDataObject cdao, final CalendarDataObject edao, final Date clientLastModified) throws SQLException, OXException {

        if ((foldertype == FolderObject.PRIVATE || foldertype == FolderObject.SHARED) && uid != owner) {
            if (foldertype == FolderObject.SHARED) {
                uid = new OXFolderAccess(ctx).getFolderOwner(fid);
            }
            // in a shared folder some other user tries to delete an appointment
            // created by the sharing user.
            boolean close_read = false;
            try {
                if (readcon == null) {
                    readcon = DBPool.pickup(ctx);
                    close_read = true;
                }
                if (!checkIfUserIstheOnlyParticipant(cid, oid, readcon) && recurring_action != collection.RECURRING_VIRTUAL_ACTION) {
                    if (close_read && readcon != null) {
                        DBPool.push(ctx, readcon);
                        close_read = false;
                    }
                    if (collection.isRecurringMaster(edao == null ? cdao : edao)) {
						// Delete by recurrence ID
						deleteOnlyOneRecurringParticipantInPrivateFolder(edao == null ? cdao.getRecurrenceID() : edao
								.getRecurrenceID(), cid, uid, fid, new ContextImpl(cid), writecon, so);
					} else {
						// Delete by object ID
						final long lastModified = deleteOnlyOneParticipantInPrivateFolder(oid, cid, uid, fid, new ContextImpl(cid), writecon, so);
						// Update last-modified time stamp of master
                        final int recurrenceId = edao == null ? (cdao == null ? -1 : cdao.getRecurrenceID()) : edao
                                .getRecurrenceID();
                        if (recurrenceId > 0) {
                            updateLastModified(recurrenceId, cid, uid, lastModified, writecon);
                        }
					}
                    return;
                }
                if (recurring_action == collection.RECURRING_VIRTUAL_ACTION) {
                    // Create an exception first, remove the user as participant
                    // and then return
                    if (checkIfUserIstheOnlyParticipant(cid, oid, readcon)) {
                        createSingleVirtualDeleteException(cdao, edao, writecon, oid, uid, fid, so, ctx, clientLastModified);
                    } else {

                        edao.setRecurrencePosition(cdao.getRecurrencePosition());
                        edao.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
                        collection.setRecurrencePositionOrDateInDAO(edao);

                        final CalendarDataObject temp = (CalendarDataObject) edao.clone();
                        final RecurringResultsInterface rss = collection.calculateRecurring(temp, 0, 0, edao.getRecurrencePosition());
                        if (rss != null) {
                            final RecurringResultInterface rs = rss.getRecurringResult(0);
                            if (rs != null) {
                                edao.setStartDate(new Date(rs.getStart()));
                                edao.setEndDate(new Date(rs.getEnd()));

                            }
                        }

                        final Date deleted_exceptions[] = edao.getDeleteException();
                        final Date changed_exceptions[] = edao.getChangeException();
                        final Date calculated_exception = edao.getRecurrenceDatePosition();
                        edao.removeDeleteExceptions();
                        edao.removeChangeExceptions();
                        edao.setChangeExceptions(new Date[] { calculated_exception });
                        collection.removeParticipant(edao, uid);
                        collection.removeUserParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        edao.setRecurrenceID(edao.getObjectID());
                        edao.removeObjectID();
                        try {
                            insertAppointment(edao, writecon, so);
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch (final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(8));
                        }
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setDeleteExceptions(collection.removeException(deleted_exceptions, calculated_exception));
                        update.setModifiedBy(uid);
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            update.setChangeExceptions(collection.addException(ldao.getChangeException(), calculated_exception));
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch (final OXException e) {
                            throw e;
                        } catch (final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(9));
                        }
                    }
                    return;
                } else if (recurring_action == collection.RECURRING_EXCEPTION_ACTION) {
                    if (checkIfUserIstheOnlyParticipant(cid, oid, readcon)) {
                        // removal of change exception happens in updateAppointment()
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setModifiedBy(uid);
                        if ((!edao.containsRecurrenceDatePosition() || edao.getRecurrenceDatePosition() == null)) {
                            /*
                             * Determine recurrence date position
                             */
                            edao.setRecurrenceDatePosition(new Date(collection.normalizeLong(edao.getStartDate().getTime())));
                        }
                        //update.setChangeExceptions(collection.removeException(edao.getChangeException(), edao.getRecurrenceDatePosition()));
                        //update.setDeleteExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            update.setChangeExceptions(collection.removeException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            update.setDeleteExceptions(collection.addException(ldao.getDeleteException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch (final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(11));
                        }
                    } else {
                        if (close_read && readcon != null) {
                            DBPool.push(ctx, readcon);
                            close_read = false;
                        }
                        // remove participant (update)
                        collection.removeParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setModifiedBy(uid);
                        if ((!edao.containsRecurrenceDatePosition() || edao.getRecurrenceDatePosition() == null)) {
                            /*
                             * Determine recurrence date position
                             */
                            edao.setRecurrenceDatePosition(new Date(collection.normalizeLong(edao.getStartDate().getTime())));
                        }
                        //update.setChangeExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            update.setChangeExceptions(collection.addException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(edao, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // EXCEPTION
                            // OBJECT
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch (final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(10));
                        }
                    }
                    if (edao.getRecurrenceID() > 0 && !cdao.containsRecurrenceID()) {
                        cdao.setRecurrenceID(edao.getRecurrenceID());
                    }
                    return;
                }
            } catch (final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch (final OXException oxe) {
                throw oxe;
            } catch (final DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } finally {
                if (close_read && readcon != null) {
                    DBPool.push(ctx, readcon);
                }
            }
        }

        if (recurring_action == collection.RECURRING_VIRTUAL_ACTION) {
            // this is an update with a new delete_exception
            if (edao == null) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_UNEXPECTED_DELETE_STATE, Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(-1));
            }
            createSingleVirtualDeleteException(cdao, edao, writecon, oid, uid, fid, so, ctx, clientLastModified);
            return;
        } else if (recurring_action == collection.RECURRING_EXCEPTION_ACTION) {
            // this is a deletion of a change exception aka existing exception
            if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0) {
                // Necessary recurrence ID is present
                boolean close_read = false;
                try {
                    if (readcon == null) {
                        readcon = DBPool.pickup(ctx);
                        close_read = true;
                    }
                    if (((foldertype == FolderObject.PRIVATE || foldertype == FolderObject.SHARED) && uid == owner) || checkIfUserIstheOnlyParticipant(cid, oid, readcon)) {
                        // removal of change exception happens in updateAppointment()
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setModifiedBy(uid);
                        if ((!edao.containsRecurrenceDatePosition() || edao.getRecurrenceDatePosition() == null)) {
                            /*
                             * Determine recurrence date position
                             */
                            edao.setRecurrenceDatePosition(edao.getChangeException()[0]);
                        }
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            update.setChangeExceptions(collection.removeException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            update.setDeleteExceptions(collection.addException(ldao.getDeleteException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJfinal ECT
                        } catch (final OXObjectNotFoundException onfe) {
                            LOG.info("Unable to find master during Exception delete. Ignoring. Seems to be corrupt data.", onfe);
                            final long modified = deleteAppointment(writecon, cid, oid, uid);

                            if (edao == null) {
                                triggerDeleteEvent(oid, fid, so, ctx, null);
                            } else {
                                edao.setModifiedBy(uid);
                                edao.setLastModified(new Date(modified));
                                triggerDeleteEvent(oid, fid, so, ctx, edao);
                            }
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch (final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(11));
                        }
                    } else {
                        if (close_read && readcon != null) {
                            DBPool.push(ctx, readcon);
                            close_read = false;
                        }
                        // remove participant (update)
                        collection.removeParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setModifiedBy(uid);
                        if ((!edao.containsRecurrenceDatePosition() || edao.getRecurrenceDatePosition() == null)) {
                            /*
                             * Determine recurrence date position
                             */
                            edao.setRecurrenceDatePosition(new Date(collection.normalizeLong(edao.getStartDate().getTime())));
                        }
                        //update.setChangeExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid);
                            update.setChangeExceptions(collection.addException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(edao, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // EXCEPTION
                            // OBJECT
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final LdapException le) {
                            throw new OXException(le);
                        } catch (final Exception e) {
                            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(10));
                        }
                    }
                    if (edao.getRecurrenceID() > 0 && !cdao.containsRecurrenceID()) {
                        cdao.setRecurrenceID(edao.getRecurrenceID());
                    }
                    return;
                } catch (final SQLException sqle) {
                    throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
                } catch (final OXException oxe) {
                    throw oxe;
                } catch (final DBPoolingException dbpe) {
                    throw new OXException(dbpe);
                } finally {
                    if (close_read && readcon != null) {
                        DBPool.push(ctx, readcon);
                    }
                }
            }
        } else if (recurring_action == collection.RECURRING_FULL_DELETE) {
            final List<Integer> al = getExceptionList(readcon, ctx, edao.getRecurrenceID());
            if (al != null && !al.isEmpty()) {
                final Integer oids[] = al.toArray(new Integer[al.size()]);
                if (oids.length > 0) {
                    deleteAllRecurringExceptions(oids, so, writecon);
                }
                for (int a = 0; a < al.size(); a++) {
                    triggerDeleteEvent(al.get(a).intValue(), fid, so, ctx, null);
                }
            }
            oid = edao.getRecurrenceID();
        }

        if (edao != null && edao.getRecurrenceID() > 0 && !cdao.containsRecurrenceID()) {
            cdao.setRecurrenceID(edao.getRecurrenceID());
        }

        /*
         * Backup appointment's data and delete from working tables
         */
        final long modified = deleteAppointment(writecon, cid, oid, uid);

        if (edao == null) {
            triggerDeleteEvent(oid, fid, so, ctx, null);
        } else {
            edao.setModifiedBy(uid);
            edao.setLastModified(new Date(modified));
            triggerDeleteEvent(oid, fid, so, ctx, edao);
        }
    }

    private final void triggerDeleteEvent(final int oid, final int fid, final Session so, final Context ctx, final CalendarDataObject edao) throws OXException {
        final CalendarDataObject ao;
        if (edao == null) {
            ao = new CalendarDataObject();
        } else {
            ao = (CalendarDataObject) edao.clone();
        }
        ao.setObjectID(oid);
        ao.setParentFolderID(fid);
        collection.triggerEvent(so, CalendarOperation.DELETE, ao);
        // deleteAllReminderEntries(edao, oid, fid, so, readcon);
        final ReminderSQLInterface rsql = new ReminderHandler(ctx);
        try {
            rsql.deleteReminder(oid, Types.APPOINTMENT);
        } catch (final AbstractOXException oxe) {
            // this is wanted if Code = Code.NOT_FOUND
            if (oxe.getDetailNumber() != Code.NOT_FOUND.getDetailNumber()) {
                throw new OXException(oxe);
            }
        }
    }

    private final void createSingleVirtualDeleteException(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final int oid, final int uid, final int fid, final Session so, final Context ctx, final java.util.Date clientLastModified) throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
        final CalendarDataObject udao = new CalendarDataObject();
        udao.setObjectID(oid);
        udao.setContext(ctx);
        udao.setModifiedBy(uid);
        java.util.Date de = null;
        if (cdao.containsRecurrenceDatePosition()) {
            de = cdao.getRecurrenceDatePosition();
        } else {
            final long del = collection.getLongByPosition(edao, cdao.getRecurrencePosition());
            if (del != 0) {
                de = new java.util.Date(del);
            }
        }
        //udao.setDeleteExceptions(new java.util.Date[] {de});
        if (de == null) {
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_UNEXPECTED_DELETE_STATE, Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(cdao.getRecurrencePosition()));
        }
        try {
            final CalendarDataObject ldao = loadObjectForUpdate(udao, so, ctx, fid);
            udao.setDeleteExceptions(collection.addException(ldao.getDeleteException(), de));
            updateAppointment(udao, ldao, writecon, so, ctx, fid, clientLastModified, false, true);
        } catch (final OXException oxe) {
            throw oxe;
        } catch (final LdapException lde) {
            throw new OXException(lde);
        }
    }

    private static final String SQL_DEL_DATES = "DELETE FROM del_dates WHERE cid = ? AND intfield01 = ?";

    private static final String SQL_DEL_DATES_MEMBERS = "DELETE FROM del_dates_members WHERE cid = ? AND object_id = ?";

    private static final String SQL_BACKUP_MEMBERS = "INSERT INTO del_dates_members SELECT * FROM prg_dates_members WHERE cid = ? AND object_id = ?";

    private static final String SQL_BACKUP_RIGHTS = "INSERT INTO del_date_rights SELECT * FROM prg_date_rights WHERE cid = ? AND object_id = ?";

    private static final String SQL_BACKUP_DATES = "INSERT INTO del_dates SELECT * FROM prg_dates WHERE cid = ? AND intfield01 = ?";

    private static final String SQL_DEL_WORKING_DATES = "DELETE FROM prg_dates WHERE cid = ? AND intfield01 = ?";

    private static final String SQL_DEL_WORKING_MEMBERS = "DELETE FROM prg_dates_members WHERE cid = ? AND object_id = ?";

    private static final String SQL_DEL_WORKING_RIGHTS = "DELETE FROM prg_date_rights WHERE cid = ? AND object_id = ?";

    private static final String SQL_UPDATE_DEL_DATES = "UPDATE del_dates SET changing_date = ?, changed_from = ? WHERE cid = ? AND intfield01 = ?";

    /**
     * Backups appointment data identified through specified <code>oid</code>
     * and <code>cid</code> arguments and removes from working tables.
     * 
     * @param writecon A connection with write capability
     * @param cid The context ID
     * @param oid The object ID
     * @param uid The user ID in whose name this operation takes place
     * @return The last-modified timestamp
     * @throws SQLException If a SQL error occurs
     */
    private static final long deleteAppointment(final Connection writecon, final int cid, final int oid, final int uid)
            throws SQLException {
        return deleteAppointment(writecon, cid, oid, uid, true);
    }

    /**
     * Optionally backups appointment data identified through specified
     * <code>oid</code> and <code>cid</code> arguments and removes from working
     * tables.
     * 
     * @param writecon A connection with write capability
     * @param cid The context ID
     * @param oid The object ID
     * @param uid The user ID in whose name this operation takes place
     * @param backup <code>true</code> to perform backup operations for the
     *            appointment to delete; otherwise <code>false</code>
     * @return The last-modified timestamp
     * @throws SQLException If a SQL error occurs
     */
    private static final long deleteAppointment(final Connection writecon, final int cid, final int oid, final int uid,
            final boolean backup) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writecon.prepareStatement(SQL_DEL_DATES);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_DEL_DATES_MEMBERS);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement("DELETE FROM del_date_rights WHERE cid = ? AND object_id = ?");
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            if (backup) {
                stmt = writecon.prepareStatement(SQL_BACKUP_MEMBERS);
                pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, oid);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;

                stmt = writecon.prepareStatement(SQL_BACKUP_RIGHTS);
                pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, oid);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;

                stmt = writecon.prepareStatement(SQL_BACKUP_DATES);
                pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, oid);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
            }

            stmt = writecon.prepareStatement(SQL_DEL_WORKING_DATES);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_DEL_WORKING_MEMBERS);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_DEL_WORKING_RIGHTS);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            final long modified = System.currentTimeMillis();
            if (backup) {
                stmt = writecon.prepareStatement(SQL_UPDATE_DEL_DATES);
                pos = 1;
                stmt.setLong(pos++, modified);
                stmt.setInt(pos++, uid);
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, oid);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
            }
            return modified;
        } finally {
            collection.closePreparedStatement(stmt);
        }
    }

    /**
     * Backups appointment data identified through specified
     * <code>oid</code> and <code>cid</code> arguments.
     * 
     * @param writecon A connection with write capability
     * @param cid The context ID
     * @param oid The object ID
     * @param uid The user ID in whose name this operation takes place
     * @return The last-modified timestamp
     * @throws SQLException If a SQL error occurs
     */
    private static final long backupAppointment(final Connection writecon, final int cid, final int oid, final int uid) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writecon.prepareStatement(SQL_DEL_DATES);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_DEL_DATES_MEMBERS);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement("DELETE FROM del_date_rights WHERE cid = ? AND object_id = ?");
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_BACKUP_MEMBERS);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_BACKUP_RIGHTS);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_BACKUP_DATES);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            final long modified = System.currentTimeMillis();
            stmt = writecon.prepareStatement(SQL_UPDATE_DEL_DATES);
            pos = 1;
            stmt.setLong(pos++, modified);
            stmt.setInt(pos++, uid);
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
            return modified;
        } finally {
            collection.closePreparedStatement(stmt);
        }
    }

    private static final String SQL_GET_EXC_LIST = "SELECT intfield01 FROM prg_dates pd"
            + " WHERE intfield02 = ? AND cid = ? AND intfield01 != intfield02 AND intfield05 > 0";

    private final List<Integer> getExceptionList(final Connection readcon, final Context c, final int rec_id)
            throws OXException {
        Connection rcon = readcon;
        boolean close_read = false;
        final List<Integer> al;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if (rcon == null) {
                rcon = DBPool.pickup(c);
                close_read = true;
            }
            al = new ArrayList<Integer>(8);
            prep = getPreparedStatement(rcon, SQL_GET_EXC_LIST);
            int pos = 1;
            prep.setInt(pos++, rec_id);
            prep.setInt(pos++, c.getContextId());
            rs = getResultSet(prep);
            while (rs.next()) {
                al.add(Integer.valueOf(rs.getInt(1)));
            }
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(prep);
            if (close_read && rcon != null) {
                DBPool.push(c, rcon);
            }
        }
        return al;
    }

    /**
     * Gets the IDs of those change exceptions which ought to be deleted
     *
     * @param readcon A connection with read capability
     * @param c The context
     * @param rec_id The recurrence ID to which the change exceptions are linked
     * @param sqlin The SQL-IN string containing the recurrence positions
     * @param dates An empty list serving as a container for the queried change exceptions' dates
     * @return The IDs of those change exceptions which ought to be deleted
     * @throws OXException If IDs cannot be determined
     */
    private final List<Integer> getDeletedExceptionList(final Connection readcon, final Context c, final int rec_id, final String sqlin, final List<Long> dates) throws OXException {
        Connection rcon = readcon;
        boolean close_read = false;
        ArrayList<Integer> al = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if (rcon == null) {
                rcon = DBPool.pickup(c);
                close_read = true;
            }
            al = new ArrayList<Integer>(8);
            final StringBuilder query = new StringBuilder(128);
            query.append("select intfield01, field08 FROM prg_dates pd WHERE intfield02 = ");
            query.append(rec_id);
            query.append(" AND cid = ");
            query.append(c.getContextId());
            query.append(" AND intfield01 != intfield02 AND intfield05 IN ");
            query.append(sqlin);
            prep = getPreparedStatement(rcon, query.toString());
            rs = getResultSet(prep);
            final List<Long> longs = new ArrayList<Long>();
            while (rs.next()) {
                al.add(Integer.valueOf(rs.getInt(1)));
                longs.add(Long.valueOf(rs.getString(2)));
            }
            dates.addAll(longs);
        } catch (final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            collection.closeResultSet(rs);
            collection.closePreparedStatement(prep);
            if (close_read && rcon != null) {
                DBPool.push(c, rcon);
            }
        }
        return al;
    }

    /**
     * Deletes those change exceptions from working tables (prg_date_rights,
     * prg_dates_members, and prg_dates) whose IDs appear in specified
     * <code>oids</code>.
     * 
     * @param oids The {@link Integer} array containing the IDs of the change
     *            exceptions
     * @param so The session providing needed user data
     * @param writecon A connection with write capability
     * @throws SQLException If a SQL error occurs
     */
    private static final void deleteAllRecurringExceptions(final Integer[] oids, final Session so,
            final Connection writecon) throws SQLException {
        deleteAllRecurringExceptions(oids, so, writecon, true);
    }

    /**
     * Deletes those change exceptions from working tables (prg_date_rights,
     * prg_dates_members, and prg_dates) whose IDs appear in specified
     * <code>oids</code>.
     * 
     * @param oids The {@link Integer} array containing the IDs of the change
     *            exceptions
     * @param so The session providing needed user data
     * @param writecon A connection with write capability
     * @param backup <code>true</code> to perform backup operations; otherwise
     *            <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static final void deleteAllRecurringExceptions(final Integer[] oids, final Session so,
            final Connection writecon, final boolean backup) throws SQLException {
        for (final Integer oid : oids) {
            deleteAppointment(writecon, so.getContextId(), oid.intValue(), so.getUserId(), backup);
        }
    }

//    private final void deleteAllReminderEntries(CalendarDataObject edao, final int oid, final int inFolder, final Session so, final Context ctx, Connection readcon) throws SQLException, OXMandatoryFieldException, OXConflictException, OXException {
//        UserParticipant up[] = null;
//        boolean close_read = false;
//        if (edao == null) {
//            PreparedStatement prep = null;
//            ResultSet rs = null;
//            try {
//                if (readcon == null) {
//                    readcon = DBPool.pickup(ctx);
//                    close_read = true;
//                }
//                final CalendarOperation co = new CalendarOperation();
//                prep = getPreparedStatement(readcon, loadAppointment(oid, ctx));
//                rs = getResultSet(prep);
//                edao = co.loadAppointment(rs, oid, inFolder, this, readcon, so, ctx, CalendarOperation.DELETE, inFolder, false); // No
//                // permission
//                // checks
//                // at
//                // all!
//            } catch (final SQLException sqle) {
//                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
//            } catch (final OXPermissionException oxpe) {
//                throw oxpe;
//            } catch (final OXException oxe) {
//                throw oxe;
//            } catch (final DBPoolingException dbpe) {
//                throw new OXException(dbpe);
//            } finally {
//                collection.closeResultSet(rs);
//                collection.closePreparedStatement(prep);
//                if (close_read && readcon != null) {
//                    try {
//                        DBPool.push(ctx, readcon);
//                    } catch (final DBPoolingException dbpe) {
//                        LOG.error("DBPoolingException:deleteAppointment (push)", dbpe);
//                    }
//                }
//            }
//        }
//        up = edao.getUsers();
//        for (int a = 0; a < up.length; a++) {
//            final int uid = up[a].getIdentifier();
//            int fid = 0;
//            if (up[a].getPersonalFolderId() > 0) {
//                fid = up[a].getPersonalFolderId();
//            } else {
//                fid = edao.getEffectiveFolderId();
//            }
//            if (uid > 0 && fid > 0) {
//                changeReminder(oid, uid, fid, ctx, edao.isSequence(), null, null, CalendarOperation.DELETE);
//            } else {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug(StringCollection.convertArraytoString(new Object[] { "Reminder object will neither be checked nor deleted -> oid:uid:fid ", oid, ":", uid, ":", fid }));
//                }
//            }
//        }
//    }


}
