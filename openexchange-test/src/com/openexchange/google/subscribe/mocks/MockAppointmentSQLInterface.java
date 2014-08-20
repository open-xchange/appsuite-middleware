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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.google.subscribe.mocks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link MockAppointmentSQLInterface}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class MockAppointmentSQLInterface implements AppointmentSQLInterface {

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#setIncludePrivateAppointments(boolean)
     */
    @Override
    public void setIncludePrivateAppointments(boolean include) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getIncludePrivateAppointments()
     */
    @Override
    public boolean getIncludePrivateAppointments() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getAppointmentsBetweenInFolder(int, int[], java.util.Date, java.util.Date, int,
     * com.openexchange.groupware.search.Order)
     */
    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(int folderId, int[] cols, Date start, Date end, int orderBy, Order order) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getAppointmentsBetweenInFolder(int, int[], java.util.Date, java.util.Date, int,
     * int, int, com.openexchange.groupware.search.Order)
     */
    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(int folderId, int[] cols, Date start, Date end, int from, int to, int orderBy, Order orderDir) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#hasAppointmentsBetween(java.util.Date, java.util.Date)
     */
    @Override
    public boolean[] hasAppointmentsBetween(Date start, Date end) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getAppointmentsWithExternalParticipantBetween(java.lang.String, int[],
     * java.util.Date, java.util.Date, int, com.openexchange.groupware.search.Order)
     */
    @Override
    public List<Appointment> getAppointmentsWithExternalParticipantBetween(String email, int[] cols, Date start, Date end, int orderBy, Order order) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getAppointmentsWithUserBetween(com.openexchange.groupware.ldap.User, int[],
     * java.util.Date, java.util.Date, int, com.openexchange.groupware.search.Order)
     */
    @Override
    public List<Appointment> getAppointmentsWithUserBetween(User user, int[] cols, Date start, Date end, int orderBy, Order order) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getModifiedAppointmentsInFolder(int, int[], java.util.Date)
     */
    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(int fid, int[] cols, Date since) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getModifiedAppointmentsBetween(int, java.util.Date, java.util.Date, int[],
     * java.util.Date, int, com.openexchange.groupware.search.Order)
     */
    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsBetween(int userId, Date start, Date end, int[] cols, Date since, int orderBy, Order orderDir) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getModifiedAppointmentsInFolder(int, java.util.Date, java.util.Date, int[],
     * java.util.Date)
     */
    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(int fid, Date start, Date end, int[] cols, Date since) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getDeletedAppointmentsInFolder(int, int[], java.util.Date)
     */
    @Override
    public SearchIterator<Appointment> getDeletedAppointmentsInFolder(int folderId, int[] cols, Date since) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#searchAppointments(com.openexchange.groupware.search.AppointmentSearchObject, int,
     * com.openexchange.groupware.search.Order, int[])
     */
    @Override
    public SearchIterator<Appointment> searchAppointments(AppointmentSearchObject searchObj, int orderBy, Order orderDir, int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getObjectById(int)
     */
    @Override
    public CalendarDataObject getObjectById(int objectId) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getObjectById(int, int)
     */
    @Override
    public CalendarDataObject getObjectById(int objectId, int inFolder) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getObjectsById(int[][], int[])
     */
    @Override
    public SearchIterator<Appointment> getObjectsById(int[][] objectIdAndInFolder, int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#insertAppointmentObject(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public Appointment[] insertAppointmentObject(CalendarDataObject cdao) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#updateAppointmentObject(com.openexchange.groupware.calendar.CalendarDataObject,
     * int, java.util.Date)
     */
    @Override
    public Appointment[] updateAppointmentObject(CalendarDataObject cdao, int inFolder, Date clientLastModified) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#updateAppointmentObject(com.openexchange.groupware.calendar.CalendarDataObject,
     * int, java.util.Date, boolean)
     */
    @Override
    public Appointment[] updateAppointmentObject(CalendarDataObject cdao, int inFolder, Date clientLastModified, boolean checkPermissions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#deleteAppointmentObject(com.openexchange.groupware.calendar.CalendarDataObject,
     * int, java.util.Date)
     */
    @Override
    public void deleteAppointmentObject(CalendarDataObject appointmentObject, int inFolder, Date clientLastModified) throws OXException, SQLException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#deleteAppointmentObject(com.openexchange.groupware.calendar.CalendarDataObject,
     * int, java.util.Date, boolean)
     */
    @Override
    public void deleteAppointmentObject(CalendarDataObject appointmentObject, int inFolder, Date clientLastModified, boolean checkPermissions) throws OXException, SQLException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#deleteAppointmentsInFolder(int)
     */
    @Override
    public void deleteAppointmentsInFolder(int inFolder) throws OXException, SQLException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#deleteAppointmentsInFolder(int, java.sql.Connection)
     */
    @Override
    public boolean deleteAppointmentsInFolder(int inFolder, Connection writeCon) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#checkIfFolderContainsForeignObjects(int, int)
     */
    @Override
    public boolean checkIfFolderContainsForeignObjects(int user_id, int inFolder) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#checkIfFolderContainsForeignObjects(int, int, java.sql.Connection)
     */
    @Override
    public boolean checkIfFolderContainsForeignObjects(int user_id, int inFolder, Connection readCon) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#isFolderEmpty(int, int)
     */
    @Override
    public boolean isFolderEmpty(int uid, int fid) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#isFolderEmpty(int, int, java.sql.Connection)
     */
    @Override
    public boolean isFolderEmpty(int uid, int fid, Connection readCon) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#setUserConfirmation(int, int, int, int, java.lang.String)
     */
    @Override
    public Date setUserConfirmation(int objectId, int folderId, int userId, int confirm, String confirmMessage) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#setExternalConfirmation(int, int, java.lang.String, int, java.lang.String)
     */
    @Override
    public Date setExternalConfirmation(int objectId, int folderId, String mail, int confirm, String message) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#setUserConfirmation(int, int, int, int, int, java.lang.String)
     */
    @Override
    public CalendarDataObject setUserConfirmation(int objectId, int folderId, int optOccurrenceId, int userId, int confirm, String confirmMessage) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#setExternalConfirmation(int, int, int, java.lang.String, int, java.lang.String)
     */
    @Override
    public CalendarDataObject setExternalConfirmation(int objectId, int folderId, int optOccurrenceId, String mail, int confirm, String message) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#attachmentAction(int, int, int, com.openexchange.session.Session,
     * com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public long attachmentAction(int folderId, int objectId, int userId, Session session, Context c, int numberOfAttachments) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getFreeBusyInformation(int, int, java.util.Date, java.util.Date)
     */
    @Override
    public SearchIterator<Appointment> getFreeBusyInformation(int id, int type, Date start, Date end) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getActiveAppointments(int, java.util.Date, java.util.Date, int[])
     */
    @Override
    public SearchIterator<Appointment> getActiveAppointments(int user_uid, Date start, Date end, int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getAppointmentsBetween(int, java.util.Date, java.util.Date, int[], int,
     * com.openexchange.groupware.search.Order)
     */
    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(int user_uid, Date start, Date end, int[] cols, int orderBy, Order order) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getAppointmentsBetween(java.util.Date, java.util.Date, int[], int,
     * com.openexchange.groupware.search.Order)
     */
    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(Date start, Date end, int[] cols, int orderBy, Order order) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#resolveUid(java.lang.String)
     */
    @Override
    public int resolveUid(String uid) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#resolveFilename(java.lang.String)
     */
    @Override
    public int resolveFilename(String filename) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#getFolder(int)
     */
    @Override
    public int getFolder(int objectId) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#countObjectsInFolder(int)
     */
    @Override
    public int countObjectsInFolder(int folderId) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

}
