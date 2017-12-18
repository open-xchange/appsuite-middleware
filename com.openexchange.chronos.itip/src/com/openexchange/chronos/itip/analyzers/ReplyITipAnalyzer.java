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

package com.openexchange.chronos.itip.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.common.mapping.EventUpdateImpl;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipSpecialHandling;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.ParticipantChange;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.itip.generators.changes.ChangeDescriber;
import com.openexchange.chronos.itip.generators.changes.generators.Details;
import com.openexchange.chronos.itip.generators.changes.generators.Rescheduling;
import com.openexchange.chronos.itip.generators.changes.generators.Transparency;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.UserService;

/**
 * {@link ReplyITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public class ReplyITipAnalyzer extends AbstractITipAnalyzer {

    public ReplyITipAnalyzer(final ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final CalendarSession session) throws OXException {

        final ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        final Event update = message.getEvent();

        String uid = null;
        if (update != null) {
            uid = update.getUid();
        } else {
            for (final Event appointment : message.exceptions()) {
                uid = appointment.getUid();
                if (uid != null) {
                    break;
                }
            }
        }

        analysis.setUid(uid);

        final Event original = util.resolveUid(uid, session);
        if (original == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CHANGE_PARTICIPANT_STATE_IN_UNKNOWN_APPOINTMENT, locale));
            return analysis;
        }

        if (update != null) {
            final ParticipantChange participantChange = applyParticipantChange(update, original, message.getMethod(), message);
            if (participantChange != null) {
                participantChange.setComment(message.getComment());
            }
            if (participantChange != null || message.getMethod() == ITipMethod.COUNTER) {
                final ITipChange change = new ITipChange();
                change.setNewEvent(update);
                change.setCurrentEvent(original);

                change.setType(Type.UPDATE);
                change.setParticipantChange(participantChange);
                describeReplyDiff(message, change, wrapper, session);
                analysis.addChange(change);
            }
        }

        final List<Event> exceptions = util.getExceptions(original, session.getSession());
        for (final Event exception : message.exceptions()) {
            final Event matchingException = findAndRemoveMatchingException(exception, exceptions);
            ITipChange change = new ITipChange();
            change.setException(true);
            change.setMaster(original);
            if (matchingException != null) {
                ParticipantChange participantChange = applyParticipantChange(exception, matchingException, message.getMethod(), message);

                change = new ITipChange();
                change.setException(true);
                change.setNewEvent(exception);
                change.setCurrentEvent(matchingException);

                change.setType(Type.UPDATE);
                if (participantChange != null) {
                    participantChange.setComment(message.getComment());
                    change.setParticipantChange(participantChange);
                }
                describeReplyDiff(message, change, wrapper, session);

                analysis.addChange(change);
            } else {
                ParticipantChange participantChange = applyParticipantChange(exception, original, message.getMethod(), message);
                change.setCurrentEvent(original);
                change.setNewEvent(exception);
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
        } else if (message.getMethod() == ITipMethod.COUNTER) {
            analysis.recommendActions(ITipAction.UPDATE, ITipAction.DECLINECOUNTER);
        } else {
            if (containsChangesForUpdate(analysis)) {
                analysis.recommendAction(ITipAction.UPDATE);
            }
        }
        return analysis;
    }

    private boolean containsChangesForUpdate(ITipAnalysis analysis) throws OXException {
        if (analysis.getChanges() == null || analysis.getChanges().size() == 0) {
            return false;
        }

        for (ITipChange change : analysis.getChanges()) {
            if (change.getDiff() == null) {
                continue;
            }

            if (change.getDiff().getUpdatedFields() == null) {
                continue;
            }

            if (change.getDiff().getUpdatedFields().size() != 0) {
                return true;
            }
        }
        return false;
    }

    private void describeReplyDiff(final ITipMessage message, final ITipChange change, final TypeWrapper wrapper, final CalendarSession session) throws OXException {
        final ContextService contexts = Services.getService(ContextService.class);
        final UserService users = Services.getService(UserService.class);

        final Context ctx = contexts.getContext(session.getContextId());
        final User user = users.getUser(session.getUserId(), ctx);
        final Locale locale = user.getLocale();
        final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());

        if (message.getMethod() == ITipMethod.COUNTER) {

            final ITipEventUpdate diff = change.getDiff();
            String displayName = null;
            ParticipationStatus newStatus = null;

            // External Participant

            outer: for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : diff.getAttendeeUpdates().getUpdatedItems()) {
                for (AttendeeField attendeeField : attendeeUpdate.getUpdatedFields()) {
                    if (attendeeField.equals(AttendeeField.PARTSTAT)) {
                        newStatus = attendeeUpdate.getUpdate().getPartStat();
                        displayName = attendeeUpdate.getOriginal().getCn() == null ? attendeeUpdate.getOriginal().getEMail() : attendeeUpdate.getOriginal().getCn();
                        break outer;
                    }
                }
            }

            if (newStatus != null) {
                change.setIntroduction(new Sentence(Messages.COUNTER_REPLY_INTRO).add(displayName, ArgumentType.PARTICIPANT).addStatus(newStatus).getMessage(wrapper, locale));
            }

            final ChangeDescriber cd = new ChangeDescriber(new Rescheduling(), new Details(), new Transparency());

            change.setDiffDescription(cd.getChanges(ctx, change.getCurrentEvent(), change.getNewEvent(), diff, wrapper, locale, tz));

        } else {
            describeDiff(change, wrapper, session, message);
        }
    }

    private boolean containsPartyCrasher(final ITipAnalysis analysis) {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getParticipantChange() != null && change.getParticipantChange().isPartyCrasher()) {
                return true;
            }
        }
        return false;
    }

    private ParticipantChange applyParticipantChange(final Event update, final Event original, final ITipMethod method, final ITipMessage message) throws OXException {

        discardAllButFirst(update);

        final ParticipantChange pChange = new ParticipantChange();
        boolean noChange = true;

        if (method == ITipMethod.COUNTER) {
            // Alright, the counter may overwrite any field
            ITipEventUpdate diff = new ITipEventUpdate(new EventUpdateImpl(original, update, false, new EventField[] { EventField.ATTENDEES }));
            Set<EventField> skipFields = skipFieldsInCounter(message);

            for (EventField field : EventField.values()) {
                if (skipFields.contains(field)) {
                    continue; // Skip
                }
                if (field != EventField.ATTENDEES && !diff.getUpdatedFields().contains(field)) {
                    if (original.isSet(field)) {
                        EventMapper.getInstance().copy(original, update, new EventField[] { field });
                    }
                }
            }

            if (message.hasFeature(ITipSpecialHandling.MICROSOFT)) {
                // Explicitely ignore title update
                update.setSummary(original.getSummary());
            }
        } else if (!update.containsRecurrenceId()) {
            // The Reply may only override participant states
            final ITipEventUpdate diff = new ITipEventUpdate(new EventUpdateImpl(original, update, false, new EventField[] { EventField.ATTENDEES }));
            for (EventField updatedField : diff.getUpdatedFields()) {
                EventMapper.getInstance().copy(diff.getUpdate(), update, new EventField[] { updatedField });
            }
        } else {
            // hier wir
        }

        List<Attendee> newUsers = new ArrayList<>();
        List<Attendee> users = original.getAttendees();
        Set<String> notFound = new HashSet<>();
        List<Attendee> users3 = update.getAttendees();
        if (users3 != null) {
            for (Attendee user : users3) {
                notFound.add(user.getEMail());
            }
        }

        if (users != null) {
            for (final Attendee up : users) {
                notFound.remove(up.getEMail());
                boolean added = false;
                List<Attendee> users2 = update.getAttendees();
                if (users2 != null) {
                    for (Attendee up2 : users2) {
                        if (up2.getEntity() == up.getEntity()) {
                            Attendee nup = new Attendee();
                            nup.setEntity(up.getEntity());
                            nup.setPartStat(up2.getPartStat());
                            nup.setComment(up2.getComment());
                            nup.setUri(up2.getUri());

                            pChange.setComment(up2.getComment());
                            pChange.setConfirmStatusUpdate(up2.getPartStat());

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
                for (String nf : notFound) {
                    for (Attendee participant : users3) {
                        if (nf.equalsIgnoreCase(participant.getEMail())) {

                            newUsers.add(participant);
                            pChange.setPartyCrasher(true);

                            noChange = false;
                        }
                    }
                }
            }
        }

        update.setAttendees(newUsers);

        if (noChange) {
            return null;
        }

        return pChange;
    }

    private Set<EventField> skipFieldsInCounter(final ITipMessage message) {
        final Set<EventField> skipList = new HashSet<>();
        if (message.hasFeature(ITipSpecialHandling.MICROSOFT)) {
            skipList.add(EventField.SUMMARY);
        }
        return skipList;
    }

    private void discardAllButFirst(final Event update) {
        if (update.getAttendees() != null && update.getAttendees().size() > 0) {
            Attendee first = update.getAttendees().get(0);
            update.getAttendees().clear();
            update.getAttendees().add(first);
        }
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.REPLY);
    }

}
