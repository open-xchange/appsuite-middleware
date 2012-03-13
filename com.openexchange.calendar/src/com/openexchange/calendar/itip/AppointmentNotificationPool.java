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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.calendar.itip.generators.ITipMailGenerator;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationMailGeneratorFactory;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.sender.MailSenderService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;

/**
 * {@link AppointmentNotificationPool}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AppointmentNotificationPool implements
		AppointmentNotificationPoolService, Runnable {

	private static final Log LOG = LogFactory
			.getLog(AppointmentNotificationPool.class);

	// Ctx ID, Appt ID, User ID
	private Map<Integer, Map<Integer, Map<Integer, OldNew>>> pool;

	private ReentrantReadWriteLock lock;

	private NotificationMailGeneratorFactory generatorFactory;

	private MailSenderService notificationMailer;

	private int interval;

	public AppointmentNotificationPool(TimerService timer,
			NotificationMailGeneratorFactory generatorFactory,
			MailSenderService notificationMailer, int interval) {
		pool = new ConcurrentHashMap<Integer, Map<Integer, Map<Integer, OldNew>>>();
		lock = new ReentrantReadWriteLock();
		this.generatorFactory = generatorFactory;
		this.notificationMailer = notificationMailer;

		timer.scheduleAtFixedRate(this, 1000, interval/2);
		this.interval = interval;
	}
	
	private OldNew get(int contextId, int apptId, int userId) {
		Map<Integer, Map<Integer, OldNew>> apptMap = pool.get(contextId);
		if (apptMap == null) {
			return null;
		}
		
		Map<Integer, OldNew> userMap = apptMap.get(apptId);
		if (userMap == null) {
			return null;
		}
		
		return userMap.get(userId);
	}
	
	private void set(int contextId, int apptId, int userId, OldNew oldNew) {
		Map<Integer, Map<Integer, OldNew>> apptMap = pool.get(contextId);
		if (apptMap == null) {
			apptMap = new ConcurrentHashMap<Integer, Map<Integer,OldNew>>();
			pool.put(contextId, apptMap);
		}
		
		Map<Integer, OldNew> userMap = apptMap.get(apptId);
		if (userMap == null) {
			userMap = new ConcurrentHashMap<Integer, AppointmentNotificationPool.OldNew>();
			apptMap.put(apptId, userMap);
		}
		
		userMap.put(userId, oldNew);
	}
	
	private void remove(int contextId, int apptId, int userId) {
		Map<Integer, Map<Integer, OldNew>> apptMap = pool.get(contextId);
		if (apptMap == null) {
			return;
		}
		
		Map<Integer, OldNew> userMap = apptMap.get(apptId);
		if (userMap == null) {
			return;
		}
		
		userMap.remove(userId);
	}
	
	private List<OldNew> removeAll(int contextId, int apptId) {
		Map<Integer, Map<Integer, OldNew>> apptMap = pool.get(contextId);
		if (apptMap == null) {
			return Collections.emptyList();
		}
		
		Map<Integer, OldNew> userMap = apptMap.remove(apptId);
		if (userMap == null) {
			return Collections.emptyList();
		}
		
		List<OldNew> retval = new ArrayList<OldNew>(userMap.size());
		List<OldNew> tail = new ArrayList<OldNew>();
		for (OldNew oldNew : userMap.values()) {
			if (oldNew.isOrganizerEntry()) {
				retval.add(oldNew);
			} else {
				tail.add(oldNew);
			}
		}
		retval.addAll(tail);
		return retval;
		
	}
	
	private void enqueue(OldNew value) {
		int contextId = value.session.getContextId();
		int apptId = (value.neww != null) ? value.neww.getObjectID() : value.old.getObjectID();
		int userId = value.session.getUserId();
		set(contextId, apptId, userId, value);
	}

	public void enqueue(Appointment original, Appointment newAppointment,
			Session session) throws OXException {
		lock.writeLock().lock();
		try {
			boolean isCreate = original == null;
			int objectId = 0;
			int onBehalfOf = 0;
			if (isCreate) {
				objectId = newAppointment.getObjectID();
				onBehalfOf = newAppointment.getPrincipalId();
			} else {
				objectId = original.getObjectID();
				onBehalfOf = original.getPrincipalId();
			}
			
			OldNew oldNew = get(session.getContextId(), objectId, session.getUserId());

			if (oldNew == null) {
				set(session.getContextId(), objectId, session.getUserId(), new OldNew(original, newAppointment,
						onBehalfOf, session));
			} else {
				oldNew.setNeww(newAppointment);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void fasttrack(Appointment appointment, Session session) {
		lock.writeLock().lock();
		try {
			if (pool.isEmpty()) {
				return;
			}
			List<OldNew> values = removeAll(session.getContextId(), appointment.getObjectID());
			
			notify(values, null, false);

		} finally {
			lock.writeLock().unlock();
		}
	}

	public void drop(Appointment appointment, Session session) {
		lock.writeLock().lock();
		try {
			if (pool.isEmpty()) {
				return;
			}

			removeAll(session.getContextId(), appointment.getObjectID());
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void run() {
		lock.writeLock().lock();
		try {
			List<OldNew> enqueueAgainList = new ArrayList<OldNew>();
			try {
				if (pool.isEmpty()) {
					return;
				}
				Set<Integer> poolKeySet = pool.keySet();
				for (Integer contextId : poolKeySet) {
					Map<Integer, Map<Integer, OldNew>> contextPool = pool.get(contextId);
					Set<Integer> contextKeySet = contextPool.keySet();
					for (Integer objectId : contextKeySet) {
						List<OldNew> values = removeAll(contextId, objectId);
						notify(values, enqueueAgainList, true);
					}
				}
			} catch (Throwable t) {
				LOG.error(t.getMessage(), t);
			} finally {
				pool.clear();
			}
			for (OldNew oldNew : enqueueAgainList) {
				enqueue(oldNew);
			}
		} finally {
			lock.writeLock().unlock();
		}

	}

	private void notify(List<OldNew> values, List<OldNew> enqueueAgainList, boolean enqueueAgain) {
		try {
			if (values.isEmpty()) {
				return;
			}
			if (values.size() == 1) {
				OldNew oldNew = values.get(0);
				if (enqueueAgain && keepAnotherRound(oldNew.updated)) {
					enqueueAgainList.add(oldNew);
				} else {
					int onBehalfOf = (oldNew.old != null) ? oldNew.old.getPrincipalId() : oldNew.neww.getPrincipalId();
					ITipMailGenerator generator = generatorFactory.create(oldNew.old, oldNew.neww,
							oldNew.session, onBehalfOf);
					List<NotificationParticipant> recipients = generator
							.getRecipients();
					for (NotificationParticipant participant : recipients) {

						NotificationMail mail = (oldNew.old == null) ? generator
								.generateCreateMailFor(participant) : generator
								.generateUpdateMailFor(participant);
						if (mail != null) {
							notificationMailer.sendMail(mail, oldNew.session);
						}
					}
				}
				
			} else {
				// Construct a new mail and send it to everyone.
				Appointment earliestOriginal = null;
				long earliestTstamp = System.currentTimeMillis();
				for(OldNew value : values) {
					if (earliestTstamp > value.tstamp) {
						earliestTstamp = value.tstamp;
						earliestOriginal = value.old;
					}
				}
				
				Appointment newestNeww = null;
				long newestTstamp = 0;
				for(OldNew value : values) {
					if (newestTstamp < value.updated) {
						newestTstamp = value.updated;
						newestNeww = value.neww;
					}
				}
				
				OldNew oldNew = values.get(0);
				
				ITipMailGenerator generator = generatorFactory.create(earliestOriginal, newestNeww,
						oldNew.session, oldNew.getSession().getUserId());
				generator.noActor();
				List<NotificationParticipant> recipients = generator
						.getRecipients();
				for (NotificationParticipant participant : recipients) {

					NotificationMail mail = (oldNew.old == null) ? generator
							.generateCreateMailFor(participant) : generator
							.generateUpdateMailFor(participant);
					if (mail != null) {
						notificationMailer.sendMail(mail, oldNew.session);
					}
				}
				
			}
		} catch (OXException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	private boolean keepAnotherRound(long updated) {
		return System.currentTimeMillis() - updated < interval;
	}


	private class OldNew {

		private Appointment old;

		private Appointment neww;

		private Session session;

		private int onBehalfOf;

		private boolean organizerEntry;
		
		public long tstamp = System.currentTimeMillis();
		public long updated = System.currentTimeMillis();

		public OldNew(Appointment old, Appointment neww, int onBehalfOf,
				Session session) {
			this.setOld(old);
			this.setNeww(neww);
			this.setOnBehalfOf(onBehalfOf);
			setSession(session);
			
			Appointment determinant = (old != null) ? old : neww;
			
			organizerEntry = determinant.getOrganizerId() == session.getUserId();
		}

		public boolean isOrganizerEntry() {
			
			return organizerEntry;
		}

		public int getOnBehalfOf() {
			return onBehalfOf;
		}

		public void setOnBehalfOf(int onBehalfOf) {
			this.onBehalfOf = onBehalfOf;
		}

		public Appointment getOld() {
			return old;
		}

		public void setOld(Appointment old) {
			this.old = old;
		}

		public Appointment getNeww() {
			return neww;
		}

		public void setNeww(Appointment neww) {
			this.neww = neww;
			updated = System.currentTimeMillis();
		}

		public Session getSession() {
			return session;
		}

		public void setSession(Session session) {
			this.session = session;
		}

	}

}
