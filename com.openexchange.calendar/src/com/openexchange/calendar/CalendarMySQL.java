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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.forSQLCommand;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.ReminderService;
import com.openexchange.caching.CacheKey;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.cache.Attribute;
import com.openexchange.calendar.cache.CalendarVolatileCache;
import com.openexchange.calendar.cache.CalendarVolatileCache.CacheType;
import com.openexchange.calendar.storage.ParticipantStorage;
import com.openexchange.calendar.storage.SQL;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCallbacks;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarConfig;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.MBoolean;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.ExternalGroupParticipant;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderChildObject;
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
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Autoboxing;
import com.openexchange.log.LogFactory;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.Resource;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.COUNT;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.ISNULL;
import com.openexchange.sql.grammar.OR;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CalendarMySQL} - The MySQL implementation of {@link CalendarSqlImp}.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class CalendarMySQL implements CalendarSqlImp {

    private static final String SELECT_ALL_PRIVATE_FOLDERS_IN_WHICH_A_USER_IS_A_PARTICIPANT = "SELECT object_id, pfid, member_uid FROM prg_dates_members WHERE member_uid = ? and cid = ?";

    private static final String PDM_AND_PD_FID = " AND pd.fid = ";

    private static final String select = "SELECT intfield01, timestampfield01, timestampfield02, field01 FROM prg_dates ";

    private static final String FREE_BUSY_SELECT = "SELECT intfield01, timestampfield01, timestampfield02, intfield07, intfield06, field01, fid, pflag, created_from, intfield02, intfield04, field06, field07, field08, timezone, intfield05, intfield03, field09 FROM prg_dates ";

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

    private static final String PDM_OR = " OR ";

    private static final String PDM_ORDER_BY = " ORDER BY ";

    private static final String PDM_GROUP_BY_PD_INTFIELD01 = " GROUP BY pd.intfield01 ";

    private static final String PDM_GROUP_BY_INTFIELD01 = " GROUP BY intfield01";

    private static final String PD_FID_IS_NULL = " AND pd.fid = 0 ";

    private static final String PD_CREATED_FROM_IS = " AND pd.created_from = ";

    private static final String DATES_IDENTIFIER_IS = " AND intfield01 = ";

    private static final String PARTICIPANTS_IDENTIFIER_IS = " AND object_id = ";

    private static final String PARTICIPANTS_IDENTIFIER_IN = " AND object_id IN ";

    private static final String UNION = " UNION ";

    private static final String CAL_TABLE_NAME = "prg_dates";

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CalendarMySQL.class));

    private static final CalendarCollection COLLECTION = new CalendarCollection();

    private static interface StatementFiller {
        void fillStatement(PreparedStatement stmt, int pos, CalendarDataObject cdao) throws OXException, SQLException;
    }

    private static final AtomicReference<AppointmentSqlFactoryService> FACTORY_REF = new AtomicReference<AppointmentSqlFactoryService>();

    private static final AtomicReference<ServiceLookup> SERVICES_REF = new AtomicReference<ServiceLookup>();

    public static void setApppointmentSqlFactory(final AppointmentSqlFactoryService factory) {
        CalendarMySQL.FACTORY_REF.set(factory);
    }

    private static final Map<Integer, StatementFiller> STATEMENT_FILLERS;

    static {
        STATEMENT_FILLERS = new HashMap<Integer, StatementFiller>();
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.TITLE), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setString(pos, cdao.getTitle());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.START_DATE), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setTimestamp(pos, new java.sql.Timestamp(cdao.getStartDate().getTime()));
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.END_DATE), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setTimestamp(pos, new java.sql.Timestamp(cdao.getEndDate().getTime()));
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(Appointment.SHOWN_AS), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, cdao.getShownAs());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(Appointment.LOCATION), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                if (cdao.getLocation() == null) {
                    stmt.setNull(pos, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos, cdao.getLocation());
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.NOTE), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                if (cdao.getNote() == null) {
                    stmt.setNull(pos, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos, cdao.getNote());
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CommonObject.CATEGORIES), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                if (cdao.getCategories() == null) {
                    stmt.setNull(pos, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos, cdao.getCategories());
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(Appointment.FULL_TIME), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, I(cdao.getFullTime()));
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CommonObject.COLOR_LABEL), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, cdao.getLabel());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(DataObject.MODIFIED_BY), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                if (cdao.containsModifiedBy()) {
                    stmt.setInt(pos, cdao.getModifiedBy());
                } else {
                    throw OXCalendarExceptionCodes.MODIFIED_BY_MISSING.create();
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(DataObject.LAST_MODIFIED), new StatementFiller() {
            @Override
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
        STATEMENT_FILLERS.put(Integer.valueOf(CommonObject.PRIVATE_FLAG), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao) throws SQLException {
                stmt.setInt(pos, I(cdao.getPrivateFlag()));
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(FolderChildObject.FOLDER_ID), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                if (cdao.getFolderType() == FolderObject.PRIVATE || cdao.getFolderType() == FolderObject.SHARED) {
                    stmt.setInt(pos, 0);
                } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                    stmt.setInt(pos, cdao.getGlobalFolderID());
                } else {
                    throw OXCalendarExceptionCodes.NOT_YET_SUPPORTED.create();
                }
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.RECURRENCE_TYPE), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setString(pos, cdao.getRecurrence());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.RECURRENCE_ID), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setInt(pos, cdao.getRecurrenceID());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.DELETE_EXCEPTIONS), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setString(pos, cdao.getDelExceptions());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.CHANGE_EXCEPTIONS), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setString(pos, cdao.getExceptions());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.RECURRENCE_CALCULATOR), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setInt(pos, cdao.getRecurrenceCalculator());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.RECURRENCE_POSITION), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setInt(pos, cdao.getRecurrencePosition());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CommonObject.NUMBER_OF_ATTACHMENTS), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setInt(pos, cdao.getNumberOfAttachments());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(Appointment.TIMEZONE), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setString(pos, cdao.getTimezoneFallbackUTC());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.ORGANIZER), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setString(pos, cdao.getOrganizer());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.SEQUENCE), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setInt(pos, cdao.getSequence());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.ORGANIZER_ID), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setInt(pos, cdao.getOrganizerId());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.PRINCIPAL), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setString(pos, cdao.getPrincipal());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CalendarObject.PRINCIPAL_ID), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setInt(pos, cdao.getPrincipalId());
            }
        });
        STATEMENT_FILLERS.put(Integer.valueOf(CommonObject.FILENAME), new StatementFiller() {
            @Override
            public void fillStatement(final PreparedStatement stmt, final int pos, final CalendarDataObject cdao)
                    throws OXException, SQLException {
                stmt.setString(pos, cdao.getFilename());
            }
        });
    }

    public CalendarMySQL() {
        super();
    }

    @Override
    public PreparedStatement getAllAppointments(final Context c, final Date d1, final Date d2, final String select, final Connection readcon, final int orderBy, final Order orderDir) throws OXException, SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
        sb.append(parseSelect(select));
        sb.append(" pd JOIN prg_dates_members pdm ON pd.intfield01 = pdm.object_id AND pd.cid = pdm.cid AND pd.cid = ");
        sb.append(c.getContextId());

        sb.append(WHERE);
        getRange(sb);

        if (COLLECTION.getFieldName(orderBy) == null || Order.NO_ORDER.equals(orderDir)) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(COLLECTION.getFieldName(orderBy));
            sb.append(' ');
            sb.append(forSQLCommand(orderDir));
        }

        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        int a = 1;

        pst.setTimestamp(a++, new Timestamp(d2.getTime()));
        pst.setTimestamp(a++, new Timestamp(d1.getTime()));

        return pst;
    }

    @Override
    public final PreparedStatement getAllAppointmentsForUser(final Context c, final int uid, final int groups[], final UserConfiguration uc, final java.util.Date d1, final java.util.Date d2, final String select, final Connection readcon, final java.util.Date since, final int orderBy, final Order orderDir) throws OXException, SQLException {
        return getAllAppointmentsForUser(c, uid, groups, uc, d1, d2, select, readcon, since, orderBy, orderDir, false);
    }

    public final PreparedStatement getAllAppointmentsForUser(final Context c, final int uid, final int groups[], final UserConfiguration uc, final java.util.Date d1, final java.util.Date d2, final String select, final Connection readcon, final java.util.Date since, final int orderBy, final Order orderDir, final boolean showPrivates) throws OXException, SQLException {
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

        COLLECTION.getVisibleFolderSQLInString(sb, uid, groups, c, uc, readcon);

        if (COLLECTION.getFieldName(orderBy) == null || Order.NO_ORDER.equals(orderDir)) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(COLLECTION.getFieldName(orderBy));
            sb.append(' ');
            sb.append(forSQLCommand(orderDir));
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

    @Override
    public final PreparedStatement getConflicts(final Context c, final java.util.Date d1, final java.util.Date d2, final java.util.Date d3, final java.util.Date d4, final Connection readcon, final String member_sql_in, final boolean free_busy_select) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
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
        sb.append(Appointment.FREE);
        if (CalendarConfig.getUndefinedStatusConflict()) {
            sb.append(" AND pdm.confirm != ");
            sb.append(CalendarObject.DECLINE);
        } else {
            sb.append(" AND pdm.confirm IN (");
            sb.append(CalendarObject.DECLINE).append(", ").append(CalendarObject.NONE).append(')');
        }

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
            sb.append(Appointment.FREE);
            if (CalendarConfig.getUndefinedStatusConflict()) {
                sb.append(" AND pdm.confirm != ");
                sb.append(CalendarObject.DECLINE);
            } else {
                sb.append(" AND pdm.confirm IN (");
                sb.append(CalendarObject.DECLINE).append(", ").append(CalendarObject.NONE).append(')');
            }
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

    @Override
    public final SearchIterator<List<Integer>> getAllPrivateAppointmentAndFolderIdsForUser(final Context c, final int id, final Connection readcon) throws SQLException {
        try {
            final CalendarVolatileCache cache = CalendarVolatileCache.getInstance();
            final CacheKey key = cache.newCacheKey(CacheType.getAllPrivateAppointmentAndFolderIdsForUser, id);
            final String gid = String.valueOf(c.getContextId());
            List<List<Integer>> list = cache.getFromGroup(key, gid);
            if (null == list) {
                synchronized (gid.intern()) {
                    list = cache.getFromGroup(key, gid);
                    if (null == list) {
                        list = getAllPrivateAppointmentAndFolderIdsForUser0(c, id, readcon);
                        cache.putInGroup(key, gid, list, Attribute.getIdleTimeSecondsAttribute(3));
                    }
                }
            }
            return new SearchIteratorAdapter<List<Integer>>(list.iterator(), list.size());
        } catch (final Exception e) {
            final List<List<Integer>> list = getAllPrivateAppointmentAndFolderIdsForUser0(c, id, readcon);
            return new SearchIteratorAdapter<List<Integer>>(list.iterator(), list.size());
        }
    }

    private final List<List<Integer>> getAllPrivateAppointmentAndFolderIdsForUser0(final Context c, final int id, final Connection readcon) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = readcon.prepareStatement(SELECT_ALL_PRIVATE_FOLDERS_IN_WHICH_A_USER_IS_A_PARTICIPANT);
            stmt.setInt(1, id);
            stmt.setInt(2, c.getContextId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<List<Integer>> list = new LinkedList<List<Integer>>();
            int object_id = 0;
            int pfid = 0;
            int uid = 0;
            do {
                object_id = rs.getInt(1);
                pfid = rs.getInt(2);
                uid = rs.getInt(3);
                if (!rs.wasNull()) {
                    final List<Integer> ints = new ArrayList<Integer>(3);
                    ints.add(Integer.valueOf(object_id));
                    ints.add(Integer.valueOf(pfid));
                    ints.add(Integer.valueOf(uid));
                    list.add(ints);
                }
            } while (rs.next());
            return list;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public PreparedStatement getSharedAppointmentFolderQuery(final Context c, final CalendarFolderObject cfo, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator("SELECT object_id, pfid, member_uid FROM prg_dates_members WHERE cid = ? AND pfid IN (");
        for(final Object o : cfo.getSharedFolderList()) {
            sb.append(o).append(',');
        }
        sb.setCharAt(sb.length()-1, ')');
        final PreparedStatement stmt = readcon.prepareStatement(sb.toString());
        stmt.setInt(1, c.getContextId());
        return stmt;

    }

    @Override
    public final SearchIterator<List<Integer>> getResourceConflictsPrivateFolderInformation(final Context c, final java.util.Date d1, final java.util.Date d2, final java.util.Date d3, final java.util.Date d4, final Connection readcon, final String resource_sql_in) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(184);
            sb.append("SELECT pdm.object_id, pdm.pfid, pdm.member_uid FROM prg_dates");
            sb.append(JOIN_PARTICIPANTS);
            sb.append(c.getContextId());
            sb.append(" AND pdr.cid = ");
            sb.append(c.getContextId());
            sb.append(" JOIN prg_dates_members pdm ON pd.intfield01 = pdm.object_id AND pdm.cid = ");
            sb.append(c.getContextId());
            sb.append(WHERE);
            getConflictRange(sb);
            sb.append(" AND pdr.id IN ");
            sb.append(resource_sql_in);
            sb.append(" AND pdr.type = ");
            sb.append(Participant.RESOURCE);
            sb.append(" AND pd.intfield06 != ");
            sb.append(Appointment.FREE);
            sb.append(UNION);
            sb.append("SELECT pdm.object_id, pdm.pfid, pdm.member_uid FROM prg_dates");
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
            sb.append(Appointment.FREE);
            pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setTimestamp(1, new Timestamp(d2.getTime()));
            pst.setTimestamp(2, new Timestamp(d1.getTime()));
            pst.setTimestamp(3, new Timestamp(d4.getTime()));
            pst.setTimestamp(4, new Timestamp(d3.getTime()));
            rs = pst.executeQuery();
            if (!rs.next()) {
                return SearchIteratorAdapter.emptyIterator();
            }
            final List<List<Integer>> list = new LinkedList<List<Integer>>();
            int object_id = 0;
            int pfid = 0;
            int uid = 0;
            do {
                object_id = rs.getInt(1);
                pfid = rs.getInt(2);
                uid = rs.getInt(3);
                if (!rs.wasNull()) {
                    final List<Integer> ints = new ArrayList<Integer>(3);
                    ints.add(Integer.valueOf(object_id));
                    ints.add(Integer.valueOf(pfid));
                    ints.add(Integer.valueOf(uid));
                    list.add(ints);
                }
            } while (rs.next());
            return new SearchIteratorAdapter<List<Integer>>(list.iterator(), list.size());
        } finally {
            DBUtils.closeSQLStuff(rs, pst);
        }
    }

    @Override
    public final PreparedStatement getResourceConflicts(final Context c, final java.util.Date d1, final java.util.Date d2, final java.util.Date d3, final java.util.Date d4, final Connection readcon, final String resource_sql_in) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(184);
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
        sb.append(Appointment.FREE);

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
        sb.append(Appointment.FREE);

        sb.append(PDM_GROUP_BY_INTFIELD01);
        sb.append(ORDER_BY_TS1);
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        pst.setTimestamp(3, new Timestamp(d4.getTime()));
        pst.setTimestamp(4, new Timestamp(d3.getTime()));
        return pst;
    }

    @Override
    public final PreparedStatement getFreeBusy(final int uid, final Context c, final java.util.Date d1, final java.util.Date d2, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
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

    @Override
    public final PreparedStatement getResourceFreeBusy(final int uid, final Context c, final java.util.Date d1, final java.util.Date d2, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
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

    @Override
    public PreparedStatement getActiveAppointments(final Context c, final int uid, final java.util.Date d1, final java.util.Date d2, final String select, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
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

    @Override
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

        COLLECTION.getVisibleFolderSQLInString(sb, uid, groups, c, uc, readcon);

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
                    COLLECTION.fillDAO(cdao);
                    cdao.setDelExceptions(rs.getString(7));
                    cdao.setExceptions(rs.getString(8));
                    cdao.setTimezone(rs.getString(9));
                    cdao.setFullTime(rs.getInt(10) > 0);
                    try {
                        if (COLLECTION.fillDAO(cdao)) {
                            final RecurringResultsInterface rrs = COLLECTION.calculateRecurring(cdao, start, end, 0);
                            final TimeZone zone = Tools.getTimeZone(cdao.getTimezoneFallbackUTC());
                            for (int a = 0; a < rrs.size(); a++) {
                                final RecurringResultInterface rr = rrs.getRecurringResult(a);
                                fillActiveDates(start, rr.getStart(), rr.getEnd(), activeDates, COLLECTION.exceedsHourOfDay(rr.getStart(), zone));
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
                    fillActiveDates(start, s.getTime(), e.getTime(), activeDates, COLLECTION.exceedsHourOfDay(s.getTime(), Tools.getTimeZone(rs.getString(9))));
                }
            }
            // collection.debugActiveDates (start, end,
            // activeDates); // TODO: Make configurable or uncomment in runtime
            // edition
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closePreparedStatement(pst);
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

    @Override
    public final PreparedStatement getPublicFolderRangeSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date d1, final java.util.Date d2, final String select, final boolean readall, final Connection readcon, final int orderBy, final Order orderDir) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(32);
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
        if (COLLECTION.getFieldName(orderBy) == null || Order.NO_ORDER.equals(orderDir)) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(COLLECTION.getFieldName(orderBy));
            sb.append(' ');
            sb.append(forSQLCommand(orderDir));
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }

    @Override
    public final PreparedStatement getPrivateFolderRangeSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date d1, final java.util.Date d2, final String select, final boolean readall, final Connection readcon, final int orderBy, final Order orderDir) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
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
        if (COLLECTION.getFieldName(orderBy) == null || Order.NO_ORDER.equals(orderDir)) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(COLLECTION.getFieldName(orderBy));
            sb.append(' ');
            sb.append(forSQLCommand(orderDir));
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }

    @Override
    public final PreparedStatement getSharedFolderRangeSQL(final Context c, final int uid, final int shared_folder_owner, final int groups[], final int fid, final java.util.Date d1, final java.util.Date d2, final String select, final boolean readall, final Connection readcon, final int orderBy, final Order orderDir) throws SQLException {
        return getSharedFolderRangeSQL(c, uid, shared_folder_owner, groups, fid, d1, d2, select, readall, readcon, orderBy, orderDir, false);
    }

    @Override
    public final PreparedStatement getSharedFolderRangeSQL(final Context c, final int uid, final int shared_folder_owner, final int groups[], final int fid, final java.util.Date d1, final java.util.Date d2, final String select, final boolean readall, final Connection readcon, final int orderBy, final Order orderDir, final boolean includePrivateAppointments) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(32);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);
        getRange(sb);
        sb.append(PD_FID_IS_NULL);
        if(! includePrivateAppointments) {
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
        if (COLLECTION.getFieldName(orderBy) == null || Order.NO_ORDER.equals(orderDir)) {
            sb.append(ORDER_BY);
        } else {
            sb.append(PDM_ORDER_BY);
            sb.append(COLLECTION.getFieldName(orderBy));
            sb.append(' ');
            sb.append(forSQLCommand(orderDir));
        }
        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setTimestamp(1, new Timestamp(d2.getTime()));
        pst.setTimestamp(2, new Timestamp(d1.getTime()));
        return pst;
    }

    @Override
    public final PreparedStatement getPrivateFolderModifiedSinceSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date since, final String select, final boolean readall, final Connection readcon, final java.util.Date d1, final java.util.Date d2) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
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

    @Override
    public final PreparedStatement getSharedFolderModifiedSinceSQL(final Context c, final int uid, final int shared_folder_owner, final int groups[], final int fid, final java.util.Date since, final String select, final boolean readall, final Connection readcon, final java.util.Date d1, final java.util.Date d2, final boolean includePrivateFlag) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(32);
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

    @Override
    public final PreparedStatement getPublicFolderModifiedSinceSQL(final Context c, final int uid, final int groups[], final int fid, final java.util.Date since, final String select, final boolean readall, final Connection readcon, final java.util.Date d1, final java.util.Date d2) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(48);
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

    @Override
    public final PreparedStatement getPrivateFolderDeletedSinceSQL(final Context c, final int uid, final int fid, final java.util.Date d1, final String select, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128);
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

    @Override
    public final PreparedStatement getSharedFolderDeletedSinceSQL(final Context c, final int uid, final int shared_folder_owner, final int fid, final java.util.Date d1, final String select, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128);
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

    @Override
    public final PreparedStatement getPublicFolderDeletedSinceSQL(final Context c, final int uid, final int fid, final java.util.Date d1, final String select, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(96);
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

    @Override
    public final String getObjectsByidSQL(final int oids[][], final int cid, final String select) {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
        sb.append(parseSelect(select));
        sb.append(" pd WHERE");
        sb.append(" pd.cid = ");
        sb.append(cid);
        sb.append(PDM_AND);
        sb.append(" intfield01 IN ");
        sb.append(StringCollection.getSqlInString(oids));
        return sb.toString();
    }

    @Override
    public PreparedStatement getPrivateFolderObjects(final int fid, final Context c, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
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

    @Override
    public PreparedStatement getPublicFolderObjects(final int fid, final Context c, final Connection readcon) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(64);
        sb.append("SELECT intfield01, created_from FROM prg_dates pd");
        sb.append(WHERE);
        sb.append(" pd.cid = ");
        sb.append(c.getContextId());
        sb.append(PDM_AND_PD_FID);
        sb.append(fid);
        return getPreparedStatement(readcon, sb.toString());
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(final int uid, final int fid, final Context c, final Connection readcon, final int foldertype) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator();
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
            COLLECTION.closeResultSet(rs);
            COLLECTION.closePreparedStatement(prep);
        }
        return ret;
    }

    @Override
    public boolean checkIfFolderIsEmpty(final int uid, final int fid, final Context c, final Connection readcon, final int foldertype) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator();
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
            COLLECTION.closePreparedStatement(prep);
            COLLECTION.closeResultSet(rs);
        }
        return ret;
    }

    private static final void getRange(final Appendable sb) {
        try {
            sb.append(" pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
        } catch (final IOException e) {
            // Cannot occur
        }
    }

    private static final void getConflictRange(final Appendable sb) {
        try {
            sb.append(" intfield07 = 0 AND pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
        } catch (final IOException e) {
            // Cannot occur
        }
    }

    private static final void getConflictRangeFullTime(final Appendable sb) {
        try {
            sb.append(" intfield07 = 1 AND pd.timestampfield01 < ? AND pd.timestampfield02 > ?");
        } catch (final IOException e) {
            // Cannot occur
        }
    }

    private static final void getSince(final Appendable sb) {
        try {
            sb.append(" pd.changing_date > ?");
        } catch (final IOException e) {
            // Cannot occur
        }
    }

    private static final String parseSelect(final String select) {
        if (select != null) {
            return select;
        }
        return CalendarMySQL.select;
    }

    @Override
    public PreparedStatement getSearchStatement(final int uid, final AppointmentSearchObject searchObj, final CalendarFolderObject cfo, final OXFolderAccess folderAccess, final String columns, final int orderBy, final Order orderDir, final Context ctx, final Connection readcon) throws SQLException, OXException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128);
        sb.append("SELECT ");
        sb.append(columns);
        sb.append(", pdm.pfid");
        sb.append(" FROM prg_dates pd JOIN prg_dates_members pdm ON pd.intfield01 = pdm.object_id AND pd.cid = ? AND pdm.cid = ? WHERE ");

        if (searchObj.hasFolders()) {
            final int folderId = searchObj.getFolders()[0];
            final int folderType = folderAccess.getFolderType(folderId, uid);

            if (folderType == FolderObject.PRIVATE) {
                sb.append("(pd.fid = 0 AND pdm.pfid = " + folderId + " AND pdm.member_uid = " + uid + ")");
            } else {
                // Folder is shared or public

                final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, uid);
                final EffectivePermission folderPermission = folderAccess.getFolderPermission(folderId, uid, userConfig);

                final boolean canReadAll = folderPermission.canReadAllObjects();
                if (folderType == FolderObject.SHARED) {
                    final int owner = folderAccess.getFolderOwner(folderId);
                    if (canReadAll) {
                        sb.append("(NOT pd.pflag = 1 AND pd.fid = 0 AND pdm.pfid = " + folderId + " AND pdm.member_uid = " + owner + ")");
                    } else {
                        sb.append("(NOT pd.pflag = 1 AND pd.fid = 0 AND pdm.pfid = " + folderId + " AND pdm.member_uid = " + owner + " AND pd.created_from = " + uid + ")");
                    }
                } else if (folderType == FolderObject.PUBLIC) {
                    if (canReadAll) {
                        sb.append("(pd.fid = " + folderId + ")");
                    } else {
                        sb.append("(pd.fid = " + folderId + " AND pd.created_from = " + uid + ")");
                    }
                }
            }
        } else {
            // Perform search over all folders in which the user has the right to see elements.

            sb.append('(');

            // Look into the users private folders
            boolean first = true;
            for (final int folder : cfo.getPrivateFolders()) {
                if (first) {
                    sb.append("(pd.fid = 0 AND pdm.pfid = " + folder + " AND pdm.member_uid = " + uid + ")");
                    first = false;
                } else {
                    sb.append(" OR (pd.fid = 0 AND pdm.pfid = " + folder + " AND pdm.member_uid = " + uid + ")");
                }
            }

            // Look into folders that are shared to the user
            // where he can read all objects
            for (final int folder : cfo.getSharedReadableAll()) {
                final int owner = folderAccess.getFolderOwner(folder);
                if (first) {
                    sb.append("(NOT pd.pflag = 1 AND pd.fid = 0 AND pdm.pfid = " + folder + " AND pdm.member_uid = " + owner + ")");
                    first = false;
                } else {
                    sb.append(" OR (NOT pd.pflag = 1 AND pd.fid = 0 AND pdm.pfid = " + folder + " AND pdm.member_uid = " + owner + ")");
                }
            }

            // where he can read own objects
            for (final int folder : cfo.getSharedReadableOwn()) {
                final int owner = folderAccess.getFolderOwner(folder);
                if (first) {
                    sb.append("(NOT pd.pflag = 1 AND pd.fid = 0 AND pdm.pfid = " + folder + " AND pdm.member_uid = " + owner + " AND pd.created_from = " + uid + ")");
                    first = false;
                } else {
                    sb.append(" OR (NOT pd.pflag = 1 AND pd.fid = 0 AND pdm.pfid = " + folder + " AND pdm.member_uid = " + owner + " AND pd.created_from = " + uid + ")");
                }
            }

            // Look into public folders
            // where the user can read all objects
            for (final int folder : cfo.getPublicReadableAll()) {
                if (first) {
                    sb.append("(pd.fid = " + folder + ")");
                    first = false;
                } else {
                    sb.append(" OR (pd.fid = " + folder + ")");
                }
            }

            // where the user can read own objects
            for (final int folder : cfo.getPublicReadableOwn()) {
                if (first) {
                    sb.append("(pd.fid = " + folder + " AND pd.created_from = " + uid + ")");
                    first = false;
                } else {
                    sb.append(" OR (pd.fid = " + folder + " AND pd.created_from = " + uid + ")");
                }
            }

            sb.append(')');
        }

        // Look for pattern
        String pattern = searchObj.getPattern();
        if (pattern != null) {
            sb.append(" AND (pd.field01 LIKE ? OR pd.field09 LIKE ?)");
            pattern = StringCollection.prepareForSearch(pattern);
        }

        sb.append(" ORDER BY ");
        String orderby = COLLECTION.getFieldName(orderBy);
        if (orderby == null) {
            orderby = COLLECTION.getFieldName(CalendarObject.START_DATE);
        }
        sb.append("pd." + orderby);
        sb.append(DBUtils.forSQLCommand(orderDir));

        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setInt(1, ctx.getContextId());
        pst.setInt(2, ctx.getContextId());

        if (pattern != null) {
            pst.setString(3, pattern);
            pst.setString(4, pattern);
        }

        return pst;
    }

    @Override
    public final PreparedStatement getSearchQuery(final String select, final int uid, final int groups[], final UserConfiguration uc, final int orderBy, final Order orderDir, final AppointmentSearchObject searchobject, final Context c, final Connection readcon, final CalendarFolderObject cfo, final boolean isShared) throws SQLException, OXException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128);
        sb.append(parseSelect(select));
        sb.append(JOIN_DATES);
        sb.append(c.getContextId());
        sb.append(PDM_CID_IS);
        sb.append(c.getContextId());
        sb.append(WHERE);

        java.util.Date range[] = searchobject.getRange();

        if (isShared) {
            sb.append(" (pflag != 1)");
            sb.append(PDM_AND);
        }

        if (range != null && range[0] != null && range[1] != null) {
            range = searchobject.getRange();
            getRange(sb);
            sb.append(PDM_AND);
        }

        final int folder = searchobject.getFolder();
        if (folder > 0) {
            sb.append(" ((pd.fid = 0");
            sb.append(PDM_PFID_IS);
            sb.append(folder);
            sb.append(PDM_MEMBER_UID_IS);
            sb.append(uid);
            sb.append(") OR (");
            sb.append("pd.fid = ");
            sb.append(folder);
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
                boolean started = false;

                if (!private_read_all.isEmpty()) {
                    sb.append(" AND (");
                    started = true;
                    sb.append("pdm.pfid IN ");
                    sb.append(StringCollection.getSqlInString(private_read_all));
                    private_query = true;
                }

                if (!private_read_own.isEmpty()) {
                    if (!started) {
                        sb.append(" AND (");
                        started = true;
                    }
                    if (private_query) {
                        sb.append(" OR pd.created_from = ");
                    } else {
                        sb.append(PD_CREATED_FROM_IS);
                    }
                    sb.append(uid);
                    sb.append(" AND pdm.pfid IN ");
                    sb.append(StringCollection.getSqlInString(private_read_own));
                    private_query = true;
                }

                if (!public_read_all.isEmpty()) {
                    if (!started) {
                        sb.append(" AND (");
                        started = true;
                    }
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
                    if (!started) {
                        sb.append(" AND (");
                        started = true;
                    }
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
                    if (started) { // Something was appended
                        sb.append(')');
                    }
                }
            }
        }

        String pattern = searchobject.getPattern();
        if (pattern != null) {
            sb.append(PDM_AND);
            sb.append(" (");
            sb.append(COLLECTION.getFieldName(CalendarObject.TITLE));
            sb.append(" LIKE ?");
            sb.append(PDM_OR);
            sb.append(COLLECTION.getFieldName(CommonObject.CATEGORIES));
            sb.append(" LIKE ?");
            sb.append(')');
            pattern = StringCollection.prepareForSearch(pattern);
        }

        sb.append(PDM_ORDER_BY);
        String orderby = COLLECTION.getFieldName(orderBy);
        if (orderby == null) {
            orderby = COLLECTION.getFieldName(CalendarObject.START_DATE);
        }
        sb.append(orderby);
        sb.append(DBUtils.forSQLCommand(orderDir));

        final PreparedStatement pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        int x = 1;

        if (range != null && range[0] != null && range[1] != null) {
            pst.setTimestamp(x++, new Timestamp(range[1].getTime()));
            pst.setTimestamp(x++, new Timestamp(range[0].getTime()));
        }

        if (pattern != null) {
            pst.setString(x++, pattern);
            pst.setString(x++, pattern);
        }

        // TODO: This should be rewritten to be more flexible and to cover all
        // expectations
        return pst;
    }

    @Override
    public final ResultSet getResultSet(final PreparedStatement pst) throws SQLException {
        return pst.executeQuery();
    }

    @Override
    public final PreparedStatement getPreparedStatement(final Connection readcon, final String sql) throws SQLException {
        return readcon.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public final String loadAppointment(final int oid, final Context c) throws SQLException {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(384);
        sb.append("SELECT creating_date, created_from, changing_date, changed_from, fid, pflag, timestampfield01, timestampfield02, timezone, ").append(" intfield02, intfield03, field01, field02, intfield06, intfield08, field04, intfield07, field09, organizer, uid, filename, sequence, organizerId, principal, principalId, intfield04, intfield05, field06, field07, field08 FROM prg_dates  WHERE cid = ");
        sb.append(c.getContextId());
        sb.append(DATES_IDENTIFIER_IS);
        sb.append(oid);
        return sb.toString();

    }

    @Override
    public final CalendarDataObject[] insertAppointment(final CalendarDataObject cdao, final Connection writecon, final Session so) throws DataTruncation, SQLException, OXException, OXException {
    	return insertAppointment0(cdao, writecon, so, true);
    }

    public static final int I(final boolean b){
        return b ? 1 : 0;
    }

    private void checkQuota(Session session) throws OXException {
        ServiceLookup serviceLookup = SERVICES_REF.get();
        if (serviceLookup == null) {
            return;
        }
        QuotaService quotaService = serviceLookup.getService(QuotaService.class);
        if (null != quotaService) {
            Quota quota = quotaService.getQuotaFor(Resource.CALENDAR, session);
            long quotaValue = quota.getQuota(QuotaType.AMOUNT);
            if (quotaValue > 0) {
                long used = countAppointments(session);
                if (used > 0 && used >= quotaValue) {
                    throw QuotaExceptionCodes.QUOTA_EXCEEDED.create();
                }
            }
        }
    }

    private final CalendarDataObject[] insertAppointment0(final CalendarDataObject cdao, final Connection writecon, final Session so, final boolean notify) throws DataTruncation, SQLException, OXException, OXException {
        checkQuota(so);

        int i = 1;
        CalendarVolatileCache.getInstance().invalidateGroup(String.valueOf(cdao.getContextID()));
        PreparedStatement pst = null;
        try {
            pst = writecon.prepareStatement("insert into prg_dates (creating_date, created_from, changing_date, changed_from," + "fid, pflag, cid, timestampfield01, timestampfield02, timezone, intfield01, intfield03, intfield06, intfield07, intfield08, " + "field01, field02, field04, field09, organizer, uid, filename, sequence, organizerId, principal, principalId, intfield02, intfield04, intfield05, field06, field07, field08) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            cdao.setObjectID(IDGenerator.getId(cdao.getContext(), Types.APPOINTMENT, writecon));
            handleUid(cdao, so, !notify);

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
                throw OXCalendarExceptionCodes.NOT_YET_SUPPORTED.create();
            }
            pst.setInt(i++, I(cdao.getPrivateFlag()));
            pst.setInt(i++, cdao.getContextID());
            pst.setTimestamp(i++, new Timestamp(cdao.getStartDate().getTime()));
            pst.setTimestamp(i++, new Timestamp(cdao.getEndDate().getTime()));
            pst.setString(i++, cdao.getTimezoneFallbackUTC());
            pst.setInt(i++, cdao.getObjectID());
            pst.setInt(i++, cdao.getLabel());
            pst.setInt(i++, cdao.getShownAs());
            pst.setInt(i++, I(cdao.getFullTime()));
            pst.setInt(i++, cdao.getNumberOfAttachments());

            if (cdao.isExternalOrganizer()) {
                int descMaxLength = DBUtils.getColumnSize(writecon, CAL_TABLE_NAME, "field01");
                int locMaxLength = DBUtils.getColumnSize(writecon, CAL_TABLE_NAME, "field02");

                if (cdao.getTitle().length() > descMaxLength) {
                    pst.setString(i++, cdao.getTitle().substring(0, descMaxLength));
                } else {
                    pst.setString(i++, cdao.getTitle());
                }

                if (cdao.containsLocation()) {
                    if (cdao.getLocation().length() > locMaxLength) {
                        pst.setString(i++, cdao.getLocation().substring(0, locMaxLength));
                    } else {
                        pst.setString(i++, cdao.getLocation());
                    }
                } else {
                    pst.setNull(i++, java.sql.Types.VARCHAR);
                }
            } else {
                pst.setString(i++, cdao.getTitle());
                if (cdao.containsLocation()) {
                    pst.setString(i++, cdao.getLocation());
                } else {
                    pst.setNull(i++, java.sql.Types.VARCHAR);
                }
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

            if (cdao.containsOrganizer()) {
                pst.setString(i++, cdao.getOrganizer());
            } else {
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }
            if (cdao.containsUid()) {
                pst.setString(i++, cdao.getUid());
            } else {
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }
            if (cdao.containsFilename()) {
                pst.setString(i++, cdao.getFilename());
            } else {
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }
            if (cdao.containsSequence()) {
                pst.setInt(i++, cdao.getSequence());
            } else {
                pst.setNull(i++, java.sql.Types.INTEGER);
            }

            if (cdao.containsOrganizerId()) {
                pst.setInt(i++, cdao.getOrganizerId());
            } else {
                pst.setNull(i++, java.sql.Types.INTEGER);
            }

            if (cdao.containsPrincipal()) {
                pst.setString(i++, cdao.getPrincipal());
            } else {
                pst.setNull(i++, java.sql.Types.VARCHAR);
            }

            if (cdao.containsPrincipalId()) {
                pst.setInt(i++, cdao.getPrincipalId());
            } else {
                pst.setNull(i++, java.sql.Types.INTEGER);
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
            ParticipantStorage.getInstance().insertParticipants(cdao.getContext(), writecon, cdao.getObjectID(), ParticipantStorage.extractExternal(cdao.getParticipants(), cdao.getConfirmations()));
        } catch (final OXException e) {
            DBUtils.rollback(writecon);
            throw e;
        } finally {
            COLLECTION.closePreparedStatement(pst);
        }
        writecon.commit();
        cdao.setParentFolderID(cdao.getActionFolder());
        if (notify && userIsOrganizer(so.getUserId(), cdao)) {
			COLLECTION.triggerEvent(so, CalendarOperation.INSERT, cdao);
		} else if (notify && !userIsOrganizer(so.getUserId(), cdao)) {
		    int confirmOfUser = 0;
		    for (final UserParticipant user : cdao.getUsers()) {
		        if (user.getIdentifier() == so.getUserId()) {
                    confirmOfUser = user.getConfirm();
                }
		    }
		    final int confirm = getConfirmAction(confirmOfUser);
		    COLLECTION.triggerEvent(so, confirm==CalendarObject.NONE ? CalendarOperation.INSERT : confirm, cdao);
		}
		return null;
    }

    private boolean userIsOrganizer(final int user, final CalendarDataObject cal) throws OXException {
        if (!cal.containsOrganizer() || cal.getOrganizer() == null) {
            return true;
        }

        String mail;
        try {
            mail = UserStorage.getInstance().getUser(user, cal.getContext()).getMail();
            if (cal.getOrganizer() != null && cal.getOrganizer().equals(mail)) {
                return true;
            }
        } catch (final OXException e) {
            throw OXCalendarExceptionCodes.EVENT_ERROR.create();
        }

        return false;
    }

    private void handleUid(final CalendarDataObject cdao, final Session so, final boolean exceptionCreate) throws OXException, OXException {
        if (exceptionCreate) {
            return;
        }
        if (cdao.containsUid()) {
            final int resolvedUid = resolveUid(so, cdao.getUid());
            if (resolvedUid > 0) {
                throw OXCalendarExceptionCodes.UID_ALREDY_EXISTS.create(cdao.getTitle(), cdao.getUid());
            }
        } else {
            cdao.setUid(UUID.randomUUID().toString());
        }
    }

    private static final String SQL_INSERT_PARTICIPANT = "INSERT INTO prg_date_rights (object_id, cid, id, type, dn, ma) VALUES (?, ?, ?, ?, ?, ?)";

    private final void insertParticipants(final CalendarDataObject cdao, final Connection writecon)
            throws SQLException, OXException {
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
                                throw OXCalendarExceptionCodes.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD.create();
                            }
                        } else {
                            pi.setString(6, participant.getEmailAddress());
                        }
                        pi.addBatch();
                    }
                }
                pi.executeBatch();
                CalendarVolatileCache.getInstance().invalidateGroup(String.valueOf(cdao.getContextID()));
            } finally {
                COLLECTION.closePreparedStatement(pi);
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
                                throw OXCalendarExceptionCodes.NO_SHARED_FOLDER_OWNER.create();
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
                            throw OXCalendarExceptionCodes.FOLDER_TYPE_UNRESOLVEABLE.create();
                        }
                        stmt.setInt(4, user.getConfirm());

                        if (folderType == FolderObject.SHARED) {
                            if (cdao.containsAlarm() && user.getIdentifier() == cdao.getSharedFolderOwner()) {
                                user.setAlarmMinutes(cdao.getAlarm());
                            } else if (!user.containsAlarm()) {
                                user.setAlarmMinutes(-1);
                            }
                        } else {
                            if (cdao.containsAlarm() && user.getIdentifier() == uid) {
                                user.setAlarmMinutes(cdao.getAlarm());
                            } else {
                                if (!user.containsAlarm()) {
                                    user.setAlarmMinutes(-1);
                                }
                            }
                        }

                        if (user.containsConfirmMessage() && user.getConfirmMessage() != null) {
                            stmt.setString(5, user.getConfirmMessage());
                        } else {
                            stmt.setNull(5, java.sql.Types.VARCHAR);
                        }

                        if (user.getAlarmMinutes() >= 0) {
                            final boolean isCreator = user.getIdentifier() == uid;
                            final long la = user.getAlarmMinutes() * 60000L;
                            Date reminder = null;
                            if (cdao.isSequence() && isJustInThePast(cdao.getStartDate())) {
                                if (isJustInThePast(cdao.getEndDate())) {
                                    stmt.setNull(6, java.sql.Types.INTEGER);
                                } else {
                                    stmt.setInt(6, user.getAlarmMinutes());
                                    if (isCreator) {
                                        final RecurringResultsInterface recurringResults = COLLECTION.calculateRecurring(
                                            cdao,
                                            cdao.getStartDate().getTime(),
                                            cdao.getEndDate().getTime(),
                                            0);
                                        for (int i = 0; i < recurringResults.size(); i++) {
                                            final RecurringResultInterface recurringResult = recurringResults.getRecurringResult(i);
                                            if (!isJustInThePast(recurringResult.getStart())) {
                                                reminder = new Date(recurringResult.getStart() - la);
                                                break;
                                            }
                                        }
                                        if (reminder == null) {
                                            final OXException e = OXCalendarExceptionCodes.NEXT_REMINDER_FAILED.create(Autoboxing.I(cdao.getContext().getContextId()), Autoboxing.I(cdao.getObjectID()));
                                            LOG.warn(e.getMessage(), e);
                                        }
                                    }
                                }
                            } else {
                                if (isJustInThePast(cdao.getEndDate())) {
                                    stmt.setNull(6, java.sql.Types.INTEGER);
                                } else {
                                    stmt.setInt(6, user.getAlarmMinutes());
                                    reminder = new Date(cdao.getStartDate().getTime() - la);
                                }
                            }
                            if (null != reminder) {
                                changeReminder(cdao.getObjectID(), user.getIdentifier(), cdao.getEffectiveFolderId(), cdao.getContext(), cdao.isSequence(true), cdao.getEndDate(), reminder, CalendarOperation.INSERT, false, writecon);
                            }
                        } else {
                            stmt.setNull(6, java.sql.Types.INTEGER);
                        }
                        stmt.setInt(7, cdao.getContextID());
                        COLLECTION.checkUserParticipantObject(user, folderType);
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
                CalendarVolatileCache.getInstance().invalidateGroup(String.valueOf(cdao.getContextID()));
            } finally {
                COLLECTION.closePreparedStatement(stmt);
            }
        } else {
            throw OXException.mandatoryField(1000011, "UserParticipant is empty!");
        }
    }

    private boolean isJustInThePast(final Date date) {
        return isJustInThePast(date.getTime());
    }

    private boolean isJustInThePast(final long millis) {
        return millis < System.currentTimeMillis();
    }

    @Override
    public final void getParticipantsSQLIn(final List<CalendarDataObject> list, final Connection readcon, final int cid, final String sqlin) throws SQLException {

        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final com.openexchange.java.StringAllocator query = new com.openexchange.java.StringAllocator(128);
            query.append("SELECT object_id, id, type, dn, ma FROM prg_date_rights WHERE cid = ");
            query.append(cid);
            query.append(PARTICIPANTS_IDENTIFIER_IN);
            query.append(sqlin);
            query.append(" ORDER BY object_id ASC");
            rs = stmt.executeQuery(query.toString());
            final TIntObjectMap<List<CalendarDataObject>> map;
            {
                final int size = list.size();
                map = new TIntObjectHashMap<List<CalendarDataObject>>(size);
                for (int i = 0; i < size; i++) {
                    final CalendarDataObject cdo = list.get(i);
                    List<CalendarDataObject> l = map.get(cdo.getObjectID());
                    if (null == l) {
                        l = new ArrayList<CalendarDataObject>();
                        map.put(cdo.getObjectID(), l);
                    }
                    l.add(cdo);
                }
            }
            int last_oid = -1;
            Participants participants = null;
            List<CalendarDataObject> cdaos = null;
            Participant participant = null;
            while (rs.next()) {
                final int oid = rs.getInt(1);
                if (last_oid != oid) {
                    if (participants != null && cdaos != null) {
                        for (final CalendarDataObject cdao : cdaos) {
                            cdao.setParticipants(participants.getList());
                        }
                    }
                    participants = new Participants();
                    last_oid = oid;
                    cdaos = map.get(oid);
                }
                final int id = rs.getInt(2);
                final int type = rs.getInt(3);
                if (type == Participant.USER) {
                    participant = new UserParticipant(id);
                } else if (type == Participant.GROUP) {
                    participant = new GroupParticipant(id);
                } else if (type == Participant.RESOURCE) {
                    participant = new ResourceParticipant(id);
                    if (null != cdaos) {
                        for (final CalendarDataObject cdao : cdaos) {
                            cdao.setContainsResources(true);
                        }
                    }
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
            if (cdaos != null && cdaos.get(0).getObjectID() == last_oid) {
                for (final CalendarDataObject cdao : cdaos) {
                    cdao.setParticipants(participants.getList());
                }
            }
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closeStatement(stmt);
        }
    }

    @Override
    public final Participants getParticipants(final CalendarDataObject cdao, final Connection readcon) throws SQLException {
        final Participants participants = new Participants();
        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final com.openexchange.java.StringAllocator query = new com.openexchange.java.StringAllocator(128);
            query.append("SELECT id, type, dn, ma FROM prg_date_rights WHERE cid=");
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
            COLLECTION.closeResultSet(rs);
            COLLECTION.closeStatement(stmt);
        }
        return participants;
    }

    @Override
    public final void getUserParticipantsSQLIn(final CalendarFolderObject visibleFolders, final List<CalendarDataObject> list, final Connection readcon, final int cid, final int uid, final String sqlin) throws SQLException, OXException {
        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final com.openexchange.java.StringAllocator query = new com.openexchange.java.StringAllocator(140);
            query.append("SELECT object_id, member_uid, confirm, reason, pfid, reminder from prg_dates_members WHERE cid = ");
            query.append(cid);
            query.append(PARTICIPANTS_IDENTIFIER_IN);
            query.append(sqlin);
            query.append(" ORDER BY object_id");
            rs = stmt.executeQuery(query.toString());
            final TIntObjectMap<List<CalendarDataObject>> map;
            {
                final int size = list.size();
                map = new TIntObjectHashMap<List<CalendarDataObject>>(size);
                for (int i = 0; i < size; i++) {
                    final CalendarDataObject cdo = list.get(i);
                    List<CalendarDataObject> l = map.get(cdo.getObjectID());
                    if (null == l) {
                        l = new ArrayList<CalendarDataObject>();
                        map.put(cdo.getObjectID(), l);
                    }
                    l.add(cdo);
                }
            }
            String temp = null;
            int last_oid = -1;
            UserParticipant up = null;
            Participants participants = null;
            List<CalendarDataObject> cdaos = null;

            while (rs.next()) {
                final int oid = rs.getInt(1);
                if (last_oid != oid) {
                    if (participants != null) {
                        participants.add(up);
                        if (cdaos != null) {
                            for (final CalendarDataObject cdao : cdaos) {
                                cdao.setUsers(participants.getUsers());
                            }
                        }
                    }
                    participants = new Participants();
                    last_oid = oid;
                    cdaos = map.get(oid);
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
                        LOG.error(StringCollection.convertArraytoString(new Object[] { "ERROR: getUserParticipantsSQLIn oid:uid ", Integer.valueOf(uid), Character.valueOf(CalendarOperation.COLON), Integer.valueOf(cdaos.get(0).getObjectID()) }));
                    }
                    for (final CalendarDataObject cdao : cdaos) {
                        if (cdao.getFolderType() == FolderObject.PRIVATE) {
                            if (uid == tuid) {
                                cdao.setGlobalFolderID(pfid);
                                cdao.setPrivateFolderID(pfid);
                            }
                            up.setPersonalFolderId(pfid);
                        } else if (cdao.getFolderType() == FolderObject.SHARED) {
                            if (cdao.getSharedFolderOwner() == 0) {
                                throw OXCalendarExceptionCodes.NO_SHARED_FOLDER_OWNER.create();
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
                            if (visibleFolders == null || visibleFolders.getSharedFolderList().contains(pfid)) {
                                cdao.setActionFolder(pfid);
                            }
                        }
                    }
                }

                final int alarm = rs.getInt(6);
                if (!rs.wasNull()) {
                    up.setAlarmMinutes(alarm);
                    if (up.getIdentifier() == uid && up.getAlarmMinutes() >= 0) {
                        for (final CalendarDataObject cdao : cdaos) {
                            cdao.setAlarm(up.getAlarmMinutes());
                        }
                    }
                }
                if (participants != null) {
                    participants.add(up);
                }
            }
            if (cdaos != null && cdaos.get(0).getObjectID() == last_oid) {
                for (final CalendarDataObject cdao : cdaos) {
                    cdao.setUsers(participants.getUsers());
                }
            }
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closeStatement(stmt);
        }
    }

    @Override
    public final Participants getUserParticipants(final CalendarDataObject cdao, final Connection readcon, final int uid) throws SQLException, OXException {
        final Participants participants = new Participants();
        final Statement stmt = readcon.createStatement();
        ResultSet rs = null;
        try {
            final com.openexchange.java.StringAllocator query = new com.openexchange.java.StringAllocator(140);
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

                final int alarm = rs.getInt(5);
                if (!rs.wasNull()) {
                    up.setAlarmMinutes(alarm);
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
                        if (tuid == uid && up.containsAlarm()) {
                            cdao.setAlarm(up.getAlarmMinutes());
                        }
                    } else if (cdao.getFolderType() == FolderObject.SHARED) {
                        if (cdao.getSharedFolderOwner() == 0) {
                            throw OXCalendarExceptionCodes.NO_SHARED_FOLDER_OWNER.create();
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

                        if (tuid == cdao.getSharedFolderOwner() && up.containsAlarm()) {
                            cdao.setAlarm(up.getAlarmMinutes());
                        }
                    } else if (uid == tuid) {
                        cdao.setGlobalFolderID(pfid);
                        cdao.setPrivateFolderID(pfid);
                    } else {
                        cdao.setActionFolder(pfid);
                    }
                }
                participants.add(up);
            }
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closeStatement(stmt);
        }
        return participants;
    }

    public final CalendarDataObject loadObjectForUpdate(final CalendarDataObject cdao, final Session so, final Context ctx, final int inFolder, final Connection con) throws SQLException, OXException {
        return loadObjectForUpdate(cdao, so, ctx, inFolder, con, true);
    }

    @Override
    public final CalendarDataObject loadObjectForUpdate(final CalendarDataObject cdao, final Session so, final Context ctx, final int inFolder, final Connection con, final boolean checkPermissions) throws SQLException, OXException {
        final CalendarOperation co = new CalendarOperation();
        Connection readcon = null;
        boolean pushCon = false;
        CalendarDataObject edao = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if (con == null) {
                readcon = DBPool.pickup(ctx);
                pushCon = true;
            } else {
                readcon = con;
            }

            int action_folder = inFolder;
            if (cdao.containsParentFolderID()) {
                action_folder = cdao.getParentFolderID();
            }
            prep = getPreparedStatement(readcon, loadAppointment(cdao.getObjectID(), cdao.getContext()));
            rs = getResultSet(prep);
            edao = co.loadAppointment(rs, cdao.getObjectID(), inFolder, this, readcon, so, ctx, CalendarOperation.UPDATE, action_folder, checkPermissions);
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch (final OXException oxe) {
            throw oxe;
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closePreparedStatement(prep);
            if (pushCon && readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
        return edao;
    }

    @Override
    public final CalendarDataObject[] updateAppointment(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified) throws SQLException, OXException {
        return updateAppointment(cdao, edao, writecon, so, ctx, inFolder, clientLastModified, true, false);
    }

    private boolean isForbiddenPrivateMoveToPublicFolder(CalendarDataObject cdao, CalendarDataObject edao) {
        if (!(cdao.getFolderMove() && cdao.getFolderType() == FolderObject.PUBLIC)) {
            return false;
        }

        if (edao.getPrivateFlag() && !cdao.containsPrivateFlag()) {
            return true;
        }

        if (edao.getPrivateFlag() && cdao.containsPrivateFlag() && cdao.getPrivateFlag()) {
            return true;
        }

        if (!edao.getPrivateFlag() && cdao.containsPrivateFlag() && cdao.getPrivateFlag()) {
            return true;
        }

        return false;
    }

    private final CalendarDataObject[] updateAppointment(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified, final boolean clientLastModifiedCheck, final boolean skipParticipants) throws DataTruncation, SQLException, OXException {
        CalendarVolatileCache.getInstance().invalidateGroup(String.valueOf(cdao.getContextID()));

        final CalendarOperation co = new CalendarOperation();

        if (isForbiddenPrivateMoveToPublicFolder(cdao, edao)) {
            throw OXCalendarExceptionCodes.PRIVATE_MOVE_TO_PUBLIC.create();
        }

        if (cdao.getFolderMove() && edao.getRecurrenceType() != CalendarObject.NO_RECURRENCE) {
            throw OXCalendarExceptionCodes.RECURRING_FOLDER_MOVE.create();
        }

        COLLECTION.detectFolderMoveAction(cdao, edao);

        if (clientLastModified == null) {
            throw OXCalendarExceptionCodes.LAST_MODIFIED_IS_NULL.create();
        } else if (edao.getLastModified() == null) {
            throw OXCalendarExceptionCodes.LAST_MODIFIED_IS_NULL.create();
        }

        if (clientLastModifiedCheck && edao.getLastModified().getTime() > clientLastModified.getTime()) {
            throw OXException.conflict();
        }

        final int rec_action = co.checkUpdateRecurring(cdao, edao);
        if (edao.containsRecurrencePosition() && edao.getRecurrencePosition() > 0) {
            /*
             * edao denotes a change exception
             */
            if (cdao.getFolderMove()) {
                throw OXCalendarExceptionCodes.RECURRING_EXCEPTION_MOVE_EXCEPTION.create();
            }
            if (edao.containsPrivateFlag() && cdao.containsPrivateFlag() && edao.getPrivateFlag() != cdao.getPrivateFlag()) {
                throw OXCalendarExceptionCodes.RECURRING_EXCEPTION_PRIVATE_FLAG.create();
            }
            if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) {
                if (edao.getRecurrencePosition() != cdao.getRecurrencePosition()) {
                    throw OXCalendarExceptionCodes.INVALID_RECURRENCE_POSITION_CHANGE.create();
                }
            } else {
                cdao.setRecurrencePosition(edao.getRecurrencePosition());
            }
        }

        CalendarDataObject clone = null;

        final boolean changeMasterTime = checkRecurrenceMasterTimeUpdate(cdao, edao);

        //Reset all exceptions (change and delete)
        if(changeMasterTime) {
            cdao.setExceptions(null);
            cdao.setDelExceptions(null);
        }

        if (rec_action == CalendarCollectionService.CHANGE_RECURRING_TYPE || changeMasterTime) {
            if (edao.getRecurrenceID() > 0 && edao.getObjectID() != edao.getRecurrenceID()) {
                throw OXCalendarExceptionCodes.INVALID_RECURRENCE_TYPE_CHANGE.create(new Object[0]);
            }
            final List<Integer> exceptions = getExceptionList(null, ctx, edao.getRecurrenceID());
            if (exceptions != null && !exceptions.isEmpty()) {
                final Integer oids[] = exceptions.toArray(new Integer[exceptions.size()]);
                if (oids.length > 0) {
                    deleteAllRecurringExceptions(oids, so, writecon, false);
                    for (int a = 0; a < exceptions.size(); a++) {
                        triggerDeleteEvent(writecon, exceptions.get(a).intValue(), inFolder, so, ctx, null);
                    }
                }
            }
            // Fake a series deletion for MS Outlook
            backupAppointment(writecon, so.getContextId(), edao.getObjectID(), so.getUserId());
        } else if (rec_action == CalendarCollectionService.RECURRING_EXCEPTION_DELETE) {
            final List<Integer> exceptions = getExceptionList(null, ctx, edao.getRecurrenceID());
            if (exceptions != null && !exceptions.isEmpty()) {
                final Integer oids[] = exceptions.toArray(new Integer[exceptions.size()]);
                if (oids.length > 0) {
                    deleteAllRecurringExceptions(oids, so, writecon);
                    for (int a = 0; a < exceptions.size(); a++) {
                        triggerDeleteEvent(writecon, exceptions.get(a).intValue(), inFolder, so, ctx, null);
                    }
                }
            }
            COLLECTION.purgeExceptionFieldsFromObject(cdao);
        } else if (rec_action == CalendarCollectionService.RECURRING_EXCEPTION_DELETE_EXISTING) {
        	final Date[] deleteExceptions = cdao.getDeleteException();
			final List<Integer> deleteExceptionPositions = new ArrayList<Integer>();
			{
				/*
				 * Get corresponding positions in recurring appointment whose
				 * change exception shall be turned to a delete exception
				 */
			    final int[] positions = COLLECTION.getDatesPositions(deleteExceptions, edao);
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
					final AppointmentSQLInterface calendarSql = FACTORY_REF.get().createAppointmentSql(so);
					for (int i = 0; i < objectIDs2Delete.length; i++) {
						final int objectID2Delete = objectIDs2Delete[i].intValue();
						final CalendarDataObject toDelete = calendarSql.getObjectById(objectID2Delete, inFolder);
						deleteAppointment(writecon, so.getContextId(), objectID2Delete, so.getUserId());
						triggerDeleteEvent(writecon, objectID2Delete, inFolder, so, ctx, toDelete);
					}
				}
				// Remove deleted change exceptions from list
				if (!dates.isEmpty()) {
					Date[] cdates = COLLECTION.removeException(edao.getChangeException(), dates
							.remove(0).longValue());
					while (!dates.isEmpty()) {
						cdates = COLLECTION.removeException(cdates, dates.remove(0).longValue());
					}
					cdao.setChangeExceptions(cdates);
				}
			}
        } else if (rec_action == CalendarCollectionService.RECURRING_CREATE_EXCEPTION) {
            // Because the GUI only sends changed fields, we have to create a
            // merged object
            // from cdao and edao and then we force an insert!
            if (edao.containsPrivateFlag() && cdao.containsPrivateFlag() && edao.getPrivateFlag() != cdao.getPrivateFlag()) {
                throw OXCalendarExceptionCodes.RECURRING_EXCEPTION_PRIVATE_FLAG.create();
            }
            /*
             * Create a clone for the "new" change exception
             */
            clone = COLLECTION.cloneObjectForRecurringException(cdao, edao, so.getUserId());
            try {
                cdao.setRecurrenceCalculator(edao.getRecurrenceCalculator());
                if (cdao.containsAlarm()) {
                    final UserParticipant[] users;
                    if (cdao.containsUserParticipants() && cdao.getUsers() != null) {
                        users = COLLECTION.checkAndModifyAlarm(cdao, cdao.getUsers(), so.getUserId(), edao.getUsers());
                    } else {
                        users = COLLECTION.checkAndModifyAlarm(cdao, edao.getUsers(), so.getUserId(), edao.getUsers());
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

                if (!clone.containsUid()) {
                    clone.setUid(edao.getUid());
                }

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
					    newCloneUsers[i] = cloneUsers[i].clone();
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
                COLLECTION.triggerEvent(so, CalendarOperation.INSERT, clone);

                // Proceed with update
                COLLECTION.removeFieldsFromObject(cdao);
                // no update here
                cdao.setParticipants(edao.getParticipants());
                cdao.setUsers(edao.getUsers());
                cdao.setRecurrence(edao.getRecurrence());
                cdao.setLastModified(clone.getLastModified());
            } catch (final SQLException sqle) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
            } catch (final RuntimeException ex) {
                throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(ex, Integer.valueOf(2));
            } catch (final CloneNotSupportedException ex) {
                throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(ex, Integer.valueOf(2));
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
            if (!COLLECTION.checkIfDatesOccurInRecurrence(COLLECTION.mergeExceptionDates(
                    cdao.getDeleteException(), cdao.getChangeException()), tdao)) {
                throw OXCalendarExceptionCodes.FOREIGN_EXCEPTION_DATE.create();
            }
        }

        if (cdao.getFolderMove()) {
            // Fake a deletion on MOVE operation for MS Outlook prior to performing actual UPDATE
            backupAppointment(writecon, so.getContextId(), cdao.getObjectID(), so.getUserId());
        }

        final int ucols[] = new int[30];
        int uc = CalendarOperation.fillUpdateArray(cdao, edao, ucols);
        final MBoolean cup = new MBoolean(false);
        boolean realChange = uc > 0;
        if (uc > 0 || COLLECTION.check(cdao.getUsers(), edao.getUsers())) {

            ucols[uc++] = DataObject.LAST_MODIFIED;
            ucols[uc++] = DataObject.MODIFIED_BY;

            // If a normal appointment is changed into a recurring appointment,
            // recurring position (intfield05) has to be set to 0 instead of staying NULL.
            // Otherwise it will disappear in outlook because of missing series information.
            if (edao.getRecurrence() == null && cdao.getRecurrence() != null && !com.openexchange.tools.arrays.Arrays.contains(ucols, CalendarObject.RECURRENCE_POSITION)) {
                cdao.setRecurrencePosition(0);
                ucols[uc++] = CalendarObject.RECURRENCE_POSITION;
            }

            final com.openexchange.java.StringAllocator update = new com.openexchange.java.StringAllocator();
            update.append("UPDATE prg_dates pd SET ");
            update.append(COLLECTION.getFieldName(ucols[0]));
            update.append(" = ?");
            for (int a = 1; a < uc; a++) {
                update.append(", ");
                update.append(COLLECTION.getFieldName(ucols[a]));
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
                    final boolean temp = updateParticipants(cdao, edao, so.getUserId(), so.getContextId(), writecon, cup);
                    realChange = realChange || temp;
                }
                final int ret = pst.executeUpdate();
                if (ret == 0) {
                    throw OXException.conflict();
                }
                if (edao.isException()) {
                    // Update last-modified of master
                    updateLastModified(edao.getRecurrenceID(), ctx.getContextId(), so.getUserId(), cdao.getLastModified().getTime(), writecon);
                }
            } catch (final OXException e) {
                if (!writecon.getAutoCommit()) {
                    writecon.rollback();
                }
                throw e;
            } finally {
                COLLECTION.closePreparedStatement(pst);
            }

        }
        cdao.setParentFolderID(cdao.getActionFolder());

        if (cdao.getFolderMove()) {

            /*
             * Update reminders' folder ID on move operation
             */
            final ReminderService reminderInterface = new ReminderHandler(ctx);

            final SearchIterator<?> it = reminderInterface.listReminder(Types.APPOINTMENT, cdao.getObjectID());
            final List<ReminderObject> toUpdate = new ArrayList<ReminderObject>();
            try {
                while (it.hasNext()) {
                    toUpdate.add((ReminderObject) it.next());
                }
            } finally {
                try {
                    it.close();
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            for (final ReminderObject reminder : toUpdate) {
                // Check for public->private move
                if (edao.getFolderType() == FolderObject.PUBLIC && cdao.getFolderType() == FolderObject.PRIVATE) {
                    if (reminder.getUser() == so.getUserId()) {
                        reminder.setFolder(cdao.getActionFolder());
                    } else {
                        final OXFolderAccess access = new OXFolderAccess(cdao.getContext());
                        reminder.setFolder(access.getDefaultFolder(reminder.getUser(), FolderObject.CALENDAR).getObjectID());
                    }
                } else {
                    reminder.setFolder(cdao.getParentFolderID());
                }
                reminderInterface.updateReminder(reminder, writecon);
            }
        }

        final boolean solo_reminder = COLLECTION.checkForSoloReminderUpdate(cdao, ucols, cup);
        COLLECTION.checkAndRemovePastReminders(cdao, edao);
        if (!solo_reminder) {
            COLLECTION.triggerModificationEvent(so, edao, cdao);
        }
        if(rec_action == CalendarCollectionService.RECURRING_CREATE_EXCEPTION) {
            CalendarCallbacks.getInstance().createdChangeExceptionInRecurringAppointment(cdao, clone,inFolder, so);
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
            final RecurringResultsInterface rresults = COLLECTION.calculateRecurring(main, 0, 0, 0,
                CalendarCollectionService.MAX_OCCURRENCESE, true);
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
                        CalendarCollectionService.RECURRING_NO_ACTION, main, main, clientLastModified);
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
        if (!currentObject.containsRecurrenceType() || currentObject.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
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

    /**
     * Updates the participants.
     *
     * @param cdao
     * @param edao
     * @param uid
     * @param cid
     * @param writecon
     * @param cup
     * @return A boolean, which indicates, if any changes are made to the participants, except confirmation updates.
     * @throws SQLException
     * @throws OXException
     * @throws OXException
     */
    private final boolean updateParticipants(final CalendarDataObject cdao, final CalendarDataObject edao, final int uid, final int cid, final Connection writecon, final MBoolean cup) throws SQLException, OXException, OXException {
        CalendarVolatileCache.getInstance().invalidateGroup(String.valueOf(cdao.getContextID()));
        boolean retval = false;
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
            throw OXCalendarExceptionCodes.PRIVATE_FLAG_AND_PARTICIPANTS.create(new Object[0]);
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

        final boolean time_change = COLLECTION.detectTimeChange(cdao, edao);

        if (time_change && users == null) {
            users = edao.getUsers();
        }

        if (users != null) {
            Arrays.sort(users);
            Arrays.sort(old_users);
            final Participants p[] = CalendarOperation.getModifiedUserParticipants(users, old_users, uid, cdao.getSharedFolderOwner(), time_change, cdao);
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
//                        throw new OXException(OXException.Code.OWNER_REMOVAL_EXCEPTION);
//                    }
//                }
                check_up -= deleted_userparticipants.length;
            }
        }
        final boolean onlyAlarmChange = modified_userparticipants == null;
        modified_userparticipants = COLLECTION.checkAndModifyAlarm(cdao, modified_userparticipants, uid, edao.getUsers());

        if (check_up < 1) {
            throw OXCalendarExceptionCodes.UPDATE_WITHOUT_PARTICIPANTS.create();
        }

        if (new_participants != null && new_participants.length > 0) {
            final Set<Integer> knownExternalIds = createExternalIdentifierSet(old_participants);
            retval = true;
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
                                throw OXCalendarExceptionCodes.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD.create();
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
                COLLECTION.closePreparedStatement(dr);
            }
        }

        if (deleted_participants != null && deleted_participants.length > 0) {
            retval = true;
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
                    COLLECTION.closePreparedStatement(pde);
                }
            } finally {
                COLLECTION.closePreparedStatement(pd);
            }
        }

        if (new_userparticipants != null && new_userparticipants.length > 0) {
            retval = true;
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
                                        throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(3));
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
                                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(4));
                                }
                            }
                        } else if (FolderObject.PUBLIC == folderType) {
                            final int pfid;
                            if (cdao.getFolderMove()) {
                                if (FolderObject.PRIVATE == cdao.getFolderType()) {
                                    // move public -> private
                                    final int defaultId = access.getDefaultFolder(new_userparticipants[a].getIdentifier(), FolderObject.CALENDAR).getObjectID();
                                    if (new_userparticipants[a].getIdentifier() == uid && cdao.getActionFolder() != defaultId) {
                                        pfid = cdao.getActionFolder();
                                    } else {
                                        pfid = defaultId;
                                    }
                                } else {
                                    // TODO needs to be implemented.
                                    pfid = -1;
                                }
                            } else {
                                // users personal folder is set to zero/null if appointment is located in public folder.
                                pfid = 0;
                            }
                            if (pfid == 0) {
                                pi.setNull(5, java.sql.Types.INTEGER);
                            } else {
                                pi.setInt(5, pfid);
                            }
                            new_userparticipants[a].setPersonalFolderId(pfid);
                        } else if (FolderObject.SHARED == folderType) {
                            if (edao.getSharedFolderOwner() == 0) {
                                throw OXCalendarExceptionCodes.NO_SHARED_FOLDER_OWNER.create();
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
                                        throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(5));
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
                                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(3));
                                }
                            }
                        } else {
                            throw OXCalendarExceptionCodes.FOLDER_TYPE_UNRESOLVEABLE.create();
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
                            changeReminder(cdao.getObjectID(), uid, cdao.getEffectiveFolderId(), cdao.getContext(), isSequence, end_date, new java.util.Date(calc_date.getTime() - la), CalendarOperation.INSERT, (isSequence ? checkRecurrenceChange(cdao, edao) : false), writecon);
                        } else {
                            pi.setNull(6, java.sql.Types.INTEGER);
                        }

                        pi.setInt(7, cid);
                        COLLECTION.checkUserParticipantObject(new_userparticipants[a], cdao.getFolderType());
                        pi.addBatch();
                        if (checkForDeletedParticipants(new_userparticipants[a].getIdentifier(), cdao.getContextID(), cdao.getObjectID(), cdao.getContext(), writecon)) {
                            deleted.add(new_userparticipants[a]);
                        }
                    }
                }
                pi.executeBatch();
            } finally {
                COLLECTION.closePreparedStatement(pi);
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
                    prepareConfirmation(edao, modified_userparticipants, pu, a);
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

                                    if (edao.getFolderType() == FolderObject.PUBLIC) {
                                        pfid = cdao.getActionFolder();
                                    }
                                    pu.setInt(3, pfid);
                                } catch (final Exception fe) {
                                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(6));
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
                                        throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(6));
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
                                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(6));
                                }
                            }
                        } else {
                            throw OXCalendarExceptionCodes.FOLDER_TYPE_UNRESOLVEABLE.create();
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
                                throw OXCalendarExceptionCodes.NO_SHARED_FOLDER_OWNER.create();
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
                                            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(fe, Integer.valueOf(7));
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
                            throw OXCalendarExceptionCodes.FOLDER_TYPE_UNRESOLVEABLE.create();
                        }
                    }

                    if (modified_userparticipants[a].getAlarmMinutes() >= 0 && modified_userparticipants[a].containsAlarm()) {
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
                         final long la = modified_userparticipants[a].getAlarmMinutes() * 60000L;
                        java.util.Date reminder = null;

                        // If the appointment is a collection that starts in the past and ends in the future
                        // the reminder will be set to the next occurrence.
                        if (cdao.isSequence() && COLLECTION.isInThePast(calc_date)) {
                            if (COLLECTION.isInThePast(end_date)) {
                                pu.setNull(4, java.sql.Types.INTEGER);
                            } else {
                                pu.setInt(4, modified_userparticipants[a].getAlarmMinutes());
                                final RecurringResultsInterface recurringResults = COLLECTION.calculateRecurring(
                                    edao,
                                    calc_date.getTime(),
                                    end_date.getTime(),
                                    0);
                                if(recurringResults == null) {
                                    break;
                                }
                                for (int i = 0; i < recurringResults.size(); i++) {
                                    final RecurringResultInterface recurringResult = recurringResults.getRecurringResult(i);
                                    if (recurringResult.getStart() > new Date().getTime()) {
                                        reminder = new java.util.Date(recurringResult.getStart() - la);
                                        break;
                                    }
                                }
                            }
                        } else {
                            pu.setInt(4, modified_userparticipants[a].getAlarmMinutes());
                        }

                        if (reminder == null) {
                            reminder = new java.util.Date(calc_date.getTime() - la);
                        }

                        int folder_id = modified_userparticipants[a].getPersonalFolderId();
                        if (folder_id <= 0) {
                            folder_id = cdao.getEffectiveFolderId();
                        }
                        final boolean isSequence = cdao.isSequence(true);

                        changeReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), folder_id, cdao.getContext(), isSequence, end_date, reminder, CalendarOperation.UPDATE, isSequence ? checkRecurrenceChange(cdao, edao) : false, writecon);

                    } else {
                        pu.setNull(4, java.sql.Types.INTEGER);
                        deleteReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), cdao.getContext(), writecon);
                        //changeReminder(cdao.getObjectID(), modified_userparticipants[a].getIdentifier(), -1, cdao.getContext(), cdao.isSequence(true), null, null, CalendarOperation.DELETE, false);
                    }

                    pu.setInt(5, cdao.getObjectID());
                    pu.setInt(6, cid);
                    pu.setInt(7, modified_userparticipants[a].getIdentifier());
                    COLLECTION.checkUserParticipantObject(modified_userparticipants[a], folderType);
                    pu.addBatch();
                }
                pu.executeBatch();
            } finally {
                COLLECTION.closePreparedStatement(pu);
            }
        }

        if (deleted_userparticipants != null && deleted_userparticipants.length > 0) {
            retval = true;
            cup.setMBoolean(true);
            PreparedStatement pd = null;
            try {
                pd = writecon.prepareStatement("delete from prg_dates_members WHERE object_id = ? AND cid = ? AND member_uid = ?");
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

                    deleteReminder(cdao.getObjectID(), uid, cdao.getContext(), writecon);
                    //changeReminder(cdao.getObjectID(), uid, -1, cdao.getContext(), cdao.isSequence(true), end_date, new java.util.Date(calc_date.getTime() + deleted_userparticipants[a].getAlarmMinutes()), CalendarOperation.DELETE, false);
                    new_deleted.add(deleted_userparticipants[a]);
                }
                pd.executeBatch();
            } finally {
                COLLECTION.closePreparedStatement(pd);
            }
        }

        boolean del_master_update = false;
        final UserParticipant newdel_up[] = new_deleted.getUsers();

        if (newdel_up != null && newdel_up.length > 0) {
            if (!checkForDeletedMasterObject(cdao.getObjectID(), cid, cdao.getContext())) {
                cup.setMBoolean(true);
                PreparedStatement pidm = null;
                try {
                    pidm = writecon.prepareStatement("DELETE FROM del_dates WHERE cid = ? AND intfield01 = ?");
                    pidm.setInt(1, cid);
                    pidm.setInt(2, cdao.getObjectID());
                    pidm.execute();
                } finally {
                    closeSQLStuff(pidm);
                }
                try {
                    pidm = writecon.prepareStatement("insert into del_dates (creating_date, created_from, changing_date, changed_from, fid, intfield01, cid, pflag) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    pidm.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    pidm.setInt(2, uid);
                    pidm.setLong(3, System.currentTimeMillis());
                    pidm.setInt(4, uid);
                    pidm.setInt(5, cdao.getGlobalFolderID());
                    pidm.setInt(6, cdao.getObjectID());
                    pidm.setInt(7, cid);
                    pidm.setInt(8, I(cdao.getPrivateFlag()));
                    pidm.addBatch();
                    pidm.executeBatch();
                } finally {
                    COLLECTION.closePreparedStatement(pidm);
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
                COLLECTION.closeStatement(cleanStatement);
                COLLECTION.closePreparedStatement(pid);
            }
        }

        final UserParticipant del_up[] = deleted.getUsers();

        if (del_up != null && del_up.length > 0) {
            cup.setMBoolean(true);
            PreparedStatement pdd = null;
            try {
                pdd = writecon.prepareStatement("delete from del_dates_members WHERE object_id = ? AND cid = ? AND member_uid = ?");
                for (int a = 0; a < del_up.length; a++) {
                    pdd.setInt(1, cdao.getObjectID());
                    pdd.setInt(2, cid);
                    pdd.setInt(3, del_up[a].getIdentifier());
                    pdd.addBatch();
                }
                pdd.executeBatch();
                del_master_update = true;
            } finally {
                COLLECTION.closePreparedStatement(pdd);
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
                    COLLECTION.closePreparedStatement(ddd);
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
                COLLECTION.closePreparedStatement(ddu);
            }
        }
        final ParticipantStorage participantStorage = ParticipantStorage.getInstance();
        participantStorage.deleteParticipants(cdao.getContext(), writecon, cdao.getObjectID(), ParticipantStorage.extractExternal(deleted_participants));
        participantStorage.insertParticipants(cdao.getContext(), writecon, cdao.getObjectID(), ParticipantStorage.extractExternal(new_participants));

        COLLECTION.fillEventInformation(cdao, edao, edao.getUsers(), new_userparticipants, deleted_userparticipants, modified_userparticipants, edao.getParticipants(), new_participants, deleted_participants, null);

        return retval;
    }

    private void prepareConfirmation(final CalendarDataObject edao, final UserParticipant[] modified_userparticipants, final PreparedStatement pu, final int a) throws SQLException {
        final UserParticipant oldUser = searchUser(modified_userparticipants[a].getIdentifier(), edao);
        if (!modified_userparticipants[a].containsConfirm() && oldUser != null) {
            pu.setInt(1, oldUser.getConfirm());
        } else {
            pu.setInt(1, modified_userparticipants[a].getConfirm());
        }

        if (!modified_userparticipants[a].containsConfirmMessage() && oldUser != null) {
            if (oldUser.getConfirmMessage() == null) {
                pu.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pu.setString(2, oldUser.getConfirmMessage());
            }
        } else {
            if (modified_userparticipants[a].getConfirmMessage() == null) {
                pu.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pu.setString(2, modified_userparticipants[a].getConfirmMessage());
            }
        }
    }

    /**
     * Searches for the given user id in the users of the given CalendarDataObject
     * @param id
     * @param calendarDataObject
     * @return the UserParticipant if found, null otherwise.
     */
    private UserParticipant searchUser(final int id, final CalendarDataObject calendarDataObject) {
        for (final UserParticipant user : calendarDataObject.getUsers()) {
            if (user.getIdentifier() == id) {
                return user;
            }
        }
        return null;
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

    @Override
    public final Date setUserConfirmation(final int oid, final int folderId, final int uid, final int confirm, final String message, final Session so, final Context ctx) throws OXException {
        checkConfirmPermission(folderId, uid, so, ctx);
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
                final OXException e = OXException.notFound("Object: " + oid + ", Context: " + so.getContextId() + ", User: " + uid);
                LOG.error(e.getMessage(), e);
                throw e;
            } else {
                LOG.warn(StringCollection.convertArraytoString(new Object[] { "Result of setUserConfirmation was ",
                        Integer.valueOf(changes), ". Check prg_dates_members object_id = ", Integer.valueOf(oid),
                        " cid = ", Integer.valueOf(so.getContextId()), " uid = ", Integer.valueOf(uid) }));
            }
        } catch (final SQLException sqle) {
            if (writecon != null) {
                try {
                    writecon.rollback();
                } catch (final SQLException rb) {
                    LOG.error("setUserConfirmation (writecon) error while rollback ", rb);
                }
            }
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            COLLECTION.closePreparedStatement(pu);
            COLLECTION.closePreparedStatement(mo);
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
        final int fid = COLLECTION.resolveFolderIDForUser(oid, uid, ctx);
        if (fid == -1) {
            LOG.warn(StringCollection.convertArraytoString(new Object[] {
                    "Confirmation event could not be triggered: Unable to resolve folder id for user:oid:context",
                    Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(so.getContextId()) }));
            return changeTimestamp;
        }
        final CalendarDataObject cdao;
        try {
            cdao = FACTORY_REF.get().createAppointmentSql(so).getObjectById(oid, fid);
        } catch (final SQLException e) {
            LOG.warn("Confirmation event could not be triggered", OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e));
            return changeTimestamp;
        }
        cdao.setParentFolderID(fid);
        COLLECTION.triggerEvent(so, getConfirmAction(confirm), cdao);
        return changeTimestamp;
    }

    @Override
    public Date setExternalConfirmation(final int oid, final int folderId, final String mail, final int confirm, final String message, final Session so, final Context ctx) throws OXException {
        checkConfirmPermission(folderId, -1, so, ctx);

        final String insert = "INSERT INTO dateExternal (confirm, reason, objectId, cid, mailAddress) VALUES (?, ?, ?, ?, ?)"; //this is a party crasher
        final String update = "UPDATE dateExternal SET confirm = ?, reason = ? WHERE objectId = ? AND cid = ? and mailAddress = ?";
        final String updateAppointment = "UPDATE prg_dates SET changing_date = ?, changed_from = ? WHERE intfield01 = ? AND cid = ?";

        Connection writeCon = null;
        Connection readCon = null;
        PreparedStatement stmt = null;
        PreparedStatement stmtUpdateAppointment = null;
        final Date changeTimestamp = new Date();
        try {
            readCon = DBPool.pickup(ctx);
            final boolean isNewParticipant = ! checkIfParticipantIsInvited(mail, ctx.getContextId(), oid, readCon);
            writeCon = DBPool.pickupWriteable(ctx);
            writeCon.setAutoCommit(false);

            if(isNewParticipant) {
                stmt = writeCon.prepareStatement(insert);
            } else {
                stmt = writeCon.prepareStatement(update);
            }

            stmt.setInt(1, confirm);
            if (message == null) {
                stmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(2, message);
            }
            stmt.setInt(3, oid);
            stmt.setInt(4, so.getContextId());
            stmt.setString(5, mail);
            final int changes = stmt.executeUpdate();
            if (changes > 0) {
                stmtUpdateAppointment = writeCon.prepareStatement(updateAppointment);
                stmtUpdateAppointment.setLong(1, changeTimestamp.getTime());
                stmtUpdateAppointment.setInt(2, so.getUserId());
                stmtUpdateAppointment.setInt(3, oid);
                stmtUpdateAppointment.setInt(4, so.getContextId());
                stmtUpdateAppointment.executeUpdate();
                writeCon.commit();
            } else {
                writeCon.rollback();
                final OXException e = OXCalendarExceptionCodes.COULD_NOT_FIND_PARTICIPANT.create();
                LOG.error(e.getMessage(), e);
                throw e;
            }
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            COLLECTION.closePreparedStatement(stmt);
            COLLECTION.closePreparedStatement(stmtUpdateAppointment);
            if (writeCon != null) {
                try {
                    writeCon.setAutoCommit(true);
                } catch (final SQLException sqle) {
                    LOG.error("setUserConfirmation (writecon) error while setAutoCommit(true) ", sqle);
                    throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
                } finally {
                    DBPool.closeWriterSilent(ctx, writeCon);
                }
            }
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
        return changeTimestamp;
    }

    private void checkConfirmPermission(final int folderId, final int uid, final Session so, final Context ctx) throws OXException {
        if (uid != so.getUserId()) {
            final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, so.getUserId());
            final OXFolderAccess ofa = new OXFolderAccess(ctx);
            final EffectivePermission oclp = ofa.getFolderPermission(folderId, so.getUserId(), userConfig);
            if (ofa.getFolderType(folderId, so.getUserId()) == FolderObject.PUBLIC) {
                throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_1.create();
            }
            if (!oclp.canWriteAllObjects()) {
                throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_1.create();
            }
        }
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
        return CalendarOperation.CONFIRM_WAITING;
    }

    @Override
    public final long attachmentAction(final int folderId, final int oid, final int uid, final Session session, final Context c, final int numberOfAttachments) throws OXException {
        int changes;
        PreparedStatement pst = null;
        int amount = 0;
        ResultSet rs = null;
        PreparedStatement prep = null;
        long last_modified = 0L;
        final Connection con = DBPool.pickupWriteable(c);
        try {
            con.setAutoCommit(false);
            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(96);
            sb.append("SELECT intfield08 FROM prg_dates WHERE intfield01=");
            sb.append(oid);
            sb.append(" AND cid=");
            sb.append(c.getContextId());
            sb.append(" FOR UPDATE");
            prep = getPreparedStatement(con, sb.toString());
            rs = prep.executeQuery();
            if (rs.next()) {
                amount = rs.getInt(1);
            } else {
                LOG.error("Object Not Found: " + "Unable to handle attachment action", new Throwable());
                throw OXException.notFound("");
            }
            amount += numberOfAttachments;
            if (amount < 0) {
                LOG.error(StringCollection.convertArraytoString(new Object[] { "Object seems to be corrupted: new number of attachments:", Autoboxing.I(amount), " oid:cid:uid ", Autoboxing.I(oid), Character.valueOf(CalendarOperation.COLON), Autoboxing.I(c.getContextId()), Character.valueOf(CalendarOperation.COLON), Autoboxing.I(uid) }), new Throwable());
                throw new OXException();
            }
            pst = con.prepareStatement("UPDATE prg_dates SET changing_date=?,changed_from=?,intfield08=? WHERE intfield01=? AND cid=?");
            last_modified = System.currentTimeMillis();
            pst.setLong(1, last_modified);
            pst.setInt(2, uid);
            pst.setInt(3, amount);
            pst.setInt(4, oid);
            pst.setInt(5, c.getContextId());
            changes = pst.executeUpdate();
            if (changes == 0) {
                LOG.error(StringCollection.convertArraytoString(new Object[] { "Object not found: attachmentAction: oid:cid:uid ", Autoboxing.I(oid), Character.valueOf(CalendarOperation.COLON), Autoboxing.I(c.getContextId()), Character.valueOf(CalendarOperation.COLON), Autoboxing.I(uid) }), new Throwable());
                throw OXException.notFound("");
            }
            LOG.debug(StringCollection.convertArraytoString(new Object[] { "Result of attachmentAction was ", Autoboxing.I(changes), ". Check prg_dates oid:cid:uid ", Autoboxing.I(oid), Character.valueOf(CalendarOperation.COLON), Autoboxing.I(c.getContextId()), Character.valueOf(CalendarOperation.COLON), Autoboxing.I(uid) }));
            con.commit();
        } catch (final SQLException sqle) {
            rollback(con);
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            closeSQLStuff(prep);
            closeSQLStuff(pst);
            autocommit(con);
            DBPool.closeWriterSilent(c, con);
        }
        final CalendarDataObject edao = new CalendarDataObject();
        edao.setParentFolderID(folderId);
        edao.setObjectID(oid);
        edao.setNumberOfAttachments(amount);
        final EventClient eventclient = new EventClient(session);
        try {
            eventclient.modify(edao);
        } catch (final OXException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e);
        }
        return last_modified;
    }

    private final boolean checkIfMasterIsOrphaned(final int oid, final int cid, final Context context) throws OXException, SQLException {
        Connection readcon = null;
        boolean ret = false;
        try {
            readcon = DBPool.pickup(context);
            final PreparedStatement pst = readcon.prepareStatement("SELECT 1 from del_dates_members WHERE object_id = ? AND cid = ?");
            ResultSet rs = null;
            try {
                pst.setInt(1, oid);
                pst.setInt(2, cid);
                rs = getResultSet(pst);
                ret = rs.next();
            } finally {
                COLLECTION.closeResultSet(rs);
                COLLECTION.closePreparedStatement(pst);
            }
        } finally {
            if (readcon != null) {
                DBPool.push(context, readcon);
            }
        }
        return ret;
    }

    private static final String SQL_CHECK_DEL_MASTER = "SELECT 1 FROM del_dates WHERE intfield01 = ? AND cid = ?";

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
                COLLECTION.closeResultSet(rs);
                COLLECTION.closePreparedStatement(pst);
            }
        } finally {
            if (readcon != null) {
                DBPool.push(context, readcon);
            }
        }
        return ret;
    }

    private static final String SQL_CHECK_DEL_PART = "SELECT 1 FROM del_dates_members WHERE object_id = ? AND cid = ? AND member_uid = ?";

    /**
     * Checks if specified participant is contained in participants' backup table.
     *
     * @param uid The participant's identifier
     * @param cid The context ID
     * @param oid The corresponding appointment's ID
     * @param context The context
     * @param con The database connection.
     * @return <code>true</code> if specified participant is contained in participants' backup table; otherwise <code>false</code>
     * @throws OXException If an OX error occurs
     * @throws SQLException If a SQL error occurs
     */
    private final boolean checkForDeletedParticipants(final int uid, final int cid, final int oid, final Context context, final Connection con) throws OXException, SQLException {
        Connection readcon = null;
        boolean pushCon = false;

        boolean ret = false;
        try {
            if (con != null) {
                readcon = con;
            } else {
                readcon = DBPool.pickup(context);
                pushCon = true;
            }

            final PreparedStatement pst = readcon.prepareStatement(SQL_CHECK_DEL_PART);
            ResultSet rs = null;
            try {
                pst.setInt(1, oid);
                pst.setInt(2, cid);
                pst.setInt(3, uid);
                rs = getResultSet(pst);
                ret = rs.next();
            } finally {
                COLLECTION.closeResultSet(rs);
                COLLECTION.closePreparedStatement(pst);
            }
        } finally {
            if (pushCon && readcon != null) {
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
            COLLECTION.closeResultSet(rs);
            COLLECTION.closePreparedStatement(pst);
        }
        return (mc == 1);
    }


    private final boolean checkIfParticipantIsInvited(final String mail, final int contextId, final int objectId, final Connection readcon) throws SQLException {
        final PreparedStatement pst = readcon.prepareStatement("SELECT objectId FROM dateExternal WHERE objectId = ? AND cid = ? AND mailAddress = ?");
        pst.setInt(1, objectId);
        pst.setInt(2, contextId);
        pst.setString(3, mail);
        final ResultSet resultSet = pst.executeQuery();
        final boolean isInvited = resultSet.next();
        pst.close();
        return isInvited;
    }

    private final long deleteOnlyOneParticipantInPrivateFolder(final int oid, final int cid, final int uid,
            final int fid, final Context c, final Connection writecon, final Session so) throws SQLException, OXException {
        CalendarVolatileCache.getInstance().invalidateGroup(String.valueOf(cid));
        final long lastModified = System.currentTimeMillis();
        final PreparedStatement pd = writecon
                .prepareStatement("delete from prg_dates_members WHERE object_id = ? AND cid = ? AND member_uid = ?");
        try {
            pd.setInt(1, oid);
            pd.setInt(2, cid);
            pd.setInt(3, uid);
            pd.addBatch();
            deleteReminder(oid, uid, c, writecon);

            pd.executeBatch();
        } finally {
            COLLECTION.closePreparedStatement(pd);
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
                final OXFolderAccess ofa = new OXFolderAccess(writecon, c);
                final int folderType = ofa.getFolderType(fid, so.getUserId());
                final PreparedStatement pidm = writecon
                        .prepareStatement("insert into del_dates (creating_date, created_from, changing_date, changed_from, fid, intfield01, cid, pflag) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                try {
                    pidm.setTimestamp(1, new Timestamp(lastModified));
                    pidm.setInt(2, uid);
                    pidm.setLong(3, lastModified);
                    pidm.setInt(4, 0);
                    if (folderType == FolderObject.PRIVATE) {
                        pidm.setInt(5, 0);
                    } else {
                        pidm.setInt(5, fid);
                    }
                    pidm.setInt(6, oid);
                    pidm.setInt(7, cid);
                    pidm.setInt(8, 0);
                    pidm.executeUpdate();
                    master_del_update = false;
                } finally {
                    COLLECTION.closePreparedStatement(pidm);
                }
            }
        } finally {
            COLLECTION.closePreparedStatement(pdr);
        }

        final PreparedStatement prepid = writecon
                .prepareStatement("DELETE FROM del_dates_members WHERE cid = ? AND object_id = ? AND member_uid = ?");
        try {
            prepid.setInt(1, cid);
            prepid.setInt(2, oid);
            prepid.setInt(3, uid);
            prepid.executeUpdate();
        } finally {
            COLLECTION.closePreparedStatement(prepid);
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
            COLLECTION.closePreparedStatement(pid);
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
            COLLECTION.closePreparedStatement(ma);
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
                COLLECTION.closePreparedStatement(ddu);
            }
        }
        final Appointment ao = new Appointment();
        ao.setObjectID(oid);
        ao.setParentFolderID(fid);
        COLLECTION.triggerEvent(so, CalendarOperation.UPDATE, ao);
        deleteReminder(oid, uid, c, writecon);

        return lastModified;
    }

    private static final String SQL_UPDATE_LAST_MODIFIED = "UPDATE prg_dates AS pd SET "
            + COLLECTION.getFieldName(Appointment.LAST_MODIFIED) + " = ?, "
            + COLLECTION.getFieldName(Appointment.MODIFIED_BY) + " = ? WHERE cid = ? AND "
            + COLLECTION.getFieldName(Appointment.OBJECT_ID) + " = ? AND "
            + COLLECTION.getFieldName(Appointment.LAST_MODIFIED) + " <= ?";

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
            COLLECTION.closePreparedStatement(stmt);
        }
    }

    private static final String SQL_SELECT_WHOLE_RECURRENCE = "SELECT "
			+ COLLECTION.getFieldName(Appointment.OBJECT_ID) + " FROM prg_dates WHERE cid = ? AND "
			+ COLLECTION.getFieldName(Appointment.RECURRENCE_ID) + " = ? ORDER BY "
			+ COLLECTION.getFieldName(Appointment.OBJECT_ID);

	private final void deleteOnlyOneRecurringParticipantInPrivateFolder(final int recurrenceId, final int cid,
			final int uid, final int fid, final Context c, final Connection writecon, final Session so)
			throws SQLException, OXException {
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
				COLLECTION.closeResultSet(rs);
				COLLECTION.closePreparedStatement(stmt);
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
    private static final void deleteReminder(final int oid, final int uid, final Context c) throws OXException {
        changeReminder(oid, uid, -1, c, false, null, null, CalendarOperation.DELETE, false);
    }

    private static final void deleteReminder(final int oid, final int uid, final Context c, final Connection con) throws OXException {
        changeReminder(oid, uid, -1, c, false, null, null, CalendarOperation.DELETE, false, con);
    }

    private static final void changeReminder(final int oid, final int uid, final int fid, final Context c, final boolean sequence, final java.util.Date end_date, final java.util.Date reminder_date, final int action, final boolean recurrenceChange) throws OXException {
        changeReminder(oid, uid, fid, c, sequence, end_date, reminder_date, action, recurrenceChange, null);
    }

    private static final void changeReminder(final int oid, final int uid, final int fid, final Context c, final boolean sequence, final java.util.Date end_date, final java.util.Date reminder_date, final int action, final boolean recurrenceChange, final Connection con) throws OXException {
        final ReminderService rsql = new ReminderHandler(c);
        if (action == CalendarOperation.DELETE || action == CalendarOperation.UPDATE && COLLECTION.isInThePast(end_date)) {
            if (rsql.existsReminder(oid, uid, Types.APPOINTMENT, con)) {
                try {
                    if (con != null) {
                        rsql.deleteReminder(oid, uid, Types.APPOINTMENT, con);
                    } else {
                        rsql.deleteReminder(oid, uid, Types.APPOINTMENT);
                    }
                } catch (final OXException exc) {
                    if (ReminderExceptionCode.NOT_FOUND.equals(exc)) {
                        LOG.debug("Reminder was not found for deletion", exc);
                    } else {
                        throw exc;
                    }
                }
            }
        } else {
            if (!COLLECTION.isInThePast(end_date)) {
                final ReminderObject ro = new ReminderObject();
                ro.setUser(uid);
                ro.setTargetId(oid);
                ro.setModule(Types.APPOINTMENT);
                ro.setRecurrenceAppointment(sequence);
                ro.setDate(reminder_date);
                ro.setFolder(fid);
                if (rsql.existsReminder(oid, uid, Types.APPOINTMENT, con)) {
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
                        if (rsql.loadReminder(oid, uid, Types.APPOINTMENT, con).getDate().getTime() < reminder_date
                                .getTime()) {
                            if (con != null) {
                                rsql.updateReminder(ro, con);
                            } else {
                                rsql.updateReminder(ro);
                            }

                        } else if (LOG.isDebugEnabled()) {
                            LOG.debug("No recurrence change! Leave corresponding reminder unchanged");
                        }
                    } else {
                        if (con != null) {
                            rsql.updateReminder(ro, con);
                        } else {
                            rsql.updateReminder(ro);
                        }
                    }
                } else {
                    if (con != null) {
                        rsql.insertReminder(ro, con);
                    } else {
                        rsql.insertReminder(ro);
                    }
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
    @Override
    public final void deleteAppointment(final int uid, final CalendarDataObject cdao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified) throws SQLException, OXException {
        deleteAppointment(uid, cdao, writecon, so, ctx, inFolder, clientLastModified, true);
    }

    @Override
   public final void deleteAppointment(final int uid, final CalendarDataObject cdao, final Connection writecon, final Session so, final Context ctx, final int inFolder, final java.util.Date clientLastModified, final boolean checkPermissions) throws SQLException, OXException {
        final Connection readcon = DBPool.pickup(ctx);
        final CalendarDataObject edao;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            final CalendarOperation co = new CalendarOperation();
            prep = getPreparedStatement(readcon, loadAppointment(cdao.getObjectID(), cdao.getContext()));
            rs = getResultSet(prep);
            edao = co.loadAppointment(rs, cdao.getObjectID(), inFolder, this, readcon, so, ctx, CalendarOperation.DELETE, inFolder, checkPermissions);
            if (edao.getRecurrenceType() == CalendarObject.NO_RECURRENCE && edao.getRecurrenceID() == 0) {
                if ((cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) || (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null)) {
                    throw OXCalendarExceptionCodes.NO_RECCURENCE.create();
                }
            }
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch (final OXException oxe) {
            throw oxe;
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closePreparedStatement(prep);
            DBPool.push(ctx, readcon);
        }
        if (clientLastModified == null) {
            throw OXCalendarExceptionCodes.LAST_MODIFIED_IS_NULL.create();
        } else if (edao.getLastModified() == null) {
            throw OXCalendarExceptionCodes.LAST_MODIFIED_IS_NULL.create();
        }

        if (edao.getLastModified().getTime() > clientLastModified.getTime()) {
            throw OXException.conflict();
        }

        deleteSingleAppointment(cdao.getContextID(), cdao.getObjectID(), uid, edao.getCreatedBy(), inFolder, null, writecon, edao.getFolderType(), so, ctx, COLLECTION.getRecurringAppointmentDeleteAction(cdao, edao), cdao, edao, clientLastModified);
        CalendarVolatileCache.getInstance().invalidateGroup(String.valueOf(cdao.getContextID()));

        if ((cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0)
                || (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null)) {
            CalendarDataObject mdao = edao;
            /*
             * Check if a change exception has been deleted
             */
            final int empty;
            boolean takeCareOfMaster = true;
            final boolean isChangeException = (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() != edao.getObjectID());
            if (isChangeException) {
                /*
                 * A change exception; load real appointment
                 */
                try {
                    mdao = FACTORY_REF.get().createAppointmentSql(so).getObjectById(edao.getRecurrenceID(), inFolder);
                } catch (final OXException e) {
                    if (e.getCode() == OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.getNumber()) {
                        LOG.debug("Unable to access Exception-Master (User-ID:" + uid + "/Folder-ID:" + inFolder + "/Exception-ID:" + cdao.getObjectID() + "/Master-ID" + edao.getRecurrenceID() + ")", e);
                        takeCareOfMaster = false;
                    } else {
                        throw e;
                    }
                }
                empty = 0;
            } else {
                empty = 1;
            }

            /*
             * Delete of a single appointment: delete exception
             */
            final RecurringResultsInterface rresults = COLLECTION.calculateRecurring(mdao, 0, 0, 0);
            if (takeCareOfMaster && rresults.size() == empty && (mdao.getChangeException() == null || (isChangeException ? mdao.getChangeException().length == 1 : mdao.getChangeException().length == 0))) {
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
                        CalendarCollectionService.RECURRING_NO_ACTION, mdao, mdao, clientLastModified);
            }
        }
    }

    @Override
    public void deleteAppointmentsInFolder(final Session so, final Context ctx, final ResultSet rs, final Connection readcon, final Connection writecon, final int foldertype, final int fid) throws SQLException, OXException {
        while (rs.next()) {
            final int oid = rs.getInt(1);
            final int owner = rs.getInt(2);
            deleteSingleAppointment(so.getContextId(), oid, so.getUserId(), owner, fid, readcon, writecon, foldertype, so, ctx, CalendarCollectionService.RECURRING_NO_ACTION, null, null, null);
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
        int folderOwner = new OXFolderAccess(ctx).getFolderOwner(fid);
        if ((foldertype == FolderObject.PRIVATE || (foldertype == FolderObject.SHARED && owner != folderOwner)) && uid != owner) {
            if (foldertype == FolderObject.SHARED) {
                uid = folderOwner;
            }
            // in a shared folder some other user tries to delete an appointment
            // created by the sharing user.
            boolean close_read = false;
            try {
                if (readcon == null || readcon.isClosed()) {
                    readcon = DBPool.pickup(ctx);
                    close_read = true;
                }
                if (!checkIfUserIstheOnlyParticipant(cid, oid, readcon) && recurring_action != CalendarCollectionService.RECURRING_VIRTUAL_ACTION) {
                    if (close_read && readcon != null) {
                        DBPool.push(ctx, readcon);
                        close_read = false;
                    }
                    if (COLLECTION.isRecurringMaster(edao == null ? cdao : edao)) {
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
                if (recurring_action == CalendarCollectionService.RECURRING_VIRTUAL_ACTION) {
                    // Create an exception first, remove the user as participant
                    // and then return
                    if (checkIfUserIstheOnlyParticipant(cid, oid, readcon)) {
                        createSingleVirtualDeleteException(cdao, edao, writecon, oid, uid, fid, so, ctx, clientLastModified);
                    } else {

                        edao.setRecurrencePosition(cdao.getRecurrencePosition());
                        edao.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
                        COLLECTION.setRecurrencePositionOrDateInDAO(edao);

                        final CalendarDataObject temp = edao.clone();
                        final RecurringResultsInterface rss = COLLECTION.calculateRecurring(temp, 0, 0, edao.getRecurrencePosition());
                        if (rss != null) {
                            final RecurringResultInterface rs = rss.getRecurringResult(0);
                            if (rs != null) {
                                edao.setStartDate(new Date(rs.getStart()));
                                edao.setEndDate(new Date(rs.getEnd()));

                            }
                        }

                        final Date deleted_exceptions[] = edao.getDeleteException();
                        final Date calculated_exception = edao.getRecurrenceDatePosition();
                        edao.removeDeleteExceptions();
                        edao.removeChangeExceptions();
                        edao.setChangeExceptions(new Date[] { calculated_exception });
                        COLLECTION.removeParticipant(edao, uid);
                        COLLECTION.removeUserParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        edao.setRecurrenceID(edao.getObjectID());
                        edao.removeObjectID();
                        try {
                            insertAppointment0(edao, writecon, so, false);
                        } catch (final RuntimeException e) {
                            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(8));
                        }
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        if (deleted_exceptions != null) {
                            final List<Date> asList = Arrays.asList(deleted_exceptions);
                            asList.remove(calculated_exception);
                            update.setDeleteExceptions(asList);
                        } else {
                            update.setDeleteExceptions((Date[]) null);
                        }
                        update.setModifiedBy(uid);
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid, writecon);
                            update.setChangeExceptions(COLLECTION.addException(ldao.getChangeException(), calculated_exception));
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final OXException le) {
                            throw le;
                        } catch (final RuntimeException e) {
                            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(9));
                        }
                    }
                    return;
                } else if (recurring_action == CalendarCollectionService.RECURRING_EXCEPTION_ACTION) {
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
                            edao.setRecurrenceDatePosition(new Date(COLLECTION.normalizeLong(edao.getStartDate().getTime())));
                        }
                        //update.setChangeExceptions(collection.removeException(edao.getChangeException(), edao.getRecurrenceDatePosition()));
                        //update.setDeleteExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid, writecon);
                            update.setChangeExceptions(COLLECTION.removeException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            update.setDeleteExceptions(COLLECTION.addException(ldao.getDeleteException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final RuntimeException e) {
                            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(11));
                        }
                    } else {
                        if (close_read && readcon != null) {
                            DBPool.push(ctx, readcon);
                            close_read = false;
                        }
                        // remove participant (update)
                        COLLECTION.removeParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setModifiedBy(uid);
                        if ((!edao.containsRecurrenceDatePosition() || edao.getRecurrenceDatePosition() == null)) {
                            /*
                             * Determine recurrence date position
                             */
                            edao.setRecurrenceDatePosition(new Date(COLLECTION.normalizeLong(edao.getStartDate().getTime())));
                        }
                        //update.setChangeExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid, writecon);
                            update.setChangeExceptions(COLLECTION.addException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(edao, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // EXCEPTION
                            // OBJECT
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final RuntimeException e) {
                            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(10));
                        }
                    }
                    if (edao.getRecurrenceID() > 0 && !cdao.containsRecurrenceID()) {
                        cdao.setRecurrenceID(edao.getRecurrenceID());
                    }
                    return;
                }
            } catch (final SQLException sqle) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
            } catch (final OXException oxe) {
                throw oxe;
            } finally {
                if (close_read && readcon != null) {
                    DBPool.push(ctx, readcon);
                }
            }
        }

        if (recurring_action == CalendarCollectionService.RECURRING_VIRTUAL_ACTION) {
            // this is an update with a new delete_exception
            if (edao == null) {
                throw OXCalendarExceptionCodes.RECURRING_UNEXPECTED_DELETE_STATE.create(Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(-1));
            }
            createSingleVirtualDeleteException(cdao, edao, writecon, oid, uid, fid, so, ctx, clientLastModified);
            return;
        } else if (recurring_action == CalendarCollectionService.RECURRING_EXCEPTION_ACTION) {
            // this is a deletion of a change exception aka existing exception
            if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0) {
                // Necessary recurrence ID is present
                boolean close_read = false;
                try {
                    if (readcon == null || readcon.isClosed()) {
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
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid, writecon);
                            update.setChangeExceptions(COLLECTION.removeException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            update.setDeleteExceptions(COLLECTION.addException(ldao.getDeleteException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJfinal ECT
                        } catch (final OXException le) {
                            if (le.isGeneric(Generic.NOT_FOUND)) {
                                LOG.info("Unable to find master during Exception delete. Ignoring. Seems to be corrupt data.", le);
                                final long modified = deleteAppointment(writecon, cid, oid, uid);

                                if (edao == null) {
                                    triggerDeleteEvent(writecon, oid, fid, so, ctx, null);
                                } else {
                                    edao.setModifiedBy(uid);
                                    edao.setLastModified(new Date(modified));
                                    triggerDeleteEvent(writecon, oid, fid, so, ctx, edao);
                                }
                            } else {
                                throw le;
                            }
                        } catch (final RuntimeException e) {
                            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(11));
                        }
                    } else {
                        if (close_read && readcon != null) {
                            DBPool.push(ctx, readcon);
                            close_read = false;
                        }
                        // remove participant (update)
                        COLLECTION.removeParticipant(edao, uid);
                        edao.setModifiedBy(uid);
                        final CalendarDataObject update = new CalendarDataObject();
                        update.setContext(ctx);
                        update.setObjectID(edao.getRecurrenceID());
                        update.setModifiedBy(uid);
                        if ((!edao.containsRecurrenceDatePosition() || edao.getRecurrenceDatePosition() == null)) {
                            /*
                             * Determine recurrence date position
                             */
                            edao.setRecurrenceDatePosition(new Date(COLLECTION.normalizeLong(edao.getStartDate().getTime())));
                        }
                        //update.setChangeExceptions(new java.util.Date[] { edao.getRecurrenceDatePosition() });
                        try {
                            final CalendarDataObject ldao = loadObjectForUpdate(update, so, ctx, fid, writecon);
                            update.setChangeExceptions(COLLECTION.addException(ldao.getChangeException(), edao.getRecurrenceDatePosition()));
                            updateAppointment(edao, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // EXCEPTION
                            // OBJECT
                            updateAppointment(update, ldao, writecon, so, ctx, fid, clientLastModified, false, false); // MAIN
                            // OBJECT
                        } catch (final RuntimeException e) {
                            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(10));
                        }
                    }
                    if (edao.getRecurrenceID() > 0 && !cdao.containsRecurrenceID()) {
                        cdao.setRecurrenceID(edao.getRecurrenceID());
                    }
                    return;
                } catch (final SQLException sqle) {
                    throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
                } catch (final OXException oxe) {
                    throw oxe;
                } finally {
                    if (close_read && readcon != null) {
                        DBPool.push(ctx, readcon);
                    }
                }
            }
        } else if (recurring_action == CalendarCollectionService.RECURRING_FULL_DELETE) {
            final List<Integer> al = getExceptionList(readcon, ctx, edao.getRecurrenceID());
            if (al != null && !al.isEmpty()) {
                final Integer oids[] = al.toArray(new Integer[al.size()]);
                if (oids.length > 0) {
                    deleteAllRecurringExceptions(oids, so, writecon);
                }
                for (int a = 0; a < al.size(); a++) {
                    triggerDeleteEvent(writecon, al.get(a).intValue(), fid, so, ctx, null);
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
            triggerDeleteEvent(writecon, oid, fid, so, ctx, null);
        } else {
            edao.setModifiedBy(uid);
            edao.setLastModified(new Date(modified));
            triggerDeleteEvent(writecon, oid, fid, so, ctx, edao);
        }
    }

    private final void triggerDeleteEvent(final Connection con, final int oid, final int fid, final Session so, final Context ctx, final CalendarDataObject edao) throws OXException {
        final CalendarDataObject ao;
        if (edao == null) {
            ao = new CalendarDataObject();
        } else {
            ao = edao.clone();
        }
        ao.setObjectID(oid);
        ao.setParentFolderID(fid);
        if (userIsOrganizer(so.getUserId(), ao)) {
            COLLECTION.triggerEvent(so, CalendarOperation.DELETE, ao);
        }
        // deleteAllReminderEntries(edao, oid, fid, so, readcon);
        final ReminderService rsql = new ReminderHandler(ctx);
        try {
            rsql.deleteReminder(oid, Types.APPOINTMENT, con);
        } catch (final OXException oxe) {
            // this is wanted if Code = Code.NOT_FOUND
            if (!ReminderExceptionCode.NOT_FOUND.equals(oxe)) {
                throw oxe;
            }
        }
    }

    private final void createSingleVirtualDeleteException(final CalendarDataObject cdao, final CalendarDataObject edao, final Connection writecon, final int oid, final int uid, final int fid, final Session so, final Context ctx, final java.util.Date clientLastModified) throws SQLException, OXException {
        final CalendarDataObject udao = new CalendarDataObject();
        udao.setObjectID(oid);
        udao.setContext(ctx);
        udao.setModifiedBy(uid);
        java.util.Date de = null;
        if (cdao.containsRecurrenceDatePosition()) {
            de = cdao.getRecurrenceDatePosition();
        } else {
            final long del = COLLECTION.getLongByPosition(edao, cdao.getRecurrencePosition());
            if (del != 0) {
                de = new java.util.Date(del);
            }
        }
        //udao.setDeleteExceptions(new java.util.Date[] {de});
        if (de == null) {
            throw OXCalendarExceptionCodes.RECURRING_UNEXPECTED_DELETE_STATE.create(Integer.valueOf(uid), Integer.valueOf(oid), Integer.valueOf(cdao.getRecurrencePosition()));
        }
        try {
            final CalendarDataObject ldao = loadObjectForUpdate(udao, so, ctx, fid, writecon);
            udao.setDeleteExceptions(COLLECTION.addException(ldao.getDeleteException(), de));
            updateAppointment(udao, ldao, writecon, so, ctx, fid, clientLastModified, false, true);
            cdao.setLastModified(udao.getLastModified());
        } catch (final OXException oxe) {
            throw oxe;
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

            stmt = writecon.prepareStatement(SQL.DELETE_BACKUPED_EXTERNAL);
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

                stmt = writecon.prepareStatement(SQL.BACKUP_EXTERNAL);
                pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, oid);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
            }

            stmt = writecon.prepareStatement(SQL.DELETE_EXTERNAL_FOR_APPOINTMENT);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

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
            COLLECTION.closePreparedStatement(stmt);
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
            COLLECTION.closePreparedStatement(stmt);
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
            if (rcon == null || rcon.isClosed()) {
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
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closePreparedStatement(prep);
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
            final com.openexchange.java.StringAllocator query = new com.openexchange.java.StringAllocator(128);
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
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            COLLECTION.closeResultSet(rs);
            COLLECTION.closePreparedStatement(prep);
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

    @Override
    public int resolveUid(final Session session, final String uid) throws OXException {
        return resolveByField(session, "uid", uid);
    }

    @Override
    public int resolveFilename(Session session, String filename) throws OXException {
        return resolveByField(session, "filename", filename);
    }

    /**
     * Gets the object ID of an appointment whose value in a specific column matches another value. The comparison is case-sensitive,
     * and exceptions from recurring appointments are not taken into account. If there are more than one matches, the first one is
     * returned.
     *
     * @param session The current session
     * @param columnName The column name
     * @param value The value to match
     * @return The appointment's object ID, or <code>0</code> if no matching appointment was found
     * @throws OXException
     */
    private static int resolveByField(Session session, String columnName, String value) throws OXException {
        Context ctx = Tools.getContext(session);
        SELECT s = new SELECT("intfield01", columnName).FROM("prg_dates").
            WHERE(new EQUALS(columnName, PLACEHOLDER).
                AND(new EQUALS("cid", PLACEHOLDER)).
                AND(new OR(new ISNULL("intfield02"), new EQUALS(new Column("intfield01"), new Column("intfield02"))))
            );

        List<Object> params = new ArrayList<Object>();
        params.add(value);
        params.add(ctx.getContextId());

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DBPool.pickup(ctx);
            stmt = new StatementBuilder().prepareStatement(connection, s, params);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String actualValue = rs.getString(2);
                if (null == value && rs.wasNull() || null != value && value.equals(actualValue)) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } finally {
            DBUtils.closeResources(rs, stmt, null, true, ctx);
            DBPool.push(ctx, connection);
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.calendar.CalendarSqlImp#getFolder(com.openexchange.session.Session, int)
     */
    @Override
    public int getFolder(final Session session, final int objectId) throws OXException {
        final Context ctx = Tools.getContext(session);

        final SELECT s = new SELECT("pfid")
            .FROM("prg_dates_members")
            .WHERE(new EQUALS("cid", PLACEHOLDER)
                .AND(new EQUALS("object_id", PLACEHOLDER)
                .AND(new EQUALS("member_uid", PLACEHOLDER)
            ))
        );

        final List<Object> params = new ArrayList<Object>();
        params.add(ctx.getContextId());
        params.add(objectId);
        params.add(session.getUserId());

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DBPool.pickup(ctx);
            stmt = new StatementBuilder().prepareStatement(connection, s, params);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } finally {
            DBUtils.closeResources(rs, stmt, null, true, ctx);
            DBPool.push(ctx, connection);
        }
        return 0;
    }

    @Override
    public int countAppointments(Session session) throws OXException {
        SELECT select = new SELECT(new COUNT(ASTERISK)).FROM("prg_dates").WHERE(new EQUALS("cid", PLACEHOLDER));
        List<Object> params = new ArrayList<Object>();
        Context ctx = Tools.getContext(session);
        params.add(ctx.getContextId());

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DBPool.pickup(ctx);
            stmt = new StatementBuilder().prepareStatement(connection, select, params);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } finally {
            DBUtils.closeResources(rs, stmt, null, true, ctx);
            DBPool.push(ctx, connection);
        }

        return 0;
    }


    @Override
    public int countObjectsInFolder(Session session, int folderId, int folderType, EffectivePermission permission) throws OXException {
        Context ctx = Tools.getContext(session);

        SELECT select = new SELECT(new COUNT(ASTERISK));
        List<Object> values = new ArrayList<Object>();
        values.add(ctx.getContextId());
        values.add(folderId);
        if (folderType == FolderObject.PUBLIC) {
            if (permission.canReadAllObjects()) {
                select.FROM("prg_dates").WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("fid", PLACEHOLDER)));
            } else if (permission.canReadOwnObjects()) {
                select.FROM("prg_dates").WHERE(
                    new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("fid", PLACEHOLDER).AND(new EQUALS("created_from", session.getUserId()))));
                values.add(session.getUserId());
            } else {
                return 0; // Cannot see any objects.
            }
        } else if (folderType == FolderObject.SHARED) {
            if (permission.canReadAllObjects()) {
                select.FROM("prg_dates_members").WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("pfid", PLACEHOLDER)));
            } else if (permission.canReadOwnObjects()) {
                return 0; // Cannot have "own" objects in shared folders.
            } else {
                return 0; // Cannot see any objects.
            }
        } else if (folderType == FolderObject.PRIVATE) {
            select.FROM("prg_dates_members").WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("pfid", PLACEHOLDER))); // Can always see everything.
        }

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DBPool.pickup(ctx);
            stmt = new StatementBuilder().prepareStatement(connection, select, values);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } finally {
            DBUtils.closeResources(rs, stmt, null, true, ctx);
            DBPool.push(ctx, connection);
        }

        return 0;
    }

    public static void setServiceLookup(ServiceLookup serviceLookup) {
        CalendarMySQL.SERVICES_REF.set(serviceLookup);
    }
}
