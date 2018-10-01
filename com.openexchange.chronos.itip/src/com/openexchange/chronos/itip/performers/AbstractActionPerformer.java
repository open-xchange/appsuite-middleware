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

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipActionPerformer;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.generators.ITipMailGenerator;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.tools.ITipUtils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * 
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class AbstractActionPerformer implements ITipActionPerformer {

    protected ITipIntegrationUtility       util;
    private final MailSenderService        sender;
    private final ITipMailGeneratorFactory mailGenerators;

    public AbstractActionPerformer(final ITipIntegrationUtility util, final MailSenderService mailSender, final ITipMailGeneratorFactory mailGenerators) {
        super();
        this.util = util;
        this.sender = mailSender;
        this.mailGenerators = mailGenerators;
    }

    /**
     * Get the original event
     * 
     * @param change The {@link ITipChange} to get the original event for
     * @param processed The proccessed events
     * @param session The {@link CalendarSession}
     * @return The original {@link Event} or <code>null</code> if no original exists
     * @throws OXException In case original can't be loaded
     */
    protected Event determineOriginalEvent(final ITipChange change, final Map<String, Event> processed, final CalendarSession session) throws OXException {
        if (change.getType().equals(Type.CREATE)) {
            return null;
        }
        Event currentEvent = change.getCurrentEvent();
        if (currentEvent == null || Strings.isEmpty(currentEvent.getId())) {
            if (change.isException()) {
                currentEvent = change.getMasterEvent();
                if (currentEvent == null || currentEvent.getId() != null) {
                    currentEvent = processed.get(change.getNewEvent().getUid());
                    if (currentEvent == null) {
                        currentEvent = change.getNewEvent();
                    }
                }
            }
        }
        return null == currentEvent ? null : util.loadEvent(currentEvent, session);
    }

    protected void ensureFolderId(final Event event, final CalendarSession session) throws OXException {
        if (event.containsFolderId() && event.getFolderId() != null) {
            return;
        }
        final String privateCalendarFolderId = util.getPrivateCalendarFolderId(session.getContextId(), session.getUserId());
        event.setFolderId(privateCalendarFolderId);

    }

    protected void writeMail(final ITipAction action, Event original, final Event update, final CalendarSession session, int owner) throws OXException {
        CalendarUser principal = ITipUtils.getPrincipal(session);
        switch (action) {
            case COUNTER:
                return;
            default: //Continue normally
        }
        original = constructOriginalForMail(action, original, update, session, owner);

        final ITipMailGenerator generator = mailGenerators.create(original, update, session.getSession(), owner, principal);
        switch (action) {
            case CREATE:
                if (!generator.userIsTheOrganizer()) {
                    return;
                }
                List<NotificationParticipant> recipients = generator.getRecipients();
                for (final NotificationParticipant p : recipients) {
                    final NotificationMail mail = generator.generateCreateExceptionMailFor(p);
                    if (mail != null) {
                        sender.sendMail(mail, session.getSession(), principal, null);
                    }
                }
                break;
            case UPDATE:
                if (!generator.userIsTheOrganizer()) {
                    return;
                }
                recipients = generator.getRecipients();
                for (final NotificationParticipant p : recipients) {
                    final NotificationMail mail = generator.generateUpdateMailFor(p);
                    if (mail != null) {
                        sender.sendMail(mail, session.getSession(), principal, null);
                    }
                }
                break;
            case DECLINECOUNTER:
                recipients = generator.getRecipients();
                for (final NotificationParticipant p : recipients) {
                    final NotificationMail mail = generator.generateDeclineCounterMailFor(p);
                    if (mail != null) {
                        sender.sendMail(mail, session.getSession(), principal, null);
                    }
                }
                break;
            case SEND_APPOINTMENT:
                recipients = generator.getRecipients();
                for (final NotificationParticipant p : recipients) {
                    final NotificationMail mail = generator.generateCreateMailFor(p);
                    if (mail != null) {
                        sender.sendMail(mail, session.getSession(), principal, null);
                    }
                }
                break;
            case REFRESH:
                recipients = generator.getRecipients();
                for (final NotificationParticipant p : recipients) {
                    final NotificationMail mail = generator.generateRefreshMailFor(p);
                    if (mail != null) {
                        sender.sendMail(mail, session.getSession(), principal, null);
                    }
                }
                break;
            default:
                recipients = generator.getRecipients();
                for (final NotificationParticipant p : recipients) {
                    final NotificationMail mail = generator.generateUpdateMailFor(p);
                    if (mail != null) {
                        sender.sendMail(mail, session.getSession(), principal, null);
                    }
                }
        }
    }

    private Event constructOriginalForMail(final ITipAction action, final Event original, final Event update, final CalendarSession session, int owner) throws OXException {
        switch (action) {
            case ACCEPT:
            case ACCEPT_AND_IGNORE_CONFLICTS:
            case ACCEPT_AND_REPLACE:
            case ACCEPT_PARTY_CRASHER:
            case DECLINE:
            case TENTATIVE:
                return constructFakeOriginal(update, session, owner);
            default:
                return original;
        }
    }

    private Event constructFakeOriginal(final Event event, final CalendarSession session, int owner) throws OXException {
        Event copy = session.getUtilities().copyEvent(event, (EventField[]) null);
        if (copy.containsAttendees()) {
            for (Attendee attendee : copy.getAttendees()) {
                if (attendee.getEntity() == owner) {
                    attendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
                }
            }
        }
        return copy;
    }

    protected int getOwner(CalendarSession session, ITipAnalysis analysis, Event event) {
        int owner = session.getUserId();
        if (analysis.getMessage().getOwner() > 0) {
            owner = analysis.getMessage().getOwner();
        }
        return owner;
    }

}
