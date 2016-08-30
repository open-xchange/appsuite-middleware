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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.storage.ParticipantStorage;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link CalendarOperation} - Provides various operations on calendar
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class CalendarOperation implements SearchIterator<CalendarDataObject> {

    private static interface FieldFiller {

        public void fillField(CalendarDataObject cdao, int columnCount, ResultSet rs) throws SQLException;
    }

    private static final Map<Integer, FieldFiller> FILLERS = new HashMap<Integer, FieldFiller>() {

        private static final long serialVersionUID = -647801170633669563L;

        // instance initializer
        {
            put(I(DataObject.OBJECT_ID), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setObjectID(rs.getInt(columnCount));
                }
            });
            put(I(CalendarObject.TITLE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String t = rs.getString(columnCount);
                    cdao.setTitle(rs.wasNull() ? null : t);
                }
            });
            put(I(Appointment.LOCATION), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String loc = rs.getString(columnCount);
                    cdao.setLocation(rs.wasNull() ? null : loc);
                }
            });
            put(I(Appointment.SHOWN_AS), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setShownAs(rs.getInt(columnCount));
                }
            });
            put(I(CalendarObject.NOTE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String note = rs.getString(columnCount);
                    cdao.setNote(rs.wasNull() ? null : note);
                }
            });
            put(I(CalendarObject.START_DATE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final Date sd = rs.getTimestamp(columnCount);
                    cdao.setStartDate(rs.wasNull() ? null : sd);
                }
            });
            put(I(CalendarObject.END_DATE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final Date ed = rs.getTimestamp(columnCount);
                    cdao.setEndDate(rs.wasNull() ? null : ed);
                }
            });
            put(I(DataObject.CREATED_BY), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setCreatedBy(rs.getInt(columnCount));
                }
            });
            put(I(DataObject.MODIFIED_BY), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setModifiedBy(rs.getInt(columnCount));
                }
            });
            put(I(DataObject.CREATION_DATE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final Timestamp ts = rs.getTimestamp(columnCount);
                    cdao.setCreationDate(rs.wasNull() ? null : ts);
                }
            });
            put(I(DataObject.LAST_MODIFIED), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final Timestamp ts = new Timestamp(rs.getLong(columnCount));
                    cdao.setLastModified(rs.wasNull() ? null : ts);
                }
            });
            put(I(Appointment.FULL_TIME), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setFullTime(rs.getInt(columnCount) > 0);
                }
            });
            put(I(CommonObject.COLOR_LABEL), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setLabel(rs.getInt(columnCount));
                }
            });
            put(I(CommonObject.PRIVATE_FLAG), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setPrivateFlag(rs.getInt(columnCount) > 0);
                }
            });
            put(I(CommonObject.NUMBER_OF_ATTACHMENTS), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setNumberOfAttachments(rs.getInt(columnCount));
                }
            });
            put(I(CalendarObject.RECURRENCE_ID), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final int recurrenceId = rs.getInt(columnCount);
                    if (!rs.wasNull()) {
                        cdao.setRecurrenceID(recurrenceId);
                    }
                }
            });
            put(I(CommonObject.CATEGORIES), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String cat = rs.getString(columnCount);
                    cdao.setCategories(rs.wasNull() ? null : cat);
                }
            });
            put(I(CalendarObject.RECURRENCE_TYPE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String rt = rs.getString(columnCount);
                    cdao.setRecurrence(rs.wasNull() ? null : rt);
                }
            });
            put(I(CalendarObject.CHANGE_EXCEPTIONS), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String ce = rs.getString(columnCount);
                    cdao.setExceptions(rs.wasNull() ? null : ce);
                }
            });
            put(I(CalendarObject.DELETE_EXCEPTIONS), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String de = rs.getString(columnCount);
                    cdao.setDelExceptions(rs.wasNull() ? null : de);
                }
            });
            put(I(CalendarObject.RECURRENCE_CALCULATOR), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setRecurrenceCalculator(rs.getInt(columnCount));
                }
            });
            put(I(CalendarObject.RECURRENCE_POSITION), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final int recurrencePosition = rs.getInt(columnCount);
                    if (!rs.wasNull()) {
                        cdao.setRecurrencePosition(recurrencePosition);
                    }
                }
            });
            put(I(Appointment.TIMEZONE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String tz = rs.getString(columnCount);
                    cdao.setTimezone(rs.wasNull() ? null : tz);
                }
            });
            put(I(Appointment.RECURRENCE_START), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final long recurring_start = rs.getLong(columnCount);
                    cdao.setRecurringStart(recurring_start);
                }
            });
            put(I(CalendarObject.ORGANIZER), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String organizer = rs.getString(columnCount);
                    if (!rs.wasNull()) {
                        cdao.setOrganizer(organizer);
                    }
                }
            });
            put(I(CommonObject.UID), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String uid = rs.getString(columnCount);
                    cdao.setUid(rs.wasNull() ? null : uid);
                }
            });
            put(I(CalendarObject.SEQUENCE), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    cdao.setSequence(rs.getInt(columnCount));
                }
            });
            put(I(CalendarObject.ORGANIZER_ID), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final int organizerId = rs.getInt(columnCount);
                    if (!rs.wasNull()) {
                        cdao.setOrganizerId(organizerId);
                    }
                }
            });
            put(I(CalendarObject.PRINCIPAL), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String principal = rs.getString(columnCount);
                    if (!rs.wasNull()) {
                        cdao.setPrincipal(principal);
                    }
                }
            });
            put(I(CalendarObject.PRINCIPAL_ID), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final int principalId = rs.getInt(columnCount);
                    if (!rs.wasNull()) {
                        cdao.setPrincipalId(principalId);
                    }
                }
            });
            put(I(CommonObject.FILENAME), new FieldFiller() {

                @Override
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs) throws SQLException {
                    final String filename = rs.getString(columnCount);
                    cdao.setFilename(rs.wasNull() ? null : filename);
                }
            });

        }
    };

    public static final int READ = 0;

    public static final int INSERT = 1;

    public static final int UPDATE = 2;

    public static final int DELETE = -1;

    /**
     * Indicates accepted event
     */
    public static final int CONFIRM_ACCEPTED = 3;

    /**
     * Indicates declined event
     */
    public static final int CONFIRM_DELINED = 4;

    /**
     * Indicates tentatively accepted event
     */
    public static final int CONFIRM_TENTATIVELY_ACCEPTED = 5;

    /**
     * Indicates removed confirmation status.
     */
    public static final int CONFIRM_WAITING = 6;

    public static final int MAX_RESULT_LIMIT = -1;

    private int result_counter;

    private final List<OXException> warnings = new ArrayList<OXException>(2);

    private boolean has_next;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarOperation.class);

    private ResultSet co_rs;

    private PreparedStatement prep;

    private int cols[];

    private Context c;

    private CalendarSqlImp cimp;

    private Connection readcon;

    private int from, to, uid;

    private Session so;

    private boolean strict;

    private int requested_folder;

    public static final int NO_MOVE_ACTION = 0;

    public static final int PRIVATE_CURRENT_PARTICIPANT_ONLY = 1;

    public static final int PRIVATE_ALL_PARTICIPANTS = 2;

    public static final int PUBLIC_ALL_PARTICIPANTS = 3;

    public static final char COLON = ':';

    public static final char PERCENT = '%';

    private int oids[][];

    private boolean includePrivateAppointmentsOfSharedFolderOwner = false;;

    private static final CalendarCollection recColl = new CalendarCollection();

    final CalendarDataObject loadAppointment(final ResultSet load_resultset, final int oid, final int inFolder, final CalendarSqlImp cimp, final Connection readcon, final Session so, final Context ctx, final int action, final int action_folder) throws SQLException, OXException {
        return loadAppointment(load_resultset, oid, inFolder, cimp, readcon, so, ctx, action, action_folder, true);
    }

    protected CalendarDataObject loadAppointment(ResultSet load_resultset, int oid, int inFolder, CalendarSqlImp cimp, Connection readcon, Session so, Context ctx, int action, int action_folder, boolean check_permissions) throws SQLException, OXException {
        return loadAppointment(load_resultset, oid, inFolder, cimp, readcon, so, ctx, action, action_folder, check_permissions, null, null);
    }

    protected CalendarDataObject loadAppointment(ResultSet load_resultset, int oid, int inFolder, CalendarSqlImp cimp, Connection readcon, Session so, Context ctx, int action, int action_folder, boolean check_permissions, String organizer, String uniqueId) throws SQLException, OXException {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setObjectID(oid);
        cdao.setContext(ctx);
        int check_special_action = action;
        if (action == UPDATE && inFolder != action_folder) {
            // We move and this means to create a new object
            check_special_action = INSERT;
        }
        try {
            if (load_resultset.next()) {
                int i = 1;
                cdao.setCreationDate(setTimestamp(i++, load_resultset));
                cdao.setCreatedBy(setInt(i++, load_resultset));
                cdao.setLastModified(setTimestampFromLong(i++, load_resultset));
                cdao.setModifiedBy(setInt(i++, load_resultset));
                cdao.setGlobalFolderID(setInt(i++, load_resultset));
                cdao.setPrivateFlag(setBooleanToInt(i++, load_resultset));
                Participant[] participants = cimp.getParticipants(cdao, readcon).getList();
                cdao.setParticipants(participants);

                cdao.setStartDate(setDate(i++, load_resultset));
                cdao.setEndDate(setDate(i++, load_resultset));
                cdao.setTimezone(setString(i++, load_resultset));
                final int recId = setInt(i++, load_resultset);
                if (recId != 0) {
                    cdao.setRecurrenceID(recId);
                }
                cdao.setLabel(setInt(i++, load_resultset));
                cdao.setTitle(setString(i++, load_resultset));
                cdao.setLocation(setString(i++, load_resultset));
                cdao.setShownAs(setInt(i++, load_resultset));
                cdao.setNumberOfAttachments(setInt(i++, load_resultset));
                cdao.setNote(setString(i++, load_resultset));
                cdao.setFullTime(setBooleanToInt(i++, load_resultset));
                cdao.setCategories(setString(i++, load_resultset));
                final String org = setString(i++, load_resultset);
                if (org != null) {
                    cdao.setOrganizer(org);
                }
                cdao.setUid(setString(i++, load_resultset));
                cdao.setFilename(setString(i++, load_resultset));
                cdao.setSequence(setInt(i++, load_resultset));
                cdao.setOrganizerId(setInt(i++, load_resultset));

                //Context of check is critical. TODO: Make independent!
                checkGeneralPermissions(oid, inFolder, readcon, so, ctx, action_folder, check_permissions, cdao, check_special_action, organizer, uniqueId);
                cdao.setPrincipal(setString(i++, load_resultset));
                cdao.setPrincipalId(setInt(i++, load_resultset));

                // ensure shared folder owner is set prior loading users
                if (FolderObject.SHARED == cdao.getFolderType() && 0 >= cdao.getSharedFolderOwner()) {
                    OXFolderAccess ofa = new OXFolderAccess(readcon, cdao.getContext());
                    if (cdao.containsParentFolderID() && 0 < cdao.getParentFolderID()) {
                        cdao.setSharedFolderOwner(ofa.getFolderOwner(cdao.getParentFolderID()));
                    } else {
                        cdao.setSharedFolderOwner(ofa.getFolderOwner(inFolder));
                    }
                }
                cdao.setUsers(cimp.getUserParticipants(cdao, readcon, so.getUserId()).getUsers());

                //Context of check is critical. TODO: Make independent!
                checkShared(oid, inFolder, so, action, action_folder, check_permissions, cdao, check_special_action);
                //Context of check is critical. TODO: Make independent!
                checkMove(oid, inFolder, readcon, so, ctx, action, action_folder, check_permissions, cdao);

                if (cdao.containsRecurrenceID()) {
                    cdao.setRecurrenceCalculator(setInt(i++, load_resultset));
                    int recurrencePosition = setInt(i++, load_resultset);
                    if (recurrencePosition != 0) {
                        cdao.setRecurrencePosition(recurrencePosition);
                    }
                    cdao.setRecurrence(setString(i++, load_resultset));
                    cdao.setDelExceptions(setString(i++, load_resultset));
                    cdao.setExceptions(setString(i++, load_resultset));
                    extractRecurringInformation(cdao);
                    if (cdao.getObjectID() == cdao.getRecurrenceID()) {
                        cdao.calculateRecurrence();
                    }
                }

                final ExternalUserParticipant[] externals =
                    ParticipantStorage.getInstance().selectExternal(ctx, readcon, cdao.getObjectID());
                cdao.setParticipants(ParticipantLogic.mergeFallback(cdao.getParticipants(), externals));
                cdao.setConfirmations(ParticipantLogic.mergeConfirmations(externals, cdao.getParticipants()));
                setAttachmentLastModified(readcon, ctx, cdao);
            } else {
                final String text = "Object " + oid + " in context " + cdao.getContextID();
                final OXException e = OXException.notFound(text);
                LOG.error("", e);
                throw e;
            }
        } finally {
            load_resultset.close();
        }
        return cdao;
    }

    private void checkGeneralPermissions(final int oid, final int inFolder, final Connection readcon, final Session so, final Context ctx, final int action_folder, boolean check_permissions, final CalendarDataObject cdao, int check_special_action, String organizer, String uniqueId) throws OXException {
        check_permissions = check_permissions && !isExternalOrganizerAndKnown(organizer, uniqueId, cdao);

        if (check_permissions && !recColl.checkPermissions(cdao, so, ctx, readcon, check_special_action, action_folder)) {
            if (action_folder != inFolder) {
                LOG.debug(
                    "Permission Exception 1 (fid!inFolder) for user:oid:fid:inFolder {}:{}:{}:{}",
                    I(so.getUserId()),
                    I(oid),
                    I(action_folder),
                    inFolder);
            }
            throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5.create(I(oid));
        } else if (!check_permissions && 0 < inFolder && inFolder == action_folder) {
            /*
             * Assign parent folder ID when not checking permissions - necessary for bug #23181 so that the parent folder is not
             * considered as 'shared' to the current user.
             */
            cdao.setParentFolderID(inFolder);
            OXFolderAccess access = new OXFolderAccess(readcon, cdao.getContext());
            cdao.setFolderType(access.getFolderType(inFolder, so.getUserId()));
        }
    }

    /**
     * OLOX2 Hack to provide the posibility to change existing appointments without using the iTip API.
     * @param cdao
     * @return
     */
    private boolean isExternalOrganizerAndKnown(String organizer, String uniqueId, CalendarDataObject cdao) {
        if (cdao.getOrganizerId() > 0) {
            return false;
        }
        if (cdao.getOrganizer() == null || cdao.getUid() == null) {
            return false;
        }
        if (!cdao.getOrganizer().equals(organizer)) {
            return false;
        }
        if (!cdao.getUid().equals(uniqueId)) {
            return false;
        }
        return true;
    }

    private void checkShared(final int oid, final int inFolder, final Session so, final int action, final int action_folder, final boolean check_permissions, final CalendarDataObject cdao, int check_special_action) throws OXException {
        if (check_permissions && cdao.getEffectiveFolderId() != inFolder) {
            if (cdao.getFolderType() != FolderObject.SHARED && check_special_action == action) {
                LOG.debug(
                    "Permission Exception 2 (fid!inFolder) for user:oid:fid:inFolder:action {}:{}:{}:{}:{}",
                    I(so.getUserId()),
                    I(oid),
                    I(cdao.getEffectiveFolderId()),
                    I(inFolder),
                    I(action));
                throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.create();
            } else if (cdao.getEffectiveFolderId() != inFolder && check_special_action == action) {
                LOG.debug(
                    "Permission Exception 3 (fid!inFolder) for user:oid:fid:inFolder:action {}:{}:{}:{}:{}",
                    I(so.getUserId()),
                    I(oid),
                    I(cdao.getEffectiveFolderId()),
                    I(inFolder),
                    I(action));
                throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_3.create();
            }
        }
    }

    private void checkMove(final int oid, final int inFolder, final Connection readcon, final Session so, final Context ctx, final int action, final int action_folder, final boolean check_permissions, final CalendarDataObject cdao) throws OXException {
        if (check_permissions && action == UPDATE && inFolder != action_folder) {
            if (!recColl.checkPermissions(cdao, so, ctx, readcon, DELETE, inFolder)) { // Move means to check delete
                if (inFolder != action_folder) {
                    LOG.debug(
                        "Permission Exception 4 (fid!inFolder) for user:oid:fid:inFolder {}:{}:{}:{}",
                        I(so.getUserId()),
                        I(oid),
                        I(action_folder),
                        I(inFolder));
                }
                throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_4.create();
            }
        }
    }

    private static final String setString(final int i, final ResultSet string_rs) throws SQLException {
        final String r = string_rs.getString(i);
        if (!string_rs.wasNull()) {
            return r;
        }
        return null;
    }

    private static final Timestamp setTimestamp(final int i, final ResultSet ts_rs) throws SQLException {
        final Timestamp r = ts_rs.getTimestamp(i);
        if (!ts_rs.wasNull()) {
            return r;
        }
        return null;
    }

    private static final Timestamp setTimestampFromLong(final int i, final ResultSet stl_rs) throws SQLException {
        final Timestamp r = new Timestamp(stl_rs.getLong(i));
        if (!stl_rs.wasNull()) {
            return r;
        }
        return null;
    }

    private static final int setInt(final int i, final ResultSet si_rs) throws SQLException {
        return si_rs.getInt(i);
    }

    private static final Date setDate(final int i, final ResultSet sd_rs) throws SQLException {
        final Date r = sd_rs.getTimestamp(i);
        if (!sd_rs.wasNull()) {
            return r;
        }
        return null;
    }

    private static boolean setBooleanToInt(final int i, final ResultSet sbti_rs) throws SQLException {
        final int r = sbti_rs.getInt(i);
        if (r == 0) {
            return false;
        }
        return true;
    }

    static int fillUpdateArray(final CalendarDataObject cdao, final CalendarDataObject edao, final int ucols[]) {
        int uc = 0;
        if (cdao.containsSequence() && recColl.check(I(cdao.getSequence()), I(edao.getSequence())) && recColl.getFieldName(CalendarObject.SEQUENCE) != null) {
            ucols[uc++] = CalendarObject.SEQUENCE;
        }
        if (cdao.containsTitle() && recColl.check(cdao.getTitle(), edao.getTitle()) && recColl.getFieldName(CalendarObject.TITLE) != null) {
            ucols[uc++] = CalendarObject.TITLE;
        }
        if (cdao.containsShownAs() && recColl.check(I(cdao.getShownAs()), I(edao.getShownAs())) && recColl.getFieldName(Appointment.SHOWN_AS) != null) {
            ucols[uc++] = Appointment.SHOWN_AS;
        }
        if (cdao.containsStartDate() && cdao.getStartDate() != null && recColl.check(
            Long.valueOf(cdao.getStartDate().getTime()),
            Long.valueOf(edao.getStartDate().getTime())) && recColl.getFieldName(CalendarObject.START_DATE) != null) {
            ucols[uc++] = CalendarObject.START_DATE;
        }
        if (cdao.containsEndDate() && cdao.getEndDate() != null && recColl.check(Long.valueOf(cdao.getEndDate().getTime()), Long.valueOf(edao.getEndDate().getTime())) && recColl.getFieldName(CalendarObject.END_DATE) != null) {
            ucols[uc++] = CalendarObject.END_DATE;
        }
        if (cdao.containsLocation() && recColl.check(cdao.getLocation(), edao.getLocation()) && recColl.getFieldName(Appointment.LOCATION) != null) {
            ucols[uc++] = Appointment.LOCATION;
        }
        if (cdao.containsNote() && recColl.check(cdao.getNote(), edao.getNote()) && recColl.getFieldName(CalendarObject.NOTE) != null) {
            ucols[uc++] = CalendarObject.NOTE;
        }
        if (cdao.containsFullTime() && recColl.check(Boolean.valueOf(cdao.getFullTime()), Boolean.valueOf(edao.getFullTime())) && recColl.getFieldName(Appointment.FULL_TIME) != null) {
            ucols[uc++] = Appointment.FULL_TIME;
        }
        if (cdao.containsCategories() && recColl.check(cdao.getCategories(), edao.getCategories()) && recColl.getFieldName(CommonObject.CATEGORIES) != null) {
            ucols[uc++] = CommonObject.CATEGORIES;
        }
        if (cdao.containsLabel() && recColl.check(I(cdao.getLabel()), I(edao.getLabel())) && recColl.getFieldName(CommonObject.COLOR_LABEL) != null) {
            ucols[uc++] = CommonObject.COLOR_LABEL;
        }
        if (cdao.containsPrivateFlag() && recColl.check(Boolean.valueOf(cdao.getPrivateFlag()), Boolean.valueOf(edao.getPrivateFlag())) && recColl.getFieldName(CommonObject.PRIVATE_FLAG) != null) {
            ucols[uc++] = CommonObject.PRIVATE_FLAG;
        }
        if (cdao.containsParentFolderID() && recColl.check(I(cdao.getGlobalFolderID()), I(edao.getGlobalFolderID())) && recColl.getFieldName(FolderChildObject.FOLDER_ID) != null) {
            ucols[uc++] = FolderChildObject.FOLDER_ID;
        }
        if ((cdao.containsRecurrenceString() || cdao.containsRecurrenceType()) && recColl.check(cdao.getRecurrence(), edao.getRecurrence()) && recColl.getFieldName(CalendarObject.RECURRENCE_TYPE) != null) {
            ucols[uc++] = CalendarObject.RECURRENCE_TYPE;
        }
        if (cdao.containsRecurrenceID() && recColl.check(I(cdao.getRecurrenceID()), I(edao.getRecurrenceID())) && recColl.getFieldName(CalendarObject.RECURRENCE_ID) != null) {
            ucols[uc++] = CalendarObject.RECURRENCE_ID;
        }
        if (cdao.containsDeleteExceptions() && recColl.check(cdao.getDelExceptions(), edao.getDelExceptions()) && recColl.getFieldName(CalendarObject.DELETE_EXCEPTIONS) != null) {
            ucols[uc++] = CalendarObject.DELETE_EXCEPTIONS;
            // cdao.setDeleteExceptions(recColl.mergeExceptions(cdao.getDeleteException(), edao.getDeleteException()));
        }
        if (cdao.containsChangeExceptions() && recColl.check(cdao.getExceptions(), edao.getExceptions()) && recColl.getFieldName(CalendarObject.CHANGE_EXCEPTIONS) != null) {
            ucols[uc++] = CalendarObject.CHANGE_EXCEPTIONS;
            // cdao.setChangeExceptions(recColl.mergeExceptions(cdao.getChangeException(), edao.getChangeException()));
        }
        if (cdao.containsRecurrencePosition() && recColl.check(I(cdao.getRecurrencePosition()), I(edao.getRecurrencePosition())) && recColl.getFieldName(CalendarObject.RECURRENCE_POSITION) != null) {
            ucols[uc++] = CalendarObject.RECURRENCE_POSITION;
        }
        if (cdao.containsNumberOfAttachments() && recColl.check(I(cdao.getNumberOfAttachments()), I(edao.getNumberOfAttachments())) && recColl.getFieldName(CommonObject.NUMBER_OF_ATTACHMENTS) != null) {
            ucols[uc++] = CommonObject.NUMBER_OF_ATTACHMENTS;
        }

        if (recColl.check(I(cdao.getRecurrenceCalculator()), I(edao.getRecurrenceCalculator())) && recColl.getFieldName(CalendarObject.RECURRENCE_CALCULATOR) != null) {
            ucols[uc++] = CalendarObject.RECURRENCE_CALCULATOR;
        }
        if (cdao.containsOrganizer() && recColl.check(cdao.getOrganizer(), edao.getOrganizer()) && recColl.getFieldName(CalendarObject.ORGANIZER) != null) {
            ucols[uc++] = CalendarObject.ORGANIZER;
        }
        if (cdao.containsOrganizerId() && recColl.check(I(cdao.getOrganizerId()), I(edao.getOrganizerId())) && recColl.getFieldName(CalendarObject.ORGANIZER_ID) != null) {
            ucols[uc++] = CalendarObject.ORGANIZER_ID;
        }
        if (cdao.containsPrincipal() && recColl.check(cdao.getPrincipal(), edao.getPrincipal()) && recColl.getFieldName(CalendarObject.PRINCIPAL) != null) {
            ucols[uc++] = CalendarObject.PRINCIPAL;
        }
        if (cdao.containsPrincipalId() && recColl.check(I(cdao.getPrincipalId()), I(edao.getPrincipalId())) && recColl.getFieldName(CalendarObject.PRINCIPAL_ID) != null) {
            ucols[uc++] = CalendarObject.PRINCIPAL_ID;
        }
        if (cdao.containsFilename() && recColl.check(cdao.getFilename(), edao.getFilename()) && recColl.getFieldName(CommonObject.FILENAME) != null) {
            ucols[uc++] = CommonObject.FILENAME;
        }
// Deactivated, because feature is postponed.
//        if (cdao.containsTimezone() && recColl.check(cdao.getTimezone(), edao.getTimezone()) && recColl.getFieldName(CalendarDataObject.TIMEZONE) != null && canChangeTZ(cdao, edao)) {
//            ucols[uc++] = CalendarDataObject.TIMEZONE;
//        }
        return uc;
    }

    /**
     * Checks, if it is allowed to change the timezone.
     *
     * @param cdao
     * @param edao
     * @return
     */
    private static boolean canChangeTZ(CalendarDataObject cdao, CalendarDataObject edao) {
        if (edao.getRecurrenceType() != CalendarDataObject.NO_RECURRENCE) {
            return false;
        }

        if (edao.getFullTime()) {
            return false;
        }

        if (cdao.getRecurrenceType() != CalendarDataObject.NO_RECURRENCE) {
            return false;
        }

        if (cdao.getFullTime()) {
            return false;
        }

        return true;
    }

    /**
     * Performs some preparations on specified calendar data object
     *
     * @param cdao The calendar data object to check
     * @param edao The storage's version of calendar data object to check; may be <code>null</code> on an insert
     * @param uid The user ID
     * @param inFolder The folder ID
     * @param timezone The time zone
     * @return <code>true</code> if an insert shall be performed; otherwise <code>false</code> for an update
     * @throws OXException If an OX error occurs
     * @throws Exception Of an error occurs
     */
    public boolean prepareUpdateAction(final CalendarDataObject cdao, final CalendarDataObject edao, final int uid, final int inFolder, final String timezone) throws OXException {
        if (cdao.getContext() == null) {
            throw OXCalendarExceptionCodes.CONTEXT_NOT_SET.create();
        }
        final OXFolderAccess ofa = new OXFolderAccess(cdao.getContext());
        if (ofa.getFolderModule(inFolder) != FolderObject.CALENDAR) {
            throw OXCalendarExceptionCodes.NON_CALENDAR_FOLDER.create();
        }

        final boolean isInsert = !cdao.containsObjectID();
        if (isInsert) {
            checkInsertMandatoryFields(cdao);
            handleFullTime(cdao, null);

            if (cdao.isSequence()) {
                cdao.setRecurrenceCalculator(((int) ((cdao.getEndDate().getTime() - cdao.getStartDate().getTime()) / Constants.MILLI_DAY)));
                if (!cdao.containsTimezone()) {
                    cdao.setTimezone(timezone);
                }
                recColl.fillDAO(cdao);

                recColl.checkRecurring(cdao);
                // cdao.setRecurrenceCalculator(((int) ((cdao.getEndDate().getTime() - cdao.getStartDate().getTime()) /
                // Constants.MILLI_DAY)));
                cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
                Date realStart = calculateRealRecurringStartDate(cdao);
                if (realStart != null) {
                    cdao.setStartDate(realStart);
                }
            } else {
                cdao.setRecurrence(CalendarCollectionService.NO_DS);
            }

            cdao.setCreatedBy(uid);
            cdao.setCreationDate(new Date());
            cdao.setModifiedBy(uid);
            cdao.setFolderType(ofa.getFolderType(inFolder, uid));
            if (cdao.getFolderType() == FolderObject.PRIVATE) {
                cdao.setPrivateFolderID(inFolder);
                cdao.setGlobalFolderID(0);
            }
            if (cdao.getFolderType() == FolderObject.SHARED) {
                final int folderOwner = ofa.getFolderOwner(inFolder);
                cdao.setCreatedBy(folderOwner);
                cdao.setModifiedBy(folderOwner);
            }
            // Strange bugs can be produced if the recurrence identifier is set to some value on insert.
            cdao.removeRecurrenceID();
        } else {
            if (!cdao.containsModifiedBy()) {
                cdao.setModifiedBy(uid);
            }

            if (cdao.containsStartDate() && cdao.getStartDate() == null) {
                throw OXCalendarExceptionCodes.FIELD_NULL_VALUE.create("Start Date");
            }

            if (cdao.containsEndDate() && cdao.getEndDate() == null) {
                throw OXCalendarExceptionCodes.FIELD_NULL_VALUE.create("End Date");
            }

            /*
             * if (!cdao.containsStartDate() || cdao.getStartDate() == null) { cdao.setStartDate((Date) edao.getStartDate().clone()); } if
             * (!cdao.containsEndDate() || cdao.getEndDate() == null) { cdao.setEndDate((Date) edao.getEndDate().clone()); }
             */
            handleFullTime(cdao, edao);
            if (cdao.isSequence()) {
                if (!cdao.containsTimezone()) {
                    cdao.setTimezone(timezone);
                }
                cdao.setRecurrenceID(edao.getRecurrenceID());
                recColl.fillDAO(cdao);
            } else if (edao.isSequence() && edao.getObjectID() != edao.getRecurrenceID()) {
                // this is a change exception.
                if (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null && !cdao.getRecurrenceDatePosition().equals(
                    edao.getRecurrenceDatePosition())) {
                    /*
                     * Deny change of recurring position in a change exception
                     */
                    throw OXCalendarExceptionCodes.INVALID_RECURRENCE_POSITION_CHANGE.create();
                } else if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0 && cdao.getRecurrencePosition() != edao.getRecurrencePosition()) {
                    /*
                     * Deny change of recurring position in a change exception
                     */
                    throw OXCalendarExceptionCodes.INVALID_RECURRENCE_POSITION_CHANGE.create();
                } else if (cdao.containsInterval() && cdao.getInterval() > 0 && cdao.getInterval() != edao.getInterval()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw OXCalendarExceptionCodes.INVALID_RECURRENCE_TYPE_CHANGE.create();
                } else if (cdao.containsDays() && cdao.getDays() > 0 && cdao.getDays() != edao.getDays()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw OXCalendarExceptionCodes.INVALID_RECURRENCE_TYPE_CHANGE.create();
                } else if (cdao.containsDayInMonth() && cdao.getDayInMonth() > 0 && cdao.getDayInMonth() != edao.getDayInMonth()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw OXCalendarExceptionCodes.INVALID_RECURRENCE_TYPE_CHANGE.create();
                } else if (cdao.containsMonth() && cdao.getMonth() > 0 && cdao.getMonth() != edao.getMonth()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw OXCalendarExceptionCodes.INVALID_RECURRENCE_TYPE_CHANGE.create();
                }
                // Not overwriting the recurrence position. This must give the exception INVALID_RECURRENCE_POSITION_CHANGE.
                // Keep the recurrence pattern
                cdao.setRecurrence(edao.getRecurrence());
                cdao.setRecurrenceID(edao.getRecurrenceID());
            }

            if (cdao.containsParentFolderID() && inFolder != cdao.getParentFolderID()) {
                cdao.setFolderMove(true);
            }

            if (cdao.containsParentFolderID()) {
                cdao.setFolderType(ofa.getFolderType(cdao.getParentFolderID(), uid));
            } else {
                cdao.setFolderType(ofa.getFolderType(inFolder, uid));
            }
            if (!cdao.containsParticipants() && !cdao.containsUserParticipants()) {
                // no participants defined at all. Reuse old participants if conflicts need to be checked.
                cdao.setParticipants(edao.getParticipants());
                cdao.setUsers(edao.getUsers());
            }
        }

        if (cdao.getFolderType() == FolderObject.PRIVATE) {
            // create in/move to private folder, update in private folder and current user is missing: add it
            if (!cdao.containsParticipants()) {
                if (null != edao && null != edao.getParticipants()) {
                    cdao.setParticipants(edao.getParticipants());
                }
            }
            if (!cdao.containsUserParticipants()) {
                if (null != edao && null != edao.getUsers()) {
                    cdao.setUsers(edao.getUsers());
                }
            }
            final UserParticipant up = new UserParticipant(uid);
            up.setConfirm(CalendarObject.ACCEPT);
            recColl.checkAndFillIfUserIsParticipant(cdao, up);
            if (null != edao && FolderObject.SHARED == edao.getFolderType()) {
                recColl.removeUserParticipant(cdao, edao.getSharedFolderOwner());
            }
        } else if (cdao.getFolderType() == FolderObject.SHARED) {
            if (cdao.containsParentFolderID()) {
                cdao.setSharedFolderOwner(ofa.getFolderOwner(cdao.getParentFolderID()));
            } else {
                cdao.setSharedFolderOwner(ofa.getFolderOwner(inFolder));
            }
            final UserParticipant up = new UserParticipant(cdao.getSharedFolderOwner());
            if (isInsert) {
                up.setConfirm(CalendarObject.ACCEPT);
            }
            if (isInsert) {
                recColl.checkAndFillIfUserIsParticipant(cdao, up);
            } else {
                if (!recColl.checkIfUserIsParticipant(edao, up)) {
                    if (edao.getFolderType() == FolderObject.PRIVATE) {
                        recColl.removeParticipant(cdao, uid);
                        recColl.removeUserParticipant(cdao, uid);
                    }
                    if (null != edao && FolderObject.SHARED == edao.getFolderType()) {
                        recColl.removeUserParticipant(cdao, edao.getSharedFolderOwner());
                    }
                    recColl.checkAndFillIfUserIsUser(cdao, up);
                    recColl.checkAndFillIfUserIsParticipant(cdao, up);
                }
            }
        } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
            if (!cdao.containsParticipants()) {
                if (null != edao && null != edao.getParticipants()) {
                    cdao.setParticipants(edao.getParticipants());
                    cdao.setUsers(edao.getUsers());
                }
            }
            final UserParticipant up = new UserParticipant(uid);
            up.setConfirm(CalendarObject.ACCEPT);
            recColl.checkAndConfirmIfUserUserIsParticipantInPublicFolder(cdao, up);
        }

        if ((isInsert || cdao.containsUserParticipants()) && cdao.getFolderType() != FolderObject.PUBLIC && !cdao.isExternalOrganizer()) {
            UserParticipant p = null;
            if (cdao.getFolderType() == FolderObject.SHARED) {
                p = new UserParticipant(cdao.getSharedFolderOwner());
            } else {
                p = new UserParticipant(uid);
            }
            p.setConfirm(CalendarObject.ACCEPT);
            recColl.checkAndFillIfUserIsUser(cdao, p);
        }
        if (isInsert && (cdao.getParticipants() == null || cdao.getParticipants().length == 0) && cdao.getFolderType() == FolderObject.PUBLIC) {
            final Participant np[] = new Participant[1];
            final Participant up = new UserParticipant(uid);
            np[0] = up;
            cdao.setParticipants(np);
        }

        if (!cdao.containsTimezone()) {
            cdao.setTimezone(timezone);
        }
        simpleDataCheck(cdao, edao, uid);
        fillUserParticipants(cdao, edao);
        if (isInsert) {
            recColl.updateDefaultStatus(cdao, cdao.getContext(), uid, inFolder);
        }
        return isInsert;
    }

    /**
     * Checks if full-time flag is set in specified parameter <code>cdao</code>. If so its start date and end date is changed to last the
     * whole day.
     *
     * @param cdao The current calendar object
     * @param edao The storage calendar object used to set start/end date if not available in specified parameter <code>cdao</code>
     * @throws OXException
     */
    private static final void handleFullTime(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        if (cdao.getFullTime()) {
            if (cdao.getStartDate() != null && cdao.getEndDate() != null) {
                final long mod = cdao.getStartDate().getTime() % Constants.MILLI_DAY;
                if (mod != 0) {
                    cdao.setStartDate(new Date(cdao.getStartDate().getTime() - mod));
                }
                if ((cdao.getStartDate().getTime() == cdao.getEndDate().getTime()) || (cdao.getEndDate().getTime() - cdao.getStartDate().getTime() < Constants.MILLI_DAY)) {
                    cdao.setEndDate(new Date(cdao.getStartDate().getTime() + Constants.MILLI_DAY));
                } else if (cdao.getEndDate().getTime() % Constants.MILLI_DAY != 0) {
                    cdao.setEndDate(new Date(
                        (cdao.getStartDate().getTime() + (((cdao.getEndDate().getTime() - cdao.getStartDate().getTime()) / Constants.MILLI_DAY) * Constants.MILLI_DAY))));
                }
            } else if (edao != null) {
                Date startDate = edao.getStartDate();
                Date endDate = edao.getEndDate();
                if (edao.isSequence()) {
                    final CalendarDataObject temp = edao.clone();
                    final RecurringResultsInterface rss = recColl.calculateFirstRecurring(temp);
                    if (rss != null) {
                        final RecurringResultInterface recurringResult = rss.getRecurringResult(0);
                        startDate = new Date(recurringResult.getStart());
                        endDate = new Date(recurringResult.getEnd());
                    }
                }
                final long mod = startDate.getTime() % Constants.MILLI_DAY;
                if (mod != 0) {
                    cdao.setStartDate(new Date(startDate.getTime() - mod));
                } else {
                    cdao.setStartDate(startDate);
                }
                if ((cdao.getStartDate().getTime() == endDate.getTime()) || (endDate.getTime() - cdao.getStartDate().getTime() < Constants.MILLI_DAY)) {
                    cdao.setEndDate(new Date(cdao.getStartDate().getTime() + Constants.MILLI_DAY));
                } else if (endDate.getTime() % Constants.MILLI_DAY != 0) {
                    cdao.setEndDate(new Date(
                        (cdao.getStartDate().getTime() + (((endDate.getTime() - cdao.getStartDate().getTime()) / Constants.MILLI_DAY) * Constants.MILLI_DAY))));
                }
            }
        }
    }

    /* This fixes bug 16107 */
    protected static void handleChangeFromFullTimeToNormal(final CalendarDataObject newApp, final CalendarDataObject oldApp) throws OXException {
        if (oldApp == null || newApp == null || !oldApp.getFullTime() || newApp.getFullTime() || newApp.getUntil() == null) {
            return;
        }
        newApp.setEndDate(calculateRealRecurringEndDate(newApp, oldApp));
    }

    private static final Date calculateRealRecurringEndDate(final CalendarDataObject cdao, CalendarDataObject edao) throws OXException {
        String tzid = cdao.getTimezone() == null ? edao.getTimezone() : cdao.getTimezone();
        boolean fulltime = cdao.containsFullTime() ? cdao.getFullTime() : (edao != null ? edao.getFullTime() : false);
        return cdao.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE ? calculateImplictEndOfSeries(edao, tzid, fulltime) : calculateImplictEndOfSeries(cdao, tzid, fulltime);
    }

    /**
     * Calclulates the start date of the first occurence of the series.
     * This might differ from the start_date if the series begin before the first occurrence.
     * @param cdao
     * @return
     * @throws OXException
     */
    private static Date calculateRealRecurringStartDate(CalendarDataObject cdao) throws OXException {
        RecurringResultsInterface firstRecurring = recColl.calculateFirstRecurring(cdao);
        if (firstRecurring != null) {
            RecurringResultInterface rs = firstRecurring.getRecurringResult(0);
            if (rs != null) {
                return new Date(rs.getStart());
            }
        }
        return null;
    }

    private static Date calculateImplictEndOfSeries(CalendarDataObject cdao, String tzid, boolean fulltime) throws OXException {
        if (cdao.containsUntil() && cdao.getUntil() != null) {
            return calculateRealRecurringEndDate(cdao.getUntil(), cdao.getEndDate(), fulltime, cdao.getRecurrenceCalculator());
        }
        CalendarDataObject clone = cdao.clone();
        RecurringResultsInterface rresults = recColl.calculateRecurringIgnoringExceptions(clone, 0, 0, 0);
        RecurringResultInterface rresult = rresults.getRecurringResult(rresults.size()-1);
        Date retval = new Date(rresult.getEnd());

        TimeZone tz = TimeZone.getTimeZone(tzid);
        int startOffset = tz.getOffset(calculateRealRecurringStartDate(cdao).getTime());
        int endOffset = tz.getOffset(retval.getTime());
        if (!fulltime) {
            retval.setTime(retval.getTime() + endOffset - startOffset);
        }
        return retval;
    }

    private static final Date calculateRealRecurringEndDate(final Date untilDate, final Date endDate, final boolean isFulltime, int recCal) {
        long until = untilDate.getTime();
        // Extract time out of until date
        long mod = until % Constants.MILLI_DAY;
        if (mod > 0) {
            until = until - mod;
        }

        // Extract time out of end date
        mod = (endDate.getTime()) % Constants.MILLI_DAY;

        if (recCal > 0) {
            until += Constants.MILLI_DAY * recCal;
        }

        return new Date(until + mod);
    }

    private static final void calculateAndSetRealRecurringStartAndEndDate(final CalendarDataObject cdao, final CalendarDataObject edao) {
        long startDate = edao.getStartDate().getTime();
        TimeZone tz = null;
        if (cdao.getTimezone() != null) {
            tz = Tools.getTimeZone(cdao.getTimezone());
        } else if (edao.getTimezone() != null) {
            tz = Tools.getTimeZone(edao.getTimezone());
        } else {
            tz = Tools.getTimeZone("UTC");
        }
        final int startDateZoneOffset = tz.getOffset(startDate);
        final long endDate = cdao.containsUntil() && cdao.getUntil() != null ? cdao.getUntil().getTime() : edao.getUntil().getTime();
        long startTime = cdao.getStartDate() == null ? edao.getStartDate().getTime() : cdao.getStartDate().getTime();
        long endTime = cdao.getEndDate() == null ? edao.getEndDate().getTime() : cdao.getEndDate().getTime();
        final int startTimeZoneOffset = tz.getOffset(startTime);
        startTime = startTime % Constants.MILLI_DAY;
        endTime = endTime % Constants.MILLI_DAY;
        // FIXME daylight saving time offset
        cdao.setStartDate(recColl.calculateRecurringDate(startDate, startTime, startTimeZoneOffset - startDateZoneOffset));
        cdao.setEndDate(recColl.calculateRecurringDate(endDate, endTime, startTimeZoneOffset - startDateZoneOffset));
    }

    @Override
    public final boolean hasNext() throws OXException {
        if (co_rs != null && result_counter != MAX_RESULT_LIMIT) {
            final boolean ret = has_next;
            if (from == 0 && to == 0) {
                return ret;
            }
            if (ret) {
                from++;
                if (from <= to) {
                    return ret;
                }
            } else {
                return ret;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return -1;
    }

    public boolean hasSize() {
        return false;
    }

    @Override
    public void addWarning(final OXException warning) {
        warnings.add(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    private void rsNext(final boolean first) {
        if (co_rs != null) {
            try {
                has_next = co_rs.next();
                if (!first) {
                    result_counter++;
                }
            } catch (final SQLException sqle) {
                has_next = false;
                LOG.error("Error while getting next result set", sqle);
            }
        }
    }

    @Override
    public final CalendarDataObject next() throws OXException {
        if (hasNext()) {
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(c);
            int g = 1;
            boolean bbs = false;
            if (co_rs == null || cols == null) {
                throw OXCalendarExceptionCodes.SEARCH_ITERATOR_NULL.create();
            }
            try {
                final boolean fastFetch = CachedCalendarIterator.CACHED_ITERATOR_FAST_FETCH.get();
                for (int col : cols) {
                    final FieldFiller ff = FILLERS.get(I(col));
                    if (null == ff) {
                        /*
                         * Fields not covered by FieldFiller: USERS, PARTICIPANTS, and FOLDER_ID
                         */
                        if (CalendarObject.USERS == col) {
                            if (cdao.containsUserParticipants() || fastFetch) {
                                cdao.setFillUserParticipants();
                            } else {
                                final Participants users = cimp.getUserParticipants(cdao, readcon, uid);
                                cdao.setUsers(users.getUsers());
                                bbs = true;
                            }
                        } else if (CalendarObject.PARTICIPANTS == col) {
                            if (fastFetch) {
                                cdao.setFillParticipants();
                            } else {
                                final Participants participants = cimp.getParticipants(cdao, readcon);
                                cdao.setParticipants(participants.getList());
                            }
                        } else if (CalendarObject.CONFIRMATIONS == col) {
                            if (fastFetch) {
                                cdao.setFillConfirmations();
                            } else {
                                final ExternalUserParticipant[] externals =
                                    ParticipantStorage.getInstance().selectExternal(cdao.getContext(), readcon, cdao.getObjectID());
                                cdao.setParticipants(ParticipantLogic.mergeFallback(cdao.getParticipants(), externals));
                                cdao.setConfirmations(ParticipantLogic.mergeConfirmations(externals, cdao.getParticipants()));
                            }
                        } else if (FolderChildObject.FOLDER_ID == col) {
                            if (recColl.getFieldName(FolderChildObject.FOLDER_ID) != null) {
                                if (oids == null) {
                                    if (requested_folder == 0) {
                                        final int x = setInt(g++, co_rs);
                                        if (x > 0) {
                                            cdao.setGlobalFolderID(x);
                                        } else {
                                            if (bbs) {
                                                cdao.setGlobalFolderID(cdao.getEffectiveFolderId());
                                            } else if (!fastFetch) {
                                                final Participants users = cimp.getUserParticipants(cdao, readcon, uid);
                                                cdao.setUsers(users.getUsers());
                                                cdao.setGlobalFolderID(cdao.getEffectiveFolderId());
                                            } else {
                                                try {
                                                    cdao.setGlobalFolderID(co_rs.getInt("pdm.pfid"));
                                                } catch (final SQLException sqle) {
                                                    if (sqle.getSQLState().equals("S0022")) { // Not found TODO: add this column to other
                                                        // search statements.
                                                        cdao.setFillFolderID();
                                                    } else {
                                                        throw sqle;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        cdao.setGlobalFolderID(requested_folder);
                                        g++;
                                    }
                                } else {
                                    int index = result_counter;
                                    if (oids[index][0] != cdao.getObjectID()) {
                                        for (int x = 0; x < oids.length; x++) {
                                            if (oids[x][0] == cdao.getObjectID()) {
                                                index = x;
                                                break;
                                            }
                                        }
                                    }
                                    cdao.setGlobalFolderID(oids[index][1]);
                                    g++;
                                }
                            }
                        } else if (CommonObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT == col) {
                            if (fastFetch) {
                                cdao.setFillLastModifiedOfNewestAttachment(true);
                            } else {
                                setAttachmentLastModified(readcon, c, cdao);
                            }
                        } else {
                            throw SearchIteratorExceptionCodes.NOT_IMPLEMENTED.create(I(col)).setPrefix("APP");
                        }
                    } else {
                        ff.fillField(cdao, g++, co_rs);
                    }
                }
            } catch (final SQLException sqle) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
            }

            extractRecurringInformation(cdao);
            if (strict && oids != null) {
                int index = result_counter;
                if (oids[index][0] != cdao.getObjectID()) {
                    for (int a = 0; a < oids.length; a++) {
                        if (oids[a][0] == cdao.getObjectID()) {
                            index = a;
                            break;
                        }
                    }
                }
                final int check_folder_id = oids[index][1];
                if (!cdao.containsParticipants()) {
                    try {
                        final Participants participants = cimp.getParticipants(cdao, readcon);
                        cdao.setParticipants(participants.getList());
                    } catch (final SQLException e) {
                        LOG.error("Error while checking special permissions", e);
                    }
                }

                if (check_folder_id != cdao.getParentFolderID()) {
                    LOG.error("Object Not Found: Object not found : uid:oid:fid:InFolder {}{}{}{}{}{}{}", so.getUserId(), ':', cdao.getObjectID(), ':', cdao.getParentFolderID(), ':', check_folder_id, new Throwable());
                    throw OXException.notFound("");
                }
                cdao.setActionFolder(check_folder_id);

                if (!recColl.checkPermissions(cdao, so, c, readcon, CalendarOperation.READ, check_folder_id)) {
                    if (LOG.isDebugEnabled()) {
                        final StringBuilder colss = new StringBuilder(cols.length << 3);
                        for (int a = 0; a < cols.length; a++) {
                            String fn = recColl.getFieldName(cols[a]);
                            if (fn == null) {
                                fn = String.valueOf(cols[a]);
                            }
                            if (a > 0) {
                                colss.append(',');
                            }
                            colss.append(fn);
                        }
                        LOG.debug("Permission Exception (fid!inFolder) for user:oid:fid:cols {}:{}:{}:{}",
                            I(so.getUserId()),
                            I(cdao.getObjectID()),
                            I(oids[index][1]),
                            colss.toString());
                    }
                    throw OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5.create(I(cdao.getObjectID()));
                }
            }
            rsNext(false);
            return cdao;
        }
        has_next = false;
        return null;
    }

    public boolean getIncludePrivateAppointmentsOfSharedFolderOwner() {
        return this.includePrivateAppointmentsOfSharedFolderOwner;
    }

    public void setIncludePrivateAppointmentsOfSharedFolderOwner(final boolean include) {
        this.includePrivateAppointmentsOfSharedFolderOwner = include;
    }

    /**
     * This method does the complex filling of the calendar object if some series appointment or some series exception appointment is
     * loaded.
     *
     * @param cdao loaded object.
     * @throws OXException if extracting the recurrence pattern string fails.
     */
    private static void extractRecurringInformation(final CalendarDataObject cdao) throws OXException {
        if (cdao.isSequence()) {
            recColl.fillDAO(cdao);
            if (cdao.getObjectID() == cdao.getRecurrenceID()) {
                if (cdao.containsOccurrence() && !cdao.containsUntil()) {
                    // INFO: Somebody needs this value, have to check for side effects
                    // cdao.setUntil(new Date(recColl.normalizeLong((cdao.getStartDate().getTime() + (Constants.MILLI_DAY *
                    // cdao.getRecurrenceCalculator())))));
                }
            } else {
                // Recurring type on a change exception must be removed.
                // Otherwise somebody may treat it as a series appointment.
                recColl.removeRecurringType(cdao);
                if (cdao.getExceptions() != null) {
                    try {
                        final long exc = Long.parseLong(cdao.getExceptions());
                        cdao.setRecurrenceDatePosition(new Date(exc));
                    } catch (final NumberFormatException nfe) {
                        LOG.warn("Unable to calculate exception oid:context:exceptions {}:{}:{}", cdao.getObjectID(), cdao.getContextID(), cdao.getExceptions());
                    }
                }
            }
        }
    }

    public final SearchIterator<CalendarDataObject> setResultSet(final ResultSet rs, final PreparedStatement prep, final int[] cols, final CalendarSqlImp cimp, final Connection readcon, final int from, final int to, final Session so, final Context ctx) throws SQLException {
        this.co_rs = rs;
        this.prep = prep;
        this.cols = cols;
        this.cimp = cimp;
        this.c = ctx;
        this.readcon = readcon;
        this.from = from;
        this.to = to;
        this.uid = so.getUserId();
        this.so = so;
        if (from != 0 && to != 0) {
            rs.absolute(from);
        }
        rsNext(true);
        return this;
    }

    public final void setRequestedFolder(final int requested_folder) {
        this.requested_folder = requested_folder;
    }

    final void setOIDS(final boolean strict, final int oids[][]) {
        this.strict = strict;
        this.oids = oids;
    }

    public static final void fillUserParticipants(CalendarDataObject cdao, CalendarDataObject edao) throws OXException {
        final Participant participants[] = cdao.getParticipants();
        final UserParticipant[] users = cdao.getUsers();
        if (participants == null) {
            return;
        }
        Participants userparticipants = null;
        for (final Participant p : participants) {
            if (userparticipants == null) {
                userparticipants = new Participants(null);
            }
            if (p.getType() == Participant.GROUP) {
                final GroupStorage gs = GroupStorage.getInstance();
                final Group g = gs.getGroup(p.getIdentifier(), cdao.getContext());
                final int m[] = g.getMember();
                for (int element : m) {
                    final UserParticipant up = new UserParticipant(element);
                    if (!userparticipants.containsUserParticipant(up) && shouldAdd((GroupParticipant) p, up, edao)) {
                        userparticipants.add(up, users);
                    }
                }
            } else if (p.getType() == Participant.USER) {
                final UserParticipant up = new UserParticipant(p.getIdentifier());
                up.setDisplayName(p.getDisplayName());
                if (!userparticipants.containsUserParticipant(up)) {
                    userparticipants.add(up, users);
                }
            } else if (p.getType() == Participant.RESOURCE) {
                cdao.setContainsResources(true);
            }
        }
        if (userparticipants != null) {
            cdao.setUsers(userparticipants.getUsers());
        }
    }

    /**
     * Checks if a user participant should be added to the new list of users.
     * Depends on the existtence of the user in the old Calendar Object and if the user is participant of the group.
     *
     * @param gp
     * @param up
     * @param edao
     * @return
     */
    private static boolean shouldAdd(GroupParticipant gp, UserParticipant up, CalendarDataObject edao) {
        if (edao == null) {
            return true;
        }
        boolean containsGroup = false;
        boolean containsUser = false;
        for (Participant p : edao.getParticipants()) {
            if (p.getType() == Participant.GROUP && p.getIdentifier() == gp.getIdentifier()) {
                containsGroup = true;
            }
            if (p.getType() == Participant.USER && p.getIdentifier() == up.getIdentifier()) {
                containsUser = true;
            }
        }
        if (!containsUser) { //double check
            for (UserParticipant u : edao.getUsers()) {
                if (u.getIdentifier() == up.getIdentifier()) {
                    containsUser = true;
                    break;
                }
            }
        }
        if (containsUser) {
            return true;
        }
        if (containsGroup && !containsUser) {
            return false;
        }
        return true;
    }

    static final Set<Participant> getNewParticipants(final Participant np[], final Participant op[]) {
        return new HashSet<Participant>(Arrays.asList(getNotContainedParticipants(np, op).getList()));
    }

    static final Set<Participant> getDeletedParticipants(final Participant np[], final Participant op[]) {
        return new HashSet<Participant>(Arrays.asList(getNotContainedParticipants(np, op).getList()));
    }

    static final Participants getNotContainedParticipants(final Participant[] toCheck, final Participant[] participants) {
        final Participants p = new Participants();
        for (final Participant newParticipant : toCheck) {
            if (Arrays.binarySearch(participants, newParticipant) < 0) {
                p.add(newParticipant);
            }
        }
        return p;
    }

    private static boolean userIsParticipant(final int uid, final CalendarDataObject cdao) {
        if (null == cdao) {
            return false;
        }
        for (final UserParticipant u : cdao.getUsers()) {
            if (null != u && u.getIdentifier() == uid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the new and modified user participants
     *
     * @param np The current user participants
     * @param op The old user participants
     * @param uid Current working session user
     * @param sharedFolderOwner The shared folder owner
     * @param time_change <code>true</code> if appointment's start date, end date and/or recurrence pattern changed; otherwise
     *            <code>false</code>
     * @param cdao The object denoting the changed appointment
     * @return An array of {@link Participants} with length <code>2</code>. If present index <code>0</code> will contain the new user
     *         participants otherwise <code>null</code>. If present index <code>1</code> will contain the modified user participants
     *         otherwise <code>null</code>.
     */
    static final Participants[] getModifiedUserParticipants(final UserParticipant np[], final UserParticipant op[], final int uid, final int sharedFolderOwner, final boolean time_change, final CalendarDataObject cdao) {
        final Participants p[] = new Participants[2];
        for (int a = 0; a < np.length; a++) {
            final int bs = Arrays.binarySearch(op, np[a]);
            if (bs < 0) {
                if (p[0] == null) {
                    p[0] = new Participants(); // new
                }
                p[0].add(np[a]);
            } else {
                if (cdao.getFolderMoveAction() == NO_MOVE_ACTION || cdao.getFolderMoveAction() == PRIVATE_CURRENT_PARTICIPANT_ONLY) {
                    if (uid == np[a].getIdentifier() || sharedFolderOwner == np[a].getIdentifier()) { // only the owner or the current user
                        // can change this object(s)
                        if (np[a].getIdentifier() == op[bs].getIdentifier() || (cdao.getFolderMoveAction() == PRIVATE_CURRENT_PARTICIPANT_ONLY && (uid == np[a].getIdentifier() || sharedFolderOwner == np[a].getIdentifier()))) {
                            if (np[a].containsAlarm() || np[a].containsConfirm() || np[a].containsConfirmMessage() || cdao.containsAlarm()) {
                                if (p[1] == null) {
                                    p[1] = new Participants(); // modified
                                }
                                np[a].setIsModified(false);
                                if (cdao.containsAlarm()) {
                                    np[a].setIsModified(true);
                                    np[a].setAlarmMinutes(cdao.getAlarm());
                                } else if (sharedFolderOwner != 0 && uid != sharedFolderOwner && sharedFolderOwner == np[a].getIdentifier() && !userIsParticipant(
                                    uid,
                                    cdao)) {
                                    // 1. The folder is shared and the actual participant is the folder owner.
                                    // 2. The user that tries to change the appointment is not the folder owner and is not participant in
                                    // this appointment.
                                    // 3. So we assume that the user wants to change the alarm for the folder owner.
                                    if (np[a].containsAlarm() && cdao.containsAlarm() && np[a].getAlarmMinutes() != cdao.getAlarm()) {
                                        np[a].setAlarmMinutes(cdao.getAlarm());
                                        np[a].setIsModified(true);
                                    } else if (cdao.containsAlarm() && !recColl.existsReminder(
                                        cdao.getContext(),
                                        cdao.getObjectID(),
                                        np[a].getIdentifier())) {
                                        np[a].setAlarmMinutes(cdao.getAlarm());
                                        np[a].setIsModified(true);
                                    } else if (cdao.getAlarm() == -1 && recColl.existsReminder(
                                        cdao.getContext(),
                                        cdao.getObjectID(),
                                        np[a].getIdentifier())) {
                                        np[a].setAlarmMinutes(-1);
                                        np[a].setIsModified(true);
                                    }
                                } else if (!np[a].containsAlarm() && recColl.existsReminder(cdao.getContext(), cdao.getObjectID(), uid)) {
                                    np[a].setIsModified(true);
                                    np[a].setAlarmMinutes(op[bs].getAlarmMinutes());
                                }
                                if (!np[a].containsConfirm() || time_change) {
                                    np[a].setIsModified(true);
                                    if (!np[a].containsConfirm()) {
                                        np[a].setConfirm(op[bs].getConfirm());
                                    }
                                }
                                if (!np[a].containsConfirmMessage()) {
                                    np[a].setIsModified(true);
                                    np[a].setConfirmMessage(op[bs].getConfirmMessage());
                                }
                                if (np[a].getPersonalFolderId() <= 0 && op[bs].getPersonalFolderId() > 0) {
                                    np[a].setPersonalFolderId(op[bs].getPersonalFolderId());
                                }
                                if (cdao.getFolderMoveAction() != NO_MOVE_ACTION) {
                                    p[1].add(np[a]);
                                } else if (np[a].isModified()) {
                                    p[1].add(np[a]);
                                }
                            } else if (cdao.getFolderMoveAction() == PRIVATE_CURRENT_PARTICIPANT_ONLY) {
                                if (p[1] == null) {
                                    p[1] = new Participants(); // modified
                                }
                                np[a].setIsModified(true);
                                np[a].setConfirm(op[bs].getConfirm());
                                np[a].setAlarmMinutes(op[bs].getAlarmMinutes());
                                p[1].add(np[a]);
                            }
                        }
                    } else {
                        if (!(np[a].getIdentifier() == op[bs].getIdentifier() && np[a].getAlarmMinutes() == op[bs].getAlarmMinutes() && np[a].getConfirm() == op[bs].getConfirm() && np[a].getConfirmMessage() == op[bs].getConfirmMessage())) {

                            /*
                             * We have two options: 1) Throw an ugly error message that nobody understands 2) Copy the older values to the
                             * submitted ones which is also ugly because we may have a different view to the data TODO: Make this
                             * configurable
                             */

                            // LOG.error("The current user ("+uid+") does not have the appropriate permissions to modify other participant ("+np[a].getIdentifier()+") properties");
                            // throw new
                            // OXPermissionException("The current user does not have the appropriate permissions to modify other participant properties");
                            if (op[bs].containsAlarm()) {
                                np[a].setAlarmMinutes(op[bs].getAlarmMinutes());
                            } else {
                                np[a].setAlarmMinutes(-1);
                            }
                            if (!time_change) {
                                np[a].setConfirm(op[bs].getConfirm());
                                np[a].setConfirmMessage(op[bs].getConfirmMessage());
                            } else {
                                np[a].setConfirm(CalendarObject.NONE);
                                np[a].setConfirmMessage(op[bs].getConfirmMessage());
                            }
                            np[a].setPersonalFolderId(op[bs].getPersonalFolderId());
                            if (p[1] == null) {
                                p[1] = new Participants(); // modified
                            }
                            p[1].add(np[a]);
                        } else if (time_change) {
                            if (p[1] == null) {
                                p[1] = new Participants(); // modified
                            }
                            if (op[bs].containsAlarm()) {
                                np[a].setAlarmMinutes(op[bs].getAlarmMinutes());
                            } else {
                                np[a].setAlarmMinutes(-1);
                            }
                            np[a].setConfirm(CalendarObject.NONE);
                            np[a].setConfirmMessage(op[bs].getConfirmMessage());
                            np[a].setPersonalFolderId(op[bs].getPersonalFolderId());
                            p[1].add(np[a]);
                        }
                    }
                } else if (cdao.getFolderMoveAction() == PRIVATE_ALL_PARTICIPANTS) {
                    if (p[1] == null) {
                        p[1] = new Participants(); // modified
                    }
                    p[1].add(np[a]);
                } else if (cdao.getFolderMoveAction() == PUBLIC_ALL_PARTICIPANTS) {
                    if (p[1] == null) {
                        p[1] = new Participants(); // modified
                    }
                    np[a].setPersonalFolderId(0);
                    p[1].add(np[a]);
                }
            }
        }
        return p;
    }

    public static final UserParticipant[] getDeletedUserParticipants(final UserParticipant np[], final UserParticipant op[], final int uid) {
        final Participants p = new Participants();
        for (UserParticipant element : np) {
            if (Arrays.binarySearch(op, element) < 0) {
                if (element.getPersonalFolderId() != 0) {
                    p.add(element);
                } else {
                    if (element.getIdentifier() != uid) {
                        p.add(element);
                    }
                }
            }
        }
        return p.getUsers();
    }

    @Override
    public void close() {
        if (co_rs != null) {
            try {
                co_rs.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing ResultSet.", sqle);
            }
        }

        if (prep != null) {
            try {
                prep.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing PreparedStatement.", sqle);
            }
        }

        if (readcon != null) {
            DBPool.push(c, readcon);
        }
    }

    private final void simpleDataCheck(final CalendarDataObject cdao, final CalendarDataObject edao, final int uid) throws OXException {
        // Both, start and end date are set
        if (cdao.getStartDate() != null && cdao.getEndDate() != null && cdao.getEndDate().getTime() < cdao.getStartDate().getTime()) {
            throw OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create();
        }
        // Only start date is set
        if (cdao.getStartDate() != null && cdao.getEndDate() == null && edao.getEndDate().getTime() < cdao.getStartDate().getTime()) {
            throw OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create();
        }// Only end date is set
        if (cdao.getStartDate() == null && cdao.getEndDate() != null && cdao.getEndDate().getTime() < edao.getStartDate().getTime()) {
            throw OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create();
        }
        if (cdao.containsUntil() && cdao.getUntil() != null) {
            final Date until = cdao.getUntil();
            Date start = null;
            if (edao != null && edao.containsStartDate()) {
                start = edao.getStartDate();
            }
            if (cdao.containsStartDate()) {
                start = cdao.getStartDate();
            }
            if (isUntilBeforeStart(until, start)) {
                throw OXCalendarExceptionCodes.UNTIL_BEFORE_START_DATE.create();
            }
        }
        if (cdao.containsLabel() && (cdao.getLabel() < 0 || cdao.getLabel() > 256)) {
            throw OXCalendarExceptionCodes.UNSUPPORTED_LABEL.create(cdao.getLabel());
        }
        if (cdao.containsPrivateFlag()) {
            if (cdao.getPrivateFlag()) {
                if (cdao.getFolderType() != CalendarCollectionService.PRIVATE) {
                    throw OXCalendarExceptionCodes.PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER.create();
                }
                if (edao == null || (edao.containsPrivateFlag() && edao.getPrivateFlag())) {
                    if (cdao.containsObjectID() && cdao.getSharedFolderOwner() != 0 && cdao.getSharedFolderOwner() != uid) {
                        throw OXCalendarExceptionCodes.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED.create();
                    }
                    if (cdao.getFolderType() != FolderObject.PRIVATE) {
                        throw OXCalendarExceptionCodes.PRIVATE_FLAG_IN_PRIVATE_FOLDER.create();
                    }
                }
            }
        } else if (edao != null && edao.containsPrivateFlag() && edao.getPrivateFlag()) {
            if (cdao.getSharedFolderOwner() != uid) {
                if (cdao.getFolderType() != CalendarCollectionService.PRIVATE) {
                    throw OXCalendarExceptionCodes.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED.create();
                }
            }
        }
        if (cdao.containsShownAs() && (cdao.getShownAs() < 0 || cdao.getShownAs() > 4)) {
            throw OXCalendarExceptionCodes.UNSUPPORTED_SHOWN_AS.create(cdao.getShownAs());
        } else if (cdao.containsShownAs() && cdao.getShownAs() == 0) {
            // auto correction
            cdao.setShownAs(Appointment.RESERVED);
        }
        if (cdao.containsParticipants()) {
            recColl.simpleParticipantCheck(cdao);
        }
    }

    private boolean isUntilBeforeStart(final Date until, final Date start) {
        return start != null && recColl.normalizeLong(until.getTime()) < recColl.normalizeLong(start.getTime());
    }

    final int checkUpdateRecurring(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        if (!edao.containsRecurrenceType() && !cdao.containsRecurrenceType()) {
            return CalendarCollectionService.RECURRING_NO_ACTION;
        }
        if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() != edao.getObjectID()) {
            /*
             * An update of a change exception
             */
            if (RecurrenceChecker.containsRecurrenceInformation(cdao)) {
                throw OXCalendarExceptionCodes.CHANGE_EXCEPTION_TO_RECURRENCE.create();
            }
            return CalendarCollectionService.RECURRING_NO_ACTION;
        }
        if (edao.containsRecurrenceType() && edao.getRecurrenceType() > CalendarObject.NO_RECURRENCE && (!cdao.containsRecurrenceType() || cdao.getRecurrenceType() == edao.getRecurrenceType())) {
            int ret = recColl.getRecurringAppoiontmentUpdateAction(cdao, edao);
            if (ret == CalendarCollectionService.RECURRING_NO_ACTION) {
                // We have to check if something has been changed in the meantime!
                if (!cdao.containsStartDate() || !cdao.containsEndDate()) {
                    final CalendarDataObject temp = edao.clone();
                    final RecurringResultsInterface rss = recColl.calculateFirstRecurring(temp);
                    if (rss != null) {
                        final RecurringResultInterface rs = rss.getRecurringResult(0);
                        if (rs != null) {
                            if (!cdao.containsStartDate()) {
                                cdao.setStartDate(new Date(rs.getStart()));
                            }
                            if (!cdao.containsEndDate()) {
                                cdao.setEndDate(new Date(rs.getEnd()));
                            }
                        }
                    }
                }

                if (cdao.containsStartDate() && cdao.containsEndDate()) {
                    ret = checkPatternChange(cdao, edao, ret);
                }
            } else {
                if (cdao.getFolderMove()) {
                    throw OXCalendarExceptionCodes.RECURRING_EXCEPTION_MOVE_EXCEPTION.create();
                }
                if (CalendarCollectionService.RECURRING_EXCEPTION_DELETE_EXISTING == ret && (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() == edao.getObjectID())) {
                    /*
                     * A formerly created change exception shall be deleted through an update on master recurring appointment
                     */
                    if (cdao.containsStartDate() && cdao.containsEndDate()) {
                        ret = checkPatternChange(cdao, edao, ret);
                    }
                }
            }
            if (ret == CalendarCollectionService.CHANGE_RECURRING_TYPE) {
                //calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
            }
            return ret;
        } else if (edao.containsRecurrenceType() && edao.getRecurrenceType() > CalendarObject.NO_RECURRENCE && cdao.getRecurrenceType() != edao.getRecurrenceType()) {
            // Recurring Pattern changed! TODO: Remove all exceptions
            if ((cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) || (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null)) {
                return CalendarCollectionService.RECURRING_CREATE_EXCEPTION;
            }
            cdao.setRecurrenceID(edao.getObjectID());
            if (!cdao.containsStartDate()) {
                cdao.setStartDate(edao.getStartDate());
            }
            if (!cdao.containsEndDate()) {
                if (cdao.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
                    calculateEndDateForNoType(cdao, edao);
                } else {
                    cdao.setEndDate(edao.getEndDate());
                }
            } else if(CalendarObject.NO_RECURRENCE != cdao.getRecurrenceType()) {
                if ((cdao.getEndDate() == null) || (cdao.getStartDate() == null)) {
                    return CalendarCollectionService.RECURRING_CREATE_EXCEPTION;
                }
                cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/Constants.MILLI_DAY)));
                calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
            }

            if (cdao.getRecurrenceType() > 0) {
                calculateEndDateForNewType(cdao, edao);
            } else if (cdao.getRecurrenceType() == 0 && !cdao.containsEndDate()) {
                calculateEndDateForNoType(cdao, edao);
            }

            recColl.changeRecurrenceString(cdao);
            cdao.setExceptions(null);
            cdao.setDelExceptions(null);
            return CalendarCollectionService.CHANGE_RECURRING_TYPE;
        } else if (!edao.containsRecurrenceType() && cdao.getRecurrenceType() > CalendarObject.NO_RECURRENCE) {
            // TODO: Change from normal appointment to sequence
            if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrence() != null) {
                throw OXCalendarExceptionCodes.RECURRING_ALREADY_EXCEPTION.create();
            }
            cdao.setRecurrenceID(edao.getObjectID());
            if (!cdao.containsStartDate()) {
                cdao.setStartDate(edao.getStartDate());
            }
            if (!cdao.containsEndDate()) {
                cdao.setEndDate(edao.getEndDate());
            }
            if (!cdao.containsRecurrenceString()) {
                recColl.changeRecurrenceString(cdao);
            }
            cdao.setRecurrenceCalculator(((int) ((cdao.getEndDate().getTime() - cdao.getStartDate().getTime()) / Constants.MILLI_DAY)));
            correctStartAndEndDate(cdao);
            cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
        } else if (edao.containsRecurrenceType() && cdao.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
            // Sequence reset, this means to delete all existing exceptions
            if (cdao.containsRecurrencePosition() || cdao.containsRecurrenceDatePosition()) {
                return CalendarCollectionService.RECURRING_CREATE_EXCEPTION;
            }
            return CalendarCollectionService.RECURRING_EXCEPTION_DELETE;
        }
        return CalendarCollectionService.RECURRING_NO_ACTION;
    }

    private void correctStartAndEndDate(CalendarDataObject cdao) throws OXException {
        RecurringResultsInterface results = recColl.calculateFirstRecurring(cdao);
        RecurringResultInterface result = results.getRecurringResult(0);
        if (cdao.getStartDate().getTime() != result.getStart()) {
            cdao.setStartDate(new Date(result.getStart()));
        }
        if (cdao.getEndDate().getTime() != result.getEnd()) {
            cdao.setEndDate(new Date(result.getEnd()));
        }
    }

    private void calculateEndDateForNoType(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        final RecurringResultInterface recurringResult = recColl.calculateFirstRecurring(edao).getRecurringResult(0);
        cdao.setEndDate(new Date(recurringResult.getEnd()));
    }

    private void calculateEndDateForNewType(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        Date occurrenceDate;
        if (cdao.getOccurrence() <= 0) {
            occurrenceDate = recColl.getOccurenceDate(cdao, CalendarCollectionService.MAX_OCCURRENCESE);
        } else {
            occurrenceDate = recColl.getOccurenceDate(cdao);
        }
        // Get corresponding until date
        final Date untilDate = new Date(recColl.normalizeLong(occurrenceDate.getTime()));
        // Set proper end time
        cdao.setEndDate(calculateRealRecurringEndDate(untilDate, edao.getEndDate(), edao.getFullTime(), edao.getRecurrenceCalculator()));
    }

    /**
     * Checks if specified recurring appointment's pattern shall be changed
     *
     * @param cdao The current calendar object (containing the changes for ongoing update operation)
     * @param edao The storage calendar object
     * @param recurringAction The previously detected recurring action constant
     * @return The recurring action appropriate for a possibly changed recurring pattern
     * @throws OXException If checking change of recurring pattern fails
     */
    private static final int checkPatternChange(final CalendarDataObject cdao, final CalendarDataObject edao, final int recurringAction) throws OXException {
        cdao.setRecurrenceCalculator(((int) ((cdao.getEndDate().getTime() - cdao.getStartDate().getTime()) / Constants.MILLI_DAY)));

        // Have to check if something in the pattern has been changed
        // and then modify the recurring. Assume all data has been provided
        boolean pattern_change = false;
        boolean completenessChecked = false;
        boolean changeStartDate = false;

        if (cdao.containsFullTime() && !cdao.getFullTime() && edao.getFullTime()) { // case of Bug 16107
            handleChangeFromFullTimeToNormal(cdao, edao);
        }
        if (cdao.containsInterval() && cdao.getInterval() != edao.getInterval()) {
            recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
            completenessChecked = true;
            pattern_change = true;
        }
        if (cdao.containsStartDate() && !cdao.getStartDate().equals(edao.getStartDate())) {
            if (!completenessChecked) {
                recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
                completenessChecked = true;
            }
            pattern_change = true;
            changeStartDate = true;
        }
        if (cdao.containsDays() && cdao.getDays() != edao.getDays()) {
            if (!completenessChecked) {
                recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
                completenessChecked = true;
            }
            pattern_change = true;
        }
        if (cdao.containsDayInMonth() && cdao.getDayInMonth() != edao.getDayInMonth()) {
            if (!completenessChecked) {
                recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
                completenessChecked = true;
            }
            pattern_change = true;
        }
        if (cdao.containsMonth() && cdao.getMonth() != edao.getMonth()) {
            if (!completenessChecked) {
                recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
                completenessChecked = true;
            }
            pattern_change = true;
        }
        if (cdao.containsOccurrence() && cdao.getOccurrence() != edao.getOccurrence()) {
            if (!completenessChecked) {
                recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
                completenessChecked = true;
            }
            //cdao.removeUntil();
            // Calculate occurrence's time
            final Date occurrenceDate;
            if (cdao.getOccurrence() <= 0) {
                occurrenceDate = recColl.getOccurenceDate(cdao, CalendarCollectionService.MAX_OCCURRENCESE);
            } else {
                occurrenceDate = recColl.getOccurenceDate(cdao);
            }
            // Get corresponding until date
            final Date untilDate = new Date(recColl.normalizeLong(occurrenceDate.getTime()));
            // Set proper end time
            cdao.setEndDate(calculateRealRecurringEndDate(untilDate, edao.getEndDate(), edao.getFullTime(), edao.getRecurrenceCalculator()));
            pattern_change = true;
        }
        if (cdao.containsEndDate() && !cdao.getEndDate().equals(edao.getEndDate())) {
            cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
        }
        if (changeUntil(cdao, edao)) {
            if (!completenessChecked) {
                recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
                completenessChecked = true;
            }
            if (cdao.getUntil() != null) {
                cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
            } else {
                /*
                 * TODO: Change behaviour! Workaround to make until=null possible for deleting until/occurrences value. If until is null, it
                 * will be removed, so that the getUntil() method calculates the effective end of the sequence and endDate can be set. After
                 * that until needs to be set to null again so that it is not stored in the database.
                 */
                cdao.removeUntil();
                cdao.setUntil(cdao.getUntil());
                cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
                cdao.setUntil(null);
            }
            pattern_change = true;
        }
        if (!cdao.containsOccurrence() && !cdao.containsUntil()) {
            /*
             * Neither occurrences nor until date set; calculate end date from last possible occurrence
             */
            if (!cdao.containsTimezone()) {
                cdao.setTimezone(edao.getTimezoneFallbackUTC());
            }
            cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
        }
        /*
         * Detect recurring action dependent on whether pattern was changed or not
         */
        final int retval;
        if (pattern_change) {
            if (!changeStartDate) {
                calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
                cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
            }
            cdao.removeRecurrenceString();

            recColl.checkRecurring(cdao);
            recColl.fillDAO(cdao);
            cdao.setExceptions(null);
            cdao.setDelExceptions(null);
            /*
             * Indicate change of recurring type
             */
            retval = CalendarCollectionService.CHANGE_RECURRING_TYPE;
        } else {
            calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
            cdao.setEndDate(calculateRealRecurringEndDate(cdao, edao));
            cdao.setRecurrence(edao.getRecurrence());
            /*
             * Return specified recurring action unchanged
             */
            retval = recurringAction;
        }
        return retval;
    }

    private static boolean changeUntil(final CalendarDataObject cdao, final CalendarDataObject edao) {
        if (!cdao.containsUntil()) {
            return false;
        }

        if (cdao.containsUntil() && cdao.getUntil() == null && !edao.containsUntil()) {
            return false;
        }

        return cdao.containsUntil() && recColl.check(cdao.getUntil(), edao.getUntil());
    }

    private static final void checkAndRemoveRecurrenceFields(final CalendarDataObject cdao) {
        if (cdao.containsDays()) {
            cdao.removeDays();
        }
        if (cdao.containsDayInMonth()) {
            cdao.removeDayInMonth();
        }
        if (cdao.containsInterval()) {
            cdao.removeInterval();
        }
        if (cdao.containsMonth()) {
            cdao.removeMonth();
        }
        if (cdao.containsOccurrence()) {
            cdao.removeOccurrence();
        }
    }

    private static final void checkInsertMandatoryFields(final CalendarDataObject cdao) throws OXException {
        if (!cdao.containsStartDate() || cdao.getStartDate() == null) {
            throw OXCalendarExceptionCodes.MANDATORY_FIELD_START_DATE.create();
        }
        if (!cdao.containsEndDate() || cdao.getEndDate() == null) {
            throw OXCalendarExceptionCodes.MANDATORY_FIELD_END_DATE.create();
        }
        if (!cdao.containsTitle()) {
            // Adapt to MS Outlook behavior and set empty title
            cdao.setTitle("");
            // throw new OXException(OXException.Code.MANDATORY_FIELD_TITLE);
        }
        if (!cdao.containsShownAs()) {
            cdao.setShownAs(Appointment.RESERVED); // auto correction
        }
    }

    private void setAttachmentLastModified(final Connection myCon, final Context ctx, final CalendarDataObject cdao) {
        if (!cdao.containsObjectID()) {
            return;
        }
        final AttachmentBase attachmentBase = Attachments.getInstance(new SimpleDBProvider(myCon, null));
        Date date = null;
        try {
            date = attachmentBase.getNewestCreationDate(ctx, Types.APPOINTMENT, cdao.getObjectID());
        } catch (final OXException e) {
            LOG.error("", e);
        }
        if (null != date) {
            cdao.setLastModifiedOfNewestAttachment(date);
        }
    }

}
