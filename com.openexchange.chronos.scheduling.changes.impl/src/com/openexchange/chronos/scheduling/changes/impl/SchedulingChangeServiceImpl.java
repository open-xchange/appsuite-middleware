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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.changes.impl;

import static com.openexchange.chronos.scheduling.common.Utils.getLocale;
import static com.openexchange.chronos.scheduling.common.Utils.getTimeZone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.dmfs.rfc5545.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.changes.SchedulingChangeService;
import com.openexchange.chronos.scheduling.changes.Sentence;
import com.openexchange.exception.OXException;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link SchedulingChangeService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class SchedulingChangeServiceImpl implements SchedulingChangeService {

    /*
     * ---------------------------- TEMPLATES ----------------------------
     */
    public final static String ACCEPT = "notify.event.accept";
    public final static String COUNTER_ORGANIZER = "notify.event.counter.organizer";
    public final static String COUNTER_ATTENDEE = "notify.event.counter.attendee";
    public final static String CREATE = "notify.event.create";
    public final static String CREATE_EXCEPTION = "notify.event.createexception";
    public final static String DECLINE = "notify.event.decline";
    public final static String DECLINE_COUNTER = "notify.event.declinecounter";
    public final static String DELETE = "notify.event.delete";
    public final static String NONE = "notify.event.none";
    public final static String REFRESH = "notify.event.refresh";
    public final static String TENTATIVE = "notify.event.tentative";
    public final static String UPDATE = "notify.event.update";
    /*
     * -------------------------------------------------------------------
     */

    protected static final Logger LOGGER = LoggerFactory.getLogger(SchedulingChangeServiceImpl.class);

    protected static final HTMLWrapper HTML_WRAPPER = new HTMLWrapper();
    protected static final PassthroughWrapper TEXT_WRAPPER = new PassthroughWrapper();

    protected final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SchedulingChangeService}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     */
    public SchedulingChangeServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public ScheduleChange describeCancel(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> removedEvents) throws OXException {
        if (null == removedEvents || removedEvents.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Event must not be null");
        }

        return describe(contextId, originator, recipient, comment, DELETE, null, removedEvents, ChangeAction.CANCEL, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public ScheduleChange describeCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> countered, List<Change> changes, boolean isExceptionCreate) throws OXException {
        if (null == countered || countered.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }
        /*
         * Get correct template
         */
        String templateName;
        if (isExceptionCreate) {
            templateName = CREATE_EXCEPTION;
        } else {
            templateName = COUNTER_ATTENDEE;
        }

        return describe(contextId, originator, recipient, comment, templateName, null, countered, ChangeAction.UPDATE, changes);
    }

    @Override
    public ScheduleChange describeDeclineCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> declinedEvent) throws OXException {
        if (null == declinedEvent || declinedEvent.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }
        return describe(contextId, originator, recipient, comment, DECLINE_COUNTER, null, declinedEvent, ChangeAction.CANCEL, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public ScheduleChange describeReply(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> changes, ParticipationStatus partStat) throws OXException {
        /*
         * Check constrains
         */
        if (null == updated || updated.isEmpty()) {
            throw CalendarExceptionCodes.INVALID_DATA.create(EventField.ATTENDEES, "Scheduling resource doesn't contain the relevant update for a REPLY");
        }

        /*
         * Get correct template
         */
        String templateName;
        if (ParticipationStatus.ACCEPTED.matches(partStat)) {
            templateName = ACCEPT;
        } else if (ParticipationStatus.TENTATIVE.matches(partStat)) {
            templateName = TENTATIVE;
        } else if (ParticipationStatus.DECLINED.matches(partStat)) {
            templateName = DECLINE;
        } else {
            // NEEDS_ACTION or unknown
            templateName = NONE;
        }
        return describe(contextId, originator, recipient, comment, templateName, partStat, updated, ChangeAction.REPLY, changes);
    }

    @Override
    public ScheduleChange describeCreationRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> created) throws OXException {
        if (null == created || created.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }
        return describe(contextId, originator, recipient, comment, CREATE, null, created, ChangeAction.CREATE, Collections.emptyList());
    }

    @Override
    public ScheduleChange describeUpdateRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> changes) throws OXException {
        if (null == updated || updated.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }

        return describe(contextId, originator, recipient, comment, UPDATE, null, updated, determineChangeAction(changes), changes);
    }

    @Override
    public ScheduleChange describeNewException(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> changes) throws OXException {
        if (null == updated || updated.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }

        return describe(contextId, originator, recipient, comment, CREATE_EXCEPTION, null, updated, determineChangeAction(changes), changes);
    }

    /*
     * ------------------------ HELPERS ------------------------
     */

    protected UserService getUserService() {
        return serviceLookup.getOptionalService(UserService.class);
    }

    private ChangeAction determineChangeAction(List<Change> changes) {
        /*
         * Check if the update was triggered through a changed participant status (only)
         */
        if (null != changes && changes.size() == 1) {
            Change change = changes.get(0);
            for (Description description : change.getDescriptions()) {
                if (description.getChangedFields().size() == 1 && EventField.ATTENDEES.equals(description.getChangedFields().get(0))) {
                    return ChangeAction.REPLY;
                }
            }
        }
        return ChangeAction.UPDATE;
    }

    private List<Attendee> getAttendees(List<Event> events, CalendarUserType type) {
        List<Attendee> attendees = events.get(0).getAttendees();
        if (null == attendees || attendees.isEmpty()) {
            return Collections.emptyList();
        }
        return attendees.stream().filter(a -> type.matches(a.getCuType())).collect(Collectors.toList());
    }

    private ScheduleChange describe(int contextId, CalendarUser originator, CalendarUser recipient, String comment, String templateName, ParticipationStatus partStat, List<Event> events, ChangeAction action, List<Change> changes) {
        List<Attendee> attendees = new LinkedList<>(getAttendees(events, CalendarUserType.INDIVIDUAL));
        attendees.addAll(getAttendees(events, CalendarUserType.GROUP));
        List<Attendee> resources = getAttendees(events, CalendarUserType.RESOURCE);
        return describe(contextId, originator, recipient, comment, templateName, partStat, events, action, changes, attendees, resources);
    }

    private ScheduleChange describe(int contextId, CalendarUser originator, CalendarUser recipient, String comment, String templateName, ParticipationStatus partStat, List<Event> events, ChangeAction action, List<Change> changes, List<Attendee> attendees, List<Attendee> resources) {
        return new ScheduleChange() {

            private String text = null;
            private String html = null;

            final TimeZone timeZone = getTimeZone(getUserService(), contextId, originator, recipient);
            final Locale locale = getLocale(getUserService(), contextId, originator, recipient);

            @Override
            public DateTime getTimeStamp() {
                return new DateTime(System.currentTimeMillis());
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
            public String getText() {
                return null == text ? (text = render(TEXT_WRAPPER, templateName + ".txt.tmpl")) : text;
            }

            @Override
            public String getHtml() {
                return null == html ? (html = render(HTML_WRAPPER, templateName + ".html.tmpl")) : html;
            }

            private String render(TypeWrapper wrapper, String templateName) {
                TemplateService templateService = serviceLookup.getOptionalService(TemplateService.class);
                if (null == templateService) {
                    return "".intern();
                }

                // XXX Only describe one event to satisfy templates 
                Event event = events.get(0);
                Map<String, Object> env = new HashMap<String, Object>();
                env.put("event", event);
                env.put("templating", templateService.createHelper(env, null, false));
                env.put("formatters", new DateHelper(event, locale, timeZone));
                env.put("labels", new LabelHelper(wrapper, serviceLookup, event, contextId, originator, recipient, comment, locale, timeZone));
                env.put("attendeeHelper", new AttendeeHelper(locale));
                env.put("attendees", attendees);
                env.put("resources", resources);
                env.put("changes", convertToString(changes, event.getRecurrenceId(), wrapper.getType()));

                try (AllocatingStringWriter writer = new AllocatingStringWriter()) {
                    OXTemplate template = templateService.loadTemplate(templateName);
                    template.process(env, writer);
                    return writer.toString();
                } catch (OXException e) {
                    LOGGER.debug("Unable to generate Description with template {}", templateName, e);
                    return "".intern();
                }
            }

            private List<String> convertToString(List<Change> changes, RecurrenceId recurrenceId, String format) {
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
                                descriptions.add(sentence.getMessage(format, locale));
                            }
                        }
                    }
                }
                return descriptions;
            }
        };
    }
}
