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
import java.util.ArrayList;
import java.util.Date;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.sessiond.Session;


/**
 * CalendarSqlImp
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public interface CalendarSqlImp {
    
    public PreparedStatement getAllAppointmentsForUser(Context c, int uid, int groups[], UserConfiguration uc, java.util.Date d1, java.util.Date d2, String select, Connection readcon, Date since, int orderBy, String orderDir) throws OXException, SQLException;
    
    public PreparedStatement getConflicts(Context c, java.util.Date d1, java.util.Date d2, java.util.Date d3, java.util.Date d4, Connection readcon, String member_sql_in, boolean free_busy_select) throws SQLException;
    
    public PreparedStatement getResourceConflicts(Context c, java.util.Date d1, java.util.Date d2, java.util.Date d3, java.util.Date d4, Connection readcon, String resource_sql_in) throws SQLException;
    
    public PreparedStatement getResourceConflictsPrivateFolderInformation(Context c, java.util.Date d1, java.util.Date d2, java.util.Date d3, java.util.Date d4, Connection readcon, String resource_sql_in) throws SQLException;
    
    public PreparedStatement getFreeBusy(int uid, Context c, Date d1, Date d2, Connection readcon) throws SQLException ;
    
    public PreparedStatement getResourceFreeBusy(int uid, Context c, Date d1, Date d2, Connection readcon) throws SQLException ;
    
    public boolean[] getUserActiveAppointmentsRangeSQL(Context c, int uid, int groups[], UserConfiguration uc, Date d1, Date d2, Connection readcon) throws SQLException, OXException;
    
    public PreparedStatement getPublicFolderRangeSQL(Context c, int uid, int groups[], int fid, Date d1, Date d2, String select, boolean readall, Connection con, int orderBy, String orderDir) throws SQLException;
    
    public String getObjectsByidSQL(int oids[][], int cid, String select);
    
    public PreparedStatement getPrivateFolderRangeSQL(Context c, int uid, int groups[], int fid, Date d1, Date d2, String select, boolean readall, Connection readcon, int orderBy, String orderDir) throws SQLException;
    
    public PreparedStatement getPrivateFolderModifiedSinceSQL(Context c, int uid, int groups[], int fid, Date since, String select, boolean readall, Connection readcon, Date d1, Date d2) throws SQLException;
    
    public PreparedStatement getPrivateFolderObjects(int fid, Context c, Connection readcon) throws SQLException ;
    
    public PreparedStatement getPublicFolderModifiedSinceSQL(Context c, int uid, int groups[], int fid, Date since, String select, boolean readall, Connection readcon, Date d1, Date d2) throws SQLException;
    
    public PreparedStatement getPrivateFolderDeletedSinceSQL(Context c, int uid, int fid, Date d1, String select, Connection readcon) throws SQLException;
    
    public PreparedStatement getPublicFolderDeletedSinceSQL(Context c, int uid, int fid, Date d1, String select, Connection readcon) throws SQLException;
    
    public PreparedStatement getPublicFolderObjects(int fid, Context c, Connection readcon) throws SQLException ;
    
    public String loadAppointment(int oid, Context c) throws SQLException;
    
    public ResultSet getResultSet(PreparedStatement pst) throws SQLException;
    
    public PreparedStatement getPreparedStatement(Connection readcon, String sql) throws SQLException;
    
    public Participants getUserParticipants(CalendarDataObject cdao, Connection readcon, int uid) throws SQLException, OXException;
    
    public void getUserParticipantsSQLIn(ArrayList al, Connection readcon, int cid, int uid, String sqlin) throws SQLException, OXException;
    
    public void getParticipantsSQLIn(final ArrayList al, final Connection readcon, final int cid, final String sqlin) throws SQLException;
    
    public Participants getParticipants(CalendarDataObject cdao, Connection readcon) throws SQLException;
    
    public CalendarDataObject[] insertAppointment(CalendarDataObject cdao, Connection writecon, Session so) throws SQLException, LdapException, Exception;
    
    public CalendarDataObject[] updateAppointment(CalendarDataObject cdao, CalendarDataObject edao, Connection writecon, Session so, int inFolder, Date clientLastModified) throws SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException;
    
    public CalendarDataObject loadObjectForUpdate(CalendarDataObject cdao, Session so, int inFolder) throws SQLException, LdapException, OXObjectNotFoundException, OXPermissionException, OXException;
    
    public void deleteAppointment(int uid, CalendarDataObject cdao, Connection writecon, Session so, int inFolder, Date clientLastModified) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException;
    
    public void deleteAppointmentsInFolder(Session so, ResultSet objects, Connection readcon, Connection writecon, int foldertype, int fid) throws SQLException, OXObjectNotFoundException, OXPermissionException, OXException;
    
    public void setUserConfirmation(int oid, int uid, int confirm, String confirm_message, Session so) throws OXException;
    
    public boolean checkIfFolderContainsForeignObjects(int uid, int fid, Context c, Connection readcon, int foldertype) throws SQLException;
    
    public boolean checkIfFolderIsEmpty(int uid, int fid, Context c, Connection readcon, int foldertype) throws SQLException;
    
    public PreparedStatement getSharedFolderRangeSQL(Context c, int uid, int shared_folder_owner, int groups[], int fid, Date d1, Date d2, String select, boolean readall, Connection readcon, int orderBy, String orderDir) throws SQLException;
    
    public PreparedStatement getSharedFolderModifiedSinceSQL(Context c, int uid, int shared_folder_owner, int groups[], int fid, Date since, String select, boolean readall, Connection readcon, Date d1, Date d2) throws SQLException;
    
    public PreparedStatement getSharedFolderDeletedSinceSQL(Context c, int uid, int shared_folder_owner, int fid, Date d1, String select, Connection readcon) throws SQLException;
    
    public long attachmentAction(int oid, int uid, Context c, boolean action) throws OXException;
    
    public PreparedStatement getSearchQuery(String select,  int uid, int groups[], UserConfiguration uc, int orderBy, String orderDir, AppointmentSearchObject searchobject, Context c, Connection readcon, CalendarFolderObject cfo) throws SQLException, OXException;
    
    public PreparedStatement getActiveAppointments(Context c, int uid, java.util.Date d1, java.util.Date d2, String select, Connection readcon) throws SQLException;
    
}
