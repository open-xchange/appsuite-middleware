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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipChange.Type;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.ParticipantChange;
import com.openexchange.calendar.itip.generators.ArgumentType;
import com.openexchange.calendar.itip.generators.Sentence;
import com.openexchange.calendar.itip.generators.TypeWrapper;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriber;
import com.openexchange.calendar.itip.generators.changes.generators.Details;
import com.openexchange.calendar.itip.generators.changes.generators.Rescheduling;
import com.openexchange.calendar.itip.generators.changes.generators.ShownAs;
import com.openexchange.context.ContextService;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.data.conversion.ical.itip.ITipSpecialHandling;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link ReplyITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public class ReplyITipAnalyzer extends AbstractITipAnalyzer {

	public ReplyITipAnalyzer(final ITipIntegrationUtility util, final ServiceLookup services) {
		super(util, services);
	}

	@Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final Session session) throws OXException {

		final ITipAnalysis analysis = new ITipAnalysis();
		analysis.setMessage(message);

		final CalendarDataObject update = message.getDataObject();

		String uid = null;
		if (update != null) {
			uid = update.getUid();
		} else {
			for (final Appointment appointment : message.exceptions()) {
				uid = appointment.getUid();
				if (uid != null) {
					break;
				}
			}
		}

		analysis.setUid(uid);

		final CalendarDataObject original = util.resolveUid(uid, session);
		if (original == null) {
			analysis.addAnnotation(new ITipAnnotation(
					Messages.CHANGE_PARTICIPANT_STATE_IN_UNKNOWN_APPOINTMENT, locale));
			return analysis;
		}

		if (update != null) {
			final ParticipantChange participantChange = applyParticipantChange(
					update, original, message.getMethod(), message);
			if (participantChange != null) {
				participantChange.setComment(message.getComment());
			}
			if (participantChange != null || message.getMethod() == ITipMethod.COUNTER) {
				final ITipChange change = new ITipChange();
				change.setNewAppointment(update);
				change.setCurrentAppointment(original);

				change.setType(Type.UPDATE);
				change.setParticipantChange(participantChange);
				describeReplyDiff(message, change, wrapper, session);
				analysis.addChange(change);
			}
		}

		final List<Appointment> exceptions = util.getExceptions(original, session);
		for (final CalendarDataObject exception : message.exceptions()) {
			final Appointment matchingException = findAndRemoveMatchingException(
					exception, exceptions);
			ITipChange change = new ITipChange();
			change.setException(true);
			change.setMaster(original);
			if (matchingException != null) {
			    ParticipantChange participantChange = applyParticipantChange(exception, matchingException, message.getMethod(), message);

				change = new ITipChange();
				change.setException(true);
				change.setNewAppointment(exception);
				change.setCurrentAppointment(matchingException);

				change.setType(Type.UPDATE);
				if (participantChange != null) {
                    participantChange.setComment(message.getComment());
    				change.setParticipantChange(participantChange);
				}
				describeReplyDiff(message, change, wrapper, session);

				analysis.addChange(change);
			} else {
                ParticipantChange participantChange = applyParticipantChange(exception, original, message.getMethod(), message);
			    change.setCurrentAppointment(original);
			    change.setNewAppointment(exception);
			    change.setType(Type.CREATE);
			    if (participantChange != null) {
			        change.setParticipantChange(participantChange);
			    }
			    describeReplyDiff(message, change, wrapper, session);
			    analysis.addChange(change);
			    
				//analysis.addAnnotation(new ITipAnnotation(Messages.CHANGE_PARTICIPANT_STATE_IN_UNKNOWN_APPOINTMENT, locale));
			}
		}
		if (containsPartyCrasher(analysis)) {
			analysis.recommendAction(ITipAction.ACCEPT_PARTY_CRASHER);
		} else if (message.getMethod() == ITipMethod.COUNTER){
			analysis.recommendActions(ITipAction.UPDATE, ITipAction.DECLINECOUNTER);
		} else {
		    if (containsChangesForUpdate(analysis)) {
		        analysis.recommendAction(ITipAction.UPDATE);
		    }
		}
		return analysis;
	}

	private boolean containsChangesForUpdate(ITipAnalysis analysis) {
	    if (analysis.getChanges() == null || analysis.getChanges().size() == 0) {
	        return false;
	    }

	    for (ITipChange change : analysis.getChanges()) {
	        if (change.getDiff() == null) {
	            continue;
	        }

	        if (change.getDiff().getUpdates() == null) {
	            continue;
	        }

	        if (change.getDiff().getUpdates().size() != 0) {
	            return true;
	        }
	    }
	    return false;
	}

	private void describeReplyDiff(final ITipMessage message, final ITipChange change, final TypeWrapper wrapper,
			final Session session) throws OXException {
		if (services == null) {
			return;
		}

		final ContextService contexts = services.getService(ContextService.class);
		final UserService users = services.getService(UserService.class);

		final Context ctx = contexts.getContext(session.getContextId());
		final User user = users.getUser(session.getUserId(), ctx);
		final Locale locale = user.getLocale();
		final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());

		if (message.getMethod() == ITipMethod.COUNTER) {

			final AppointmentDiff diff = change.getDiff();
			String displayName = null;
			ConfirmStatus newStatus = null;

			// TODO

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

			if (newStatus != null) {
				change.setIntroduction(new Sentence(Messages.COUNTER_REPLY_INTRO)
				.add(displayName, ArgumentType.PARTICIPANT)
				.addStatus(newStatus).getMessage(wrapper, locale));
			}

			final ChangeDescriber cd = new ChangeDescriber(new Rescheduling(),
					new Details(), new ShownAs());

			change.setDiffDescription(cd.getChanges(ctx, change.getCurrentAppointment(), change.getNewAppointment(), diff, wrapper, locale, tz));

		} else {
			describeDiff(change, wrapper, session, message);
		}
	}

	private boolean containsPartyCrasher(final ITipAnalysis analysis) {
		for (final ITipChange change : analysis.getChanges()) {
			if (change.getParticipantChange() != null
					&& change.getParticipantChange().isPartyCrasher()) {
				return true;
			}
		}
		return false;
	}

	private ParticipantChange applyParticipantChange(final Appointment update,
			final Appointment original, final ITipMethod method, final ITipMessage message) {

		discardAllButFirst(update);

		final ParticipantChange pChange = new ParticipantChange();
		boolean noChange = true;

		if (method == ITipMethod.COUNTER) {
			// Alright, the counter may overwrite any field
			final AppointmentDiff diff = AppointmentDiff.compare(original, update,
					CalendarObject.PARTICIPANTS, CalendarObject.USERS,
					CalendarObject.CONFIRMATIONS);
			final Set<Integer> skipFields = skipFieldsInCounter(message);


			for(final int field : Appointment.ALL_COLUMNS) {
				if (skipFields.contains(field)) {
					continue; // Skip
				}
				if (field != CalendarObject.PARTICIPANTS && field != CalendarObject.USERS && field != CalendarObject.CONFIRMATIONS && !diff.anyFieldChangedOf(field)) {
					if (original.contains(field)) {
						update.set(field, original.get(field));
					}
				}
			}

			if (message.hasFeature(ITipSpecialHandling.MICROSOFT)) {
				// Explicitely ignore title update
				update.setTitle(original.getTitle());
			}
		} else if (!update.containsRecurrenceDatePosition() && !update.containsRecurrencePosition()) {
			// The Reply may only override participant states
			final AppointmentDiff diff = AppointmentDiff.compare(update, original,
					CalendarObject.PARTICIPANTS, CalendarObject.USERS,
					CalendarObject.CONFIRMATIONS);
			for (final FieldUpdate upd : diff.getUpdates()) {
				update.set(upd.getFieldNumber(), upd.getNewValue());
			}
		} else {
		    // hier wir
		}

		final List<Participant> newParticipants = new ArrayList<Participant>();
		final Participant[] participants = original.getParticipants();
		Set<String> notFound = new HashSet<String>();
		final Participant[] participants3 = update.getParticipants();
		if (participants3 != null) {
			for (final Participant participant : participants3) {
				notFound.add(participant.getEmailAddress());
			}
		}

		if (participants != null) {
			for (final Participant participant : participants) {
				notFound.remove(participant.getEmailAddress());
				boolean added = false;
				if (participant instanceof UserParticipant) {
					final UserParticipant up = (UserParticipant) participant;
					final Participant[] participants2 = update.getParticipants();
					if (participants2 != null) {
						for (final Participant participant2 : participants2) {
							if (participant2 instanceof UserParticipant) {
								final UserParticipant up2 = (UserParticipant) participant2;
								if (up2.getIdentifier() == up.getIdentifier()) {
									final UserParticipant nup = new UserParticipant(
											up.getIdentifier());
									nup.setConfirm(up2.getConfirm());
									nup.setConfirmMessage(up2.getConfirmMessage());

									pChange.setComment(up2.getConfirmMessage());
									pChange.setConfirmStatusUpdate(ConfirmStatus
											.byId(up2.getConfirm()));

									newParticipants.add(nup);
									added = true;
									noChange = false;
								}
							}
						}
					}
				} else if (participant instanceof ExternalUserParticipant) {
					final ExternalUserParticipant ep = (ExternalUserParticipant) participant;
					final Participant[] participants2 = update.getParticipants();
					if (participants2 != null) {
						for (final Participant participant2 : participants2) {
							if (participant2 instanceof ExternalUserParticipant) {
								final ExternalUserParticipant ep2 = (ExternalUserParticipant) participant2;
								if (ep2.getEmailAddress().equalsIgnoreCase(
										ep.getEmailAddress())) {
									final ExternalUserParticipant nup = new ExternalUserParticipant(
											ep.getEmailAddress());
									nup.setStatus(ep2.getStatus());
									nup.setMessage(ep2.getMessage());

									pChange.setComment(ep2.getMessage());
									pChange.setConfirmStatusUpdate(ConfirmStatus
											.byId(ep2.getConfirm()));

									newParticipants.add(nup);
									noChange = false;
									added = true;
								}
							}
						}
					}
				}

				if (!added) {
					newParticipants.add(participant);
				}
			}

			if (participants3 != null) {
				for (final String nf : notFound) {
					for (final Participant participant : participants3) {
						if (nf.equalsIgnoreCase(participant.getEmailAddress())) {

							newParticipants.add(participant);
							pChange.setPartyCrasher(true);

							noChange = false;
						}
					}
				}
			}
		}

		update.setParticipants(newParticipants);

		// Users
		final List<UserParticipant> newUsers = new ArrayList<UserParticipant>();
		final UserParticipant[] users = original.getUsers();
		notFound = new HashSet<String>();
		final UserParticipant[] users3 = update.getUsers();
		if (users3 != null) {
			for (final UserParticipant user : users3) {
				notFound.add(user.getEmailAddress());
			}
		}

		if (users != null) {
			for (final UserParticipant up : users) {
				notFound.remove(up.getEmailAddress());
				boolean added = false;
				final UserParticipant[] users2 = update.getUsers();
				if (users2 != null) {
					for (final UserParticipant up2 : users2) {
						if (up2.getIdentifier() == up.getIdentifier()) {
							final UserParticipant nup = new UserParticipant(
									up.getIdentifier());
							nup.setConfirm(up2.getConfirm());
							nup.setConfirmMessage(up2.getConfirmMessage());

							pChange.setComment(up2.getConfirmMessage());
							pChange.setConfirmStatusUpdate(ConfirmStatus
									.byId(up2.getConfirm()));

							newUsers.add(nup);
							added = true;
							noChange = false;
						}
					}
				}

				if (!added) {
					newUsers.add(up);
				}
			}

			if (users3 != null) {
				for (final String nf : notFound) {
					for (final UserParticipant participant : users3) {
						if (nf.equalsIgnoreCase(participant.getEmailAddress())) {

							newUsers.add(participant);
							pChange.setPartyCrasher(true);

							noChange = false;
						}
					}
				}
			}
		}

		update.setUsers(newUsers);

		// Confirmations
		final List<ConfirmableParticipant> newConfirmations = new ArrayList<ConfirmableParticipant>();
		final ConfirmableParticipant[] confirmations = original.getConfirmations();
		notFound = new HashSet<String>();
		final ConfirmableParticipant[] confirmations3 = update.getConfirmations();
		if (confirmations3 != null) {
			for (final ConfirmableParticipant participant : confirmations3) {
				notFound.add(participant.getEmailAddress());
			}
		}

		if (confirmations != null) {
			for (final ConfirmableParticipant participant : confirmations) {
				notFound.remove(participant.getEmailAddress());
				boolean added = false;
				if (participant instanceof ExternalUserParticipant) {
					final ExternalUserParticipant ep = (ExternalUserParticipant) participant;
					final ConfirmableParticipant[] participants2 = update.getConfirmations();
					if (confirmations != null) {
						if (participants2 != null) {
							for (final ConfirmableParticipant	 participant2 : participants2) {
								if (participant2 instanceof ExternalUserParticipant) {
									final ExternalUserParticipant ep2 = (ExternalUserParticipant) participant2;
									if (ep2.getEmailAddress().equalsIgnoreCase(
											ep.getEmailAddress())) {
										final ExternalUserParticipant nup = new ExternalUserParticipant(
												ep.getEmailAddress());
										nup.setStatus(ep2.getStatus());
										nup.setMessage(ep2.getMessage());

										pChange.setComment(ep2.getMessage());
										pChange.setConfirmStatusUpdate(ConfirmStatus
												.byId(ep2.getConfirm()));

										newConfirmations.add(nup);
										noChange = false;
										added = true;
									}
								}
							}
						}
					}
				}

				if (!added) {
					newConfirmations.add(participant);
				}
			}

			if (confirmations3 != null) {
				for (final String nf : notFound) {
					for (final ConfirmableParticipant participant : confirmations3) {
						if (nf.equalsIgnoreCase(participant.getEmailAddress())) {

							newConfirmations.add(participant);
							pChange.setPartyCrasher(true);

							noChange = false;
						}
					}
				}
			}

			update.setConfirmations(newConfirmations);
		}

		if (noChange) {
			return null;
		}

		return pChange;
	}

	private Set<Integer> skipFieldsInCounter(final ITipMessage message) {
		final Set<Integer> skipList = new HashSet<Integer>();
//		skipList.add(CommonObject.NUMBER_OF_LINKS);
		if (message.hasFeature(ITipSpecialHandling.MICROSOFT)) {
			skipList.add(CalendarObject.TITLE);
		}
		return skipList;
	}

	private void discardAllButFirst(final Appointment update) {
		Participant[] participants = update.getParticipants();
		if (participants != null && participants.length > 1) {
			participants = new Participant[]{participants[0]};
			update.setParticipants(participants);
		}

		UserParticipant[] users = update.getUsers();
		if (users != null && users.length > 1) {
			users = new UserParticipant[]{users[0]};
			update.setUsers(users);
		}

		ConfirmableParticipant[] confirmations = update.getConfirmations();
		if (confirmations != null && confirmations.length > 1) {
			confirmations = new ConfirmableParticipant[]{confirmations[0]};
			update.setConfirmations(confirmations);
		}
	}

	@Override
    public List<ITipMethod> getMethods() {
		return Arrays.asList(ITipMethod.REPLY);
	}

}
