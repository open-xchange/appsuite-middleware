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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.caching.CacheException;
import com.openexchange.event.EventException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
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

public final class CalendarCommonCollection {
    
    public static final int PRIVATE = 1;
    public static final int PUBLIC = 2;
    public static final int SHARED = 3;
    
    private static int unique_session_int;
    private static final String calendar_session_name = "CalendarSession";
    
    private static final String fieldMap[] = new String[409];
    private static final Log LOG = LogFactory.getLog(CalendarCommonCollection.class);
    
    private static CalendarCache cache;
    
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
    
    private CalendarCommonCollection() { super(); }
    
    public static String getFieldName(final int i) throws IndexOutOfBoundsException {
        return fieldMap[i];
    }
    
    public static int getFieldId(final String field) {
        int id = -1;
        for (int a = 0; a < fieldMap.length; a++) {
            if (fieldMap[a] != null && fieldMap[a].equalsIgnoreCase(field)) {
                id = a;
                break;
            }
        }
        return id;
    }
    
    
    public static boolean checkPermissions(final CalendarDataObject cdao, final Session so, final Context ctx, final Connection readcon, final int action, final int inFolder) throws OXException {
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
                if (cdao.getFolderType() == FolderObject.SHARED) {
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
                } else {
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
                if (cdao.getFolderType() == FolderObject.SHARED) {
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
                } else {
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
    
    public static boolean getReadPermission(final int oid, final int fid, final Session so, final Context ctx) throws OXException {
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
        } catch(final OXObjectNotFoundException onfe) {
        	throw onfe;
        } catch(final Exception ex) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, ex);
        }
    }
    
