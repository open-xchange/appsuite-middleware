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

import static com.openexchange.chronos.common.CalendarUtils.extractEMailAddress;
import static com.openexchange.java.Autoboxing.i;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.mail.internet.AddressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipExceptions;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.LegacyAnalyzing;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 * {@link ReplyAnalyzer} - 'Reply' is a action only performed as answer on a 'request'
 * For details see <a href="https://tools.ietf.org/html/rfc2446#section-3.2.3">RFC 2446</a>
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class ReplyAnalyzer extends AbstractITipAnalyzer implements LegacyAnalyzing {

    private final static Logger LOGGER = LoggerFactory.getLogger(ReplyAnalyzer.class);

    /**
     * Initializes a new {@link ReplyAnalyzer}.
     * 
     * @param util The utilities
     */
    public ReplyAnalyzer(final ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.REPLY);
    }

    @Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final CalendarSession session) throws OXException {
        ITipMethod method = message.getMethod();
        if (false == method.equals(ITipMethod.REPLY)) {
            LOGGER.error("Wrong analyzer for provided iTip method {}", method);
            throw new IllegalStateException("Wrong mehtod to analyze");
        }
        return analyze(session, session.getUserId(), message, header, wrapper, locale);
    }

    private ITipAnalysis analyze(CalendarSession session, int calendarUserId, ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale) throws OXException {
        final ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);
        /*
         * Get data
         */
        CalendarObjectResource resource;
        try {
            resource = getResource(session, message, true);
        } catch (OXException e) {
            analysis.addAnnotation(new ITipAnnotation(e.getDisplayMessage(locale), locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        CalendarObjectResource originalResource = getOriginalResource(session, resource.getUid(), calendarUserId);
        analysis.setUid(resource.getUid());
        /*
         * Check if the event is known
         */
        if (null == originalResource || Collections.isNullOrEmpty(originalResource.getEvents())) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CHANGE_PARTICIPANT_STATE_IN_UNKNOWN_APPOINTMENT, locale));
            return analysis;
        }
        /*
         * Check if the calendar user is the organizer or if the current user is allowed to act as organizer
         */
        if (false == isAllowedOrganizer(session, originalResource, calendarUserId)) {
            analysis.recommendAction(ITipAction.IGNORE);
            analysis.addAnnotation(new ITipAnnotation(Messages.NO_PERMISSION, locale));
            return analysis;
        }
        /*
         * Describe event updates
         */
        for (Event event : resource.getEvents()) {
            /*
             * patch event & get or derive replies based on currently stored resource
             */
            Event patchedEvent = patchEvent(session, event, originalResource.getFirstEvent(), calendarUserId);
            ITipChange change = new ITipChange();
            if (null == patchedEvent.getRecurrenceId()) {
                Event originalEvent;
                if (Strings.isNotEmpty(patchedEvent.getRecurrenceRule())) {
                    /*
                     * Update status in series master
                     */
                    originalEvent = originalResource.getSeriesMaster();
                } else {
                    /*
                     * Update of single event
                     */
                    originalEvent = originalResource.getFirstEvent();
                }
                change.setCurrentEvent(originalEvent);
                change.setNewEvent(ensureAttendees(originalEvent, resource.getFirstEvent()));
                change.setType(Type.UPDATE);
            } else {
                /*
                 * Describe exception
                 */
                change.setException(true);
                change.setMaster(originalResource.getSeriesMaster());
                Event originalEvent = originalResource.getChangeException(patchedEvent.getRecurrenceId());
                if (null == originalEvent) {
                    /*
                     * Check that attendee didn't reply to a delete exception
                     */
                    if (isDeleteException(originalResource.getSeriesMaster(), patchedEvent)) {
                        analysis.addAnnotation(new ITipAnnotation(Messages.CHANGE_PARTICIPANT_STATE_IN_DELETED_APPOINTMENT, locale));
                        analysis.recommendAction(ITipAction.IGNORE);
                        return analysis;
                    }
                    /*
                     * Announce new change exception due participant status update
                     */
                    change.setCurrentEvent(originalResource.getSeriesMaster());
                    change.setNewEvent(ensureAttendees(originalResource.getSeriesMaster(), patchedEvent));
                    change.setType(Type.CREATE);
                } else {
                    /*
                     * Update status in known change exception
                     */
                    change.setCurrentEvent(originalEvent);
                    change.setNewEvent(ensureAttendees(originalEvent, patchedEvent));
                    change.setType(Type.UPDATE);
                }
            }
            /*
             * Describe the difference per exception
             */
            describeDiff(change, wrapper, session, message);
            analysis.addChange(change);
        }

        if (containsPartyCrasher(analysis)) {
            analysis.recommendAction(ITipAction.ACCEPT_PARTY_CRASHER);
            return analysis;
        }
        if (containsChangesForUpdate(analysis)) {
            analysis.recommendAction(ITipAction.UPDATE);
            handleUnallowedSender(header, locale, analysis, resource, originalResource);
        }
        return analysis;
    }

    /**
     * Gets a value indicating whether the current calendar user or the current session user is
     * allowed to perform changes as organizer of the event
     *
     * @param originalResource The original resources
     * @return <code>true</code> if acting as organizer is allowed, <code>false</code> otherwise
     */
    private boolean isAllowedOrganizer(CalendarSession session, CalendarObjectResource originalResource, int calendarUserId) throws OXException {
        Event firstEvent = originalResource.getFirstEvent();
        if (CalendarUtils.isOrganizer(firstEvent, calendarUserId)) {
            return true;
        }
        /*
         * Check if the current user is allowed to perform the change
         */
        try {
            Context context = Services.getService(ContextService.class, true).getContext(session.getContextId());
            Integer folderId = Integer.valueOf(firstEvent.getFolderId());
            FolderObject folder = new OXFolderAccess(context).getFolderObject(i(folderId));
            UserPermissionBits permissionBits = ServerSessionAdapter.valueOf(session.getSession()).getUserPermissionBits();
            EffectivePermission permission = folder.getEffectiveUserPermission(session.getUserId(), permissionBits);
            if (false == permission.isFolderVisible() //
                || false == (permission.canWriteOwnObjects() && session.getUserId() == firstEvent.getCreatedBy().getEntity()) // Can modify own?
                || false == permission.canWriteAllObjects()) {
                return false;
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("Unable to get correct folder", e);
            return false;
        }
        return true;
    }

    /** Fields to copy when a attendee is replying */
    private final static AttendeeField[] TO_COPY = new AttendeeField[] { AttendeeField.COMMENT, AttendeeField.EXTENDED_PARAMETERS, AttendeeField.PARTSTAT, AttendeeField.SENT_BY };

    /**
     * Updates the attendee provided by the REPLY in the original event.
     * Ignores all other changed fields.
     * 
     * @param original The original event
     * @param update The event containing the attendee
     * @return The event with correct set attendees
     * @throws OXException In case event can't be copied or reply is not RFC conform
     */
    private Event ensureAttendees(Event original, Event update) throws OXException {
        Event event = EventMapper.getInstance().copy(original, new Event(), (EventField[]) null);
        event = EventMapper.getInstance().copy(update, event, EventField.RECURRENCE_ID);
        event.removeId();
        event.removeSeriesId();

        Attendee reply = getReplyingAttendee(update);

        List<Attendee> attendees = new LinkedList<>();
        // XXX [MW-852] Resolve possible aliases to avoid false party-crashers
        boolean partyCrasher = true;
        for (Attendee attendee : original.getAttendees()) {
            if (extractEMailAddress(reply.getUri()).equals(extractEMailAddress(attendee.getUri()))) {
                attendee = AttendeeMapper.getInstance().copy(attendee, null, (AttendeeField[]) null);
                AttendeeMapper.getInstance().copy(reply, attendee, TO_COPY);
                partyCrasher = false;
            }
            attendees.add(attendee);
        }

        if (partyCrasher) {
            // Add 'as-is'
            attendees.add(reply);
        }

        event.setAttendees(attendees);
        return event;
    }

    private boolean containsChangesForUpdate(ITipAnalysis analysis) throws OXException {
        if (analysis.getChanges().size() == 0) {
            return false;
        }

        for (ITipChange change : analysis.getChanges()) {
            if (change.getDiff() == null) {
                continue;
            }

            if (change.getDiff().getUpdatedFields() == null) {
                continue;
            }

            if (false == change.getDiff().getAttendeeUpdates().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPartyCrasher(ITipAnalysis analysis) throws OXException {
        for (ITipChange change : analysis.getChanges()) {
            ITipEventUpdate eventUpdate = change.getDiff();
            if (null != eventUpdate && null != eventUpdate.getAttendeeUpdates()//
                && null != eventUpdate.getAttendeeUpdates().getAddedItems()//
                && false == eventUpdate.getAttendeeUpdates().getAddedItems().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the attendee that replied
     *
     * @param update The event containing the replying attendee
     * @return The attendee replying
     * @throws OXException
     */
    private Attendee getReplyingAttendee(Event update) throws OXException {
        if (null == update.getAttendees() || update.getAttendees().size() != 1) {
            // Not RFC conform
            throw ITipExceptions.NOT_CONFORM.create();
        }
        return update.getAttendees().get(0);
    }

    /**
     * Checks if an malicious sender has send the mail and inform the user
     *
     * @param header To get the FROM address from
     * @param locale The locale of the user
     * @param analysis The analysis
     * @param resource The resource to process
     * @param originalResource The original resources
     * @throws OXException In case of error
     */
    private void handleUnallowedSender(Map<String, String> header, Locale locale, final ITipAnalysis analysis, CalendarObjectResource resource, CalendarObjectResource originalResource) throws OXException {
        /*
         * Check if sender is allowed to perform the update.
         * Notify user with warning if not
         */
        QuotedInternetAddress fromAddress = null == header ? null : getAddress(header.get("from"));
        if (isAllowedSender(originalResource, fromAddress)) {
            return;
        }
        /*
         * No IGNORE action implemented, UI doesn't hide the button. Don't send atm
         * analysis.recommendAction(ITipAction.IGNORE);
         */
        Attendee replyingAttendee = getReplyingAttendee(resource.getFirstEvent());
        // @formatter:off
        analysis.addAnnotation(new ITipAnnotation(
            new Sentence(Messages.MALICIOUS_SENDER_WARNING)
            .add(Strings.isEmpty(replyingAttendee.getEMail()) ? 
                replyingAttendee.getUri() : 
                replyingAttendee.getEMail())
            .add(null == fromAddress ? null : fromAddress.getAddress())
            .getMessage(locale),
            locale));
        // @formatter:on
    }

    /**
     * Gets a value indicating whether the sender of the mail address is allowed
     * to perform the change for the attendee replying
     *
     * @param original The original event
     * @param analysis The current analysis
     * @param from The sender address extracted from the <code>FROM</code> header
     * @return <code>true</code> if the sender is allowed to perform the change for the attendee,
     *         <code>false</code> otherwise, i.e. if FROM not set at all
     * @throws OXException In case of error
     */
    private boolean isAllowedSender(CalendarObjectResource originalResource, QuotedInternetAddress from) {
        if (null == from) {
            return false;
        }
        CalendarUser calendarUser = new CalendarUser();
        calendarUser.setEMail(from.getAddress());
        calendarUser.setUri(CalendarUtils.getURI(from.getAddress()));
        /*
         * Lookup replying attendee in all known events
         */
        for (Event original : originalResource.getEvents()) {
            if (null != CalendarUtils.find(original.getAttendees(), calendarUser)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the from address as {@link QuotedInternetAddress}
     *
     * @param fromAddress The address to convert
     * @return The address or <code>null</code>
     */
    private QuotedInternetAddress getAddress(String fromAddress) {
        try {
            QuotedInternetAddress from = new QuotedInternetAddress(fromAddress);
            return from;
        } catch (AddressException e) {
            LOGGER.debug("unable to parse mail", e);
        }
        return null;
    }
}
