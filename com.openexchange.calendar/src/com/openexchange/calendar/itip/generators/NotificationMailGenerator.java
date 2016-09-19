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

package com.openexchange.calendar.itip.generators;

import static com.openexchange.calendar.itip.ITipUtils.endOfTheDay;
import static com.openexchange.calendar.itip.ITipUtils.startOfTheDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.ITipRole;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.changes.PassthroughWrapper;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.State;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link NotificationMailGenerator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NotificationMailGenerator implements ITipMailGenerator {

    private NotificationParticipant organizer;

    private NotificationParticipant actor;

    private final List<NotificationParticipant> recipients;

    private final Appointment appointment;

    private final Appointment original;

    private final ITipIntegrationUtility util;

    private final Session session;

    private AppointmentDiff diff;

    private MailGeneratorState state;

    private final Context ctx;

    private final AttachmentMemory attachmentMemory;

    private final ServiceLookup services;

    private final List<NotificationParticipant> participants;

    private final User user;

    private UserConfiguration userConfig;

    public static final int[] DEFAULT_SKIP = new int[]{
        Appointment.OBJECT_ID,
        Appointment.FOLDER_ID,
        Appointment.CREATED_BY,
        Appointment.MODIFIED_BY,
        Appointment.CREATION_DATE,
        Appointment.LAST_MODIFIED,
        Appointment.LAST_MODIFIED_UTC,
        Appointment.ALARM,
        Appointment.NOTIFICATION,
        Appointment.RECURRENCE_TYPE,
        Appointment.CATEGORIES,
        Appointment.SEQUENCE,
        Appointment.SHOWN_AS
    };

    private final List<NotificationParticipant> resources;

	private NotificationParticipant principal;

	private NotificationParticipant onBehalfOf;



    public NotificationMailGenerator(final ServiceLookup services, final AttachmentMemory attachmentMemory, final NotificationParticipantResolver resolver, final ITipIntegrationUtility util, final Appointment original, final Appointment appointment, final User user, final User onBehalfOf, final Context ctx, final Session session) throws OXException {
        this.util = util;
        this.ctx = ctx;
        this.services = services;
        this.attachmentMemory = attachmentMemory;

        this.user = user;
        if (UserConfigurationStorage.getInstance() != null) {
            this.userConfig = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
        }

        this.recipients = resolver.resolveAllRecipients(original, appointment, user, onBehalfOf, ctx);
        this.participants = resolver.getAllParticipants(this.recipients, appointment, user, ctx);
        this.resources = resolver.getResources(appointment, ctx);

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

        this.original = original;
        this.appointment = appointment;
        this.session = session;

        if (original != null) {
            this.diff = AppointmentDiff.compare(original, appointment, DEFAULT_SKIP);
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

        if (diff != null && diff.getDifferingFieldNames().isEmpty() && !attachmentMemory.hasAttachmentChanged(appointment.getObjectID(), session.getContextId())) {
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
    };


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
        mail.setSender(recipient.isExternal() ? organizer : actor);
        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setAppointment(appointment);

        mail.setMessage(message);

        return mail;
    }

    private LabelHelper getLabelHelper(final NotificationMail mail, final TypeWrapper wrapper, final NotificationParticipant participant) {
    	return new LabelHelper(dateHelperFor(participant), participant.getTimeZone(), mail, participant.getLocale() , ctx,wrapper, services);
    }

    // IMIP Messages

    protected NotificationMail noITIP(final NotificationParticipant recipient, final State.Type type) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(recipient.isExternal() ? organizer : actor);
        mail.setStateType(type);
        initMail(mail);

        return mail;
    }

    protected NotificationMail counterNoITIP(final NotificationParticipant recipient, final State.Type type) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(type);
        initMail(mail);

        return mail;
    }

    protected NotificationMail request(final NotificationParticipant recipient, final Appointment override,  final State.Type type) throws OXException {
        final Appointment appointmentToReport = (override != null) ? override : this.appointment;
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(recipient.isExternal() ? organizer : actor);
        mail.setStateType(type);
        initMail(mail);
        initAttachments(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REQUEST);
        message.setAppointment(appointmentToReport);

        if (appointmentToReport.isMaster()) {
            final List<Appointment> exceptions = util.getExceptions(appointmentToReport, session);
            if (exceptions != null) {
                for (final Appointment exception : exceptions) {
                    message.addException(exception);
                }
            }
        }
        mail.setMessage(message);
        mail.setOriginal(original);
        mail.setAppointment(appointmentToReport);

        return mail;
    }

    protected NotificationMail add(final NotificationParticipant recipient) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(recipient.isExternal() ? organizer : actor);
        mail.setStateType(State.Type.NEW);
        initMail(mail);
        initAttachments(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REQUEST);
        message.addException(appointment);
        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail counter(final NotificationParticipant recipient) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.MODIFIED);
        initMail(mail);
        initAttachments(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.COUNTER);
        if (appointment.isException()) {
            message.addException(appointment);
        } else {
            message.setAppointment(appointment);
        }

        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail reply(final NotificationParticipant recipient, final ConfirmStatus confirmStatus) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(onBehalfOf != null ? onBehalfOf : actor);
        mail.setStateType(getStateTypeForStatus(confirmStatus));
        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REPLY);

        final Appointment reply = new Appointment();
        for (final int field : Appointment.ALL_COLUMNS) {
            if (field == CalendarObject.USERS || field == CalendarObject.PARTICIPANTS || field == CalendarObject.CONFIRMATIONS) {
                continue;
            }
            if (appointment.contains(field)) {
                reply.set(field, appointment.get(field));
            }
        }
        final UserParticipant userParticipant = new UserParticipant((onBehalfOf != null ? onBehalfOf : actor).getIdentifier());
        userParticipant.setConfirm(null == confirmStatus ? ConfirmStatus.NONE.getId() : confirmStatus.getId());
        reply.setParticipants(Arrays.asList((Participant) userParticipant));
        reply.setUsers(Arrays.asList(userParticipant));
        if (reply.containsRecurrenceDatePosition() || reply.containsRecurrenceDatePosition()) {
            message.addException(reply);
        } else {
            message.setAppointment(reply);
        }

        mail.setMessage(message);

        return mail;
    }

    private State.Type getStateTypeForStatus(final ConfirmStatus confirmStatus) {
    	if (confirmStatus == null) {
    		return State.Type.MODIFIED;
    	}
    	switch (confirmStatus) {
        case ACCEPT: return State.Type.ACCEPTED;
        case DECLINE:return State.Type.DECLINED;
        case TENTATIVE:return State.Type.TENTATIVELY_ACCEPTED;
        case NONE:return State.Type.NONE_ACCEPTED;
        }
    	return State.Type.MODIFIED;
	}

	protected NotificationMail refresh(final NotificationParticipant recipient) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.REFRESH);
        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REFRESH);

        final Appointment address = appointment.clone();
        for (final int field : new int[] { CalendarObject.USERS, CalendarObject.CONFIRMATIONS, CalendarObject.PARTICIPANTS, CalendarObject.NOTE, Appointment.LOCATION }) {
            address.remove(field);
        }

        if (address.containsRecurrenceDatePosition() || address.containsRecurrenceDatePosition()) {
            message.addException(address);
        } else {
            message.setAppointment(address);
        }

        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail declinecounter(final NotificationParticipant recipient) throws OXException {
        final NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.DECLINE_COUNTER);

        initMail(mail);

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);

        if (appointment.containsRecurrenceDatePosition() || appointment.containsRecurrenceDatePosition()) {
            message.addException(appointment);
        } else {
            message.setAppointment(appointment);
        }

        mail.setMessage(message);

        return mail;
    }

    private void initMail(final NotificationMail mail) {
        mail.setOrganizer(organizer);
        mail.setActor(actor);
        mail.setPrincipal(principal);
        mail.setSharedCalendarOwner(onBehalfOf);
        mail.setAppointment(appointment);
        mail.setOriginal(original);
        mail.setParticipants(participants);
        mail.setResources(resources);

    }

    private void initAttachments(final NotificationMail mail) throws OXException {
    	if (services == null) {
    		return;
    	}
    	if (!attachmentMemory.hasAttachmentChanged(appointment.getObjectID(), ctx.getContextId())) {
    		return;
    	}
		mail.setAttachmentUpdate(true);
		AttachmentBase attachments = services.getService(AttachmentBase.class);
    	SearchIterator<AttachmentMetadata> results = null;
    	try {
    		TimedResult<AttachmentMetadata> attachmentsResult = attachments.getAttachments(session, mail.getAppointment().getParentFolderID(), mail.getAppointment().getObjectID(), Types.APPOINTMENT, ctx, user, userConfig);
    		results = attachmentsResult.results();
    		while(results.hasNext()) {
    			mail.addAttachment(results.next());
    		}
    	} catch (OXException x) {
    		// Best effort only
    	} finally {
    		if (results != null) {
        		results.close();
    		}
    	}
    }

    // Templating and mail text

    protected NotificationMail create(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.create");
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_NEW_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        if (mail.getRecipient().isResource()) {
            mail.setAdditionalHeader("X-Open-Xchange-Recipient-Type", "Resource");
        }
        render(mail);
        return mail;
    }

    protected NotificationMail update(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.update");
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_CHANGED_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        if (mail.getRecipient().isResource()) {
            mail.setAdditionalHeader("X-Open-Xchange-Recipient-Type", "Resource");
        }
        render(mail);
        return mail;
    }

    protected NotificationMail delete(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.delete");
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_CANCELLED_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail createException(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.createexception");
        recalculateOccurrence(mail);
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_CHANGED_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail askForUpdate(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.refresh");
        recalculateOccurrence(mail);
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_REFRESH).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail declinecounter(final NotificationMail mail) throws OXException {
        mail.setTemplateName("notify.appointment.declinecounter");
        recalculateOccurrence(mail);
        mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_DECLINECOUNTER).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    private void recalculateOccurrence(final NotificationMail mail) {
        final CalendarCollection calCol = new CalendarCollection();
        final Appointment originalOcurrence = mail.getOriginal();
        final Appointment newAppointment = mail.getAppointment();
        if (newAppointment.getRecurrenceDatePosition() == null) {
            return;
        }
        try {
            if (!originalOcurrence.containsTimezone()) {
                originalOcurrence.setTimezone("UTC");
            }
            final RecurringResultsInterface recurring = calCol.calculateRecurring(originalOcurrence, startOfTheDay(newAppointment.getRecurrenceDatePosition()), endOfTheDay(newAppointment.getRecurrenceDatePosition()), 0);
            if (recurring != null && recurring.size() > 0) {
                final RecurringResultInterface recurringResult = recurring.getRecurringResult(0);
                originalOcurrence.setStartDate(new Date(recurringResult.getStart()));
                originalOcurrence.setEndDate(new Date(recurringResult.getEnd()));
            }
        } catch (final OXException e) {
            // IGNORE
        }

        mail.setOriginal(originalOcurrence);
    }



    protected NotificationMail counter(final NotificationMail mail, final ITipRole role) throws OXException {
        switch (role) {
        case ATTENDEE:
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_COUNTER_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.counter.participant");
            break;
        case ORGANIZER:
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_COUNTER_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.counter.organizer");
            break;
        }
        render(mail);
        return mail;
    }

    protected NotificationMail stateChanged(final NotificationMail mail, final ConfirmStatus status) throws OXException {
        final ConfirmStatus stat = null == status ? ConfirmStatus.NONE : status;
        switch (stat) {
        case ACCEPT:
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).addStatus(status).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.accept");
            break;
        case DECLINE:
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).addStatus(status).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.decline");
            break;
        case TENTATIVE:
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).addStatus(status).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.tentative");
            break;
        case NONE:
            mail.setSubject(prefix(mail) + new Sentence(Messages.SUBJECT_NONE).add(actor.getDisplayName()).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.none");
            break;
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
        final Appointment originalForRendering = mail.getOriginal();
		final Appointment appointmentForRendering = mail.getAppointment();



		fastForwardToConcreteOriginalOccurrence(originalForRendering, appointmentForRendering);

		final NotificationParticipant participant = mail.getRecipient();

        final TemplateService templates = services.getService(TemplateService.class);
        if (templates == null) {
            return;
        }
        final OXTemplate textTemplate = templates.loadTemplate(mail.getTemplateName()+".txt.tmpl");
        final OXTemplate htmlTemplate = templates.loadTemplate(mail.getTemplateName()+".html.tmpl");


        final Map<String,Object> env = new HashMap<String, Object>();
        PassthroughWrapper wrapper = new PassthroughWrapper();

        env.put("mail", mail);
        env.put("templating", templates.createHelper(env, null, false));
        env.put("formatters", dateHelperFor(mail.getRecipient()));
        env.put("labels",getLabelHelper(mail, wrapper, participant));
		if (originalForRendering != null) {
            env.put("changes", new ChangeHelper(ctx, mail.getRecipient(), originalForRendering, appointmentForRendering, mail.getDiff(), participant.getLocale(), participant.getTimeZone(), wrapper, attachmentMemory, services).getChanges());
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
            env.put("changes", new ChangeHelper(ctx, mail.getRecipient(), originalForRendering, appointmentForRendering, mail.getDiff(), participant.getLocale(), participant.getTimeZone(), wrapper, attachmentMemory, services).getChanges());
        }
        writer = new AllocatingStringWriter();
        htmlTemplate.process(env, writer);
        mail.setHtml(writer.toString());
    }

    private void fastForwardToConcreteOriginalOccurrence(
			final Appointment originalForRendering,
			final Appointment appointmentForRendering) {
    	if (originalForRendering != null && appointmentForRendering != null && originalForRendering.isMaster() && appointmentForRendering.isException()) {
        	final CalendarCollection calCol = new CalendarCollection();
            try {
                if (!originalForRendering.containsTimezone()) {
                	originalForRendering.setTimezone("UTC");
                }
                final RecurringResultsInterface recurring = calCol.calculateRecurring(originalForRendering, startOfTheDay(appointmentForRendering.getRecurrenceDatePosition()), endOfTheDay(appointmentForRendering.getRecurrenceDatePosition()), 0);
                if (recurring.size() > 0) {
                    final RecurringResultInterface recurringResult = recurring.getRecurringResult(0);
                    originalForRendering.setStartDate(new Date(recurringResult.getStart()));
                    originalForRendering.setEndDate(new Date(recurringResult.getEnd()));
                }
            } catch (final OXException e) {
                // IGNORE
            }
    	}
	}

	private DateHelper dateHelperFor(final NotificationParticipant participant) {
		return new DateHelper(appointment, participant.getLocale(), participant.getTimeZone());
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
            return update(request(participant, null, State.Type.MODIFIED	));
        }

        private boolean isAboutStateChangesOnly() {
			return diff.isAboutStateChangesOnly();
		}

        protected boolean existsInUpdate(NotificationParticipant participant) {
            for (UserParticipant userParticipant : appointment.getUsers()) {
                if (participant.getIdentifier() == userParticipant.getIdentifier()) {
                    return true;
                }
            }
            return false;
        }

        protected boolean existsInOriginal(NotificationParticipant participant) {
            for (UserParticipant userParticipant : original.getUsers()) {
                if (participant.getIdentifier() == userParticipant.getIdentifier()) {
                    return true;
                }
            }
            return false;
        }

        protected boolean hasBeenRemoved(final NotificationParticipant participant) {
            if (diff == null) {
                return false;
            }
            if (diff.anyFieldChangedOf("participants")) {
                final FieldUpdate update = diff.getUpdateFor("participants");
                if (update == null) {
                    return false;
                }
                final Difference difference = (Difference) update.getExtraInfo();
                final List<Object> removed = difference.getRemoved();
                for (final Object object : removed) {
                    if (participant.matches((Participant) object)) {
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
            if (diff.anyFieldChangedOf("participants")) {
                final FieldUpdate update = diff.getUpdateFor("participants");
                if (update == null) {
                    return false;
                }
                final Difference difference = (Difference) update.getExtraInfo();
                final List<Object> added = difference.getAdded();
                for (final Object object : added) {
                    if (participant.matches((Participant) object)) {
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
        public NotificationMail generateDeclineCounterMailFor(
				final NotificationParticipant participant) throws OXException {
			return declinecounter(declinecounter(participant));
		}

		@Override
        public NotificationMail generateRefreshMailFor(
				final NotificationParticipant participant) throws OXException {
			return null;
		}
    }

    protected class AttendeeWithExternalOrganizerState implements MailGeneratorState {

        protected Boolean stateChanged = null;

        protected ConfirmStatus confirmStatus;

        @Override
        public NotificationMail generateCreateMailFor(final NotificationParticipant participant) throws OXException {
            return null;
        }

        @Override
        public NotificationMail generateDeleteMailFor(final NotificationParticipant participant) throws OXException {
            return stateChanged(reply(participant, ConfirmStatus.DECLINE), ConfirmStatus.DECLINE);
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

        	return diff.anyFieldChangedOf(CalendarFields.START_DATE,  CalendarFields.END_DATE,  AppointmentFields.LOCATION,  CalendarFields.TITLE, CalendarFields.PARTICIPANTS, CalendarFields.USERS, CalendarFields.CONFIRMATIONS);
		}

        protected boolean ignorableChangedOnly() {
            if (diff == null) {
                return true;
            }

            return diff.onlyTheseChanged(AppointmentFields.SHOW_AS);
        }

		protected boolean onlyMyStateChanged() {
            if (stateChanged != null) {
                return stateChanged;
            }
            if (diff == null) {
                return false;
            }
            if (diff.onlyTheseChanged("participants", "confirmations", "users") && diff.anyFieldChangedOf("users")) {
                final FieldUpdate update = diff.getUpdateFor("users");
                if (update == null) {
                    return false;
                }
                final Difference difference = (Difference) update.getExtraInfo();
                final List<Change> changed = difference.getChanged();
                if (changed.size() > 1) {
                    return stateChanged = false;
                }
                String identifier = Integer.toString(actor.getIdentifier());
                if (onBehalfOf != null) {
                    identifier = Integer.toString(onBehalfOf.getIdentifier());
                }
                for (final Change change : changed) {
                    if (change.getIdentifier().equals(identifier)) {
                        final ConfirmationChange confChange = (ConfirmationChange) change;
                        confirmStatus = ConfirmStatus.byId(confChange.getNewStatus());
                        return stateChanged = true;
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
        public NotificationMail generateDeclineCounterMailFor(
				final NotificationParticipant participant) throws OXException {
			return null;
		}

		@Override
        public NotificationMail generateRefreshMailFor(
				final NotificationParticipant participant) throws OXException {
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
                Appointment appointmentToReport = appointment.clone();
                removeParticipant(appointmentToReport, session.getUserId());
                NotificationMail request = request(participant, appointmentToReport, State.Type.MODIFIED);
                request.setOriginal(appointment);

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

        private void removeParticipant(final Appointment appointment, final int userId) {
            final List<Participant> purged = new ArrayList<Participant>(appointment.getParticipants().length);
            for (final Participant p : appointment.getParticipants()) {
                if (p instanceof UserParticipant) {
                    final UserParticipant up = (UserParticipant) p;
                    if (up.getIdentifier() != userId) {
                        purged.add(up);
                    }
                } else {
                    purged.add(p);
                }
            }
            appointment.setParticipants(purged);

            final List<UserParticipant> users = new ArrayList<UserParticipant>(appointment.getUsers().length);
            for (final UserParticipant up : appointment.getUsers()) {
                if (up.getIdentifier() != userId) {
                    users.add(up);
                }
            }
            appointment.setUsers(users);
        }

        @Override
        public NotificationMail generateUpdateMailFor(final NotificationParticipant participant) throws OXException {
            if (participant.hasRole(ITipRole.ORGANIZER)) {
            	if (onlyMyStateChanged()) {
            		if (confirmStatus == null) {
            			return null;
            		}
                    // This needs to be a reply
                    if (participant.hasRole(ITipRole.ORGANIZER)) {
                        return stateChanged(reply(participant, confirmStatus), confirmStatus);
                    }
                    if (!participant.isExternal()) {
                        return stateChanged(noITIP(participant, getStateTypeForStatus(confirmStatus)), confirmStatus);
                    }
                    return null;
                }

                // This is a update on behalf of the organizer

                if (participant.hasRole(ITipRole.ORGANIZER)) {
                	if (isCounterableOnly()) {
                        return update(counter(participant));
                	} else if (ignorableChangedOnly()) {
                	    return null;
                	} else {
                        return update(noITIP(participant, Type.MODIFIED));
                	}
                }
                if (!participant.isExternal()) {
                    return update(noITIP(participant, State.Type.MODIFIED));
                }
                return null;
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

    protected class DoNothingState implements MailGeneratorState {

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
        public NotificationMail generateDeclineCounterMailFor(
				final NotificationParticipant participant) throws OXException {
			return null;
		}

		@Override
        public NotificationMail generateRefreshMailFor(
				final NotificationParticipant participant) throws OXException {
			return null;
		}

    }


}
