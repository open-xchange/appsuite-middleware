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
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAttributes;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.UserService;

/**
 * 
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
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
        session.<Boolean>set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
        List<ITipChange> changes = analysis.getChanges();
        List<Event> result = new ArrayList<Event>(changes.size());

        Map<String, Event> processed = new HashMap<String, Event>();

        for (ITipChange change : changes) {

            Event event = change.getNewEvent();
            if (event == null) {
                continue;
            }
            // TODO: event.setNotification(true);
            int owner = session.getUserId();
            if (analysis.getMessage().getOwner() > 0) {
                owner = analysis.getMessage().getOwner();
            }
            ensureAttendee(event, change.getCurrentEvent(), action, owner, session.getContextId(), attributes);
            Event original = determineOriginalEvent(change, processed, session);
            Event forMail = event;
            if (original != null) {
                ITipEventUpdate diff = change.getDiff();
                if (null != diff && false == diff.isEmpty()) {
                    updateEvent(original, event, session);
                }
            } else {
                ensureFolderId(event, session);
                createEvent(event, session);
                forMail = util.reloadEvent(event, session);
            }

            if (event != null && !change.isException()) {
                processed.put(event.getUid(), event);
            }

            writeMail(action, original, forMail, session, owner);
            result.add(event);
        }

        return result;
    }

    private void updateEvent(Event original, Event event, CalendarSession session) throws OXException {
        EventUpdate diff = session.getUtilities().compare(original, event, false, (EventField[]) null);

        Event update = new Event();
        boolean write = false;
        if (!diff.getUpdatedFields().isEmpty()) {
            EventMapper.getInstance().copy(diff.getUpdate(), update, diff.getUpdatedFields().toArray(new EventField[diff.getUpdatedFields().size()]));
            write = true;
        }

        update.setFolderId(original.getFolderId());
        update.setId(original.getId());
        update.setSeriesId(original.getSeriesId());

        if (!original.containsRecurrenceId() && event.containsRecurrenceId()) {
            update.setRecurrenceId(event.getRecurrenceId());
        } else if (original.containsRecurrenceId()) {
            update.setRecurrenceId(original.getRecurrenceId());
        }

        if (write) {
            session.getCalendarService().updateEvent(session, new EventID(update.getFolderId(), update.getId()), update, original.getLastModified().getTime());
        }

        event.setId(update.getId());
        event.setSeriesId(update.getSeriesId());
        event.setFolderId(update.getFolderId());

        if (update.containsRecurrenceId()) {
            event.setRecurrenceId(update.getRecurrenceId());
        }
    }

    private void createEvent(Event event, CalendarSession session) throws OXException {
        CalendarResult createResult = session.getCalendarService().createEvent(session, event.getFolderId(), event);
        event.setId(createResult.getCreations().get(0).getCreatedEvent().getId());
    }

    private void ensureAttendee(Event event, Event currentEvent, ITipAction action, int owner, int contextId, ITipAttributes attributes) {
        ParticipationStatus confirm = null;
        switch (action) {
            case ACCEPT:
            case ACCEPT_AND_IGNORE_CONFLICTS:
            case CREATE:
                confirm = ParticipationStatus.ACCEPTED;
                break;
            case DECLINE:
                confirm = ParticipationStatus.DECLINED;
                break;
            case TENTATIVE:
                confirm = ParticipationStatus.TENTATIVE;
                break;
            case UPDATE:
                confirm = getCurrentConfirmation(currentEvent, owner);
                break;
            default:
                confirm = ParticipationStatus.NEEDS_ACTION;
        }

        String message = null;
        if (attributes != null && attributes.getConfirmationMessage() != null && !attributes.getConfirmationMessage().trim().equals("")) {
            message = attributes.getConfirmationMessage();
        } else {
            message = getCurrentMessage(currentEvent, owner);
        }

        boolean found = false;
        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getEntity() == owner) {
                attendee.setPartStat(confirm);
                if (message != null) {
                    attendee.setComment(message);
                }
                found = true;
            }
        }

        if (!found) {
            Attendee attendee = new Attendee();
            attendee.setEntity(owner);
            if (confirm != null) {
                attendee.setPartStat(confirm);
            }
            if (message != null) {
                attendee.setComment(message);
            }
            try {
                User user = Services.getService(UserService.class, true).getUser(owner, contextId);
                attendee.setCn(user.getDisplayName());
                attendee.setEMail(user.getMail());
                attendee.setUri(CalendarUtils.getURI(user.getMail()));
                attendee.setCuType(CalendarUserType.INDIVIDUAL);
            } catch (OXException e) {
                LOGGER.error("Could not resolve user with identifier {}", Integer.valueOf(owner), e);
            }
            event.getAttendees().add(attendee);
        }

    }

    private String getCurrentMessage(Event event, int userId) {
        if (event == null || event.getAttendees() == null || event.getAttendees().isEmpty()) {
            return null;
        }

        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getEntity() == userId) {
                return attendee.getComment();
            }
        }

        return null;
    }

    private ParticipationStatus getCurrentConfirmation(Event event, int userId) {
        if (event == null || event.getAttendees() == null || event.getAttendees().isEmpty()) {
            return null;
        }

        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getEntity() == userId) {
                return attendee.getPartStat();
            }
        }

        return null;
    }
}
