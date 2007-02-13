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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.event;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.ServerTimer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * EventQueue
 *
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class EventQueue extends TimerTask {
	
	private static boolean isFirst = true;
	
	private static boolean isInit = false;
	
	private static boolean noDelay = false;
	
	private static ArrayList<EventObject> queue1 = null;
	
	private static ArrayList<EventObject> queue2 = null;
	
	private static int delay = 180000;
	
	private static boolean isEnabled = false;
	
	private static final Log LOG = LogFactory.getLog(EventQueue.class);
	
	private static ArrayList<AppointmentEvent> appointmentEventList = new ArrayList<AppointmentEvent>();
	private static ArrayList<TaskEvent> taskEventList = new ArrayList<TaskEvent>();
	private static ArrayList<ContactEvent> contactEventList = new ArrayList<ContactEvent>();
	private static ArrayList<FolderEvent> folderEventList = new ArrayList<FolderEvent>();
	private static ArrayList<InfostoreEvent> infostoreEventList = new ArrayList<InfostoreEvent>();
	
	public EventQueue(EventConfig config) {
		delay = config.getEventQueueDelay();
		
		if (config.isEventQueueEnabled()) {
			LOG.info("Starting EventQueue");
			
			queue1 = new ArrayList<EventObject>();
			queue2 = new ArrayList<EventObject>();
			
			noDelay = (delay == 0);
			
			if (!noDelay) {
				Timer t = ServerTimer.getTimer();
				t.schedule(this, delay, delay);
			}			
			
			isEnabled = true;
		} else {
			LOG.info("EventQueue is disabled");
		}
		
		isInit = true;
	}
	
	public static void add(final EventObject eventObj) throws InvalidStateException {
		LOG.debug("add EventObject: " + eventObj);
		
		if (!isEnabled) {
			return;
		}
		
		if (!isInit) {
			throw new InvalidStateException("EventQueue not initialisiert!");
		}
		
		if (!noDelay) {
			if (isFirst) {
				queue1.add(eventObj);
			} else {
				queue2.add(eventObj);
			}
		} else {
			event(eventObj);
		}
	}
	
	public void run() {
		try {
			if (isFirst) {
				isFirst = false;
				callEvent(queue1);
			} else {
				isFirst = true;
				callEvent(queue2);
			}
		} catch (Exception exc) {
			LOG.error("run", exc);
		}
	}
	
	protected static void callEvent(final ArrayList<EventObject> al) {
		for (int a = 0; a < al.size(); a++) {
			event(al.get(a));
		}
		
		al.clear();
	}
	
	protected static void event(final EventObject eventObj) {
		final int module = eventObj.getModule();
		switch (module) {
			case Types.APPOINTMENT:
				appointment(eventObj);
				break;
			case Types.CONTACT:
				contact(eventObj);
				break;
			case Types.TASK:
				task(eventObj);
				break;
			case Types.FOLDER:
				folder(eventObj);
				break;
			case Types.INFOSTORE:
				infostore(eventObj);
				break;
			default:
				LOG.error("invalid module: " + module);
		}
	}
	
	protected static void appointment(final EventObject eventObj) {
		final int action = eventObj.getAction();
		switch (action) {
			case EventClient.CREATED:
				for (int a = 0; a < appointmentEventList.size(); a++) {
					appointmentEventList.get(a).appointmentCreated((AppointmentObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.CHANGED:
				for (int a = 0; a < appointmentEventList.size(); a++) {
					appointmentEventList.get(a).appointmentModified((AppointmentObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.DELETED:
				for (int a = 0; a < appointmentEventList.size(); a++) {
					appointmentEventList.get(a).appointmentDeleted((AppointmentObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			default:
				LOG.error("invalid action for appointment: " + action);
		}
	}
	
	protected static void contact(final EventObject eventObj) {
		final int action = eventObj.getAction();
		switch (action) {
			case EventClient.CREATED:
				for (int a = 0; a < contactEventList.size(); a++) {
					contactEventList.get(a).contactCreated((ContactObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.CHANGED:
				for (int a = 0; a < contactEventList.size(); a++) {
					contactEventList.get(a).contactModified((ContactObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.DELETED:
				for (int a = 0; a < contactEventList.size(); a++) {
					contactEventList.get(a).contactDeleted((ContactObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			default:
				LOG.error("invalid action for contact: " + action);
		}
	}
	
	protected static void task(final EventObject eventObj) {
		final int action = eventObj.getAction();
		switch (action) {
			case EventClient.CREATED:
				for (int a = 0; a < taskEventList.size(); a++) {
					taskEventList.get(a).taskCreated((Task)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.CHANGED:
				for (int a = 0; a < taskEventList.size(); a++) {
					taskEventList.get(a).taskModified((Task)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.DELETED:
				for (int a = 0; a < taskEventList.size(); a++) {
					taskEventList.get(a).taskDeleted((Task)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			default:
				LOG.error("invalid action for task: " + action);
		}
	}
	
	protected static void folder(final EventObject eventObj) {
		final int action = eventObj.getAction();
		switch (action) {
			case EventClient.CREATED:
				for (int a = 0; a < folderEventList.size(); a++) {
					folderEventList.get(a).folderCreated((FolderObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.CHANGED:
				for (int a = 0; a < folderEventList.size(); a++) {
					folderEventList.get(a).folderModified((FolderObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.DELETED:
				for (int a = 0; a < folderEventList.size(); a++) {
					folderEventList.get(a).folderDeleted((FolderObject)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			default:
				LOG.error("invalid action for folder: " + action);
		}
	}
	
	protected static void infostore(final EventObject eventObj) {
		final int action = eventObj.getAction();
		switch (action) {
			case EventClient.CREATED:
				for (int a = 0; a < infostoreEventList.size(); a++) {
					infostoreEventList.get(a).infoitemCreated((DocumentMetadata)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.CHANGED:
				for (int a = 0; a < infostoreEventList.size(); a++) {
					infostoreEventList.get(a).infoitemModified((DocumentMetadata)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			case EventClient.DELETED:
				for (int a = 0; a < infostoreEventList.size(); a++) {
					infostoreEventList.get(a).infoitemDeleted((DocumentMetadata)eventObj.getObject(), eventObj.getSessionObject());
				}
				break;
			default:
				LOG.error("invalid action for infostore: " + action);
		}
	}
	
	public static void addAppointmentEvent(final AppointmentEvent event) {
		appointmentEventList.add(event);
	}

	public static void addTaskEvent(final TaskEvent event) {
		taskEventList.add(event);
	}

	public static void addContactEvent(final ContactEvent event) {
		contactEventList.add(event);
	}

	public static void addFolderEvent(final FolderEvent event) {
		folderEventList.add(event);
	}
	
	public static void addInfostoreEvent(final InfostoreEvent event) {
		infostoreEventList.add(event);
	}
}
