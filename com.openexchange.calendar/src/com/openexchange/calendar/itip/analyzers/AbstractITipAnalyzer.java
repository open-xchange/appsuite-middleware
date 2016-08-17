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

package com.openexchange.calendar.itip.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnalyzer;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipChange.Type;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.ArgumentType;
import com.openexchange.calendar.itip.generators.HTMLWrapper;
import com.openexchange.calendar.itip.generators.Sentence;
import com.openexchange.calendar.itip.generators.TypeWrapper;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriber;
import com.openexchange.calendar.itip.generators.changes.PassthroughWrapper;
import com.openexchange.calendar.itip.generators.changes.generators.Details;
import com.openexchange.calendar.itip.generators.changes.generators.Participants;
import com.openexchange.calendar.itip.generators.changes.generators.Rescheduling;
import com.openexchange.calendar.itip.generators.changes.generators.ShownAs;
import com.openexchange.context.ContextService;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
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
			Appointment.SEQUENCE, Appointment.ALARM };
	protected ITipIntegrationUtility util;
	protected ServiceLookup services;

	@Override
    public ITipAnalysis analyze(final ITipMessage message, Map<String, String> header,
			final String style, final Session session) throws OXException {
		if (header == null) {
			header = new HashMap<String, String>();
		}
		header = lowercase(header);
		if (services != null) {

			final ContextService contexts = services.getService(ContextService.class);
			final UserService users = services.getService(UserService.class);

			final Context ctx = contexts.getContext(session.getContextId());
			final User user = users.getUser(session.getUserId(), ctx);

			return analyze(message, header, wrapperFor(style), user.getLocale(), user, ctx, session);

		}

		return analyze(message, header, wrapperFor(style), null, null, null, session);
	}

	private Map<String, String> lowercase(final Map<String, String> header) {
		final Map<String, String> copy = new HashMap<String, String>();
		for(final Map.Entry<String, String> entry : header.entrySet()) {
			copy.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		return copy;
	}

	protected abstract ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, Session session) throws OXException;

	public AbstractITipAnalyzer(final ITipIntegrationUtility util,
			final ServiceLookup services) {
		this.util = util;
		this.services = services;
	}

	protected TypeWrapper wrapperFor(final String style) {
		TypeWrapper w = new PassthroughWrapper();
		if (style != null && style.equalsIgnoreCase("html")) {
			w = new HTMLWrapper();
		}
		return w;
	}


	public void describeDiff(final ITipChange change, final TypeWrapper wrapper,
			final Session session, ITipMessage message) throws OXException {
		if (services == null) {
			change.setDiffDescription(new ArrayList<String>());
			return;
		}

		final ContextService contexts = services.getService(ContextService.class);
		final UserService users = services.getService(UserService.class);
		final GroupService groups = services.getService(GroupService.class);
		final ResourceService resources = services.getService(ResourceService.class);

		final Context ctx = contexts.getContext(session.getContextId());
		final User user = users.getUser(session.getUserId(), ctx);

		switch (change.getType()) {
		case CREATE:
			createIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		case UPDATE:
			updateIntro(change, users, ctx, wrapper, user.getLocale(), message);
			break;
		case CREATE_DELETE_EXCEPTION:
		case DELETE:
			deleteIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		}

		final Appointment currentAppointment = change.getCurrentAppointment();
		final CalendarDataObject newAppointment = change.getNewAppointment();

		if (currentAppointment == null || newAppointment == null) {
			change.setDiffDescription(new ArrayList<String>());
			return;
		}



		String organizer = currentAppointment.getOrganizer();
		if (organizer == null) {
			organizer = newAppointment.getOrganizer();
		}


		final ChangeDescriber cd = new ChangeDescriber(new Rescheduling(),
				new Details(), new Participants(users, groups, resources,
						true), new ShownAs());

		final List<String> descriptions = cd.getChanges(ctx, currentAppointment,
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
			updateIntro(change, users, ctx, wrapper, user.getLocale(), message);
			break;
		case CREATE_DELETE_EXCEPTION:
		case DELETE:
			deleteIntro(change, users, ctx, wrapper, user.getLocale());
			break;
		}
	}

	private void deleteIntro(final ITipChange change, final UserService users, final Context ctx,
			final TypeWrapper wrapper, final Locale locale) throws OXException {
		final String displayName = displayNameFor(change.getDeletedAppointment()
				.getOrganizer(), users, ctx);
		change.setIntroduction(new Sentence(Messages.DELETE_INTRO).add(
				displayName, ArgumentType.PARTICIPANT).getMessage(wrapper,
				locale));

	}

	private void updateIntro(final ITipChange change, final UserService users, final Context ctx,
			final TypeWrapper wrapper, final Locale locale, ITipMessage message) throws OXException {
		String displayName = displayNameFor(change.getCurrentAppointment()
				.getOrganizer(), users, ctx);
		if (onlyStateChanged(change.getDiff())) {
			// External Participant

			ConfirmStatus newStatus = null;

			final AppointmentDiff diff = change.getDiff();
			FieldUpdate update = diff
					.getUpdateFor(CalendarFields.CONFIRMATIONS);
			if (update != null) {
				final Difference difference = (Difference) update.getExtraInfo();
				final List<Change> changed = difference.getChanged();
				if (changed != null && !changed.isEmpty()) {
					final ConfirmationChange chng = (ConfirmationChange) changed
							.get(0);
					displayName = chng.getIdentifier();
					newStatus = ConfirmStatus.byId(chng.getNewStatus());
				}
			}

			// Internal Participant
			update = diff.getUpdateFor("users");
			if (update != null && newStatus == null) {
				final Difference difference = (Difference) update.getExtraInfo();
				final List<Change> changed = difference.getChanged();
				if (changed != null && !changed.isEmpty()) {
					final ConfirmationChange chng = (ConfirmationChange) changed
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
							ArgumentType.PARTICIPANT).add("",
							ArgumentType.STATUS, newStatus);
					break;
				case DECLINE:
					sentence = new Sentence(Messages.DECLINE_INTRO).add(
							displayName, ArgumentType.PARTICIPANT).add(
							"", ArgumentType.STATUS, newStatus);
					break;
				case TENTATIVE:
					sentence = new Sentence(Messages.TENTATIVE_INTRO).add(
							displayName, ArgumentType.PARTICIPANT).add(
							"", ArgumentType.STATUS, newStatus);
					break;
				}

				if (sentence != null) {
					change.setIntroduction(sentence.getMessage(wrapper, locale));
				}
			}
		} else {
		    if (message.getMethod() != ITipMethod.COUNTER) {
	            change.setIntroduction(new Sentence(Messages.UPDATE_INTRO).add(
                    displayName, ArgumentType.PARTICIPANT).getMessage(wrapper,
                    locale));		        
		    }
		}
	}

	private boolean onlyStateChanged(final AppointmentDiff diff) {
		// First, let's see if any fields besides the state tracking fields have
		// changed
		final HashSet<String> differing = new HashSet<String>(
				diff.getDifferingFieldNames());

		for (final String field : new String[] { CalendarFields.PARTICIPANTS,
				CalendarFields.USERS, CalendarFields.CONFIRMATIONS }) {
			differing.remove(field);
		}
		if (!differing.isEmpty()) {
			return false;
		}

		// Hm, okay, so no let's see if any participants were added or removed.
		for (final String field : new String[] { CalendarFields.PARTICIPANTS,
				CalendarFields.USERS, CalendarFields.CONFIRMATIONS }) {
			final FieldUpdate update = diff.getUpdateFor(field);
			if (update == null) {
				continue;
			}
			final Difference extraInfo = (Difference) update.getExtraInfo();
			if (!extraInfo.getAdded().isEmpty()) {
				return false;
			}
			if (!extraInfo.getRemoved().isEmpty()) {
				return false;
			}

		}

		return true;
	}

	private void createIntro(final ITipChange change, final UserService users, final Context ctx,
			final TypeWrapper wrapper, final Locale locale) throws OXException {
		final String displayName = displayNameFor(change.getNewAppointment()
				.getOrganizer(), users, ctx);
		change.setIntroduction(new Sentence(Messages.CREATE_INTRO).add(
				displayName, ArgumentType.PARTICIPANT).getMessage(wrapper,
				locale));
	}

	protected String displayNameFor(String organizer, final UserService users,
			final Context ctx) throws OXException {
		if (organizer == null) {
			return "unknown";
		}
		organizer = organizer.toLowerCase();
		if (organizer.startsWith("mailto:")) {
			organizer = organizer.substring(7);
		}

		try {
			final User result = users.searchUser(organizer, ctx);
			if (result != null) {
				if (result.getDisplayName() != null) {
					return result.getDisplayName();
				}
			}
		} catch (final OXException x) {
			return organizer;
		}

		return organizer;
	}

	protected Appointment findAndRemoveMatchingException(final Appointment exception, final List<Appointment> exceptions) {
		for (final Iterator<Appointment> iterator = exceptions.iterator(); iterator.hasNext();) {
			final Appointment existingException = iterator.next();
			if (sameDay(existingException.getRecurrenceDatePosition(), exception.getRecurrenceDatePosition())) {
				iterator.remove();
				return existingException;
			}
		}
		return null;
	}

	private boolean sameDay(final Date date1, final Date date2) {
		final GregorianCalendar gregorianCalendar1 = new GregorianCalendar();
		gregorianCalendar1.setTime(date1);
		gregorianCalendar1.setTimeZone(TimeZone.getTimeZone("UTC"));

		final GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
		gregorianCalendar2.setTime(date2);
		gregorianCalendar2.setTimeZone(TimeZone.getTimeZone("UTC"));

		for (final int field : new int[] { Calendar.DAY_OF_YEAR, Calendar.YEAR }) {
			if (gregorianCalendar1.get(field) != gregorianCalendar2.get(field)) {
				return false;
			}
		}

		return true;
	}

	public boolean doAppointmentsDiffer(final Appointment update, final Appointment original) {
		final AppointmentDiff diff = AppointmentDiff.compare(original, update, SKIP);

		return !diff.getDifferingFieldNames().isEmpty();
	}

	public boolean hasConflicts(final ITipAnalysis analysis) {
		for (final ITipChange change : analysis.getChanges()) {
			if (change.getConflicts() != null
					&& !change.getConflicts().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void purgeConflicts(final ITipAnalysis analysis) {
		final Map<Integer, ITipChange> knownAppointments = new HashMap<Integer, ITipChange>();
		for (final ITipChange change : analysis.getChanges()) {
			final Appointment currentAppointment = change.getCurrentAppointment();
			if (currentAppointment != null) {
				knownAppointments.put(currentAppointment.getObjectID(), change);
			}
			final Appointment deletedAppointment = change.getDeletedAppointment();
			if (deletedAppointment != null) {
				knownAppointments.put(deletedAppointment.getObjectID(), change);
			}
		}

		for (final ITipChange change : analysis.getChanges()) {
			final List<Appointment> conflicts = change.getConflicts();
			if (conflicts == null) {
				continue;
			}
			final CalendarDataObject newAppointment = change.getNewAppointment();
			if (newAppointment == null) {
				continue;
			}
			final Appointment currentAppointment = change.getCurrentAppointment();
			final CalendarDataObject masterAppointment = change
					.getMasterAppointment();
			for (final Iterator<Appointment> iterator = conflicts.iterator(); iterator
					.hasNext();) {
				final Appointment conflict = iterator.next();
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
				final ITipChange changeToConflict = knownAppointments.get(conflict
						.getObjectID());
				if (changeToConflict == null) {
					continue;
				}
				if (changeToConflict.getType() == ITipChange.Type.DELETE) {
					iterator.remove();
				} else {
					final CalendarDataObject changedAppointment = changeToConflict
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

	public boolean overlaps(final CalendarDataObject app1, final CalendarDataObject app2) {
		if (app2.getStartDate().after(app1.getEndDate())) {
			return false;
		}

		if (app1.getStartDate().after(app2.getEndDate())) {
			return false;
		}

		return true;
	}

	public boolean isCreate(final ITipAnalysis analysis) {
		for (final ITipChange change : analysis.getChanges()) {
			if (change.getType() == Type.CREATE) {
				return true;
			}
		}
		return false;
	}

	public boolean rescheduling(final ITipAnalysis analysis) {
		for (final ITipChange change : analysis.getChanges()) {
			if (change.getType() == Type.CREATE && change.isException()) {
				final AppointmentDiff diff = AppointmentDiff.compare(
						change.getCurrentAppointment(),
						change.getNewAppointment());
				if (diff.anyFieldChangedOf(CalendarFields.START_DATE,
						CalendarFields.END_DATE)) {
					return true;
				}
				return false;
			}
			if (change.getType() != Type.UPDATE) {
				return true;
			}
			final AppointmentDiff diff = change.getDiff();
			if (diff != null
					&& diff.anyFieldChangedOf(CalendarFields.START_DATE,
							CalendarFields.END_DATE)) {
				return true;
			}
		}
		return false;
	}

	protected void ensureParticipant(final CalendarDataObject appointment, final Session session, int owner) {
        final int confirm = CalendarObject.NONE;
        final Participant[] participants = appointment.getParticipants();
        boolean found = false;
        if (null != participants) {
            for (final Participant participant : participants) {
                if (participant instanceof UserParticipant) {
                    final UserParticipant up = (UserParticipant) participant;
                    if (up.getIdentifier() == owner) {
                        found = true;
                    }
                }
            }
        }

        if (!found) {
            final UserParticipant up = new UserParticipant(owner);
            up.setConfirm(confirm);
            final Participant[] tmp = appointment.getParticipants();
            final List<Participant> participantList = (null == tmp) ? new ArrayList<Participant>(1) : new ArrayList<Participant>(Arrays.asList(tmp));
            participantList.add(up);
            appointment.setParticipants(participantList);
        }

        if (appointment.getUsers() != null) {
            found = false;
            final UserParticipant[] users = appointment.getUsers();
            if (users != null) {
                for (final UserParticipant userParticipant : users) {
                    if (userParticipant.getIdentifier() == owner) {
                        found = true;
                    }
                }
            }
    
            if (!found) {
                final UserParticipant up = new UserParticipant(owner);
                up.setConfirm(confirm);
                final UserParticipant[] tmp = appointment.getUsers();
                final List<UserParticipant> participantList = (tmp == null) ? new ArrayList<UserParticipant>(1) : new ArrayList<UserParticipant>(Arrays.asList(tmp));
                participantList.add(up);
                appointment.setUsers(participantList);
            }
        }
    }
}
