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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * CalendarCommonCollection
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarCommonCollection {
    
    public static final int PRIVATE = 1;
    public static final int PUBLIC = 2;
    public static final int SHARED = 3;
    
    private static int unique_session_int;
    private static final String calendar_session_name = "CalendarSession";
    
    private static final String fieldMap[] = new String[409];
    private static final Log LOG = LogFactory.getLog(CalendarCommonCollection.class);
    
    public static CalendarCache cache;
    
    static {
        fieldMap[AppointmentObject.TITLE] = "field01";
        
        fieldMap[AppointmentObject.LOCATION] = "field02";
        fieldMap[AppointmentObject.NOTE] = "field04";
        fieldMap[AppointmentObject.RECURRENCE_TYPE] = "field06";
        fieldMap[AppointmentObject.DELETE_EXCEPTIONS] = "field07";
        fieldMap[AppointmentObject.CHANGE_EXCEPTIONS] = "field08";
        fieldMap[AppointmentObject.CATEGORIES] = "field09";
        
        fieldMap[AppointmentObject.START_DATE] =  "timestampfield01";
        fieldMap[AppointmentObject.END_DATE] = "timestampfield02";
        
        fieldMap[AppointmentObject.OBJECT_ID] = "intfield01";
        fieldMap[AppointmentObject.RECURRENCE_ID] = "intfield02";
        fieldMap[AppointmentObject.COLOR_LABEL] = "intfield03";
        fieldMap[AppointmentObject.RECURRENCE_CALCULATOR] = "intfield04";
        fieldMap[AppointmentObject.RECURRENCE_POSITION] = "intfield05";
        fieldMap[AppointmentObject.SHOWN_AS] = "intfield06";
        fieldMap[AppointmentObject.FULL_TIME] = "intfield07";
        fieldMap[AppointmentObject.NUMBER_OF_ATTACHMENTS] = "intfield08";
        fieldMap[AppointmentObject.PRIVATE_FLAG] = "pflag";
        
        fieldMap[AppointmentObject.CREATED_BY] = "pd.created_from";
        fieldMap[AppointmentObject.MODIFIED_BY] =  "pd.changed_from";
        fieldMap[AppointmentObject.CREATION_DATE] = "pd.creating_date";
        fieldMap[AppointmentObject.LAST_MODIFIED] = "pd.changing_date";
        
        fieldMap[AppointmentObject.FOLDER_ID] = "fid";
        fieldMap[CalendarDataObject.TIMEZONE] = "timezone";
    }
    
    private CalendarCommonCollection() { }
    
    public static final String getFieldName(final int i) throws IndexOutOfBoundsException {
        return fieldMap[i];
    }
    
    public static final int getFieldId(final String field) {
        int id = -1;
        for (int a = 0; a < fieldMap.length; a++) {
            if (fieldMap[a] != null && fieldMap[a].equalsIgnoreCase(field)) {
                id = a;
                break;
            }
        }
        return id;
    }
    
    
    public static final boolean checkPermissions(final CalendarDataObject cdao, final Session so, final Context ctx, final Connection readcon, final int action, final int inFolder) throws OXException {
        try {
            final OXFolderAccess access = new OXFolderAccess(readcon, cdao.getContext());
            cdao.setFolderType(access.getFolderType(inFolder, so.getUserId()));
            //cdao.setFolderType(OXFolderTools.getFolderType(inFolder, so.getUserObject().getId(), cdao.getContext(), readcon));
            
            if (action == CalendarOperation.READ) {
                if (cdao.getFolderType() != FolderObject.SHARED) {
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canReadAllObjects()) {
                        return true;
                    } else if (oclp.canReadOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                } else {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));
                    //cdao.setSharedFolderOwner(OXFolderTools.getFolderOwner(inFolder, cdao.getContext(), readcon));
                    if (cdao.getPrivateFlag()) {
                        return false;
                    }
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canReadAllObjects()) {
                        return true;
                    } else if (oclp.canReadOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                }
            } else if (action == CalendarOperation.INSERT) {
                EffectivePermission oclp = null;
                oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                if (cdao.getFolderType() == FolderObject.SHARED) {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));
                    //cdao.setSharedFolderOwner(OXFolderTools.getFolderOwner(inFolder, cdao.getContext(), readcon));
                }
                return oclp.canCreateObjects();
            } else if (action == CalendarOperation.UPDATE) {
                if (cdao.getFolderType() != FolderObject.SHARED) {
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canWriteAllObjects()) {
                        return true;
                    } else if (oclp.canWriteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                } else {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));
                    //cdao.setSharedFolderOwner(OXFolderTools.getFolderOwner(inFolder, cdao.getContext(), readcon));
                    if (cdao.getPrivateFlag()) {
                        return false;
                    }
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canWriteAllObjects()) {
                        return true;
                    } else if (oclp.canWriteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                }
            } else if (action == CalendarOperation.DELETE) {
                if (cdao.getFolderType() != FolderObject.SHARED) {
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canDeleteAllObjects()) {
                        return true;
                    } else if (oclp.canDeleteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                } else {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));
                    //cdao.setSharedFolderOwner(OXFolderTools.getFolderOwner(inFolder, cdao.getContext(), readcon));
                    if (cdao.getPrivateFlag()) {
                        return false;
                    }
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canDeleteAllObjects()) {
                        return true;
                    } else if (oclp.canDeleteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOG.error("ERROR getting read permissions", e);
            return false;
        }
        return false;
    }
    
    public static final boolean getReadPermission(final int oid, final int fid, final Session so, final Context ctx) throws OXException {
        try {
            final OXFolderAccess access = new OXFolderAccess(ctx);
            final int type = access.getFolderType(fid, so.getUserId());
            //int type = OXFolderTools.getFolderType(fid, so.getUserObject().getId(), so.getContext());
            if (type != FolderObject.SHARED) {
                EffectivePermission oclp = null;
                oclp = access.getFolderPermission(fid, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                //oclp = OXFolderTools.getEffectiveFolderOCL(fid, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                if (oclp.canReadAllObjects()) {
                    return true;
                }
                return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.READ);
            }
            return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.READ);
        } catch(final Exception ex) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, ex);
        }
    }
    
    public static final boolean getWritePermission(final int oid, final int fid, final Session so, Context ctx) throws OXException {
        try {
            final OXFolderAccess access = new OXFolderAccess(ctx);
            final int type = access.getFolderType(fid, so.getUserId());
            //int type = OXFolderTools.getFolderType(fid, so.getUserObject().getId(), so.getContext());
            if (type != FolderObject.SHARED) {
                EffectivePermission oclp = null;
                oclp = access.getFolderPermission(fid, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                //oclp = OXFolderTools.getEffectiveFolderOCL(fid, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                if (oclp.canWriteAllObjects()) {
                    return true;
                }
                return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.UPDATE);
            }
            return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.UPDATE);
        } catch(final Exception ex) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, ex);
        }
    }
    
    private static final boolean loadObjectAndCheckPermisions(final int oid, final int fid, final Session so, Context ctx, final int type) throws Exception {
        Connection readcon = null;
        try {
            readcon = DBPool.pickup(ctx);
            final CalendarSql csql = new CalendarSql(so);
            final CalendarDataObject cdao = csql.getObjectById(oid, fid);
            return checkPermissions(cdao, so, ctx, readcon, type, fid);
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (readcon != null) {
                try {
                    DBPool.push(ctx, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("error pushing readable connection", dbpe);
                }
            }
        }
    }
    
    static final void checkAndFillIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up) {
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            Arrays.sort(check);
            int x = Arrays.binarySearch(check, up);
            if (x < 0) {
                final UserParticipant newup[] = new UserParticipant[check.length+1];
                System.arraycopy(check, 0, newup, 0, check.length);
                newup[check.length] = up;
                cdao.setUsers(newup);
            } else if (!cdao.containsObjectID() && !check[x].containsConfirm() && check[x].getConfirm() == CalendarDataObject.NONE) {
                check[x].setConfirm(CalendarDataObject.ACCEPT);
            }
        } else {
            final UserParticipant newup[] = new UserParticipant[1];
            newup[0] = up;
            cdao.setUsers(newup);
        }
    }
    
    static final void checkAndConfirmIfUserUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up) {
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            Arrays.sort(check);
            final int fi = Arrays.binarySearch(check, up);
            if (fi >= 0) {
                if (!check[fi].containsConfirm()) {
                    check[fi].setConfirm(CalendarDataObject.ACCEPT);
                    cdao.setUsers(check);
                }
            }
        } else {
            cdao.setUsers(new UserParticipant[] { up } );
        }
    }
    
    static final UserParticipant[] checkAndModifyAlarm(final CalendarDataObject cdao, UserParticipant check[], final int uid,  final UserParticipant orig[]) {
        if (cdao.containsAlarm()) {
            final UserParticipant up = new UserParticipant(uid);
            if (check == null) {
                check = new UserParticipant[1];
                check[0] = up;
            } else {
                Arrays.sort(check);
            }
            
            final int o = Arrays.binarySearch(orig, up);
            final int f = Arrays.binarySearch(check, up);
            if (f >= 0 && f < check.length) {
                check[f].setAlarmMinutes(cdao.getAlarm());
                check[f].setIsModified(true);
                if (o >= 0 && o < orig.length) {
                    if (!check[f].containsConfirm()) {
                        check[f].setConfirm(orig[o].getConfirm());
                    }
                    if (!check[f].containsConfirmMessage()) {
                        check[f].setConfirmMessage(orig[o].getConfirmMessage());
                    }
                    check[f].setPersonalFolderId(orig[o].getPersonalFolderId());
                }
                
                return check;
            }
        }
        return check;
    }
    
    static final void simpleParticipantCheck(final CalendarDataObject cdao) throws OXException {
        // TODO: Maybe we have to enhance this simple check
        final Participant check[] = cdao.getParticipants();
        if (check != null && check.length > 0) {
            for (int a = 0; a < check.length; a++)  {
                if (check[a].getType() == Participant.EXTERNAL_USER) {
                    if (check[a].getIdentifier() != 0) {
                        check[a].setIdentifier(0); // auto correction ! should not happen !
                    }
                    if (check[a].getEmailAddress() == null) {
                        throw new OXCalendarException(OXCalendarException.Code.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD);
                    }
                }
            }
        }
    }
    
    static final void checkAndFillIfUserIsUser(final CalendarDataObject cdao, final Participant p) {
        final Participant check[] = cdao.getParticipants();
        if (check != null && check.length > 0) {
            Arrays.sort(check);
            if (Arrays.binarySearch(check, p) < 0) {
                final Participant newp[] = new Participant[check.length+1];
                System.arraycopy(check, 0, newp, 0, check.length);
                newp[check.length] = p;
                cdao.setParticipants(newp);
            }
        } else {
            final Participant newp[] = new Participant[1];
            newp[0] = p;
            cdao.setParticipants(newp);
        }
    }
    
    static final void removeParticipant(final CalendarDataObject cdao, final int uid) throws OXException {
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            final UserParticipant ret[] = new UserParticipant[check.length-1];
            int x = 0;
            for (int a = 0; a < check.length; a++)  {
                if (check[a].getIdentifier() != uid) {
                    if (x < ret.length) {
                        ret[x++] = check[a];
                    } else {
                        throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_REMOVE_PARTICIPANT, uid);
                    }
                }
                cdao.setUsers(ret);
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_REMOVE_PARTICIPANT_2);
        }
    }
    
    static final void removeUserParticipant(final CalendarDataObject cdao, final int uid) throws OXException {
    	        final Participant check[] = cdao.getParticipants();
    	        if (check != null && check.length > 0) {
    	            final Participant ret[] = new Participant[check.length-1];
    	            int x = 0;
    	            for (int a = 0; a < check.length; a++)  {
    	                if (check[a].getIdentifier() != uid) {
    	                    if (x < ret.length) {
    	                        ret[x++] = check[a];
    	                    } else {
    	                        throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_REMOVE_PARTICIPANT, uid);
    	                    }
    	                }
    	                cdao.setParticipants(ret);
    	            }
    	        } else {
    	            throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_REMOVE_PARTICIPANT_2);
    	        }
    }        
    
    public static final Date getNextReminderDate(final int oid, final int fid, final Session so) throws OXException, SQLException {
        return getNextReminderDate(oid, fid, so, 0L);
    }
    
    public static final Date getNextReminderDate(final int oid, final int fid, final Session so, long last) throws OXException, SQLException {
        final CalendarSql csql = new CalendarSql(so);
        final CalendarDataObject cdao = csql.getObjectById(oid, fid);
        final int alarm = cdao.getAlarm();
        long start = System.currentTimeMillis();
        if (last > 0) {
            start = last;
            start = ((start/CalendarRecurringCollection.MILLI_DAY)*CalendarRecurringCollection.MILLI_DAY);
            start += CalendarRecurringCollection.MILLI_DAY;
        } else {
            start = ((start/CalendarRecurringCollection.MILLI_DAY)*CalendarRecurringCollection.MILLI_DAY);
        }
        final long end = (start + (CalendarRecurringCollection.MILLI_YEAR * 10L));
        final  RecurringResults rss = CalendarRecurringCollection.calculateRecurring(cdao, start, end, 0, 1, false);
        if (rss != null && rss.size() >= 1) {
            final RecurringResult rs = rss.getRecurringResult(0);
            return new Date(rs.getStart()-(alarm*60*1000L));
        }
        return null;
    }    
    
    public static final boolean existsReminder(final Context c, final int oid, final int uid) {
        final ReminderSQLInterface rsql = new ReminderHandler(c);
        try {
            return rsql.existsReminder(oid, uid, Types.APPOINTMENT);
        } catch (final OXException ex) {
            LOG.error(ex);
        }
        return false;
    }
    
    public static final void debugActiveDates(final long start, final long end, final boolean activeDates[]) {
        System.out.println("\n\nRange : "+new Date(start)+"  -  "+ new Date(end));
        int a = 1;
        long s = start;
        for (; s < end; s+=CalendarRecurringCollection.MILLI_DAY) {
            if (a <= activeDates.length) {
                System.out.print(activeDates[a-1]);
                System.out.print(" ");
                if (a % 7 == 0) {
                    System.out.println("");
                }
            } else {
                System.out.println("a == "+a + " activeDates == "+activeDates.length);
            }
            a++;
        }
        System.out.println("\n\n\n");
    }
    
    public static final void debugRecurringResult(final RecurringResult rr) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(Integer.valueOf(rr.getPosition()));
            LOG.debug(" : ");
            LOG.debug(StringCollection.date2String(new Date(rr.getStart())));
            LOG.debug(" ");
            LOG.debug(StringCollection.date2String(new Date(rr.getEnd())));
            LOG.debug("");
        }
    }
    
    public static final String getUniqueCalendarSessionName() {
        if (unique_session_int == Integer.MAX_VALUE) {
            unique_session_int = 0;
        }
        unique_session_int++;
        return calendar_session_name + unique_session_int;
    }
    
    public static final int[] checkAndAlterCols(int cols[]) {
        boolean RECURRENCE_TYPE = false;
        boolean CHANGE_EXCEPTIONS = false;
        boolean DELETE_EXCEPTIONS = false;
        boolean RECURRENCE_CALCULATOR = false;
        for (int a = 0; a < cols.length; a++) {
            if (cols[a] == AppointmentObject.RECURRENCE_TYPE) {
                RECURRENCE_TYPE = true;
            } else if (cols[a] == AppointmentObject.CHANGE_EXCEPTIONS) {
                CHANGE_EXCEPTIONS = true;
            } else if (cols[a] == AppointmentObject.DELETE_EXCEPTIONS) {
                DELETE_EXCEPTIONS = true;
            } else if (cols[a] == AppointmentObject.RECURRENCE_CALCULATOR) {
                RECURRENCE_CALCULATOR = true;
            }
        }
        int c = 0;
        final int ara[] = new int[3];
        if (RECURRENCE_TYPE) {
            if (!CHANGE_EXCEPTIONS) {
                ara[c++] = AppointmentObject.CHANGE_EXCEPTIONS;
            }
            if (!DELETE_EXCEPTIONS) {
                ara[c++] = AppointmentObject.DELETE_EXCEPTIONS;
            }
            if (!RECURRENCE_CALCULATOR) {
                ara[c++] = AppointmentObject.RECURRENCE_CALCULATOR;
            }
            cols = enhanceCols(cols, ara, c);
        }
        
        return cols;
    }
    
    public static final int[] enhanceCols(final int cols[], final int ara[], final int i) {
        final int ncols[] = new int[cols.length+i];
        System.arraycopy(cols, 0, ncols, 0, cols.length);
        for (int a = 0; a < i; a++)  {
            ncols[cols.length+a] = ara[a];
        }
        return ncols;
    }
    
    public static final void triggerEvent(final Session session, final int action, final AppointmentObject appointmentobject) throws OXException {
        final EventClient eventclient = new EventClient(session);
        switch (action) {
            case CalendarOperation.INSERT:
                try {
                    eventclient.create(appointmentobject); // TODO
                } catch (final Exception e) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 16);
                }
                break;
            case CalendarOperation.UPDATE:
                try {
                    eventclient.modify(appointmentobject); // TODO
                } catch (final Exception e) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 17);
                }
                break;
            case CalendarOperation.DELETE:
                try {
                    eventclient.delete(appointmentobject); // TODO
                } catch (final Exception e) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 18);
                }
                break;
            default:
                throw new OXCalendarException(OXCalendarException.Code.UNSUPPORTED_ACTION_TYPE, action);
        }
    }
    
    public static final String getSQLInStringForParticipants(final UserParticipant[] userParticipant) {
        final StringBuilder sb = new StringBuilder(32);
        if (userParticipant != null && userParticipant.length > 0) {
            sb.append('(');
            for (int a = 0; a < userParticipant.length; a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(userParticipant[a].getIdentifier());
                } else {
                    sb.append(userParticipant[a].getIdentifier());
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }
    
    public static final String getSQLInStringForParticipants(final Participant[] participant) {
        final StringBuilder sb = new StringBuilder(32);
        if (participant != null && participant.length > 0) {
            sb.append('(');
            for (int a = 0; a < participant.length; a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(participant[a].getIdentifier());
                } else {
                    sb.append(participant[a].getIdentifier());
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }
    
    public static final String getSQLInStringForResources(final Participant[] participant) {
        final  StringBuilder sb = new StringBuilder(32);
        boolean containsResources = false;
        if (participant != null && participant.length > 0) {
            for (int a = 0; a < participant.length; a++) {
                if (participant[a].getType() == Participant.RESOURCE) {
                    containsResources = true;
                    break;
                }
            }
        }
        if (containsResources) {
            int x  = 0;
            sb.append('(');
            for (int a = 0; a < participant.length; a++) {
                if (participant[a].getType() == Participant.RESOURCE) {
                    if (x > 0) {
                        sb.append(',');
                        sb.append(participant[a].getIdentifier());
                    } else {
                        sb.append(participant[a].getIdentifier());
                        x = 1;
                    }
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }
    
    public static final boolean inBetween(final long check_start, final long check_end, final long range_start, final long range_end) {
        if (check_start <= range_start  && check_start >= range_start) { return true; } else if (check_start >= range_start && check_end <= range_end) { return true; } else if (check_start > range_start && check_end > range_end && check_start < range_end) { return true; } else if (check_start < range_start && check_end > range_start && check_start < range_end) { return true; }
        return false;
    }
    
    public static final Date[] convertString2Dates(final String s) {
        Date d[] = null;
        final StringTokenizer stok = new StringTokenizer(s, ", ");
        d = new Date[stok.countTokens()];
        int c = 0;
        while (stok.hasMoreTokens()) {
            d[c++] = new Date(Long.valueOf(stok.nextToken()));
        }
        return d;
    }
    
    public static final String convertDates2String(final Date[] d) {
        if (d != null && d.length > 0) {
            final StringBuilder sb = new StringBuilder(32);
            Arrays.sort(d);
            long last = 0;
            for (int a = 0; a < d.length; a++) {
                if (d[a] != null) {
                    final long l = d[a].getTime();
                    if (l != last) {
                        if (last == 0) {
                            sb.append(l);
                        } else {
                            sb.append(',');
                            sb.append(l);
                        }
                        last = l;
                    }
                }
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
        }
        return null;
    }
    
    public static final boolean check(final Object a, final Object b) {
        if (a == b) {
            return false;
        }
        if (a != null && a.equals(b)) {
            return false;
        }
        return true;
    }
    
    public static final CalendarFolderObject getVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, DBPoolingException, SearchIteratorException, OXException {
        
        CalendarFolderObject cfo = null;
        final CalendarFolderObject check = new CalendarFolderObject(uid, c.getContextId(), false);
        Object o = null;
        if (cache == null) {
            cache = CalendarCache.getInstance();
        }
        
        try {
            o = cache.get(check.getObjectKey(), check.getGroupKey());
        } catch (final CacheException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        
        if (o != null) {
            cfo = (CalendarFolderObject)o;
        } else {
            final SearchIterator si = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(uid, groups, uc.getAccessibleModules(), FolderObject.CALENDAR, c);
            EffectivePermission oclp = null;
            FolderObject fo = null;
            try {
                cfo = new CalendarFolderObject(uid, c.getContextId(), false);
                while (si.hasNext()) {
                    fo  = (FolderObject)si.next();
                    oclp = fo.getEffectiveUserPermission(uid, uc);
                    cfo.addFolder(oclp.canReadAllObjects(), oclp.canReadOwnObjects(), fo.isShared(uid), fo.getObjectID(), fo.getType());
                }
                try {
                    cache.add(cfo.getObjectKey(), cfo.getGroupKey(), cfo);
                } catch (final CacheException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            } finally {
                si.close();
            }
        }
        return cfo;
    }
    
    public static final CalendarFolderObject getAllVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, DBPoolingException, SearchIteratorException, OXException {
        CalendarFolderObject cfo = null;
        final CalendarFolderObject check = new CalendarFolderObject(uid, c.getContextId(), true);
        Object o = null;
        if (cache == null) {
            cache = CalendarCache.getInstance();
        }
        
        try {
            o = cache.get(check.getObjectKey(), check.getGroupKey());
        } catch (final CacheException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        
        if (o != null) {
            cfo = (CalendarFolderObject)o;
        } else {
            final SearchIterator si = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(uid, groups, uc.getAccessibleModules(), FolderObject.CALENDAR, c);
            EffectivePermission oclp = null;
            FolderObject fo = null;
            try {
                cfo = new CalendarFolderObject(uid, c.getContextId(), true);
                while (si.hasNext()) {
                    fo  = (FolderObject)si.next();
                    oclp = fo.getEffectiveUserPermission(uid, uc);
                    cfo.addFolder(oclp.canReadAllObjects(), oclp.canReadOwnObjects(), fo.isShared(uid), fo.getObjectID(), fo.getType());
                }
                try {
                    cache.add(cfo.getObjectKey(), cfo.getGroupKey(), cfo);
                } catch (final CacheException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            } finally {
                si.close();
            }
        }
        return cfo;
    }
    
    public static final void getVisibleFolderSQLInString(final StringBuilder sb, final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, OXException, OXCalendarException {
        CalendarFolderObject cfo = null;
        try {
            cfo = CalendarCommonCollection.getVisibleAndReadableFolderObject(uid, groups, c, uc, readcon);
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch (final SearchIteratorException sie) {
            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, sie, 1);
        }
        if (cfo != null) {
            final  Object private_read_all[] = cfo.getPrivateReadableAll();
            final Object private_read_own[] = cfo.getPrivateReadableOwn();
            final Object public_read_all[] = cfo.getPublicReadableAll();
            final Object public_read_own[] = cfo.getPublicReadableOwn();
            
            boolean private_query = false;
            boolean public_query = false;
            int brack = 0;
            if (private_read_all.length > 0) {
                sb.append(" AND (pdm.pfid IN ");
                brack++;
                sb.append(StringCollection.getSqlInString(private_read_all));
                private_query = true;
            }
            
            if (private_read_own.length > 0) {
                if (!private_query) {
                    sb.append(" AND (pd.created_from = ");
                } else {
                    sb.append("OR (pd.created_from = ");
                }
                sb.append(uid);
                sb.append(" AND (pdm.pfid IN ");
                sb.append(StringCollection.getSqlInString(private_read_own));
                sb.append("))");
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
                    sb.append(" AND (pd.fid IN ");
                    sb.append(StringCollection.getSqlInString(public_read_own));
                    sb.append(" AND (pd.created_from = ");
                    sb.append(uid);
                    sb.append("))");
                } else {
                    sb.append(" OR (pd.fid IN ");
                    sb.append(StringCollection.getSqlInString(public_read_own));
                    sb.append(" AND (pd.created_from = ");
                    sb.append(uid);
                    sb.append("))");
                }
            }
            for (int a = 0; a < brack; a++) {
                sb.append(")");
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.CFO_NOT_INITIALIZIED);
        }
    }
    
    static final Date[] removeException(final Date[] date, final Date d) {
        if (date != null && date.length > 0) {
            final Date ret[] = new Date[date.length-1];
            int x = 0;
            for (int a = 0; a < date.length; a++) {
                if (!date[a].equals(d)) {
                    if (x < ret.length) {
                        ret[x++] = d;
                    }
                }
            }
            if (x > 0) {
                return ret;
            }
        }
        return null;
    }
    
    static final CalendarDataObject fillObject(final CalendarDataObject source, final CalendarDataObject destination) {
        if (source.containsTitle()) {
            destination.setTitle(source.getTitle());
        }
        if (source.containsLocation()) {
            destination.setLocation(source.getLocation());
        }
        if (source.containsShownAs()) {
            destination.setShownAs(source.getShownAs());
        }
        if (source.containsCategories()) {
            destination.setCategories(source.getCategories());
        }
        if (source.containsStartDate()) {
            destination.setStartDate(source.getStartDate());
        }
        if (source.containsEndDate()) {
            destination.setEndDate(source.getEndDate());
        }
        if (source.containsRecurrencePosition()) {
            destination.setRecurrencePosition(source.getRecurrencePosition());
        }
        if (source.containsFullTime()) {
            destination.setFullTime(source.getFullTime());
        }
        if (source.containsLabel()) {
            destination.setLabel(source.getLabel());
        }
        if (source.containsNote()) {
            destination.setNote(source.getNote());
        }
        if (source.containsParticipants()) {
            destination.setParticipants(source.getParticipants());
        }
        if (source.containsUserParticipants()) {
            destination.setUsers(source.getUsers());
        }
        if (source.containsPrivateFlag()) {
            destination.setPrivateFlag(source.getPrivateFlag());
        }
        return destination;
    }
    
    static final void removeFieldsFromObject(final CalendarDataObject cdao) {
        cdao.removeTitle();
        cdao.removeLocation();
        cdao.removeShownAs();
        cdao.removeCategories();
        cdao.removeStartDate();
        cdao.removeEndDate();
        cdao.removeNote();
        cdao.removeFullTime();
        cdao.removeLabel();
        cdao.removePrivateFlag();
        cdao.removeUsers();
        cdao.removeParticipants();
        
        cdao.removeRecurrencePosition();
        cdao.removeRecurrenceDatePosition();
        cdao.removeRecurrenceType();
        cdao.removeRecurrenceCount();
    }
    
    static final void purgeExceptionFieldsFromObject(final CalendarDataObject cdao) {
        cdao.setRecurrenceID(0);
        cdao.setRecurrencePosition(0);
        cdao.setRecurrence(null);
    }
    
    public static final boolean isInThePast(final java.sql.Date check) {
        return checkMillisInThePast(check.getTime());
    }
    
    public static final boolean isInThePast(final java.util.Date check) {
        return checkMillisInThePast(check.getTime());
    }
    
    static final boolean checkMillisInThePast(final long check) {
        final long today = CalendarRecurringCollection.normalizeLong(System.currentTimeMillis());
        return check < today;
    }
    
    static CalendarDataObject[] copyAndExpandCalendarDataObjectArray(final CalendarDataObject source[], final CalendarDataObject dest[]) {
        if (source != null && dest != null && source.length > 0) {
            final CalendarDataObject ret[] = new CalendarDataObject[dest.length+source.length];
            System.arraycopy(dest, 0, ret, 0, dest.length);
            System.arraycopy(source, 0, ret, dest.length, source.length);
            return ret;
        }
        return dest;
    }
    
    static final void executeStatement(final String statement, final Object[] fields , final int[] types, Connection writecon, final Context context) throws SQLException, OXException {
        boolean close_write = false;
        try {
            if (writecon == null) {
                writecon = DBPool.pickupWriteable(context);
                close_write = true;
            }
            final PreparedStatement pst = writecon.prepareStatement(statement);
            if (types != null && fields != null && types.length > 0 && fields.length > 0) {
                for (int a  = 0; a < types.length; a ++) {
                    if (fields[a] != null) {
                        pst.setObject(a+1, fields[a], types[a]);
                    } else {
                        pst.setNull(a+1, types[a]);
                    }
                }
            }
            pst.executeBatch();
            pst.close();
        } catch(final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        } catch(final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } finally {
            if (close_write && writecon != null) {
                try {
                    DBPool.pushWrite(context, writecon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("DBPoolingException:deleteSingleAppointment (push) ", dbpe);
                }
            }
        }
    }
    
    static final void removeRecurringType(final CalendarDataObject cdao) {
        cdao.setRecurrenceType(CalendarDataObject.NONE);
        cdao.removeInterval();
        cdao.removeUntil();
        cdao.removeOccurrence();
    }
    
    public static final void closeResultSet(final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch(final SQLException sqle) {
                LOG.warn("Error closing ResultSet", sqle);
            }
        }
    }
    
    public static final void  closePreparedStatement(final PreparedStatement prep) {
        if (prep != null) {
            try {
                prep.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing PreparedStatement.", sqle);
            }
        }
    }
    
    public static final void closeStatement(final Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing Statement.", sqle);
            }
        }
    }
    
    static CalendarDataObject fillFieldsForConflictQuery(final CalendarDataObject cdao, final CalendarDataObject edao, boolean action) {
        if (!action && !cdao.containsStartDate() && !cdao.containsEndDate() && !cdao.containsParticipants() && !cdao.containsRecurrenceType()) {
            return cdao;
        }
        
        final CalendarDataObject clone = (CalendarDataObject)cdao.clone();
        if (!clone.containsShownAs()) {
            clone.setShownAs(edao.getShownAs());
        }
        if (!clone.containsStartDate()) {
            clone.setStartDate(edao.getStartDate());
        }
        if (!clone.containsEndDate()) {
            clone.setEndDate(edao.getEndDate());
        }
        if (!clone.containsObjectID()  || clone.getObjectID() == 0) {
            clone.setObjectID(edao.getObjectID());
        }
        if (clone.getUsers() == null) {
            clone.setUsers(edao.getUsers());
        }
        if (!cdao.containsResources() && edao.containsResources()) {
            // TODO: Take care if edao contains Ressources and remove and new ones !!! We have to merge this!
            clone.setParticipants(edao.getParticipants());
            clone.setContainsResources(edao.containsResources());
            if (!clone.containsParticipants()) {
                clone.setParticipants(edao.getParticipants());
            }
        }
        if (edao.getRecurrenceType() != CalendarObject.NONE) {
            if (cdao.containsRecurrenceDatePosition()) {
                clone.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
            } else if (cdao.containsRecurrencePosition()) {
                clone.setRecurrencePosition(cdao.getRecurrencePosition());
            } else {
                clone.setRecurrenceType(edao.getRecurrenceType());
            }
        }
        
        return clone;
    }
    
    static void detectFolderMoveAction(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        if (cdao.getFolderMove()) { // TODO: Recurring apointments are not allowed to move, this must be checked !!
            if (cdao.getFolderType() != FolderObject.SHARED) {
                if (edao.getFolderType() == cdao.getFolderType()) {
                    if (edao.getFolderType() == FolderObject.PRIVATE) {
                        // Simple: Just change the uid's private folder id
                        cdao.setFolderMoveAction(CalendarOperation.PRIVATE_CURRENT_PARTICIPANT_ONLY);
                    }  // Simple: Just update the overall fid, no seperate action needed
                } else {
                    if (edao.getFolderType() == FolderObject.PRIVATE && cdao.getFolderType() == FolderObject.PUBLIC) {
                        // Move from private to public
                        cdao.setFolderMoveAction(CalendarOperation.PUBLIC_ALL_PARTICIPANTS);
                    } else if (edao.getFolderType() == FolderObject.PUBLIC && cdao.getFolderType() == FolderObject.PRIVATE) {
                        // Move from public to private
                        cdao.setParentFolderID(0);
                        cdao.setFolderMoveAction(CalendarOperation.PRIVATE_ALL_PARTICIPANTS);
                    } else {
                        throw new OXCalendarException(OXCalendarException.Code.MOVE_NOT_SUPPORTED, edao.getFolderType(), cdao.getFolderType());
                    }
                }
            } else {
                //throw new OXCalendarException(OXCalendarException.Code.SHARED_FOLDER_MOVE_NOT_SUPPORTED); // TODO: Allow move from a shared folder
                return;
            }
        }
    }
    
    static final void checkUserParticipantObject(final UserParticipant up, final int folder_type) throws OXException {
        if (up.getIdentifier() < 1) {
            throw new OXCalendarException(OXCalendarException.Code.INTERNAL_USER_PARTICIPANT_CHECK_1, up.getIdentifier(), folder_type);
        } else if ((folder_type == FolderObject.PRIVATE || folder_type == FolderObject.SHARED) && up.getPersonalFolderId() < 1) {
            throw new OXCalendarException(OXCalendarException.Code.INTERNAL_USER_PARTICIPANT_CHECK_2, up.getIdentifier());
        } else if (folder_type == FolderObject.PUBLIC && up.getPersonalFolderId() > 0) {
            throw new OXCalendarException(OXCalendarException.Code.INTERNAL_USER_PARTICIPANT_CHECK_3, up.getIdentifier());
        }
    }
    
    static final boolean detectTimeChange(final CalendarDataObject cdao, final CalendarDataObject edao) {
        if (cdao.containsStartDate() && cdao.containsEndDate()) {
            if (cdao.getStartDate().getTime() != edao.getStartDate().getTime() || cdao.getEndDate().getTime() != edao.getEndDate().getTime()) {
                return true;
            }
        }
        return false;
    }
    
    static final int resolveFolderIDForUser(final int oid, final int uid, final Context c) throws OXException {
        int ret = -1;
        final CalendarSqlImp calendarsqlimp = CalendarSql.getCalendarSqlImplementation();
        Connection readcon = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            readcon = DBPool.pickup(c);
            prep = calendarsqlimp.getPreparedStatement(readcon, "SELECT fid FROM prg_dates WHERE intfield01 = "+ oid + " AND cid = "+ c.getContextId());
            rs = calendarsqlimp.getResultSet(prep);
            if (rs.next()) {
                final int tmp = rs.getInt(1);
                if (!rs.wasNull()) {
                    return tmp;
                }
            }
            closePreparedStatement(prep);
            closeResultSet(rs);
            prep = calendarsqlimp.getPreparedStatement(readcon, "SELECT pfid FROM prg_dates_members WHERE object_id = "+ oid + " AND cid = "+ c.getContextId() + " AND member_uid = "+uid);
            rs = calendarsqlimp.getResultSet(prep);
            if (rs.next()) {
                ret = rs.getInt(1);
                if (rs.wasNull() || ret == 0) {
                    ret = -1;
                }
            }
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch(final SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, sqle, Integer.valueOf(37));
        } finally {
            closePreparedStatement(prep);
            closeResultSet(rs);
            if (readcon != null) {
                try {
                    DBPool.push(c, readcon);
                } catch (final DBPoolingException dbpe) {
                    LOG.error("error pushing readable connection" ,dbpe);
                }
            }
            //return ret;
        }
        return ret;
    }
    
    static final void fillEventInformation(final CalendarDataObject cdao, final CalendarDataObject edao, UserParticipant up_event[], final UserParticipant[] new_userparticipants, final UserParticipant[] deleted_userparticipants, Participant p_event[], final Participant new_participants[], final Participant deleted_participants[]) {
        final Participants pu = new Participants();
        final Participants p = new Participants();
        for (int a = 0; a < up_event.length; a++) {
            pu.add(up_event[a]);
        }
        for (int a = 0; a < p_event.length; a++) {
            p.add(p_event[a]);
        }
        if (new_userparticipants != null && new_userparticipants.length > 0) {
            for (int a = 0; a < new_userparticipants.length; a++) {
                pu.add(new_userparticipants[a]);
            }
        }
        if (new_participants != null && new_participants.length > 0) {
            for (int a = 0; a < new_participants.length; a++) {
                p.add(new_participants[a]);
            }
        }
        if (deleted_userparticipants != null && deleted_userparticipants.length > 0) {
            up_event = pu.getUsers();
            Arrays.sort(up_event);
            for (int a  = 0; a < deleted_userparticipants.length; a++) {
                final int x =  Arrays.binarySearch(up_event, deleted_userparticipants[a]);
                if (x > -1) {
                    final UserParticipant temp[] = new UserParticipant[up_event.length-1];
                    System.arraycopy(up_event, 0, temp, 0, x);
                    System.arraycopy(up_event, x+1, temp, x, ((up_event.length-1)-x));
                }
            }
        }
        if (deleted_participants != null && deleted_participants.length > 0) {
            p_event = p.getList();
            Arrays.sort(p_event);
            for (int a  = 0; a < deleted_participants.length; a++) {
                final int x =  Arrays.binarySearch(p_event, deleted_participants[a]);
                if (x > -1) {
                    final Participant temp[] = new Participant[p_event.length-1];
                    System.arraycopy(p_event, 0, temp, 0, x);
                    System.arraycopy(p_event, x+1, temp, x, ((p_event.length-1)-x));
                }
            }
        }
        cdao.setUsers(up_event);
        cdao.setParticipants(p_event);
        if (!cdao.containsTitle()) {
            cdao.setTitle(edao.getTitle());
        }
        if (!cdao.containsStartDate()) {
            cdao.setStartDate(edao.getStartDate());
        }
        if (!cdao.containsEndDate()) {
            cdao.setEndDate(edao.getEndDate());
        }
        if (!cdao.containsLocation()) {
            cdao.setLocation(edao.getLocation());
        }
        if (!cdao.containsShownAs()) {
            cdao.setShownAs(edao.getShownAs());
        }
        if (!cdao.containsNote()) {
            cdao.setNote(edao.getNote());
        }
        if (!cdao.containsCreatedBy()) {
            cdao.setCreatedBy(edao.getCreatedBy());
        }
    }
    
    public static final CalendarDataObject getDAOFromList(ArrayList al, int oid) {
        CalendarDataObject cdao = null;
        for (int a = 0; a < al.size(); a++) {
            cdao = (CalendarDataObject) al.get(a);
            if (cdao.getObjectID() == oid) {
                return cdao;
            }
        }
        return null;
    }
    
    static boolean checkForSoloReminderUpdate(final CalendarDataObject cdao, final int uc, final boolean cup) {
        if (uc > 2 || cup) {
            return false;
        } else if (CalendarConfig.getSoloReminderTriggerEvent() && cdao.containsAlarm()) {
            return true;
        }
        return false;
    }
    
    static void checkAndRemovePastReminders(CalendarDataObject cdao, CalendarDataObject edao) {
        if (CalendarConfig.getCheckAndRemovePastReminders() && cdao.containsAlarm() && cdao.getAlarm() >= 0) {
            long reminder = 0;
            if (cdao.containsStartDate()) {
                reminder = cdao.getStartDate().getTime();
            } else {
                reminder = edao.getStartDate().getTime();
            }
            if (checkMillisInThePast(reminder-(cdao.getAlarm()*60000))) {
                cdao.removeAlarm();
            }
        }
    }

    static long getUserTimeUTCDate(Date date, String timezone) {
        TimeZone tz = TimeZone.getTimeZone(timezone);
        int offset = tz.getOffset(date.getTime());
        long ndl = date.getTime()+offset;
        long off = ndl%CalendarRecurringCollection.MILLI_DAY;
        ndl -= off;
        //System.out.println(" GOT "+date+" and return "+new Date(ndl));
        return ndl;
    }

	public static boolean checkIfArrayKeyExistInArray(Object a[], Object b[]) {
		if (a != null && b != null) {
			Arrays.sort(b);
			for (int x = 0; x < a.length; x++) {
				if (Arrays.binarySearch(b, a[x]) >= 0) {
					return true;
				}
			}				
		}
		return false;		
	}
	
	public static void checkForInvalidCharacters(CalendarDataObject cdao) throws OXException {
		String error = null;
		if (cdao.containsTitle() && cdao.getTitle() != null) {
			error = Check.containsInvalidChars(cdao.getTitle());
			if (error != null) {
				throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Title", error);
			}
		}
		if (cdao.containsLocation() && cdao.getLocation() != null) {
			error = Check.containsInvalidChars(cdao.getLocation());
			if (error != null) {
				throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Location", error);
			}
		}
		if (cdao.containsNote() && cdao.getNote() != null) {
			error = Check.containsInvalidChars(cdao.getNote());
			if (error != null) {
				throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Note", error);
			}
		}		
		if (cdao.containsCategories() && cdao.getCategories() != null) {
			error = Check.containsInvalidChars(cdao.getCategories());
			if (error != null) {
				throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Tags", error);
			}
		}
		if (cdao.containsUserParticipants() && cdao.getUsers() != null) {
			UserParticipant up[] = cdao.getUsers();
			for (int a = 0; a < up.length; a++) {
				error = Check.containsInvalidChars(up[a].getDisplayName());
				if (error != null) {
					throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Display Name", error);
				}
				error = Check.containsInvalidChars(up[a].getConfirmMessage());
				if (error != null) {
					throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Confirm Message", error);
				}				
			}
		}		
	}
    
}
