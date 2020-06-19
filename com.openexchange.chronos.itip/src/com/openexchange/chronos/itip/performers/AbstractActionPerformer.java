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
import javax.mail.internet.InternetAddress;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipActionPerformer;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipExceptions;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.generators.ITipMailGenerator;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.ITipNotificationParticipantResolver;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.tools.ITipUtils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.session.Session;
import com.openexchange.tools.functions.ErrorAwareFunction;

/**
 * 
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class AbstractActionPerformer implements ITipActionPerformer {

    protected ITipIntegrationUtility util;
    private final MailSenderService sender;
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
     * @param processed The processed events
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

    protected void writeMail(final AJAXRequestData request, final ITipAction action, Event original, final Event update, final CalendarSession session, int owner) throws OXException {
        if (ITipAction.COUNTER.equals(action)) {
            return;
        }

        CalendarUser principal = ITipUtils.getPrincipal(session);
        final ITipMailGenerator generator = mailGenerators.create(constructOriginalForMail(action, original, update, session, owner), update, session, owner, principal);
        ErrorAwareFunction<NotificationParticipant, NotificationMail> f = null;
        switch (action) {
            case CREATE:
                if (!generator.userIsTheOrganizer()) {
                    return;
                }
                List<NotificationParticipant> recipients = generator.getRecipients();
                for (final NotificationParticipant p : recipients) {
                    final NotificationMail mail = generator.generateCreateExceptionMailFor(p);
                    if (mail != null) {
                        sender.sendMail(mail, session, principal, null);
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
                        sender.sendMail(mail, session, principal, null);
                    }
                }
                break;
            case DECLINECOUNTER:
                f = (p) -> generator.generateDeclineCounterMailFor(p);
                break;
            case SEND_APPOINTMENT:
                f = (p) -> generator.generateCreateMailFor(p);
                break;
            case REFRESH:
                f = (p) -> generator.generateRefreshMailFor(p);
                break;
            default:
                throw ITipExceptions.UNKNOWN_METHOD.create(action.toString());
        }
        /*
         * For certain operations only the originator needs to get a response
         */
        if (null != f) {
            NotificationParticipant p = getOriginator(request, session.getSession(), update);
            p.setConfiguration(ITipNotificationParticipantResolver.getDefaultConfiguration());
            final NotificationMail mail = f.apply(p);
            if (mail != null) {
                sender.sendMail(mail, session, principal, null);
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

    /**
     * Construct a new {@link Event} based on the current event and resets the currents users status in the newly created event.
     *
     * @param event The current {@link Event}
     * @param session The {@link CalendarSession}
     * @param owner The owner (aka the current user)
     * @return A new {@link Event} with owners status reset to {@link ParticipationStatus#NEEDS_ACTION}
     * @throws OXException
     */
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

    /**
     * Gets the originator of the mail.
     *
     * @param request The request
     * @param session The session
     * @param update The updated event
     * @return The originator of the mail or <code>null</code>
     * @throws OXException In case mail can't be found or mail access is unavailable
     */
    private NotificationParticipant getOriginator(AJAXRequestData request, Session session, Event update) throws OXException {
        FullnameArgument argument = MailFolderUtility.prepareMailFolderParam(getData(request, "com.openexchange.mail.conversion.fullname"));
        if (null == argument) {
            return null;
        }
        String mailId = getData(request, "com.openexchange.mail.conversion.mailid");

        /*
         * Load mail and get the FROM address.
         */
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, argument.getAccountId());
            mailAccess.connect();
            MailMessage message = mailAccess.getMessageStorage().getMessage(argument.getFullname(), mailId, false);
            if (null == message) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(mailId, argument.getFullname());
            }
            InternetAddress[] from = message.getFrom();
            if (null == from || 1 != from.length) {
                return null;
            }

            /*
             * Only generate participant if the originator participates in the event.
             */
            InternetAddress address = from[0];
            CalendarUser calendarUser = new CalendarUser();
            calendarUser.setUri(CalendarUtils.getURI(address.getAddress()));
            if (null == CalendarUtils.find(update.getAttendees(), calendarUser)) {
                return null;
            }
            return new NotificationParticipant(ITipRole.ATTENDEE, true, address.getAddress());
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    /**
     * Gets a value from the request.
     * 
     * @param request The request
     * @param key The key to get the value for
     * @return The value or <code>null</code>
     */
    private String getData(AJAXRequestData request, String key) {
        final Object data = request.getData();
        if (data != null) {
            final JSONObject body = (JSONObject) data;
            try {
                return body.getString(key);
            } catch (JSONException ignoree) {
                return null;
            }
        }
        return request.getParameters().get(key);
    }

}
