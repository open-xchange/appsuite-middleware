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

package com.openexchange.chronos.itip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.common.mapping.EventUpdateImpl;
import com.openexchange.chronos.itip.generators.ITipMailGenerator;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationMailGenerator;
import com.openexchange.chronos.itip.generators.NotificationMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.State;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;

/**
 * {@link AppointmentNotificationPool}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppointmentNotificationPool implements AppointmentNotificationPoolService, Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentNotificationPool.class);

    // TODO: Keep shared folder owner, if possible

    private static final int MINUTES = 60000;

    private int detailChangeInterval = 2 * MINUTES;
    private int stateChangeInterval = 10 * MINUTES;
    private int priorityInterval = 15 * MINUTES;

    private final NotificationMailGeneratorFactory generatorFactory;
    private final MailSenderService notificationMailer;

    private final ReentrantLock lock = new ReentrantLock();

    private final Map<Integer, Map<String, QueueItem>> items = new HashMap<Integer, Map<String, QueueItem>>();

    private final Map<Integer, Map<NotificationParticipant, List<Event>>> sent = new HashMap<Integer, Map<NotificationParticipant, List<Event>>>();

    public AppointmentNotificationPool(TimerService timer, NotificationMailGeneratorFactory generatorFactory, MailSenderService notificationMailer, int detailChangeInterval, int stateChangeInterval, int priorityInterval) {
        this.generatorFactory = generatorFactory;
        this.notificationMailer = notificationMailer;

        this.detailChangeInterval = detailChangeInterval;
        this.stateChangeInterval = stateChangeInterval;
        this.priorityInterval = priorityInterval;

        timer.scheduleAtFixedRate(this, 1000, Math.min(stateChangeInterval, Math.min(detailChangeInterval, priorityInterval)) / 2);
    }

    @Override
    public void run() {
        try {
            lock.lock();

            Collection<QueueItem> allItems = allItems();
            for (QueueItem item : allItems) {
                tick(item.getContextId(), item.getEventId(), false);
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("", t);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void enqueue(Event original, Event newAppointment, Session session, int sharedFolderOwner, CalendarUser principal) throws OXException {
        if (original == null) {
            throw new NullPointerException("Please specify an original appointment, a new appointment and a session");
        }

        if (newAppointment == null) {
            throw new NullPointerException("Please specify an original appointment, a new appointment and a session");
        }

        if (session == null) {
            throw new NullPointerException("Please specify an original appointment, a new appointment and a session");
        }

        try {
            lock.lock();
            item(session.getContextId(), original.getId()).remember(original, newAppointment, session, sharedFolderOwner, principal);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void fasttrack(Event appointment, Session session) throws OXException {
        try {
            lock.lock();
            tick(session.getContextId(), appointment.getId(), true);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void aware(Event appointment, NotificationParticipant recipient, Session session) {
        Map<NotificationParticipant, List<Event>> participants = sent.get(session.getContextId());
        if (participants == null) {
            participants = new HashMap<NotificationParticipant, List<Event>>();
            sent.put(session.getContextId(), participants);
        }

        List<Event> appointments = participants.get(recipient);
        if (appointments == null) {
            appointments = new ArrayList<Event>();
            participants.put(recipient, appointments);
        }

        appointments.remove(appointment); // Stops working, if equals() depends on more than the objectId
        appointments.add(appointment);
    }

    /**
     * Searches for an Appointment about a recipient was already informed. Removes this appointments from memory.
     *
     * @param participant
     * @param appointment
     * @param contextId
     * @return The appointment, null if not found.
     */
    private Event removeFromSent(NotificationParticipant participant, Event appointment, int contextId) {
        Map<NotificationParticipant, List<Event>> participants = sent.get(contextId);
        if (participants == null) {
            return null;
        }

        List<Event> appointments = participants.get(participant);
        if (appointments == null) {
            return null;
        }

        Event retval = null;
        for (Event app : appointments) {
            if (app.getId().equals(appointment.getId())) {
                retval = app;
            }
        }
        appointments.remove(retval);

        if (appointments.isEmpty()) {
            participants.remove(participant);
        }
        if (participants.isEmpty()) {
            sent.remove(contextId);
        }

        return retval;
    }

    private void clearSentItems(int contextId) {
        sent.remove(contextId);
    }

    private void tick(int contextId, String objectID, boolean force) {
        try {
            HandlingSuggestion handlingSuggestion = item(contextId, objectID).tick(force);
            if (handlingSuggestion == HandlingSuggestion.DONE) {
                drop(contextId, objectID);
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("", t);
            drop(contextId, objectID);
        }
    }

    @Override
    public void drop(Event appointment, Session session) throws OXException {
        drop(session.getContextId(), appointment.getId());
    }

    private Collection<QueueItem> allItems() {
        List<QueueItem> allItems = new LinkedList<QueueItem>();
        for (Map<String, QueueItem> contextMaps : items.values()) {
            allItems.addAll(contextMaps.values());
        }
        return allItems;
    }

    private QueueItem item(int contextId, String objectID) {
        Map<String, QueueItem> contextMap = items.get(contextId);
        if (contextMap == null) {
            contextMap = new HashMap<String, QueueItem>();
            QueueItem queueItem = new QueueItem();
            contextMap.put(objectID, queueItem);
            items.put(contextId, contextMap);
            return queueItem;
        }
        QueueItem queueItem = contextMap.get(objectID);
        if (queueItem == null) {
            queueItem = new QueueItem();
            contextMap.put(objectID, queueItem);
        }
        return queueItem;
    }

    private void drop(int contextId, String objectID) {
        Map<String, QueueItem> contextMap = items.get(contextId);
        if (contextMap == null) {
            clearSentItems(contextId);
            return;
        }
        contextMap.remove(objectID);
        if (contextMap.isEmpty()) {
            items.remove(contextId);
            clearSentItems(contextId);
        }
    }

    private static final class Update {

        private final Event oldAppointment;
        private final Event newAppointment;
        private final Session session;
        private final long timestamp;
        private ITipEventUpdate diff;
        private int sharedFolderOwner = -1;

        public Update(Event oldAppointment, Event newAppointment, Session session, int sharedFolderOwner) {
            this.oldAppointment = oldAppointment;
            this.newAppointment = newAppointment;
            this.session = session;
            this.sharedFolderOwner = sharedFolderOwner;
            this.timestamp = System.currentTimeMillis();
        }

        public Session getSession() {
            return session;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Event getOldEvent() {
            return oldAppointment;
        }

        public Event getNewEvent() {
            return newAppointment;
        }

        public int getSharedFolderOwner() {
            return sharedFolderOwner;
        }

        public ITipEventUpdate getDiff() throws OXException {
            if (diff == null) {
                diff = new ITipEventUpdate(new EventUpdateImpl(oldAppointment, newAppointment, true, NotificationMailGenerator.DEFAULT_SKIP));
            }
            return diff;
        }

        public PartitionIndex getPartitionIndex() {
            return new PartitionIndex(session.getUserId(), sharedFolderOwner);
        }
    }

    private static enum HandlingSuggestion {
        KEEP, DONE
    }

    private final class QueueItem {

        private Event original;
        private Event mostRecent;
        private long newestTime;
        private long lastKnownStartDateForNextOccurrence;
        private Session session;
        private CalendarUser principal;

        private final LinkedList<Update> updates = new LinkedList<Update>();

        public void remember(Event original, Event newAppointment, Session session, int sharedFolderOwner, CalendarUser principal) throws OXException {
            this.principal = principal;
            if (this.original == null) {
                this.original = original;
                this.mostRecent = newAppointment;
                this.session = session;
            }
            if (this.session.getUserId() != original.getOrganizer().getEntity() && session.getUserId() == original.getOrganizer().getEntity()) {
                this.session = session;
            }
            this.mostRecent = newAppointment;
            this.newestTime = System.currentTimeMillis();
            this.lastKnownStartDateForNextOccurrence = newAppointment.getStartDate().getTimestamp();
            Update update = new Update(original, newAppointment, session, sharedFolderOwner);
            updates.add(update);
            if (update.getDiff().containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE, EventField.LOCATION, EventField.RECURRENCE_RULE, EventField.RECURRENCE_ID })) {
                // Participant State has been reset
                // Purge state only changes
                Iterator<Update> iterator = updates.iterator();
                while (iterator.hasNext()) {
                    Update u = iterator.next();
                    if (u.getDiff().isAboutStateChangesOnly()) {
                        iterator.remove();
                    }
                }
                this.original = updates.get(0).getOldEvent();
                // Apply new reset states to original appointment
                copyParticipantStates(newAppointment, this.original);
            }
        }

        public HandlingSuggestion tick(boolean force) throws OXException {
            if (original == null) {
                return HandlingSuggestion.DONE;
            }
            // Diff most recent and original version
            ITipEventUpdate overallDiff = new ITipEventUpdate(new EventUpdateImpl(original, mostRecent, true, NotificationMailGenerator.DEFAULT_SKIP));

            if (overallDiff.isAboutStateChangesOnly()) {
                if (!force && getInterval() < stateChangeInterval && getIntervalToStartDate() > priorityInterval) {
                    return HandlingSuggestion.KEEP;
                }
                notifyAllParticipantsAboutOverallChanges();
                return HandlingSuggestion.DONE;
            } else if (overallDiff.isAboutDetailChangesOnly()) {
                if (!force && getInterval() < detailChangeInterval && getIntervalToStartDate() > priorityInterval) {
                    return HandlingSuggestion.KEEP;
                }
                notifyInternalParticipantsAboutDetailChangesAsIndividualUsers();
                notifyExternalParticipantsAboutOverallChangesAsOrganizer();
                proposeChangesToExternalOrganizer();
                return HandlingSuggestion.DONE;
            } else {
                if (!force && getInterval() < Math.min(detailChangeInterval, stateChangeInterval) && getIntervalToStartDate() > priorityInterval) {
                    return HandlingSuggestion.KEEP;
                }
                notifyInternalParticipantsAboutDetailChangesAsIndividualUsers();
                notifyInternalParticipantsAboutStateChanges();
                notifyExternalParticipantsAboutOverallChangesAsOrganizer();
                proposeChangesToExternalOrganizer();
                return HandlingSuggestion.DONE;
            }
        }

        private void notifyAllParticipantsAboutOverallChanges() throws OXException {
            ITipMailGenerator generator = generatorFactory.create(original, mostRecent, session, -1, principal);
            if (moreThanOneUserActed()) {
                generator.noActor();
            }
            List<NotificationParticipant> recipients = generator.getRecipients();
            for (NotificationParticipant participant : recipients) {
                if (isAlreadyInformed(participant, mostRecent, session.getContextId())) {
                    continue; // Skip this participant. He was already informed about the exact same Appointment.
                }

                NotificationMail mail = generator.generateUpdateMailFor(participant);
                if (mail != null && mail.getStateType() != State.Type.NEW) {
                    notificationMailer.sendMail(mail, session, principal);
                }
            }
        }

        private void notifyInternalParticipantsAboutOverallChanges() throws OXException {
            ITipMailGenerator generator = generatorFactory.create(original, mostRecent, session, -1, principal);
            if (moreThanOneUserActed()) {
                generator.noActor();
            }
            List<NotificationParticipant> recipients = generator.getRecipients();
            for (NotificationParticipant participant : recipients) {
                if (!participant.isExternal()) {
                    if (isAlreadyInformed(participant, mostRecent, session.getContextId())) {
                        continue; // Skip this participant. He was already informed about the exact same Appointment.
                    }

                    NotificationMail mail = generator.generateUpdateMailFor(participant);
                    if (mail != null && mail.getStateType() != State.Type.NEW) {
                        notificationMailer.sendMail(mail, session, principal);
                    }
                }
            }
        }

        private boolean isAlreadyInformed(NotificationParticipant participant, Event mostRecent, int contextId) throws OXException {
            Event alreadySent = removeFromSent(participant, mostRecent, contextId);
            if (alreadySent != null) {
                ITipEventUpdate diff = new ITipEventUpdate(new EventUpdateImpl(alreadySent, mostRecent, true, (EventField[]) null));
                return diff.getUpdatedFields().isEmpty();
            }
            return false;
        }

        private boolean moreThanOneUserActed() {
            int userId = session.getUserId();
            for (Update update : updates) {
                if (update.getSession().getUserId() != userId) {
                    return true;
                }
            }
            return false;
        }

        // TODO: What about combined state changes and detail changes? The user should send a mail about both and the state change should be omitted in the state change summary.
        private void notifyInternalParticipantsAboutDetailChangesAsIndividualUsers() throws OXException {
            if (!moreThanOneUserActed()) {
                notifyInternalParticipantsAboutOverallChanges();
                return;
            }
            Map<PartitionIndex, Update[]> partitions = new HashMap<PartitionIndex, Update[]>();
            for (Update update : updates) {
                if (update.getDiff().isAboutCertainParticipantsStateChangeOnly(Integer.toString(update.getSession().getUserId()))) {
                    continue;
                }
                Update[] partition = partitions.get(update.getPartitionIndex());
                if (partition == null) {
                    partition = new Update[2];
                    partitions.put(update.getPartitionIndex(), partition);
                    partition[0] = update;
                }
                partition[1] = update;
            }
            List<Update[]> userScopedUpdates = new ArrayList<Update[]>(partitions.values());
            Collections.sort(userScopedUpdates, new Comparator<Update[]>() {

                @Override
                public int compare(Update[] o1, Update[] o2) {
                    return (int) (o1[1].getTimestamp() - o2[1].getTimestamp());
                }
            });

            for (Update[] userScopedUpdate : userScopedUpdates) {
                Session session = userScopedUpdate[1].getSession();
                Event oldAppointment = userScopedUpdate[0].getOldEvent();
                Event newAppointment = userScopedUpdate[1].getNewEvent();
                ITipMailGenerator generator = generatorFactory.create(oldAppointment, newAppointment, session, userScopedUpdate[0].getSharedFolderOwner(), principal);
                List<NotificationParticipant> recipients = generator.getRecipients();
                for (NotificationParticipant participant : recipients) {
                    if (participant.isExternal() && !participant.hasRole(ITipRole.ORGANIZER)) {
                        continue;
                    }
                    NotificationMail mail = generator.generateUpdateMailFor(participant);
                    if (mail != null && mail.getStateType() != State.Type.NEW) {
                        notificationMailer.sendMail(mail, session, principal);
                    }
                }
            }
        }

        private void copyParticipantStates(Event src, Event dest) {
            Map<String, Attendee> oldStates = new HashMap<>();
            if (src.getAttendees() != null) {
                for (Attendee up : src.getAttendees()) {
                    if (up.getCuType().equals(CalendarUserType.INDIVIDUAL)) {
                        if (CalendarUtils.isInternal(up)) {
                            oldStates.put(String.valueOf(up.getEntity()), up);
                        } else {
                            oldStates.put(up.getEMail(), up);
                        }
                    }
                }
            }

            if (dest.getAttendees() != null) {
                List<Attendee> newAttendees = new ArrayList<>(dest.getAttendees().size());
                for (Attendee p : dest.getAttendees()) {
                    if (p.getCuType().equals(CalendarUserType.INDIVIDUAL)) {
                        Attendee oup;
                        if (CalendarUtils.isInternal(p)) {
                            oup = oldStates.get(String.valueOf(p.getEntity()));
                        } else {
                            oup = oldStates.get(p.getEMail());
                        }
                        Attendee up = new Attendee();
                        up.setPartStat(oup.getPartStat());
                        up.setComment(oup.getComment());
                    } else {
                        newAttendees.add(p);
                    }
                }
                dest.setAttendees(newAttendees);
            }
        }

        private void notifyInternalParticipantsAboutStateChanges() throws OXException {
            // We have to construct a pair of appointments in which only the participant status is changed
            // For that we clone the new appointment
            // And set the participant states to the values in the old appointment
            // Then finally construct a mail to all internal participants
            Event facsimile = EventMapper.getInstance().copy(mostRecent, new Event(), false, (EventField[]) null);

            copyParticipantStates(original, facsimile);

            ITipMailGenerator generator = generatorFactory.create(facsimile, mostRecent, session, -1, principal);
            generator.noActor();
            List<NotificationParticipant> recipients = generator.getRecipients();
            for (NotificationParticipant participant : recipients) {
                if (participant.isExternal()) {
                    continue;
                }
                NotificationMail mail = generator.generateUpdateMailFor(participant);
                if (mail != null && mail.getStateType() != State.Type.NEW) {
                    notificationMailer.sendMail(mail, session, principal);
                }
            }
        }

        private void proposeChangesToExternalOrganizer() throws OXException {
            ITipMailGenerator generator = generatorFactory.create(original, mostRecent, session, -1, principal);
            if (moreThanOneUserActed()) {
                generator.noActor();
            }
            List<NotificationParticipant> recipients = generator.getRecipients();
            for (NotificationParticipant participant : recipients) {
                if (!participant.isExternal() || !participant.hasRole(ITipRole.ORGANIZER)) {
                    continue;
                }
                NotificationMail mail = generator.generateUpdateMailFor(participant);
                if (mail != null && mail.getStateType() != State.Type.NEW) {
                    notificationMailer.sendMail(mail, session, principal);
                }
            }
        }

        private void notifyExternalParticipantsAboutOverallChangesAsOrganizer() throws OXException {
            ITipMailGenerator generator = generatorFactory.create(original, mostRecent, session, -1, principal);
            if (moreThanOneUserActed()) {
                generator.noActor();
            }
            List<NotificationParticipant> recipients = generator.getRecipients();
            for (NotificationParticipant participant : recipients) {
                if (!participant.isExternal() || participant.hasRole(ITipRole.ORGANIZER)) {
                    continue;
                }
                NotificationMail mail = generator.generateUpdateMailFor(participant);
                if (mail != null && mail.getStateType() != State.Type.NEW) {
                    notificationMailer.sendMail(mail, session, principal);
                }
            }
        }

        private int getIntervalToStartDate() {
            return (int) (lastKnownStartDateForNextOccurrence - System.currentTimeMillis());
        }

        private int getInterval() {
            return (int) (System.currentTimeMillis() - newestTime);
        }

        public int getContextId() {
            return session.getContextId();
        }

        public String getEventId() {
            return original.getId();
        }
    }

    private static final class PartitionIndex {

        public int uid, sharedFolderOwner;

        public PartitionIndex(int uid, int sharedFolderOwner) {
            super();
            this.uid = uid;
            this.sharedFolderOwner = sharedFolderOwner;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + sharedFolderOwner;
            result = prime * result + uid;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PartitionIndex other = (PartitionIndex) obj;
            if (sharedFolderOwner != other.sharedFolderOwner) {
                return false;
            }
            if (uid != other.uid) {
                return false;
            }
            return true;
        }

    }

}
