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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.itip.generators.AttachmentMemory;
import com.openexchange.calendar.itip.generators.ITipMailGenerator;
import com.openexchange.calendar.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.sender.MailSenderService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.State;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link NotifyingCalendar}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NotifyingCalendar extends ITipCalendarWrapper implements AppointmentSQLInterface {

    private final AppointmentSQLInterface delegate;

    private final MailSenderService sender;

    private final ITipMailGeneratorFactory generators;

    private final CalendarCollection calendarCollection;

    private final AttachmentMemory attachmentMemory;

    private static class AppointmentAddress {
        private final int id;
        private final int cid;
        public AppointmentAddress(final int id, final int cid) {
            super();
            this.id = id;
            this.cid = cid;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + id;
            return result;
        }
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AppointmentAddress other = (AppointmentAddress) obj;
            if (cid != other.cid) {
                return false;
            }
            if (id != other.id) {
                return false;
            }
            return true;
        }

    }

    private static ConcurrentHashMap<AppointmentAddress, AppointmentAddress> createNewLimbo = new ConcurrentHashMap<AppointmentAddress, AppointmentAddress>();


    public NotifyingCalendar(final ITipMailGeneratorFactory generators, final MailSenderService sender, final AppointmentSQLInterface delegate, final AttachmentMemory attachmentMemory, final ServiceLookup services, final Session session) {
        super(session, services);
        this.delegate = delegate;
        this.generators = generators;
        this.sender = sender;
        this.attachmentMemory = attachmentMemory;

        calendarCollection = new CalendarCollection();
    }

    @Override
    public long attachmentAction(final int folderId, final int objectId, final int userId, final Session session, final Context c, final int numberOfAttachments) throws OXException {
        attachmentMemory.rememberAttachmentChange(objectId, c.getContextId());

        final long retval = delegate.attachmentAction(folderId, objectId, userId, session, c, numberOfAttachments);
        // Trigger Update Mail unless attachment is in create new limbo
        if (!createNewLimbo.containsKey(new AppointmentAddress(objectId, c.getContextId()))) {
            try {
                final CalendarDataObject reloaded = getObjectById(objectId);
                final ITipMailGenerator generator = generators.create(reloaded, reloaded, session, onBehalfOf(folderId));
                final List<NotificationParticipant> recipients = generator.getRecipients();
                for (final NotificationParticipant notificationParticipant : recipients) {
                    NotificationMail mail;
                    mail = generator.generateUpdateMailFor(notificationParticipant);
                    if (mail != null) {
                        if (mail.getStateType() == null) {
                            mail.setStateType(State.Type.MODIFIED);
                        }
                        sender.sendMail(mail, session);
                    }
                }
            } catch (final SQLException e) {
                throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
            }
        }
        return retval;

    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(final int user_id, final int inFolder, final Connection readCon) throws OXException, SQLException {
        return delegate.checkIfFolderContainsForeignObjects(user_id, inFolder, readCon);
    }

    @Override
    public boolean checkIfFolderContainsForeignObjects(final int user_id, final int inFolder) throws OXException, SQLException {
        return delegate.checkIfFolderContainsForeignObjects(user_id, inFolder);
    }

    @Override
    public void deleteAppointmentObject(final CalendarDataObject appointmentObject, final int inFolder, final Date clientLastModified) throws OXException, SQLException {
        deleteAppointmentObject(appointmentObject, inFolder, clientLastModified, true);
    }

    @Override
    public void deleteAppointmentObject(final CalendarDataObject appointmentObject, final int inFolder, final Date clientLastModified, final boolean checkPermissions) throws OXException, SQLException {
        CalendarDataObject original = null;
        try {
            original = getObjectById(appointmentObject.getObjectID());
            if (appointmentObject.containsNotification()) {
                original.setNotification(appointmentObject.getNotification());
            }
        } catch (final OXException x) {
            // IGNORE
        } catch (final SQLException e) {
            // IGNORE
        }
        delegate.deleteAppointmentObject(appointmentObject, inFolder, clientLastModified, checkPermissions);

        if (original == null) {
            return;
        }
        calculateExceptionPosition(appointmentObject, original, true);
        final ITipMailGenerator generator = generators.create(null, original,
            session, onBehalfOf(inFolder));
        final List<NotificationParticipant> recipients = generator
            .getRecipients();
        for (final NotificationParticipant notificationParticipant : recipients) {
            final NotificationMail mail = generator
                .generateDeleteMailFor(notificationParticipant);
            if (mail != null) {
                if (mail.getStateType() == null) {
                    mail.setStateType(State.Type.DELETED);
                }
                sender.sendMail(mail, session);
            }
        }
    }

    @Override
    public boolean deleteAppointmentsInFolder(final int inFolder, final Connection writeCon) throws OXException, SQLException {
        return delegate.deleteAppointmentsInFolder(inFolder, writeCon);
    }

    @Override
    public void deleteAppointmentsInFolder(final int inFolder) throws OXException, SQLException {
        delegate.deleteAppointmentsInFolder(inFolder);
    }

    @Override
    public SearchIterator<Appointment> getActiveAppointments(final int user_uid, final Date start, final Date end, final int[] cols) throws OXException {
        return delegate.getActiveAppointments(user_uid, start, end, cols);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(final int user_uid, final Date start, final Date end, final int[] cols, final int orderBy, final Order order) throws OXException, SQLException {
        return delegate.getAppointmentsBetween(user_uid, start, end, cols, orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetween(Date start, Date end, int cols[], int orderBy, Order order) throws OXException, SQLException {
        return delegate.getAppointmentsBetween(start, end, cols, orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(final int folderId, final int[] cols, final Date start, final Date end, final int from, final int to, final int orderBy, final Order orderDir) throws OXException, SQLException {
        return delegate.getAppointmentsBetweenInFolder(folderId, cols, start, end, from, to, orderBy, orderDir);
    }

    @Override
    public SearchIterator<Appointment> getAppointmentsBetweenInFolder(final int folderId, final int[] cols, final Date start, final Date end, final int orderBy, final Order order) throws OXException, SQLException {
        return delegate.getAppointmentsBetweenInFolder(folderId, cols, start, end, orderBy, order);
    }

    @Override
    public SearchIterator<Appointment> getDeletedAppointmentsInFolder(final int folderId, final int[] cols, final Date since) throws OXException, SQLException {
        return delegate.getDeletedAppointmentsInFolder(folderId, cols, since);
    }

    @Override
    public long getSequenceNumber(int folderId) throws OXException {
        return delegate.getSequenceNumber(folderId);
    }

    @Override
    public int getFolder(final int objectId) throws OXException {
        return delegate.getFolder(objectId);
    }

    @Override
    public SearchIterator<Appointment> getFreeBusyInformation(final int id, final int type, final Date start, final Date end) throws OXException {
        return delegate.getFreeBusyInformation(id, type, start, end);
    }

    @Override
    public boolean getIncludePrivateAppointments() {
        return delegate.getIncludePrivateAppointments();
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsBetween(final int userId, final Date start, final Date end, final int[] cols, final Date since, final int orderBy, final Order orderDir) throws OXException, SQLException {
        return delegate.getModifiedAppointmentsBetween(userId, start, end, cols, since, orderBy, orderDir);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(final int fid, final Date start, final Date end, final int[] cols, final Date since) throws OXException, SQLException {
        return delegate.getModifiedAppointmentsInFolder(fid, start, end, cols, since);
    }

    @Override
    public SearchIterator<Appointment> getModifiedAppointmentsInFolder(final int fid, final int[] cols, final Date since) throws OXException {
        return delegate.getModifiedAppointmentsInFolder(fid, cols, since);
    }

    @Override
    public CalendarDataObject getObjectById(final int objectId, final int inFolder) throws OXException, SQLException {
        return delegate.getObjectById(objectId, inFolder);
    }

    @Override
    public SearchIterator<Appointment> getObjectsById(final int[][] objectIdAndInFolder, final int[] cols) throws OXException {
        return delegate.getObjectsById(objectIdAndInFolder, cols);
    }

    @Override
    public boolean[] hasAppointmentsBetween(final Date start, final Date end) throws OXException {
        return delegate.hasAppointmentsBetween(start, end);
    }

    @Override
    public Appointment[] insertAppointmentObject(final CalendarDataObject cdao) throws OXException {
        try {
            final Appointment[] retval = delegate.insertAppointmentObject(cdao);
            if (retval == null || retval.length == 0) {
                final CalendarDataObject reloaded = getObjectById(cdao.getObjectID());
                if (cdao.containsNotification()) {
                    reloaded.setNotification(cdao.getNotification());
                }
                final ITipMailGenerator generator = generators.create(null, reloaded, session, onBehalfOf(cdao.getParentFolderID()));
                final List<NotificationParticipant> recipients = generator.getRecipients();
                for (final NotificationParticipant notificationParticipant : recipients) {
                    final NotificationMail mail = generator.generateCreateMailFor(notificationParticipant);
                    if (mail != null) {
                        if (mail.getStateType() == null) {
                            mail.setStateType(State.Type.NEW);
                        }
                        sender.sendMail(mail, session);
                    }
                }
            }
            return retval;
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        }
    }



    @Override
    public boolean isFolderEmpty(final int uid, final int fid, final Connection readCon) throws OXException, SQLException {
        return delegate.isFolderEmpty(uid, fid, readCon);
    }

    @Override
    public boolean isFolderEmpty(final int uid, final int fid) throws OXException, SQLException {
        return delegate.isFolderEmpty(uid, fid);
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
    public SearchIterator<Appointment> searchAppointments(final AppointmentSearchObject searchObj, final int orderBy, final Order orderDir, final int[] cols) throws OXException {
        return delegate.searchAppointments(searchObj, orderBy, orderDir, cols);
    }

    @Override
    public SearchIterator<Appointment> searchAppointments(AppointmentSearchObject searchObj, int orderBy, Order orderDir, int limit, int[] cols) throws OXException {
        return delegate.searchAppointments(searchObj, orderBy, orderDir, limit, cols);
    }

    @Override
    public Date setExternalConfirmation(final int objectId, final int folderId, final String mail, final int confirm, final String message) throws OXException {
        return delegate.setExternalConfirmation(objectId, folderId, mail, confirm, message);
    }

    @Override
    public CalendarDataObject setUserConfirmation(final int objectId, final int folderId, final int optOccurrenceId, final int userId, final int confirm, final String confirmMessage) throws OXException {
        CalendarDataObject retval = null;
        try {
            CalendarDataObject original = getObjectById(objectId);
            retval = delegate.setUserConfirmation(objectId, folderId, optOccurrenceId, userId, confirm, confirmMessage);
            if (retval.getObjectID() > 0 && retval.getObjectID() != objectId) { // Change exception was created
                retval = getObjectById(retval.getObjectID());
                generateUpdateMail(folderId, original, retval);
            }
        } catch (SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        }
        return retval;
    }

    @Override
    public CalendarDataObject setExternalConfirmation(final int objectId, final int folderId, final int optOccurrenceId, final String mail, final int confirm, final String message) throws OXException {
        CalendarDataObject retval = null;
        try {
            CalendarDataObject original = getObjectById(objectId);
            retval = delegate.setExternalConfirmation(objectId, folderId, optOccurrenceId, mail, confirm, message);
            if (retval.getObjectID() > 0 && retval.getObjectID() != objectId) { // Change exception was created
                retval = getObjectById(retval.getObjectID());
                generateUpdateMail(folderId, original, retval);
            }
        } catch (SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        }
        return retval;
    }

    @Override
    public void setIncludePrivateAppointments(final boolean include) {
        delegate.setIncludePrivateAppointments(include);
    }

    @Override
    public Date setUserConfirmation(final int objectId, final int folderId, final int userId, final int confirm, final String confirmMessage) throws OXException {
        try {
            final CalendarDataObject original = getObjectById(objectId);
            final Date retval = delegate.setUserConfirmation(objectId, folderId, userId, confirm, confirmMessage);
            final CalendarDataObject reloaded = getObjectById(objectId);
            final ITipMailGenerator generator = generators.create(original, reloaded, session, onBehalfOf(folderId));
            final List<NotificationParticipant> recipients = generator.getRecipients();
            for (final NotificationParticipant notificationParticipant : recipients) {
                final NotificationMail mail = generator.generateUpdateMailFor(notificationParticipant);
                if (mail != null) {
                    if (mail.getStateType() == null) {
                        State.Type type;
                        switch (ConfirmStatus.byId(confirm)) {
                        case ACCEPT:
                            type = State.Type.ACCEPTED;
                            break;
                        case DECLINE:
                            type = State.Type.DECLINED;
                            break;
                        case TENTATIVE:
                            type = State.Type.TENTATIVELY_ACCEPTED;
                            break;
                        case NONE:
                        default:
                            type = State.Type.NONE_ACCEPTED;
                        }

                        mail.setStateType(type);
                    }

                    sender.sendMail(mail, session);
                }
            }

            return retval;
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        }
    }

    @Override
    public Appointment[] updateAppointmentObject(final CalendarDataObject cdao, final int inFolder, final Date clientLastModified) throws OXException {
        return updateAppointmentObject(cdao, inFolder, clientLastModified, true);
    }

    @Override
    public CalendarDataObject getObjectById(final int objectId) throws OXException, SQLException {
        return delegate.getObjectById(objectId);
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

    @Override
    public Appointment[] updateAppointmentObject(final CalendarDataObject cdao, final int inFolder, final Date clientLastModified, final boolean checkPermissions) throws OXException {
        try {
            final CalendarDataObject original = getObjectById(cdao.getObjectID());
            final Appointment[] retval = delegate.updateAppointmentObject(cdao, inFolder, clientLastModified, checkPermissions);
            if (retval == null || retval.length == 0) {
                final CalendarDataObject reloaded = getObjectById(cdao.getObjectID());
                if (cdao.containsNotification()) {
                    reloaded.setNotification(cdao.getNotification());
                }
                calculateExceptionPosition(cdao, original, false);
                generateUpdateMail(inFolder, original, reloaded);
            }
            return retval;
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        }
    }

    private void generateUpdateMail(final int inFolder, final CalendarDataObject original, final CalendarDataObject reloaded) throws OXException {
        final ITipMailGenerator generator = generators.create(original, reloaded, session, onBehalfOf(inFolder));
        final List<NotificationParticipant> recipients = generator.getRecipients();
        for (final NotificationParticipant notificationParticipant : recipients) {
            NotificationMail mail;
            mail = generator.generateUpdateMailFor(notificationParticipant);
            if (mail != null) {
                if (mail.getStateType() == null) {
                    mail.setStateType(State.Type.MODIFIED);
                }
                sender.sendMail(mail, session);
            }
        }
    }

    private void calculateExceptionPosition(final CalendarDataObject source, final CalendarDataObject target, final boolean isDelete) throws OXException {
        try {
            boolean isException = target.isException();
            if (source.containsRecurrenceDatePosition()) {
                target.setRecurrenceDatePosition(source.getRecurrenceDatePosition());
            }
            if (source.containsRecurrencePosition()) {
                target.setRecurrencePosition(source.getRecurrencePosition());
            }
            if (!isDelete && target.isException()) {
                return;
            }
            if (target.containsRecurrenceDatePosition() && target.getRecurrenceDatePosition() != null || target.containsRecurrencePosition() && target.getRecurrencePosition() != 0) {
                if (!(target.getRecurrenceDatePosition() != null && target.getRecurrencePosition() != 0)) {
                    calendarCollection.setRecurrencePositionOrDateInDAO(target, true);
                }
                RecurringResultsInterface recResults;
                if (!isException) {
                    recResults = calendarCollection.calculateRecurring(target, 0, 0, target.getRecurrencePosition());
                    if (recResults == null) {
                        return;
                    }
                    final RecurringResultInterface recurringResult = recResults.getRecurringResult(0);
                    target.setStartDate(new Date(recurringResult.getStart()));
                    target.setEndDate(new Date(recurringResult.getEnd()));
                }
            }
        } catch (final OXException x) {
            // IGNORE: This is all best effort
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.AppointmentSQLInterface#count(int)
     */
    @Override
    public int countObjectsInFolder(int folderId) throws OXException {
        return delegate.countObjectsInFolder(folderId);
    }

}
