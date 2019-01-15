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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.scheduling.changes.impl.desc.AttachmentDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.AttendeeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.DescriptionDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.LocationDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.OrganizerDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.RRuleDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.ReschedulingDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.SummaryDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.TransperencyDescriber;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link DescriptionService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class DescriptionServiceImpl implements DescriptionService {

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
    // XXX Unused, at the moment
    public final static String REFRESH = "notify.event.refresh";
    public final static String TENTATIVE = "notify.event.tentative";
    public final static String UPDATE = "notify.event.update";
    /*
     * -------------------------------------------------------------------
     */

    protected static final Logger LOGGER = LoggerFactory.getLogger(DescriptionServiceImpl.class);

    protected static final HTMLWrapper HTML_WRAPPER = new HTMLWrapper();
    protected static final PassthroughWrapper TEXT_WRAPPER = new PassthroughWrapper();

    private static final AttendeeDescriber ATTENDEE_DESCRIBER = new AttendeeDescriber();

    //@formatter:off
    private static final ImmutableSet<ChangeDescriber> DESCRIPTIONS = ImmutableSet.of(
        ATTENDEE_DESCRIBER,
        new DescriptionDescriber(), 
        new LocationDescriber(),
        new OrganizerDescriber(),
        new ReschedulingDescriber(),
        new SummaryDescriber(),
        new TransperencyDescriber(),
        new AttachmentDescriber(),
        new RRuleDescriber()
        );
    //@formatter:on

    protected final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link DescriptionService}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     */
    public DescriptionServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public Description describeCancel(int contextId, CalendarUser originator, CalendarUser recipient, String comment, Event removedEvent) throws OXException {
        if (null == removedEvent) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Event must not be null");
        }

        return describe(contextId, originator, recipient, comment, DELETE, removedEvent, ChangeAction.CANCEL, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public Description describeCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate, boolean isExceptionCreate) throws OXException {
        if (null == eventUpdate || eventUpdate.isEmpty()) {
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

        TimeZone timeZone = getTimeZone(getUserService(), contextId, originator, recipient);
        Locale locale = getLocale(getUserService(), contextId, originator, recipient);
        return describe(contextId, originator, recipient, comment, templateName, eventUpdate.getUpdate(), ChangeAction.UPDATE, describeChanges(eventUpdate, timeZone, locale));
    }

    @Override
    public Description describeDeclineCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, Event declinedEvent) throws OXException {
        return describe(contextId, originator, recipient, comment, DECLINE_COUNTER, declinedEvent, ChangeAction.CANCEL, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public Description describeReply(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
        /*
         * Check constrains
         */
        if (null == eventUpdate || eventUpdate.isEmpty() || null == eventUpdate.getAttendeeUpdates()) {
            throw CalendarExceptionCodes.INVALID_DATA.create(EventField.ATTENDEES, "Scheduling resource doesn't contain the relevant update for a REPLY");
        }
        CollectionUpdate<Attendee, AttendeeField> attendeeUpdate = eventUpdate.getAttendeeUpdates();
        if (null == attendeeUpdate.getUpdatedItems() || attendeeUpdate.getUpdatedItems().size() != 1) {
            throw CalendarExceptionCodes.INVALID_DATA.create(EventField.ATTENDEES, "Attendee status didn't change");
        }

        /*
         * Get correct template
         */
        String templateName;
        ParticipationStatus partStat = attendeeUpdate.getUpdatedItems().get(0).getUpdate().getPartStat();
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

        /*
         * Describe only attendee change
         */
        TimeZone timeZone = getTimeZone(getUserService(), contextId, originator, recipient);
        Locale locale = getLocale(getUserService(), contextId, originator, recipient);
        return describe(contextId, originator, recipient, comment, templateName, eventUpdate.getUpdate(), ChangeAction.REPLY, Collections.singletonList(ATTENDEE_DESCRIBER.describe(eventUpdate, timeZone, locale)));
    }

    @Override
    public Description describeCreationRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, Event created) throws OXException {
        return describe(contextId, originator, recipient, comment, CREATE, created, ChangeAction.CREATE, Collections.emptyList());
    }

    @Override
    public Description describeUpdateRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
        if (null == eventUpdate || eventUpdate.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }

        TimeZone timeZone = getTimeZone(getUserService(), contextId, originator, recipient);
        Locale locale = getLocale(getUserService(), contextId, originator, recipient);
        List<Change> changes = describeChanges(eventUpdate, timeZone, locale);
        return describe(contextId, originator, recipient, comment, UPDATE, eventUpdate.getUpdate(), determineChangeAction(changes), changes);
    }

    @Override
    public Description describeNewException(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
        if (null == eventUpdate || eventUpdate.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }

        TimeZone timeZone = getTimeZone(getUserService(), contextId, originator, recipient);
        Locale locale = getLocale(getUserService(), contextId, originator, recipient);
        List<Change> changes = describeChanges(eventUpdate, timeZone, locale);
        return describe(contextId, originator, recipient, comment, CREATE_EXCEPTION, eventUpdate.getUpdate(), determineChangeAction(changes), changes);
    }

    @Override
    public Description describeUpdateAfterSplit(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
        if (null == eventUpdate || eventUpdate.isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No changes to propagate");
        }

        TimeZone timeZone = getTimeZone(getUserService(), contextId, originator, recipient);
        Locale locale = getLocale(getUserService(), contextId, originator, recipient);
        // Attendees participant status will be reset, so avoid describing
        List<Change> changes = describeChanges(eventUpdate, timeZone, locale, EventField.ATTENDEES);
        return describe(contextId, originator, recipient, comment, UPDATE, eventUpdate.getUpdate(), ChangeAction.UPDATE, changes);
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
            if (change.getDescribedFields().size() == 1 && EventField.ATTENDEES.equals(change.getDescribedFields().get(0))) {
                return ChangeAction.REPLY;
            }
        }
        return ChangeAction.UPDATE;
    }

    private List<Attendee> getAttendees(Event update, CalendarUserType type) {
        List<Attendee> attendees = update.getAttendees();
        if (null == attendees || attendees.isEmpty()) {
            return Collections.emptyList();
        }
        return attendees.stream().filter(a -> type.matches(a.getCuType())).collect(Collectors.toList());
    }

    private List<Change> describeChanges(EventUpdate eventUpdate, TimeZone timeZone, Locale locale, EventField... ignorees) {
        List<Change> descriptions = new LinkedList<>();
        for (ChangeDescriber describer : DESCRIPTIONS) {
            if (false == isIgnored(describer.getFields(), ignorees) && eventUpdate.containsAnyChangeOf(describer.getFields())) {
                descriptions.add(describer.describe(eventUpdate, timeZone, locale));
            }
        }
        return descriptions;
    }

    private boolean isIgnored(EventField[] describedFields, EventField... ignorees) {
        for (EventField f : describedFields) {
            for (EventField i : ignorees) {
                if (f.equals(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Description describe(int contextId, CalendarUser originator, CalendarUser recipient, String comment, String templateName, Event event, ChangeAction action, List<Change> changes) {
        List<Attendee> attendees = new LinkedList<>(getAttendees(event, CalendarUserType.INDIVIDUAL));
        attendees.addAll(getAttendees(event, CalendarUserType.GROUP));
        List<Attendee> resources = getAttendees(event, CalendarUserType.RESOURCE);
        return describe(contextId, originator, recipient, comment, templateName, event, action, changes, attendees, resources);
    }

    private Description describe(int contextId, CalendarUser originator, CalendarUser recipient, String comment, String templateName, Event event, ChangeAction action, List<Change> changes, List<Attendee> attendees, List<Attendee> resources) {
        return new Description() {

            private String text = null;
            private String html = null;

            final TimeZone timeZone = getTimeZone(getUserService(), contextId, originator, recipient);
            final Locale locale = getLocale(getUserService(), contextId, originator, recipient);

            @Override
            public ChangeAction getAction() {
                return action;
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
                Map<String, Object> env = new HashMap<String, Object>();
                env.put("event", event);
                env.put("templating", templateService.createHelper(env, null, false));
                env.put("formatters", new DateHelper(event, locale, timeZone));
                env.put("labels", new LabelHelper(wrapper, serviceLookup, event, contextId, originator, recipient, comment, locale, timeZone));
                env.put("attendeeHelper", new AttendeeHelper(locale));
                env.put("attendees", attendees);
                env.put("resources", resources);
                env.put("changes", convertToString(changes, wrapper));

                try (AllocatingStringWriter writer = new AllocatingStringWriter()) {
                    OXTemplate template = templateService.loadTemplate(templateName);
                    template.process(env, writer);
                    return writer.toString();
                } catch (OXException e) {
                    LOGGER.debug("Unable to generate description with template {}", templateName, e);
                    return "".intern();
                }
            }

            private List<String> convertToString(List<Change> changes, TypeWrapper wrapper) {
                if (null == changes || changes.isEmpty()) {
                    return Collections.emptyList();
                }
                List<String> descriptions = new ArrayList<>(changes.size());
                for (Change change : changes) {
                    for (Sentence description : change.getSentences()) {
                        descriptions.add(description.getMessage(wrapper, locale));
                    }
                }
                return descriptions;
            }
        };
    }
}
