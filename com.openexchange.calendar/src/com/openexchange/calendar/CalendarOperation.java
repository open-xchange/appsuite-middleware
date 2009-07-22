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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link CalendarOperation} - Provides various operations on calendar
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class CalendarOperation implements SearchIterator<CalendarDataObject> {

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

    public static final int MAX_RESULT_LIMIT = -1;
    private int result_counter;

    private final List<AbstractOXException> warnings = new ArrayList<AbstractOXException>(2);

    private boolean has_next;
    private static final Log LOG = LogFactory.getLog(CalendarOperation.class);

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
    
    private static CalendarCollection recColl = new CalendarCollection();

    final CalendarDataObject loadAppointment(final ResultSet load_resultset, final int oid, final int inFolder, final CalendarSqlImp cimp, final Connection readcon, final Session so, final Context ctx, final int action, final int action_folder) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException {
        return loadAppointment(load_resultset, oid, inFolder, cimp, readcon, so, ctx, action, action_folder, true);
    }

    protected final CalendarDataObject loadAppointment(final ResultSet load_resultset, final int oid, final int inFolder, final CalendarSqlImp cimp, final Connection readcon, final Session so, final Context ctx, final int action, final int action_folder, final boolean check_permissions) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setObjectID(oid);
        cdao.setContext(ctx);
        int check_special_action = action;
        if (action == UPDATE && inFolder != action_folder) { // We move and this means to create a new object
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
                if (check_permissions && !recColl.checkPermissions(cdao, so, ctx, readcon, check_special_action, action_folder)) {
                    if (LOG.isDebugEnabled() && action_folder != inFolder) {
                        LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 1 (fid!inFolder) for user:oid:fid:inFolder ", Integer.valueOf(so.getUserId()), ":",Integer.valueOf(oid),":",Integer.valueOf(action_folder),":",inFolder }));
                    }
                    throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_1));
                }
                cdao.setStartDate(setDate(i++, load_resultset));
                cdao.setEndDate(setDate(i++, load_resultset));
                cdao.setTimezone(setString(i++, load_resultset));
                cdao.setRecurrenceID(setInt(i++, load_resultset));
                cdao.setLabel(setInt(i++, load_resultset));
                cdao.setTitle(setString(i++, load_resultset));
                cdao.setLocation(setString(i++, load_resultset));
                cdao.setShownAs(setInt(i++, load_resultset));
                cdao.setNumberOfAttachments(setInt(i++, load_resultset));
                cdao.setNote(setString(i++, load_resultset));
                cdao.setFullTime(setBooleanToInt(i++, load_resultset));
                cdao.setCategories(setString(i++, load_resultset));
                cdao.setUsers(cimp.getUserParticipants(cdao, readcon, so.getUserId()).getUsers());
                cdao.setParticipants(cimp.getParticipants(cdao, readcon).getList());
                if (check_permissions && cdao.getEffectiveFolderId() != inFolder) {
                    if (cdao.getFolderType() != FolderObject.SHARED && check_special_action == action) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 2 (fid!inFolder) for user:oid:fid:inFolder ", Integer.valueOf(so.getUserId()), ":",Integer.valueOf(oid),":",Integer.valueOf(inFolder),":",Integer.valueOf(action) }));
                        }
                        throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_2));
                    } else if (action_folder != inFolder && check_special_action == action) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 3 (fid!inFolder) for user:oid:fid:inFolder ", Integer.valueOf(so.getUserId()), ":",Integer.valueOf(oid),":",Integer.valueOf(inFolder),":",Integer.valueOf(action) }));
                        }
                        throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_3));
                    }
                }
                if (check_permissions && action == UPDATE && inFolder != action_folder) {
                    if (!recColl.checkPermissions(cdao, so, ctx, readcon, DELETE, inFolder)) { // Move means to check delete
                        if (LOG.isDebugEnabled() && inFolder != action_folder) {
                            LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 4 (fid!inFolder) for user:oid:fid:inFolder ", Integer.valueOf(so.getUserId()), ":",Integer.valueOf(oid),":",Integer.valueOf(action_folder),":",Integer.valueOf(inFolder) }));
                        }
                        throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_4));
                    }
                }
                if (cdao.containsRecurrenceID()) {
                    cdao.setRecurrenceCalculator(setInt(i++, load_resultset));
                    cdao.setRecurrencePosition(setInt(i++, load_resultset));
                    cdao.setRecurrence(setString(i++, load_resultset));
                    cdao.setDelExceptions(setString(i++, load_resultset));
                    cdao.setExceptions(setString(i++, load_resultset));
                    extractRecurringInformation(cdao);
                    if (cdao.getObjectID() == cdao.getRecurrenceID()) {
                        cdao.calculateRecurrence();
                    }
                }
            } else {
                final String text = "Object " + oid + " in context " + cdao.getContextID();
                final OXObjectNotFoundException e = new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, APPOINTMENT, text);
                LOG.error(e.getMessage(), e);
                throw e;
            }
        } finally {
            load_resultset.close();
        }
        return cdao;
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
        final Date r =  sd_rs.getTimestamp(i);
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
        if (cdao.containsTitle() && recColl.check(cdao.getTitle(), edao.getTitle()) && recColl.getFieldName(Appointment.TITLE) != null) {
            ucols[uc++] = Appointment.TITLE;
        }
        if (cdao.containsShownAs() && recColl.check(Integer.valueOf(cdao.getShownAs()), Integer.valueOf(edao.getShownAs())) && recColl.getFieldName(Appointment.SHOWN_AS) != null) {
            ucols[uc++] = Appointment.SHOWN_AS;
        }
        if (cdao.containsStartDate() && recColl.check(Long.valueOf(cdao.getStartDate().getTime()), Long.valueOf(edao.getStartDate().getTime())) && recColl.getFieldName(Appointment.START_DATE) != null) {
            ucols[uc++] = Appointment.START_DATE;
        }
        if (cdao.containsEndDate() && recColl.check(Long.valueOf(cdao.getEndDate().getTime()), Long.valueOf(edao.getEndDate().getTime())) && recColl.getFieldName(Appointment.END_DATE) != null) {
            ucols[uc++] = Appointment.END_DATE;
        }
        if (cdao.containsLocation() && recColl.check(cdao.getLocation(), edao.getLocation()) && recColl.getFieldName(Appointment.LOCATION) != null) {
            ucols[uc++] = Appointment.LOCATION;
        }
        if (cdao.containsNote() && recColl.check(cdao.getNote(), edao.getNote()) && recColl.getFieldName(Appointment.NOTE) != null) {
            ucols[uc++] = Appointment.NOTE;
        }
        if (cdao.containsFullTime() && recColl.check(Boolean.valueOf(cdao.getFullTime()), Boolean.valueOf(edao.getFullTime())) && recColl.getFieldName(Appointment.FULL_TIME) != null) {
            ucols[uc++] = Appointment.FULL_TIME;
        }
        if (cdao.containsCategories() && recColl.check(cdao.getCategories(), edao.getCategories()) && recColl.getFieldName(Appointment.CATEGORIES) != null) {
            ucols[uc++] = Appointment.CATEGORIES;
        }
        if (cdao.containsLabel() && recColl.check(Integer.valueOf(cdao.getLabel()), Integer.valueOf(edao.getLabel())) && recColl.getFieldName(Appointment.COLOR_LABEL) != null) {
            ucols[uc++] = Appointment.COLOR_LABEL;
        }
        if (cdao.containsPrivateFlag() && recColl.check(Boolean.valueOf(cdao.getPrivateFlag()), Boolean.valueOf(edao.getPrivateFlag())) && recColl.getFieldName(Appointment.PRIVATE_FLAG) != null) {
            ucols[uc++] = Appointment.PRIVATE_FLAG;
        }
        if (cdao.containsParentFolderID() && recColl.check(Integer.valueOf(cdao.getGlobalFolderID()), Integer.valueOf(edao.getGlobalFolderID())) && recColl.getFieldName(Appointment.FOLDER_ID) != null) {
            ucols[uc++] = Appointment.FOLDER_ID;
        }
        if ((cdao.containsRecurrenceString() || cdao.containsRecurrenceType()) && recColl.check(cdao.getRecurrence(), edao.getRecurrence()) && recColl.getFieldName(Appointment.RECURRENCE_TYPE) != null) {
            ucols[uc++] = Appointment.RECURRENCE_TYPE;
        }
        if (cdao.containsRecurrenceID() && recColl.check(Integer.valueOf(cdao.getRecurrenceID()), Integer.valueOf(edao.getRecurrenceID())) && recColl.getFieldName(Appointment.RECURRENCE_ID) != null) {
            ucols[uc++] = Appointment.RECURRENCE_ID;
        }
        if (cdao.containsDeleteExceptions() && recColl.check(cdao.getDelExceptions(), edao.getDelExceptions()) && recColl.getFieldName(Appointment.DELETE_EXCEPTIONS) != null) {
            ucols[uc++] = Appointment.DELETE_EXCEPTIONS;
            //cdao.setDeleteExceptions(recColl.mergeExceptions(cdao.getDeleteException(), edao.getDeleteException()));
        }
        if (cdao.containsChangeExceptions() && recColl.check(cdao.getExceptions(), edao.getExceptions()) && recColl.getFieldName(Appointment.CHANGE_EXCEPTIONS) != null) {
            ucols[uc++] = Appointment.CHANGE_EXCEPTIONS;
            //cdao.setChangeExceptions(recColl.mergeExceptions(cdao.getChangeException(), edao.getChangeException()));
        }
        if (cdao.containsRecurrencePosition() && recColl.check(Integer.valueOf(cdao.getRecurrencePosition()), Integer.valueOf(edao.getRecurrencePosition())) && recColl.getFieldName(Appointment.RECURRENCE_POSITION) != null) {
            ucols[uc++] = Appointment.RECURRENCE_POSITION;
        }
        if (cdao.containsNumberOfAttachments() && recColl.check(Integer.valueOf(cdao.getNumberOfAttachments()), Integer.valueOf(edao.getNumberOfAttachments())) && recColl.getFieldName(Appointment.NUMBER_OF_ATTACHMENTS) != null) {
            ucols[uc++] = Appointment.NUMBER_OF_ATTACHMENTS;
        }

        if (recColl.check(Integer.valueOf(cdao.getRecurrenceCalculator()), Integer.valueOf(edao.getRecurrenceCalculator())) && recColl.getFieldName(Appointment.RECURRENCE_CALCULATOR) != null) {
            ucols[uc++] = Appointment.RECURRENCE_CALCULATOR;
        }
        return uc;
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
            throw new OXCalendarException(OXCalendarException.Code.CONTEXT_NOT_SET);
        }
        final OXFolderAccess ofa = new OXFolderAccess(cdao.getContext());
        if (ofa.getFolderModule(inFolder) != FolderObject.CALENDAR) {
            throw new OXCalendarException(OXCalendarException.Code.NON_CALENDAR_FOLDER);
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
                //cdao.setRecurrenceCalculator(((int) ((cdao.getEndDate().getTime() - cdao.getStartDate().getTime()) / Constants.MILLI_DAY)));
                cdao.setEndDate(calculateRealRecurringEndDate(cdao));
            } else {
                cdao.setRecurrence(recColl.NO_DS);
            }

            cdao.setCreatedBy(uid);
            cdao.setCreationDate(new Date());
            cdao.setModifiedBy(uid);
            cdao.setFolderType(ofa.getFolderType(inFolder, uid));
            if (cdao.getFolderType() == FolderObject.PRIVATE) {
                cdao.setPrivateFolderID(inFolder);
                cdao.setGlobalFolderID(0);
            }
            // Strange bugs can be produced if the recurrence identifier is set to some value on insert.
            cdao.removeRecurrenceID();
        } else {
            if (!cdao.containsModifiedBy()) {
                cdao.setModifiedBy(uid);
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
                recColl.fillDAO(cdao);
            } else if (edao.isSequence() && edao.getObjectID() != edao.getRecurrenceID()) {
                // this is a change exception.
                if (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null && !cdao.getRecurrenceDatePosition().equals(
                    edao.getRecurrenceDatePosition())) {
                    /*
                     * Deny change of recurring position in a change exception
                     */
                    throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_POSITION_CHANGE);
                } else if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0 && cdao.getRecurrencePosition() != edao.getRecurrencePosition()) {
                    /*
                     * Deny change of recurring position in a change exception
                     */
                    throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_POSITION_CHANGE);
                } else if (cdao.containsInterval() && cdao.getInterval() > 0 && cdao.getInterval() != edao.getInterval()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_TYPE_CHANGE);
                } else if (cdao.containsDays() && cdao.getDays() > 0 && cdao.getDays() != edao.getDays()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_TYPE_CHANGE);
                } else if (cdao.containsDayInMonth() && cdao.getDayInMonth() > 0 && cdao.getDayInMonth() != edao.getDayInMonth()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_TYPE_CHANGE);
                } else if (cdao.containsMonth() && cdao.getMonth() > 0 && cdao.getMonth() != edao.getMonth()) {
                    /*
                     * Deny change of recurring type/pattern in a change exception
                     */
                    throw new OXCalendarException(OXCalendarException.Code.INVALID_RECURRENCE_TYPE_CHANGE);
                }
                // Not overwriting the recurrence position. This must give the exception INVALID_RECURRENCE_POSITION_CHANGE.
                // Keep the recurrence pattern
                cdao.setRecurrence(edao.getRecurrence());
            }

            if (cdao.containsParentFolderID() && inFolder != cdao.getParentFolderID()) {
                cdao.setFolderMove(true);
            }

            if (cdao.containsParentFolderID()) {
                cdao.setFolderType(ofa.getFolderType(cdao.getParentFolderID(), uid));
            } else {
                cdao.setFolderType(ofa.getFolderType(inFolder, uid));
            }
        }

        if (cdao.getFolderType() == FolderObject.PRIVATE) {
            if (isInsert || cdao.containsParticipants()) {
                final UserParticipant up = new UserParticipant(uid);
                up.setConfirm(CalendarDataObject.ACCEPT);
                recColl.checkAndFillIfUserIsParticipant(cdao, up);
            }
        } else if (cdao.getFolderType() == FolderObject.SHARED) {
            if (cdao.containsParentFolderID()) {
                cdao.setSharedFolderOwner(ofa.getFolderOwner(cdao.getParentFolderID()));
            } else {
                cdao.setSharedFolderOwner(ofa.getFolderOwner(inFolder));
            }
            final UserParticipant up = new UserParticipant(cdao.getSharedFolderOwner());
            if (isInsert) {
                up.setConfirm(CalendarDataObject.ACCEPT);
            }
            if (isInsert) {
                recColl.checkAndFillIfUserIsParticipant(cdao, up);
            } else {
                if (!cdao.containsUserParticipants() && !recColl.checkIfUserIsParticipant(edao, up)) {
                    cdao.setUsers(edao.getUsers());
                    if (edao.getFolderType() == FolderObject.PRIVATE) {
                        recColl.removeParticipant(cdao, uid);
                    }
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
            up.setConfirm(CalendarDataObject.ACCEPT);
            recColl.checkAndConfirmIfUserUserIsParticipant(cdao, up);
        }

        UserParticipant p = null;
        if (cdao.getFolderType() == FolderObject.SHARED) {
            p = new UserParticipant(cdao.getSharedFolderOwner());
        } else {
            p = new UserParticipant(uid);
        }
        p.setConfirm(CalendarDataObject.ACCEPT);
        if ((isInsert || cdao.containsUserParticipants()) && cdao.getFolderType() != FolderObject.PUBLIC) {
            recColl.checkAndFillIfUserIsUser(cdao, p);
        }

        if (!cdao.containsTimezone()) {
            cdao.setTimezone(timezone);
        }
        
        simpleDataCheck(cdao, edao, uid);
        if (isInsert && cdao.getParticipants() == null && cdao.getFolderType() == FolderObject.PUBLIC) {
            final Participant np[] = new Participant[1];
            final Participant up = new UserParticipant(uid);
            np[0] = up;
            cdao.setParticipants(np);
        }
        try {
            fillUserParticipants(cdao);
        } catch (final LdapException e) {
            throw new OXCalendarException(e);
        }
        recColl.updateDefaultStatus(cdao, cdao.getContext(), uid, inFolder);
        return isInsert;
    }

    /**
     * Checks if full-time flag is set in specified parameter <code>cdao</code>.
     * If so its start date and end date is changed to last the whole day.
     *
     * @param cdao
     *            The current calendar object
     * @param edao
     *            The storage calendar object used to set start/end date if not
     *            available in specified parameter <code>cdao</code>
     */
    private static final void handleFullTime(final CalendarDataObject cdao, final CalendarDataObject edao) {
        if (cdao.getFullTime()) {
            if (cdao.containsStartDate() && cdao.containsEndDate()) {
                final long mod = cdao.getStartDate().getTime() % Constants.MILLI_DAY;
                if (mod != 0) {
                    cdao.setStartDate(new Date(cdao.getStartDate().getTime() - mod));
                }
                if ((cdao.getStartDate().getTime() == cdao.getEndDate().getTime())
                        || (cdao.getEndDate().getTime() - cdao.getStartDate().getTime() < Constants.MILLI_DAY)) {
                    cdao.setEndDate(new Date(cdao.getStartDate().getTime() + Constants.MILLI_DAY));
                } else if (cdao.getEndDate().getTime() % Constants.MILLI_DAY != 0) {
                    cdao.setEndDate(new Date((cdao.getStartDate().getTime() + (((cdao.getEndDate().getTime() - cdao
                            .getStartDate().getTime()) / Constants.MILLI_DAY) * Constants.MILLI_DAY))));
                }
            } else if (edao != null) {
                final long mod = edao.getStartDate().getTime() % Constants.MILLI_DAY;
                if (mod != 0) {
                    cdao.setStartDate(new Date(edao.getStartDate().getTime() - mod));
                }
                if ((cdao.getStartDate().getTime() == edao.getEndDate().getTime())
                        || (edao.getEndDate().getTime() - cdao.getStartDate().getTime() < Constants.MILLI_DAY)) {
                    cdao.setEndDate(new Date(cdao.getStartDate().getTime() + Constants.MILLI_DAY));
                } else if (edao.getEndDate().getTime() % Constants.MILLI_DAY != 0) {
                    cdao.setEndDate(new Date((cdao.getStartDate().getTime() + (((edao.getEndDate().getTime() - cdao
                            .getStartDate().getTime()) / Constants.MILLI_DAY) * Constants.MILLI_DAY))));
                }
            }
        }
    }

    private static final Date calculateRealRecurringEndDate(final CalendarDataObject cdao) {
        final Date until = cdao.getUntil();
        return calculateRealRecurringEndDate(
            null == until ? recColl.getMaxUntilDate(cdao) : until,
            cdao.getEndDate(),
            cdao.getFullTime());
    }

    private static final Date calculateRealRecurringEndDate(final Date untilDate, final Date endDate, final boolean isFulltime) {
        long until = untilDate.getTime();
        // Extract time out of until date
        long mod = until % Constants.MILLI_DAY;
        if (mod > 0) {
            until = until - mod;
        }
        // Extract time out of end date
        mod = (endDate.getTime()) % Constants.MILLI_DAY;
        if (isFulltime) {
            /*
             * Add one day for general handling of full-time appointments: from 00:00h day 1 to 00:00h day 2
             */
            return new Date(until + Constants.MILLI_DAY);
        }
        return new Date(until + mod);
    }

    private static final void calculateAndSetRealRecurringStartAndEndDate(CalendarDataObject cdao, CalendarDataObject edao) {
        long startDate = edao.getRecurringStart();
        if (startDate == 0) {
            startDate = edao.getStartDate().getTime();
        }
        TimeZone tz = Tools.getTimeZone(cdao.getTimezone());
        int startDateZoneOffset = tz.getOffset(startDate);
        final long endDate = edao.getUntil().getTime();
        long startTime = cdao.getStartDate().getTime();
        long endTime = (cdao.getEndDate().getTime());
        int startTimeZoneOffset = tz.getOffset(startTime);
        startTime = startTime % Constants.MILLI_DAY;
        endTime = endTime % Constants.MILLI_DAY  + (cdao.getRecurrenceCalculator() * Constants.MILLI_DAY);
        // FIXME daylight saving time offset
        cdao.setStartDate(recColl.calculateRecurringDate(startDate, startTime, startTimeZoneOffset - startDateZoneOffset));
        cdao.setEndDate(recColl.calculateRecurringDate(endDate, endTime, startTimeZoneOffset - startDateZoneOffset));
    }

    public final boolean hasNext() {
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

    public final CalendarDataObject next() throws SearchIteratorException, OXException, OXObjectNotFoundException, OXPermissionException {
        if (hasNext()) {
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(c);
            int g = 1;
            boolean bbs = false;
            if (co_rs == null || cols == null) {
                throw new OXCalendarException(OXCalendarException.Code.SEARCH_ITERATOR_NULL);
            }
            try {
                for (int a = 0; a < cols.length; a++) {
                    final FieldFiller ff = FILLERS.get(Integer.valueOf(cols[a]));
                    if (null == ff) {
                        /*
                         * Fields not covered by FieldFiller: USERS, PARTICIPANTS, and FOLDER_ID
                         */
                        if (Appointment.USERS == cols[a]) {
                            if (cdao.containsUserParticipants() || CachedCalendarIterator.CACHED_ITERATOR_FAST_FETCH) {
                                cdao.setFillUserParticipants();
                            } else {
                                final Participants users = cimp.getUserParticipants(cdao, readcon, uid);
                                cdao.setUsers(users.getUsers());
                                bbs = true;
                            }
                        } else if (Appointment.PARTICIPANTS == cols[a]) {
                            if (CachedCalendarIterator.CACHED_ITERATOR_FAST_FETCH) {
                                cdao.setFillParticipants();
                            } else {
                                final Participants participants = cimp.getParticipants(cdao, readcon);
                                cdao.setParticipants(participants.getList());
                            }
                        } else if (Appointment.FOLDER_ID == cols[a]) {
                            if (recColl.getFieldName(Appointment.FOLDER_ID) != null) {
                                if (oids == null) {
                                    if (requested_folder == 0) {
                                        final int x = setInt(g++, co_rs);
                                        if (x > 0) {
                                            cdao.setGlobalFolderID(x);
                                        } else {
                                            if (bbs) {
                                                cdao.setGlobalFolderID(cdao.getEffectiveFolderId());
                                            } else if (!CachedCalendarIterator.CACHED_ITERATOR_FAST_FETCH) {
                                                final Participants users = cimp.getUserParticipants(cdao, readcon, uid);
                                                cdao.setUsers(users.getUsers());
                                                cdao.setGlobalFolderID(cdao.getEffectiveFolderId());
                                            } else {
                                                cdao.setFillFolderID();
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
                        } else {
                            throw new SearchIteratorException(
                                    SearchIteratorException.SearchIteratorCode.NOT_IMPLEMENTED,
                                    APPOINTMENT, Integer.valueOf(cols[a]));
                        }
                    } else {
                        ff.fillField(cdao, g++, co_rs);
                    }
                }
            } catch(final SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
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
                /*
                if (check_folder_id == 0) { // TODO: Remove this debug information
                    System.out.println("\n\nGOT A zero folder_id :"+cdao.toString());
                    for (int a = 0; a < oids.length; a++) {
                        System.out.print(oids[a][0]+","+oids[a][1]+":");
                    }
                    System.out.println("\n\n");
                }
                 */
                if (check_folder_id != cdao.getParentFolderID()) {
                    LOG.error("Object Not Found: " + "Object not found : uid:oid:fid:InFolder "+so.getUserId() + ':'+ cdao.getObjectID() + ':' + cdao.getParentFolderID() + ':' + check_folder_id, new Throwable());
                    throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, APPOINTMENT, "");
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
                            if (a > 0)  {
                                colss.append(',');
                            }
                            colss.append(fn);
                        }
                        LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception (fid!inFolder) for user:oid:fid:cols ", Integer.valueOf(so.getUserId()), ":", Integer.valueOf(cdao.getObjectID()),":",Integer.valueOf(oids[index][1]),":",colss.toString() }));
                    }
                    throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_5));
                }
            }
            rsNext(false);
            return cdao;
        }
        has_next = false;
        return null;
    }

    /**
     * This method does the complex filling of the calendar object if some
     * series appointment or some series exception appointment is loaded.
     * @param cdao loaded object.
     * @throws OXException if extracting the recurrence pattern string fails.
     */
    private static void extractRecurringInformation(final CalendarDataObject cdao)
        throws OXException {
        if (cdao.isSequence()) {
            recColl.fillDAO(cdao);
            if (cdao.getObjectID() == cdao.getRecurrenceID()) {
                if (cdao.containsOccurrence() && !cdao.containsUntil()) {
                    // INFO: Somebody needs this value, have to check for side effects
                    cdao.setUntil(new Date(recColl.normalizeLong((cdao.getStartDate().getTime() + (Constants.MILLI_DAY * cdao.getRecurrenceCalculator())))));
                }
            } else {
                // Recurring type on a change exception must be removed.
                // Otherwise somebody may treat it as a series appointment.
                recColl.removeRecurringType(cdao);
                if (cdao.getExceptions() != null) {
                    try {
                        final long exc = Long.parseLong(cdao.getExceptions());
                        cdao.setRecurrenceDatePosition(new Date(exc));
                    } catch(final NumberFormatException nfe) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Unable to calculate exception oid:context:exceptions "+cdao.getObjectID()+":"+cdao.getContextID()+":"+cdao.getExceptions());
                        }
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

    public static final void fillUserParticipants(final CalendarDataObject cdao) throws LdapException {
        final Participant participants[] = cdao.getParticipants();
        if (participants == null) {
            return;
        }
        Participants userparticipants = null;
        for (int a = 0; a < participants.length; a++) {
            final Participant p = participants[a];
            if (userparticipants == null) {
                userparticipants = new Participants(cdao.getUsers());
            }
            if (p.getType() == Participant.GROUP) {
                final GroupStorage gs = GroupStorage.getInstance();
                final Group g = gs.getGroup(p.getIdentifier(), cdao.getContext());
                final int m[] = g.getMember();
                for (int b = 0; b < m.length; b++) {
                    final UserParticipant up = new UserParticipant(m[b]);
                    if (!userparticipants.containsUserParticipant(up)) {
                        userparticipants.add(up);
                    }
                }
            } else if (p.getType() == Participant.USER) {
                final UserParticipant up = new UserParticipant(p.getIdentifier());
                up.setDisplayName(p.getDisplayName());
                if (!userparticipants.containsUserParticipant(up)) {
                    userparticipants.add(up);
                }
            } else if (p.getType() == Participant.RESOURCE) {
                cdao.setContainsResources(true);

            }
        }
        cdao.setUsers(userparticipants.getUsers());
    }

    static final Participant[] getNewParticipants(final Participant np[], final Participant op[]) {
        final Participants p = new Participants();
        for (int a = 0; a < np.length; a++ ) {
            if (Arrays.binarySearch(op, np[a]) < 0) {
                p.add(np[a]);
            }
        }
        return p.getList();
    }

    static final Participant[] getDeletedParticipants(final Participant np[], final Participant op[]) {
        final Participants p = new Participants();
        for (int a = 0; a < np.length; a++ ) {
            if (Arrays.binarySearch(op, np[a]) < 0) {
                p.add(np[a]);
            }
        }
        return p.getList();
    }

    /**
     * Gets the new and modified user participants
     *
     * @param np
     *            The current user participants
     * @param op
     *            The old user participants
     * @param owner
     *            The appointment's owner
     * @param uid
     *            Current working session user
     * @param sharedFolderOwner
     *            The shared folder owner
     * @param time_change
     *            <code>true</code> if appointment's start date, end date and/or
     *            recurrence pattern changed; otherwise <code>false</code>
     * @param cdao
     *            The object denoting the changed appointment
     * @return An array of {@link Participants} with length <code>2</code>. If
     *         present index <code>0</code> will contain the new user
     *         participants otherwise <code>null</code>. If present index
     *         <code>1</code> will contain the modified user participants
     *         otherwise <code>null</code>.
     * @throws OXPermissionException
     *             If a permission error occurs
     */
    static final Participants[] getModifiedUserParticipants(final UserParticipant np[], final UserParticipant op[], final int owner, final int uid, final int sharedFolderOwner, final boolean time_change, final CalendarDataObject cdao) throws OXPermissionException {
        final Participants p[] = new Participants[2];
        for (int a = 0; a < np.length; a++ ) {
            final int bs = Arrays.binarySearch(op, np[a]);
            if (bs < 0) {
                if (p[0] == null) {
                    p[0] = new Participants(); // new
                }
                p[0].add(np[a]);
            } else {
                if (cdao.getFolderMoveAction() == NO_MOVE_ACTION || cdao.getFolderMoveAction() == PRIVATE_CURRENT_PARTICIPANT_ONLY) {
                    if (uid == np[a].getIdentifier() || sharedFolderOwner == np[a].getIdentifier()) { // only the owner or the current user can change this object(s)
                        if (np[a].getIdentifier() == op[bs].getIdentifier() ||
                                (cdao.getFolderMoveAction() == PRIVATE_CURRENT_PARTICIPANT_ONLY &&
                                (uid == np[a].getIdentifier() || sharedFolderOwner == np[a].getIdentifier()))) {
                            if (np[a].containsAlarm() || np[a].containsConfirm() || np[a].containsConfirmMessage() || cdao.containsAlarm()) {
                                if (p[1] == null) {
                                    p[1] = new Participants(); // modified
                                }
                                np[a].setIsModified(false);
                                if (cdao.containsAlarm() && recColl.existsReminder(cdao.getContext(), cdao.getObjectID(), uid)) {
                                    np[a].setIsModified(true);
                                    np[a].setAlarmMinutes(cdao.getAlarm());
                                } else if (!np[a].containsAlarm() && recColl.existsReminder(cdao.getContext(), cdao.getObjectID(), uid)) {
                                    np[a].setIsModified(true);
                                    np[a].setAlarmMinutes(op[bs].getAlarmMinutes());
                                }
                                if (!np[a].containsConfirm() || time_change) {
                                    np[a].setIsModified(true);
                                    if (!time_change || np[a].getIdentifier() == uid || np[a].getIdentifier() == sharedFolderOwner) {
                                        np[a].setConfirm(op[bs].getConfirm());
                                    } else {
                                        np[a].setConfirm(CalendarDataObject.NONE);
                                        np[a].setConfirmMessage(null);
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
                        if (!(np[a].getIdentifier() == op[bs].getIdentifier() &&
                                np[a].getAlarmMinutes() == op[bs].getAlarmMinutes() &&
                                np[a].getConfirm() == op[bs].getConfirm() &&
                                np[a].getConfirmMessage() == op[bs].getConfirmMessage())) {

                            /*
                             * We have two options:
                             * 1) Throw an ugly error message that nobody understands
                             * 2) Copy the older values to the submitted ones which is also ugly because
                             *    we may have a different view to the data
                             *
                             * TODO: Make this configurable
                             */

                            //LOG.error("The current user ("+uid+") does not have the appropriate permissions to modify other participant ("+np[a].getIdentifier()+") properties");
                            //throw new OXPermissionException("The current user does not have the appropriate permissions to modify other participant properties");
                            if (op[bs].containsAlarm()) {
                                np[a].setAlarmMinutes(op[bs].getAlarmMinutes());
                            } else {
                                np[a].setAlarmMinutes(-1);
                            }
                            if (!time_change) {
                                np[a].setConfirm(op[bs].getConfirm());
                                np[a].setConfirmMessage(op[bs].getConfirmMessage());
                            } else {
                                np[a].setConfirm(CalendarDataObject.NONE);
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
                            np[a].setConfirm(CalendarDataObject.NONE);
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
        for (int a = 0; a < np.length; a++ ) {
            if (Arrays.binarySearch(op, np[a]) < 0) {
                if (np[a].getPersonalFolderId() != 0) {
                    p.add(np[a]);
                } else {
                    if (np[a].getIdentifier() != uid) {
                        p.add(np[a]);
                    }
                }
            }
        }
        return p.getUsers();
    }

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
            } catch(final SQLException sqle) {
                LOG.error("Error closing PreparedStatement.", sqle);
            }
        }

        if (readcon != null) {
            DBPool.push(c, readcon);
        }
    }

    private final void simpleDataCheck(final CalendarDataObject cdao, final CalendarDataObject edao, final int uid) throws OXException {
        // Both, start and end date are set
        if (cdao.containsStartDate() && cdao.containsEndDate() && cdao.getEndDate().getTime() < cdao.getStartDate().getTime()) {
            throw new OXCalendarException(OXCalendarException.Code.END_DATE_BEFORE_START_DATE);
        }
        // Only start date is set
        if (cdao.containsStartDate() && !cdao.containsEndDate() && edao.getEndDate().getTime() < cdao.getStartDate().getTime()) {
            throw new OXCalendarException(OXCalendarException.Code.END_DATE_BEFORE_START_DATE);
        }// Only end date is set
        if (!cdao.containsStartDate() && cdao.containsEndDate() && cdao.getEndDate().getTime() < edao.getStartDate().getTime()) {
            throw new OXCalendarException(OXCalendarException.Code.END_DATE_BEFORE_START_DATE);
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
                throw new OXCalendarException(OXCalendarException.Code.UNTIL_BEFORE_START_DATE);
            }
        }
        if (cdao.containsLabel() && (cdao.getLabel() < 0 || cdao.getLabel() > 256)) {
            throw new OXCalendarException(OXCalendarException.Code.UNSUPPORTED_LABEL, cdao.getLabel());
        }
        if (cdao.containsPrivateFlag()) {
            if (cdao.getPrivateflag() == 1) {
                if (cdao.getFolderType() != recColl.PRIVATE) {
                    throw new OXCalendarException(OXCalendarException.Code.PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER);
                }
                if (edao == null || (edao != null && edao.containsPrivateFlag() && edao.getPrivateflag() == 1)) {
                    if (cdao.containsObjectID() && cdao.getSharedFolderOwner() != 0 && cdao.getSharedFolderOwner() != uid) {
                        throw new OXCalendarException(OXCalendarException.Code.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED);
                    }
                    if (cdao.getFolderType() != FolderObject.PRIVATE) {
                        throw new OXCalendarException(OXCalendarException.Code.PRIVATE_FLAG_IN_PRIVATE_FOLDER);
                    }
                    if ( (cdao.getUsers() != null && cdao.getUsers().length > 1) || (cdao.getParticipants() != null && cdao.getParticipants().length > 1) ) {
                        throw new OXCalendarException(OXCalendarException.Code.PRIVATE_FLAG_AND_PARTICIPANTS);
                    }
                }
            } else if (cdao.getPrivateflag() != 0) {
                throw new OXCalendarException(OXCalendarException.Code.UNSUPPORTED_PRIVATE_FLAG, cdao.getPrivateflag());
            }
        } else if (edao != null && edao.containsPrivateFlag() && edao.getPrivateflag() == 1) {
            if (cdao.getSharedFolderOwner() != uid) {
                if (cdao.getFolderType() != recColl.PRIVATE) {
                    throw new OXCalendarException(OXCalendarException.Code.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED);
                }
            }
        }
        if (cdao.containsShownAs() && (cdao.getShownAs() < 0 || cdao.getShownAs() > 4)) {
            throw new OXCalendarException(OXCalendarException.Code.UNSUPPORTED_SHOWN_AS, cdao.getShownAs());
        } else if (cdao.containsShownAs() && cdao.getShownAs() == 0) {
            // auto correction
            cdao.setShownAs(CalendarDataObject.RESERVED);
        }
        if (cdao.containsParticipants()) {
            recColl.simpleParticipantCheck(cdao);
        }
    }

    private boolean isUntilBeforeStart(Date until, Date start) {
        return start != null && recColl.normalizeLong(until.getTime()) < recColl.normalizeLong(start.getTime());
    }

    final int checkUpdateRecurring(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        if (!edao.containsRecurrenceType() && !cdao.containsRecurrenceType()) {
            return recColl.RECURRING_NO_ACTION;
        }
        if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() != edao.getObjectID()) {
            /*
             * An update of a change exception
             */
            return recColl.RECURRING_NO_ACTION;
        }
        if (edao.containsRecurrenceType() && edao.getRecurrenceType() > CalendarDataObject.NO_RECURRENCE && (!cdao.containsRecurrenceType() || cdao.getRecurrenceType() == edao.getRecurrenceType())) {
            int ret = recColl.getRecurringAppoiontmentUpdateAction(cdao, edao);
            if (ret == recColl.RECURRING_NO_ACTION) {
                // We have to check if something has been changed in the meantime!
                if (!cdao.containsStartDate() && !cdao.containsEndDate()) {
                    final CalendarDataObject temp = (CalendarDataObject) edao.clone();
                    final RecurringResultsInterface rss = recColl.calculateFirstRecurring(temp);
                    if (rss != null) {
                        final RecurringResultInterface rs = rss.getRecurringResult(0);
                        if (rs != null) {
                            cdao.setStartDate(new Date(rs.getStart()));
                            cdao.setEndDate(new Date(rs.getEnd()));
                        }
                    }
                }

                if (cdao.containsStartDate() && cdao.containsEndDate()) {
                    ret = checkPatternChange(cdao, edao, ret);
                }
            } else {
                if (cdao.getFolderMove()) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_MOVE_EXCEPTION);
                }
                if (recColl.RECURRING_EXCEPTION_DELETE_EXISTING == ret
                        && (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() == edao
                                .getObjectID())) {
                    /*
                     * A formerly created change exception shall be deleted
                     * through an update on master recurring appointment
                     */
                    if (cdao.containsStartDate() && cdao.containsEndDate()) {
                        ret = checkPatternChange(cdao, edao, ret);
                    }
                }
            }
            return ret;
        } else if (edao.containsRecurrenceType() && edao.getRecurrenceType() > CalendarDataObject.NO_RECURRENCE && cdao.getRecurrenceType() != edao.getRecurrenceType()) {
            // Recurring Pattern changed! TODO: Remove all exceptions
            if ((cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) || (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null)) {
                return recColl.RECURRING_CREATE_EXCEPTION;
            }
            cdao.setRecurrenceID(edao.getObjectID());
            if (!cdao.containsStartDate() && !cdao.containsEndDate()) {
                cdao.setStartDate(edao.getStartDate());
                cdao.setEndDate(edao.getEndDate());
            } else if(CalendarDataObject.NO_RECURRENCE != cdao.getRecurrenceType()) {
                cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/Constants.MILLI_DAY)));
                calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
            }

            if (cdao.getRecurrenceType() > 0) {
                calculateEndDateForNewType(cdao, edao);
            }
            
            recColl.changeRecurrenceString(cdao);
            cdao.setExceptions(null);
            cdao.setDelExceptions(null);
            return recColl.CHANGE_RECURRING_TYPE;
        } else if (!edao.containsRecurrenceType() && cdao.getRecurrenceType() > CalendarDataObject.NO_RECURRENCE) {
            // TODO: Change from normal apointment to sequence
            if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrence() != null) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_ALREADY_EXCEPTION);
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
            cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/Constants.MILLI_DAY)));
            cdao.setEndDate(calculateRealRecurringEndDate(cdao));
        } else if (edao.containsRecurrenceType() && cdao.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE) {
            // Sequence reset, this means to delete all existing exceptions
            if (cdao.containsRecurrencePosition() || cdao.containsRecurrenceDatePosition()) {
                return recColl.RECURRING_CREATE_EXCEPTION;
            }
            return recColl.RECURRING_EXCEPTION_DELETE;
        }
        return recColl.RECURRING_NO_ACTION;
    }

    private void calculateEndDateForNewType(CalendarDataObject cdao, CalendarDataObject edao) throws OXException {
        Date occurrenceDate;
        if (cdao.getOccurrence() <= 0) {
            occurrenceDate = recColl.getOccurenceDate(cdao, recColl.MAXTC);
        } else {
            occurrenceDate = recColl.getOccurenceDate(cdao);
        }
        // Get corresponding until date
        final Date untilDate = new Date(recColl.normalizeLong(occurrenceDate.getTime()));
        // Set proper end time
        cdao.setEndDate(calculateRealRecurringEndDate(untilDate, edao.getEndDate(), edao.getFullTime()));
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
    private static final int checkPatternChange(final CalendarDataObject cdao, final CalendarDataObject edao,
            final int recurringAction) throws OXException {
        cdao.setRecurrenceCalculator(((int) ((cdao.getEndDate().getTime() - cdao.getStartDate().getTime()) / Constants.MILLI_DAY)));

        // Have to check if something in the pattern has been changed
        // and then modify the recurring. Assume all data has been provided
        boolean pattern_change = false;
        boolean completenessChecked = false;

        if (cdao.containsInterval() && cdao.getInterval() != edao.getInterval()) {
            recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
            completenessChecked = true;
            pattern_change = true;
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
            cdao.removeUntil();
            // Calculate occurrence's time
            final Date occurrenceDate;
            if (cdao.getOccurrence() <= 0) {
                occurrenceDate = recColl.getOccurenceDate(cdao, recColl.MAXTC);
            } else {
                occurrenceDate = recColl.getOccurenceDate(cdao);
            }
            // Get corresponding until date
            final Date untilDate = new Date(recColl.normalizeLong(occurrenceDate.getTime()));
            // Set proper end time
            cdao.setEndDate(calculateRealRecurringEndDate(untilDate, edao.getEndDate(), edao.getFullTime()));
            pattern_change = true;
        }
        if (cdao.containsUntil() && edao.containsUntil() && recColl.check(cdao.getUntil(), edao.getUntil())) {
            if (!completenessChecked) {
                recColl.checkRecurringCompleteness(cdao, !edao.containsUntil() && !edao.containsOccurrence());
                completenessChecked = true;
            }
            if (cdao.getUntil() != null) {
                cdao.setEndDate(calculateRealRecurringEndDate(cdao));
            } else {
                /* TODO: Change behaviour!
                 * Workaround to make until=null possible for deleting until/occurrences value.
                 * If until is null, it will be removed, so that the getUntil() method calculates
                 * the effective end of the sequence and endDate can be set. After that until needs
                 * to be set to null again so that it is not stored in the database.
                 */
                cdao.removeUntil();
                cdao.setUntil(cdao.getUntil());
                cdao.setEndDate(calculateRealRecurringEndDate(cdao));
                cdao.setUntil(null);
            }
            pattern_change = true;
        }
        if (!cdao.containsOccurrence() && !cdao.containsUntil()) {
            /*
             * Neither occurrences nor until date set; calculate end date from
             * last possible occurrence
             */
            if (!cdao.containsTimezone()) {
                cdao.setTimezone(edao.getTimezoneFallbackUTC());
            }
            cdao.setEndDate(calculateRealRecurringEndDate(cdao));
        }
        /*
         * Detect recurring action dependent on whether pattern was changed or not
         */
        final int retval;
        if (pattern_change) {
            cdao.setRecurrence(null);

            recColl.checkRecurring(cdao);
            recColl.fillDAO(cdao);
            cdao.setExceptions(null);
            cdao.setDelExceptions(null);
            /*
             * Indicate change of recurring type
             */
            retval = recColl.CHANGE_RECURRING_TYPE;
        } else {
            calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
            checkAndRemoveRecurrenceFields(cdao);
            cdao.setRecurrence(edao.getRecurrence());
            /*
             * Return specified recurring action unchanged
             */
            retval = recurringAction;
        }
        return retval;
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
        if (!cdao.containsStartDate()) {
            throw new OXCalendarException(OXCalendarException.Code.MANDATORY_FIELD_START_DATE);
        }
        if (!cdao.containsEndDate()) {
            throw new OXCalendarException(OXCalendarException.Code.MANDATORY_FIELD_END_DATE);
        }
        if (!cdao.containsTitle()) {
            // Adapt to MS Outlook behavior and set empty title
            cdao.setTitle("");
            //throw new OXCalendarException(OXCalendarException.Code.MANDATORY_FIELD_TITLE);
        }
        if (!cdao.containsShownAs()) {
            cdao.setShownAs(CalendarDataObject.RESERVED); // auto correction
        }
    }

    private static interface FieldFiller {
        public void fillField(CalendarDataObject cdao, int columnCount, ResultSet rs) throws SQLException;
    }

    private static final Map<Integer, FieldFiller> FILLERS = new HashMap<Integer, FieldFiller>() {

        private static final long serialVersionUID = -647801170633669563L;

        // instance initializer
        {
            put(Integer.valueOf(Appointment.OBJECT_ID), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setObjectID(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.TITLE), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String t = rs.getString(columnCount);
                    cdao.setTitle(rs.wasNull() ? null : t);
                }
            });
            put(Integer.valueOf(Appointment.LOCATION), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String loc = rs.getString(columnCount);
                    cdao.setLocation(rs.wasNull() ? null : loc);
                }
            });
            put(Integer.valueOf(Appointment.SHOWN_AS), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setShownAs(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.NOTE), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String note = rs.getString(columnCount);
                    cdao.setNote(rs.wasNull() ? null : note);
                }
            });
            put(Integer.valueOf(Appointment.START_DATE), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final Date sd = rs.getTimestamp(columnCount);
                    cdao.setStartDate(rs.wasNull() ? null : sd);
                }
            });
            put(Integer.valueOf(Appointment.END_DATE), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final Date ed = rs.getTimestamp(columnCount);
                    cdao.setEndDate(rs.wasNull() ? null : ed);
                }
            });
            put(Integer.valueOf(Appointment.CREATED_BY), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setCreatedBy(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.MODIFIED_BY), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setModifiedBy(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.CREATION_DATE), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final Timestamp ts = rs.getTimestamp(columnCount);
                    cdao.setCreationDate(rs.wasNull() ? null : ts);
                }
            });
            put(Integer.valueOf(Appointment.LAST_MODIFIED), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final Timestamp ts = new Timestamp(rs.getLong(columnCount));
                    cdao.setLastModified(rs.wasNull() ? null : ts);
                }
            });
            put(Integer.valueOf(Appointment.FULL_TIME), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setFullTime(rs.getInt(columnCount) > 0);
                }
            });
            put(Integer.valueOf(Appointment.COLOR_LABEL), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setLabel(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.PRIVATE_FLAG), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setPrivateFlag(rs.getInt(columnCount) > 0);
                }
            });
            put(Integer.valueOf(Appointment.NUMBER_OF_ATTACHMENTS), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setNumberOfAttachments(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.RECURRENCE_ID), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setRecurrenceID(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.CATEGORIES), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String cat = rs.getString(columnCount);
                    cdao.setCategories(rs.wasNull() ? null : cat);
                }
            });
            put(Integer.valueOf(Appointment.RECURRENCE_TYPE), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String rt = rs.getString(columnCount);
                    cdao.setRecurrence(rs.wasNull() ? null : rt);
                }
            });
            put(Integer.valueOf(Appointment.CHANGE_EXCEPTIONS), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String ce = rs.getString(columnCount);
                    cdao.setExceptions(rs.wasNull() ? null : ce);
                }
            });
            put(Integer.valueOf(Appointment.DELETE_EXCEPTIONS), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String de = rs.getString(columnCount);
                    cdao.setDelExceptions(rs.wasNull() ? null : de);
                }
            });
            put(Integer.valueOf(Appointment.RECURRENCE_CALCULATOR), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setRecurrenceCalculator(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.RECURRENCE_POSITION), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    cdao.setRecurrencePosition(rs.getInt(columnCount));
                }
            });
            put(Integer.valueOf(Appointment.TIMEZONE), new FieldFiller() {
                public void fillField(final CalendarDataObject cdao, final int columnCount, final ResultSet rs)
                        throws SQLException {
                    final String tz = rs.getString(columnCount);
                    cdao.setTimezone(rs.wasNull() ? null : tz);
                }
            });
        }
    };
}
