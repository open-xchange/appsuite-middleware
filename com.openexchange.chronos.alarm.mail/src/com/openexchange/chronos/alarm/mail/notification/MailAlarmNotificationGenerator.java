/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.chronos.alarm.mail.impl.MailAlarmMailStrings;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.generators.DateHelper;
import com.openexchange.chronos.itip.generators.LabelHelper;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.generators.ParticipantHelper;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.provider.composition.IDMangling;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.State;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Strings;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.User;

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

    public MailAlarmNotificationGenerator(ServiceLookup services, Event event, User user, Context ctx, int accountId) throws OXException {
        this.services = services;
        this.event = event;
        this.ctx = ctx;
        this.user = user;

        if (null != event.getOrganizer() && Strings.isNotEmpty(event.getOrganizer().getEMail())) {
            this.recipient = new NotificationParticipant(event.getOrganizer().getEMail().equals(user.getMail()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE, false, user.getMail(), user.getId());
        } else {
            this.recipient = new NotificationParticipant(ITipRole.ATTENDEE, false, user.getMail(), user.getId());
        }
        recipient.setFolderId(IDMangling.getUniqueFolderId(accountId, event.getFolderId()));
        recipient.setTimezone(TimeZone.getTimeZone(user.getTimeZone()));
        recipient.setLocale(user.getLocale());
        recipient.setUser(user);
        recipient.setContext(ctx);
        recipient.setDisplayName(user.getDisplayName());

        List<NotificationParticipant> lResources = getResources(services, event, ctx, user);
        if (!lResources.isEmpty()) {
            this.resources = lResources;
        }

        List<Attendee> attendees = CalendarUtils.filter(event.getAttendees(), null, CalendarUserType.INDIVIDUAL);
        if (attendees != null && !attendees.isEmpty()) {
            this.participants = new ArrayList<>();
            for (Attendee attendee : attendees) {
                NotificationParticipant participant = null;
                String eMail = extractEMailAddress(attendee);
                if (CalendarUtils.isOrganizer(event, attendee.getEntity())) {
                    this.organizer = new NotificationParticipant(ITipRole.ORGANIZER, false, eMail, attendee.getEntity());
                    this.organizer.setConfirmStatus(attendee.getPartStat());
                    participants.add(this.organizer);
                    continue;
                } else if (CalendarUtils.isExternalUser(attendee)) {
                    participant = new NotificationParticipant(ITipRole.ATTENDEE, true, eMail);
                } else {
                    participant = new NotificationParticipant(ITipRole.ATTENDEE, false, eMail, attendee.getEntity());
                }
                participant.setConfirmStatus(attendee.getPartStat());
                participants.add(participant);
            }
        }
    }

    private String extractEMailAddress(Attendee attendee) {
        if (Strings.isEmpty(attendee.getEMail())) {
            return CalendarUtils.extractEMailAddress(attendee.getUri());
        }
        return attendee.getEMail();
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
        RegionalSettingsService regionalSettingsService = requireService(RegionalSettingsService.class, services);
        Locale locale = user.getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Translator translator = translatorFactory.translatorFor(locale);
        String summary = event.getSummary();
        if (summary.length() > 40) {
            summary = summary.substring(0, 36).concat("...");
        }
        DateFormat df;
        if (null == regionalSettingsService) {
            df = CalendarUtils.isAllDay(event) ? DateFormat.getDateInstance(DateFormat.LONG, locale) : DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
        } else {
            df = CalendarUtils.isAllDay(event) ? regionalSettingsService.getDateFormat(ctx.getContextId(), user.getId(), locale, DateFormat.LONG) :
                                                 regionalSettingsService.getDateTimeFormat(ctx.getContextId(), user.getId(), locale, DateFormat.LONG, DateFormat.SHORT);
        }
        df.setTimeZone(TimeZone.getTimeZone(CalendarUtils.isFloating(event) ? "UTC" : user.getTimeZone()));
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

        TypeWrapper wrapper = TypeWrapper.WRAPPER.get("text");
        env.put("mail", mail);
        env.put("templating", templateService.createHelper(env, null, false));
        env.put("formatters", dateHelperFor(mail.getRecipient()));
        env.put("participantHelper", new ParticipantHelper(participant.getLocale()));
        env.put("labels", getLabelHelper(mail, wrapper, participant));

        AllocatingStringWriter writer = new AllocatingStringWriter();
        textTemplate.process(env, writer);
        mail.setText(writer.toString());

        wrapper = TypeWrapper.WRAPPER.get("html");
        env.put("labels", getLabelHelper(mail, wrapper, participant));
        writer = new AllocatingStringWriter();
        htmlTemplate.process(env, writer);
        mail.setHtml(writer.toString());

        mail.setEnvironment(env);
    }

    private LabelHelper getLabelHelper(final NotificationMail mail, final TypeWrapper wrapper, final NotificationParticipant participant) throws OXException {
        return new LabelHelper(dateHelperFor(participant), participant.getTimeZone(), mail, participant.getLocale(), ctx, wrapper, services);
    }

    private DateHelper dateHelperFor(final NotificationParticipant participant) throws OXException {
        RegionalSettingsService regionalSettingsService = requireService(RegionalSettingsService.class, services);
        RegionalSettings regionalSettings = regionalSettingsService.get(participant.getContext().getContextId(), participant.getUser().getId());
        return new DateHelper(event, participant.getLocale(), participant.getTimeZone(), regionalSettings);
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
