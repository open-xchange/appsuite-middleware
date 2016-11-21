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

package com.openexchange.calendar.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

public class TransactionallyCachingCalendar implements AppointmentSQLInterface {
    private final AppointmentSQLInterface delegate;
    private final Map<Integer, CalendarDataObject> cached = new HashMap<Integer, CalendarDataObject>();

    public TransactionallyCachingCalendar(CalendarSql calendarSql) {
        this.delegate = calendarSql;
    }

    @Override
    public void setIncludePrivateAppointments(boolean include) {
        delegate.setIncludePrivateAppointments(include);
    }

    @Override
    public boolean getIncludePrivateAppointments() {
        return delegate.getIncludePrivateAppointments();
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(
        int folderId, int[] cols, Date start, Date end, int orderBy,
        Order order) throws com.openexchange.exception.OXException,
        SQLException {
        return delegate.getAppointmentsBetweenInFolder(folderId, cols, start,
            end, orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(
        int folderId, int[] cols, Date start, Date end, int from, int to,
        int orderBy, Order orderDir)
            throws com.openexchange.exception.OXException, SQLException {
        return delegate.getAppointmentsBetweenInFolder(folderId, cols, start,
            end, from, to, orderBy, orderDir);
    }

    @Override
    public boolean[] hasAppointmentsBetween(Date start, Date end)
        throws com.openexchange.exception.OXException {
        return delegate.hasAppointmentsBetween(start, end);
    }

    @Override
    public List<Appointment> getAppointmentsWithExternalParticipantBetween(
        String email, int[] cols, Date start, Date end, int orderBy,
        Order order) throws com.openexchange.exception.OXException {
        return delegate.getAppointmentsWithExternalParticipantBetween(email,
            cols, start, end, orderBy, order);
    }

    @Override
    public List<Appointment> getAppointmentsWithUserBetween(User user,
        int[] cols, Date start, Date end, int orderBy, Order order)
            throws com.openexchange.exception.OXException {
        return delegate.getAppointmentsWithUserBetween(user, cols, start, end,
            orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(int fid,
        int[] cols, Date since)
            throws com.openexchange.exception.OXException {
        return delegate.getModifiedAppointmentsInFolder(fid, cols, since);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsBetween(
        int userId, Date start, Date end, int[] cols, Date since,
        int orderBy, Order orderDir)
            throws com.openexchange.exception.OXException, SQLException {
        return delegate.getModifiedAppointmentsBetween(userId, start, end,
            cols, since, orderBy, orderDir);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(int fid,
        Date start, Date end, int[] cols, Date since)
            throws com.openexchange.exception.OXException, SQLException {
        return delegate.getModifiedAppointmentsInFolder(fid, start, end, cols,
            since);
    }

    @Override
    public SearchIterator<Appointment> getDeletedAppointmentsInFolder(
        int folderId, int[] cols, Date since)
            throws com.openexchange.exception.OXException, SQLException {
        return delegate.getDeletedAppointmentsInFolder(folderId, cols, since);
    }

    @Override
    public long getSequenceNumber(int folderId) throws OXException {
        return delegate.getSequenceNumber(folderId);
    }

    @Override
    public void deleteAppointmentObject(CalendarDataObject appointmentObject,
        int inFolder, Date clientLastModified, boolean checkPermissions)
            throws OXException, SQLException {
        cached.remove(appointmentObject.getObjectID());
        delegate.deleteAppointmentObject(appointmentObject, inFolder, clientLastModified, checkPermissions);

    }

    @Override
    public SearchIterator<Appointment> searchAppointments(
        AppointmentSearchObject searchObj, int orderBy, Order orderDir,
        int[] cols) throws com.openexchange.exception.OXException {
        return delegate.searchAppointments(searchObj, orderBy, orderDir, cols);
    }

    @Override
    public SearchIterator<Appointment> searchAppointments(AppointmentSearchObject searchObj, int orderBy, Order orderDir, int limit, int[] cols) throws OXException {
        return delegate.searchAppointments(searchObj, orderBy, orderDir, limit, cols);
    }

    @Override
    public CalendarDataObject getObjectById(int objectId) throws OXException, SQLException {
        CalendarDataObject cachedAppointment = cached.get(objectId);
        if (cachedAppointment != null) {
            return cachedAppointment.clone();
        }
        CalendarDataObject loaded = delegate.getObjectById(objectId);
        cached.put(objectId, loaded);
        return loaded;
    }

    @Override
    public CalendarDataObject getObjectById(int objectId, int inFolder)
        throws com.openexchange.exception.OXException, SQLException {
        return delegate.getObjectById(objectId, inFolder);
    }

    @Override
    public CalendarDataObject getObjectById(int objectId, int inFolder, Connection readConnection) throws OXException, SQLException {
        return delegate.getObjectById(objectId, inFolder, readConnection);
    }
    
    @Override
    public SearchIterator<Appointment> getObjectsById(
        int[][] objectIdAndInFolder, int[] cols)
            throws com.openexchange.exception.OXException {
        return delegate.getObjectsById(objectIdAndInFolder, cols);
    }

    @Override
    public Appointment[] insertAppointmentObject(CalendarDataObject cdao)
        throws com.openexchange.exception.OXException {
        return delegate.insertAppointmentObject(cdao);
    }

    @Override
    public Appointment[] updateAppointmentObject(CalendarDataObject cdao,
        int inFolder, Date clientLastModified)
            throws com.openexchange.exception.OXException {
        cached.remove(cdao.getObjectID());
        return delegate.updateAppointmentObject(cdao, inFolder,
            clientLastModified);
    }

    @Override
    public Appointment[] updateAppointmentObject(CalendarDataObject cdao,
        int inFolder, Date clientLastModified, boolean checkPermissions)
            throws OXException {
        cached.remove(cdao.getObjectID());
        return delegate.updateAppointmentObject(cdao, inFolder, clientLastModified, checkPermissions);
    }

    @Override
    public void deleteAppointmentObject(CalendarDataObject appointmentObject,
        int inFolder, Date clientLastModified)
            throws com.openexchange.exception.OXException, SQLException {
        delegate.deleteAppointmentObject(appointmentObject, inFolder,
            clientLastModified);
    }

    @Override
    public void deleteAppointmentsInFolder(int inFolder)
        throws com.openexchange.exception.OXException, SQLException {
        cached.clear();
        delegate.deleteAppointmentsInFolder(inFolder);
    }

    @Override
    public boolean deleteAppointmentsInFolder(int inFolder, Connection writeCon)
        throws com.openexchange.exception.OXException, SQLException {
        cached.clear();
        return delegate.deleteAppointmentsInFolder(inFolder, writeCon);
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(int user_id, int inFolder)
        throws com.openexchange.exception.OXException, SQLException {
        return delegate.checkIfFolderContainsForeignObjects(user_id, inFolder);
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(int user_id,
        int inFolder, Connection readCon)
            throws com.openexchange.exception.OXException, SQLException {
        return delegate.checkIfFolderContainsForeignObjects(user_id, inFolder,
            readCon);
    }

    @Override
    public boolean isFolderEmpty(int uid, int fid)
        throws com.openexchange.exception.OXException, SQLException {
        return delegate.isFolderEmpty(uid, fid);
    }

    @Override
    public boolean isFolderEmpty(int uid, int fid, Connection readCon)
        throws com.openexchange.exception.OXException, SQLException {
        return delegate.isFolderEmpty(uid, fid, readCon);
    }

    @Override
    public Date setUserConfirmation(int objectId, int folderId, int userId,
        int confirm, String confirmMessage)
            throws com.openexchange.exception.OXException {
        cached.remove(objectId);
        return delegate.setUserConfirmation(objectId, folderId, userId,
            confirm, confirmMessage);
    }

    @Override
    public Date setExternalConfirmation(int objectId, int folderId, String mail,
        int confirm, String message)
            throws com.openexchange.exception.OXException {
        cached.remove(objectId);
        return delegate.setExternalConfirmation(objectId, folderId, mail, confirm,
            message);
    }

    @Override
    public CalendarDataObject setUserConfirmation(final int objectId, final int folderId, final int optOccurrenceId, final int userId, final int confirm, final String confirmMessage) throws OXException {
        cached.remove(objectId);
        return delegate.setUserConfirmation(objectId, folderId, optOccurrenceId, userId, confirm, confirmMessage);
    }

    @Override
    public CalendarDataObject setExternalConfirmation(final int objectId, final int folderId, final int optOccurrenceId, final String mail, final int confirm, final String message) throws OXException {
        cached.remove(objectId);
        return delegate.setExternalConfirmation(objectId, folderId, optOccurrenceId, mail, confirm, message);
    }

    @Override
    public long attachmentAction(int folderId, int objectId, int userId,
        Session session, Context c, int numberOfAttachments)
            throws com.openexchange.exception.OXException {
        cached.remove(objectId);
        return delegate.attachmentAction(folderId, objectId, userId, session, c,
            numberOfAttachments);
    }

    @Override
    public SearchIterator<Appointment> getFreeBusyInformation(int id, int type,
        Date start, Date end) throws com.openexchange.exception.OXException {
        return delegate.getFreeBusyInformation(id, type, start, end);
    }

    @Override
    public SearchIterator<Appointment> getActiveAppointments(int user_uid,
        Date start, Date end, int[] cols)
            throws com.openexchange.exception.OXException {
        return delegate.getActiveAppointments(user_uid, start, end, cols);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(int user_uid,
        Date start, Date end, int[] cols, int orderBy, Order order)
            throws com.openexchange.exception.OXException, SQLException {
        return delegate.getAppointmentsBetween(user_uid, start, end, cols,
            orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(Date start, Date end, int cols[], int orderBy, Order order) throws OXException, SQLException {
        return delegate.getAppointmentsBetween(start, end, cols, orderBy, order);
    }

    @Override
    public int resolveUid(String uid)
        throws com.openexchange.exception.OXException {
        return delegate.resolveUid(uid);
    }

    @Override
    public int resolveFilename(String filename)
        throws com.openexchange.exception.OXException {
        return delegate.resolveFilename(filename);
    }

    @Override
    public int getFolder(int objectId)
        throws com.openexchange.exception.OXException {
        return delegate.getFolder(objectId);
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#count(int)
     */
    @Override
    public int countObjectsInFolder(int folderId) throws OXException {
        return delegate.countObjectsInFolder(folderId);
    }

}
