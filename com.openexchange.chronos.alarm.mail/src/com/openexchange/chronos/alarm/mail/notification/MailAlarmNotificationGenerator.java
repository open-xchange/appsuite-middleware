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

package com.openexchange.chronos.alarm.mail.notification;

import static com.openexchange.osgi.Tools.requireService;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.alarm.mail.impl.MailAlarmMailStrings;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.compat.ShownAsTransparency;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.generators.DateHelper;
import com.openexchange.chronos.itip.generators.HTMLWrapper;
import com.openexchange.chronos.itip.generators.LabelHelper;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.generators.ParticipantHelper;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.State;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;

/**
 * 
 * {@link MailAlarmNotificationGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class MailAlarmNotificationGenerator {

    private final ServiceLookup services;

    private final Event event;

    private final Context ctx;

    private final User user;

    private final NotificationParticipant recipient;

    private NotificationParticipant organizer;

    private List<NotificationParticipant> participants;

    private List<NotificationParticipant> resources;

    public MailAlarmNotificationGenerator(ServiceLookup services, Event event, User user, Context ctx) throws OXException {
        this.services = services;
        this.event = event;
        this.ctx = ctx;
        this.user = user;

        if (null != event.getOrganizer() && Strings.isNotEmpty(event.getOrganizer().getEMail())) {
            this.recipient = new NotificationParticipant(event.getOrganizer().getEMail().equals(user.getMail()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE, false, user.getMail(), user.getId());
        } else {
            this.recipient = new NotificationParticipant(ITipRole.ATTENDEE, false, user.getMail(), user.getId());
        }
        List<NotificationParticipant> lResources = getResources(services, event, ctx, user);
        if (!lResources.isEmpty()) {
            this.resources = lResources;
        }

        List<Attendee> attendees = event.getAttendees();
        if (attendees != null && !attendees.isEmpty()) {
            this.participants = new ArrayList<>();
            for (Attendee attendee : attendees) {
                if (attendee.getCuType() != CalendarUserType.INDIVIDUAL) {
                    continue;
                }
                NotificationParticipant participant = null;
                if (CalendarUtils.isOrganizer(event, attendee.getEntity())) {
                    this.organizer = new NotificationParticipant(ITipRole.ORGANIZER, false, attendee.getEMail(), attendee.getEntity());
                    this.organizer.setConfirmStatus(attendee.getPartStat());
                    participants.add(this.organizer);
                    continue;
                } else if (CalendarUtils.isExternalUser(attendee)) {
                    participant = new NotificationParticipant(ITipRole.ATTENDEE, true, attendee.getEMail());
                } else {
                    participant = new NotificationParticipant(ITipRole.ATTENDEE, false, attendee.getEMail(), attendee.getEntity());
                }
                participant.setConfirmStatus(attendee.getPartStat());
                participants.add(participant);
            }
        }
    }

    public ExtendedNotificationMail create(String templateName) throws OXException {
        ExtendedNotificationMail mail = new ExtendedNotificationMail();
        initMail(mail);
        mail.setTemplateName(templateName);
        render(mail);
        return mail;
    }

    private void initMail(final ExtendedNotificationMail mail) throws OXException {
        mail.setRecipient(recipient);
        mail.setOrganizer(organizer);
        mail.setActor(organizer);
        mail.setEvent(event);
        mail.setParticipants(participants);
        mail.setResources(resources);
        mail.setStateType(State.Type.REMINDER);
        mail.setSubject(generateSubject());
    }

    private String generateSubject() throws OXException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        Locale locale = user.getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Translator translator = translatorFactory.translatorFor(locale);
        String summary = event.getSummary();
        if (summary.length() > 40) {
            summary = summary.substring(0, 36).concat("...");
        }
        DateFormat df = CalendarUtils.isAllDay(event) ? DateFormat.getDateInstance(DateFormat.LONG, user.getLocale()) : DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, user.getLocale());
        String formattedStartDate = df.format(new Date(event.getStartDate().getTimestamp()));

        return translator.translate(MailAlarmMailStrings.REMINDER).concat(": ").concat(summary).concat(" - ").concat(formattedStartDate);
    }

    private void render(final ExtendedNotificationMail mail) throws OXException {
        if (services == null) {
            return;
        }
        final NotificationParticipant participant = mail.getRecipient();

        final TemplateService templateService = services.getService(TemplateService.class);
        if (templateService == null) {
            return;
        }
        final OXTemplate textTemplate = templateService.loadTemplate(mail.getTemplateName() + ".txt.tmpl");
        final OXTemplate htmlTemplate = templateService.loadTemplate(mail.getTemplateName() + ".html.tmpl");

        final Map<String, Object> env = new HashMap<String, Object>();

        TypeWrapper wrapper = new PassthroughWrapper();
        env.put("mail", mail);
        env.put("templating", templateService.createHelper(env, null, false));
        env.put("formatters", dateHelperFor(mail.getRecipient()));
        env.put("participantHelper", new ParticipantHelper(participant.getLocale()));
        env.put("labels", getLabelHelper(mail, wrapper, participant));

        AllocatingStringWriter writer = new AllocatingStringWriter();
        textTemplate.process(env, writer);
        mail.setText(writer.toString());

        wrapper = new HTMLWrapper();
        env.put("labels", getLabelHelper(mail, wrapper, participant));
        writer = new AllocatingStringWriter();
        htmlTemplate.process(env, writer);
        mail.setHtml(writer.toString());

        mail.setEnvironment(env);
    }

    private LabelHelper getLabelHelper(final NotificationMail mail, final TypeWrapper wrapper, final NotificationParticipant participant) throws OXException {
        return new LabelHelper(dateHelperFor(participant), participant.getTimeZone(), mail, participant.getLocale(), ctx, wrapper, services);
    }

    private DateHelper dateHelperFor(final NotificationParticipant participant) {
        return new DateHelper(event, participant.getLocale(), participant.getTimeZone());
    }

    private class PassthroughWrapper implements TypeWrapper {

        @Override
        public String none(final Object argument) {
            if (argument != null) {
                return argument.toString();
            }
            return "";
        }

        @Override
        public String original(final Object argument) {
            return none(argument);
        }

        @Override
        public String participant(final Object argument) {
            return none(argument);
        }

        @Override
        public String state(final Object argument, final ParticipationStatus status) {
            return none(argument);
        }

        @Override
        public String updated(final Object argument) {
            return none(argument);
        }

        @Override
        public String emphasiszed(final Object argument) {
            return none(argument);
        }

        @Override
        public String reference(final Object argument) {
            return none(argument);
        }

        @Override
        public String shownAs(final Object argument, final ShownAsTransparency shownAs) {
            return none(argument);
        }

    }

    private static List<NotificationParticipant> getResources(ServiceLookup services, Event event, Context ctx, User user) throws OXException {
        List<Attendee> resources = CalendarUtils.filter(event.getAttendees(), Boolean.TRUE, CalendarUserType.RESOURCE);
        if (null == resources || resources.isEmpty()) {
            return Collections.emptyList();
        }

        ResourceService resourceService = requireService(ResourceService.class, services);

        final List<NotificationParticipant> resourceParticipants = new ArrayList<NotificationParticipant>(resources.size());
        for (final Attendee attResource : resources) {
            Resource resource = resourceService.getResource(attResource.getEntity(), ctx);
            if (resource.getMail() != null) {
                final NotificationParticipant participant = new NotificationParticipant(ITipRole.ATTENDEE, false, resource.getMail());
                participant.setLocale(user.getLocale());
                participant.setTimezone(TimeZone.getDefault());
                participant.setResource(true);
                participant.setDisplayName(resource.getDisplayName());
                resourceParticipants.add(participant);
            }
        }
        return resourceParticipants;
    }
}
