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

package com.openexchange.chronos.scheduling.changes.impl;

import static com.openexchange.chronos.common.CalendarUtils.filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.itip.generators.DateHelper;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.changes.Sentence;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.exception.OXException;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;

/**
 * {@link ScheduleChangeImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class ScheduleChangeImpl implements ScheduleChange {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleChangeImpl.class);

    private final Date timestamp = new Date();
    private final ServiceLookup services;
    private final CalendarUser originator;
    private final String comment;
    private final String templateName;
    private final ParticipationStatus partStat;
    private final CalendarObjectResource resource;
    private final Event seriesMaster;
    private final ChangeAction action;
    private final List<Change> changes;

    /**
     * Initializes a new {@link ScheduleChangeImpl}.
     *
     * @param services A service lookup reference
     * @param originator The originator of the schedule change
     * @param comment An optional comment set by the originator
     * @param templateName The template name to use for rendering
     * @param partStat The participations status of the originator
     * @param resource The underlying calendar object resource
     * @param seriesMaster The series master event if changes affect a recurrence instance, <code>null</code>, otherwise
     * @param action The change action
     * @param changes The changes
     */
    public ScheduleChangeImpl(ServiceLookup services, CalendarUser originator, String comment, String templateName, ParticipationStatus partStat, CalendarObjectResource resource, Event seriesMaster, ChangeAction action, List<Change> changes) {
        super();
        this.services = services;
        this.originator = originator;
        this.comment = comment;
        this.templateName = templateName;
        this.partStat = partStat;
        this.resource = resource;
        this.seriesMaster = seriesMaster;
        this.action = action;
        this.changes = changes;
    }

    @Override
    public Date getTimeStamp() {
        return timestamp;
    }

    @Override
    public List<Change> getChanges() {
        return changes;
    }

    @Override
    public ChangeAction getAction() {
        return null == action ? ChangeAction.NONE : action;
    }

    @Override
    @Nullable
    public ParticipationStatus getOriginatorPartStat() {
        return partStat;
    }

    @Override
    public String getText(RecipientSettings recipientSettings) {
        return render(TypeWrapper.WRAPPER.get("text"), templateName + ".txt.tmpl", recipientSettings);
    }

    @Override
    public String getHtml(RecipientSettings recipientSettings) {
        return render(TypeWrapper.WRAPPER.get("html"), templateName + ".html.tmpl", recipientSettings);
    }

    private String render(TypeWrapper wrapper, String templateName, RecipientSettings recipientSettings) {
        TemplateService templateService = services.getOptionalService(TemplateService.class);
        if (null == templateService) {
            return "";
        }

        MessageContext messageContext = new DefaultMessageContext(wrapper, recipientSettings);

        // XXX Only describe one event to satisfy templates
        Event event = Utils.selectDescribedEvent(resource, changes);
        List<Attendee> participants;
        List<Attendee> resources;
        if (false == ChangeAction.CANCEL.equals(action)) {
            participants = filter(event.getAttendees(), null, CalendarUserType.INDIVIDUAL, CalendarUserType.GROUP);
            resources = filter(event.getAttendees(), null, CalendarUserType.RESOURCE);
        } else {
            participants = resources = Collections.emptyList();
        }
        participants = ChangesUtils.sortAttendees(participants);
        resources = ChangesUtils.sortAttendees(resources, CalendarUserType.RESOURCE);

        Map<String, Object> env = new HashMap<String, Object>();
        env.put("mail", new NotificationMail(event, participants, resources, event.getConferences()));
        env.put("templating", templateService.createHelper(env, null, false));
        env.put("formatters", new DateHelper(event, recipientSettings.getLocale(), recipientSettings.getTimeZone(), recipientSettings.getRegionalSettings()));
        env.put("labels", new LabelHelper(services, event, seriesMaster, originator, recipientSettings, comment, messageContext));
        env.put("participantHelper", new ParticipantHelper(recipientSettings.getLocale()));
        env.put("changes", convertToString(messageContext, changes, event.getRecurrenceId()));

        try (AllocatingStringWriter writer = new AllocatingStringWriter()) {
            OXTemplate template = templateService.loadTemplate(templateName);
            template.process(env, writer);
            return writer.toString();
        } catch (OXException e) {
            LOG.warn("Error rendering schedule change using template '{}'", templateName, e);
            return "";
        }
    }

    private List<String> convertToString(MessageContext messageContext, List<Change> changes, RecurrenceId recurrenceId) {
        if (null == changes || changes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> descriptions = new ArrayList<>(changes.size());
        for (Change change : changes) {
            /*
             * XXX Only describe one event to satisfy templates
             */
            if (null == recurrenceId && null == change.getRecurrenceId() || null != recurrenceId && 0 == recurrenceId.compareTo(change.getRecurrenceId())) {
                for (Description description : change.getDescriptions()) {
                    for (Sentence sentence : description.getSentences()) {
                        descriptions.add(sentence.getMessage(messageContext.getWrapper().getFormat(), messageContext.getLocale(), messageContext.getTimeZone(), messageContext.getRegionalSettings()));
                    }
                }
            }
        }
        return descriptions;
    }

}
