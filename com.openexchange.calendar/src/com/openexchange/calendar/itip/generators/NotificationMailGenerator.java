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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.api2.OXException;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.ITipRole;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.changes.PassthroughWrapper;
import com.openexchange.calendar.itip.generators.changes.generators.Style;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.State;
import com.openexchange.html.HTMLService;
import com.openexchange.html.tools.HTMLUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.templating.base.OXTemplate;
import com.openexchange.templating.base.TemplateService;
import com.openexchange.user.UserService;
import static com.openexchange.calendar.itip.ITipUtils.*;
import static com.openexchange.java.Autoboxing.I;

/**
 * {@link NotificationMailGenerator}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NotificationMailGenerator implements ITipMailGenerator {

    private NotificationParticipant organizer;

    private NotificationParticipant actor;

    private List<NotificationParticipant> recipients;

    private Appointment appointment;

    private Appointment original;

    private ITipIntegrationUtility util;

    private Session session;

    private AppointmentDiff diff;

    private MailGeneratorState state;

    private Context ctx;
    
    private ServiceLookup services;

    private List<NotificationParticipant> participants;

    public static final int[] DEFAULT_SKIP = new int[]{
        Appointment.OBJECT_ID,
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

    private List<NotificationParticipant> resources;

	private NotificationParticipant principal;

	private NotificationParticipant onBehalfOf;

    

    public NotificationMailGenerator(ServiceLookup services, NotificationParticipantResolver resolver, ITipIntegrationUtility util, Appointment original, Appointment appointment, User user, User onBehalfOf, Context ctx, Session session) throws AbstractOXException {
        this.util = util;
        this.ctx = ctx;
        this.services = services;
        
        this.recipients = resolver.resolveAllRecipients(original, appointment, user, onBehalfOf, ctx);
        this.participants = resolver.getAllParticipants(this.recipients, appointment, user, ctx);
        this.resources = resolver.getResources(appointment, ctx);
        
        for (NotificationParticipant participant : recipients) {
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
        
        if (diff != null && diff.getDifferingFieldNames().isEmpty()) {
            state = new DoNothingState();
        }
    }
    
    public void noActor() {
    	this.actor = this.actor.clone();
    	this.actor.setVirtual(true);
    }
    
    public boolean userIsTheOrganizer() {
        return actor.hasRole(ITipRole.ORGANIZER);
    };


    public NotificationMail generateCreateMailFor(NotificationParticipant participant) throws AbstractOXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }

        return state.generateCreateMailFor(participant);
    }

    public NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws AbstractOXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }

        return state.generateUpdateMailFor(participant);
    }

    public NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws AbstractOXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }

        return state.generateCreateExceptionMailFor(participant);
    }

    public NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws AbstractOXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }
        return state.generateDeleteMailFor(participant);
    }
    
    public NotificationMail generateRefreshMailFor(NotificationParticipant participant) throws AbstractOXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }
        return state.generateRefreshMailFor(participant); 
    }

    public NotificationMail generateDeclineCounterMailFor(NotificationParticipant participant) throws AbstractOXException {
        if (participant.equals(actor) && !this.actor.isVirtual()) {
            return null;
        }
        return state.generateDeclineCounterMailFor(participant);
    }


    protected NotificationMail cancel(NotificationParticipant recipient) {
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(organizer);
        initMail(mail);
        
        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setAppointment(appointment);

        mail.setMessage(message);

        return mail;
    }
    
    private LabelHelper getLabelHelper(NotificationMail mail, TypeWrapper wrapper, NotificationParticipant participant) {
    	return new LabelHelper(dateHelperFor(participant), participant.getTimeZone(), mail, participant.getLocale() , ctx,wrapper, services);
    }

    // IMIP Messages

    protected NotificationMail noITIP(NotificationParticipant recipient, State.Type type) throws AbstractOXException {
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(type);
        initMail(mail);

        return mail;
    }

    protected NotificationMail request(NotificationParticipant recipient, Appointment override,  State.Type type) throws AbstractOXException {
        Appointment appointmentToReport = (override != null) ? override : this.appointment;
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(organizer);
        mail.setStateType(type);
        initMail(mail);

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REQUEST);
        message.setAppointment(appointmentToReport);
        
        if (appointmentToReport.isMaster()) {
            List<Appointment> exceptions = util.getExceptions(appointmentToReport, session);
            if (exceptions != null) {
                for (Appointment exception : exceptions) {
                    message.addException(exception);
                }
            }
        }
        mail.setMessage(message);

        return mail;
    }
    
    protected NotificationMail add(NotificationParticipant recipient) throws AbstractOXException {
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(organizer);
        mail.setStateType(State.Type.NEW);
        initMail(mail);

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REQUEST);
        message.addException(appointment);
        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail counter(NotificationParticipant recipient) throws AbstractOXException {
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.MODIFIED);
        initMail(mail);
        
        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.COUNTER);
        if (appointment.isException()) {
            message.addException(appointment);
        } else {
            message.setAppointment(appointment);
        }

        mail.setMessage(message);

        return mail;
    }

    protected NotificationMail reply(NotificationParticipant recipient, ConfirmStatus confirmStatus) throws AbstractOXException {
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(getStateTypeForStatus(confirmStatus));
        initMail(mail);

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REPLY);

        Appointment reply = new Appointment();
        for (int field : Appointment.ALL_COLUMNS) {
            if (field == Appointment.USERS || field == Appointment.PARTICIPANTS || field == Appointment.CONFIRMATIONS) {
                continue;
            }
            if (appointment.contains(field)) {
                reply.set(field, appointment.get(field));
            }
        }
        UserParticipant userParticipant = new UserParticipant(actor.getIdentifier());
        userParticipant.setConfirm(confirmStatus.getId());
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
    
    private State.Type getStateTypeForStatus(ConfirmStatus confirmStatus) {
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

	protected NotificationMail refresh(NotificationParticipant recipient) throws AbstractOXException {
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.REFRESH);
        initMail(mail);

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.REFRESH);

        Appointment address = appointment.clone();
        for (int field : new int[] { Appointment.USERS, Appointment.CONFIRMATIONS, Appointment.PARTICIPANTS, Appointment.NOTE, Appointment.LOCATION }) {
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
    
    protected NotificationMail declinecounter(NotificationParticipant recipient) throws AbstractOXException {
        NotificationMail mail = new NotificationMail();
        mail.setRecipient(recipient);
        mail.setSender(actor);
        mail.setStateType(State.Type.DECLINE_COUNTER);

        initMail(mail);

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);

        if (appointment.containsRecurrenceDatePosition() || appointment.containsRecurrenceDatePosition()) {
            message.addException(appointment);
        } else {
            message.setAppointment(appointment);
        }

        mail.setMessage(message);

        return mail;
    }

    private void initMail(NotificationMail mail) {
        mail.setOrganizer(organizer);
        mail.setActor(actor);
        mail.setPrincipal(principal);
        mail.setSharedCalendarOwner(onBehalfOf);
        mail.setAppointment(appointment);
        mail.setOriginal(original);
        mail.setParticipants(participants);
        mail.setResources(resources);

    }

    // Templating and mail text

    protected NotificationMail create(NotificationMail mail) throws AbstractOXException {
        mail.setTemplateName("notify.appointment.create");
        mail.setSubject(new Sentence(Messages.SUBJECT_NEW_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail update(NotificationMail mail) throws AbstractOXException {
        mail.setTemplateName("notify.appointment.update");
        mail.setSubject(new Sentence(Messages.SUBJECT_CHANGED_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail delete(NotificationMail mail) throws AbstractOXException {
        mail.setTemplateName("notify.appointment.delete");
        mail.setSubject(new Sentence(Messages.SUBJECT_CANCELLED_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }
    
    protected NotificationMail createException(NotificationMail mail) throws AbstractOXException {
        mail.setTemplateName("notify.appointment.createexception");
        recalculateOccurrence(mail);
        mail.setSubject(new Sentence(Messages.SUBJECT_CHANGED_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }
    
    protected NotificationMail askForUpdate(NotificationMail mail) throws AbstractOXException {
        mail.setTemplateName("notify.appointment.refresh");
        recalculateOccurrence(mail);
        mail.setSubject(new Sentence(Messages.SUBJECT_REFRESH).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    protected NotificationMail declinecounter(NotificationMail mail) throws AbstractOXException {
        mail.setTemplateName("notify.appointment.declinecounter");
        recalculateOccurrence(mail);
        mail.setSubject(new Sentence(Messages.SUBJECT_DECLINECOUNTER).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
        render(mail);
        return mail;
    }

    private void recalculateOccurrence(NotificationMail mail) {
        CalendarCollection calCol = new CalendarCollection();
        Appointment originalOcurrence = mail.getOriginal();
        Appointment newAppointment = mail.getAppointment();
        if (newAppointment.getRecurrenceDatePosition() == null) {
            return;
        }
        try {
            if (!originalOcurrence.containsTimezone()) {
                originalOcurrence.setTimezone("UTC");
            }
            RecurringResultsInterface recurring = calCol.calculateRecurring(originalOcurrence, startOfTheDay(newAppointment.getRecurrenceDatePosition()), endOfTheDay(newAppointment.getRecurrenceDatePosition()), 0);
            if (recurring != null && recurring.size() > 0) {
                RecurringResultInterface recurringResult = recurring.getRecurringResult(0);
                originalOcurrence.setStartDate(new Date(recurringResult.getStart()));
                originalOcurrence.setEndDate(new Date(recurringResult.getEnd()));
            }
        } catch (OXException e) {
            // IGNORE
        }
        
        mail.setOriginal(originalOcurrence);
    }



    protected NotificationMail counter(NotificationMail mail, ITipRole role) throws AbstractOXException {
        switch (role) {
        case ATTENDEE:
            mail.setSubject(new Sentence(Messages.SUBJECT_COUNTER_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.counter.participant");
            break;
        case ORGANIZER:
            mail.setSubject(new Sentence(Messages.SUBJECT_COUNTER_APPOINTMENT).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.counter.organizer");
            break;
        }
        render(mail);
        return mail;
    }

    protected NotificationMail stateChanged(NotificationMail mail, ConfirmStatus status) throws AbstractOXException {
        switch (status) {
        case ACCEPT:
            mail.setSubject(new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).add(Messages.ACCEPTED).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.accept");
            break;
        case DECLINE:
            mail.setSubject(new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).add(Messages.DECLINED).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.decline");
            break;
        case TENTATIVE:
            mail.setSubject(new Sentence(Messages.SUBJECT_STATE_CHANGED).add(actor.getDisplayName()).add(Messages.TENTATIVELY_ACCEPTED).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.tentative");
            break;
        case NONE:
            mail.setSubject(new Sentence(Messages.SUBJECT_NONE).add(actor.getDisplayName()).add(mail.getAppointment().getTitle()).getMessage(mail.getRecipient().getLocale()));
            mail.setTemplateName("notify.appointment.none");
            break;
        }
        render(mail);
        return mail;
    }

    private void render(NotificationMail mail) throws AbstractOXException {
        if (services == null) {
            return;
        }
        Appointment originalForRendering = mail.getOriginal();
		Appointment appointmentForRendering = mail.getAppointment();
		
		

		fastForwardToConcreteOriginalOccurrence(originalForRendering, appointmentForRendering);
		
		NotificationParticipant participant = mail.getRecipient();
        
        TemplateService templates = services.getService(TemplateService.class);
        if (templates == null) {
            return;
        }
        OXTemplate textTemplate = templates.loadTemplate(mail.getTemplateName()+".txt.tmpl", mail.getTemplateName()+".txt.tmpl", session, false);
        OXTemplate htmlTemplate = templates.loadTemplate(mail.getTemplateName()+".html.tmpl", mail.getTemplateName()+".html.tmpl", session, false);
        
        Style style = null;
        if (mail.getRecipient().hasRole(ITipRole.ORGANIZER)) {
            style = Style.ASK;
        }
        if (mail.getSender().hasRole(ITipRole.ORGANIZER)) {
            style = Style.FAIT_ACCOMPLI;
        }
        if (mail.getOrganizer().isExternal() && mail.getRecipient().hasRole(ITipRole.ATTENDEE)) {
            style = Style.INTENTION;
        }
        
        Map<String,Object> env = new HashMap<String, Object>();
        PassthroughWrapper wrapper = new PassthroughWrapper();
        
        env.put("mail", mail);
        env.put("templating", templates.createHelper(env, session));
        env.put("formatters", dateHelperFor(mail.getRecipient()));
        env.put("labels",getLabelHelper(mail, wrapper, participant));
		if (originalForRendering != null) {
            env.put("changes", new ChangeHelper(ctx, mail.getRecipient(), originalForRendering, appointmentForRendering, mail.getDiff(), participant.getLocale(), participant.getTimeZone(), wrapper, services, style).getChanges());
        } else {
            env.put("changes", new ArrayList<String>());
        }
        env.put("participantHelper", new ParticipantHelper(participant.getLocale()));
        
        StringWriter writer = new StringWriter();
        textTemplate.process(env, writer);
        mail.setText(writer.toString());
        
        wrapper = new HTMLWrapper();
        env.put("labels", getLabelHelper(mail, wrapper, participant));
        if (originalForRendering != null) {
            env.put("changes", new ChangeHelper(ctx, mail.getRecipient(), originalForRendering, appointmentForRendering, mail.getDiff(), participant.getLocale(), participant.getTimeZone(), wrapper, services, style).getChanges());
        }
        writer = new StringWriter();
        htmlTemplate.process(env, writer);
        mail.setHtml(writer.toString());
    }

    private void fastForwardToConcreteOriginalOccurrence(
			Appointment originalForRendering,
			Appointment appointmentForRendering) {
    	if (originalForRendering != null && appointmentForRendering != null && originalForRendering.isMaster() && appointmentForRendering.isException()) {
        	CalendarCollection calCol = new CalendarCollection();
            try {
                if (!originalForRendering.containsTimezone()) {
                	originalForRendering.setTimezone("UTC");
                }
                RecurringResultsInterface recurring = calCol.calculateRecurring(originalForRendering, startOfTheDay(appointmentForRendering.getRecurrenceDatePosition()), endOfTheDay(appointmentForRendering.getRecurrenceDatePosition()), 0);
                if (recurring.size() > 0) {
                    RecurringResultInterface recurringResult = recurring.getRecurringResult(0);
                    originalForRendering.setStartDate(new Date(recurringResult.getStart()));
                    originalForRendering.setEndDate(new Date(recurringResult.getEnd()));
                }
            } catch (OXException e) {
                // IGNORE
            }
    	}
	}

	private DateHelper dateHelperFor(NotificationParticipant participant) {
		return new DateHelper(appointment, participant.getLocale(), participant.getTimeZone());
	}

	public NotificationMail generateCreateMailFor(String email) throws AbstractOXException {
        for (NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateCreateMailFor(p);
            }
        }
        return null;
    }

    public NotificationMail generateUpdateMailFor(String email) throws AbstractOXException {
        for (NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateUpdateMailFor(p);
            }
        }
        return null;
    }

    public NotificationMail generateDeleteMailFor(String email) throws AbstractOXException {
        for (NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateDeleteMailFor(p);
            }
        }
        return null;
    }

    public NotificationMail generateCreateExceptionMailFor(String email) throws AbstractOXException {
        for (NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateCreateExceptionMailFor(p);
            }
        }
        return null;
    }
    
    public NotificationMail generateRefreshMailFor(String email) throws AbstractOXException {
        for (NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateRefreshMailFor(p);
            }
        }
        return null;
    }

	public NotificationMail generateDeclineCounterMailFor(String email) throws AbstractOXException {
        for (NotificationParticipant p : recipients) {
            if (p.getEmail().equalsIgnoreCase(email)) {
                return generateDeclineCounterMailFor(p);
            }
        }
        return null;
	}

    public List<NotificationParticipant> getRecipients() {
        return recipients;
    }

    protected static interface MailGeneratorState {

        public NotificationMail generateCreateMailFor(NotificationParticipant participant) throws AbstractOXException;

        public NotificationMail generateDeclineCounterMailFor(NotificationParticipant participant) throws AbstractOXException;

		public NotificationMail generateRefreshMailFor(NotificationParticipant participant) throws AbstractOXException;

		public NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws AbstractOXException;

        public NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws AbstractOXException;

        public NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws AbstractOXException;
    }

    protected class OrganizerState implements MailGeneratorState {

        public NotificationMail generateCreateMailFor(NotificationParticipant participant) throws AbstractOXException {
            return create(request(participant, null, State.Type.NEW));
        }

        public NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws AbstractOXException {
            if (hasBeenRemoved(participant)) {
                return delete(cancel(participant));
            }
            if (hasBeenAdded(participant)) {
                return create(request(participant, null, State.Type.NEW));
            }
            return update(request(participant, null, State.Type.MODIFIED	));
        }

        protected boolean hasBeenRemoved(NotificationParticipant participant) {
            if (diff == null) {
                return false;
            }
            if (diff.anyFieldChangedOf("participants")) {
                FieldUpdate update = diff.getUpdateFor("participants");
                Difference difference = (Difference) update.getExtraInfo();
                List<Object> removed = difference.getRemoved();
                for (Object object : removed) {
                    if (participant.matches((Participant) object)) {
                        return true;
                    }
                }
            }
            return false;
        }

        protected boolean hasBeenAdded(NotificationParticipant participant) {
            if (diff == null) {
                return false;
            }
            if (diff.anyFieldChangedOf("participants")) {
                FieldUpdate update = diff.getUpdateFor("participants");
                Difference difference = (Difference) update.getExtraInfo();
                List<Object> added = difference.getAdded();
                for (Object object : added) {
                    if (participant.matches((Participant) object)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws AbstractOXException {
            return delete(cancel(participant));
        }

        public NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws AbstractOXException {
            return createException(add(participant));
        }

		public NotificationMail generateDeclineCounterMailFor(
				NotificationParticipant participant) throws AbstractOXException {
			return declinecounter(declinecounter(participant));
		}

		public NotificationMail generateRefreshMailFor(
				NotificationParticipant participant) throws AbstractOXException {
			return null;
		}
    }

    protected class AttendeeWithExternalOrganizerState implements MailGeneratorState {

        protected Boolean stateChanged = null;

        protected ConfirmStatus confirmStatus;

        public NotificationMail generateCreateMailFor(NotificationParticipant participant) throws AbstractOXException {
            return null;
        }

        public NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws AbstractOXException {
            return stateChanged(reply(participant, ConfirmStatus.DECLINE), ConfirmStatus.DECLINE);
        }

        public NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws AbstractOXException {
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
                return counter(counter(participant), ITipRole.ORGANIZER);
            }
            if (!participant.isExternal()) {
                return counter(noITIP(participant, getStateTypeForStatus(confirmStatus)), ITipRole.ATTENDEE);
            }
            return null;
        }

        protected boolean onlyMyStateChanged() {
            if (stateChanged != null) {
                return stateChanged;
            }
            if (diff == null) {
                return false;
            }
            if (diff.onlyTheseChanged("participants", "confirmations", "users") && diff.anyFieldChangedOf("users")) {
                FieldUpdate update = diff.getUpdateFor("users");
                Difference difference = (Difference) update.getExtraInfo();
                List<Change> changed = difference.getChanged();
                String identifier = String.valueOf(actor.getIdentifier());
                for (Change change : changed) {
                    if (change.getIdentifier().equals(identifier)) {
                        ConfirmationChange confChange = (ConfirmationChange) change;
                        confirmStatus = ConfirmStatus.byId(confChange.getNewStatus());
                        return stateChanged = true;
                    }
                }
            }
            return false;
        }

        public NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws AbstractOXException {
            if (participant.hasRole(ITipRole.ORGANIZER)) {
                return createException(counter(participant));
            }
            return createException(noITIP(participant, State.Type.MODIFIED));
        }

		public NotificationMail generateDeclineCounterMailFor(
				NotificationParticipant participant) throws AbstractOXException {
			return null;
		}

		public NotificationMail generateRefreshMailFor(
				NotificationParticipant participant) throws AbstractOXException {
			return askForUpdate(refresh(participant));
		}

    }

    protected class AttendeeWithInternalOrganizerState extends AttendeeWithExternalOrganizerState {

        private OrganizerState ORGANIZER = new OrganizerState();

        private AttendeeWithExternalOrganizerState ATTENDEE = new AttendeeWithExternalOrganizerState();
        
        @Override
        public NotificationMail generateCreateMailFor(NotificationParticipant participant) throws AbstractOXException {
            return null;
        }
        
        @Override
        public NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws AbstractOXException {
            if (participant.hasRole(ITipRole.ATTENDEE)) {
                Appointment appointmentToReport = appointment.clone();
                removeParticipant(appointmentToReport, session.getUserId());
                return update(request(participant, appointmentToReport, State.Type.DELETED));
            }
            return ATTENDEE.generateDeleteMailFor(participant);
        }

        private void removeParticipant(Appointment appointment, int userId) {
            List<Participant> purged = new ArrayList<Participant>(appointment.getParticipants().length);
            for (Participant p : appointment.getParticipants()) {
                if (p instanceof UserParticipant) {
                    UserParticipant up = (UserParticipant) p;
                    if (up.getIdentifier() != userId) {
                        purged.add(up);
                    }
                } else {
                    purged.add(p);
                }
            }
            appointment.setParticipants(purged);

            List<UserParticipant> users = new ArrayList<UserParticipant>(appointment.getUsers().length);
            for (UserParticipant up : appointment.getUsers()) {
                if (up.getIdentifier() != userId) {
                    users.add(up);
                }
            }
            appointment.setUsers(users);
        }
        
        @Override
        public NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws AbstractOXException {
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
                    return update(counter(participant));
                }
                if (!participant.isExternal()) {
                    return update(noITIP(participant, State.Type.MODIFIED));
                }
                return null;
            } else if (participant.hasRole(ITipRole.ATTENDEE)) {
                if (onlyMyStateChanged()) {
                    return stateChanged(request(participant, null, getStateTypeForStatus(confirmStatus)), confirmStatus);
                } 
                return ORGANIZER.generateUpdateMailFor(participant);
            }
            return null;
        }
        
        @Override
        public NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws AbstractOXException {
        	if (participant.hasRole(ITipRole.ORGANIZER)) {
                return createException(noITIP(participant, State.Type.MODIFIED));
        	} else if (participant.hasRole(ITipRole.ATTENDEE)) {
                return ORGANIZER.generateCreateExceptionMailFor(participant);
        	}
            return null;
        }

    }
    
    protected class DoNothingState implements MailGeneratorState {

        public NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws AbstractOXException {
            return null;
        }

        public NotificationMail generateCreateMailFor(NotificationParticipant participant) throws AbstractOXException {
            return null;
        }

        public NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws AbstractOXException {
            return null;
        }

        public NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws AbstractOXException {
            return null;
        }

		public NotificationMail generateDeclineCounterMailFor(
				NotificationParticipant participant) throws AbstractOXException {
			return null;
		}

		public NotificationMail generateRefreshMailFor(
				NotificationParticipant participant) throws AbstractOXException {
			return null;
		}
        
    }


}
