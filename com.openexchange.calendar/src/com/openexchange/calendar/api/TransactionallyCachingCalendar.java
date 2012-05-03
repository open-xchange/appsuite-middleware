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
	public void deleteAppointmentObject(CalendarDataObject appointmentObject,
			int inFolder, Date clientLastModified, boolean checkPermissions)
			throws OXException, SQLException {
		cached.remove(appointmentObject.getObjectID());
		delegate.deleteAppointmentObject(appointmentObject, inFolder, clientLastModified, checkPermissions);
		
	}

	@Override
    public SearchIterator<Appointment> getAppointmentsByExtendedSearch(
			AppointmentSearchObject searchObject, int orderBy, Order orderDir,
			int[] cols) throws com.openexchange.exception.OXException,
			SQLException {
		return delegate.getAppointmentsByExtendedSearch(searchObject, orderBy,
				orderDir, cols);
	}

	@Override
    public SearchIterator<Appointment> searchAppointments(
			AppointmentSearchObject searchObj, int orderBy, Order orderDir,
			int[] cols) throws com.openexchange.exception.OXException {
		return delegate.searchAppointments(searchObj, orderBy, orderDir, cols);
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
    public void deleteAppointmentsInFolder(int inFolder, Connection writeCon)
			throws com.openexchange.exception.OXException, SQLException {
		cached.clear();
		delegate.deleteAppointmentsInFolder(inFolder, writeCon);
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
    public Date setUserConfirmation(int object_id, int folderId, int user_id,
			int confirm, String confirm_message)
			throws com.openexchange.exception.OXException {
		cached.remove(object_id);
		return delegate.setUserConfirmation(object_id, folderId, user_id,
				confirm, confirm_message);
	}

	@Override
    public Date setExternalConfirmation(int oid, int folderId, String mail,
			int confirm, String message)
			throws com.openexchange.exception.OXException {
		cached.remove(oid);
		return delegate.setExternalConfirmation(oid, folderId, mail, confirm,
				message);
	}

	@Override
    public long attachmentAction(int objectId, int uid, int folderId,
			Session session, Context c, int numberOfAttachments)
			throws com.openexchange.exception.OXException {
		cached.remove(objectId);
		return delegate.attachmentAction(objectId, uid, folderId, session, c,
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
    public int resolveUid(String uid)
			throws com.openexchange.exception.OXException {
		return delegate.resolveUid(uid);
	}

	@Override
    public int getFolder(int objectId)
			throws com.openexchange.exception.OXException {
		return delegate.getFolder(objectId);
	}

}
