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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.calendar.itip.analyzers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnalyzer;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.ITipChange.Type;
import com.openexchange.calendar.itip.generators.ArgumentType;
import com.openexchange.calendar.itip.generators.HTMLWrapper;
import com.openexchange.calendar.itip.generators.Sentence;
import com.openexchange.calendar.itip.generators.TypeWrapper;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriber;
import com.openexchange.calendar.itip.generators.changes.PassthroughWrapper;
import com.openexchange.calendar.itip.generators.changes.generators.Details;
import com.openexchange.calendar.itip.generators.changes.generators.Participants;
import com.openexchange.calendar.itip.generators.changes.generators.Rescheduling;
import com.openexchange.calendar.itip.generators.changes.generators.Style;
import com.openexchange.context.ContextService;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link AbstractITipAnalyzer}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public abstract class AbstractITipAnalyzer implements ITipAnalyzer {
	public static final int[] SKIP = new int[] { Appointment.FOLDER_ID,
			Appointment.OBJECT_ID, Appointment.CREATED_BY,
			Appointment.CREATION_DATE, Appointment.LAST_MODIFIED,
			Appointment.LAST_MODIFIED_UTC, Appointment.MODIFIED_BY,
			Appointment.SEQUENCE };
	protected ITipIntegrationUtility util;
	protected ServiceLookup services;
	
	public ITipAnalysis analyze(ITipMessage message, Map<String, String> header,
			String style, Session session) throws AbstractOXException {
		if (header == null) {
			header = new HashMap<String, String>();
		}
		header = lowercase(header);
		if (services != null) {
			
			ContextService contexts = services.getService(ContextService.class);
			UserService users = services.getService(UserService.class);
		
			Context ctx = contexts.getContext(session.getContextId());
			User user = users.getUser(session.getUserId(), ctx);
			
			return analyze(message, header, wrapperFor(style), user.getLocale(), user, ctx, session);
		
		}

		return analyze(message, header, wrapperFor(style), null, null, null, session);
	}
	
	private Map<String, String> lowercase(Map<String, String> header) {
		Map<String, String> copy = new HashMap<String, String>();
		for(Map.Entry<String, String> entry : header.entrySet()) {
			copy.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		return copy;
	}

	protected abstract ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, Session session) throws AbstractOXException;

	public AbstractITipAnalyzer(ITipIntegrationUtility util,
			ServiceLookup services) {
		this.util = util;
		this.services = services;
	}

	protected TypeWrapper wrapperFor(String style) {
		TypeWrapper w = new PassthroughWrapper();
		if (style != null && style.equalsIgnoreCase("html")) {
			w = new HTMLWrapper();
		}
		return w;
	}
	

	public void describeDiff(ITipChange change, TypeWrapper wrapper,
			Session session) throws AbstractOXException {
		if (services == null) {
			change.setDiffDescription(new ArrayList<String>());
			return;
		}

		ContextService contexts = services.getService(ContextService.class);
		UserService users = services.getService(UserService.class);
		GroupService groups = services.getService(GroupService.class);
		ResourceService resources = services.getService(ResourceService.class);

		Context ctx = contexts.getContext(session.getContextId());
		User user = users.getUser(session.getUserId(), ctx);

		switch (change.getType()) {
		case CREATE:
			createIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		case UPDATE:
			updateIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		case CREATE_DELETE_EXCEPTION:
		case DELETE:
			deleteIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		}

		Appointment currentAppointment = change.getCurrentAppointment();
		CalendarDataObject newAppointment = change.getNewAppointment();

		if (currentAppointment == null || newAppointment == null) {
			change.setDiffDescription(new ArrayList<String>());
			return;
		}

		Style style = Style.ASK;

		String organizer = currentAppointment.getOrganizer();
		if (organizer == null) {
			organizer = newAppointment.getOrganizer();
		}

		if (organizer != null) {
			if (!organizer.equalsIgnoreCase(user.getMail())) {
				style = Style.FAIT_ACCOMPLI;
			}
		} else {
			if (user.getId() == currentAppointment.getCreatedBy()) {
				style = Style.FAIT_ACCOMPLI;
			}
		}

		ChangeDescriber cd = new ChangeDescriber(new Rescheduling(style),
				new Details(style), new Participants(users, groups, resources,
						style, true));

		List<String> descriptions = cd.getChanges(ctx, currentAppointment,
				newAppointment, change.getDiff(), wrapper, user.getLocale(),
				TimeZone.getTimeZone(user.getTimeZone()));
		change.setDiffDescription(descriptions);

		// Now let's choose an introduction sentence
		switch (change.getType()) {
		case CREATE:
			if (!change.isException()) {
				createIntro(change, users, ctx, wrapper, user.getLocale());
				break;
			} // Else Fall Through, creating change exceptions is more similar to updates
		case UPDATE:
			updateIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		case CREATE_DELETE_EXCEPTION:
		case DELETE:
			deleteIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		}
	}

	private void deleteIntro(ITipChange change, UserService users, Context ctx,
			TypeWrapper wrapper, Locale locale) throws AbstractOXException {
		String displayName = displayNameFor(change.getDeletedAppointment()
				.getOrganizer(), users, ctx);
		change.setIntroduction(new Sentence(Messages.DELETE_INTRO).add(
				displayName, ArgumentType.PARTICIPANT).getMessage(wrapper,
				locale));

	}

	private void updateIntro(ITipChange change, UserService users, Context ctx,
			TypeWrapper wrapper, Locale locale) throws AbstractOXException {
		String displayName = displayNameFor(change.getCurrentAppointment()
				.getOrganizer(), users, ctx);
		if (onlyStateChanged(change.getDiff())) {
			// External Participant
			
			ConfirmStatus newStatus = null;

			AppointmentDiff diff = change.getDiff();
			FieldUpdate update = diff
					.getUpdateFor(AppointmentFields.CONFIRMATIONS);
			if (update != null) {
				Difference difference = (Difference) update.getExtraInfo();
				List<Change> changed = difference.getChanged();
				if (changed != null && !changed.isEmpty()) {
					ConfirmationChange chng = (ConfirmationChange) changed
							.get(0);
					displayName = chng.getIdentifier();
					newStatus = ConfirmStatus.byId(chng.getNewStatus());
				}
			}

			// Internal Participant
			update = diff.getUpdateFor("users");
			if (update != null && newStatus == null) {
				Difference difference = (Difference) update.getExtraInfo();
				List<Change> changed = difference.getChanged();
				if (changed != null && !changed.isEmpty()) {
					ConfirmationChange chng = (ConfirmationChange) changed
							.get(0);
					displayName = users.getUser(
							Integer.valueOf(chng.getIdentifier()), ctx)
							.getDisplayName();
					newStatus = ConfirmStatus.byId(chng.getNewStatus());
				}
			}

			Sentence sentence = null;
			if (newStatus != null) {
				switch (newStatus) {
				case ACCEPT:
					sentence = new Sentence(Messages.ACCEPT_INTRO).add(displayName,
							ArgumentType.PARTICIPANT).add(Messages.ACCEPTED,
							ArgumentType.STATUS, newStatus);
					break;
				case DECLINE:
					sentence = new Sentence(Messages.DECLINE_INTRO).add(
							displayName, ArgumentType.PARTICIPANT).add(
							Messages.DECLINED, ArgumentType.STATUS, newStatus);
					break;
				case TENTATIVE:
					sentence = new Sentence(Messages.TENTATIVE_INTRO).add(
							displayName, ArgumentType.PARTICIPANT).add(
							Messages.TENTATIVELY_ACCEPTED, ArgumentType.STATUS, newStatus);
					break;
				}

				if (sentence != null) {
					change.setIntroduction(sentence.getMessage(wrapper, locale));
				}
			}
		} else {
			change.setIntroduction(new Sentence(Messages.UPDATE_INTRO).add(
					displayName, ArgumentType.PARTICIPANT).getMessage(wrapper,
					locale));
		}
	}

	private boolean onlyStateChanged(AppointmentDiff diff) {
		// First, let's see if any fields besides the state tracking fields have
		// changed
		HashSet<String> differing = new HashSet<String>(
				diff.getDifferingFieldNames());

		for (String field : new String[] { AppointmentFields.PARTICIPANTS,
				AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS }) {
			differing.remove(field);
		}
		if (!differing.isEmpty()) {
			return false;
		}

		// Hm, okay, so no let's see if any participants were added or removed.
		for (String field : new String[] { AppointmentFields.PARTICIPANTS,
				AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS }) {
			FieldUpdate update = diff.getUpdateFor(field);
			if (update == null) {
				continue;
			}
			Difference extraInfo = (Difference) update.getExtraInfo();
			if (!extraInfo.getAdded().isEmpty()) {
				return false;
			}
			if (!extraInfo.getRemoved().isEmpty()) {
				return false;
			}

		}

		return true;
	}

	private void createIntro(ITipChange change, UserService users, Context ctx,
			TypeWrapper wrapper, Locale locale) throws AbstractOXException {
		String displayName = displayNameFor(change.getNewAppointment()
				.getOrganizer(), users, ctx);
		change.setIntroduction(new Sentence(Messages.CREATE_INTRO).add(
				displayName, ArgumentType.PARTICIPANT).getMessage(wrapper,
				locale));
	}

	protected String displayNameFor(String organizer, UserService users,
			Context ctx) throws AbstractOXException {
		if (organizer == null) {
			return "unknown";
		}
		organizer = organizer.toLowerCase();
		if (organizer.startsWith("mailto:")) {
			organizer = organizer.substring(7);
		}

		try {
			User result = users.searchUser(organizer, ctx);
			if (result != null) {
				if (result.getDisplayName() != null) {
					return result.getDisplayName();
				}
			}
		} catch (UserException x) {
			return organizer;
		}

		return organizer;
	}

	protected Appointment findAndRemoveMatchingException(Appointment exception,
			List<Appointment> exceptions) {
		for (Iterator<Appointment> iterator = exceptions.iterator(); iterator
				.hasNext();) {
			Appointment existingException = iterator.next();
			if (sameDay(existingException.getRecurrenceDatePosition(),
					(exception.getRecurrenceDatePosition()))) {
				iterator.remove();
				return existingException;
			}
		}
		return null;
	}

	private boolean sameDay(Date date1, Date date2) {
		GregorianCalendar gregorianCalendar1 = new GregorianCalendar();
		gregorianCalendar1.setTime(date1);
		gregorianCalendar1.setTimeZone(TimeZone.getTimeZone("UTC"));

		GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
		gregorianCalendar2.setTime(date2);
		gregorianCalendar2.setTimeZone(TimeZone.getTimeZone("UTC"));

		for (int field : new int[] { Calendar.DAY_OF_YEAR, Calendar.YEAR }) {
			if (gregorianCalendar1.get(field) != gregorianCalendar2.get(field)) {
				return false;
			}
		}

		return true;
	}

	public boolean doAppointmentsDiffer(Appointment update, Appointment original) {
		AppointmentDiff diff = AppointmentDiff.compare(original, update, SKIP);

		return !diff.getDifferingFieldNames().isEmpty();
	}

	public boolean hasConflicts(ITipAnalysis analysis) {
		for (ITipChange change : analysis.getChanges()) {
			if (change.getConflicts() != null
					&& !change.getConflicts().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void purgeConflicts(ITipAnalysis analysis) {
		Map<Integer, ITipChange> knownAppointments = new HashMap<Integer, ITipChange>();
		for (ITipChange change : analysis.getChanges()) {
			Appointment currentAppointment = change.getCurrentAppointment();
			if (currentAppointment != null) {
				knownAppointments.put(currentAppointment.getObjectID(), change);
			}
			Appointment deletedAppointment = change.getDeletedAppointment();
			if (deletedAppointment != null) {
				knownAppointments.put(deletedAppointment.getObjectID(), change);
			}
		}

		for (ITipChange change : analysis.getChanges()) {
			List<Appointment> conflicts = change.getConflicts();
			if (conflicts == null) {
				continue;
			}
			CalendarDataObject newAppointment = change.getNewAppointment();
			if (newAppointment == null) {
				continue;
			}
			Appointment currentAppointment = change.getCurrentAppointment();
			CalendarDataObject masterAppointment = change
					.getMasterAppointment();
			for (Iterator<Appointment> iterator = conflicts.iterator(); iterator
					.hasNext();) {
				Appointment conflict = iterator.next();
				if (currentAppointment != null
						&& (currentAppointment.getObjectID() == conflict
								.getObjectID())) {
					iterator.remove();
					continue;
				}
				if (masterAppointment != null
						&& (masterAppointment.getObjectID() == conflict
								.getObjectID())) {
					iterator.remove();
					continue;
				}
				ITipChange changeToConflict = knownAppointments.get(conflict
						.getObjectID());
				if (changeToConflict == null) {
					continue;
				}
				if (changeToConflict.getType() == ITipChange.Type.DELETE) {
					iterator.remove();
				} else {
					CalendarDataObject changedAppointment = changeToConflict
							.getNewAppointment();
					if (changedAppointment == null) {
						continue;
					}
					if (!overlaps(changedAppointment, newAppointment)) {
						iterator.remove();
					}
				}
			}
		}
	}

	public boolean overlaps(CalendarDataObject app1, CalendarDataObject app2) {
		if (app2.getStartDate().after(app1.getEndDate())) {
			return false;
		}

		if (app1.getStartDate().after(app2.getEndDate())) {
			return false;
		}

		return true;
	}

	public boolean isCreate(ITipAnalysis analysis) {
		for (ITipChange change : analysis.getChanges()) {
			if (change.getType() == Type.CREATE) {
				return true;
			}
		}
		return false;
	}

	public boolean rescheduling(ITipAnalysis analysis) {
		for (ITipChange change : analysis.getChanges()) {
			if (change.getType() == Type.CREATE && change.isException()) {
				AppointmentDiff diff = AppointmentDiff.compare(
						change.getCurrentAppointment(),
						change.getNewAppointment());
				if (diff.anyFieldChangedOf(AppointmentFields.START_DATE,
						AppointmentFields.END_DATE)) {
					return true;
				}
				return false;
			}
			if (change.getType() != Type.UPDATE) {
				return true;
			}
			AppointmentDiff diff = change.getDiff();
			if (diff != null
					&& diff.anyFieldChangedOf(AppointmentFields.START_DATE,
							AppointmentFields.END_DATE)) {
				return true;
			}
		}
		return false;
	}
}
