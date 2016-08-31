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

package com.openexchange.event.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.context.ContextService;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.folder.FolderService;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.pns.DefaultPushNotification;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationField;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.pns.PushNotifications;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * EventClient
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class EventClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventClient.class);

    public static final int CREATED = 5;
    public static final int CHANGED = 6;
    public static final int DELETED = 7;
    public static final int MOVED = 8;
    public static final int CONFIRM_ACCEPTED = 9;
    public static final int CONFIRM_DECLINED = 10;
    public static final int CONFIRM_TENTATIVE = 11;
    public static final int CONFIRM_WAITING = 11;

    private final Session session;

    private final int userId;

    private final int contextId;

    public EventClient(final Session session) {
        this.session = session;
        userId = session.getUserId();
        contextId = session.getContextId();
    }

    public void create(final Appointment appointment) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointment.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            create(appointment, folderObj);
        }
    }

    public void create(final Appointment appointment, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { appointment }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.INSERT, Types.APPOINTMENT, appointment, null, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(appointment, CREATED, session);
        EventQueue.add(eventObject);

        PushNotificationService pushNotificationService = ServerServiceRegistry.getInstance().getService(PushNotificationService.class);
        if (null != pushNotificationService) {
            for (Map.Entry<Integer, Set<Integer>> entry : affectedUsers.entrySet()) {
                int userId = entry.getKey().intValue();
                Integer folderId = entry.getValue().iterator().next();
                Date[] startAndEndDate = determineStartAndEndDate(appointment);

                Map<String, Object> messageData = PushNotifications.messageDataBilder()
                    .put(PushNotificationField.ID, Integer.valueOf(appointment.getObjectID()))
                    .put(PushNotificationField.FOLDER, folderId)
                    .put(PushNotificationField.APPOINTMENT_TITLE, appointment.getTitle())
                    .put(PushNotificationField.APPOINTMENT_LOCATION, appointment.getLocation())
                    .put(PushNotificationField.APPOINTMENT_START_DATE, startAndEndDate[0])
                    .put(PushNotificationField.APPOINTMENT_END_DATE, startAndEndDate[1])
                    .build();

                PushNotification notification = DefaultPushNotification.builder()
                    .contextId(contextId)
                    .userId(userId)
                    .topic(KnownTopic.CALENDAR_NEW.getName())
                    .messageData(messageData)
                    .build();
                pushNotificationService.handle(notification);
            }
        }
    }

    /**
     * Determines specified appointment's end date
     *
     * @param appointment The appointment
     * @return The end date
     */
    private Date[] determineStartAndEndDate(Appointment appointment) {
        if (appointment.getRecurrenceType() == CalendarObject.NONE) {
            return new Date[] { appointment.getStartDate(), appointment.getEndDate() };
        }

        CalendarCollectionService calColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        RecurringResultsInterface recuResults = null;
        try {
            recuResults = calColl.calculateFirstRecurring(appointment);
        } catch (final OXException e) {
            LOG.error("Failed calculating recurrence {}", appointment.getObjectID(), e);
        }
        if (recuResults != null && recuResults.size() == 1) {
            return new Date[] { new Date(recuResults.getRecurringResult(0).getStart()), new Date(recuResults.getRecurringResult(0).getEnd()) };
        }

        LOG.warn("Failed loading first recurring appointment from appointment object: {} / {}\n\n\n", appointment.getRecurrenceType(), appointment.getObjectID());
        return new Date[] { appointment.getStartDate(), appointment.getEndDate() };
    }

    public void modify(final Appointment appointment) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointment.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            modify(null, appointment, folderObj);
        }
    }

    public void modify(final Appointment oldAppointment, final Appointment newAppointment, final FolderObject folderObj) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldAppointment, newAppointment }, new FolderObject[] { folderObj });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.UPDATE, Types.APPOINTMENT, newAppointment, oldAppointment, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointment, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void accepted(final Appointment appointment) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointment.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            accepted(null, appointment, folderObj);
        }
    }

    public void accepted(final Appointment oldAppointment, final Appointment newAppointment, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldAppointment, newAppointment }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.CONFIRM_ACCEPTED, Types.APPOINTMENT, newAppointment, oldAppointment, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/accepted", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointment, CONFIRM_ACCEPTED, session);
        EventQueue.add(eventObject);
    }

    public void declined(final Appointment appointment) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointment.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            declined(null, appointment, folderObj);
        }
    }

    public void declined(final Appointment oldAppointment, final Appointment newAppointment, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldAppointment, newAppointment }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.CONFIRM_DECLINED, Types.APPOINTMENT, newAppointment, oldAppointment, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/declined", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointment, CONFIRM_DECLINED, session);
        EventQueue.add(eventObject);
    }

    public void tentative(final Appointment appointment) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointment.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            tentative(null, appointment, folderObj);
        }
    }

    public void tentative(final Appointment oldAppointment, final Appointment newAppointment, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldAppointment, newAppointment }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.CONFIRM_TENTATIVE, Types.APPOINTMENT, newAppointment, oldAppointment, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/tentative", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointment, CONFIRM_TENTATIVE, session);
        EventQueue.add(eventObject);
    }

    public void waiting(final Appointment appointment) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointment.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            waiting(null, appointment, folderObj);
        }
    }

    public void waiting(final Appointment oldAppointment, final Appointment newAppointment, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldAppointment, newAppointment }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.CONFIRM_WAITING, Types.APPOINTMENT, newAppointment, oldAppointment, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/waiting", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointment, CONFIRM_WAITING, session);
        EventQueue.add(eventObject);
    }

    public void delete(final Appointment appointment) throws OXException, OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointment.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            delete(appointment, folderObj);
        }
    }

    public void delete(final Appointment appointment, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { appointment }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.DELETE, Types.APPOINTMENT, appointment, null, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(appointment, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void move(final Appointment appointment, final FolderObject sourceFolder, final FolderObject destinationFolder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { appointment }, new FolderObject[] { sourceFolder, destinationFolder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.MOVE, Types.APPOINTMENT, appointment, null, sourceFolder, destinationFolder, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/move", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(appointment, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void create(final Task task, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { task }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.INSERT, Types.TASK, task, null, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(task, CREATED, session);
        EventQueue.add(eventObject);
    }

    public void modify(final Task oldTask, final Task newTask, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldTask, newTask }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.UPDATE, Types.TASK, newTask, oldTask, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void accept(final Task oldTask, final Task newTask) throws  OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = newTask.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folder = getFolder(folderId, ctx);
            accept(oldTask, newTask, folder);
        }
    }

    public void accept(final Task oldTask, final Task newTask, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldTask, newTask }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.CONFIRM_ACCEPTED, Types.TASK, newTask, oldTask, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/accepted", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CONFIRM_ACCEPTED, session);
        EventQueue.add(eventObject);
    }

    public void declined(final Task oldTask, final Task newTask) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = newTask.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folder = getFolder(folderId, ctx);
            declined(oldTask, newTask, folder);
        }
    }

    public void declined(final Task oldTask, final Task newTask, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldTask, newTask }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.CONFIRM_DECLINED, Types.TASK, newTask, oldTask, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/declined", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CONFIRM_DECLINED, session);
        EventQueue.add(eventObject);
    }

    public void tentative(final Task oldTask, final Task newTask) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = newTask.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folder = getFolder(folderId, ctx);
            tentative(oldTask, newTask, folder);
        }
    }

    public void tentative(final Task oldTask, final Task newTask, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { oldTask, newTask }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.CONFIRM_TENTATIVE, Types.TASK, newTask, oldTask, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/tentative", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CONFIRM_TENTATIVE, session);
        EventQueue.add(eventObject);
    }

    public void delete(final Task task) throws OXException, OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);


        final int folderId = task.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folder = getFolder(folderId, ctx);
            delete(task, folder);
        }
    }

    public void delete(final Task task, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { task }, new FolderObject[] { folder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.DELETE, Types.TASK, task, null, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(task, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void move(final Task task, final FolderObject sourceFolder, final FolderObject destinationFolder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new CalendarObject[] { task }, new FolderObject[] { sourceFolder, destinationFolder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.MOVE, Types.TASK, task, null, sourceFolder, destinationFolder, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/move", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(task, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void create(final Contact contact) throws OXException, OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = contact.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folder = getFolder(folderId, ctx);
            create(contact, folder);
        }
    }

    public void create(final Contact contact, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { folder }, contact.getParentFolderID());
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.INSERT, Types.CONTACT, contact, null, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(contact, CREATED, session);
        EventQueue.add(eventObject);
    }

    public void modify(final Contact oldContact, final Contact newContact, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { folder }, oldContact.getParentFolderID(), newContact.getParentFolderID());
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.UPDATE, Types.CONTACT, newContact, oldContact, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newContact, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void delete(final Contact contact) throws OXException, OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = contact.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folder = getFolder(folderId, ctx);
            delete(contact, folder);
        }
    }

    public void delete(final Contact contact, final FolderObject folder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { folder }, contact.getParentFolderID());
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.DELETE, Types.CONTACT, contact, null, folder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(contact, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void move(final Contact contact, final FolderObject sourceFolder, final FolderObject destinationFolder) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { sourceFolder, destinationFolder }, contact.getParentFolderID());
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.MOVE, Types.CONTACT, contact, null, sourceFolder, destinationFolder, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/move", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(contact, MOVED, session);
        EventQueue.add(eventObject);
    }

    /**
     * Raises a folder "create" event.
     *
     * @param folder The created folder
     */
    public void create(final FolderObject folder) throws OXException, OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = folder.getParentFolderID();
        if (folderId > 0) {
            final FolderObject parentFolderObj = getFolder(folderId, ctx);
            create(folder, parentFolderObj);
        }
    }

    /**
     * Raises a folder "create" event.
     *
     * @param folder The created folder
     * @param parentFolder The parent folder
     * @throws OXException
     */
    public void create(final FolderObject folder, final FolderObject parentFolder) throws OXException {
        create(folder, parentFolder, null);
    }

    /**
     * Raises a folder "create" event.
     *
     * @param folder The created folder
     * @param parentFolder The parent folder
     * @param folderPath The full path of the folder down to the root folder
     * @throws OXException
     */
    public void create(final FolderObject folder, final FolderObject parentFolder, String[] folderPath) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { folder, parentFolder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.INSERT, Types.FOLDER, folder, null, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/folder/insert", ht);
        triggerEvent(event);
        if (null != folder && FolderObject.INFOSTORE == folder.getModule()) {
            triggerEvent(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(folder, parentFolder, folderPath)));
        }

        final EventObject eventObject = new EventObject(folder, CREATED, session).setNoDelay(true);
        EventQueue.add(eventObject);
    }

    /**
     * Raises a folder "modify" event.
     *
     * @param oldFolder The old folder
     * @param newFolder The new folder
     * @param parentFolder The parent folder
     * @throws OXException
     */
    public void modify(final FolderObject oldFolder, final FolderObject newFolder, final FolderObject parentFolder) throws OXException {
        modify(oldFolder, newFolder, parentFolder, null);
    }

    /**
     * Raises a folder "modify" event.
     *
     * @param oldFolder The old folder
     * @param newFolder The new folder
     * @param parentFolder The parent folder
     * @param folderPath The full path of the folder down to the root folder
     * @throws OXException
     */
    public void modify(final FolderObject oldFolder, final FolderObject newFolder, final FolderObject parentFolder, String[] folderPath) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { oldFolder, newFolder, parentFolder }, oldFolder.getParentFolderID(), newFolder.getParentFolderID());
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.UPDATE, Types.FOLDER, newFolder, oldFolder, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/folder/update", ht);
        triggerEvent(event);
        if (null != newFolder && FolderObject.INFOSTORE == newFolder.getModule()) {
            Dictionary<String, Object> properties = getEventProperties(newFolder, parentFolder, folderPath);
            if (null != oldFolder && oldFolder.getParentFolderID() != newFolder.getParentFolderID()) {
                properties.put(FileStorageEventConstants.OLD_PARENT_FOLDER_ID, String.valueOf(oldFolder.getParentFolderID()));
            }
            triggerEvent(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, properties));
        }

        final EventObject eventObject = new EventObject(newFolder, CHANGED, session).setNoDelay(true);
        EventQueue.add(eventObject);
    }

    /**
     * Raises a folder "delete" event.
     *
     * @param folder The folder
     * @throws OXException
     */
    public void delete(final FolderObject folder) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);
        final int folderId = folder.getParentFolderID();
        if (folderId > 0) {
            FolderObject parentFolderObj = null;
            try {
                parentFolderObj = getFolder(folderId, ctx);
            } catch (final OXException exc) {
                if (exc.isGeneric(Generic.NO_PERMISSION)) {
                    LOG.error("cannot load folder", exc);
                } else {
                    throw exc;
                }
            }
            delete(folder, parentFolderObj);
        }
    }

    /**
     * Raises a folder "delete" event.
     *
     * @param folder The folder
     * @param parentFolder The parent folder
     * @throws OXException
     */
    public void delete(final FolderObject folder, final FolderObject parentFolder) throws OXException {
        delete(folder, parentFolder, null);
    }

    /**
     * Raises a folder "delete" event.
     *
     * @param folder The folder
     * @param parentFolder The parent folder
     * @param folderPath The full path of the folder down to the root folder
     * @throws OXException
     */
    public void delete(final FolderObject folder, final FolderObject parentFolder, String[] folderPath) throws OXException {
        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { folder, parentFolder });
        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.DELETE, Types.FOLDER, folder, null, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/folder/delete", ht);
        triggerEvent(event);
        if (null != parentFolder && FolderObject.INFOSTORE == parentFolder.getModule()) {
            triggerEvent(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, getEventProperties(folder, parentFolder, folderPath)));
        }

        final EventObject eventObject = new EventObject(folder, DELETED, session).setNoDelay(true);
        EventQueue.add(eventObject);
    }

