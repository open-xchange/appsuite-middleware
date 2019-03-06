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

package com.openexchange.chronos.itip.performers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAttributes;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.java.Strings;

/**
 *
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class UpdatePerformer extends AbstractActionPerformer {

    private final static Logger LOGGER = LoggerFactory.getLogger(UpdatePerformer.class);

    public UpdatePerformer(ITipIntegrationUtility util, MailSenderService sender, ITipMailGeneratorFactory generators) {
        super(util, sender, generators);
    }

    @Override
    public Collection<ITipAction> getSupportedActions() {
        return EnumSet.of(ITipAction.ACCEPT, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.ACCEPT_PARTY_CRASHER, ITipAction.ACCEPT_AND_REPLACE, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.UPDATE, ITipAction.CREATE, ITipAction.COUNTER);
    }

    @Override
    public List<Event> perform(ITipAction action, ITipAnalysis analysis, CalendarSession session, ITipAttributes attributes) throws OXException {
        session.<Boolean> set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
        List<ITipChange> changes = analysis.getChanges();
        List<Event> result = new ArrayList<Event>(changes.size());

        Map<String, Event> processed = new HashMap<String, Event>();

        NextChange: for (ITipChange change : changes) {

            Event event = change.getNewEvent();
            if (event == null) {
                LOGGER.debug("No event found to process.");
                continue NextChange;
            }

            // TODO: event.setNotification(true);
            final int owner = analysis.getMessage().getOwner() > 0 ? analysis.getMessage().getOwner() : session.getUserId();
            boolean exceptionCreate = isExceptionCreate(change);

            ensureAttendee(event, exceptionCreate ? change.getMasterEvent() : change.getCurrentEvent(), action, owner, attributes, session);
            Event original = determineOriginalEvent(change, processed, session);

            if (original != null) {
                ITipEventUpdate diff = change.getDiff();
                if (null != diff && false == diff.isEmpty()) {
                    adjusteAttendeesPartStats(action, original, event, diff, owner);
                    event = updateEvent(original, event, session);
                    if (null == event) {
                        LOGGER.warn("No event found to process.");
                        continue NextChange;
                    }
                } else {
                    continue NextChange;
                }
            } else if (exceptionCreate) {
                Event masterEvent = original = change.getMasterEvent();
                event.setSeriesId(masterEvent.getSeriesId());
                event = updateEvent(masterEvent, event, session);
                if (null == event) {
                    LOGGER.warn("No event found to process.");
                    continue NextChange;
                }
            } else {
                ensureFolderId(event, session);
                event.removeId();
                event = createEvent(event, session);
                if (null == event) {
                    LOGGER.warn("No event found to process.");
                    continue NextChange;
                }
            }

            if (!change.isException()) {
                processed.put(event.getUid(), event);
            }

            event = util.loadEvent(event, session);
            if (event != null) {
                writeMail(action, original, event, session, owner);
                result.add(event);
            }
        }

        return result;
    }

    private Event updateEvent(Event original, Event event, CalendarSession session) throws OXException {
        EventUpdate diff = session.getUtilities().compare(original, event, true, (EventField[]) null);

        Event update = new Event();
        boolean write = false;
        if (!diff.getUpdatedFields().isEmpty()) {
            EventMapper.getInstance().copy(diff.getUpdate(), update, diff.getUpdatedFields().toArray(new EventField[diff.getUpdatedFields().size()]));
            write = true;
        }

        update.setFolderId(original.getFolderId());
        update.setId(original.getId());

        if (!update.containsSequence()) {
            update.setSequence(original.getSequence());
        }
        if (!update.containsUid()) {
            update.setUid(original.getUid());
        }
        if (!update.containsOrganizer()) {
            update.setOrganizer(original.getOrganizer());
        }
        if (original.containsSeriesId()) {
            update.setSeriesId(original.getSeriesId());
        }
        if (!original.containsRecurrenceId() && event.containsRecurrenceId()) {
            update.setRecurrenceId(event.getRecurrenceId());
        } else if (original.containsRecurrenceId()) {
            update.setRecurrenceId(original.getRecurrenceId());
        }

        if (write) {
            CalendarResult calendarResult = session.getCalendarService().updateEventAsOrganizer(session, new EventID(update.getFolderId(), update.getId()), update, original.getLastModified().getTime());
            /*
             * Check creations first because;
             * + Party crasher creates event in own folder
             * + To create exceptions master needs to be updated. Nevertheless the created exception must be returned
             */
            if (false == calendarResult.getCreations().isEmpty()) {
                update = calendarResult.getCreations().get(0).getCreatedEvent();
            }
            if (null == update && false == calendarResult.getUpdates().isEmpty()) {
                update = calendarResult.getUpdates().get(0).getUpdate();
            }
        }
        return update;
    }

    /**
     * Creates a new event based on the given event.
     *
     * @param event The event to create
     * @param session The {@link CalendarSession}
     * @return The newly created event
     * @throws OXException In case event can't be created
     */
    private Event createEvent(Event event, CalendarSession session) throws OXException {
        CalendarResult createResult = session.getCalendarService().createEvent(session, event.getFolderId(), event);
        return createResult.getCreations().get(0).getCreatedEvent();
    }

    /*
     * ==============================================================================
     * =============================== HELPERS ======================================
     * ==============================================================================
     */

    /**
     * Ensures that the given user is attendee of the event
     *
     * @param event The event to check if the user is in
     * @param currentEvent The original event
     * @param action The {@link ITipAction} to be performed
     * @param owner The user identifier
     * @param attributes The update {@link ITipAttributes}
     * @param session The {@link CalendarSession}
     */
    private void ensureAttendee(Event event, Event currentEvent, ITipAction action, int owner, ITipAttributes attributes, CalendarSession session) {
        ParticipationStatus confirm = getParticipantStatus(currentEvent, action, owner);
        String message = null;
        if (attributes != null && attributes.getConfirmationMessage() != null && Strings.isNotEmpty(attributes.getConfirmationMessage().trim())) {
            message = attributes.getConfirmationMessage();
        }

        try {
            // Trust analyze to provide accurate set of attendees and their status
            List<Attendee> attendees = new LinkedList<>(event.getAttendees());

            // Get attendee to add
            Attendee attendee = CalendarUtils.find(attendees, owner);
            if (null == attendee) {
                attendee = loadAttendee(session, owner);
            } else {
                attendees.remove(attendee);
            }

            // Update from attributes
            if (null != confirm) {
                attendee.setPartStat(confirm);
                attendee.setRsvp(false);
            }
            if (Strings.isNotEmpty(message)) {
                attendee.setComment(message);
            }
            attendees.add(attendee);
            event.setAttendees(attendees);
        } catch (OXException e) {
            LOGGER.error("Could not resolve user with identifier {}", Integer.valueOf(owner), e);
        }
    }

    private ParticipationStatus getParticipantStatus(Event currentEvent, ITipAction action, int owner) {
        switch (action) {
            case ACCEPT:
            case ACCEPT_AND_IGNORE_CONFLICTS:
            case CREATE:
                return ParticipationStatus.ACCEPTED;
            case DECLINE:
                return ParticipationStatus.DECLINED;
            case TENTATIVE:
                return ParticipationStatus.TENTATIVE;
            case UPDATE:
                // Might return null
                return getFieldValue(currentEvent, owner, AttendeeField.PARTSTAT, ParticipationStatus.class);
            default:
                // Fall through
        }
        return null;
    }

    /**
     * Loads a specific user
     *
     * @param session The {@link CalendarSession}
     * @param userId The user to load
     * @return The user as {@link Attendee}
     * @throws OXException If the user can't be found
     */
    private Attendee loadAttendee(CalendarSession session, int userId) throws OXException {
        return session.getEntityResolver().prepareUserAttendee(userId);
    }

    /**
     * Get a specific value from a specific attendee
     *
     * @param event The event containing the attendees
     * @param userId The identifier of the attendee
     * @param field The {@link AttendeeField} to get the value from
     * @param clazz The class to cast the value to
     * @return The value of the field or the default value if the field is <code>null</code>, an error occurs or the attendee is not set
     */
    private <T> T getFieldValue(Event event, int userId, AttendeeField field, Class<T> clazz) {
        try {
            if (containsAttendees(event)) {
                Attendee attendee = CalendarUtils.find(event.getAttendees(), userId);
                if (null != attendee) {
                    Mapping<? extends Object, Attendee> mapping = AttendeeMapper.getInstance().get(field);
                    return clazz.cast(mapping.get(attendee));
                }
            }
        } catch (OXException | ClassCastException e) {
            // Fall through
            LOGGER.debug("Could not get value for field {} of attendee with id {}", field, Integer.valueOf(userId), e);
        }
        return null;
    }

    private boolean containsAttendees(Event event) {
        return event != null && event.getAttendees() != null && false == event.getAttendees().isEmpty();
    }

    private boolean isExceptionCreate(ITipChange change) {
        return change.isException() && CalendarUtils.isSeriesMaster(change.getMasterEvent()) && null == change.getNewEvent().getId();
    }

    private final static Collection<ITipAction> OWN_CHANGE = EnumSet.of(ITipAction.ACCEPT, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.ACCEPT_AND_REPLACE, ITipAction.DECLINE, ITipAction.TENTATIVE);

    /**
     * Adjusted attendees status to avoid over right.
     * Only call for updates on existing events
     * 
     * @param action The {@link ITipAction}
     * @param original The original {@link Event}
     * @param event The updated {@link Event}
     * @param owner The acting user
     */
    private void adjusteAttendeesPartStats(ITipAction action, Event original, Event event, ITipEventUpdate diff, int owner) {
        if (OWN_CHANGE.contains(action) && diff.isAboutStateChangesOnly() && false == diff.isAboutCertainParticipantsStateChangeOnly(String.valueOf(owner))) {
            // Changed more than one PartStat with a action that should only update the current users status?!
            List<Attendee> attendees = event.getAttendees();
            for (Attendee o : original.getAttendees()) {
                if (CalendarUtils.isInternal(o) && false == ParticipationStatus.NEEDS_ACTION.equals(o.getPartStat())) {
                    Attendee find = CalendarUtils.find(attendees, o.getEntity());
                    if (null != find && ParticipationStatus.NEEDS_ACTION.equals(find.getPartStat())) {
                        // Copy from DB event
                        find.setPartStat(o.getPartStat());
                        if (o.containsComment()) {
                            find.setComment(o.getComment());
                        }
                    }
                }
            }
        }
    }
}
