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

package com.openexchange.calendar.itip;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

public class ITipConsistencyCalendar extends ITipCalendarWrapper implements AppointmentSQLInterface {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ITipConsistencyCalendar.class);
    
    protected AppointmentSQLInterface delegate;

    private final UserService users;


    static interface ITipStrategy {

        void beforeUpdate(CalendarDataObject cdao)
            throws OXException;

        void afterUpdate(CalendarDataObject cdao);

        void delete(CalendarDataObject appointmentObject, int inFolder,
            Date clientLastModified, boolean checkPermissions)
                throws OXException;

    }

    private class InternalOrganizerStrategy implements ITipStrategy {

        @Override
        public void beforeUpdate(final CalendarDataObject cdao)
            throws OXException {
            // Increase the Sequence Number if dates, recurrences, full_time
            // change or if this is a create exception
            if (!cdao.containsSequence()) {
                try {
                    final CalendarDataObject loaded = getObjectById(cdao
                        .getObjectID());
                    final AppointmentDiff diff = AppointmentDiff
                        .compare(loaded, cdao);
                    if (diff.anyFieldChangedOf(CalendarObject.START_DATE,
                        CalendarObject.END_DATE,
                        CalendarObject.RECURRENCE_POSITION,
                        CalendarObject.RECURRENCE_ID,
                        CalendarObject.RECURRENCE_DATE_POSITION,
                        CalendarObject.RECURRENCE_COUNT,
                        Appointment.RECURRENCE_START,
                        Appointment.FULL_TIME, CalendarObject.TITLE,
                        Appointment.LOCATION, CalendarObject.PARTICIPANTS)) {
                        cdao.setSequence(loaded.getSequence() + 1);
                    }
                    if (cdao.getRecurrenceType() == 0) {
                        if (loaded.getRecurrenceType() != 0) {
                            cdao.setSequence(loaded.getSequence() + 1);
                        }
                    }
                } catch (final SQLException e) {
                    throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
                }
            }
        }

        @Override
        public void afterUpdate(final CalendarDataObject cdao) {

        }

        @Override
        public void delete(final CalendarDataObject appointmentObject, final int inFolder,
            final Date clientLastModified, final boolean checkPermissions)
                throws OXException {
            try {
                delegate.deleteAppointmentObject(appointmentObject, inFolder,
                    clientLastModified, checkPermissions);
            } catch (final SQLException e) {
                throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
            }
        }

    }

    private class ExternalOrganizerStrategy implements ITipStrategy {

        @Override
        public void beforeUpdate(final CalendarDataObject cdao) {
            // Nothing to do

        }

        @Override
        public void afterUpdate(final CalendarDataObject cdao) {
            // Nothing to do

        }

        @Override
        public void delete(final CalendarDataObject appointmentObject, final int inFolder, final Date clientLastModified, final boolean checkPermissions) throws OXException {
            try {
                final CalendarDataObject original = delegate.getObjectById(appointmentObject.getObjectID(), inFolder);
                if (original.containsUntil() && original.containsOccurrence()) {
                    original.removeUntil();
                }
                if (onlyOneParticipantRemaining(original)) {
                    cleanOccurrencesAndUnitl(original);
                    delegate.deleteAppointmentObject(appointmentObject, inFolder, clientLastModified, checkPermissions);
                } else {
                    removeCurrentUserFromParticipants(original);
                    original.setExternalOrganizer(true);
                    original.setIgnoreConflicts(true);
                    delegate.updateAppointmentObject(original, inFolder, clientLastModified, checkPermissions);
                }
            } catch (final SQLException e) {
                throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
            }
        }

        private void cleanOccurrencesAndUnitl(CalendarDataObject original) {
            if (original.containsOccurrence()) {
                original.removeUntil();
            }
        }

        private void removeCurrentUserFromParticipants(final CalendarDataObject original) {
            // New participants are all externals + all resources + all resolved
            // users from user participants - the current user participant
            int folderOwner = getSharedFolderOwner(original, session);
            final List<Participant> participants = new ArrayList<Participant>();
            final Participant[] p = original.getParticipants();
            if (p != null) {
                for (final Participant participant : p) {
                    if (!(participant instanceof GroupParticipant)
                        && !(participant instanceof UserParticipant)) {
                        participants.add(participant);
                    }
                }
            }

            final UserParticipant[] u = original.getUsers();
            final List<UserParticipant> newUserParticipants = new ArrayList<UserParticipant>();

            if (u != null) {
                for (final UserParticipant userParticipant : u) {
                    if (userParticipant.getIdentifier() != folderOwner) {
                        participants.add(userParticipant);
                        newUserParticipants.add(userParticipant);
                    }
                }
            }

            original.setParticipants(participants);
            original.setUsers(newUserParticipants);
        }

        private int getSharedFolderOwner(final CalendarDataObject cdao, final Session session) {
            if (cdao.getFolderType() != FolderObject.SHARED) {
                return session.getUserId();
            }
            try {
                final OXFolderAccess oxfa = new OXFolderAccess(new ServerSessionAdapter(session).getContext());
                return oxfa.getFolderOwner(cdao.getParentFolderID());
            } catch (final OXException e) {
                e.printStackTrace();
                return session.getUserId();
            }
        }

        private boolean onlyOneParticipantRemaining(final CalendarDataObject original) {
            int user = getSharedFolderOwner(original, session);
            final Participant[] participants = original.getParticipants();
            if (participants != null) {
                for (final Participant p : participants) {
                    if (p instanceof UserParticipant) {
                        final UserParticipant up = (UserParticipant) p;
                        if (up.getIdentifier() != user) {
                            return false;
                        }
                    }
                }
                return true;
            }

            final UserParticipant[] userParticipants = original.getUsers();
            if (userParticipants != null) {
                if (userParticipants.length > 1) {
                    return false;
                }

                if (userParticipants.length == 0) {
                    return true;
                }

                final UserParticipant up = userParticipants[0];
                return up.getIdentifier() == user;
            }
            return true;
        }

    }

    public ITipConsistencyCalendar(final AppointmentSQLInterface delegate,
        final Session session, final ServiceLookup services) throws OXException {
        super(session, services);
        this.delegate = delegate;
        this.users = services.getService(UserService.class);
        loadContext();
    }

    @Override
    public void setIncludePrivateAppointments(final boolean include) {
        delegate.setIncludePrivateAppointments(include);
    }

    @Override
    public boolean getIncludePrivateAppointments() {
        return delegate.getIncludePrivateAppointments();
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(
        final int folderId, final int[] cols, final Date start, final Date end, final int orderBy,
        final Order order) throws OXException, SQLException {
        return delegate.getAppointmentsBetweenInFolder(folderId, cols, start,
            end, orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(
        final int folderId, final int[] cols, final Date start, final Date end, final int from, final int to,
        final int orderBy, final Order orderDir) throws OXException, SQLException {
        return delegate.getAppointmentsBetweenInFolder(folderId, cols, start,
            end, from, to, orderBy, orderDir);
    }

    @Override
    public boolean[] hasAppointmentsBetween(final Date start, final Date end)
        throws OXException {
        return delegate.hasAppointmentsBetween(start, end);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(final int fid,
        final int[] cols, final Date since) throws OXException {
        return delegate.getModifiedAppointmentsInFolder(fid, cols, since);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsBetween(
        final int userId, final Date start, final Date end, final int[] cols, final Date since,
        final int orderBy, final Order orderDir) throws OXException, SQLException {
        return delegate.getModifiedAppointmentsBetween(userId, start, end,
            cols, since, orderBy, orderDir);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(final int fid,
        final Date start, final Date end, final int[] cols, final Date since) throws OXException,
        SQLException {
        return delegate.getModifiedAppointmentsInFolder(fid, start, end, cols,
            since);
    }

    @Override
    public SearchIterator<Appointment> getDeletedAppointmentsInFolder(
        final int folderId, final int[] cols, final Date since) throws OXException,
        SQLException {
        return delegate.getDeletedAppointmentsInFolder(folderId, cols, since);
    }

    @Override
    public SearchIterator<Appointment> searchAppointments(
        final AppointmentSearchObject searchObj, final int orderBy, final Order orderDir,
        final int[] cols) throws OXException {
        return delegate.searchAppointments(searchObj, orderBy, orderDir, cols);
    }

    @Override
    public CalendarDataObject getObjectById(final int objectId) throws OXException,
    SQLException {
        return addOrganizer(delegate.getObjectById(objectId));
    }

    @Override
    public CalendarDataObject getObjectById(final int objectId, final int inFolder)
        throws OXException, SQLException {
        return addOrganizer(delegate.getObjectById(objectId, inFolder));
    }

    private CalendarDataObject addOrganizer(final CalendarDataObject objectById) throws OXException{
        if (objectById.getOrganizer() == null) {
            final User u = users.getUser(objectById.getCreatedBy(), ctx);
            final String mail = u.getMail();
            objectById.setOrganizer(mail.toLowerCase());
            objectById.setOrganizerId(u.getId());
        }
        return objectById;
    }

    @Override
    public SearchIterator<Appointment> getObjectsById(
        final int[][] objectIdAndInFolder, final int[] cols) throws OXException {
        return delegate.getObjectsById(objectIdAndInFolder, cols);
    }

    @Override
    public Appointment[] insertAppointmentObject(final CalendarDataObject cdao)
        throws OXException {
        setOrganizer(cdao);
        setPrincipal(cdao);
        return delegate.insertAppointmentObject(cdao);
    }

    private void setPrincipal(final CalendarDataObject cdao) throws OXException {
        loadContext();
        if (cdao.getPrincipal() == null) {
            final int onBehalfOf = onBehalfOf(cdao.getParentFolderID());
            if (onBehalfOf > 0) {
                cdao.setPrincipal(users.getUser(onBehalfOf,ctx).getMail().toLowerCase());
                cdao.setPrincipalId(onBehalfOf);
            }
        } else {
            String principal = cdao.getPrincipal().toLowerCase();
            if (principal.startsWith("mailto:")) {
                principal = principal.substring(7);
            }
            try {
                final User result = users.searchUser(principal, ctx);
                final int uid = (result != null) ? result.getId() : 0;
                cdao.setPrincipalId(uid);
            } catch (final OXException e) {
            }
        }
    }

    private void setOrganizer(final CalendarDataObject cdao) throws OXException {
        if (cdao.getOrganizer() == null) {
            loadUser();
            cdao.setOrganizer(user.getMail().toLowerCase());
            cdao.setOrganizerId(user.getId());
        } else {

            String organizer = cdao.getOrganizer().toLowerCase();
            if (organizer.startsWith("mailto:")) {
                organizer = organizer.substring(7);
            }
            try {
                final User result = users.searchUser(organizer, ctx);
                final int uid = (result != null) ? result.getId() : 0;
                cdao.setOrganizerId(uid);
            } catch (final OXException e) {
            }
        }

    }



    @Override
    public Appointment[] updateAppointmentObject(final CalendarDataObject cdao,
        final int inFolder, final Date clientLastModified) throws OXException {
        return updateAppointmentObject(cdao, inFolder, clientLastModified, true);
    }

    @Override
    public Appointment[] updateAppointmentObject(final CalendarDataObject cdao,
        final int inFolder, final Date clientLastModified, final boolean checkPermissions)
            throws OXException {

        final ITipStrategy strategy = chooseStrategy(cdao);
        strategy.beforeUpdate(cdao);

        final Appointment[] retval = delegate.updateAppointmentObject(cdao, inFolder,
            clientLastModified, checkPermissions);

        if (retval == null || retval.length == 0) {
            strategy.afterUpdate(cdao);
        }

        return retval;
    }

    @Override
    public void deleteAppointmentObject(final CalendarDataObject appointmentObject,
        final int inFolder, final Date clientLastModified) throws OXException {
        deleteAppointmentObject(appointmentObject, inFolder,
            clientLastModified, true);
    }

    @Override
    public void deleteAppointmentObject(final CalendarDataObject appointmentObject,
        final int inFolder, final Date clientLastModified, final boolean checkPermissions)
            throws OXException {
        final ITipStrategy strategy = chooseStrategy(appointmentObject);

        strategy.delete(appointmentObject, inFolder, clientLastModified,
            checkPermissions);
    }

    @Override
    public void deleteAppointmentsInFolder(final int inFolder) throws OXException,
    SQLException {
        delegate.deleteAppointmentsInFolder(inFolder);
    }

    @Override
    public boolean deleteAppointmentsInFolder(final int inFolder, final Connection writeCon)
        throws OXException, SQLException {
        return delegate.deleteAppointmentsInFolder(inFolder, writeCon);
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(final int user_id, final int inFolder)
        throws OXException, SQLException {
        return delegate.checkIfFolderContainsForeignObjects(user_id, inFolder);
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(final int user_id,
        final int inFolder, final Connection readCon) throws OXException, SQLException {
        return delegate.checkIfFolderContainsForeignObjects(user_id, inFolder,
            readCon);
    }

    @Override
    public boolean isFolderEmpty(final int uid, final int fid) throws OXException,
    SQLException {
        return delegate.isFolderEmpty(uid, fid);
    }

    @Override
    public boolean isFolderEmpty(final int uid, final int fid, final Connection readCon)
        throws OXException, SQLException {
        return delegate.isFolderEmpty(uid, fid, readCon);
    }

    @Override
    public Date setUserConfirmation(final int objectId, final int folderId, final int userId,
        final int confirm, final String confirmMessage) throws OXException {
        return delegate.setUserConfirmation(objectId, folderId, userId,
            confirm, confirmMessage);
    }

    @Override
    public Date setExternalConfirmation(final int objectId, final int folderId, final String mail,
        final int confirm, final String message) throws OXException {
        return delegate.setExternalConfirmation(objectId, folderId, mail, confirm,
            message);
    }

    @Override
    public CalendarDataObject setUserConfirmation(int objectId, int folderId, int optOccurrenceId, int userId, int confirm, String confirmMessage) throws OXException {
        if (optOccurrenceId <= 0) {
            LOG.warn("No occurrence to set confirmation for found. Delegate set confirmation for whole series or one time appointment!");
            CalendarDataObject retval = new CalendarDataObject();
            retval.setLastModified(setUserConfirmation(objectId, folderId, userId, confirm, confirmMessage));
            return retval;
        }
        return delegate.setUserConfirmation(objectId, folderId, optOccurrenceId, userId, confirm, confirmMessage);
    }

    @Override
    public CalendarDataObject setExternalConfirmation(int objectId, int folderId, int optOccurrenceId, String mail, int confirm, String message) throws OXException {
        if (optOccurrenceId <= 0) {
            LOG.warn("No occurrence to set confirmation for found. Delegate set confirmation for whole series or one time appointment!");
            CalendarDataObject retval = new CalendarDataObject();
            retval.setLastModified(setExternalConfirmation(objectId, folderId, mail, confirm, message));
            return retval;
        }
        return delegate.setExternalConfirmation(objectId, folderId, optOccurrenceId, mail, confirm, message);
    }

    @Override
    public long attachmentAction(final int folderId, final int objectId, final int userId,
        final Session session, final Context c, final int numberOfAttachments)
            throws OXException {
        return delegate.attachmentAction(folderId, objectId, userId, session, c,
            numberOfAttachments);
    }

    @Override
    public SearchIterator<Appointment> getFreeBusyInformation(final int id, final int type,
        final Date start, final Date end) throws OXException {
        return delegate.getFreeBusyInformation(id, type, start, end);
    }

    @Override
    public SearchIterator<Appointment> getActiveAppointments(final int user_uid,
        final Date start, final Date end, final int[] cols) throws OXException {
        return delegate.getActiveAppointments(user_uid, start, end, cols);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(final int user_uid,
        final Date start, final Date end, final int[] cols, final int orderBy, final Order order)
            throws OXException, SQLException {
        return delegate.getAppointmentsBetween(user_uid, start, end, cols,
            orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(Date start, Date end, int cols[], int orderBy, Order order) throws OXException, SQLException {
        return delegate.getAppointmentsBetween(start, end, cols, orderBy, order);
    }

    @Override
    public int resolveUid(final String uid) throws OXException {
        return delegate.resolveUid(uid);
    }

    @Override
    public int resolveFilename(final String filename) throws OXException {
        return delegate.resolveFilename(filename);
    }

    @Override
    public int getFolder(final int objectId) throws OXException {
        return delegate.getFolder(objectId);
    }



    @Override
    public List<Appointment> getAppointmentsWithExternalParticipantBetween(
        final String email, final int[] cols, final Date start, final Date end, final int orderBy,
        final Order order) throws OXException {
        return delegate.getAppointmentsWithExternalParticipantBetween(email,
            cols, start, end, orderBy, order);
    }

    @Override
    public List<Appointment> getAppointmentsWithUserBetween(final User user,
        final int[] cols, final Date start, final Date end, final int orderBy, final Order order)
            throws OXException {
        return delegate.getAppointmentsWithUserBetween(user, cols, start, end,
            orderBy, order);
    }

    private void setOrganizerType(final CalendarDataObject appointment)
        throws OXException {
        String organizer = appointment.getOrganizer();
        if (organizer == null) {
            Appointment loaded = null;
            try {
                loaded = getObjectById(appointment.getObjectID());
            } catch (final SQLException e) {
                appointment.setExternalOrganizer(false);
                return;
            } catch (final OXException x) {
                appointment.setExternalOrganizer(false);
                return;
            }
            organizer = loaded.getOrganizer();
            if (organizer == null || organizer.trim().equals("") || appointment.getOrganizerId() > 0 || appointment.getPrincipalId() > 0) {
                appointment.setExternalOrganizer(false);
                return;
            }
        }

        if (organizer.startsWith("mailto:")) {
            organizer = organizer.substring(7);
        }
        int uid = -1;
        try {
            final User result = users.searchUser(organizer, ctx);
            uid = (result != null) ? result.getId() : -1;
        } catch (final OXException e) {
        }

        if (uid == -1) {
            appointment.setExternalOrganizer(true);
            return;
        }
        appointment.setExternalOrganizer(false);
        return;
    }

    private ITipStrategy chooseStrategy(final CalendarDataObject appointment)
        throws OXException {
        setOrganizerType(appointment);
        if (appointment.isExternalOrganizer()) {
            return new ExternalOrganizerStrategy();
        }
        return new InternalOrganizerStrategy();
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#count(int)
     */
    @Override
    public int countObjectsInFolder(int folderId) throws OXException {
        return delegate.countObjectsInFolder(folderId);
    }

}