    public static boolean getWritePermission(final int oid, final int fid, final Session so, final Context ctx) throws OXException {
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
        } catch(final OXObjectNotFoundException onfe) {
        	throw onfe;            
        } catch(final Exception ex) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, ex);
        }
    }
    
    private static boolean loadObjectAndCheckPermisions(final int oid, final int fid, final Session so, final Context ctx, final int type) throws Exception {
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

    static final boolean checkIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up) {
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            Arrays.sort(check);
            int x = Arrays.binarySearch(check, up);
            if (x >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add editing user or shared folder owner to user participants. This ensures
     * the user itself is always on the participants list.
     */
    static void checkAndFillIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up) {
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            Arrays.sort(check);
            final int x = Arrays.binarySearch(check, up);
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
    
    static void checkAndConfirmIfUserUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up) {
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
    
    static UserParticipant[] checkAndModifyAlarm(final CalendarDataObject cdao, UserParticipant check[], final int uid,  final UserParticipant orig[]) {
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
    
    static void simpleParticipantCheck(final CalendarDataObject cdao) throws OXException {
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
    
    /**
     * If user or shared folder owner is missing in participants it is added.
     */
    static void checkAndFillIfUserIsUser(final CalendarDataObject cdao, final Participant p) {
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
    
    static void removeParticipant(final CalendarDataObject cdao, final int uid) throws OXException {
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
    
    static void removeUserParticipant(final CalendarDataObject cdao, final int uid) throws OXException {
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
    
    public static Date getNextReminderDate(final int oid, final int fid, final Session so) throws OXException, SQLException {
        return getNextReminderDate(oid, fid, so, 0L);
    }
    
    public static Date getNextReminderDate(final int oid, final int fid, final Session so, final long last) throws OXException, SQLException {
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
    
    public static boolean existsReminder(final Context c, final int oid, final int uid) {
        final ReminderSQLInterface rsql = new ReminderHandler(c);
        try {
            return rsql.existsReminder(oid, uid, Types.APPOINTMENT);
        } catch (final OXException ex) {
            LOG.error(ex);
        }
        return false;
    }
    
    public static void debugActiveDates(final long start, final long end, final boolean activeDates[]) {
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
    
    public static void debugRecurringResult(final RecurringResult rr) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(Integer.valueOf(rr.getPosition()));
            LOG.debug(" : ");
            LOG.debug(StringCollection.date2String(new Date(rr.getStart())));
            LOG.debug(" ");
            LOG.debug(StringCollection.date2String(new Date(rr.getEnd())));
            LOG.debug("");
        }
    }
    
    public static String getUniqueCalendarSessionName() {
        if (unique_session_int == Integer.MAX_VALUE) {
            unique_session_int = 0;
        }
        unique_session_int++;
        return calendar_session_name + unique_session_int;
    }
    
    public static int[] checkAndAlterCols(int cols[]) {
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
    
    public static int[] enhanceCols(final int cols[], final int ara[], final int i) {
        final int ncols[] = new int[cols.length+i];
        System.arraycopy(cols, 0, ncols, 0, cols.length);
        for (int a = 0; a < i; a++)  {
            ncols[cols.length+a] = ara[a];
        }
        return ncols;
    }
    
    public static void triggerEvent(final Session session, final int action, final AppointmentObject appointmentobject) throws OXException {
        final EventClient eventclient = new EventClient(session);
        switch (action) {
            case CalendarOperation.INSERT:
                try {
                    eventclient.create(appointmentobject); // TODO
                } catch (final Exception e) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(16));
                }
                break;
            case CalendarOperation.UPDATE:
                try {
                    eventclient.modify(appointmentobject); // TODO
                } catch (final Exception e) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(17));
                }
                break;
            case CalendarOperation.DELETE:
                try {
                    eventclient.delete(appointmentobject); // TODO
                } catch (final Exception e) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, Integer.valueOf(18));
                }
                break;
            default:
                throw new OXCalendarException(OXCalendarException.Code.UNSUPPORTED_ACTION_TYPE, Integer.valueOf(action));
        }
    }

    public static void triggerModificationEvent(final Session session, final CalendarDataObject oldAppointment,
			final CalendarDataObject newAppointment) throws OXCalendarException {
		final EventClient eventclient = new EventClient(session);
		try {
			final FolderObject sourceFolder = getFolder(session, oldAppointment.getEffectiveFolderId());
			eventclient.modify(oldAppointment, newAppointment, sourceFolder); // TODO
		} catch (final AbstractOXException e) {
			throw new OXCalendarException(e);
		} catch (final EventException e) {
			throw new OXCalendarException(OXCalendarException.Code.EVENT_ERROR, e, e.getMessage());
		}

	}

	private static FolderObject getFolder(final Session session, final int fid) throws OXException, ContextException {
		final Context ctx = ContextStorage.getStorageContext(session);

		if (FolderCacheManager.isEnabled()) {
			return FolderCacheManager.getInstance().getFolderObject(fid, true, ctx, null);
		}
		return FolderObject.loadFolderObjectFromDB(fid, ctx, null);
	}

    public static String getSQLInStringForParticipants(final UserParticipant[] userParticipant) {
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
    
    public static String getSQLInStringForParticipants(final Participant[] participant) {
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
    
    public static String getSQLInStringForResources(final Participant[] participant) {
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
    
    /**
	 * Checks if range specified by <code>check_start</code> and
	 * <code>check_end</code> intersects/overlaps the range specified by
	 * <code>range_start</code> and <code>range_end</code>.
	 * 
	 * @param check_start
	 *            The check start
	 * @param check_end
	 *            The check end
	 * @param range_start
	 *            The range start
	 * @param range_end
	 *            The range end
	 * @return <code>true</code> if range specified by <code>check_start</code>
	 *         and <code>check_end</code> intersects/overlaps the range
	 *         specified by <code>range_start</code> and <code>range_end</code>;
	 *         otherwise <code>false</code>
	 */
	public static boolean inBetween(final long check_start, final long check_end, final long range_start,
			final long range_end) {
		return (check_start < range_end) && (check_end > range_start);
		
		/*if (check_start <= range_start && check_start >= range_start) {
			return true;
		} else if (check_start >= range_start && check_end <= range_end) {
			return true;
		} else if (check_start > range_start && check_end > range_end && check_start < range_end) {
			return true;
		} else if (check_start < range_start && check_end > range_start && check_start < range_end) {
			return true;
		}
		return false;*/
	}

    /**
	 * Converts given string of comma-separated <i>long</i>s to an array of
	 * {@link Date} objects
	 * 
	 * @param s
	 *            The string of comma-separated <i>long</i>s
	 * @return An array of {@link Date} objects
	 */
	public static Date[] convertString2Dates(final String s) {
		if (s == null) {
			return null;
		} else if (s.length() == 0) {
			return new Date[0];
		}
		final String[] sa = s.split(" *, *");
		final Date dates[] = new Date[sa.length];
		for (int i = 0; i < dates.length; i++) {
			dates[i] = new Date(Long.parseLong(sa[i]));
		}
		return dates;
	}

    /**
	 * Converts given array of {@link Date} objects to a string of
	 * comma-separated <i>long</i>s
	 * 
	 * @param d
	 *            The array of {@link Date} objects
	 * @return A string of comma-separated <i>long</i>s
	 */
	public static String convertDates2String(final Date[] d) {
		if (d == null || d.length == 0) {
			return null;
		}
		final StringBuilder sb = new StringBuilder(d.length * 16);
		Arrays.sort(d);
		sb.append(d[0].getTime());
		for (int i = 1; i < d.length; i++) {
			sb.append(',').append(d[i].getTime());
		}
		return sb.toString();
	}
    
    public static boolean check(final Object a, final Object b) {
        if (a == b) {
            return false;
        }
        if (a != null && a.equals(b)) {
            return false;
        }
        return true;
    }
    
    public static CalendarFolderObject getVisibleAndReadableFolderObject(final int uid, final int groups[],
			final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException,
			DBPoolingException, SearchIteratorException, OXException {
    	return _getVisibleAndReadableFolderObject(uid, groups, c, uc, readcon, false);
	}

	public static CalendarFolderObject getAllVisibleAndReadableFolderObject(final int uid, final int groups[],
			final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException,
			DBPoolingException, SearchIteratorException, OXException {
		return _getVisibleAndReadableFolderObject(uid, groups, c, uc, readcon, true);
	}

	private static CalendarFolderObject _getVisibleAndReadableFolderObject(final int uid, final int groups[],
			final Context c, final UserConfiguration uc, final Connection readcon, final boolean fillShared) throws SQLException,
			DBPoolingException, SearchIteratorException, OXException {
		CalendarFolderObject cfo = null;
		final CalendarFolderObject check = new CalendarFolderObject(uid, c.getContextId(), fillShared);
		Object o = null;
		if (cache == null) {
			cache = CalendarCache.getInstance();
		}

		o = cache.get(check.getObjectKey(), check.getGroupKey());

		if (o == null) {
			final SearchIterator<FolderObject> si = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(uid,
					groups, uc.getAccessibleModules(), FolderObject.CALENDAR, c, readcon);
			EffectivePermission oclp = null;
			FolderObject fo = null;
			try {
				cfo = new CalendarFolderObject(uid, c.getContextId(), fillShared);
				while (si.hasNext()) {
					fo = si.next();
					oclp = fo.getEffectiveUserPermission(uid, uc);
					cfo.addFolder(oclp.canReadAllObjects(), oclp.canReadOwnObjects(), fo.isShared(uid), fo
							.getObjectID(), fo.getType());
				}
				try {
					cache.add(cfo.getObjectKey(), cfo.getGroupKey(), cfo);
				} catch (final CacheException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			} finally {
				si.close();
			}
		} else {
			cfo = (CalendarFolderObject) o;
		}
		return cfo;
	}
    
    public static void getVisibleFolderSQLInString(final StringBuilder sb, final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, OXException, OXCalendarException {
        CalendarFolderObject cfo = null;
        try {
            cfo = CalendarCommonCollection.getVisibleAndReadableFolderObject(uid, groups, c, uc, readcon);
        } catch (final DBPoolingException dbpe) {
            throw new OXException(dbpe);
        } catch (final SearchIteratorException sie) {
            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, sie, Integer.valueOf(1));
        }
        if (cfo == null) {
            throw new OXCalendarException(OXCalendarException.Code.CFO_NOT_INITIALIZIED);
        }
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
		    if (private_query) {
		        sb.append("OR (pd.created_from = ");
		    } else {
		        sb.append(" AND (pd.created_from = ");
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
		    if (private_query || public_query) {
		        sb.append(" OR (pd.fid IN ");
		        sb.append(StringCollection.getSqlInString(public_read_own));
		        sb.append(" AND (pd.created_from = ");
		        sb.append(uid);
		        sb.append("))");
		    } else {
		        sb.append(" AND (pd.fid IN ");
		        sb.append(StringCollection.getSqlInString(public_read_own));
		        sb.append(" AND (pd.created_from = ");
		        sb.append(uid);
		        sb.append("))");
		    }
		}
		for (int a = 0; a < brack; a++) {
		    sb.append(')');
		}
    }
    
    /**
     * Returns a {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     * 
     * @param dates The date array
     * @param d The date to check against
     * @return A {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     */
    static Date[] removeException(final Date[] dates, final Date d) {
        return removeException(dates, d.getTime());
    }

    /**
     * Returns a {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     * 
     * @param dates The date array
     * @param dateTime The date time to check against
     * @return A {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     */
    static Date[] removeException(final Date[] dates, final long dateTime) {
		if (dates != null && dates.length > 0) {
			final Date ret[] = new Date[dates.length - 1];
			int x = 0;
			for (int a = 0; a < dates.length; a++) {
				if (dates[a].getTime() != dateTime) {
					if (x < ret.length) {
						ret[x++] = dates[a];
					}
				}
			}
			if (x > 0) {
				return ret;
			}
		}
		return null;
	}

    /**
     * Returns a {@link Date} array with <code>d</code> added to given date array
     * if not already contained.
     * 
     * @param dates The date array
     * @param d The date to add
     * @return A {@link Date} array with <code>d</code> added to given date array
     */
    static Date[] addException(final Date[] dates, final Date d) {
		if (dates != null && dates.length > 0) {
			for (int i = 0; i < dates.length; i++) {
				if (dates[i].equals(d)) {
					return dates;
				}
			}
			final Date ret[] = new Date[dates.length + 1];
			System.arraycopy(dates, 0, ret, 1, dates.length);
			ret[0] = d;
			Arrays.sort(ret);
			return ret;
		}
		return new Date[] { d };
	}
    
    static CalendarDataObject fillObject(final CalendarDataObject source, final CalendarDataObject destination) {
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
    
    static void removeFieldsFromObject(final CalendarDataObject cdao) {
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
    
    static void purgeExceptionFieldsFromObject(final CalendarDataObject cdao) {
        cdao.setRecurrenceID(0);
        cdao.setRecurrencePosition(0);
        cdao.setRecurrence(null);
    }
    
    public static boolean isInThePast(final java.sql.Date check) {
        return checkMillisInThePast(check.getTime());
    }
    
    public static boolean isInThePast(final java.util.Date check) {
        return checkMillisInThePast(check.getTime());
    }

    /**
	 * Checks if given time millis are less than today (normalized current time
	 * millis):
	 * 
	 * <pre>
	 * return check &lt; (CalendarRecurringCollection.normalizeLong(System.currentTimeMillis()));
	 * </pre>
	 * 
	 * @param check
	 *            The time millis to check against today's millis
	 * @return <code>true</code> if given time millis are less than normalized
	 *         current time millis; otherwise <code>false</code>
	 */
	static boolean checkMillisInThePast(final long check) {
		return check < (CalendarRecurringCollection.normalizeLong(System.currentTimeMillis()));
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
    
    static void executeStatement(final String statement, final Object[] fields , final int[] types, Connection writecon, final Context context) throws SQLException, OXException {
        boolean close_write = false;
        try {
            if (writecon == null) {
                writecon = DBPool.pickupWriteable(context);
                close_write = true;
            }
            final PreparedStatement pst = writecon.prepareStatement(statement);
            if (types != null && fields != null && types.length > 0 && fields.length > 0) {
                for (int a  = 0; a < types.length; a ++) {
                    if (fields[a] == null) {
                        pst.setNull(a+1, types[a]);
                    } else {
                        pst.setObject(a+1, fields[a], types[a]);
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
    
    static void removeRecurringType(final CalendarDataObject cdao) {
        cdao.setRecurrenceType(CalendarDataObject.NONE);
        cdao.removeInterval();
        cdao.removeUntil();
        cdao.removeOccurrence();
    }
    
    public static void closeResultSet(final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch(final SQLException sqle) {
                LOG.warn("Error closing ResultSet", sqle);
            }
        }
    }
    
    public static void  closePreparedStatement(final PreparedStatement prep) {
        if (prep != null) {
            try {
                prep.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing PreparedStatement.", sqle);
            }
        }
    }
    
    public static void closeStatement(final Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing Statement.", sqle);
            }
        }
    }
    
    static CalendarDataObject fillFieldsForConflictQuery(final CalendarDataObject cdao, final CalendarDataObject edao, final boolean action) throws OXException {
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
        if (cdao.containsParticipants() && cdao.getParticipants() != null) {
        	// cdao contains participants informations; just ensure containsResources is set correctly
        	if (!cdao.containsResources()) {
            	// Ensure containsResources is set properly
            	final Participant[] participants = cdao.getParticipants();
        		for (int i = 0; i < participants.length; i++) {
    				if (participants[i].getType() == Participant.RESOURCE) {
    					clone.setContainsResources(true);
    					break;
    				}
    			}
        	}
        } else {
        	// fill participants information from edao
        	// TODO: Take care if edao contains Ressources and remove and new ones !!! We have to merge this!
            clone.setParticipants(edao.getParticipants());
            clone.setContainsResources(edao.containsResources());
            if (!clone.containsParticipants()) {
                clone.setParticipants(edao.getParticipants());
            }
        }      
//        if (!cdao.containsParticipants() && !cdao.containsResources() && edao.containsResources()) {
//            // TODO: Take care if edao contains Ressources and remove and new ones !!! We have to merge this!
//            clone.setParticipants(edao.getParticipants());
//            clone.setContainsResources(edao.containsResources());
//            if (!clone.containsParticipants()) {
//                clone.setParticipants(edao.getParticipants());
//            }
//        }
        if (edao.getRecurrenceType() != CalendarObject.NONE) {
            if (cdao.containsRecurrenceDatePosition()) {
                clone.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
            } else if (cdao.containsRecurrencePosition()) {
                clone.setRecurrencePosition(cdao.getRecurrencePosition());
            } else if (!cdao.containsRecurrenceType()) {
                clone.setRecurrence(edao.getRecurrence());
            } else {
                CalendarRecurringCollection.fillDAO(cdao);
                clone.setRecurrence(cdao.getRecurrence());
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
    
    static void checkUserParticipantObject(final UserParticipant up, final int folder_type) throws OXException {
        if (up.getIdentifier() < 1) {
            throw new OXCalendarException(OXCalendarException.Code.INTERNAL_USER_PARTICIPANT_CHECK_1, up.getIdentifier(), folder_type);
        } else if ((folder_type == FolderObject.PRIVATE || folder_type == FolderObject.SHARED) && up.getPersonalFolderId() < 1) {
            throw new OXCalendarException(OXCalendarException.Code.INTERNAL_USER_PARTICIPANT_CHECK_2, up.getIdentifier());
        } else if (folder_type == FolderObject.PUBLIC && up.getPersonalFolderId() > 0) {
            throw new OXCalendarException(OXCalendarException.Code.INTERNAL_USER_PARTICIPANT_CHECK_3, up.getIdentifier());
        }
    }
    
    static boolean detectTimeChange(final CalendarDataObject cdao, final CalendarDataObject edao) {
        if (cdao.containsStartDate() && cdao.containsEndDate()) {
            if (cdao.getStartDate().getTime() != edao.getStartDate().getTime() || cdao.getEndDate().getTime() != edao.getEndDate().getTime()) {
                return true;
            }
        }
        return false;
    }
    
    static int resolveFolderIDForUser(final int oid, final int uid, final Context c) throws OXException {
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
    
    static void fillEventInformation(final CalendarDataObject cdao, final CalendarDataObject edao, UserParticipant up_event[], final UserParticipant[] new_userparticipants, final UserParticipant[] deleted_userparticipants, Participant p_event[], final Participant new_participants[], final Participant deleted_participants[]) {
        final Participants pu = new Participants();
        final Participants p = new Participants();
        final UserParticipant oup[] = edao.getUsers();
        final Participant op[] = edao.getParticipants();
        if (oup != null && oup.length > 0) {
	        for (int a = 0; a < oup.length; a++) {
	        	pu.add(oup[a]);
	        }
        }
        if (op != null && op.length > 0) {
	        for (int a = 0; a < op.length; a++) {
	        	p.add(op[a]);
	        }
        }        
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
        up_event = pu.getUsers();
        if (deleted_userparticipants != null && deleted_userparticipants.length > 0) {
            Arrays.sort(up_event);
            for (int a  = 0; a < deleted_userparticipants.length; a++) {
                final int x =  Arrays.binarySearch(up_event, deleted_userparticipants[a]);
                if (x > -1) {
                    final UserParticipant temp[] = new UserParticipant[up_event.length-1];
                    System.arraycopy(up_event, 0, temp, 0, x);
                    System.arraycopy(up_event, x+1, temp, x, ((up_event.length-1)-x));
                    up_event = temp;
                }
            }
        }
        p_event = p.getList();
        if (deleted_participants != null && deleted_participants.length > 0) {
            Arrays.sort(p_event);
            for (int a  = 0; a < deleted_participants.length; a++) {
                final int x =  Arrays.binarySearch(p_event, deleted_participants[a]);
                if (x > -1) {
                    final Participant temp[] = new Participant[p_event.length-1];
                    System.arraycopy(p_event, 0, temp, 0, x);
                    System.arraycopy(p_event, x+1, temp, x, ((p_event.length-1)-x));
                    p_event = temp;
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
    
    public static CalendarDataObject getDAOFromList(final ArrayList al, final int oid) {
        CalendarDataObject cdao = null;
        for (int a = 0; a < al.size(); a++) {
            cdao = (CalendarDataObject) al.get(a);
            if (cdao.getObjectID() == oid) {
                return cdao;
            }
        }
        return null;
    }
    
    static boolean checkForSoloReminderUpdate(final CalendarDataObject cdao, final int[] ucols, final MBoolean cup) {
        if (cup.getMBoolean()) {
            return false;
        } else if (CalendarConfig.getSoloReminderTriggerEvent()) {
            final Set<Integer> IGNORE_FIELDS = new HashSet<Integer>(Arrays.asList(AppointmentObject.ALARM, AppointmentObject.LAST_MODIFIED, AppointmentObject.MODIFIED_BY, 0));
            for (int i = 0; i < ucols.length; i++) {
                final int ucol = ucols[i];
                if(!IGNORE_FIELDS.contains(ucol)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    static void checkAndRemovePastReminders(final CalendarDataObject cdao, final CalendarDataObject edao) {
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

    /**
	 * Adds the time zone offset to given date's time millis and determines
	 * corresponding date based on resulting time millis
	 * 
	 * @param date
	 *            The date whose UTC-based date shall be calculated
	 * @param timezone
	 *            The time zone identifier
	 * @return The UTC-based date
	 */
	static long getUserTimeUTCDate(final Date date, final String timezone) {
		final long ndl = date.getTime() + (Tools.getTimeZone(timezone).getOffset(date.getTime()));
		return ndl - (ndl % CalendarRecurringCollection.MILLI_DAY);
		// System.out.println(" GOT "+date+" and return "+new Date(ndl));
	}

	public static boolean checkIfArrayKeyExistInArray(final Object a[], final Object b[]) {
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

	/**
	 * Checks if specified (exception) date occurs in given recurring appointment
	 * 
	 * @param date The normalized (exception) date
	 * @param recurringAppointment The recurring appointment
	 * @return <code>true</code> if date occurs in recurring appointment; otherwise <code>false</code>
	 * @throws OXException If occurrences cannot be calculated
	 */
	public static boolean checkIfDateOccursInRecurrence(final Date date, final CalendarDataObject recurringAppointment) throws OXException {
		if (date == null) {
			/*
			 * No dates given
			 */
			return true;
		}
		final RecurringResults rresults = CalendarRecurringCollection.calculateRecurring(recurringAppointment, 0, 0, 0, CalendarRecurringCollection.MAXTC, true);
		return (rresults.getPositionByLong(date.getTime()) != -1);
	}

	/**
	 * Checks if specified (exception) dates occur in given recurring appointment
	 * 
	 * @param dates The (exception) dates
	 * @param recurringAppointment The recurring appointment
	 * @return <code>true</code> if every date occurs in recurring appointment; otherwise <code>false</code>
	 * @throws OXException If occurrences cannot be calculated
	 */
	public static boolean checkIfDatesOccurInRecurrence(final Date[] dates, final CalendarDataObject recurringAppointment) throws OXException {
		if (dates == null || dates.length == 0) {
			/*
			 * No dates given
			 */
			return true;
		}
		final RecurringResults rresults = CalendarRecurringCollection.calculateRecurring(recurringAppointment, 0, 0, 0, CalendarRecurringCollection.MAXTC, true);
		boolean result = true;
		for (int i = 0; i < dates.length && result; i++) {
			result = (rresults.getPositionByLong(dates[i].getTime()) != -1);
		}
		return result;
	}

	/**
	 * Merges the specified (exception) dates
	 * 
	 * @param ddates The first dates
	 * @param cdates The second dates
	 * @return The sorted and merged dates
	 */
	public static Date[] mergeExceptionDates(final Date[] ddates, final Date[] cdates) {
		final Set<Date> set;
		{
			int initialCapacity = 0;
			if (ddates != null) {
				initialCapacity += ddates.length;
			}
			if (cdates != null) {
				initialCapacity += cdates.length;
			}
			if (initialCapacity == 0) {
				return new Date[0];
			}
			set = new HashSet<Date>(initialCapacity);
		}
		if (ddates != null) {
			set.addAll(Arrays.asList(ddates));
		}
		if (cdates != null) {
			set.addAll(Arrays.asList(cdates));
		}
		final Date[] merged = set.toArray(new Date[set.size()]);
		Arrays.sort(merged);
		return merged;
	}
	
	public static void checkForInvalidCharacters(final CalendarDataObject cdao) throws OXException {
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
			final UserParticipant up[] = cdao.getUsers();
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

        if(cdao.containsParticipants()) {
            for(final Participant p : cdao.getParticipants()) {
                error = Check.containsInvalidChars(p.getDisplayName());
				if (error != null) {
					throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Display Name", error);
				}
				error = Check.containsInvalidChars(p.getEmailAddress());
				if (error != null) {
					throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Email Address", error);
				}
            }
        }
    }


    public static String getString(final CalendarDataObject cdao, final int fieldID) {
        switch(fieldID) {
            case AppointmentObject.TITLE : return cdao.getTitle();
            case AppointmentObject.LOCATION  : return cdao.getLocation();
            case AppointmentObject.NOTE : return cdao.getNote();
            case AppointmentObject.CATEGORIES : return cdao.getCategories();
            case AppointmentObject.TIMEZONE : return cdao.getTimezone();
            case AppointmentObject.DELETE_EXCEPTIONS : return cdao.getDelExceptions();
            case AppointmentObject.CHANGE_EXCEPTIONS : return cdao.getExceptions();
        }
        return null;
    }

    public static void recoverForInvalidPattern(CalendarDataObject cdao) {
        CalendarCommonCollection.removeRecurringType(cdao);
    }
}
