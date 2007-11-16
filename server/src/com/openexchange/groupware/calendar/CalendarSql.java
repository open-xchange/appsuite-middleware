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

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.*;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * CalendarSql
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarSql implements AppointmentSQLInterface {
    
    public static final String default_class = "com.openexchange.groupware.calendar.CalendarMySQL";
    
    public static final String ERROR_PUSHING_DATABASE = "error pushing readable connection";
    public static final String ERROR_PUSHING_WRITEABLE_CONNECTION = "error pushing writeable connection";
    
    public static final String DATES_TABLE_NAME = "prg_dates";
    public static final String VIEW_TABLE_NAME = "prg_date_rights";
    public static final String PARTICIPANT_TABLE_NAME = "prg_dates_members";
    
    private static CalendarSqlImp cimp;
    private final Session session;
    private UserConfiguration userconfiguration;
    private User user;
    private static final Log LOG = LogFactory.getLog(CalendarSql.class);
    
    public CalendarSql(final Session session) {
        this.session = session;
    }
    
    private final UserConfiguration getUserConfiguration() {
    	if (null == userconfiguration) {
    		userconfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext());
    	}
    	return userconfiguration;
    }
    
    private final User getUser() {
    	if (null == user) {
    		user = UserStorage.getStorageUser(session.getUserId(), session.getContext());
    	}
    	return user;
    }
    
    public boolean[] hasAppointmentsBetween(Date d1, Date d2) throws OXException {
        if (session != null) {
            Connection readcon = null;
            try {
                readcon = DBPool.pickup(session.getContext());
                return cimp.getUserActiveAppointmentsRangeSQL(session.getContext(), session.getUserId(), getUser().getGroups(), getUserConfiguration(), d1, d2, readcon);
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e);
            } finally {
                try {
                    if (readcon != null) {
                        DBPool.push(session.getContext(), readcon);
                    }
                } catch (DBPoolingException dbpe) {
                    LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getAppointmentsBetweenInFolder(int fid, int[] cols, Date start, Date end, int orderBy, String orderDir) throws OXException, SQLException {
        return getAppointmentsBetweenInFolder(fid, cols, start, end, 0, 0, orderBy, orderDir);
    }
    
    public SearchIterator getAppointmentsBetweenInFolder(int fid, int[] cols, Date start, Date end, int from, int to, int orderBy, String orderDir) throws OXException, SQLException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            boolean close_connection = true;
            try {
                readcon = DBPool.pickup(session.getContext());
                cols = CalendarCommonCollection.checkAndAlterCols(cols);
                OXFolderAccess ofa = new OXFolderAccess(readcon, session.getContext());
                if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                    CalendarOperation co = new CalendarOperation();
                    EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), getUserConfiguration());
                    if (oclp.canReadAllObjects()) {
                        prep = cimp.getPrivateFolderRangeSQL(session.getContext(), session.getUserId(), getUser().getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), true, readcon, orderBy, orderDir);
                        rs = cimp.getResultSet(prep);
                        co.setRequestedFolder(fid);
                        co.setResultSet(rs, prep, cols, cimp, readcon, from, to, session);
                        close_connection = false;
                        return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                    } else if (oclp.canReadOwnObjects()) {
                        prep = cimp.getPrivateFolderRangeSQL(session.getContext(), session.getUserId(), getUser().getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), false, readcon, orderBy, orderDir);
                        rs = cimp.getResultSet(prep);
                        co.setRequestedFolder(fid);
                        co.setResultSet(rs, prep, cols, cimp, readcon, from, to, session);
                        close_connection = false;
                        return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                    } else {
                        throw new OXCalendarException(OXCalendarException.Code.NO_PERMISSION);
                    }
                } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                    CalendarOperation co = new CalendarOperation();
                    EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), getUserConfiguration());                    
                    if (oclp.canReadAllObjects()) {
                        prep = cimp.getPublicFolderRangeSQL(session.getContext(), session.getUserId(), getUser().getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), true, readcon, orderBy, orderDir);
                        rs = cimp.getResultSet(prep);
                        co.setRequestedFolder(fid);
                        co.setResultSet(rs, prep, cols, cimp, readcon, from, to, session);
                        close_connection = false;
                        return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                    } else if (oclp.canReadOwnObjects()) {
                        prep = cimp.getPublicFolderRangeSQL(session.getContext(), session.getUserId(), getUser().getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), false, readcon, orderBy, orderDir);
                        rs = cimp.getResultSet(prep);
                        co.setRequestedFolder(fid);
                        co.setResultSet(rs, prep, cols, cimp, readcon, from, to, session);
                        close_connection = false;
                        return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                    } else {
                        throw new OXCalendarException(OXCalendarException.Code.NO_PERMISSION);
                    }
                } else {
                    CalendarOperation co = new CalendarOperation();
                    EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), getUserConfiguration());
                    int shared_folder_owner = ofa.getFolderOwner(fid);
                    if (oclp.canReadAllObjects()) {
                        prep = cimp.getSharedFolderRangeSQL(session.getContext(), session.getUserId(), shared_folder_owner, getUser().getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), true, readcon, orderBy, orderDir);
                        rs = cimp.getResultSet(prep);
                        co.setRequestedFolder(fid);
                        co.setResultSet(rs, prep,cols, cimp, readcon, from, to, session);
                        close_connection = false;
                        return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                    } else if (oclp.canReadOwnObjects()) {
                        prep = cimp.getSharedFolderRangeSQL(session.getContext(), session.getUserId(), shared_folder_owner, getUser().getGroups(), fid, start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), false, readcon, orderBy, orderDir);
                        rs = cimp.getResultSet(prep);
                        co.setRequestedFolder(fid);
                        co.setResultSet(rs, prep, cols, cimp, readcon, from, to, session);
                        close_connection = false;
                        return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                    } else {
                        throw new OXCalendarException(OXCalendarException.Code.NO_PERMISSION);
                    }
                }
            } catch (IndexOutOfBoundsException ioobe) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, ioobe, 19);
            } catch (OXPermissionException oxpe) {
                throw oxpe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch (Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 20);
            } finally  {
                if (close_connection) {
                    CalendarCommonCollection.closeResultSet(rs);
                    CalendarCommonCollection.closePreparedStatement(prep);
                }
                if (readcon != null && close_connection) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getModifiedAppointmentsInFolder(int fid, Date start, Date end, int[] cols, Date since) throws OXException, SQLException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            boolean close_connection = true;
            try {
                readcon = DBPool.pickup(session.getContext());
                cols = CalendarCommonCollection.checkAndAlterCols(cols);
                OXFolderAccess ofa = new OXFolderAccess(readcon, session.getContext());
                if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                    CalendarOperation co = new CalendarOperation();
                    EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), getUserConfiguration());
                    prep = cimp.getPrivateFolderModifiedSinceSQL(session.getContext(), session.getUserId(), getUser().getGroups(), fid, since, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, start, end);
                    rs = cimp.getResultSet(prep);
                    co.setRequestedFolder(fid);
                    co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                    close_connection = false;
                    return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                    CalendarOperation co = new CalendarOperation();
                    EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), getUserConfiguration());
                    prep = cimp.getPublicFolderModifiedSinceSQL(session.getContext(), session.getUserId(), getUser().getGroups(), fid, since, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, start, end);
                    rs = cimp.getResultSet(prep);
                    co.setRequestedFolder(fid);
                    co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                    close_connection = false;
                    return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                } else {
                    CalendarOperation co = new CalendarOperation();
                    EffectivePermission oclp = ofa.getFolderPermission(fid, session.getUserId(), getUserConfiguration());
                    int shared_folder_owner = ofa.getFolderOwner(fid);
                    prep = cimp.getSharedFolderModifiedSinceSQL(session.getContext(), session.getUserId(), shared_folder_owner, getUser().getGroups(), fid, since, StringCollection.getSelect(cols, DATES_TABLE_NAME), oclp.canReadAllObjects(), readcon, start, end);
                    rs = cimp.getResultSet(prep);
                    co.setRequestedFolder(fid);
                    co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                    close_connection = false;
                    return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                }
            } catch (IndexOutOfBoundsException ioobe) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, ioobe, 21);
            } catch (OXPermissionException oxpe) {
                throw oxpe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch (Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 22);
            } finally {
                if (close_connection) {
                    CalendarCommonCollection.closeResultSet(rs);
                    CalendarCommonCollection.closePreparedStatement(prep);
                }
                if (readcon != null && close_connection) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getModifiedAppointmentsInFolder(int fid, int cols[], Date since) throws OXException, SQLException {
        return getModifiedAppointmentsInFolder(fid, null, null, cols, since);
    }
    
    public SearchIterator getDeletedAppointmentsInFolder(int fid, int cols[], Date since) throws OXException, SQLException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            boolean close_connection = true;
            try {
                readcon = DBPool.pickup(session.getContext());
                cols = CalendarCommonCollection.checkAndAlterCols(cols);
                OXFolderAccess ofa = new OXFolderAccess(readcon, session.getContext());
                if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                    CalendarOperation co = new CalendarOperation();
                    prep = cimp.getPrivateFolderDeletedSinceSQL(session.getContext(), session.getUserId(), fid, since, StringCollection.getSelect(cols, "del_dates"), readcon);
                    rs = cimp.getResultSet(prep);
                    co.setRequestedFolder(fid);
                    co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                    close_connection = false;
                    return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                    CalendarOperation co = new CalendarOperation();
                    prep = cimp.getPublicFolderDeletedSinceSQL(session.getContext(), session.getUserId(), fid, since, StringCollection.getSelect(cols, "del_dates"), readcon);
                    rs = cimp.getResultSet(prep);
                    co.setRequestedFolder(fid);
                    co.setResultSet(rs, prep,cols, cimp, readcon, 0, 0, session);
                    close_connection = false;
                    return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                } else {
                    CalendarOperation co = new CalendarOperation();
                    int shared_folder_owner = ofa.getFolderOwner(fid);
                    prep = cimp.getSharedFolderDeletedSinceSQL(session.getContext(), session.getUserId(), shared_folder_owner, fid, since, StringCollection.getSelect(cols, "del_dates"), readcon);
                    rs = cimp.getResultSet(prep);
                    co.setRequestedFolder(fid);
                    co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                    close_connection = false;
                    return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
                }
            } catch (IndexOutOfBoundsException ioobe) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, ioobe, 23);
            } catch (OXPermissionException oxpe) {
                throw oxpe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch (Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 24);
            } finally {
                if (close_connection) {
                    CalendarCommonCollection.closeResultSet(rs);
                    CalendarCommonCollection.closePreparedStatement(prep);
                }
                if (readcon != null && close_connection) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public CalendarDataObject getObjectById(int oid, int inFolder) throws OXException, SQLException, OXObjectNotFoundException, OXPermissionException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try {
                readcon = DBPool.pickup(session.getContext());
                CalendarOperation co = new CalendarOperation();
                prep = cimp.getPreparedStatement(readcon, cimp.loadAppointment(oid, session.getContext()));
                rs = cimp.getResultSet(prep);
                CalendarDataObject cdao = co.loadAppointment(rs, oid, inFolder, cimp, readcon, session, CalendarOperation.READ, inFolder);
                if (cdao.getRecurrenceType() != AppointmentObject.NO_RECURRENCE) {
                    RecurringResults rrs = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1, 999, true);
                    RecurringResult rr = rrs.getRecurringResultByPosition(1);
                    if (rr != null) {
                        cdao.setStartDate(new Date(rr.getStart()));
                        cdao.setEndDate(new Date(rr.getEnd()));
                    }
                }
                return cdao;
            } catch (OXPermissionException oxpe) {
                throw oxpe;
            } catch(OXObjectNotFoundException oxonfe) {
                throw oxonfe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } finally {
                CalendarCommonCollection.closeResultSet(rs);
                CalendarCommonCollection.closePreparedStatement(prep);
                if (readcon != null) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public CalendarDataObject[] insertAppointmentObject(CalendarDataObject cdao) throws OXException, OXPermissionException {
        if (session != null) {
            Connection writecon = null;
            try {
                CalendarOperation co = new CalendarOperation();
                if (co.prepareUpdateAction(cdao, null, session.getUserId(), cdao.getParentFolderID(), getUser().getTimeZone())) {
                    try {
                        OXFolderAccess ofa = new OXFolderAccess(session.getContext());
                        EffectivePermission oclp = ofa.getFolderPermission(cdao.getEffectiveFolderId(), session.getUserId(), getUserConfiguration());
                        if (oclp.canCreateObjects()) {
                        	CalendarCommonCollection.checkForInvalidCharacters(cdao);
                            cdao.setActionFolder(cdao.getParentFolderID());
                            ConflictHandler ch = new ConflictHandler(cdao, session, true);
                            CalendarDataObject conflicts[] = ch.getConflicts();
                            if (conflicts.length == 0) {
                                writecon = DBPool.pickupWriteable(session.getContext());
                                writecon.setAutoCommit(false);
                                return cimp.insertAppointment(cdao, writecon, session);
                            } else {
                                return conflicts;
                            }
                        } else {
                            throw new OXPermissionException(new OXCalendarException(OXCalendarException.Code.LOAD_PERMISSION_EXCEPTION_6));
                        }
                    } catch(DataTruncation dt) {
                        String fields[] = DBUtils.parseTruncatedFields(dt);
                        int fid[] = new int[fields.length];
                        OXException oxe = new OXException(new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR));
                        oxe.setCategory(AbstractOXException.Category.TRUNCATED);
                        int id = -1;
                        for (int a = 0; a < fid.length; a++) {
                            id = CalendarCommonCollection.getFieldId(fields[a]);
                            oxe.addTruncatedId(id);
                        }
                        throw oxe;
                    } catch(SQLException sqle) {
                        try {
                            if (!writecon.getAutoCommit()) {
                                writecon.rollback();
                            }
                        } catch(SQLException rb) {
                            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, rb);
                        }
                        throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
                    } finally {
                        if (writecon != null) {
                            writecon.setAutoCommit(true);
                        }
                    }
                } else {
                    throw new OXCalendarException(OXCalendarException.Code.INSERT_WITH_OBJECT_ID);
                }
            } catch(DataTruncation dt) {
                String fields[] = DBUtils.parseTruncatedFields(dt);
                int fid[] = new int[fields.length];
                OXException oxe = new OXException(new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR));
                oxe.setCategory(AbstractOXException.Category.TRUNCATED);
                int id = -1;
                for (int a = 0; a < fid.length; a++) {
                    id = CalendarCommonCollection.getFieldId(fields[a]);
                    oxe.addTruncatedId(id);
                }
                throw oxe;
            } catch (SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch (OXPermissionException oxpe) {
                throw oxpe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 25);
            } finally {
                if (writecon != null) {
                    try {
                        DBPool.pushWrite(session.getContext(), writecon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_WRITEABLE_CONNECTION, dbpe);
                    }
                }
            }
            
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public CalendarDataObject[] updateAppointmentObject(CalendarDataObject cdao, int inFolder, Date clientLastModified) throws OXException, OXPermissionException, OXConcurrentModificationException, OXObjectNotFoundException {
        if (session != null) {
            Connection writecon = null;
            try {
                CalendarOperation co = new CalendarOperation();
                CalendarDataObject edao = cimp.loadObjectForUpdate(cdao, session, inFolder);
                if (!co.prepareUpdateAction(cdao, edao, session.getUserId(), inFolder, getUser().getTimeZone())) {
                	CalendarCommonCollection.checkForInvalidCharacters(cdao);
                    CalendarDataObject conflict_dao = CalendarCommonCollection.fillFieldsForConflictQuery(cdao, edao, false);
                    ConflictHandler ch = new ConflictHandler(conflict_dao, session, false);
                    CalendarDataObject conflicts[] = ch.getConflicts();
                    if (conflicts.length == 0) {
                        writecon = DBPool.pickupWriteable(session.getContext());
                        try {
                            writecon.setAutoCommit(false);
                            if (cdao.containsParentFolderID()) {
                                cdao.setActionFolder(cdao.getParentFolderID());
                            } else {
                                cdao.setActionFolder(inFolder);
                            }
                            return cimp.updateAppointment(cdao, edao, writecon, session, inFolder, clientLastModified);
                        } catch(DataTruncation dt) {
                            String fields[] = DBUtils.parseTruncatedFields(dt);
                            int fid[] = new int[fields.length];
                            OXException oxe = new OXException(new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR));
                            oxe.setCategory(AbstractOXException.Category.TRUNCATED);
                            int id = -1;
                            for (int a = 0; a < fid.length; a++) {
                                id = CalendarCommonCollection.getFieldId(fields[a]);
                                oxe.addTruncatedId(id);
                            }
                            throw oxe;
                        } catch(SQLException sqle) {
                            try {
                                if (writecon != null) {
                                    writecon.rollback();
                                }
                            } catch(SQLException rb) {
                                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, rb);
                            }
                            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
                        } finally {
                            if (writecon != null) {
                                writecon.setAutoCommit(true);
                            }
                        }
                    } else {
                        return conflicts;
                    }
                } else {
                    throw new OXCalendarException(OXCalendarException.Code.UPDATE_WITHOUT_OBJECT_ID);
                }
            } catch(DataTruncation dt) {
                String fields[] = DBUtils.parseTruncatedFields(dt);
                int fid[] = new int[fields.length];
                OXException oxe = new OXException(new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR));
                oxe.setCategory(AbstractOXException.Category.TRUNCATED);
                int id = -1;
                for (int a = 0; a < fid.length; a++) {
                    id = CalendarCommonCollection.getFieldId(fields[a]);
                    oxe.addTruncatedId(id);
                }
                throw oxe;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch(OXObjectNotFoundException oxonfe) {
                throw oxonfe;
            } catch(OXCalendarException oxce) {
                throw oxce;
            } catch(OXException oxe) {
                throw oxe;
            } catch (Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 26);
            } finally {
                if (writecon != null) {
                    try {
                        DBPool.pushWrite(session.getContext(), writecon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_WRITEABLE_CONNECTION, dbpe);
                    }
                }
            }
            
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public void deleteAppointmentObject(CalendarDataObject cdao, int inFolder, Date clientLastModified) throws OXException, SQLException, OXPermissionException, OXConcurrentModificationException {
        if (session != null) {
            Connection writecon = null;
            try  {
                writecon = DBPool.pickupWriteable(session.getContext());
                try {
                    writecon.setAutoCommit(false);
                    cimp.deleteAppointment(session.getUserId(), cdao, writecon, session, inFolder, clientLastModified);
                } catch(SQLException sqle) {
                    try {
                        writecon.rollback();
                    } catch(SQLException rb) {
                        throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, rb);
                    }
                    throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
                } finally {
                    try {
                        writecon.setAutoCommit(true);
                    } catch(SQLException ac) {
                        throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, ac);
                    }
                }
            } catch(OXConcurrentModificationException oxcme) {
                throw oxcme;
            } catch(OXPermissionException oxpe) {
                throw oxpe;
            } catch(OXObjectNotFoundException oxonfe) {
                throw oxonfe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 28);
            } finally {
                if (writecon != null) {
                    try {
                        DBPool.pushWrite(session.getContext(), writecon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_WRITEABLE_CONNECTION,  dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public void deleteAppointmentsInFolder(int fid) throws OXException, SQLException, OXPermissionException {
        if (session != null) {
            Connection readcon = null, writecon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try  {
                readcon = DBPool.pickup(session.getContext());
                try {
                    OXFolderAccess ofa = new OXFolderAccess(readcon, session.getContext());
                    if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                        prep = cimp.getPrivateFolderObjects(fid, session.getContext(), readcon);
                        rs = cimp.getResultSet(prep);
                        writecon = DBPool.pickupWriteable(session.getContext());
                        writecon.setAutoCommit(false);
                        cimp.deleteAppointmentsInFolder(session, rs, readcon, writecon, FolderObject.PRIVATE, fid);
                    } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                        prep = cimp.getPublicFolderObjects(fid, session.getContext(), readcon);
                        rs = cimp.getResultSet(prep);
                        writecon = DBPool.pickupWriteable(session.getContext());
                        writecon.setAutoCommit(false);
                        cimp.deleteAppointmentsInFolder(session, rs, readcon, writecon, FolderObject.PUBLIC, fid);
                    } else {
                        throw new OXCalendarException(OXCalendarException.Code.FOLDER_DELETE_INVALID_REQUEST);
                    }
                } catch(SQLException sqle) {
                    try {
                        writecon.rollback();
                    } catch(SQLException rb) {
                        throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, rb);
                    }
                    throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
                } finally {
                    try {
                        writecon.setAutoCommit(true);
                    } catch(SQLException ac) {
                        throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, ac);
                    }
                }
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXPermissionException oxpe) {
                throw oxpe;
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 29);
            } finally {
                CalendarCommonCollection.closeResultSet(rs);
                CalendarCommonCollection.closePreparedStatement(prep);
                if (readcon != null) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                    }
                }
                if (writecon != null) {
                    try {
                        DBPool.pushWrite(session.getContext(), writecon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_WRITEABLE_CONNECTION,  dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public boolean checkIfFolderContainsForeignObjects(int uid, int fid) throws OXException, SQLException {
        if (session != null) {
            Connection readcon = null;
            try {
                readcon = DBPool.pickup(session.getContext());
                OXFolderAccess ofa = new OXFolderAccess(readcon, session.getContext());
                if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                    return cimp.checkIfFolderContainsForeignObjects(uid, fid, session.getContext(), readcon, FolderObject.PRIVATE);
                } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) {
                    return cimp.checkIfFolderContainsForeignObjects(uid, fid, session.getContext(), readcon, FolderObject.PUBLIC);
                } else {
                    throw new OXCalendarException(OXCalendarException.Code.FOLDER_FOREIGN_INVALID_REQUEST);
                }
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 30);
            } finally {
                if (readcon != null) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public boolean isFolderEmpty(int uid, int fid) throws OXException, SQLException {
        if (session != null) {
            Connection readcon = null;
            try {
                readcon = DBPool.pickup(session.getContext());
                OXFolderAccess ofa = new OXFolderAccess(readcon, session.getContext());
                if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PRIVATE) {
                    return cimp.checkIfFolderIsEmpty(uid, fid, session.getContext(), readcon, FolderObject.PRIVATE);
                } else if (ofa.getFolderType(fid, session.getUserId()) == FolderObject.PUBLIC) { 
                    return cimp.checkIfFolderIsEmpty(uid, fid, session.getContext(), readcon, FolderObject.PUBLIC);
                } else {
                    throw new OXCalendarException(OXCalendarException.Code.FOLDER_IS_EMPTY_INVALID_REQUEST);
                }
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 31);
            } finally {
                if (readcon != null) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE, dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    
    public void setUserConfirmation(int oid, int uid, int confirm, String confirm_message) throws OXException {
        if (session != null) {
        	if (confirm_message != null) {
        		String error = null;
        		error = Check.containsInvalidChars(confirm_message);
        		if (error != null) {
        			throw new OXCalendarException(OXCalendarException.Code.INVALID_CHARACTER, "Confirm Message", error);
        		}
        	}
            cimp.setUserConfirmation(oid, uid, confirm, confirm_message, session);
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getObjectsById(int[][] oids, int[] cols) throws OXException {
        if (session != null) {
            if (oids.length > 0) {
                Connection readcon = null;
                PreparedStatement prep = null;
                ResultSet rs = null;
                boolean close_connection = true;
                try {
                    readcon = DBPool.pickup(session.getContext());
                    cols = CalendarCommonCollection.checkAndAlterCols(cols);
                    CalendarOperation co = new CalendarOperation();
                    prep = cimp.getPreparedStatement(readcon, cimp.getObjectsByidSQL(oids, session.getContext().getContextId(), StringCollection.getSelect(cols, DATES_TABLE_NAME)));
                    rs = cimp.getResultSet(prep);
                    co.setOIDS(true, oids);
                    co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                    close_connection = false;
                    return new CachedCalendarIterator(co, session.getContext(), session.getUserId(), oids);
                } catch(SQLException sqle) {
                    throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
                } catch(DBPoolingException dbpe) {
                    throw new OXException(dbpe);
                } catch(OXObjectNotFoundException oxonfe) {
                    throw oxonfe;
                } catch(OXCalendarException oxc) {
                    throw oxc;
                } catch(OXException oxe) {
                    throw oxe;
                } catch(Exception e) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 32);
                } finally {
                    if (readcon != null && close_connection) {
                        CalendarCommonCollection.closeResultSet(rs);
                        CalendarCommonCollection.closePreparedStatement(prep);
                        try {
                            DBPool.push(session.getContext(), readcon);
                        } catch (DBPoolingException dbpe) {
                            LOG.error(ERROR_PUSHING_DATABASE ,dbpe);
                        }
                    }
                }
            } else {
                return new CalendarOperation();
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getAppointmentsByExtendedSearch(AppointmentSearchObject searchobject, int orderBy, String orderDir, int cols[]) throws OXException, SQLException {
        return getAppointmentsByExtendedSearch(searchobject, orderBy, orderDir, cols, 0, 0);
    }
    
    public SearchIterator getAppointmentsByExtendedSearch(AppointmentSearchObject searchobject, int orderBy, String orderDir, int cols[], int from, int to) throws OXException, SQLException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            boolean close_connection = true;
            try {
                CalendarOperation co = new CalendarOperation();
                if (searchobject.getFolder() > 0) {
                    co.setRequestedFolder(searchobject.getFolder());
                } else {
                    int ara[] = new int[1];
                    ara[0] = AppointmentObject.PARTICIPANTS;
                    cols = CalendarCommonCollection.enhanceCols(cols, ara, 1);
                }
                cols = CalendarCommonCollection.checkAndAlterCols(cols);
                CalendarFolderObject cfo = null;
                try {
                    cfo = CalendarCommonCollection.getVisibleAndReadableFolderObject(session.getUserId(), getUser().getGroups(), session.getContext(), getUserConfiguration(), readcon);
                } catch (DBPoolingException dbpe) {
                    throw new OXException(dbpe);
                } catch (SearchIteratorException sie) {
                    throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, sie, 1);
                }
                readcon = DBPool.pickup(session.getContext());
                prep = cimp.getSearchQuery(StringCollection.getSelect(cols, DATES_TABLE_NAME), session.getUserId(), getUser().getGroups(), getUserConfiguration(), orderBy, orderDir, searchobject, session.getContext(), readcon, cfo);
                rs = cimp.getResultSet(prep);
                co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                close_connection = false;
                return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch(OXObjectNotFoundException oxonfe) {
                throw oxonfe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 33);
            } finally {
                if (close_connection) {
                    CalendarCommonCollection.closeResultSet(rs);
                    CalendarCommonCollection.closePreparedStatement(prep);
                }
                if (readcon != null && close_connection) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE ,dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator searchAppointments(String searchpattern, int fid, int orderBy, String orderDir, int[] cols) throws OXException {
        AppointmentSearchObject searchobject = new AppointmentSearchObject();
        searchobject.setPattern(searchpattern);
        searchobject.setFolder(fid);
        try {
            return getAppointmentsByExtendedSearch(searchobject, orderBy, orderDir, cols);
        } catch (SQLException sqle) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
        }
    }
    
    public final long attachmentAction(int oid, int uid, Context c, boolean action) throws OXException {
        return cimp.attachmentAction(oid, uid, c, action);
    }
    
    public SearchIterator getFreeBusyInformation(int uid, int type, Date start, Date end) throws OXException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            boolean close_connection = true;
            try {
                if (!getUserConfiguration().hasFreeBusy()) {
                    return new CalendarOperation();
                }
                readcon = DBPool.pickup(session.getContext());
                switch(type) {
                    case Participant.USER:
                        prep = cimp.getFreeBusy(uid, session.getContext(), start, end, readcon);
                        break;
                    case Participant.RESOURCE:
                        prep = cimp.getResourceFreeBusy(uid, session.getContext(), start, end, readcon);
                        break;
                    default:
                        throw new OXCalendarException(OXCalendarException.Code.FREE_BUSY_UNSUPPOTED_TYPE, type);
                }
                rs = cimp.getResultSet(prep);
                SearchIterator si = new FreeBusyResults(rs, prep, session.getContext(), readcon, start.getTime(), end.getTime());
                close_connection = false;
                return si;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch(OXObjectNotFoundException oxonfe) {
                throw oxonfe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 34);
            } finally {
                if (close_connection) {
                    CalendarCommonCollection.closeResultSet(rs);
                    CalendarCommonCollection.closePreparedStatement(prep);
                }
                if (readcon != null && close_connection) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE ,dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getActiveAppointments(int user_uid, Date start, Date end, int cols[]) throws OXException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            boolean close_connection = true;
            try {
                readcon = DBPool.pickup(session.getContext());
                cols = CalendarCommonCollection.checkAndAlterCols(cols);
                CalendarOperation co = new CalendarOperation();
                prep = cimp.getActiveAppointments(session.getContext(), session.getUserId(), start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), readcon);
                ResultSet rs = cimp.getResultSet(prep);
                co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                close_connection = false;
                return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch(OXObjectNotFoundException oxonfe) {
                throw oxonfe;
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 35);
            } finally {
                if (readcon != null && close_connection) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE ,dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getModifiedAppointmentsBetween(int userId, Date start, Date end, int[] cols, Date since, int orderBy, String orderDir) throws OXException, SQLException {
        if (session != null) {
            Connection readcon = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            boolean close_connection = true;
            try {
                readcon = DBPool.pickup(session.getContext());
                cols = CalendarCommonCollection.checkAndAlterCols(cols);
                CalendarOperation co = new CalendarOperation();
                prep = cimp.getAllAppointmentsForUser(session.getContext(), session.getUserId(), getUser().getGroups(), getUserConfiguration(), start, end, StringCollection.getSelect(cols, DATES_TABLE_NAME), readcon, since, orderBy, orderDir);
                rs = cimp.getResultSet(prep);
                co.setResultSet(rs, prep, cols, cimp, readcon, 0, 0, session);
                close_connection = false;
                return new CachedCalendarIterator(co, session.getContext(), session.getUserId());
            } catch(OXPermissionException oxpe) {
                throw oxpe;
            } catch(SQLException sqle) {
                throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, sqle);
            } catch(DBPoolingException dbpe) {
                throw new OXException(dbpe);
            } catch(OXCalendarException oxc) {
                throw oxc;
            } catch(OXException oxe) {
                throw oxe;
            } catch(Exception e) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 36);
            } finally {
                if (close_connection) {
                    CalendarCommonCollection.closeResultSet(rs);
                    CalendarCommonCollection.closePreparedStatement(prep);
                }
                if (readcon != null && close_connection) {
                    try {
                        DBPool.push(session.getContext(), readcon);
                    } catch (DBPoolingException dbpe) {
                        LOG.error(ERROR_PUSHING_DATABASE ,dbpe);
                    }
                }
            }
        } else {
            throw new OXCalendarException(OXCalendarException.Code.ERROR_SESSIONOBJECT_IS_NULL);
        }
    }
    
    public SearchIterator getAppointmentsBetween(int user_uid, Date start, Date end, int cols[], int orderBy, String orderDir) throws OXException, SQLException {
        return getModifiedAppointmentsBetween(user_uid, start, end, cols, null, orderBy, orderDir);
    }
    
    public static final CalendarSqlImp getCalendarSqlImplementation() {
        if (cimp != null){
            return cimp;
        } else {
            LOG.error("No CalendarSqlImp Class found !");
            try {
                cimp = (CalendarSqlImp) Class.forName(default_class).newInstance();
                return cimp;
            } catch(ClassNotFoundException cnfe) {
                LOG.error(cnfe.getMessage(), cnfe);
            } catch (IllegalAccessException iae) {
                LOG.error(iae.getMessage(), iae);
            } catch (InstantiationException ie) {
                LOG.error(ie.getMessage(), ie);
            }
            return null;
        }
    }
    
    static {
        try {
            if (cimp == null) {
                CalendarConfig.init();
                String classname = CalendarConfig.getProperty("CalendarSQL");
                if (classname == null) {
                    classname = default_class;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using "+classname+" in CalendarSql");
                }
                cimp = (CalendarSqlImp) Class.forName(classname).newInstance();
            }
        } catch(ConfigurationException ce) {
            LOG.error(ce.getMessage(), ce);
        } catch(ClassNotFoundException cnfe) {
            LOG.error(cnfe.getMessage(), cnfe);
        } catch (IllegalAccessException iae) {
            LOG.error(iae.getMessage(), iae);
        } catch (InstantiationException ie) {
            LOG.error(ie.getMessage(), ie);
        }
    }
    
    
}



