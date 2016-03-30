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
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.itip.generators.ITipMailGenerator;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationMailGenerator;
import com.openexchange.calendar.itip.generators.NotificationMailGeneratorFactory;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.sender.MailSenderService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.notify.State;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;

/**
 * {@link AppointmentNotificationPool}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppointmentNotificationPool implements
		AppointmentNotificationPoolService, Runnable {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentNotificationPool.class);

	// TODO: Keep shared folder owner, if possible

	private static final int MINUTES = 60000;

	private int detailChangeInterval = 2 *MINUTES;
	private int stateChangeInterval = 10 *MINUTES;
	private int priorityInterval = 15 *MINUTES;

	private final NotificationMailGeneratorFactory generatorFactory;
	private final MailSenderService notificationMailer;

	private final ReentrantLock lock = new ReentrantLock();

	private final Map<Integer, Map<Integer, QueueItem>> items = new HashMap<Integer, Map<Integer, QueueItem>>();

	private final Map<Integer, Map<NotificationParticipant, List<Appointment>>> sent = new HashMap<Integer, Map<NotificationParticipant, List<Appointment>>>();

	public AppointmentNotificationPool(TimerService timer,
			NotificationMailGeneratorFactory generatorFactory,
			MailSenderService notificationMailer, int detailChangeInterval, int stateChangeInterval, int priorityInterval) {
		this.generatorFactory = generatorFactory;
		this.notificationMailer = notificationMailer;

		this.detailChangeInterval = detailChangeInterval;
		this.stateChangeInterval = stateChangeInterval;
		this.priorityInterval = priorityInterval;

		timer.scheduleAtFixedRate(this, 1000, Math.min(stateChangeInterval, Math.min(detailChangeInterval, priorityInterval))/2);
	}

	@Override
    public void run() {
		try {
			lock.lock();

			Collection<QueueItem> allItems = allItems();
            for(QueueItem item: allItems) {
				tick(item.getContextId(), item.getAppointmentId(), false);
			}
		} catch (Throwable t) {
		    ExceptionUtils.handleThrowable(t);
			LOG.error("", t);
		} finally {
			lock.unlock();
		}
	}

	@Override
    public void enqueue(Appointment original, Appointment newAppointment,
			Session session, int sharedFolderOwner) throws OXException {
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
			item(session.getContextId(), original.getObjectID()).remember(original, newAppointment, session, sharedFolderOwner);
		} finally {
			lock.unlock();
		}
	}


	@Override
    public void fasttrack(Appointment appointment, Session session)
			throws OXException {
		try {
			lock.lock();
			tick(session.getContextId(), appointment.getObjectID(), true);
		} finally {
			lock.unlock();
		}
	}

    @Override
    public void aware(Appointment appointment, NotificationParticipant recipient, Session session) {
        Map<NotificationParticipant, List<Appointment>> participants = sent.get(session.getContextId());
        if (participants == null) {
            participants = new HashMap<NotificationParticipant, List<Appointment>>();
            sent.put(session.getContextId(), participants);
        }

        List<Appointment> appointments = participants.get(recipient);
        if (appointments == null) {
            appointments = new ArrayList<Appointment>();
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
    private Appointment removeFromSent(NotificationParticipant participant, Appointment appointment, int contextId) {
        Map<NotificationParticipant, List<Appointment>> participants = sent.get(contextId);
        if (participants == null) {
            return null;
        }

        List<Appointment> appointments = participants.get(participant);
        if (appointments == null) {
            return null;
        }

        Appointment retval = null;
        for (Appointment app : appointments) {
            if (app.getObjectID() == appointment.getObjectID()) {
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

	private void tick(int contextId, int objectID, boolean force) {
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
    public void drop(Appointment appointment, Session session)
			throws OXException {
		drop(session.getContextId(), appointment.getObjectID());
	}

	private Collection<QueueItem> allItems() {
		List<QueueItem> allItems = new LinkedList<QueueItem>();
		for(Map<Integer, QueueItem> contextMaps: items.values()) {
			allItems.addAll(contextMaps.values());
		}
		return allItems;
	}

	private QueueItem item(int contextId, int objectID) {
		Map<Integer, QueueItem> contextMap = items.get(contextId);
		if (contextMap == null) {
			contextMap = new HashMap<Integer, QueueItem>();
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

	private void drop(int contextId, int objectID) {
		Map<Integer, QueueItem> contextMap = items.get(contextId);
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
		private final Appointment oldAppointment;
		private final Appointment newAppointment;
		private final Session session;
		private final long timestamp;
		private AppointmentDiff diff;
		private int sharedFolderOwner = -1;

		public Update(Appointment oldAppointment, Appointment newAppointment, Session session, int sharedFolderOwner) {
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

		public Appointment getOldAppointment() {
			return oldAppointment;
		}

		public Appointment getNewAppointment() {
			return newAppointment;
		}

		public int getSharedFolderOwner() {
			return sharedFolderOwner;
		}

		public AppointmentDiff getDiff() {
			if (diff == null) {
				diff = AppointmentDiff.compare(oldAppointment, newAppointment, NotificationMailGenerator.DEFAULT_SKIP);
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
		private Appointment original;
		private Appointment mostRecent;
		private long newestTime;
		private long lastKnownStartDateForNextOccurrence;
		private Session session;

		private final LinkedList<Update> updates = new LinkedList<Update>();

		public void remember(Appointment original, Appointment newAppointment, Session session, int sharedFolderOwner) {
			if (this.original == null) {
				this.original = original;
				this.mostRecent = newAppointment;
				this.session = session;
			}
			if (this.session.getUserId() != original.getOrganizerId() && session.getUserId() == original.getOrganizerId()) {
				this.session = session;
			}
			this.mostRecent = newAppointment;
			this.newestTime = System.currentTimeMillis();
			this.lastKnownStartDateForNextOccurrence = newAppointment.getStartDate().getTime();
			Update update = new Update(original, newAppointment, session, sharedFolderOwner);
			updates.add(update);
			if (update.getDiff().anyFieldChangedOf(CalendarFields.START_DATE, CalendarFields.END_DATE, AppointmentFields.LOCATION, CalendarFields.RECURRENCE_TYPE, CalendarFields.DAY_IN_MONTH, CalendarFields.DAYS, AppointmentFields.FULL_TIME, CalendarFields.INTERVAL, CalendarFields.MONTH, CalendarFields.RECURRENCE_POSITION, CalendarFields.RECURRENCE_DATE_POSITION)) {
				// Participant State has been reset
				// Purge state only changes
				Iterator<Update> iterator = updates.iterator();
				while(iterator.hasNext()) {
					Update u = iterator.next();
					if (u.getDiff().isAboutStateChangesOnly()) {
						iterator.remove();
					}
				}
				this.original = updates.get(0).getOldAppointment();
				// Apply new reset states to original appointment
				copyParticipantStates(newAppointment, this.original);
			}
		}


		public HandlingSuggestion tick(boolean force) throws OXException {
			if (original == null) {
				return HandlingSuggestion.DONE;
			}
			// Diff most recent and original version
			AppointmentDiff overallDiff = AppointmentDiff.compare(original, mostRecent, NotificationMailGenerator.DEFAULT_SKIP);

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
			ITipMailGenerator generator = generatorFactory.create(original, mostRecent, session, -1);
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
					notificationMailer.sendMail(mail, session);
				}
			}
		}


        private void notifyInternalParticipantsAboutOverallChanges() throws OXException {
            ITipMailGenerator generator = generatorFactory.create(original, mostRecent, session, -1);
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
                        notificationMailer.sendMail(mail, session);
                    }
                }
            }
        }

        private boolean isAlreadyInformed(NotificationParticipant participant, Appointment mostRecent, int contextId) {
            Appointment alreadySent = removeFromSent(participant, mostRecent, contextId);
            if (alreadySent != null) {
                AppointmentDiff diff = AppointmentDiff.compare(alreadySent, mostRecent);
                return diff.getDifferingFieldNames().isEmpty();
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
			for(Update update: updates) {
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
				Appointment oldAppointment = userScopedUpdate[0].getOldAppointment();
				Appointment newAppointment = userScopedUpdate[1].getNewAppointment();
				ITipMailGenerator generator = generatorFactory.create(oldAppointment, newAppointment,
						session, userScopedUpdate[0].getSharedFolderOwner());
				List<NotificationParticipant> recipients = generator
						.getRecipients();
				for (NotificationParticipant participant : recipients) {
					if (participant.isExternal() && !participant.hasRole(ITipRole.ORGANIZER)) {
						continue;
					}
					NotificationMail mail = generator.generateUpdateMailFor(participant);
					if (mail != null && mail.getStateType() != State.Type.NEW) {
						notificationMailer.sendMail(mail, session);
					}
				}
			}
		}

		private void copyParticipantStates(Appointment src, Appointment dest) {
			Map<String, Participant> oldStates = new HashMap<String, Participant>();
			if (src.getUsers() != null) {
				for (UserParticipant up : src.getUsers()) {
					oldStates.put(String.valueOf(up.getIdentifier()), up);
				}
			}

			if (src.getConfirmations() != null) {
				for (ConfirmableParticipant cp: src.getConfirmations()) {
					oldStates.put(cp.getEmailAddress(), cp);
				}
			}


			if (dest.getParticipants() != null) {
				List<Participant> newParticipants = new ArrayList<Participant>(dest.getParticipants().length);
				for(Participant p: dest.getParticipants()) {
					if (p instanceof UserParticipant) {
						UserParticipant up = (UserParticipant) p;
						UserParticipant oup = (UserParticipant) oldStates.get(String.valueOf(up.getIdentifier()));
						up = new UserParticipant(up.getIdentifier());
						if (oup != null) {
							up.setConfirm(oup.getConfirm());
							up.setConfirmMessage(oup.getConfirmMessage());
						}
						newParticipants.add(up);
					} else if (p instanceof ConfirmableParticipant) {
						ConfirmableParticipant cp = (ConfirmableParticipant) p;
						ConfirmableParticipant ocp = (ConfirmableParticipant) oldStates.get(String.valueOf(cp.getEmailAddress()));
						cp = new ExternalUserParticipant(cp.getEmailAddress());
						if (ocp != null) {
							cp.setStatus(ocp.getStatus());
							cp.setMessage(ocp.getMessage());
						}
						newParticipants.add(cp);
					} else {
						newParticipants.add(p);
					}

				}
				dest.setParticipants(newParticipants);
			}

			if (dest.getUsers() != null) {
				List<UserParticipant> newUsers = new ArrayList<UserParticipant>(dest.getUsers().length);

				for (UserParticipant up: dest.getUsers()) {
					up = new UserParticipant(up.getIdentifier());
					UserParticipant oup = (UserParticipant) oldStates.get(String.valueOf(up.getIdentifier()));
					if (oup != null) {
						up.setConfirm(oup.getConfirm());
						up.setConfirmMessage(oup.getConfirmMessage());
					}
					newUsers.add(up);
				}

				dest.setUsers(newUsers);
			}

			if (dest.getConfirmations() != null) {
				List<ConfirmableParticipant> newConfirmations = new ArrayList<ConfirmableParticipant>(dest.getConfirmations().length);

				for (ConfirmableParticipant cp: dest.getConfirmations()) {
					cp = new ExternalUserParticipant(cp.getEmailAddress());
					ConfirmableParticipant ocp = (ConfirmableParticipant) oldStates.get(String.valueOf(cp.getEmailAddress()));
					if (ocp != null) {
						cp.setStatus(ocp.getStatus());
						cp.setMessage(ocp.getMessage());
					}

					newConfirmations.add(cp);
				}

				dest.setConfirmations(newConfirmations);
			}
		}

		private void notifyInternalParticipantsAboutStateChanges() throws OXException {
			// We have to construct a pair of appointments in which only the participant status is changed
			// For that we clone the new appointment
			// And set the participant states to the values in the old appointment
			// Then finally construct a mail to all internal participants
			Appointment facsimile = mostRecent.clone();

			copyParticipantStates(original, facsimile);


			ITipMailGenerator generator = generatorFactory.create(facsimile, mostRecent,
					session, -1);
			generator.noActor();
			List<NotificationParticipant> recipients = generator
					.getRecipients();
			for (NotificationParticipant participant : recipients) {
				if (participant.isExternal()) {
					continue;
				}
				NotificationMail mail = generator.generateUpdateMailFor(participant);
				if (mail != null && mail.getStateType() != State.Type.NEW) {
					notificationMailer.sendMail(mail, session);
				}
			}
		}

        private void proposeChangesToExternalOrganizer() throws OXException {
            ITipMailGenerator generator = generatorFactory.create(original, mostRecent, session, -1);
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
                    notificationMailer.sendMail(mail, session);
                }
            }
        }

		private void notifyExternalParticipantsAboutOverallChangesAsOrganizer() throws OXException {
			ITipMailGenerator generator = generatorFactory.create(original, mostRecent,
					session, -1);
			if (moreThanOneUserActed()) {
				generator.noActor();
			}
			List<NotificationParticipant> recipients = generator
					.getRecipients();
			for (NotificationParticipant participant : recipients) {
				if (!participant.isExternal() || participant.hasRole(ITipRole.ORGANIZER)) {
					continue;
				}
				NotificationMail mail = generator.generateUpdateMailFor(participant);
				if (mail != null && mail.getStateType() != State.Type.NEW) {
					notificationMailer.sendMail(mail, session);
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

		public int getAppointmentId() {
			return original.getObjectID();
		}
	}

	private static final class PartitionIndex {
		public int uid,sharedFolderOwner;

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
