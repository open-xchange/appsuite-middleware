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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarConfig;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ParticipantsDiffer;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.exceptions.SimpleTruncatedAttribute;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CalendarSql} - The implementation of {@link AppointmentSQLInterface}.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> (some refactoring)
 */
public class CalendarSql implements AppointmentSQLInterface {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarSql.class);

    public static final String default_class = "com.openexchange.calendar.CalendarMySQL";

    public static final String ERROR_PUSHING_DATABASE = "error pushing readable connection";

    public static final String ERROR_PUSHING_WRITEABLE_CONNECTION = "error pushing writeable connection";

    public static final String DATES_TABLE_NAME = "prg_dates";

    public static final String VIEW_TABLE_NAME = "prg_date_rights";

    public static final String PARTICIPANT_TABLE_NAME = "prg_dates_members";

    protected final static int EXCEPTION_NOT_FOUND = -1;

    private static final Set<Integer> EXEMPT = new HashSet<Integer>(Arrays.asList(
        Appointment.RECURRENCE_START,
        Appointment.ALARM,
        Appointment.RECURRENCE_DATE_POSITION,
        Appointment.DAYS,
        Appointment.DAY_IN_MONTH,
        Appointment.MONTH,
        Appointment.INTERVAL,
        Appointment.UNTIL,
        Appointment.NOTIFICATION,
        Appointment.RECURRENCE_COUNT,
        Appointment.LAST_MODIFIED_UTC));

    public static final int[] EXCEPTION_FIELDS = new int[Appointment.ALL_COLUMNS.length - EXEMPT.size()];

    private static volatile int MAX_SEARCH_FOLDER = 100;

    private static final AtomicReference<ContactCollectorService> SERVICES_REF = new AtomicReference<ContactCollectorService>();

    public static void setContactCollectorService(ContactCollectorService contactCollectorService) {
        CalendarSql.SERVICES_REF.set(contactCollectorService);
    }

    static {
        int i = 0;
        for (final int col : Appointment.ALL_COLUMNS) {
            if (!EXEMPT.contains(col)) {
                EXCEPTION_FIELDS[i++] = col;
            }
        }
    }

    private static volatile CalendarSqlImp cimp;

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------------- //

    private final Session session;
    private final CalendarCollection calendarCollection;
    private boolean includePrivateAppointments;

    /**
     * Initializes a new {@link CalendarSql}.
     *
     * @param session The session providing needed user data
     */
    public CalendarSql(final Session session) {
        this.session = session;
        this.calendarCollection = new CalendarCollection();
    }

    @Override
    public boolean[] hasAppointmentsBetween(final Date d1, final Date d2) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        final UserConfiguration userConfiguration = Tools.getUserConfiguration(ctx, session.getUserId());
        Connection readcon = null;
        try {
            readcon = DBPool.pickup(ctx);
            return cimp.getUserActiveAppointmentsRangeSQL(ctx, session.getUserId(), user.getGroups(), userConfiguration, d1, d2, readcon);
        } catch (final OXException e) {
            // Don't mask OX exceptions in a SQL exception.
            throw e;
        } catch(final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, e.getMessage());
        } finally {
            if (readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(final int fid, final int[] cols, final Date start, final Date end, final int orderBy, final Order orderDir) throws OXException, SQLException {
        return getAppointmentsBetweenInFolder(fid, cols, start, end, 0, 0, orderBy, orderDir);
    }


    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(final int fid, int[] cols, final Date start, final Date end, final int from, final int to, final int orderBy, final Order orderDir) throws OXException, SQLException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        try {
            readcon = DBPool.pickup(ctx);
            cols = calendarCollection.checkAndAlterCols(cols);
            final OXFolderAccess ofa = new OXFolderAccess(readcon, ctx);
            final int folderType = ofa.getFolderType(fid, session.getUserId());
            final CalendarOperation co = new CalendarOperation();
            final EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), userConfig);

            mayRead(oclp);

            final CalendarSqlImp cimp = CalendarSql.cimp;
            if (folderType == FolderObject.PRIVATE) {
                prep = cimp.getPrivateFolderRangeSQL(ctx, session.getUserId(), user.getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, orderBy, orderDir);
            } else if (folderType == FolderObject.PUBLIC) {
                prep = cimp.getPublicFolderRangeSQL(ctx, session.getUserId(), user.getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, orderBy, orderDir);
            } else {
                final int shared_folder_owner = ofa.getFolderOwner(fid);
                prep = cimp.getSharedFolderRangeSQL(ctx, session.getUserId(), shared_folder_owner, user.getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, orderBy, orderDir, doesIncludePrivateAppointments());
            }

            rs = cimp.getResultSet(prep);
            co.setRequestedFolder(fid);
            co.setResultSet(rs, prep, cols, cimp, readcon, from, to, session, ctx);
            close_connection = false;
            return new AppointmentIteratorAdapter(new AnonymizingIterator(co, ctx, session.getUserId()));

        } catch (final IndexOutOfBoundsException ioobe) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(ioobe, Integer.valueOf(19));
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch (final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(20));
        } finally  {
            if (close_connection) {
                calendarCollection.closeResultSet(rs);
                calendarCollection.closePreparedStatement(prep);
            }
            if (readcon != null && close_connection) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    /**
     * @return
     */
    private boolean doesIncludePrivateAppointments() {
        return includePrivateAppointments;
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(final int fid, final Date start, final Date end, int[] cols, final Date since) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        try {
            readcon = DBPool.pickup(ctx);
            cols = calendarCollection.checkAndAlterCols(cols);
            final OXFolderAccess ofa = new OXFolderAccess(readcon, ctx);
            final int folderType = ofa.getFolderType(fid, session.getUserId());
            final CalendarOperation co = new CalendarOperation();
            final EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), userConfig);
            final int shared_folder_owner = ofa.getFolderOwner(fid);
            mayRead(oclp);

            final CalendarSqlImp cimp = CalendarSql.cimp;
            if (folderType == FolderObject.PRIVATE) {
                prep = cimp.getPrivateFolderModifiedSinceSQL(ctx, session.getUserId(), user.getGroups(), fid, since, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, start, end);
            } else if (folderType == FolderObject.PUBLIC) {
                prep = cimp.getPublicFolderModifiedSinceSQL(ctx, session.getUserId(), user.getGroups(), fid, since, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, start, end);
            } else {
                prep = cimp.getSharedFolderModifiedSinceSQL(ctx, session.getUserId(), shared_folder_owner, user.getGroups(), fid, since, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, start, end, !this.includePrivateAppointments);
            }
            rs = cimp.getResultSet(prep);
            co.setRequestedFolder(fid);
            co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session, ctx);
            close_connection = false;
            if(includePrivateAppointments) {
                return new AppointmentIteratorAdapter(new AnonymizingIterator(co, ctx, session.getUserId()));
            }
            return new AppointmentIteratorAdapter(new CachedCalendarIterator(co, ctx, session.getUserId()));
        } catch (final IndexOutOfBoundsException ioobe) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(ioobe, I(21));
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch (final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(22));
        } finally {
            if (close_connection) {
                calendarCollection.closeResultSet(rs);
                calendarCollection.closePreparedStatement(prep);
            }
            if (readcon != null && close_connection) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    private void mayRead(final EffectivePermission oclp) throws OXException {
        if (!oclp.canReadAllObjects() && !oclp.canReadOwnObjects()) {
            throw OXCalendarExceptionCodes.NO_PERMISSION.create(I(oclp.getFuid()));
        }
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(final int fid, final int cols[], final Date since) throws OXException {
        return getModifiedAppointmentsInFolder(fid, null, null, cols, since);
    }

    @Override
    public SearchIterator<Appointment> getDeletedAppointmentsInFolder(final int fid, int cols[], final Date since) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(session);
        final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        try {
            readcon = DBPool.pickup(ctx);
            cols = calendarCollection.checkAndAlterColsForDeleted(cols);
            final OXFolderAccess ofa = new OXFolderAccess(readcon, ctx);
            final EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), userConfig);
            mayRead(oclp);
            final CalendarSqlImp cimp = CalendarSql.cimp;
            if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                final CalendarOperation co = new CalendarOperation();
                prep = cimp.getPrivateFolderDeletedSinceSQL(ctx, session.getUserId(), fid, since, StringCollection.getSelect(cols, "del_dates"), readcon);
                rs = cimp.getResultSet(prep);
                co.setRequestedFolder(fid);
                co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session, ctx);
                close_connection = false;
                return new AppointmentIteratorAdapter(new CachedCalendarIterator(co, ctx, session.getUserId()));
            } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                final CalendarOperation co = new CalendarOperation();
                prep = cimp.getPublicFolderDeletedSinceSQL(ctx, session.getUserId(), fid, since, StringCollection.getSelect(cols, "del_dates"), readcon);
                rs = cimp.getResultSet(prep);
                co.setRequestedFolder(fid);
                co.setResultSet(rs, prep,cols, cimp, readcon, 0, 0, session, ctx);
                close_connection = false;
                return new AppointmentIteratorAdapter(new CachedCalendarIterator(co, ctx, session.getUserId()));
            } else {
                final CalendarOperation co = new CalendarOperation();
                final int shared_folder_owner = ofa.getFolderOwner(fid);
                prep = cimp.getSharedFolderDeletedSinceSQL(ctx, session.getUserId(), shared_folder_owner, fid, since, StringCollection.getSelect(cols, "del_dates"), readcon);
                rs = cimp.getResultSet(prep);
                co.setRequestedFolder(fid);
                co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session, ctx);
                close_connection = false;
                return new AppointmentIteratorAdapter(new CachedCalendarIterator(co, ctx, session.getUserId()));
            }
        } catch (final IndexOutOfBoundsException ioobe) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(ioobe, Integer.valueOf(23));
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch (final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(24));
        } finally {
            if (close_connection) {
                calendarCollection.closeResultSet(rs);
                calendarCollection.closePreparedStatement(prep);
            }
            if (readcon != null && close_connection) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public long getSequenceNumber(int folderId) throws OXException {
        if (null == session) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        Context ctx = Tools.getContext(session);
        UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        try {
            readcon = DBPool.pickup(ctx);
            OXFolderAccess ofa = new OXFolderAccess(readcon, ctx);
            mayRead(ofa.getFolderPermission(folderId, session.getUserId(), userConfig));
            CalendarSqlImp cimp = CalendarSql.cimp;

            if (FolderObject.PUBLIC == ofa.getFolderType(folderId, session.getUserId())) {
                prep = cimp.getPublicFolderSequenceNumber(ctx.getContextId(), folderId, readcon);
            } else if (FolderObject.PRIVATE == ofa.getFolderType(folderId, session.getUserId())) {
                prep = cimp.getPrivateFolderSequenceNumber(ctx.getContextId(), session.getUserId(), folderId, readcon);
            } else {
                int shared_folder_owner = ofa.getFolderOwner(folderId);
                prep = cimp.getPrivateFolderSequenceNumber(ctx.getContextId(), shared_folder_owner, folderId, readcon);
            }
            rs = cimp.getResultSet(prep);
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            calendarCollection.closeResultSet(rs);
            calendarCollection.closePreparedStatement(prep);
            if (readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public CalendarDataObject getObjectById(final int oid) throws OXException, SQLException {
        return getObjectById(oid, 0, null, false);
    }

    @Override
    public CalendarDataObject getObjectById(final int oid, final int inFolder) throws OXException, SQLException {
        return getObjectById(oid, inFolder, null, true);
    }

    /**
     * Gets the appointment denoted by specified object ID in given folder
     *
     * @param oid The object ID
     * @param inFolder The folder ID
     * @param readcon A connection with read capability (leave to <code>null</code> to fetch from pool)
     * @return The appointment object
     * @throws OXException
     * @throws OXObjectNotFoundException
     * @throws OXPermissionException
     */
    private CalendarDataObject getObjectById(final int oid, final int inFolder, final Connection readcon, final boolean checkPermissions) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection rcon = readcon;
        boolean closeRead = false;
        PreparedStatement prep = null;
        ResultSet rs = null;
        final Context ctx = Tools.getContext(session);
        try {
            if (rcon == null) {
                rcon = DBPool.pickup(ctx);
                closeRead = true;
            }
            final CalendarOperation co = new CalendarOperation();
            final CalendarSqlImp cimp = CalendarSql.cimp;
            prep = cimp.getPreparedStatement(rcon, cimp.loadAppointment(oid, ctx));
            rs = cimp.getResultSet(prep);
            final CalendarDataObject cdao = co.loadAppointment(rs, oid, inFolder, cimp, rcon, session, ctx, CalendarOperation.READ, inFolder, checkPermissions);
            calendarCollection.safelySetStartAndEndDateForRecurringAppointment(cdao);
            return cdao;
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            calendarCollection.closeResultSet(rs);
            calendarCollection.closePreparedStatement(prep);
            if (closeRead && rcon != null) {
                DBPool.push(ctx, rcon);
            }
        }
    }

    @Override
    public CalendarDataObject[] insertAppointmentObject(final CalendarDataObject cdao) throws OXException {
        RecurrenceChecker.check(cdao);
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection writecon = null;
        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        boolean modificationPerformed = false;
        try {
            final CalendarOperation co = new CalendarOperation();
            if (cdao.containsRecurrenceType()) {
                calendarCollection.checkRecurring(cdao);
            }
            if (co.prepareUpdateAction(cdao, null, session.getUserId(), cdao.getParentFolderID(), user.getTimeZone())) {
                try {
                    final OXFolderAccess ofa = new OXFolderAccess(ctx);
                    final EffectivePermission oclp = ofa.getFolderPermission(cdao.getEffectiveFolderId(), session.getUserId(), userConfig);
                    if (oclp.canCreateObjects()) {
                        calendarCollection.checkForInvalidCharacters(cdao);
                        cdao.setActionFolder(cdao.getParentFolderID());
                        final ConflictHandler ch = new ConflictHandler(cdao, null, session, true);
                        final CalendarDataObject conflicts[] = ch.getConflicts();
                        if (conflicts.length == 0) {
                            writecon = DBPool.pickupWriteable(ctx);
                            writecon.setAutoCommit(false);
                            try {
                                final CalendarDataObject[] appointments = cimp.insertAppointment(cdao, writecon, session);
                                collectAddresses(cdao, false);
                                modificationPerformed = true;
                                return appointments;
                            } catch(final DataTruncation dt) {
                                final String fields[] = DBUtils.parseTruncatedFields(dt);
                                final int fid[] = new int[fields.length];
                                final OXException oxe = OXCalendarExceptionCodes.TRUNCATED_SQL_ERROR.create();
                                int id = -1;
                                for (int a = 0; a < fid.length; a++) {
                                    id = calendarCollection.getFieldId(fields[a]);
                                    final String value = calendarCollection.getString(cdao, id);
                                    if(value == null) {
                                        oxe.addTruncatedId(id);
                                    } else {
                                        final int valueLength = Charsets.getBytes(value, Charsets.UTF_8).length;
                                        final int maxLength = DBUtils.getColumnSize(writecon, "prg_dates", fields[a]);
                                        oxe.addProblematic(new SimpleTruncatedAttribute(id, maxLength, valueLength, value));
                                    }
                                }
                                throw oxe;
                            }
                        }
                        return conflicts;
                    }
                    throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_6.create();
                } catch (OXException e) {
                    Databases.rollback(writecon);
                    throw e;
                } catch(final SQLException sqle) {
                    Databases.rollback(writecon);
                    throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
                } catch(final RuntimeException re) {
                    Databases.rollback(writecon);
                    throw re;
                } finally {
                    Databases.autocommit(writecon);
                }
            }
            throw OXCalendarExceptionCodes.INSERT_WITH_OBJECT_ID.create();
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(25));
        } finally {
            if (writecon != null) {
                if (modificationPerformed) {
                    DBPool.pushWrite(ctx, writecon);
                } else {
                    DBPool.pushWriteAfterReading(ctx, writecon);
                }
            }
        }
    }

    /**
     * Tries to add addresses of external participants to the ContactCollector.
     *
     * @param cdao - the {@link CalendarDataObject} to collect addresses for
     */
    private void collectAddresses(CalendarDataObject cdao, boolean incrementUseCount) {
        if (cdao == null) {
            LOG.info("Provided CalendarDataObject object is null. Nothing to collect for the ContactCollector!");
            return;
        }

        ContactCollectorService contactCollectorService = SERVICES_REF.get();
        if ((contactCollectorService != null) && (cdao.getParticipants().length > 0)) {
            Participant[] participants = cdao.getParticipants();

            List<InternetAddress> addresses = new ArrayList<InternetAddress>(participants.length);
            for (Participant participant : participants) {
                String emailAddress = participant.getEmailAddress();
                if ((participant.getType() == Participant.EXTERNAL_USER) && (emailAddress != null)) {
                    try {
                        addresses.add(new InternetAddress(emailAddress));
                    } catch (AddressException addressException) {
                        LOG.warn("Unable to add address " + emailAddress + " to ContactCollector.", addressException);
                    }
                }
            }

            contactCollectorService.memorizeAddresses(addresses, incrementUseCount, session);
        }
    }

    /**
     * Tries to add addresses of external participants to the ContactCollector.
     *
     * @param addresses - List of addresses to be collected
     */
    private void collectAddresses(List<Object> participants, boolean incrementUseCount) {
        if (participants == null) {
            LOG.info("Provided list with participants is null. Nothing to collect for the ContactCollector!");
            return;
        }

        ContactCollectorService contactCollectorService = SERVICES_REF.get();
        if ((contactCollectorService != null) && (participants.size() > 0)) {

            List<InternetAddress> addresses = new ArrayList<InternetAddress>(participants.size());
            for (Object participant : participants) {
                if (participant instanceof ExternalUserParticipant) {
                    ExternalUserParticipant externalUserParticipant = (ExternalUserParticipant) participant;
                    String emailAddress = externalUserParticipant.getEmailAddress();
                    try {
                        if (emailAddress != null) {
                            addresses.add(new InternetAddress(emailAddress));
                        }
                    } catch (AddressException addressException) {
                        LOG.warn("Unable to add address " + emailAddress + " to ContactCollector.", addressException);
                    }
                }
            }

            contactCollectorService.memorizeAddresses(addresses, incrementUseCount, session);
        }
    }

    @Override
    public CalendarDataObject[] updateAppointmentObject(final CalendarDataObject cdao, final int inFolder, final Date clientLastModified) throws OXException {
        return updateAppointmentObject(cdao, inFolder, clientLastModified, true);
    }

    @Override
    public CalendarDataObject[] updateAppointmentObject(final CalendarDataObject cdao, final int inFolder, final Date clientLastModified, final boolean checkPermissions) throws OXException {
        RecurrenceChecker.check(cdao);
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection writecon = null;
        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        try {
            final CalendarOperation co;
            final CalendarSqlImp cimp;
            final CalendarDataObject edao;
            Connection readcon = DBPool.pickup(ctx);
            try {
                co = new CalendarOperation();
                cimp = CalendarSql.cimp;
                edao = cimp.loadObjectForUpdate(cdao, session, ctx, inFolder, readcon, checkPermissions);
            } finally {
                DBPool.push(ctx, readcon);
            }

            if (cdao.isIgnoreOutdatedSequence() && cdao.getSequence() < edao.getSequence()) {
                // Silently ignore updates on Appointments with an outdated Sequence. OLOX2-Requirement.
                cdao.setLastModified(edao.getLastModified());
                LOG.info("Ignored update on Appointment due to outdated sequence: {}-{} (cid-objectId)", edao.getContextID(), edao.getObjectID());
                return null;
            }

            String timezone = edao.getTimezone() == null ? user.getTimeZone() : edao.getTimezone();
            if (co.prepareUpdateAction(cdao, edao, session.getUserId(), inFolder, timezone)) {
                // Insert-through-update detected
                throw OXCalendarExceptionCodes.UPDATE_WITHOUT_OBJECT_ID.create();
            }
            calendarCollection.checkForInvalidCharacters(cdao);
            final CalendarDataObject[] conflicts;
            {
                final CalendarDataObject conflict_dao = calendarCollection.fillFieldsForConflictQuery(cdao, edao, false);
                final ConflictHandler ch = new ConflictHandler(conflict_dao, edao, session, false);
                conflicts = ch.getConflicts();
            }
            if (conflicts.length == 0) {
                // Check user participants completeness
                if (cdao.containsUserParticipants()) {
                    final UserParticipant[] edaoUsers = edao.getUsers();
                    final List<UserParticipant> origUsers = Arrays.asList(edaoUsers);

                    for (final UserParticipant cur : cdao.getUsers()) {
                        if (cur.containsAlarm() && cur.containsConfirm()) {
                            continue;
                        }

                        // Get corresponding user from edao
                        final int index = origUsers.indexOf(cur);
                        if (index != -1) {
                            final UserParticipant origUser = origUsers.get(index);
                            if (!cur.containsConfirm()) {
                                cur.setConfirm(origUser.getConfirm());
                            }
                            if (!cur.containsAlarm()) {
                                cur.setAlarmMinutes(origUser.getAlarmMinutes());
                            }
                        }
                    }
                }

                writecon = DBPool.pickupWriteable(ctx);
                try {
                    writecon.setAutoCommit(false);
                    if (cdao.containsParentFolderID()) {
                        cdao.setActionFolder(cdao.getParentFolderID());
                    } else {
                        cdao.setActionFolder(inFolder);
                    }

                    ParticipantsDiffer participantsDiffer = new ParticipantsDiffer();
                    Difference difference = participantsDiffer.getDifference(edao, cdao);
                    if (difference != null) {
                        List<Object> added = difference.getAdded();
                        this.collectAddresses(added, false);
                    }

                    return cimp.updateAppointment(cdao, edao, writecon, session, ctx, inFolder, clientLastModified);
                } catch(final DataTruncation dt) {
                    throwTruncationError(cdao, writecon, dt);
                } catch(final BatchUpdateException bue) {
                    if (bue.getCause() instanceof DataTruncation) {
                        throwTruncationError(cdao, writecon, (DataTruncation) bue.getCause());
                    } else {
                        throw bue;
                    }
                } catch(final SQLException sqle) {
                    Databases.rollback(writecon);
                    throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
                } finally {
                    Databases.autocommit(writecon);
                }
                DBPool.pushWrite(ctx, writecon);
                writecon = null;
            }
            return conflicts;
        } catch(final DataTruncation dt) {
            final String fields[] = DBUtils.parseTruncatedFields(dt);
            final int fid[] = new int[fields.length];
            final OXException oxe = OXCalendarExceptionCodes.TRUNCATED_SQL_ERROR.create(dt, new Object[0]);
            int id = -1;
            for (int a = 0; a < fid.length; a++) {
                id = calendarCollection.getFieldId(fields[a]);
                final String value = calendarCollection.getString(cdao, id);
                if(value == null) {
                    oxe.addTruncatedId(id);
                } else {
                    final int valueLength = Charsets.getBytes(value, Charsets.UTF_8).length;
                    int maxLength = 0;
                    try {
                        maxLength = DBUtils.getColumnSize(writecon, "prg_dates", fields[a]);
                        oxe.addProblematic(new SimpleTruncatedAttribute(id, maxLength, valueLength));
                    } catch (final SQLException e) {
                        LOG.error("", e);
                        oxe.addTruncatedId(id);
                    }

                }
            }
            throw oxe;
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final OXException oxe) {
            throw oxe;
        } catch (final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(26));
        } finally {
            DBPool.pushWrite(ctx, writecon);
        }
    }

    /**
     * @param cdao
     * @param writecon
     * @param dt
     * @throws SQLException
     * @throws OXException
     */
    private void throwTruncationError(final CalendarDataObject cdao, final Connection writecon, final DataTruncation dt) throws SQLException, OXException {
        final String fields[] = DBUtils.parseTruncatedFields(dt);
        final int fid[] = new int[fields.length];
        final OXException oxe = OXCalendarExceptionCodes.TRUNCATED_SQL_ERROR.create(dt, new Object[0]);
        int id = -1;
        for (int a = 0; a < fid.length; a++) {
            id = calendarCollection.getFieldId(fields[a]);
            final String value = calendarCollection.getString(cdao, id);
            if(value == null) {
                oxe.addTruncatedId(id);
            } else {
                final int valueLength = Charsets.getBytes(value, Charsets.UTF_8).length;
                final int maxLength = DBUtils.getColumnSize(writecon, "prg_dates", fields[a]);
                oxe.addProblematic(new SimpleTruncatedAttribute(id, maxLength, valueLength));
            }
        }
        throw oxe;
    }

    @Override
    public void deleteAppointmentObject(final CalendarDataObject cdao, final int inFolder, final Date clientLastModified) throws OXException {
        deleteAppointmentObject(cdao, inFolder, clientLastModified, true);
    }

    @Override
    public void deleteAppointmentObject(final CalendarDataObject cdao, final int inFolder, final Date clientLastModified, final boolean checkPermissions) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        try {
            final DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                final Context ctx = Tools.getContext(session);
                final Connection writecon = DBPool.pickupWriteable(ctx);
                condition.resetTransactionRollbackException();
                try  {
                    DBUtils.startTransaction(writecon);
                    final CalendarDataObject c = cdao.clone();
                    cimp.deleteAppointment(session.getUserId(), c, writecon, session, ctx, inFolder, clientLastModified);
                    if (c.containsLastModified()) {
                        cdao.setLastModified(c.getLastModified());
                    }
                    if (c.containsRecurrenceID()) {
                        cdao.setRecurrenceID(c.getRecurrenceID());
                    }
                    writecon.commit();
                } catch(final OXException oxc) {
                    DBUtils.rollback(writecon);
                    if (!condition.isFailedTransactionRollback(oxc)) {
                        throw oxc;
                    }
                } catch(final SQLException e) {
                    DBUtils.rollback(writecon);
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, Integer.valueOf(28));
                    }
                } catch(final RuntimeException e) {
                    DBUtils.rollback(writecon);
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(1337));
                    }
                } finally {
                    DBUtils.autocommit(writecon);
                    DBPool.pushWrite(ctx, writecon);
                }
            } while (condition.checkRetry());
        } catch (final SQLException e) {
            if (DBUtils.isTransactionRollbackException(e)) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR_RETRY.create(e, Integer.valueOf(28));
            }
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, Integer.valueOf(28));
        }
    }

    @Override
    public void deleteAppointmentsInFolder(final int fid) throws OXException, SQLException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        final Context ctx = Tools.getContext(session);
        Connection writecon = null;
        boolean modified = false;
        try {
            writecon = DBPool.pickupWriteable(ctx);
            modified = deleteAppointmentsInFolder(fid, writecon);
        } finally {
            if (writecon != null) {
                if(modified) {
                    DBPool.pushWrite(ctx, writecon);
                } else {
                    DBPool.pushWriteAfterReading(ctx, writecon);
                }
            }
        }
    }

    @Override
    public boolean deleteAppointmentsInFolder(final int fid, final Connection writeCon) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        PreparedStatement prep = null;
        ResultSet rs = null;
        final Context ctx = Tools.getContext(session);
        try  {
            try {
                final OXFolderAccess ofa = new OXFolderAccess(writeCon, ctx);
                final CalendarSqlImp cimp = CalendarSql.cimp;
                if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                    prep = cimp.getPrivateFolderObjects(fid, ctx, writeCon);
                    rs = cimp.getResultSet(prep);
                    return cimp.deleteAppointmentsInFolder(session, ctx, rs, writeCon, writeCon, FolderObject.PRIVATE, fid);
                } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                    prep = cimp.getPublicFolderObjects(fid, ctx, writeCon);
                    rs = cimp.getResultSet(prep);
                    return cimp.deleteAppointmentsInFolder(session, ctx, rs, writeCon, writeCon, FolderObject.PUBLIC, fid);
                } else {
                    throw OXCalendarExceptionCodes.FOLDER_DELETE_INVALID_REQUEST.create();
                }
            } catch(final SQLException sqle) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
            }
        } catch(final OXException oxc) {
            throw oxc;
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(29));
        } finally {
            calendarCollection.closeResultSet(rs);
            calendarCollection.closePreparedStatement(prep);
        }
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(final int uid, final int fid) throws OXException, SQLException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        final Context ctx = Tools.getContext(session);
        try {
            readcon = DBPool.pickup(ctx);
            final OXFolderAccess ofa = new OXFolderAccess(readcon, ctx);
            if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                return cimp.checkIfFolderContainsForeignObjects(uid, fid, ctx, readcon, FolderObject.PRIVATE);
            } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                return cimp.checkIfFolderContainsForeignObjects(uid, fid, ctx, readcon, FolderObject.PUBLIC);
            } else {
                throw OXCalendarExceptionCodes.FOLDER_FOREIGN_INVALID_REQUEST.create();
            }
        } catch(final OXException oxc) {
            throw oxc;
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(30));
        } finally {
            if (readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(final int uid, final int fid, final Connection readCon) throws OXException,
    SQLException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        final Context ctx = Tools.getContext(session);
        try {
            final OXFolderAccess ofa = new OXFolderAccess(readCon, ctx);
            if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                return cimp.checkIfFolderContainsForeignObjects(uid, fid, ctx, readCon, FolderObject.PRIVATE);
            } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                return cimp.checkIfFolderContainsForeignObjects(uid, fid, ctx, readCon, FolderObject.PUBLIC);
            } else {
                throw OXCalendarExceptionCodes.FOLDER_FOREIGN_INVALID_REQUEST.create();
            }
        } catch (final OXException oxc) {
            throw oxc;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch (final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(30));
        }
    }

    @Override
    public boolean isFolderEmpty(final int uid, final int fid) throws OXException, SQLException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        final Context ctx = Tools.getContext(session);
        try {
            readcon = DBPool.pickup(ctx);
            final OXFolderAccess ofa = new OXFolderAccess(readcon, ctx);
            if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                return cimp.checkIfFolderIsEmpty(uid, fid, ctx, readcon, FolderObject.PRIVATE);
            } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                return cimp.checkIfFolderIsEmpty(uid, fid, ctx, readcon, FolderObject.PUBLIC);
            } else {
                throw OXCalendarExceptionCodes.FOLDER_IS_EMPTY_INVALID_REQUEST.create();
            }
        } catch(final OXException oxc) {
            throw oxc;
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(31));
        } finally {
            if (readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public boolean isFolderEmpty(final int uid, final int fid, final Connection readCon) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        final Context ctx = Tools.getContext(session);
        try {
            final OXFolderAccess ofa = new OXFolderAccess(readCon, ctx);
            if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                return cimp.checkIfFolderIsEmpty(uid, fid, ctx, readCon, FolderObject.PRIVATE);
            } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                return cimp.checkIfFolderIsEmpty(uid, fid, ctx, readCon, FolderObject.PUBLIC);
            } else {
                throw OXCalendarExceptionCodes.FOLDER_IS_EMPTY_INVALID_REQUEST.create();
            }
        } catch (final OXException oxc) {
            throw oxc;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch (final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(31));
        }
    }

    @Override
    public Date setUserConfirmation(final int oid, final int folderId, final int uid, final int confirm, final String confirmMessage) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        final Context ctx = Tools.getContext(session);
        validateConfirmMessage(confirmMessage);
        return cimp.setUserConfirmation(oid, folderId, uid, confirm, confirmMessage, session, ctx);
    }

    @Override
    public Date setExternalConfirmation(final int objectId, final int folderId, final String mail, final int confirm, final String message) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        final Context ctx = Tools.getContext(session);
        validateConfirmMessage(message);
        return cimp.setExternalConfirmation(objectId, folderId, mail, confirm, message, session, ctx);
    }

    /**
     * {@inheritDoc
     */
    @Override
    public CalendarDataObject setUserConfirmation(final int objectId, final int folderId, final int optOccurrenceId, final int userId, final int confirm, final String confirmMessage) throws OXException {
        if (optOccurrenceId <= 0) {
            CalendarDataObject retval = new CalendarDataObject();
            retval.setLastModified(setUserConfirmation(objectId, folderId, userId, confirm, confirmMessage));
            return retval;
        }

        validateConfirmMessage(confirmMessage);

        CalendarDataObject edao = new CalendarDataObject();

        try {
            edao = getObjectById(objectId);
        } catch (SQLException x) {
            LOG.warn("Not able to retrieve original object!", x);
        }

        CalendarDataObject[] changeExceptionsByRecurrence = calendarCollection.getChangeExceptionsByRecurrence(edao.getRecurrenceID(), EXCEPTION_FIELDS, session);
        for (CalendarDataObject exception : changeExceptionsByRecurrence) {
            if (exception.getRecurrencePosition() == optOccurrenceId) {
                CalendarDataObject retval = new CalendarDataObject();
                retval.setLastModified(setUserConfirmation(exception.getObjectID(), folderId, userId, confirm, confirmMessage));
                return retval;
            }
        }
        CalendarDataObject cdao = edao.clone();

        final RecurringResultsInterface recurringResultsInterface = calendarCollection.calculateRecurringIgnoringExceptions(edao, 0, 0, optOccurrenceId);

        if (recurringResultsInterface != null) {
            final RecurringResultInterface rs = recurringResultsInterface.getRecurringResult(0);

            if (rs != null) {
                cdao.setEndDate(new Date(rs.getEnd()));
                cdao.setRecurrencePosition(rs.getPosition());
                cdao.setStartDate(new Date(rs.getStart()));
                cdao.setIgnoreConflicts(true);
                if (cdao.containsOccurrence()) {
                    cdao.removeUntil();
                }

                List<UserParticipant> users = new LinkedList<UserParticipant>();
                for (final UserParticipant cur : edao.getUsers()) {

                    if (cur.getIdentifier() == userId) {
                        UserParticipant participant = null;
                        try {
                            participant = cur.clone();
                            participant.setConfirm(confirm);
                            participant.setConfirmMessage(confirmMessage);

                            users.add(participant);
                        } catch (CloneNotSupportedException e) {
                            LOG.error("Cloning participant not possible", e);
                        }
                        continue;
                    }
                    users.add(cur);
                }
                cdao.setUsers(users);

                this.updateAppointmentObject(cdao, folderId, edao.getLastModified());
            } else {
                int relevantException = getExceptionObjectIdForRecurrence(edao, objectId, optOccurrenceId);

                if (relevantException != EXCEPTION_NOT_FOUND) {
                    CalendarDataObject retval = new CalendarDataObject();
                    retval.setLastModified(setUserConfirmation(relevantException, folderId, userId, confirm, confirmMessage));
                    return retval;
                }
                LOG.warn("No existing appointment series exception to update found for object {} in context {}", edao.getObjectID(), edao.getContextID());
            }
        } else {
            CalendarDataObject retval = new CalendarDataObject();
            retval.setLastModified(setUserConfirmation(objectId, folderId, userId, confirm, confirmMessage));
            return retval;
        }

        return cdao;
    }

    /**
     * Validates provided confirm message and throws exception if an invalid character was found
     *
     * @param confirmMessage - the message to check
     * @throws OXException
     */
    protected void validateConfirmMessage(final String confirmMessage) throws OXException {
        if (confirmMessage != null) {
            String error = null;
            error = Check.containsInvalidChars(confirmMessage);
            if (error != null) {
                throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Confirm Message", error);
            }
        }
    }

    /**
     * Returns the object id from the given CalendarDataObject exception if there is an exception.
     *
     * @param original - the original CalendarDataObject to get the exception id from
     * @param objectId - id of the series object to get all exceptions for
     * @param optOccurrenceId - the occurrence to get the exception from
     * @return int - with the object id of the exception or -1 if no exception is available for the given occurrence
     * @throws OXException
     */
    protected int getExceptionObjectIdForRecurrence(final CalendarDataObject original, final int objectId, final int optOccurrenceId) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        CalendarDataObject[] changeExceptionsByRecurrence = calendarCollection.getChangeExceptionsByRecurrence(objectId, EXCEPTION_FIELDS, session);
        RecurringResultInterface recurringResult = calendarCollection.calculateRecurringIgnoringExceptions(original, 0, 0, optOccurrenceId).getRecurringResult(0);

        for (CalendarDataObject cdao : changeExceptionsByRecurrence) {
            if ((recurringResult.getStart() == cdao.getStartDate().getTime()) && (recurringResult.getEnd() == cdao.getEndDate().getTime())) {
                return cdao.getObjectID();
            }
        }
        return EXCEPTION_NOT_FOUND;
    }

    /**
     * {@inheritDoc
     */
    @Override
    public CalendarDataObject setExternalConfirmation(final int objectId, final int folderId, final int optOccurrenceId, final String mail, final int confirm, final String message) throws OXException {
        if (optOccurrenceId <= 0) {
            CalendarDataObject retval = new CalendarDataObject();
            retval.setLastModified(setExternalConfirmation(objectId, folderId, mail, confirm, message));
            return retval;
        }

        if ((mail == null) || Strings.isEmpty(mail)) {
            throw OXCalendarExceptionCodes.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD.create();
        }

        validateConfirmMessage(message);

        CalendarDataObject edao = new CalendarDataObject();

        try {
            edao = getObjectById(objectId);
        } catch (SQLException x) {
            LOG.warn("Not able to retrieve original object!", x);
        }

        CalendarDataObject[] changeExceptionsByRecurrence = calendarCollection.getChangeExceptionsByRecurrence(edao.getRecurrenceID(), EXCEPTION_FIELDS, session);
        for (CalendarDataObject exception : changeExceptionsByRecurrence) {
            if (exception.getRecurrencePosition() == optOccurrenceId) {
                CalendarDataObject retval = new CalendarDataObject();
                retval.setLastModified(setExternalConfirmation(exception.getObjectID(), folderId, mail, confirm, message));
                return retval;
            }
        }

        CalendarDataObject cdao = edao.clone();

        final RecurringResultsInterface recurringResultsInterface = calendarCollection.calculateRecurringIgnoringExceptions(edao, 0, 0, optOccurrenceId);

        if (recurringResultsInterface != null) {
            final RecurringResultInterface rs = recurringResultsInterface.getRecurringResult(0);

            if (rs != null) {
                cdao.setEndDate(new Date(rs.getEnd()));
                cdao.setRecurrencePosition(rs.getPosition());
                cdao.setStartDate(new Date(rs.getStart()));
                cdao.setIgnoreConflicts(true);
                if (cdao.containsOccurrence()) {
                    cdao.removeUntil();
                }

                List<Participant> users = new LinkedList<Participant>();
                for (final Participant cur : edao.getParticipants()) {

                    if (cur instanceof ExternalUserParticipant) {
                        ExternalUserParticipant eup = (ExternalUserParticipant) cur;
                        if (mail.equalsIgnoreCase(eup.getEmailAddress())) {
                            ExternalUserParticipant participant = null;
                            try {
                                participant = eup.clone();
                                participant.setConfirm(confirm);
                                participant.setMessage(message);

                                users.add(participant);
                            } catch (CloneNotSupportedException e) {
                                LOG.error("Cloning participant not possible", e);
                            }
                            continue;
                        }
                    }
                    users.add(cur);
                }
                cdao.setParticipants(users);
                updateAppointmentObject(cdao, folderId, edao.getLastModified());

                setExternalConfirmation(cdao.getObjectID(), cdao.getParentFolderID(), mail, confirm, message);
            } else {
                int relevantExceptionId = getExceptionObjectIdForRecurrence(edao, objectId, optOccurrenceId);

                if (relevantExceptionId != EXCEPTION_NOT_FOUND) {
                    CalendarDataObject retval = new CalendarDataObject();
                    retval.setLastModified(setExternalConfirmation(relevantExceptionId, folderId, mail, confirm, message));
                    return retval;
                }
                LOG.warn("No existing appointment series exception to update found for object {} in context {}", edao.getObjectID(), edao.getContextID());
            }
        } else {
            CalendarDataObject retval = new CalendarDataObject();
            retval.setLastModified(setExternalConfirmation(objectId, folderId, mail, confirm, message));
            return retval;
        }

        return cdao;
    }

    @Override
    public SearchIterator<Appointment> getObjectsById(final int[][] oids, int[] cols) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        if (oids.length > 0) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            boolean close_connection = true;
            final Context ctx = Tools.getContext(session);
            try {
                readcon = DBPool.pickup(ctx);
                cols = calendarCollection.checkAndAlterCols(cols);
                final CalendarOperation co = new CalendarOperation();
                final CalendarSqlImp cimp = CalendarSql.cimp;
                prep = cimp.getPreparedStatement(readcon, cimp.getObjectsByidSQL(oids, session.getContextId(), StringCollection.getSelect(cols, DATES_TABLE_NAME)));
                rs = cimp.getResultSet(prep);
                co.setOIDS(true, oids);
                co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session, ctx);
                close_connection = false;
                return new AppointmentIteratorAdapter(new AnonymizingIterator(co, ctx, session.getUserId(), oids));
            } catch(final SQLException sqle) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
            } catch(final OXException oxc) {
                throw oxc;
            } catch(final RuntimeException e) {
                throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(32));
            } finally {
                if (readcon != null && close_connection) {
                    calendarCollection.closeResultSet(rs);
                    calendarCollection.closePreparedStatement(prep);
                    DBPool.push(ctx, readcon);
                }
            }
        }
        return SearchIteratorAdapter.emptyIterator();
    }

    @Override
    public SearchIterator<Appointment> searchAppointments(final AppointmentSearchObject searchObj, final int orderBy, final Order orderDir, int[] cols) throws OXException {
        return searchAppointments(searchObj, orderBy, orderDir, -1, cols);
    }

    @Override
    public SearchIterator<Appointment> searchAppointments(final AppointmentSearchObject searchObj, final int orderBy, final Order orderDir, int limit, int[] cols) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }

        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        cols = calendarCollection.checkAndAlterCols(cols);

        final Connection readcon = DBPool.pickup(ctx);

        boolean closeCon = true;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final OXFolderAccess folderAccess = new OXFolderAccess(readcon, ctx);
            final CalendarOperation co = new CalendarOperation();

            CalendarFolderObject cfo = null;
            Set<Integer> folderIDs = searchObj.getFolderIDs();
            if (null == folderIDs || 0 == folderIDs.size()) {
                // Missing folder attribute indicates a search over all calendar folders the user can see,
                // so create a list with all folders in which the user is allowed to see appointments
                try {
                    cfo = calendarCollection.getAllVisibleAndReadableFolderObject(user.getId(), user.getGroups(), ctx, userConfig, readcon);
                } catch (final SQLException e) {
                    throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
                }
            } else if (1 == folderIDs.size()) {
                final int folderId = folderIDs.iterator().next().intValue();
                final EffectivePermission folderPermission = folderAccess.getFolderPermission(folderId, user.getId(), userConfig);

                if (folderPermission.isFolderVisible() && (folderPermission.canReadAllObjects() || folderPermission.canReadOwnObjects())) {
                    co.setRequestedFolder(folderId);
                } else {
                    throw OXCalendarExceptionCodes.NO_PERMISSIONS_TO_READ.create();
                }
            } else {
                /*
                 * build a custom CalendarFolderObject containing the requested folders
                 */
                cfo = new CalendarFolderObject(user.getId(), ctx.getContextId(), true);
                for (Integer folderID : folderIDs) {
                    FolderObject folder = folderAccess.getFolderObject(folderID);
                    EffectivePermission permission = folder.getEffectiveUserPermission(user.getId(), userConfig);
                    if (permission.isFolderVisible() && (permission.canReadAllObjects() || permission.canReadOwnObjects())) {
                        cfo.addFolder(permission.canReadAllObjects(), permission.canReadOwnObjects(),
                            folder.isShared(user.getId()), folder.getObjectID(), folder.getType());
                    } else {
                        throw OXCalendarExceptionCodes.NO_PERMISSIONS_TO_READ.create();
                    }
                }
            }

            final StringBuilder columnBuilder = new StringBuilder(cols.length << 4);
            boolean first = true;
            for (int col : cols) {
                final String temp = calendarCollection.getFieldName(col);

                if (temp != null) {
                    if (first) {
                        columnBuilder.append(temp);
                        first = false;
                    } else {
                        columnBuilder.append(',');
                        columnBuilder.append(temp);
                    }
                }
            }

            final CalendarSqlImp cimp = CalendarSql.cimp;
            boolean exceedsFolderLimit = exceedsFolderLimit(searchObj, cfo);
            Set<Integer> searchFolder = null;
            if (exceedsFolderLimit) {
                CalendarFolderObject emptyCFO = new CalendarFolderObject(user.getId(), ctx.getContextId(), true);
                searchFolder = searchObj.getFolderIDs();
                searchObj.setFolderIDs(Collections.<Integer>emptySet());
                stmt = cimp.getSearchStatement(user.getId(), searchObj, emptyCFO, folderAccess, columnBuilder.toString(), orderBy, orderDir, limit, ctx, readcon);
            } else {
                stmt = cimp.getSearchStatement(user.getId(), searchObj, cfo, folderAccess, columnBuilder.toString(), orderBy, orderDir, limit, ctx, readcon);
            }
            rs = cimp.getResultSet(stmt);
            co.setResultSet(rs, stmt, cols, cimp, readcon, 0, 0, session, ctx);

            // Don't close connection, it's used within the SearchIterator
            closeCon = false;

            SearchIterator<CalendarDataObject> iterator = new CachedCalendarIterator(cfo, co, ctx, session.getUserId());
            if (exceedsFolderLimit) {
                iterator = new FolderSearchAppointmentIterator(iterator, cfo, searchFolder, session.getUserId(), folderAccess);
            }
            return new AppointmentIteratorAdapter(iterator);
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } finally {
            if (stmt != null) {
                calendarCollection.closeResultSet(rs);
                calendarCollection.closePreparedStatement(stmt);
            }

            if (closeCon && readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    private boolean exceedsFolderLimit(AppointmentSearchObject searchObj, CalendarFolderObject cfo) {
        if (searchObj == null || cfo == null) {
            return false;
        }
        if (searchObj.getFolderIDs() == null || searchObj.getFolderIDs().isEmpty()) {
            return false;
        }

        int readableFolder = 0;
        readableFolder += cfo.getPrivateFolders() == null ? 0 : cfo.getPrivateFolders().size();
        readableFolder += cfo.getPublicReadableOwn() == null ? 0 : cfo.getPublicReadableOwn().size();
        readableFolder += cfo.getPublicReadableAll() == null ? 0 : cfo.getPublicReadableAll().size();
        readableFolder += cfo.getSharedReadableOwn() == null ? 0 : cfo.getSharedReadableOwn().size();
        readableFolder += cfo.getSharedReadableAll() == null ? 0 : cfo.getSharedReadableAll().size();
        int maxSearchFolder = MAX_SEARCH_FOLDER;
        if (maxSearchFolder >= 0 && readableFolder > maxSearchFolder) {
            return true;
        }

        return false;
    }

    @Override
    public final long attachmentAction(final int folderId, final int oid, final int uid, final Session session, final Context c, final int numberOfAttachments) throws OXException {
        return cimp.attachmentAction(folderId, oid, uid, session, c, numberOfAttachments);
    }

    @Override
    public SearchIterator<Appointment> getFreeBusyInformation(final int uid, final int type, final Date start, final Date end) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        Connection readcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean close_connection = true;
        final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        final CalendarSqlImp calendarsqlimp = CalendarSql.getCalendarSqlImplementation();
        SearchIterator<List<Integer>> private_folder_information = null;
        try {
            if (!userConfig.hasFreeBusy()) {
                return SearchIteratorAdapter.emptyIterator();
            }
            readcon = DBPool.pickup(ctx);
            final CalendarSqlImp cimp = CalendarSql.cimp;
            switch(type) {
            case Participant.USER:
                private_folder_information = calendarsqlimp.getAllPrivateAppointmentAndFolderIdsForUser(ctx, user.getId(), readcon);
                prep = cimp.getFreeBusy(uid, ctx, start, end, readcon);
                break;
            case Participant.RESOURCE:
                final long whole_day_start = calendarCollection.getUserTimeUTCDate(start, user.getTimeZone());
                long whole_day_end = calendarCollection.getUserTimeUTCDate(end, user.getTimeZone());
                if (whole_day_end <= whole_day_start) {
                    whole_day_end = whole_day_start+Constants.MILLI_DAY;
                }
                private_folder_information = calendarsqlimp.getResourceConflictsPrivateFolderInformation(ctx, start, end, new Date(whole_day_start), new Date(whole_day_end), readcon, wrapParenthesis(uid));
                prep = cimp.getResourceFreeBusy(uid, ctx, start, end, readcon);
                break;
            default:
                throw OXCalendarExceptionCodes.FREE_BUSY_UNSUPPOTED_TYPE.create(Integer.valueOf(type));
            }
            rs = cimp.getResultSet(prep);
            //final SearchIterator si = new FreeBusyResults(rs, prep, ctx, readcon, start.getTime(), end.getTime());
            final SearchIterator si = new FreeBusyResults(rs, prep, ctx, session.getUserId(), user.getGroups(), userConfig, readcon, true, new Participant[0], private_folder_information, calendarsqlimp, start.getTime(), end.getTime());
            close_connection = false;
            return si;
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final OXException oxc) {
            throw oxc;
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(34));
        } finally {
            if (close_connection) {
                calendarCollection.closeResultSet(rs);
                calendarCollection.closePreparedStatement(prep);
            }
            if (readcon != null && close_connection) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public SearchIterator<Appointment> getActiveAppointments(final int user_uid, final Date start, final Date end, int cols[]) throws OXException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        PreparedStatement prep = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(session);
        try {
            readcon = DBPool.pickup(ctx);
            cols = calendarCollection.checkAndAlterCols(cols);
            final CalendarOperation co = new CalendarOperation();
            final CalendarSqlImp cimp = CalendarSql.cimp;
            prep = cimp.getActiveAppointments(ctx, session.getUserId(), start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), readcon);
            final ResultSet rs = cimp.getResultSet(prep);
            co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session, ctx);
            close_connection = false;
            return new AppointmentIteratorAdapter(new CachedCalendarIterator(co, ctx, session.getUserId()));
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final OXException oxc) {
            throw oxc;
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(35));
        } finally {
            if (readcon != null && close_connection) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsBetween(final int userId, final Date start, final Date end, int[] cols, final Date since, final int orderBy, final Order orderDir) throws OXException, SQLException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(session);
        final User user = Tools.getUser(session, ctx);
        final UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
        try {
            readcon = DBPool.pickup(ctx);
            cols = calendarCollection.checkAndAlterCols(cols);
            final CalendarOperation co = new CalendarOperation();
            final CalendarSqlImp cimp = CalendarSql.cimp;
            prep = cimp.getAllAppointmentsForUser(ctx, session.getUserId(), user.getGroups(), userConfig, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), readcon, since, orderBy, orderDir);
            rs = cimp.getResultSet(prep);
            co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session, ctx);
            close_connection = false;
            return new AppointmentIteratorAdapter(new CachedCalendarIterator(co, ctx, session.getUserId()));
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final OXException oxc) {
            throw oxc;
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, e.getMessage());
        } finally {
            if (close_connection) {
                calendarCollection.closeResultSet(rs);
                calendarCollection.closePreparedStatement(prep);
            }
            if (readcon != null && close_connection) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(final int user_uid, final Date start, final Date end, final int cols[], final int orderBy, final Order orderDir) throws OXException, SQLException {
        return getModifiedAppointmentsBetween(user_uid, start, end, cols, null, orderBy, orderDir);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(final Date start, final Date end, int cols[], final int orderBy, final Order order) throws OXException, SQLException {
        if (session == null) {
            throw OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL.create();
        }
        Connection readcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean close_connection = true;
        final Context ctx = Tools.getContext(session);
        try {
            readcon = DBPool.pickup(ctx);
            cols = calendarCollection.checkAndAlterCols(cols);
            final CalendarOperation co = new CalendarOperation();
            final CalendarSqlImp cimp = CalendarSql.cimp;
            prep = cimp.getAllAppointments(ctx, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), readcon, orderBy, order);
            rs = cimp.getResultSet(prep);
            co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session, ctx);
            close_connection = false;
            return new AppointmentIteratorAdapter(new CachedCalendarIterator(co, ctx, session.getUserId()));
        } catch(final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final OXException oxc) {
            throw oxc;
        } catch(final RuntimeException e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(36));
        } finally {
            if (close_connection) {
                calendarCollection.closeResultSet(rs);
                calendarCollection.closePreparedStatement(prep);
            }
            if (readcon != null && close_connection) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    public static final CalendarSqlImp getCalendarSqlImplementation() {
        CalendarSqlImp cimp = CalendarSql.cimp;
        if (cimp != null){
            return cimp;
        }
        LOG.error("No CalendarSqlImp Class found !");
        try {
            cimp = (CalendarSqlImp) Class.forName(default_class).newInstance();
            CalendarSql.cimp = cimp;
            return cimp;
        } catch(final ClassNotFoundException cnfe) {
            LOG.error("", cnfe);
        } catch (final IllegalAccessException iae) {
            LOG.error("", iae);
        } catch (final InstantiationException ie) {
            LOG.error("", ie);
        }
        return null;
    }

    static {
        try {
            if (cimp == null) {
                CalendarConfig.init();
                String classname = CalendarConfig.getProperty("CalendarSQL");
                if (classname == null) {
                    classname = default_class;
                }
                LOG.debug("Using {} in CalendarSql", classname);
                cimp = (CalendarSqlImp) Class.forName(classname).newInstance();
            }
            String maxFolder = CalendarConfig.getProperty("MAX_SEARCH_FOLDER");
            if (maxFolder != null && !maxFolder.trim().equals("")) {
                MAX_SEARCH_FOLDER = Integer.valueOf(maxFolder.trim());
            }
        } catch(final ConfigurationException ce) {
            LOG.error("", ce);
        } catch(final ClassNotFoundException cnfe) {
            LOG.error("", cnfe);
        } catch (final IllegalAccessException iae) {
            LOG.error("", iae);
        } catch (final InstantiationException ie) {
            LOG.error("", ie);
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Wraps specified <code>int</code> with parenthesis
     *
     * @param i
     *            The <code>int</code> value to wrap
     * @return The wrapped <code>int</code> value
     */
    private static final String wrapParenthesis(final int i) {
        final String str = String.valueOf(i);
        return new StringBuilder(str.length() + 2).append('(').append(str).append(')').toString();
    }

    @Override
    public int resolveUid(final String uid) throws OXException {
        return cimp.resolveUid(session, uid);
    }

    @Override
    public int resolveFilename(final String filename) throws OXException {
        return cimp.resolveFilename(session, filename);
    }

    @Override
    public int getFolder(final int objectId) throws OXException {
        return cimp.getFolder(session, objectId);
    }

    @Override
    public void setIncludePrivateAppointments(final boolean include) {
        this.includePrivateAppointments = include;
    }

    @Override
    public boolean getIncludePrivateAppointments() {
        return this.includePrivateAppointments;
    }

    @Override
    public List<Appointment> getAppointmentsWithExternalParticipantBetween(final String email, int[] cols, final Date start, final Date end, final int orderBy, final Order order) throws OXException {
        final List<Appointment> appointments = new LinkedList<Appointment>();
        cols = addColumnIfNecessary(cols, CalendarObject.PARTICIPANTS, CalendarObject.RECURRENCE_TYPE, CalendarObject.RECURRENCE_POSITION, CalendarObject.RECURRENCE_ID);
        SearchIterator<Appointment> searchIterator;
        try {
            searchIterator = getModifiedAppointmentsBetween(session.getUserId(), start, end, cols, null, orderBy, order);
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        }
        while (searchIterator.hasNext()) {
            final Appointment app = searchIterator.next();
            final Participant[] participants = app.getParticipants();
            if (null != participants) {
                for (final Participant participant : participants) {
                    if (participant.getType() == Participant.EXTERNAL_USER && participant.getEmailAddress().equals(email)) {
                        appointments.add(app);
                        break;
                    }
                }
            }
        }

        return extractOccurrences(appointments, start, end);
    }

    @Override
    public List<Appointment> getAppointmentsWithUserBetween(final User user, int[] cols, final Date start, final Date end, final int orderBy, final Order order) throws OXException {
        final List<Appointment> appointments = new LinkedList<Appointment>();
        cols = addColumnIfNecessary(cols, CalendarObject.USERS, CalendarObject.RECURRENCE_TYPE, CalendarObject.RECURRENCE_POSITION, CalendarObject.RECURRENCE_ID, Appointment.TIMEZONE);
        SearchIterator<Appointment> searchIterator;
        try {
            searchIterator = getModifiedAppointmentsBetween(session.getUserId(), start, end, cols, null, orderBy, order);
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        }
        while (searchIterator.hasNext()) {
            final Appointment app = searchIterator.next();
            final UserParticipant[] users = app.getUsers();
            for (final UserParticipant userParticipant : users) {
                if (userParticipant.getIdentifier() == user.getId()) {
                    appointments.add(app);
                    break;
                }
            }
        }

        return extractOccurrences(appointments, start, end);
    }

    @Override
    public int countObjectsInFolder(int folderId) throws OXException {
        OXFolderAccess oxfa = new OXFolderAccess(Tools.getContext(session));
        int folderType = oxfa.getFolderType(folderId, session.getUserId());
        UserConfiguration userConfiguration = Tools.getUserConfiguration(Tools.getContext(session), session.getUserId());
        EffectivePermission folderPermission = oxfa.getFolderPermission(folderId, session.getUserId(), userConfiguration);

        if (!folderPermission.isFolderVisible()) {
            throw OXCalendarExceptionCodes.NO_PERMISSIONS_TO_READ.create();
        }

        return CalendarSql.cimp.countObjectsInFolder(session, folderId, folderType, folderPermission);
    }

    private int[] addColumnIfNecessary(final int[] cols, final int... columnsToAdd) {

        final List<Integer> columns = new LinkedList<Integer>();
        for (final int c : cols) {
            columns.add(c);
        }
        for (int columnToAdd : columnsToAdd) {
            if (!columns.contains(columnToAdd)) {
                columns.add(columnToAdd);
            }
        }

        return I2i(columns);
    }

    private List<Appointment> extractOccurrences(List<Appointment> appointments, Date start, Date end) throws OXException {
        List<Appointment> retval = new LinkedList<Appointment>();

        for (Appointment appointment : appointments) {
            if (appointment.getRecurrenceType() == Appointment.NO_RECURRENCE || appointment.getRecurrencePosition() != 0) {
                retval.add(appointment);
                continue;
            }

            if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {
                RecurringResultsInterface recuResults = calendarCollection.calculateRecurring(appointment, start.getTime(), end.getTime(), 0);

                if (recuResults != null) {
                    for (int a = 0; a < recuResults.size(); a++) {
                        Appointment app = appointment.clone();
                        final RecurringResultInterface result = recuResults.getRecurringResult(a);
                        app.setStartDate(new Date(result.getStart()));
                        app.setEndDate(new Date(result.getEnd()));
                        app.setRecurrencePosition(result.getPosition());

                        retval.add(app);
                    }
                }
            }
        }

        return retval;
    }
}
