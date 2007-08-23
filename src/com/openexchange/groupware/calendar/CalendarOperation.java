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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * CalendarOperation
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarOperation implements SearchIterator {
    
    public static final int READ = 0;
    public static final int INSERT = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = -1;
    
    public static final int MAX_RESULT_LIMIT = -1;
    private int result_counter;
    
    private boolean has_next;
    private static final Log LOG = LogFactory.getLog(CalendarOperation.class);
    
    private ResultSet co_rs;
    private PreparedStatement prep;
    private int cols[];
    private Context c;
    private CalendarSqlImp cimp;
    private Connection readcon;
    private int from, to, uid;
    private SessionObject so;
    private boolean strict;
    private int requested_folder;
    
    public static final int NO_MOVE_ACTION = 0;
    public static final int PRIVATE_CURRENT_PARTICIPANT_ONLY = 1;
    public static final int PRIVATE_ALL_PARTICIPANTS = 2;
    public static final int PUBLIC_ALL_PARTICIPANTS = 3;
    
    public static final char COLON = ':';
    public static final char PERCENT = '%';
    
    private int oids[][];
    
    final CalendarDataObject loadAppointment(final ResultSet load_resultset, final int oid, final int inFolder, final CalendarSqlImp cimp, final Connection readcon, final SessionObject so, final int action, final int action_folder) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException {
        return loadAppointment(load_resultset, oid, inFolder, cimp, readcon, so, action, action_folder, true);
    }
    
    protected final CalendarDataObject loadAppointment(final ResultSet load_resultset, final int oid, final int inFolder, final CalendarSqlImp cimp, final Connection readcon, final SessionObject so, final int action, final int action_folder, final boolean check_permissions) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setObjectID(oid);
        cdao.setContext(so.getContext());
        int check_special_action = action;
        if (action == UPDATE && inFolder != action_folder) { // We move and this means to create a new object
            check_special_action = INSERT;
        }
        try {
            if (load_resultset.next()) {
                int i = 1;
                cdao.setCreatingDate(setTimestamp(i++, load_resultset));
                cdao.setCreatedBy(setInt(i++, load_resultset));
                cdao.setChangingDate(setTimestampFromLong(i++, load_resultset));
                cdao.setModifiedBy(setInt(i++, load_resultset));
                cdao.setGlobalFolderID(setInt(i++, load_resultset));
                cdao.setPrivateFlag(setBooleanToInt(i++, load_resultset));
                if (check_permissions && !CalendarCommonCollection.checkPermissions(cdao, so, readcon, check_special_action, action_folder)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 1 (fid!inFolder) for user:oid:fid:inFolder ", so.getUserObject().getId(), ":",oid,":",inFolder,":",action }));
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
                cdao.setUsers(cimp.getUserParticipants(cdao, readcon, so.getUserObject().getId()).getUsers());
                cdao.setParticipants(cimp.getParticipants(cdao, readcon).getList());
                if (check_permissions && cdao.getEffectiveFolderId() != inFolder) {
                    if (cdao.getFolderType() != FolderObject.SHARED && check_special_action == action) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 2 (fid!inFolder) for user:oid:fid:inFolder ", so.getUserObject().getId(), ":",oid,":",inFolder,":",action }));
                        }
                        throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_2));
                    } else if (action_folder != inFolder && check_special_action == action) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 3 (fid!inFolder) for user:oid:fid:inFolder ", so.getUserObject().getId(), ":",oid,":",inFolder,":",action }));
                        }
                        throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_3));
                    }
                }
                if (check_permissions && action == UPDATE && inFolder != action_folder) {
                    if (!CalendarCommonCollection.checkPermissions(cdao, so, readcon, DELETE, inFolder)) { // Move means to check delete
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception 4 (fid!inFolder) for user:oid:fid:inFolder ", so.getUserObject().getId(), ":",oid,":",inFolder,":",action }));
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
                    if (cdao.getObjectID() == cdao.getRecurrenceID()) {
                        cdao.calculateRecurrence();
                        /*
                        if (cdao.containsOccurrence()) {
                            cdao.removeUntil();
                        }
                         */
                    }
                }
            } else {
                throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.Component.APPOINTMENT, "Object "+oid+" in context "+cdao.getContextID()+" does not exists");
            }
        } finally {
            load_resultset.close();
        }
        return cdao;
    }
    
    private final String setString(final int i, final ResultSet string_rs) throws SQLException {
        final String r = string_rs.getString(i);
        if (!string_rs.wasNull()) {
            return r;
        }
        return null;
    }
    
    private final Timestamp setTimestamp(final int i, final ResultSet ts_rs) throws SQLException {
        final Timestamp r = ts_rs.getTimestamp(i);
        if (!ts_rs.wasNull()) {
            return r;
        }
        return null;
    }
    
    private final Timestamp setTimestampFromLong(final int i, final ResultSet stl_rs) throws SQLException {
        final Timestamp r = new Timestamp(stl_rs.getLong(i));
        if (!stl_rs.wasNull()) {
            return r;
        }
        return null;
    }
    
    private final int setInt(final int i, final ResultSet si_rs) throws SQLException {
        return si_rs.getInt(i);
    }
    
    private final Date setDate(final int i, final ResultSet sd_rs) throws SQLException {
        final Date r =  sd_rs.getTimestamp(i);
        if (!sd_rs.wasNull()) {
            return r;
        }
        return null;
    }
    
    private boolean setBooleanToInt(final int i, final ResultSet sbti_rs) throws SQLException {
        final int r = sbti_rs.getInt(i);
        if (r == 0) {
            return false;
        }
        return true;
    }
    
    static int fillUpdateArray(final CalendarDataObject cdao, final CalendarDataObject edao, final int ucols[]) {
        int uc = 0;
        if (cdao.containsTitle()) {
            if (CalendarCommonCollection.check(cdao.getTitle(), edao.getTitle())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.TITLE) != null) {
                    ucols[uc++] = AppointmentObject.TITLE;
                }
            }
        }
        if (cdao.containsShownAs()) {
            if (CalendarCommonCollection.check(cdao.getShownAs(), edao.getShownAs())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.SHOWN_AS) != null) {
                    ucols[uc++] = AppointmentObject.SHOWN_AS;
                }
            }
        }
        if (cdao.containsStartDate()) {
            if (CalendarCommonCollection.check(cdao.getStartDate().getTime(), edao.getStartDate().getTime())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.START_DATE) != null) {
                    ucols[uc++] = AppointmentObject.START_DATE;
                }
            }
        }
        if (cdao.containsEndDate()) {
            if (CalendarCommonCollection.check(cdao.getEndDate().getTime(), edao.getEndDate().getTime())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.END_DATE) != null) {
                    ucols[uc++] = AppointmentObject.END_DATE;
                }
            }
        }
        if (cdao.containsLocation()) {
            if (CalendarCommonCollection.check(cdao.getLocation(), edao.getLocation())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.LOCATION) != null) {
                    ucols[uc++] = AppointmentObject.LOCATION;
                }
            }
        }
        if (cdao.containsNote()) {
            if (CalendarCommonCollection.check(cdao.getNote(), edao.getNote())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.NOTE) != null) {
                    ucols[uc++] = AppointmentObject.NOTE;
                }
            }
        }
        if (cdao.containsFullTime()) {
            if (CalendarCommonCollection.check(cdao.getFullTime(), edao.getFullTime())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.FULL_TIME) != null) {
                    ucols[uc++] = AppointmentObject.FULL_TIME;
                }
            }
        }
        if (cdao.containsCategories()) {
            if (CalendarCommonCollection.check(cdao.getCategories(), edao.getCategories())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.CATEGORIES) != null) {
                    ucols[uc++] = AppointmentObject.CATEGORIES;
                }
            }
        }
        if (cdao.containsLabel()) {
            if (CalendarCommonCollection.check(cdao.getLabel(), edao.getLabel())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.COLOR_LABEL) != null) {
                    ucols[uc++] = AppointmentObject.COLOR_LABEL;
                }
            }
        }
        if (cdao.containsPrivateFlag()) {
            if (CalendarCommonCollection.check(cdao.getPrivateFlag(), edao.getPrivateFlag())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.PRIVATE_FLAG) != null) {
                    ucols[uc++] = AppointmentObject.PRIVATE_FLAG;
                }
            }
        }
        if (cdao.containsParentFolderID()) {
            if (CalendarCommonCollection.check(cdao.getGlobalFolderID(), edao.getGlobalFolderID())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.FOLDER_ID) != null) {
                    ucols[uc++] = AppointmentObject.FOLDER_ID;
                }
            }
        }
        if (cdao.containsRecurrenceString() || cdao.containsRecurrenceType()) {
            if (CalendarCommonCollection.check(cdao.getRecurrence(), edao.getRecurrence())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_TYPE) != null) {
                    ucols[uc++] = AppointmentObject.RECURRENCE_TYPE;
                }
            }
        }
        if (cdao.containsRecurrenceID()) {
            if (CalendarCommonCollection.check(cdao.getRecurrenceID(), edao.getRecurrenceID())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_ID) != null) {
                    ucols[uc++] = AppointmentObject.RECURRENCE_ID;
                }
            }
        }
        if (cdao.containsDeleteExceptions()) {
            if (CalendarCommonCollection.check(cdao.getDelExceptions(), edao.getDelExceptions())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.DELETE_EXCEPTIONS) != null) {
                    ucols[uc++] = AppointmentObject.DELETE_EXCEPTIONS;
                    cdao.setDeleteExceptions(CalendarRecurringCollection.mergeExceptions(cdao.getDeleteException(), edao.getDeleteException()));
                }
            }
        }
        if (cdao.containsChangeExceptions()) {
            if (CalendarCommonCollection.check(cdao.getExceptions(), edao.getExceptions())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.CHANGE_EXCEPTIONS) != null) {
                    ucols[uc++] = AppointmentObject.CHANGE_EXCEPTIONS;
                    cdao.setChangeExceptions(CalendarRecurringCollection.mergeExceptions(cdao.getChangeException(), edao.getChangeException()));
                }
            }
        }
        if (cdao.containsRecurrencePosition()) {
            if (CalendarCommonCollection.check(cdao.getRecurrencePosition(), edao.getRecurrencePosition())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_POSITION) != null) {
                    ucols[uc++] = AppointmentObject.RECURRENCE_POSITION;
                }
            }
        }
        if (cdao.containsNumberOfAttachments()) {
            if (CalendarCommonCollection.check(cdao.getNumberOfAttachments(), edao.getNumberOfAttachments())) {
                if (CalendarCommonCollection.getFieldName(AppointmentObject.NUMBER_OF_ATTACHMENTS) != null) {
                    ucols[uc++] = AppointmentObject.NUMBER_OF_ATTACHMENTS;
                }
            }
        }
        
        if (CalendarCommonCollection.check(cdao.getRecurrenceCalculator(), edao.getRecurrenceCalculator())) {
            if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_CALCULATOR) != null) {
                ucols[uc++] = AppointmentObject.RECURRENCE_CALCULATOR;
            }
        }
        return uc;
    }
    
    public boolean prepareUpdateAction(final CalendarDataObject cdao, final CalendarDataObject edao, final int uid, final int inFolder, final String timezone) throws OXException, SQLException, Exception {
        boolean action = true;
        if (cdao.getContext() != null) {
            
            OXFolderAccess ofa = new OXFolderAccess(cdao.getContext());
            if (ofa.getFolderModule(inFolder) == FolderObject.CALENDAR) {
                
                
                if (cdao.containsObjectID()) {
                    action = false;
                    if (!cdao.containsModifiedBy()) {
                        cdao.setModifiedBy(uid);
                    }
                    handleFullTime(cdao);
                    if (cdao.isSequence()) {
                        CalendarRecurringCollection.fillDAO(cdao);
                    }
                    prepareUpdate(cdao, inFolder);
                } else {
                    handleFullTime(cdao);
                    if (cdao.isSequence()) {
                        cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/CalendarRecurringCollection.MILLI_DAY)));
                        CalendarRecurringCollection.fillDAO(cdao);
                    }
                    prepareInsert(cdao);
                }
                
                if (action) {
                    cdao.setCreatedBy(uid);
                    cdao.setModifiedBy(uid);
                    checkInsertMandatoryFields(cdao);
                    cdao.setFolderType(ofa.getFolderType(inFolder, uid));
                    
                    if (cdao.getFolderType() == FolderObject.PRIVATE) {
                        cdao.setPrivateFolderID(inFolder);
                        cdao.setGlobalFolderID(0);
                    }
                    
                } else {
                    if (cdao.containsParentFolderID()) {
                        cdao.setFolderType(ofa.getFolderType(cdao.getParentFolderID(), uid));
                    } else {
                        cdao.setFolderType(ofa.getFolderType(inFolder, uid));
                    }
                }
                
                if (cdao.getFolderType() == FolderObject.PRIVATE) {
                    if (action || cdao.containsParticipants()) {
                        UserParticipant up = new UserParticipant(uid);
                        up.setConfirm(CalendarDataObject.ACCEPT);
                        CalendarCommonCollection.checkAndFillIfUserIsParticipant(cdao, up);
                    }
                } else if (cdao.getFolderType() == FolderObject.SHARED) {
                    if (cdao.containsParentFolderID()) {
                        cdao.setSharedFolderOwner(ofa.getFolderOwner(cdao.getParentFolderID()));
                    } else {
                        cdao.setSharedFolderOwner(ofa.getFolderOwner(inFolder));
                    }
                    if (!action && !cdao.containsUserParticipants()) {
                        cdao.setUsers(edao.getUsers());
                    }
                    UserParticipant up = new UserParticipant(cdao.getSharedFolderOwner());
                    if (action) {
                        up.setConfirm(CalendarDataObject.ACCEPT);
                    }
                    CalendarCommonCollection.checkAndFillIfUserIsParticipant(cdao, up);
                    
                } else if (cdao.getFolderType() == FolderObject.PUBLIC) {
                    UserParticipant up = new UserParticipant(uid);
                    up.setConfirm(CalendarDataObject.ACCEPT);
                    CalendarCommonCollection.checkAndConfirmIfUserUserIsParticipant(cdao, up);
                }
                
                Participant p = null;
                if (cdao.getFolderType() == FolderObject.SHARED) {
                    p = new UserParticipant(cdao.getSharedFolderOwner());
                } else {
                    p = new UserParticipant(uid);
                }
                if ((action || cdao.containsUserParticipants()) && cdao.getFolderType() != FolderObject.PUBLIC) {
                    CalendarCommonCollection.checkAndFillIfUserIsUser(cdao, p);
                }
                
                if (!cdao.containsTimezone()) {
                    cdao.setTimezone(timezone);
                }
                
                
            } else {
                throw new OXCalendarException(OXCalendarException.Code.NON_CALENDAR_FOLDER);
            }
            
        } else {
            throw new OXCalendarException(OXCalendarException.Code.CONTEXT_NOT_SET);
        }
        
        simpleDataCheck(cdao, edao, uid);
        if (action && cdao.getParticipants() == null && cdao.getFolderType() == FolderObject.PUBLIC) {
            Participant np[] = new Participant[1];
            final Participant p = new UserParticipant(uid);
            np[0] = p;
            cdao.setParticipants(np);
        }
        fillUserParticipants(cdao);
        return action;
    }
    
    private final void prepareInsert(final CalendarDataObject cdao) throws OXException {
        if (cdao.isSequence(true)) {
            CalendarRecurringCollection.checkRecurring(cdao);
            CalendarRecurringCollection.createDSString(cdao);
            cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/CalendarRecurringCollection.MILLI_DAY)));
            cdao.setEndDate(calculateRealRecurringEndDate(cdao));
        } else {
            cdao.setRecurrence(CalendarRecurringCollection.NO_DS);
        }
    }
    
    private final void prepareUpdate(final CalendarDataObject cdao, final int inFolder) {
        if (cdao.containsParentFolderID()) {
            if (inFolder != cdao.getParentFolderID()) {
                cdao.setFolderMove(true);
            }
        }
    }
    
    public static final void handleFullTime(final CalendarDataObject cdao) {
        if (cdao.getFullTime() && cdao.containsStartDate() && cdao.containsEndDate()) {
            final long mod = cdao.getStartDate().getTime()%CalendarRecurringCollection.MILLI_DAY;
            if (mod != 0) {
                cdao.setStartDate(new Date(cdao.getStartDate().getTime()-mod));
            }
            if ((cdao.getStartDate().getTime() == cdao.getEndDate().getTime()) || (cdao.getEndDate().getTime()-cdao.getStartDate().getTime() < CalendarRecurringCollection.MILLI_DAY)) {
                cdao.setEndDate(new Date(cdao.getStartDate().getTime()+CalendarRecurringCollection.MILLI_DAY));
            } else if (cdao.getEndDate().getTime()%CalendarRecurringCollection.MILLI_DAY != 0) {
                cdao.setEndDate(new Date((cdao.getStartDate().getTime()+(((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/CalendarRecurringCollection.MILLI_DAY)*CalendarRecurringCollection.MILLI_DAY))));
            }
        }
    }
    
    private final Date calculateRealRecurringEndDate(final CalendarDataObject cdao) {
        long until = cdao.getUntil().getTime();
        final long end = cdao.getEndDate().getTime();
        long mod = until%CalendarRecurringCollection.MILLI_DAY;
        until = until - mod;
        mod = end%CalendarRecurringCollection.MILLI_DAY;
        return new Date(until+mod);
    }
    
    private final void calculateAndSetRealRecurringStartAndEndDate(final CalendarDataObject cdao, final CalendarDataObject edao) {
        long start_date = edao.getRecurringStart();
        if (start_date == 0) {
            start_date = edao.getStartDate().getTime();
        }
        final long end_date = edao.getUntil().getTime();
        long start_time = cdao.getStartDate().getTime();
        long end_time = (cdao.getEndDate().getTime() + (cdao.getRecurrenceCalculator() * CalendarRecurringCollection.MILLI_DAY));
        start_time = start_time % CalendarRecurringCollection.MILLI_DAY;
        end_time = end_time % CalendarRecurringCollection.MILLI_DAY;
        cdao.setStartDate(CalendarRecurringCollection.calculateRecurringDate(start_date, start_time));
        cdao.setEndDate(CalendarRecurringCollection.calculateRecurringDate(end_date, end_time));
    }
    
    
    public final boolean hasNext() {
        if (co_rs != null) {
            if (result_counter != MAX_RESULT_LIMIT) {
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
        }
        return false;
    }
    
    public int size() {
        throw new UnsupportedOperationException("Mehtod size() not implemented");
    }
    
    public boolean hasSize() {
        return false;
    }
    
    private void rsNext(final boolean first) {
        if (co_rs != null) {
            try {
                has_next = co_rs.next();
                if (!first) {
                    result_counter++;
                }
            } catch (SQLException sqle) {
                has_next = false;
                LOG.error("Error while getting next result set", sqle);
            }
        }
    }
    
    public final Object next() throws SearchIteratorException, OXException, OXObjectNotFoundException, OXPermissionException {
        if (hasNext()) {
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(c);
            int g = 1;
            boolean bbs = false;
            if (co_rs != null && cols != null) {
                for (int a = 0; a < cols.length; a++) {
                    try {
                        switch (cols[a]) {
                            case AppointmentObject.OBJECT_ID:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.OBJECT_ID) != null) {
                                    cdao.setObjectID(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.TITLE:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.TITLE) != null) {
                                    cdao.setTitle(setString(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.USERS:
                                if (!cdao.containsUserParticipants() && !CachedCalendarIterator.CACHED_ITERATOR_FAST_FETCH) {
                                    final Participants users = cimp.getUserParticipants(cdao, readcon, uid);
                                    cdao.setUsers(users.getUsers());
                                    bbs = true;
                                } else {
                                    cdao.setFillUserParticipants();
                                }
                                break;
                            case AppointmentObject.PARTICIPANTS:
                                if (!CachedCalendarIterator.CACHED_ITERATOR_FAST_FETCH) {
                                    final Participants participants = cimp.getParticipants(cdao, readcon);
                                    cdao.setParticipants(participants.getList());
                                } else {
                                    cdao.setFillParticipants();
                                }
                                break;
                            case AppointmentObject.FOLDER_ID:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.FOLDER_ID) != null) {
                                    if (oids == null) {
                                        if (requested_folder != 0) {
                                            cdao.setGlobalFolderID(requested_folder);
                                            g++;
                                        } else {
                                            int x = setInt(g++, co_rs);
                                            if (x > 0) {
                                                cdao.setGlobalFolderID(x);
                                            } else {
                                                if (bbs) {
                                                    cdao.setGlobalFolderID(cdao.getEffectiveFolderId());
                                                } else if (!CachedCalendarIterator.CACHED_ITERATOR_FAST_FETCH) {
                                                    Participants users = cimp.getUserParticipants(cdao, readcon, uid);
                                                    cdao.setUsers(users.getUsers());
                                                    cdao.setGlobalFolderID(cdao.getEffectiveFolderId());
                                                } else {
                                                    cdao.setFillFolderID();
                                                }
                                            }
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
                                break;
                            case AppointmentObject.LOCATION:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.LOCATION) != null) {
                                    cdao.setLocation(setString(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.SHOWN_AS:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.SHOWN_AS) != null) {
                                    cdao.setShownAs(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.NOTE:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.NOTE) != null) {
                                    cdao.setNote(setString(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.START_DATE:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.START_DATE) != null) {
                                    cdao.setStartDate(setDate(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.END_DATE:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.END_DATE) != null) {
                                    cdao.setEndDate(setDate(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.CREATED_BY:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.CREATED_BY) != null) {
                                    cdao.setCreatedBy(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.MODIFIED_BY:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.MODIFIED_BY) != null) {
                                    cdao.setModifiedBy(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.CREATION_DATE:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.CREATION_DATE) != null) {
                                    cdao.setCreatingDate(setTimestamp(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.LAST_MODIFIED:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.LAST_MODIFIED) != null) {
                                    cdao.setChangingDate(setTimestampFromLong(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.FULL_TIME:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.FULL_TIME) != null) {
                                    cdao.setFullTime(setBooleanToInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.COLOR_LABEL:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.COLOR_LABEL) != null) {
                                    cdao.setLabel(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.PRIVATE_FLAG:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.PRIVATE_FLAG) != null) {
                                    cdao.setPrivateFlag(setBooleanToInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.NUMBER_OF_ATTACHMENTS:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.NUMBER_OF_ATTACHMENTS) != null) {
                                    cdao.setNumberOfAttachments(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.RECURRENCE_ID:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_ID) != null) {
                                    cdao.setRecurrenceID(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.CATEGORIES:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.CATEGORIES) != null) {
                                    cdao.setCategories(setString(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.RECURRENCE_TYPE:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_TYPE) != null) {
                                    cdao.setRecurrence(setString(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.CHANGE_EXCEPTIONS:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.CHANGE_EXCEPTIONS) != null) {
                                    cdao.setExceptions(setString(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.DELETE_EXCEPTIONS:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.DELETE_EXCEPTIONS) != null) {
                                    cdao.setDelExceptions(setString(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.RECURRENCE_CALCULATOR:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_CALCULATOR) != null) {
                                    cdao.setRecurrenceCalculator(setInt(g++, co_rs));
                                }
                                break;
                            case AppointmentObject.RECURRENCE_POSITION:
                                if (CalendarCommonCollection.getFieldName(AppointmentObject.RECURRENCE_POSITION) != null) {
                                    cdao.setRecurrencePosition(setInt(g++, co_rs));
                                }
                                break;
                            case CalendarDataObject.TIMEZONE:
                                if (CalendarCommonCollection.getFieldName(CalendarDataObject.TIMEZONE) != null) {
                                    cdao.setTimezone(setString(g++, co_rs));
                                    break;
                                }
                            default:
                                throw new SearchIteratorException(SearchIteratorException.SearchIteratorCode.NOT_IMPLEMENTED, com.openexchange.groupware.Component.APPOINTMENT, cols[a]);
                        }
                    } catch(SQLException sqle) {
                        throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
                    }
                }
                
                if (cdao.isSequence()) {
                    CalendarRecurringCollection.fillDAO(cdao);
                    if (cdao.getObjectID() != cdao.getRecurrenceID()) {
                        CalendarCommonCollection.removeRecurringType(cdao);
                        if (cdao.getExceptions() != null) {
                            try {
                                long exc = new Long(cdao.getExceptions()).longValue();
                                cdao.setRecurrenceDatePosition(new Date(exc));
                            } catch(NumberFormatException nfe) {
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn("Unable to calculate exception oid:context:exceptions "+cdao.getObjectID()+":"+cdao.getContextID()+":"+cdao.getExceptions());
                                }
                            }
                        }
                    } else {
                        if (cdao.containsOccurrence()) {
                            if (!cdao.containsUntil()) {
                                // INFO: Sombody needs this value, have to check for side effects
                                cdao.setUntil(new Date(CalendarRecurringCollection.normalizeLong((cdao.getStartDate().getTime() + (CalendarRecurringCollection.MILLI_DAY * cdao.getRecurrenceCalculator())))));
                            }
                        }
                    }
                }
            } else {
                throw new OXCalendarException(OXCalendarException.Code.SEARCH_ITERATOR_NULL);
            }
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
                int check_folder_id = oids[index][1];
                if (!cdao.containsParticipants()) {
                    try {
                        Participants participants = cimp.getParticipants(cdao, readcon);
                        cdao.setParticipants(participants.getList());
                    } catch (SQLException e) {
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
                    throw new OXObjectNotFoundException(OXObjectNotFoundException.Code.OBJECT_NOT_FOUND, com.openexchange.groupware.Component.APPOINTMENT, "Object not found : uid:oid:fid:InFolder "+so.getUserObject().getId() + ":"+ cdao.getObjectID() + ":"+cdao.getParentFolderID()+":"+check_folder_id);
                }
                
                if (!CalendarCommonCollection.checkPermissions(cdao, so, readcon, CalendarOperation.READ, check_folder_id)) {
                    StringBuffer colss = new StringBuffer();
                    for (int a = 0; a < cols.length; a++) {
                        String fn = CalendarCommonCollection.getFieldName(cols[a]);
                        if (fn == null) {
                            fn = ""+cols[a];
                        }
                        if (a > 0)  {
                            colss.append(',');
                            colss.append(fn);
                        } else {
                            colss.append(fn);
                        }
                    }
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(StringCollection.convertArraytoString(new Object[] { "Permission Exception (fid!inFolder) for user:oid:fid:cols ", so.getUserObject().getId(), ":", cdao.getObjectID(),":",oids[index][1],":",colss.toString() }));
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
    
    public final SearchIterator setResultSet(final ResultSet rs, final PreparedStatement prep, final int[] cols, final CalendarSqlImp cimp, final Connection readcon, final int from, final int to, final SessionObject so) throws SQLException {
        this.co_rs = rs;
        this.prep = prep;
        this.cols = cols;
        this.cimp = cimp;
        this.c = so.getContext();
        this.readcon = readcon;
        this.from = from;
        this.to = to;
        this.uid = so.getUserObject().getId();
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
        Participant participants[] = cdao.getParticipants();
        if (participants == null) {
            return;
        }
        Participants userparticipants = null;
        for (int a = 0; a < participants.length; a++) {
            Participant p = participants[a];
            if (userparticipants == null) {
                userparticipants = new Participants(cdao.getUsers());
            }
            if (p.getType() == Participant.GROUP) {
                GroupStorage gs = GroupStorage.getInstance(cdao.getContext(), true);
                Group g = gs.getGroup(p.getIdentifier());
                int m[] = g.getMember();
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
        Participants p = new Participants();
        for (int a = 0; a < np.length; a++ ) {
            if (Arrays.binarySearch(op, np[a]) < 0) {
                p.add(np[a]);
            }
        }
        return p.getList();
    }
    
    static final Participant[] getDeletedParticipants(final Participant np[], final Participant op[]) {
        Participants p = new Participants();
        for (int a = 0; a < np.length; a++ ) {
            if (Arrays.binarySearch(op, np[a]) < 0) {
                p.add(np[a]);
            }
        }
        return p.getList();
    }
    
    static final Participants[] getModifiedUserParticipants(final UserParticipant np[], final UserParticipant op[], final int owner, final int uid, final boolean time_change, final CalendarDataObject cdao) throws OXPermissionException {
        Participants p[] = new Participants[2];
        for (int a = 0; a < np.length; a++ ) {
            int bs = Arrays.binarySearch(op, np[a]);
            if (bs < 0) {
                if (p[0] == null) {
                    p[0] = new Participants(); // new
                }
                p[0].add(np[a]);
            } else {
                if (cdao.getFolderMoveAction() == NO_MOVE_ACTION || cdao.getFolderMoveAction() == PRIVATE_CURRENT_PARTICIPANT_ONLY) {
                    if (uid == np[a].getIdentifier()) { // only the owner or the current user can change this object(s)
                        if (np[a].getIdentifier() == op[bs].getIdentifier() ||
                                (cdao.getFolderMoveAction() == PRIVATE_CURRENT_PARTICIPANT_ONLY &&
                                uid == np[a].getIdentifier())) {
                            if (np[a].containsAlarm() || np[a].containsConfirm() || np[a].containsConfirmMessage() || cdao.containsAlarm()) {
                                if (p[1] == null) {
                                    p[1] = new Participants(); // modified
                                }
                                np[a].setIsModified(false);
                                if (cdao.containsAlarm() && CalendarCommonCollection.existsReminder(cdao.getContext(), cdao.getObjectID(), uid)) {
                                    np[a].setIsModified(true);
                                    np[a].setAlarmMinutes(cdao.getAlarm());
                                } else if (!np[a].containsAlarm() && CalendarCommonCollection.existsReminder(cdao.getContext(), cdao.getObjectID(), uid)) {
                                    np[a].setIsModified(true);
                                    np[a].setAlarmMinutes(op[bs].getAlarmMinutes());
                                }
                                if (!np[a].containsConfirm() || time_change) {
                                    np[a].setIsModified(true);
                                    if (!time_change) {
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
                                np[a].setConfirmMessage(null);
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
        Participants p = new Participants();
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
            } catch (SQLException sqle) {
                LOG.error("Error closing ResultSet.", sqle);
            }
        }
        
        if (prep != null) {
            try {
                prep.close();
            } catch(SQLException sqle) {
                LOG.error("Error closing PreparedStatement.", sqle);
            }
        }
        
        if (readcon != null) {
            try {
                DBPool.push(c, readcon);
            } catch (DBPoolingException dbpe) {
                LOG.error(CalendarSql.ERROR_PUSHING_DATABASE, dbpe);
            }
        }
    }
    
    private final void simpleDataCheck(final CalendarDataObject cdao, final CalendarDataObject edao, int uid) throws OXException {
        if (cdao.containsStartDate() && cdao.containsEndDate() && cdao.getEndDate().getTime() < cdao.getStartDate().getTime()) {
            throw new OXCalendarException(OXCalendarException.Code.END_DATE_BEFORE_START_DATE);
        }
        
        if (cdao.containsLabel() && (cdao.getLabel() < 0 || cdao.getLabel() > 256)) {
            throw new OXCalendarException(OXCalendarException.Code.UNSUPPORTED_LABEL, cdao.getLabel());
        }
        if (cdao.containsPrivateFlag()) {
            if (cdao.getPrivateflag() == 1) {
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
                throw new OXCalendarException(OXCalendarException.Code.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED);
            }
        }
        if (cdao.containsShownAs() && (cdao.getShownAs() < 0 || cdao.getShownAs() > 4)) {
            throw new OXCalendarException(OXCalendarException.Code.UNSUPPORTED_SHOWN_AS, cdao.getShownAs());
        } else if (cdao.containsShownAs() && cdao.getShownAs() == 0) {
            // auto correction
            cdao.setShownAs(CalendarDataObject.RESERVED);
        }
        if (cdao.containsParticipants()) {
            CalendarCommonCollection.simpleParticipantCheck(cdao);
        }
    }
    
    final int checkUpdateRecurring(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {        
        if (!edao.containsRecurrenceType() && !cdao.containsRecurrenceType()) {
            return CalendarRecurringCollection.RECURRING_NO_ACTION;
        } else if (edao.containsRecurrenceType() && edao.getRecurrenceType() > CalendarDataObject.NO_RECURRENCE && (!cdao.containsRecurrenceType() || cdao.getRecurrenceType() == edao.getRecurrenceType())) {
            int ret = CalendarRecurringCollection.getRecurringAppoiontmentUpdateAction(cdao, edao);
            if (ret == CalendarRecurringCollection.RECURRING_NO_ACTION) {
                // We have to check if something has been changed in the meantime!
                if (!cdao.containsStartDate() && !cdao.containsEndDate()) {
                    CalendarDataObject temp = (CalendarDataObject) edao.clone();
                    final RecurringResults rss = CalendarRecurringCollection.calculateFirstRecurring(temp);
                    if (rss != null) {
                        RecurringResult rs = rss.getRecurringResult(0);
                        if (rss != null) {
                            cdao.setStartDate(new Date(rs.getStart()));
                            cdao.setEndDate(new Date(rs.getEnd()));
                        }
                    }
                }
                
                
                if (cdao.containsStartDate() && cdao.containsEndDate()) {
                    cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/CalendarRecurringCollection.MILLI_DAY)));
                    
                    // Have to check if something in the pattern has been changed
                    // and then modify the recurring. This means we have to look very
                    // deep into our digital crystal ball because the GUI does not send
                    // all information ...
                    
                    CalendarDataObject clone = (CalendarDataObject)edao.clone();
                    boolean pattern_change = false;
                    boolean time_change = false;
                    
                    if (cdao.containsInterval()) {
                        if (cdao.getInterval() != edao.getInterval()) {
                            clone.setInterval(cdao.getInterval());
                            pattern_change = true;
                        }
                    }
                    if (cdao.containsDays()) {
                        if (cdao.getDays() != edao.getDays()) {
                            clone.setDays(cdao.getDays());
                            pattern_change = true;
                        }
                    }
                    if (cdao.containsDayInMonth()) {
                        if (cdao.getDayInMonth() != edao.getDayInMonth()) {
                            clone.setDayInMonth(cdao.getDayInMonth());
                            pattern_change = true;
                        }
                    }
                    if (cdao.containsMonth()) {
                        if (cdao.getMonth() != edao.getMonth()) {
                            clone.setMonth(cdao.getMonth());
                            pattern_change = true;
                        }
                    }
                    if (cdao.containsOccurrence()) {
                        if (cdao.getOccurrence() != edao.getOccurrence()) {
                            clone.setOccurrence(cdao.getOccurrence());
                            clone.removeUntil();
                            cdao.removeUntil();
                            edao.setUntil(new Date(CalendarRecurringCollection.normalizeLong(CalendarRecurringCollection.getOccurenceDate(clone).getTime())));
                            
                            pattern_change = true;
                            time_change = true;
                        }
                    }
                    if (cdao.containsUntil()) {
                        if (CalendarCommonCollection.check(cdao.getUntil(), edao.getUntil())) {
                            if (cdao.getUntil() == null) {
                                clone.removeUntil();
                            } else {
                                clone.setUntil(cdao.getUntil());
                                cdao.setEndDate(calculateRealRecurringEndDate(clone));
                                pattern_change = true;
                            }
                        }
                    }
                    
                    if (pattern_change) {
                        clone.setRecurrence(null);
                        if (time_change) {
                            calculateAndSetRealRecurringStartAndEndDate(clone, edao);
                            cdao.setStartDate(clone.getStartDate());
                            cdao.setEndDate(clone.getEndDate());
                        } else {
                            cdao.setStartDate(edao.getStartDate());
                            cdao.setEndDate(edao.getEndDate());
                        }
                        CalendarRecurringCollection.fillDAO(clone);
                        cdao.setRecurrence(clone.getRecurrence());
                        cdao.setExceptions(null);
                        cdao.setDelExceptions(null);
                        return CalendarRecurringCollection.CHANGE_RECURRING_TYPE;
                    }
                    calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
                    checkAndRemoveRecurrenceFields(cdao);
                    cdao.setRecurrence(edao.getRecurrence());
                }
            } else {
                if (cdao.getFolderMove()) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_EXCEPTION_MOVE_EXCEPTION);
                }
            }
            return ret;
        } else if (edao.containsRecurrenceType() && edao.getRecurrenceType() > CalendarDataObject.NO_RECURRENCE && cdao.getRecurrenceType() != edao.getRecurrenceType()) {
            // Recurring Pattern changed! TODO: Remove all exceptions
            if (cdao.containsRecurrencePosition() || cdao.containsRecurrenceDatePosition()) {
                return CalendarRecurringCollection.RECURRING_CREATE_EXCEPTION;
            }
            cdao.setRecurrenceID(edao.getObjectID());
            if (!cdao.containsStartDate() && !cdao.containsEndDate()) {
                cdao.setStartDate(edao.getStartDate());
                cdao.setEndDate(edao.getEndDate());
            } else {
                cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/CalendarRecurringCollection.MILLI_DAY)));
                calculateAndSetRealRecurringStartAndEndDate(cdao, edao);
            }
            
            cdao.setRecurrence(CalendarRecurringCollection.createDSString(cdao));
            cdao.setExceptions(null);
            cdao.setDelExceptions(null);
            return CalendarRecurringCollection.CHANGE_RECURRING_TYPE;
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
                cdao.setRecurrence(CalendarRecurringCollection.createDSString(cdao));
            }
            cdao.setRecurrenceCalculator(((int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/CalendarRecurringCollection.MILLI_DAY)));
            cdao.setEndDate(calculateRealRecurringEndDate(cdao));
        } else if (edao.containsRecurrenceType() && cdao.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE) {
            // Sequence reset, this means to delete all existing exceptions
            if (cdao.containsRecurrencePosition() || cdao.containsRecurrenceDatePosition()) {
                return CalendarRecurringCollection.RECURRING_CREATE_EXCEPTION;
            }
            return CalendarRecurringCollection.RECURRING_EXCEPTION_DELETE;
        }
        return CalendarRecurringCollection.RECURRING_NO_ACTION;
    }
    
    private void checkAndRemoveRecurrenceFields(final CalendarDataObject cdao) {
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
    
    private void checkInsertMandatoryFields(final CalendarDataObject cdao) throws OXException {
        if (!cdao.containsStartDate()) {
            throw new OXCalendarException(OXCalendarException.Code.MANDATORY_FIELD_START_DATE);
        } else if (!cdao.containsEndDate()) {
            throw new OXCalendarException(OXCalendarException.Code.MANDATORY_FIELD_END_DATE);
        } else if (!cdao.containsTitle()) {
            throw new OXCalendarException(OXCalendarException.Code.MANDATORY_FIELD_TITLE);
        }  else if (!cdao.containsShownAs()) {
            cdao.setShownAs(CalendarDataObject.RESERVED); // auto correction
        }
    }
    
}

