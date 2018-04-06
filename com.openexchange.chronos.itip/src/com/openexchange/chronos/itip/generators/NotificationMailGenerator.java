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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.changes.PassthroughWrapper;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.NotificationConfig;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.groupware.notify.State;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Strings;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;

/**
 * {@link NotificationMailGenerator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NotificationMailGenerator implements ITipMailGenerator {

    private final static Logger LOGGER = LoggerFactory.getLogger(NotificationMailGenerator.class);

    private NotificationParticipant organizer;

    private final List<NotificationParticipant> recipients;

    private final ITipIntegrationUtility util;

    private MailGeneratorState state;

    private final Context ctx;

    private final AttachmentMemory attachmentMemory;

    private final ServiceLookup services;

    private final List<NotificationParticipant> resources;

    private NotificationParticipant principal;

    protected NotificationParticipant actor;

    protected final Event updated;

    protected final Event original;

    protected final CalendarSession session;

    protected ITipEventUpdate diff;

    protected final List<NotificationParticipant> participants;

    protected NotificationParticipant onBehalfOf;

    protected CalendarUtilities calendarUtilities;

    public static final EventField[] DEFAULT_SKIP = new EventField[] { EventField.ID, EventField.FOLDER_ID, EventField.CREATED_BY, EventField.MODIFIED_BY, EventField.CREATED, EventField.LAST_MODIFIED, EventField.ALARMS, EventField.SEQUENCE,
        EventField.TRANSP, EventField.TIMESTAMP, EventField.FLAGS };

    public NotificationMailGenerator(final ServiceLookup services, final AttachmentMemory attachmentMemory, final NotificationParticipantResolver resolver, final ITipIntegrationUtility util, final Event original, final Event updated, User user,
        final User onBehalfOf, final Context ctx, final Session session, CalendarUser principal) throws OXException {
        this.util = util;
        this.ctx = ctx;
        this.services = services;
        this.attachmentMemory = attachmentMemory;
        this.calendarUtilities = Services.getService(CalendarUtilities.class);

        this.recipients = resolver.resolveAllRecipients(original, updated, user, onBehalfOf, ctx, session, principal);
        this.participants = resolver.getAllParticipants(this.recipients, updated);
        this.resources = resolver.getResources(updated);

        for (final NotificationParticipant participant : recipients) {
            if (participant.hasRole(ITipRole.ORGANIZER)) {
                this.organizer = participant;
            }
            if (participant.hasRole(ITipRole.PRINCIPAL)) {
                this.principal = participant;
            }
            if (participant.hasRole(ITipRole.ON_BEHALF_OF)) {
                this.onBehalfOf = participant;
            }
            if (participant.getIdentifier() == user.getId()) {
                this.actor = participant;
            }
        }
        if (this.actor == null) {
            throw new IllegalStateException("Resolver didn't resolve the acting user");
        }

        if (this.organizer == null) {
            organizer = actor; // Is that so? As a fallback this could be good enough
        }

        CalendarService calendarService = Services.getService(CalendarService.class);
        this.session = calendarService.init(session);
        this.original = original;
        this.updated = updated;

        if (original != null) {
            this.diff = new ITipEventUpdate(original, updated, true, DEFAULT_SKIP);
        }

        if (actor.hasRole(ITipRole.ORGANIZER) || actor.hasRole(ITipRole.PRINCIPAL)) {
            state = new OrganizerState();
        } else {
            if (organizer.isExternal()) {
                state = new AttendeeWithExternalOrganizerState();
            } else {
                state = new AttendeeWithInternalOrganizerState();
            }
        }

        if (diff != null && diff.getUpdatedFields().isEmpty() && !attachmentMemory.hasAttachmentChanged(updated.getId(), session.getContextId())) {
            state = new DoNothingState();
        }
    }

    @Override
    public void noActor() {
        this.actor = this.actor.clone();
        this.actor.setVirtual(true);
    }

    @Override
    public boolean userIsTheOrganizer() {
        return actor.hasRole(ITipRole.ORGANIZER);
    }

    @Override
    public NotificationMail generateCreateMailFor(final NotificationParticipant participant) throws OXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }

        return state.generateCreateMailFor(participant);
    }

    @Override
    public NotificationMail generateUpdateMailFor(final NotificationParticipant participant) throws OXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }

        return state.generateUpdateMailFor(participant);
    }

    @Override
    public NotificationMail generateCreateExceptionMailFor(final NotificationParticipant participant) throws OXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }

        return state.generateCreateExceptionMailFor(participant);
    }

    @Override
    public NotificationMail generateDeleteMailFor(final NotificationParticipant participant) throws OXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }
        return state.generateDeleteMailFor(participant);
    }

    @Override
    public NotificationMail generateRefreshMailFor(final NotificationParticipant participant) throws OXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }
        return state.generateRefreshMailFor(participant);
    }

    @Override
    public NotificationMail generateDeclineCounterMailFor(final NotificationParticipant participant) throws OXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }
        return state.generateDeclineCounterMailFor(participant);
    }

    protected NotificationMail cancel(final NotificationParticipant recipient) {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(determinateSender(recipient.isExternal()));
        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setEvent(updated);

        mail.setMessage(message);

        return mail;
    }

    private LabelHelper getLabelHelper(final NotificationMail mail, final TypeWrapper wrapper, final NotificationParticipant participant) throws OXException {
        return new LabelHelper(dateHelperFor(participant), participant.getTimeZone(), mail, participant.getLocale(), ctx, wrapper, services);
    }

    // IMIP Messages

    protected NotificationMail noITIP(final NotificationParticipant recipient, final State.Type type) {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(determinateSender(recipient.isExternal()));
        mail.setStateType(type);
        initMail(mail);

        return mail;
    }

    protected NotificationMail counterNoITIP(final NotificationParticipant recipient, final State.Type type) {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(type);
        initMail(mail);

        return mail;
    }

    protected NotificationMail request(final NotificationParticipant recipient, final Event override, final State.Type type) throws OXException {
        final Event eventToReport = (override != null) ? override : this.updated;
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(determinateSender(recipient.isExternal()));
        mail.setStateType(type);
        initMail(mail);
        initAttachments(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REQUEST);
        message.setEvent(eventToReport);

        if (CalendarUtils.isSeriesMaster(eventToReport)) {
            final List<Event> exceptions = util.getExceptions(eventToReport, session);
            if (exceptions != null) {
                for (final Event exception : exceptions) {
                    message.addException(exception);
                }
            }
        }
        mail.setMessage(message);
        mail.setOriginal(original);
        mail.setEvent(eventToReport);

        return mail;
    }

    /**
     * Determinate the {@link NotificationParticipant} to use as the sender of the mail.
     * Considers {@link NotificationProperty#FROM_SOURCE} for the correct mail address to use.
     * 
     * @param external If the receiver of the mail is internal or external
     * @return The sender with correct mail set
     */
    private NotificationParticipant determinateSender(boolean external) {
        NotificationParticipant n;
        if (external) {
            n = organizer.clone();
            try {
                String fromAddr;
                final String senderSource = NotificationConfig.getProperty(NotificationProperty.FROM_SOURCE, "primaryMail");
                Context context = Services.getService(ContextService.class, true).getContext(session.getContextId());
                if (senderSource.equals("defaultSenderAddress")) {
                    try {
                        fromAddr = UserSettingMailStorage.getInstance().loadUserSettingMail(n.getIdentifier(), context).getSendAddr();
                    } catch (final OXException e) {
                        LOGGER.debug("", e);
                        fromAddr = UserStorage.getInstance().getUser(n.getIdentifier(), context).getMail();
                    }
                } else {
                    fromAddr = UserStorage.getInstance().getUser(n.getIdentifier(), context).getMail();
                }
                if (Strings.isNotEmpty(fromAddr)) {
                    n.setEmail(fromAddr);
                }
            } catch (OXException e) {
                LOGGER.debug("Couldn't change sender mail address to configuared defaults.", e);
            }

        } else {
            n = actor;
        }
        return n;
    }

    protected NotificationMail add(final NotificationParticipant recipient) {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(determinateSender(recipient.isExternal()));
        mail.setStateType(State.Type.NEW);
        initMail(mail);
        initAttachments(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REQUEST);
        message.addException(updated);
        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail counter(final NotificationParticipant recipient) {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.MODIFIED);
        initMail(mail);
        initAttachments(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.COUNTER);
        if (CalendarUtils.isSeriesException(updated)) {
            message.addException(updated);
        } else {
            message.setEvent(updated);
        }

        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail reply(final NotificationParticipant recipient, final ParticipationStatus confirmStatus) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(onBehalfOf != null ? onBehalfOf : actor);
        mail.setStateType(getStateTypeForStatus(confirmStatus));
        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REPLY);

        final Event reply = new Event();
        for (EventField field : EventField.values()) {
            if (field == EventField.ATTENDEES) {
                continue;
            }
            if (updated.isSet(field)) {
                EventMapper.getInstance().copy(updated, reply, field);
            }
        }
        Attendee attendee = new Attendee();
        NotificationParticipant notificationParticipant = onBehalfOf != null ? onBehalfOf : actor;
        attendee.setEntity(notificationParticipant.getIdentifier());
        attendee.setPartStat(confirmStatus);
        attendee.setUri(CalendarUtils.getURI(mail.getSender().getEmail()));
        attendee.setCuType(CalendarUserType.INDIVIDUAL);
        attendee.setCn(notificationParticipant.getDisplayName());
        attendee.setComment(notificationParticipant.getComment());
        reply.setAttendees(Arrays.asList(attendee));
        if (CalendarUtils.isSeriesException(reply)) {
            message.addException(reply);
        } else {
            message.setEvent(reply);
        }

        mail.setMessage(message);

        return mail;
    }

    protected State.Type getStateTypeForStatus(final ParticipationStatus confirmStatus) {
        if (confirmStatus == null) {
            return State.Type.MODIFIED;
        }
        if (confirmStatus.equals(ParticipationStatus.ACCEPTED)) {
            return State.Type.ACCEPTED;
        } else if (confirmStatus.equals(ParticipationStatus.DECLINED)) {
            return State.Type.DECLINED;
        } else if (confirmStatus.equals(ParticipationStatus.TENTATIVE)) {
            return State.Type.TENTATIVELY_ACCEPTED;
        } else if (confirmStatus.equals(ParticipationStatus.NEEDS_ACTION)) {
            return State.Type.NONE_ACCEPTED;
        } else {
            return State.Type.MODIFIED;
        }
    }

    protected NotificationMail refresh(final NotificationParticipant recipient) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.REFRESH);
        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REFRESH);

        final Event address = calendarUtilities.copyEvent(updated, (EventField[]) null);
        address.removeAttendees();
        address.removeDescription();
        address.removeLocation();

        if (CalendarUtils.isSeriesException(address)) {
            message.addException(address);
        } else {
            message.setEvent(address);
        }

        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail declinecounter(final NotificationParticipant recipient) {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.DECLINE_COUNTER);

        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);

        if (CalendarUtils.isSeriesException(updated)) {
            message.addException(updated);
        } else {
            message.setEvent(updated);
        }

        mail.setMessage(message);

        return mail;
    }

    private void initMail(final NotificationMail mail) {
        mail.setOrganizer(organizer);
        mail.setActor(actor);
        mail.setPrincipal(principal);
        mail.setSharedCalendarOwner(onBehalfOf);
        mail.setEvent(updated);
        mail.setOriginal(original);
        mail.setParticipants(participants);
        mail.setResources(resources);

    }

    private void initAttachments(final NotificationMail mail) {
        if (services == null) {
            return;
        }
        if (!attachmentMemory.hasAttachmentChanged(updated.getId(), ctx.getContextId())) {
            return;
        }
        if (false == mail.getEvent().containsAttachments() || null == mail.getEvent().getAttachments() || mail.getEvent().getAttachments().isEmpty()) {
            return;
        }
        mail.setAttachmentUpdate(true);
        List<Attachment> atts = mail.getEvent().getAttachments();
        for (Attachment attachment : atts) {
            mail.addAttachment(attachment);
        }
    }

    // Templating and mail text

    protected NotificationMail create(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.create");
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_NEW_APPOINTMENT).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
        if (mail.getRecipient().isResource()) {
            mail.setAdditionalHeader("X-Open-Xchange-Recipient-Type", "Resource");
        }
        render(mail);
        return mail;
    }

    protected NotificationMail update(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.update");
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_CHANGED_APPOINTMENT).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
        if (mail.getRecipient().isResource()) {
            mail.setAdditionalHeader("X-Open-Xchange-Recipient-Type", "Resource");
        }
        render(mail);
        return mail;
    }

    protected NotificationMail delete(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.delete");
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_CANCELLED_APPOINTMENT).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail createException(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.createexception");
        recalculateOccurrence(mail);
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_CHANGED_APPOINTMENT).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail askForUpdate(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.refresh");
        recalculateOccurrence(mail);
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_REFRESH).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail declinecounter(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.declinecounter");
        recalculateOccurrence(mail);
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_DECLINECOUNTER).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    private void recalculateOccurrence(final NotificationMail mail) throws OXException {
        final Event originalOcurrence = mail.getOriginal();
        final Event newEvent = mail.getEvent();
        if (!CalendarUtils.isSeriesException(newEvent)) {
            return;
        }
        RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);

        RecurrenceIterator<Event> recurrenceIterator = recurrenceService.iterateEventOccurrences(originalOcurrence, null, null);
        while (recurrenceIterator.hasNext()) {
            Event next = recurrenceIterator.next();
            if (next.getRecurrenceId().equals(newEvent.getRecurrenceId())) {
                originalOcurrence.setStartDate(next.getStartDate());
                originalOcurrence.setEndDate(next.getEndDate());
                return;
            }
        }

        mail.setOriginal(originalOcurrence);
    }

    protected NotificationMail counter(final NotificationMail mail, final ITipRole role) throws OXException {
        switch (role) {
            case ATTENDEE:
                mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_COUNTER_APPOINTMENT).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
                mail.setTemplateName("notify.appointment.counter.participant");
                break;
            case ORGANIZER:
                mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_COUNTER_APPOINTMENT).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
                mail.setTemplateName("notify.appointment.counter.organizer");
                break;
            default:
                //Fall through
                LOGGER.debug("Can not counter for role {}.", role.toString());
                break;
        }
        render(mail);
        return mail;
    }

    protected NotificationMail stateChanged(final NotificationMail mail, final ParticipationStatus status) throws OXException {
        final ParticipationStatus stat = null == status ? ParticipationStatus.NEEDS_ACTION : status;
        if (stat.equals(ParticipationStatus.ACCEPTED)) {
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).addStatus(status).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.accept");
        } else if (stat.equals(ParticipationStatus.DECLINED)) {
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).addStatus(status).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.decline");
        } else if (stat.equals(ParticipationStatus.TENTATIVE)) {
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).addStatus(status).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.tentative");
        } else if (stat.equals(ParticipationStatus.NEEDS_ACTION)) {
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_NONE).add(actor.getDisplayName()).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.none");
        } else {
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_NONE).add(actor.getDisplayName()).add(mail.getEvent().getSummary()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.none");
        }

        render(mail);
        return mail;
    }

    private String prefix(NotificationMail mail) {
        if (mail.getRecipient().isResource()) {
            return "[" + StringHelper.valueOf(mail.getRecipient().getLocale()).getString(Messages.LABEL_RESOURCES) + "] ";
        }

        return "";
    }

    private void render(final NotificationMail mail) throws OXException {
        if (services == null) {
            return;
        }
        final Event originalForRendering = mail.getOriginal();
        final Event updateForRendering = mail.getEvent();

        fastForwardToConcreteOriginalOccurrence(originalForRendering, updateForRendering);

        final NotificationParticipant participant = mail.getRecipient();

        final TemplateService templates = services.getService(TemplateService.class);
        if (templates == null) {
            return;
        }
        final OXTemplate textTemplate = templates.loadTemplate(mail.getTemplateName() + ".txt.tmpl");
        final OXTemplate htmlTemplate = templates.loadTemplate(mail.getTemplateName() + ".html.tmpl");

        final Map<String, Object> env = new HashMap<String, Object>();
        PassthroughWrapper wrapper = new PassthroughWrapper();

        env.put("mail", mail);
        env.put("templating", templates.createHelper(env, null, false));
        env.put("formatters", dateHelperFor(mail.getRecipient()));
        env.put("labels", getLabelHelper(mail, wrapper, participant));
        if (originalForRendering != null) {
            env.put("changes", new ChangeHelper(ctx, mail.getRecipient(), originalForRendering, updateForRendering, mail.getDiff(), participant.getLocale(), participant.getTimeZone(), wrapper, attachmentMemory, services).getChanges());
        } else {
            env.put("changes", new ArrayList<String>());
        }
        env.put("participantHelper", new ParticipantHelper(participant.getLocale()));

        AllocatingStringWriter writer = new AllocatingStringWriter();
        textTemplate.process(env, writer);
        mail.setText(writer.toString());

        wrapper = new HTMLWrapper();
        env.put("labels", getLabelHelper(mail, wrapper, participant));
        if (originalForRendering != null) {
            env.put("changes", new ChangeHelper(ctx, mail.getRecipient(), originalForRendering, updateForRendering, mail.getDiff(), participant.getLocale(), participant.getTimeZone(), wrapper, attachmentMemory, services).getChanges());
        }
        writer = new AllocatingStringWriter();
        htmlTemplate.process(env, writer);
        mail.setHtml(writer.toString());
    }

    private void fastForwardToConcreteOriginalOccurrence(final Event originalForRendering, final Event updateForRendering) throws OXException {
        if (originalForRendering != null && updateForRendering != null && CalendarUtils.isSeriesMaster(originalForRendering) && CalendarUtils.isSeriesException(updateForRendering)) {
            RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);

            RecurrenceIterator<Event> recurrenceIterator = recurrenceService.iterateEventOccurrences(originalForRendering, null, null);
            while (recurrenceIterator.hasNext()) {
                Event next = recurrenceIterator.next();
                if (next.getRecurrenceId().equals(updateForRendering.getRecurrenceId())) {
                    originalForRendering.setStartDate(next.getStartDate());
                    originalForRendering.setEndDate(next.getEndDate());
                    return;
                }
            }
        }
    }

    private DateHelper dateHelperFor(final NotificationParticipant participant) {
        return new DateHelper(updated, participant.getLocale(), participant.getTimeZone());
    }

    @Override
    public NotificationMail generateCreateMailFor(final String email) throws OXException {
        for (final NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateCreateMailFor(p);
            }
        }
        return null;
    }

    @Override
    public NotificationMail generateUpdateMailFor(final String email) throws OXException {
        for (final NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateUpdateMailFor(p);
            }
        }
        return null;
    }

    @Override
    public NotificationMail generateDeleteMailFor(final String email) throws OXException {
        for (final NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateDeleteMailFor(p);
            }
        }
        return null;
    }

    @Override
    public NotificationMail generateCreateExceptionMailFor(final String email) throws OXException {
        for (final NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateCreateExceptionMailFor(p);
            }
        }
        return null;
    }

    @Override
    public NotificationMail generateRefreshMailFor(final String email) throws OXException {
        for (final NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateRefreshMailFor(p);
            }
        }
        return null;
    }

    @Override
    public NotificationMail generateDeclineCounterMailFor(final String email) throws OXException {
        for (final NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateDeclineCounterMailFor(p);
            }
        }
        return null;
    }

    @Override
    public List<NotificationParticipant> getRecipients() {
        return recipients;
    }

    protected static interface MailGeneratorState {

        public NotificationMail generateCreateMailFor(NotificationParticipant participant) throws OXException;

        public NotificationMail generateDeclineCounterMailFor(NotificationParticipant participant) throws OXException;

        public NotificationMail generateRefreshMailFor(NotificationParticipant participant) throws OXException;

        public NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws OXException;

        public NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws OXException;

        public NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws OXException;
    }

    protected class OrganizerState implements MailGeneratorState {

        @Override
        public NotificationMail generateCreateMailFor(final NotificationParticipant participant) throws OXException {
            return create(request(participant, null, State.Type.NEW));
        }

        @Override
        public NotificationMail generateUpdateMailFor(final NotificationParticipant participant) throws OXException {
            if (hasBeenRemoved(participant)) {
                if (existsInUpdate(participant)) {
                    return null;
                }
                return delete(cancel(participant));
            }
            if (hasBeenAdded(participant)) {
                if (existsInOriginal(participant)) {
                    return null;
                }
                return create(request(participant, null, State.Type.NEW));
            }
            if (isAboutStateChangesOnly() && participant.isExternal()) {
                return null;
            }
            return update(request(participant, null, State.Type.MODIFIED));
        }

        private boolean isAboutStateChangesOnly() {
            return diff.isAboutStateChangesOnly();
        }

        protected boolean existsInUpdate(NotificationParticipant participant) {
            for (Attendee attendee : updated.getAttendees()) {
                if (participant.getIdentifier() == attendee.getEntity()) {
                    return true;
                }
            }
            return false;
        }

        protected boolean existsInOriginal(NotificationParticipant participant) {
            for (Attendee attendee : original.getAttendees()) {
                if (participant.getIdentifier() == attendee.getEntity()) {
                    return true;
                }
            }
            return false;
        }

        protected boolean hasBeenRemoved(final NotificationParticipant participant) {
            if (diff == null) {
                return false;
            }
            if (diff.containsAnyChangeOf(new EventField[] { EventField.ATTENDEES })) {
                CollectionUpdate<Attendee, AttendeeField> update = diff.getAttendeeUpdates();
                if (update == null || update.isEmpty()) {
                    return false;
                }

                List<Attendee> removed = update.getRemovedItems();
                if (removed == null || removed.isEmpty()) {
                    return false;
                }

                for (Attendee attendee : removed) {
                    if (participant.matches(attendee)) {
                        return true;
                    }
                }
            }
            return false;
        }

        protected boolean hasBeenAdded(final NotificationParticipant participant) {
            if (diff == null) {
                return false;
            }
            if (diff.containsAnyChangeOf(new EventField[] { EventField.ATTENDEES })) {
                CollectionUpdate<Attendee, AttendeeField> update = diff.getAttendeeUpdates();
                if (update == null || update.isEmpty()) {
                    return false;
                }

                List<Attendee> added = update.getAddedItems();
                if (added == null || added.isEmpty()) {
                    return false;
                }

                for (Attendee attendee : added) {
                    if (participant.matches(attendee)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public NotificationMail generateDeleteMailFor(final NotificationParticipant participant) throws OXException {
            return delete(cancel(participant));
        }

        @Override
        public NotificationMail generateCreateExceptionMailFor(final NotificationParticipant participant) throws OXException {
            return createException(add(participant));
        }

        @Override
        public NotificationMail generateDeclineCounterMailFor(final NotificationParticipant participant) throws OXException {
            return declinecounter(declinecounter(participant));
        }

        @Override
        public NotificationMail generateRefreshMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }
    }

    protected class AttendeeWithExternalOrganizerState implements MailGeneratorState {

        protected Boolean stateChanged = null;

        protected ParticipationStatus confirmStatus;

        @Override
        public NotificationMail generateCreateMailFor(final NotificationParticipant participant) throws OXException {
            if (CalendarUtils.isSeriesException(updated)) {
                return counter(counter(participant), ITipRole.ORGANIZER);
            }
            return null;
        }

        @Override
        public NotificationMail generateDeleteMailFor(final NotificationParticipant participant) throws OXException {
            if (participant.hasRole(ITipRole.ORGANIZER) && false == ParticipationStatus.DECLINED.equals(actor.getConfirmStatus())) {
                return stateChanged(reply(participant, ParticipationStatus.DECLINED), ParticipationStatus.DECLINED);
            }
            return null;
        }

        @Override
        public NotificationMail generateUpdateMailFor(final NotificationParticipant participant) throws OXException {
            if (onlyMyStateChanged()) {
                // This needs to be a reply
                if (participant.hasRole(ITipRole.ORGANIZER)) {
                    return stateChanged(reply(participant, confirmStatus), confirmStatus);
                }
                if (!participant.isExternal()) {
                    return stateChanged(noITIP(participant, getStateTypeForStatus(confirmStatus)), confirmStatus);
                }
                return null;
            }

            // This is a counter proposal

            if (participant.hasRole(ITipRole.ORGANIZER)) {
                if (isCounterableOnly()) {
                    return counter(counter(participant), ITipRole.ORGANIZER);
                } else if (ignorableChangedOnly()) {
                    return null;
                } else {
                    return counter(counterNoITIP(participant, Type.MODIFIED), ITipRole.ORGANIZER);
                }
            }
            if (!participant.isExternal()) {
                return counter(noITIP(participant, getStateTypeForStatus(confirmStatus)), ITipRole.ATTENDEE);
            }
            return null;
        }

        protected boolean isCounterableOnly() {
            if (diff == null) {
                return false;
            }

            return diff.containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE, EventField.LOCATION, EventField.SUMMARY, EventField.ATTENDEES, EventField.DESCRIPTION, EventField.RECURRENCE_RULE, EventField.ATTACHMENTS });
        }

        protected boolean ignorableChangedOnly() {
            if (diff == null) {
                return true;
            }

            return diff.containsOnlyChangeOf(new EventField[] { EventField.TRANSP });
        }

        protected boolean onlyMyStateChanged() {
            if (stateChanged != null) {
                return stateChanged.booleanValue();
            }

            if (diff == null) {
                return false;
            }

            if (diff.containsOnlyChangeOf(new EventField[] { EventField.ATTENDEES })) {
                CollectionUpdate<Attendee, AttendeeField> update = diff.getAttendeeUpdates();
                if (update == null || update.isEmpty()) {
                    return false;
                }

                List<? extends ItemUpdate<Attendee, AttendeeField>> updatedItems = update.getUpdatedItems();
                if (updatedItems == null || updatedItems.isEmpty()) {
                    return false;
                }

                if (updatedItems.size() > 1) {
                    return (stateChanged = Boolean.FALSE).booleanValue();
                }

                int identifier = actor.getIdentifier();
                if (onBehalfOf != null) {
                    identifier = onBehalfOf.getIdentifier();
                }
                for (ItemUpdate<Attendee, AttendeeField> updatedItem : updatedItems) {
                    if (updatedItem.getUpdate().getEntity() == identifier && (updatedItem.getUpdatedFields().contains(AttendeeField.PARTSTAT) || updatedItem.getUpdatedFields().contains(AttendeeField.COMMENT))) {
                        confirmStatus = updatedItem.getUpdate().getPartStat();
                        return (stateChanged = Boolean.TRUE).booleanValue();
                    }
                }
            }
            return false;
        }

        @Override
        public NotificationMail generateCreateExceptionMailFor(final NotificationParticipant participant) throws OXException {
            if (participant.hasRole(ITipRole.ORGANIZER)) {
                return createException(counter(participant));
            }
            return createException(noITIP(participant, State.Type.MODIFIED));
        }

        @Override
        public NotificationMail generateDeclineCounterMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateRefreshMailFor(final NotificationParticipant participant) throws OXException {
            return askForUpdate(refresh(participant));
        }

    }

    protected class AttendeeWithInternalOrganizerState extends AttendeeWithExternalOrganizerState {

        private final OrganizerState ORGANIZER = new OrganizerState();

        private final AttendeeWithExternalOrganizerState ATTENDEE = new AttendeeWithExternalOrganizerState();

        @Override
        public NotificationMail generateCreateMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateDeleteMailFor(final NotificationParticipant participant) throws OXException {
            if (actor.hasRole(ITipRole.ATTENDEE)) {
                Event eventToReport = calendarUtilities.copyEvent(updated, (EventField[]) null);
                removeParticipant(eventToReport, session.getUserId());
                NotificationMail request = request(participant, eventToReport, State.Type.MODIFIED);
                request.setOriginal(updated);

                List<NotificationParticipant> newParticipants = new ArrayList<NotificationParticipant>();
                for (NotificationParticipant np : participants) {
                    if (np.getIdentifier() != session.getUserId()) {
                        newParticipants.add(np);
                    }
                }
                request.setParticipants(newParticipants);
                return update(request);
            }
            return ATTENDEE.generateDeleteMailFor(participant);
        }

        private void removeParticipant(final Event event, final int userId) {
            List<Attendee> purged = new ArrayList<>(event.getAttendees().size());
            for (Attendee attendee : event.getAttendees()) {
                if (attendee.getEntity() != userId) {
                    purged.add(attendee);
                }
            }
            event.setAttendees(purged);
        }

        @Override
        public NotificationMail generateUpdateMailFor(final NotificationParticipant participant) throws OXException {
            if (participant.hasRole(ITipRole.ORGANIZER)) {
                if (onlyMyStateChanged()) {
                    if (confirmStatus == null) {
                        return null;
                    }
                    // This needs to be a reply
                    return stateChanged(reply(participant, confirmStatus), confirmStatus);
                }

                // This is a update on behalf of the organizer
                if (isCounterableOnly()) {
                    return update(counter(participant));
                } else if (ignorableChangedOnly()) {
                    return null;
                } else {
                    return update(noITIP(participant, Type.MODIFIED));
                }
            } else if (participant.hasRole(ITipRole.ATTENDEE)) {
                if (onlyMyStateChanged() && confirmStatus != null) {
                    if (participant.isExternal()) {
                        return null; // External users don't care about participant state changes unless they are the organizer
                    }
                    return stateChanged(request(participant, null, getStateTypeForStatus(confirmStatus)), confirmStatus);
                }
                return ORGANIZER.generateUpdateMailFor(participant);
            }
            return null;
        }

        @Override
        public NotificationMail generateCreateExceptionMailFor(final NotificationParticipant participant) throws OXException {
            if (participant.hasRole(ITipRole.ORGANIZER)) {
                return createException(noITIP(participant, State.Type.MODIFIED));
            } else if (participant.hasRole(ITipRole.ATTENDEE)) {
                return ORGANIZER.generateCreateExceptionMailFor(participant);
            }
            return null;
        }

    }

    static protected class DoNothingState implements MailGeneratorState {

        @Override
        public NotificationMail generateCreateExceptionMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateCreateMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateDeleteMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateUpdateMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateDeclineCounterMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateRefreshMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

    }

}
