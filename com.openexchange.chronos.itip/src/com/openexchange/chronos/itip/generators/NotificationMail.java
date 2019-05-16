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

package com.openexchange.chronos.itip.generators;

import static com.openexchange.java.Autoboxing.B;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.mail.config.MailProperties;

/**
 * {@link NotificationMail}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NotificationMail {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NotificationMail.class);

    private ITipMessage itipMessage;

    private String templateName;
    private String text;
    private String html;
    private String subject;

    private Event original;
    private Event event;
    private ITipEventUpdate diff;

    private NotificationParticipant sender;
    private NotificationParticipant recipient;
    private NotificationParticipant organizer;
    private NotificationParticipant principal;
    private NotificationParticipant sharedCalendarOwner;

    private List<NotificationParticipant> participants;
    private List<NotificationParticipant> resources;

    private NotificationParticipant actor;

    private final List<Attachment> attachments = new ArrayList<>();

    private Type stateType;

    private boolean attachmentUpdate;

    private boolean sortedParticipants;

    private final Map<String, String> additionalHeaders;

    /**
     * Initializes a new {@link NotificationMail}.
     */
    public NotificationMail() {
        super();
        additionalHeaders = new LinkedHashMap<>(4);
    }

    /*
     * ============= SETTER and GETTER =============
     */

    /**
     * Sets given additional header
     * <p>
     * Note: Header name is required to start with <code>"X-"</code> prefix.
     *
     * @param name The header name
     * @param value The header value
     * @throws IllegalArgumentException If either name/value is <code>null</code> or name does not start with <code>"X-"</code> prefix
     */
    public void setAdditionalHeader(String name, String value) {
        if (null == name) {
            throw new IllegalArgumentException("name is null");
        }
        if (null == value) {
            throw new IllegalArgumentException("value is null");
        }
        if (!name.startsWith("X-")) {
            throw new IllegalArgumentException("name does not start with \"X-\" prefix");
        }

        additionalHeaders.put(name, value);
    }

    /**
     * Gets the additional headers
     *
     * @return The additional headers as an unmodifiable map
     */
    public Map<String, String> getAdditionalHeaders() {
        return Collections.unmodifiableMap(additionalHeaders);
    }

    public ITipMessage getMessage() {
        return itipMessage;
    }

    public void setMessage(ITipMessage itipMessage) {
        this.itipMessage = itipMessage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Event getOriginal() {
        return original;
    }

    public void setOriginal(Event original) {
        this.original = original;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event updated) {
        this.event = updated;
    }

    public ITipEventUpdate getDiff() throws OXException {
        if (diff == null && original != null && event != null) {
            diff = new ITipEventUpdate(original, event, true, ITipNotificationMailGenerator.DEFAULT_SKIP);
        }
        return diff;
    }

    public NotificationParticipant getRecipient() {
        return recipient;
    }

    public void setRecipient(NotificationParticipant recipient) {
        this.recipient = recipient;
    }

    public void setSender(NotificationParticipant sender) {
        this.sender = sender;
    }

    public NotificationParticipant getSender() {
        return sender;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public NotificationParticipant getOrganizer() {
        return organizer;
    }

    public void setOrganizer(NotificationParticipant organizer) {
        this.organizer = organizer;
    }

    public NotificationParticipant getPrincipal() {
        if (principal == null) {
            return principal = organizer;
        }
        return principal;
    }

    public void setPrincipal(NotificationParticipant principal) {
        this.principal = principal;
    }

    public NotificationParticipant getOnBehalfOf() throws OXException {
        if (isAboutActorsStateChangeOnly()) {
            return actor;
        }

        if (sharedCalendarOwner != null) {
            return sharedCalendarOwner;
        }

        return sender;
    }

    public NotificationParticipant getSharedCalendarOwner() {
        return sharedCalendarOwner;
    }

    public void setSharedCalendarOwner(NotificationParticipant sharedCalendarOwner) {
        this.sharedCalendarOwner = sharedCalendarOwner;
    }

    public void setParticipants(List<NotificationParticipant> recipients) {
        sortedParticipants = false;
        this.participants = recipients;
    }

    public List<NotificationParticipant> getParticipants() {
        if (!sortedParticipants) {
            if (participants == null) {
                return null;
            }
            Collections.sort(participants, new Comparator<NotificationParticipant>() {

                @Override
                public int compare(NotificationParticipant p1, NotificationParticipant p2) {
                    return p1.getDisplayName().compareTo(p2.getDisplayName());
                }

            });
        }
        return participants;
    }

    public void setResources(List<NotificationParticipant> resources) {
        this.resources = resources;
    }

    public List<NotificationParticipant> getResources() {
        return resources;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setAttachmentUpdate(boolean attachmentUpdate) {
        this.attachmentUpdate = attachmentUpdate;
    }

    public boolean isAttachmentUpdate() {
        return attachmentUpdate;
    }

    public void setActor(NotificationParticipant actor) {
        this.actor = actor;
    }

    public NotificationParticipant getActor() {
        return actor;
    }

    public Type getStateType() {
        return stateType;
    }

    public void setStateType(Type stateType) {
        this.stateType = stateType;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    /*
     * ============= FUNCTIONS =============
     */

    public boolean actionIsDoneOnBehalfOfAnother() throws OXException {
        if (actor == null || actor.hasRole(ITipRole.PRINCIPAL)) {
            return false;
        }
        return !actor.equals(getOnBehalfOf());
    }

    public boolean actionIsDoneOnMyBehalf() throws OXException {
        if (isAboutActorsStateChangeOnly()) {
            return false;
        }
        if (actor != null && actor.hasRole(ITipRole.PRINCIPAL)) {
            return false;
        }

        return recipient.equals(principal) || recipient.equals(sharedCalendarOwner);
    }

    public boolean isAboutStateChangesOnly() throws OXException {
        if (getDiff() == null) {
            return false;
        }

        if (isAttachmentUpdate()) {
            return false;
        }

        if (isStateChangeExceptionCreate()) {
            return true;
        }
        return diff.isAboutStateChangesOnly(FIELDS_TO_REPORT);
    }

    public boolean isAboutStateChanges() throws OXException {
        if (getDiff() == null) {
            return false;
        }

        return diff.isAboutStateChanges();
    }

    public boolean isAboutActorsStateChangeOnly() throws OXException {
        if (!isAboutStateChangesOnly()) {
            return false;
        }
        return diff.isAboutCertainParticipantsStateChangeOnly(Integer.toString(actor.getIdentifier()));
    }

    public boolean someoneElseChangedPrincipalsState() {
        if (actor.getIdentifier() == getPrincipal().getIdentifier()) {
            return false;
        }
        return diff.isAboutCertainParticipantsStateChangeOnly(Integer.toString(getPrincipal().getIdentifier()));
    }

    public boolean isStateChangeExceptionCreate() {
        boolean candidate = diff.containsExactTheseChanges(new EventField[] { EventField.CHANGE_EXCEPTION_DATES, EventField.RECURRENCE_ID, EventField.ATTENDEES });
        if (candidate) {
            return diff.isAboutStateChanges();
        }
        return false;
    }

    public boolean shouldBeSent(CalendarSession session) throws OXException {
        if (null != event) {
            if (endsInPast(event)) {
                // No mail for events in the past
                if (null != original && endsInPast(original) || stateType.equals(Type.NEW) || stateType.equals(Type.MODIFIED)) {
                    return false;
                }
            }
            if (recipient.getConfiguration().forceCancelMails() && isCancelMail()) {
                return true;
            }
            if (stateType.equals(Type.DELETED)) {
                return false;
            }
        }
        if (!recipientIsOrganizerAndHasNoAccess()) {
            return false;
        }
        if (false == recipient.isExternal() && false == session.get(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.class, Boolean.TRUE).booleanValue()) {
            // Don't send notification mails for internal users if the flag is set. See bug 62098.
            return false;
        }

        // Does the appointment have any change to notify about?
        if (!anInterestingFieldChanged()) {
            return false;
        }

        if (recipient.getConfiguration().sendITIP() && itipMessage != null) {
            return true;
        }

        boolean isInterestedInChanges = recipient.getConfiguration().interestedInChanges();
        boolean isInterestedInStateChanges = recipient.getConfiguration().interestedInStateChanges();

        // Not interested in anything
        if (!isInterestedInChanges && !isInterestedInStateChanges) {
            return false;
        }

        // Interested in state changes, but not in other changes
        if (isInterestedInStateChanges && !isInterestedInChanges) {
            boolean aboutStateChanges = isAboutStateChanges();
            LOG.debug("NotificationMail.shouldBeSend (1), User: {}, {}, {}, {}\nDiffering Fields: {}", id(), B(isInterestedInStateChanges), B(isInterestedInChanges), B(aboutStateChanges), diffs());
            return aboutStateChanges;
        }

        // Interested in other changes, but not in state changes
        boolean aboutStateChangesOnly = isAboutStateChangesOnly();
        if (!isInterestedInStateChanges && isInterestedInChanges) {
            LOG.debug("NotificationMail.shouldBeSend (2), User: {}, {}, {}, {}\nDiffering Fields: {}, {}", id(), B(isInterestedInStateChanges), B(isInterestedInChanges), B(aboutStateChangesOnly), diffs(), getUserDiff());
            return !aboutStateChangesOnly;
        }
        LOG.debug("NotificationMail.shouldBeSend (3), User: {}, {}, {}, {}\nDiffering Fields: {}, {}", id(), B(isInterestedInStateChanges), B(isInterestedInChanges), B(aboutStateChangesOnly), diffs(), getUserDiff());
        return true;
    }

    /*
     * ============= HELPERS FOR LOGGING =============
     */

    private String id() {
        if (null != recipient && null != recipient.getUser()) {
            return String.valueOf(recipient.getUser().getId());
        }
        return null;
    }

    private String getUserDiff() {
        // Try to get diff
        ITipEventUpdate eventUpdate = null;
        try {
            eventUpdate = getDiff();
        } catch (OXException e) {
            LOG.debug("Unable to get EventUpdate", e);
        }
        if (null == eventUpdate) {
            return null;
        }

        // Explain diff
        StringBuilder sb = new StringBuilder(" Changed Users: ");
        if (eventUpdate.getAttendeeUpdates() != null) {
            if (eventUpdate.getAttendeeUpdates().getAddedItems() != null && eventUpdate.getAttendeeUpdates().getAddedItems().size() > 0) {
                sb.append("Added: ");
                for (Attendee added : eventUpdate.getAttendeeUpdates().getAddedItems()) {
                    sb.append(added.getEMail() + "(" + added.getEntity() + "), ");
                }
            }
            if (eventUpdate.getAttendeeUpdates().getRemovedItems() != null && eventUpdate.getAttendeeUpdates().getRemovedItems().size() > 0) {
                sb.append("Removed: ");
                for (Attendee removed : eventUpdate.getAttendeeUpdates().getRemovedItems()) {
                    sb.append(removed.getEMail() + "(" + removed.getEntity() + "), ");
                }
            }
            if (eventUpdate.getAttendeeUpdates().getUpdatedItems() != null && eventUpdate.getAttendeeUpdates().getUpdatedItems().size() > 0) {
                sb.append("Updated: ");
                for (ItemUpdate<Attendee, AttendeeField> updated : eventUpdate.getAttendeeUpdates().getUpdatedItems()) {
                    sb.append(updated.getOriginal().getEMail() + "(" + updated.getOriginal().getEntity() + ") | ");
                    sb.append(updated.getUpdatedFields().toString()).append(", ");
                }
            }
        }
        return sb.toString();
    }

    private String diffs() {
        ITipEventUpdate eventUpdate = null;
        try {
            eventUpdate = getDiff();
        } catch (Exception e) {
            LOG.debug("Unable to get EventUpdate", e);
        }

        if (null != eventUpdate && null != eventUpdate.getUpdatedFields()) {
            return eventUpdate.getUpdatedFields().toString();
        }
        return null;
    }

    /*
     * ============= HELPERS =============
     */

    private boolean endsInPast(final Event event) throws OXException {
        final Date now = new Date();
        Date endDate = new Date(event.getEndDate().getTimestamp());

        // In case of series master the date of the last occurrence has to be validated
        if (CalendarUtils.isSeriesMaster(event)) {
            try {
                RecurrenceRule eventRule = new RecurrenceRule(event.getRecurrenceRule());
                Date eventEnd = null;

                RecurrenceService rService = Services.getService(RecurrenceService.class);
                if (eventRule.getUntil() != null) {
                    eventEnd = new Date(eventRule.getUntil().getTimestamp());
                } else if (eventRule.getCount() != null) {
                    Iterator<Event> instances = rService.calculateInstances(event, null, null, null);
                    Event last = null;
                    while (instances.hasNext()) {
                        last = instances.next();
                    }
                    if (null != last) {
                        eventEnd = new Date(last.getEndDate().getTimestamp());
                    }
                } else {
                    // Recurrence rule has no 'limit' defined. 
                    return false;
                }
                if (eventEnd != null) {
                    return eventEnd.before(now);
                }
            } catch (InvalidRecurrenceRuleException e) {
                LOG.debug("Invalid recurrence rule. Fallback to notify");
            }
        }

        return endDate.before(now);
    }

    private boolean isCancelMail() {
        return itipMessage != null && itipMessage.getMethod() == ITipMethod.CANCEL;
    }

    private static final EventField[] FIELDS_TO_REPORT = new EventField[] { EventField.LOCATION, EventField.SUMMARY, EventField.START_DATE, EventField.END_DATE, EventField.DESCRIPTION, EventField.RECURRENCE_RULE, EventField.ATTENDEES, EventField.CHANGE_EXCEPTION_DATES, EventField.ORGANIZER };

    private boolean anInterestingFieldChanged() throws OXException {
        if (getDiff() == null) {
            return true;
        }
        if (isAttachmentUpdate()) {
            return true;
        }
        if (isChangeUserSpecific()) {
            return false;
        }

        return getDiff().containsAnyChangeOf(FIELDS_TO_REPORT);
    }

    private boolean isChangeUserSpecific() {
        return diff.containsExactTheseChanges(new EventField[] { EventField.RECURRENCE_ID });
    }

    private boolean recipientIsOrganizerAndHasNoAccess() {
        if (recipient.isExternal()) {
            return true;
        }
        if (!recipient.hasRole(ITipRole.ORGANIZER)) {
            return true;
        }
        if (recipient.getIdentifier() == recipient.getContext().getMailadmin()) {
            if (MailProperties.getInstance().isAdminMailLoginEnabled() == false) {
                // Context administrator is recipient but does not have permission to access mail
                return false;
            }
        }
        return true;
    }

}
