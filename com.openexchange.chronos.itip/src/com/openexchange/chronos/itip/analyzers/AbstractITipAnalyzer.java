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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.mapping.EventUpdateImpl;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnalyzer;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.itip.generators.HTMLWrapper;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.itip.generators.changes.ChangeDescriber;
import com.openexchange.chronos.itip.generators.changes.PassthroughWrapper;
import com.openexchange.chronos.itip.generators.changes.generators.Details;
import com.openexchange.chronos.itip.generators.changes.generators.Participants;
import com.openexchange.chronos.itip.generators.changes.generators.Rescheduling;
import com.openexchange.chronos.itip.generators.changes.generators.Transparency;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;

/**
 * {@link AbstractITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public abstract class AbstractITipAnalyzer implements ITipAnalyzer {

    public static final EventField[] SKIP = new EventField[] { EventField.FOLDER_ID, EventField.ID, EventField.CREATED_BY, EventField.CREATED, EventField.TIMESTAMP, EventField.LAST_MODIFIED, EventField.MODIFIED_BY, EventField.SEQUENCE, EventField.ALARMS };
    protected ITipIntegrationUtility util;

    @Override
    public ITipAnalysis analyze(final ITipMessage message, Map<String, String> header, final String style, final CalendarSession session) throws OXException {
        if (header == null) {
            header = new HashMap<String, String>();
        }
        header = lowercase(header);

        final ContextService contexts = Services.getService(ContextService.class);
        final UserService users = Services.getService(UserService.class);

        final Context ctx = contexts.getContext(session.getContextId());
        final User user = users.getUser(session.getUserId(), ctx);

        return analyze(message, header, wrapperFor(style), user.getLocale(), user, ctx, session);
    }

    private Map<String, String> lowercase(final Map<String, String> header) {
        final Map<String, String> copy = new HashMap<String, String>();
        for (final Map.Entry<String, String> entry : header.entrySet()) {
            copy.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return copy;
    }

    protected abstract ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException;

    public AbstractITipAnalyzer(final ITipIntegrationUtility util) {
        this.util = util;
    }

    protected TypeWrapper wrapperFor(final String style) {
        TypeWrapper w = new PassthroughWrapper();
        if (style != null && style.equalsIgnoreCase("html")) {
            w = new HTMLWrapper();
        }
        return w;
    }

    public void describeDiff(final ITipChange change, final TypeWrapper wrapper, final CalendarSession session, ITipMessage message) throws OXException {

        final ContextService contexts = Services.getService(ContextService.class);
        final UserService users = Services.getService(UserService.class);
        final GroupService groups = Services.getService(GroupService.class);
        final ResourceService resources = Services.getService(ResourceService.class);

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

        final Event currentEvent = change.getCurrentEvent();
        final Event newEvent = change.getNewEvent();

        if (currentEvent == null || newEvent == null) {
            change.setDiffDescription(new ArrayList<String>());
            return;
        }

        final ChangeDescriber cd = new ChangeDescriber(new Rescheduling(), new Details(), new Participants(users, groups, resources, true), new Transparency());

        final List<String> descriptions = cd.getChanges(ctx, currentEvent, newEvent, change.getDiff(), wrapper, user.getLocale(), TimeZone.getTimeZone(user.getTimeZone()));
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

    private void deleteIntro(final ITipChange change, final UserService users, final Context ctx, final TypeWrapper wrapper, final Locale locale) throws OXException {
        final String displayName = displayNameFor(change.getDeletedEvent().getOrganizer(), users, ctx);
        change.setIntroduction(new Sentence(Messages.DELETE_INTRO).add(displayName, ArgumentType.PARTICIPANT).getMessage(wrapper, locale));

    }

    private void updateIntro(final ITipChange change, final UserService users, final Context ctx, final TypeWrapper wrapper, final Locale locale, ITipMessage message) throws OXException {
        String displayName = displayNameFor(change.getCurrentEvent().getOrganizer(), users, ctx);
        if (onlyStateChanged(change.getDiff())) {
            // External Participant

            ParticipationStatus newStatus = null;

            outer: for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : change.getDiff().getAttendeeUpdates().getUpdatedItems()) {
                for (AttendeeField attendeeField : attendeeUpdate.getUpdatedFields()) {
                    if (attendeeField.equals(AttendeeField.PARTSTAT)) {
                        newStatus = attendeeUpdate.getUpdate().getPartStat();
                        displayName = attendeeUpdate.getOriginal().getCn() == null ? attendeeUpdate.getOriginal().getEMail() : attendeeUpdate.getOriginal().getCn();
                        break outer;
                    }
                }
            }

            Sentence sentence = null;
            if (newStatus != null) {
                if (newStatus.equals(ParticipationStatus.ACCEPTED)) {
                    sentence = new Sentence(Messages.ACCEPT_INTRO).add(displayName, ArgumentType.PARTICIPANT).add("", ArgumentType.STATUS, newStatus);
                } else if (newStatus.equals(ParticipationStatus.DECLINED)) {
                    sentence = new Sentence(Messages.DECLINE_INTRO).add(displayName, ArgumentType.PARTICIPANT).add("", ArgumentType.STATUS, newStatus);
                } else if (newStatus.equals(ParticipationStatus.TENTATIVE)) {
                    sentence = new Sentence(Messages.TENTATIVE_INTRO).add(displayName, ArgumentType.PARTICIPANT).add("", ArgumentType.STATUS, newStatus);
                }

                if (sentence != null) {
                    change.setIntroduction(sentence.getMessage(wrapper, locale));
                }
            }
        } else {
            if (message.getMethod() != ITipMethod.COUNTER) {
                change.setIntroduction(new Sentence(Messages.UPDATE_INTRO).add(displayName, ArgumentType.PARTICIPANT).getMessage(wrapper, locale));
            }
        }
    }

    AttendeeField[] ALL_BUT_CONFIRMATION = new AttendeeField[] { AttendeeField.CN, AttendeeField.CU_TYPE, AttendeeField.EMAIL, AttendeeField.ENTITY, AttendeeField.FOLDER_ID, AttendeeField.MEMBER, AttendeeField.ROLE, AttendeeField.RSVP, AttendeeField.SENT_BY, AttendeeField.URI };

    private boolean onlyStateChanged(ITipEventUpdate diff) {
        if (diff.containsAnyChangesBeside(new EventField[] { EventField.ATTENDEES })) {
            return false;
        }

        CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = diff.getAttendeeUpdates();

        if (attendeeUpdates == null || attendeeUpdates.isEmpty()) {
            return true;
        }

        if (attendeeUpdates.getAddedItems() != null && !attendeeUpdates.getAddedItems().isEmpty()) {
            return false;
        }

        if (attendeeUpdates.getRemovedItems() != null && !attendeeUpdates.getRemovedItems().isEmpty()) {
            return false;
        }

        List<? extends ItemUpdate<Attendee, AttendeeField>> updatedItems = attendeeUpdates.getUpdatedItems();
        for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : updatedItems) {
            if (attendeeUpdate.containsAnyChangeOf(ALL_BUT_CONFIRMATION))
                ;
        }

        return true;
    }

    private void createIntro(final ITipChange change, final UserService users, final Context ctx, final TypeWrapper wrapper, final Locale locale) throws OXException {
        final String displayName = displayNameFor(change.getNewEvent().getOrganizer(), users, ctx);
        change.setIntroduction(new Sentence(Messages.CREATE_INTRO).add(displayName, ArgumentType.PARTICIPANT).getMessage(wrapper, locale));
    }

    protected String displayNameFor(Organizer organizer, final UserService users, final Context ctx) throws OXException {
        if (organizer == null) {
            return "unknown";
        }

        if (organizer.getCn() != null) {
            return organizer.getCn();
        }

        if (organizer.getEMail() != null) {
            return organizer.getEMail();
        }

        return "unknown";
    }

    protected Event findAndRemoveMatchingException(final Event exception, final List<Event> exceptions) {
        for (Iterator<Event> iterator = exceptions.iterator(); iterator.hasNext();) {
            Event existingException = iterator.next();
            if (existingException.getRecurrenceId().compareTo(exception.getRecurrenceId()) == 0) {
                iterator.remove();
                return existingException;
            }
        }
        return null;
    }

    public boolean doAppointmentsDiffer(final Event update, final Event original) throws OXException {
        final ITipEventUpdate diff = new ITipEventUpdate(new EventUpdateImpl(original, update, false, AbstractITipAnalyzer.SKIP));
        return !diff.getUpdatedFields().isEmpty();
    }

    public boolean hasConflicts(final ITipAnalysis analysis) {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getConflicts() != null && !change.getConflicts().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void purgeConflicts(final ITipAnalysis analysis) throws OXException {
        final Map<String, ITipChange> knownAppointments = new HashMap<>();
        for (final ITipChange change : analysis.getChanges()) {
            final Event currentAppointment = change.getCurrentEvent();
            if (currentAppointment != null) {
                knownAppointments.put(currentAppointment.getId(), change);
            }
            final Event deletedAppointment = change.getDeletedEvent();
            if (deletedAppointment != null) {
                knownAppointments.put(deletedAppointment.getId(), change);
            }
        }

        for (final ITipChange change : analysis.getChanges()) {
            final List<EventConflict> conflicts = change.getConflicts();
            if (conflicts == null) {
                continue;
            }
            final Event newAppointment = change.getNewEvent();
            if (newAppointment == null) {
                continue;
            }
            final Event currentAppointment = change.getCurrentEvent();
            final Event masterAppointment = change.getMasterEvent();
            for (final Iterator<EventConflict> iterator = conflicts.iterator(); iterator.hasNext();) {
                final EventConflict conflict = iterator.next();
                if (currentAppointment != null && (currentAppointment.getId().equals(conflict.getConflictingEvent().getId()))) {
                    iterator.remove();
                    continue;
                }
                if (masterAppointment != null && (masterAppointment.getId().equals(conflict.getConflictingEvent().getId()))) {
                    iterator.remove();
                    continue;
                }
                final ITipChange changeToConflict = knownAppointments.get(conflict.getConflictingEvent().getId());
                if (changeToConflict == null) {
                    continue;
                }
                if (changeToConflict.getType() == ITipChange.Type.DELETE) {
                    iterator.remove();
                } else {
                    final Event changedAppointment = changeToConflict.getNewEvent();
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

    public boolean overlaps(final Event event1, final Event event2) {
        if (event2.getStartDate().after(event1.getEndDate())) {
            return false;
        }

        if (event1.getStartDate().after(event2.getEndDate())) {
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

    public boolean rescheduling(final ITipAnalysis analysis) throws OXException {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getType() == Type.CREATE && change.isException()) {
                final ITipEventUpdate diff = new ITipEventUpdate(new EventUpdateImpl(change.getCurrentEvent(), change.getNewEvent(), false, (EventField[]) null));
                if (diff.containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE })) {
                    return true;
                }
                return false;
            }
            if (change.getType() != Type.UPDATE) {
                return true;
            }
            final ITipEventUpdate diff = change.getDiff();
            if (diff != null && diff.containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE })) {
                return true;
            }
        }
        return false;
    }

    protected void ensureParticipant(final Event event, final CalendarSession session, int owner) {
        List<Attendee> attendees = event.getAttendees();
        boolean found = false;
        if (attendees != null) {
            for (Attendee attendee : attendees) {
                if (attendee.getEntity() == owner) {
                    found = true;
                }
            }
        }

        if (!found) {
            Attendee attendee = new Attendee();
            attendee.setEntity(owner);
            attendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
            if (event.getAttendees() == null) {
                event.setAttendees(new ArrayList<>());
            }
            event.getAttendees().add(attendee);
        }
    }
}
