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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link CalendarSqlImp} - The calendar SQL interface
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public interface CalendarSqlImp {

    PreparedStatement getAllAppointments(Context c, Date d1, Date d2, String select, Connection readcon, int orderBy, Order orderDir) throws OXException, SQLException;

    PreparedStatement getAllAppointmentsForUser(Context c, int uid, int groups[], UserConfiguration uc, Date d1, Date d2, String select, Connection readcon, Date since, int orderBy, Order orderDir) throws OXException, SQLException;

    PreparedStatement getConflicts(Context c, Date d1, Date d2, Date d3, Date d4, Connection readcon, String member_sql_in, boolean free_busy_select) throws SQLException;

    SearchIterator<List<Integer>> getAllPrivateAppointmentAndFolderIdsForUser(Context c, int id, Connection readcon) throws SQLException;

    PreparedStatement getSharedAppointmentFolderQuery(Context c, int id, CalendarFolderObject cfo, Connection con) throws SQLException;

    PreparedStatement getResourceConflicts(Context c, Date d1, Date d2, Date d3, Date d4, Connection readcon, String resource_sql_in) throws SQLException;

    SearchIterator<List<Integer>> getResourceConflictsPrivateFolderInformation(Context c, Date d1, Date d2, Date d3, Date d4, Connection readcon, String resource_sql_in) throws SQLException;

    PreparedStatement getFreeBusy(int uid, Context c, Date d1, Date d2, Connection readcon) throws SQLException ;

    PreparedStatement getResourceFreeBusy(int uid, Context c, Date d1, Date d2, Connection readcon) throws SQLException ;

    boolean[] getUserActiveAppointmentsRangeSQL(Context c, int uid, int groups[], UserConfiguration uc, Date d1, Date d2, Connection readcon) throws SQLException, OXException;

    PreparedStatement getPublicFolderRangeSQL(Context c, int uid, int groups[], int fid, Date d1, Date d2, String select, boolean readall, Connection con, int orderBy, Order orderDir) throws SQLException;

    String getObjectsByidSQL(int oids[][], int cid, String select);

    PreparedStatement getPrivateFolderRangeSQL(Context c, int uid, int groups[], int fid, Date d1, Date d2, String select, boolean readall, Connection readcon, int orderBy, Order orderDir) throws SQLException;

    PreparedStatement getPrivateFolderModifiedSinceSQL(Context c, int uid, int groups[], int fid, Date since, String select, boolean readall, Connection readcon, Date d1, Date d2) throws SQLException;

    PreparedStatement getPrivateFolderObjects(int fid, Context c, Connection readcon) throws SQLException ;

    PreparedStatement getPublicFolderModifiedSinceSQL(Context c, int uid, int groups[], int fid, Date since, String select, boolean readall, Connection readcon, Date d1, Date d2) throws SQLException;

    PreparedStatement getPrivateFolderDeletedSinceSQL(Context c, int uid, int fid, Date d1, String select, Connection readcon) throws SQLException;

    PreparedStatement getPublicFolderDeletedSinceSQL(Context c, int uid, int fid, Date d1, String select, Connection readcon) throws SQLException;

    /**
     * Constructs a prepared statement to select the sequence number of a private or shared folder, which is evaluated by determining the
     * biggest changing date of all contained appointments, considering both the "working" as well as the "backup" tables.
     *
     * @param cid The context identifier
     * @param uid The folder owner's user identifier
     * @param fid The folder identifier
     * @param readcon The connection for creating the prepared statement
     * @return The prepared statement
     */
    PreparedStatement getPrivateFolderSequenceNumber(int cid, int uid, int fid, Connection readcon) throws SQLException;

    /**
     * Constructs a prepared statement to select the sequence number of a public folder, which is evaluated by determining the biggest
     * changing date of all contained appointments, considering both the "working" as well as the "backup" tables.
     *
     * @param cid The context identifier
     * @param fid The folder identifier
     * @param readcon The connection for creating the prepared statement
     * @return The prepared statement
     */
    PreparedStatement getPublicFolderSequenceNumber(int cid, int fid, Connection readcon) throws SQLException;

    PreparedStatement getPublicFolderObjects(int fid, Context c, Connection readcon) throws SQLException ;

    String loadAppointment(int oid, Context c) throws SQLException;

    ResultSet getResultSet(PreparedStatement pst) throws SQLException;

    PreparedStatement getPreparedStatement(Connection readcon, String sql) throws SQLException;

    Participants getUserParticipants(CalendarDataObject cdao, Connection readcon, int uid) throws SQLException, OXException;

    void getUserParticipantsSQLIn(CalendarFolderObject visibleFolders, List<CalendarDataObject> list, Connection readcon, int cid, int uid, String sqlin) throws SQLException, OXException;

    void getParticipantsSQLIn(List<CalendarDataObject> list, Connection readcon, int cid, String sqlin) throws SQLException;

    Participants getParticipants(CalendarDataObject cdao, Connection readcon) throws SQLException;

    CalendarDataObject[] insertAppointment(CalendarDataObject cdao, Connection writecon, Session so) throws SQLException, OXException;

    CalendarDataObject[] updateAppointment(CalendarDataObject cdao, CalendarDataObject edao, Connection writecon, Session so, Context ctx, int inFolder, Date clientLastModified) throws SQLException, OXException;

    CalendarDataObject loadObjectForUpdate(CalendarDataObject cdao, Session so, Context ctx, int inFolder, Connection con, boolean checkPermissions) throws SQLException, OXException;

    void deleteAppointment(int uid, CalendarDataObject cdao, Connection writecon, Session so, Context ctx, int inFolder, Date clientLastModified) throws SQLException, OXException;

    void deleteAppointment(int uid, CalendarDataObject cdao, Connection writecon, Session so, Context ctx, int inFolder, Date clientLastModified, boolean checkPermissions) throws SQLException, OXException;

    boolean deleteAppointmentsInFolder(Session so, Context ctx, ResultSet objects, Connection readcon, Connection writecon, int foldertype, int fid) throws SQLException, OXException;

    public Date setUserConfirmation(int oid, int folderId, int uid, int confirm, String confirm_message, Session so, Context ctx) throws OXException;

    boolean checkIfFolderContainsForeignObjects(int uid, int fid, Context c, Connection readcon, int foldertype) throws SQLException;

    boolean checkIfFolderIsEmpty(int uid, int fid, Context c, Connection readcon, int foldertype) throws SQLException;

    PreparedStatement getSharedFolderRangeSQL(Context c, int uid, int shared_folder_owner, int groups[], int fid, Date d1, Date d2, String select, boolean readall, Connection readcon, int orderBy, Order orderDir) throws SQLException;

    PreparedStatement getSharedFolderRangeSQL(Context c, int uid, int shared_folder_owner, int groups[], int fid, Date d1, Date d2, String select, boolean readall, Connection readcon, int orderBy, Order orderDir, boolean includePrivateAppointments) throws SQLException;

    PreparedStatement getSharedFolderModifiedSinceSQL(Context c, int uid, int shared_folder_owner, int groups[], int fid, Date since, String select, boolean readall, Connection readcon, Date d1, Date d2, boolean includePrivateFlag) throws SQLException;

    PreparedStatement getSharedFolderDeletedSinceSQL(Context c, int uid, int shared_folder_owner, int fid, Date d1, String select, Connection readcon) throws SQLException;

    long attachmentAction(int folderId, int oid, int uid, Session session, Context c, int numberOfAttachments) throws OXException;

    public PreparedStatement getSearchStatement(final int uid, final AppointmentSearchObject searchObj, final CalendarFolderObject cfo, final OXFolderAccess folderAccess, final String columns, final int orderBy, final Order orderDir, int limit, final Context ctx, final Connection readcon) throws SQLException, OXException;

    PreparedStatement getActiveAppointments(Context c, int uid, Date d1, Date d2, String select, Connection readcon) throws SQLException;

    public int resolveUid(Session session, String uid) throws OXException;

    int resolveFilename(Session session, String filename) throws OXException;

    int getFolder(Session session, int objectId) throws OXException;

    public Date setExternalConfirmation(int oid, int folderId, String mail, int confirm, String message, Session so, Context ctx) throws OXException;

    public int countAppointments(Session session) throws OXException;

    public int countObjectsInFolder(Session session, int folderId, int folderType, EffectivePermission permission) throws OXException;


}