//    public void create(final DocumentMetadata document) throws OXException, OXException {
//        final Context ctx = ContextStorage.getInstance().getContext(contextId);
//
//        final long folderId = document.getFolderId();
//        if (folderId > 0) {
//            final FolderObject folder = getFolder((int)folderId, ctx);
//            create(document, folder);
//        }
//    }
//
//    public void create(final DocumentMetadata document, final FolderObject folder) throws OXException {
//        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { folder }, (int) document.getFolderId());
//        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.INSERT, Types.INFOSTORE, document, null, folder, null, session);
//
//        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
//        ht.put(CommonEvent.EVENT_KEY, genericEvent);
//
//        final Event event = new Event("com/openexchange/groupware/infostore/insert", ht);
//        triggerEvent(event);
//
//        final EventObject eventObject = new EventObject(document, CREATED, session);
//        EventQueue.add(eventObject);
//    }
//
//    public void modify(final DocumentMetadata document) throws OXException, OXException {
//        final Context ctx = ContextStorage.getInstance().getContext(contextId);
//
//        final long folderId = document.getFolderId();
//        if (folderId > 0) {
//            final FolderObject folder = getFolder((int)folderId, ctx);
//            modify(null, document, folder);
//        }
//    }
//
//    public void modify(final DocumentMetadata oldDocument, final DocumentMetadata newDocument, final FolderObject folder) throws OXException {
//        final Map<Integer, Set<Integer>> affectedUsers;
//        if (null == oldDocument) {
//            affectedUsers = getAffectedUsers(new FolderObject[] { folder }, (int) newDocument.getFolderId());
//        } else {
//            affectedUsers = getAffectedUsers(new FolderObject[] { folder }, (int) oldDocument.getFolderId(), (int) newDocument.getFolderId());
//        }
//        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.UPDATE, Types.INFOSTORE, newDocument, oldDocument, folder, null, session);
//
//        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
//        ht.put(CommonEvent.EVENT_KEY, genericEvent);
//
//        final Event event = new Event("com/openexchange/groupware/infostore/update", ht);
//        triggerEvent(event);
//
//        final EventObject eventObject = new EventObject(newDocument, CHANGED, session);
//        EventQueue.add(eventObject);
//    }
//
//    public void delete(final DocumentMetadata document) throws OXException, OXException {
//        final Context ctx = ContextStorage.getInstance().getContext(contextId);
//        //FolderSQLInterface folderSql = new RdbFolderSQLInterface(session, ctx);
//
//        final long folderId = document.getFolderId();
//        if (folderId > 0) {
//            final FolderObject folder = getFolder((int)folderId, ctx);
//            delete(document, folder);
//        }
//    }
//
//    public void delete(final DocumentMetadata document, final FolderObject folder) throws OXException {
//        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { folder }, (int) document.getFolderId());
//        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.DELETE, Types.INFOSTORE, document, null, folder, null, session);
//
//        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
//        ht.put(CommonEvent.EVENT_KEY, genericEvent);
//
//        final Event event = new Event("com/openexchange/groupware/infostore/delete", ht);
//        triggerEvent(event);
//
//        final EventObject eventObject = new EventObject(document, DELETED, session);
//        EventQueue.add(eventObject);
//    }
//
//    public void move(final DocumentMetadata document, final FolderObject sourceFolder, final FolderObject destinationFolder) throws OXException {
//        final Map<Integer, Set<Integer>> affectedUsers = getAffectedUsers(new FolderObject[] { sourceFolder, destinationFolder }, (int) document.getFolderId());
//        final CommonEvent genericEvent = new CommonEventImpl(contextId, userId, unmodifyable(affectedUsers), CommonEvent.MOVE, Types.INFOSTORE, document, null, sourceFolder, destinationFolder, session);
//
//        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
//        ht.put(CommonEvent.EVENT_KEY, genericEvent);
//
//        final Event event = new Event("com/openexchange/groupware/infostore/move", ht);
//        triggerEvent(event);
//
//        final EventObject eventObject = new EventObject(document, MOVED, session);
//        EventQueue.add(eventObject);
//    }

    protected void triggerEvent(final Event event) throws OXException {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (eventAdmin == null) {
            throw new OXException().setLogMessage("event service not available");
        }
        eventAdmin.postEvent(event);
    }

    /**
     * Constructs the properties for a file storage folder event.
     *
     * @param folder The folder
     * @return The event properties
     */
    private Dictionary<String, Object> getEventProperties(FolderObject folder) {
        return getEventProperties(folder, null);
    }

    /**
     * Constructs the properties for a file storage folder event.
     *
     * @param folder The folder
     * @param parentFolder The parent folder
     * @return The event properties
     */
    private Dictionary<String, Object> getEventProperties(FolderObject folder, FolderObject parentFolder) {
        return getEventProperties(folder, parentFolder, null);
    }

    /**
     * Constructs the properties for a file storage folder event.
     *
     * @param folder The folder
     * @param parentFolder The parent folder
     * @param folderPath The folder path
     * @return The event properties
     */
    private Dictionary<String, Object> getEventProperties(FolderObject folder, FolderObject parentFolder, String[] folderPath) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put(FileStorageEventConstants.SESSION, session);
        properties.put(FileStorageEventConstants.FOLDER_ID, String.valueOf(folder.getObjectID()));
        properties.put(FileStorageEventConstants.ACCOUNT_ID, "infostore");
        properties.put(FileStorageEventConstants.SERVICE, "com.openexchange.infostore");
        if (null != parentFolder) {
            properties.put(FileStorageEventConstants.PARENT_FOLDER_ID, String.valueOf(parentFolder.getObjectID()));
        }
        if (null != folderPath) {
            properties.put(FileStorageEventConstants.FOLDER_PATH, folderPath);
        }
        return properties;
    }

    private FolderObject getFolder(final int folderId, final Context ctx) throws OXException {
        return new OXFolderAccess(ctx).getFolderObject(folderId);
    }

    private Map<Integer, Set<Integer>> getAffectedUsers(final FolderObject[] folders, final int... folderIds) throws OXException {
        final Map<Integer, Set<Integer>> retval = getAffectedUsers(folders);
        for (final int folderId : folderIds) {
            getFolderSet(retval, userId).add(I(folderId));
        }
        return retval;
    }

    private Map<Integer, Set<Integer>> getAffectedUsers(final FolderObject[] folders) throws OXException {
        final Map<Integer, Set<Integer>> retval = new HashMap<Integer, Set<Integer>>();
        retval.put(I(userId), new HashSet<Integer>());
        for (final FolderObject folder : folders) {
        	addFolderToAffectedMap(retval, folder);
        }
        return retval;
    }

    private Map<Integer, Set<Integer>> getAffectedUsers(final CalendarObject[] objects, final FolderObject[] folders) throws OXException {
        final Map<Integer, Set<Integer>> retval = getAffectedUsers(folders);
        for (final CalendarObject object : objects) {
            if (null != object) {
                getFolderSet(retval, userId).add(I(object.getParentFolderID()));
                UserParticipant[] participants = object.getUsers();
                if (null != participants) {
                    for (final UserParticipant participant : participants) {
                        final int participantId = participant.getIdentifier();
                        if (Participant.NO_ID == participantId) {
                            continue;
                        }
                        getFolderSet(retval, participantId);
                        final int folderId = participant.getPersonalFolderId();
                        if (UserParticipant.NO_PFID == folderId || 0 == folderId) {
                            continue;
                        }
                        final FolderService folderService = ServerServiceRegistry.getInstance().getService(FolderService.class, true);
                        final FolderObject folder = folderService.getFolderObject(folderId, contextId);
                        addFolderToAffectedMap(retval, folder);
                    }
                }
            }
        }
        return retval;
    }

    private void addFolderToAffectedMap(final Map<Integer, Set<Integer>> retval, final FolderObject folder) throws OXException {
        for (final OCLPermission permission : folder.getPermissions()) {
            if (permission.isFolderVisible()) {
                if (permission.isGroupPermission()) {
                    final GroupService groupService = ServerServiceRegistry.getInstance().getService(GroupService.class, true);
                    final Group group = groupService.getGroup(getContext(contextId), permission.getEntity());
                    for (final int groupMember : group.getMember()) {
                        getFolderSet(retval, groupMember).add(I(folder.getObjectID()));
                    }
                } else {
                    getFolderSet(retval, permission.getEntity()).add(I(folder.getObjectID()));
                }
            }
        }
    }

    private static Set<Integer> getFolderSet(final Map<Integer, Set<Integer>> map, final int userId) {
        Set<Integer> retval = map.get(I(userId));
        if (null == retval) {
            retval = new HashSet<Integer>();
            map.put(I(userId), retval);
        }
        return retval;
    }

    private static Context getContext(final int contextId) throws OXException {
        final ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class, true);
        return contextService.getContext(contextId);
    }

    private static Map<Integer, Set<Integer>> unmodifyable(final Map<Integer, Set<Integer>> map) {
        for (final Entry<Integer, Set<Integer>> entry : map.entrySet()) {
            entry.setValue(Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }
}
