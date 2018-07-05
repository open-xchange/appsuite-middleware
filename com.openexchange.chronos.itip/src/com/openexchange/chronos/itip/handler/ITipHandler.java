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

package com.openexchange.chronos.itip.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.generators.ITipMailGenerator;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.tools.ITipUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.State;
import com.openexchange.session.Session;

/**
 * {@link ITipHandler}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ITipHandler implements CalendarHandler {

    private final static Logger LOG = LoggerFactory.getLogger(ITipHandler.class);

    /**
     * Contains the fields that are updated if a series is updated
     */
    private final static EventField[] SERIES_UPDATE = new EventField[] { EventField.SEQUENCE };

    /**
     * Contains the fields that are updated if a master event has a new change exception
     */
    private final static EventField[] MASTER_EXCEPTION_UPDATE = new EventField[] { EventField.CHANGE_EXCEPTION_DATES };

    /**
     * Contains the fields that are updated if a master event has a new delete exception
     */
    private final static EventField[] EXCEPTION_DELETE = new EventField[] { EventField.DELETE_EXCEPTION_DATES };

    private NotificationMailGeneratorFactory generators;
    private MailSenderService                sender;

    public ITipHandler(NotificationMailGeneratorFactory generatorFactory, MailSenderService sender) {
        this.generators = generatorFactory;
        this.sender = sender;
    }

    @Override
    public void handle(CalendarEvent event) {
        if (!shouldHandle(event)) {
            return;
        }

        try {
            List<CreateResult> creations = new LinkedList<>(event.getCreations());
            List<UpdateResult> updates = new LinkedList<>(event.getUpdates());
            List<DeleteResult> deletions = new LinkedList<>(event.getDeletions());

            if (creations.size() > 0) {
                for (CreateResult create : creations) {
                    handleCreate(create, creations, updates, event);
                }
            }

            if (updates.size() > 0) {
                Set<UpdateResult> ignore = new HashSet<>();
                for (UpdateResult update : updates) {
                    if (ignore.contains(update)) {
                        continue;
                    }
                    handleUpdate(update, updates, ignore, event);
                }
            }

            if (deletions.size() > 0) {
                Set<DeleteResult> ignore = new HashSet<>();
                for (DeleteResult delete : deletions) {
                    if (ignore.contains(delete)) {
                        continue;
                    }
                    handleDelete(delete, deletions, ignore, event);
                }
            }
        } catch (OXException oe) {
            LOG.error("Unable to handle CalendarEvent", oe);
        }
    }

    protected boolean shouldHandle(CalendarEvent event) {
        if (event == null || event.getAccountId() != CalendarAccount.DEFAULT_ACCOUNT.getAccountId()) {
            return false;
        }

        if (event.getCalendarParameters() != null) {

            if (event.getCalendarParameters().contains(CalendarParameters.PARAMETER_SUPPRESS_ITIP)) {
                Boolean suppress = event.getCalendarParameters().get(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.class);
                if (suppress != null && suppress.booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    private void handleCreate(CreateResult create, List<CreateResult> creations, List<UpdateResult> updates, CalendarEvent event) throws OXException {
        UpdateResult master = getExceptionMaster(create, updates);
        if (null != master) {
            // Gather other creations to this series
            List<CreateResult> group = creations.stream().filter(c -> master.getUpdate().getId().equals(c.getCreatedEvent().getSeriesId())).collect(Collectors.toList());
            // Handle as update
            for (CreateResult c : group) {
                handle(event, State.Type.NEW, master.getOriginal(), c.getCreatedEvent(), null);
            }
            // Remove master to avoid additional mail
            updates.remove(master);
        } else {
            handle(event, State.Type.NEW, null, create.getCreatedEvent(), null);
        }
    }

    private void handleUpdate(UpdateResult update, List<UpdateResult> updates, Set<UpdateResult> ignore, CalendarEvent event) throws OXException {
        List<UpdateResult> exceptions = Collections.emptyList();

        if (CalendarUtils.isSeriesMaster(update.getUpdate()) && update.containsAnyChangeOf(EXCEPTION_DELETE)) {
            // Handle as delete 
            RecurrenceService service = Services.getService(RecurrenceService.class, true);
            RecurrenceIterator<Event> recurrenceIterator = service.iterateEventOccurrences(update.getOriginal(), null, null);

            // Get delete exceptions
            List<RecurrenceId> newDeletions = new LinkedList<>(update.getUpdate().getDeleteExceptionDates());
            if (null != update.getOriginal().getDeleteExceptionDates() && false == update.getOriginal().getDeleteExceptionDates().isEmpty()) {
                newDeletions.removeAll(update.getOriginal().getDeleteExceptionDates());
            }

            // Iterate over all occurrences until all new deletions are consumed
            while (recurrenceIterator.hasNext() && false == newDeletions.isEmpty()) {
                Event next = recurrenceIterator.next();
                Iterator<RecurrenceId> iterator = newDeletions.iterator();
                Inner: while (iterator.hasNext()) {
                    RecurrenceId recurrenceId = iterator.next();
                    if (next.getRecurrenceId().equals(recurrenceId)) {
                        handle(event, State.Type.DELETED, null, EventMapper.getInstance().copy(next, new Event(), (EventField[]) null), null);
                        // Consume element from above list
                        iterator.remove();
                        break Inner;
                    }
                }
            }
            return;
        } else if (update.getUpdate().containsSeriesId()) {
            // Check for series update
            // Get all events of the series
            String seriesId = update.getUpdate().getSeriesId();
            List<UpdateResult> eventGroup = updates.stream()
                .filter(u -> !ignore.contains(u))
                .filter(u -> seriesId.equals(u.getUpdate().getSeriesId()))
                .collect(Collectors.toList());

            // Check if there is a group to handle
            if (eventGroup.size() > 1) {
                // Check if master is present and if every update is a series update
                Optional<UpdateResult> master = eventGroup.stream().filter(u -> seriesId.equals(u.getUpdate().getId())).findFirst();
                if (master.isPresent() && CalendarUtils.isSeriesMaster(master.get().getUpdate())) {
                    UpdateResult masterUpdate = master.get();
                    if (eventGroup.stream().filter(u -> u.containsAnyChangeOf(SERIES_UPDATE)).collect(Collectors.toList()).size() == eventGroup.size()) {
                        // Series update, remove those items from the update list and the master from the exceptions
                        ignore.addAll(eventGroup);
                        eventGroup.remove(masterUpdate);

                        // Set for processing
                        update = masterUpdate;
                        exceptions = eventGroup;
                    } else {
                        Set<EventField> fields = new HashSet<>(masterUpdate.getUpdatedFields());
                        fields.remove(EventField.TIMESTAMP);
                        fields.remove(EventField.LAST_MODIFIED);
                        if (fields.isEmpty()) {
                            // Exception update, no update on master
                            ignore.add(masterUpdate);
                        }
                    }
                }
            }
        }

        // Handle update
        handle(event, State.Type.MODIFIED, update.getOriginal(), update.getUpdate(), exceptions.stream().map(UpdateResult::getUpdate).collect(Collectors.toList()));
    }

    private void handleDelete(DeleteResult delete, List<DeleteResult> deletions, Set<DeleteResult> ignore, CalendarEvent event) throws OXException {
        List<DeleteResult> exceptions = Collections.emptyList();

        // Check for series update
        if (delete.getOriginal().containsSeriesId()) {
            // Get all events of the series
            String seriesId = delete.getOriginal().getSeriesId();
            List<DeleteResult> eventGroup = deletions.stream()
                .filter(u -> !ignore.contains(u))
                .filter(u -> seriesId.equals(u.getOriginal().getSeriesId()))
                .collect(Collectors.toList());

            // Check if there is a group to handle
            if (eventGroup.size() > 1) {
                // Check if master is present 
                Optional<DeleteResult> master = eventGroup.stream().filter(u -> seriesId.equals(u.getOriginal().getId())).findFirst();
                if (master.isPresent() && CalendarUtils.isSeriesMaster(master.get().getOriginal())) {
                    // Series update, remove those items from the update list and the master from the exceptions
                    ignore.addAll(eventGroup);
                    eventGroup.remove(master.get());

                    // Set for processing
                    delete = master.get();
                    exceptions = eventGroup;
                }
            }
        }

        handle(event, State.Type.DELETED, null, delete.getOriginal(), exceptions.stream().map(DeleteResult::getOriginal).collect(Collectors.toList()));
    }

    private void handle(CalendarEvent event, State.Type type, Event original, Event update, List<Event> exceptions) throws OXException {
        Session session = event.getSession();
        int onBehalfOf = onBehalfOf(event.getCalendarUser(), session);
        CalendarUser principal = ITipUtils.getPrincipal(event.getCalendarParameters());

        // Copy event to avoid UOE due UnmodifieableEvent
        if (null != original) {
            original = EventMapper.getInstance().copy(original, new Event(), (EventField[]) null);
        }
        ITipMailGenerator generator = generators.create(original, EventMapper.getInstance().copy(update, new Event(), (EventField[]) null), session, onBehalfOf, principal);
        List<NotificationParticipant> recipients = generator.getRecipients();
        for (NotificationParticipant notificationParticipant : recipients) {
            NotificationMail mail;
            switch (type) {
                case NEW:
                    if (CalendarUtils.isSeriesMaster(original) && CalendarUtils.isSeriesException(update)) {
                        mail = generator.generateCreateExceptionMailFor(notificationParticipant);
                    } else {
                        mail = generator.generateCreateMailFor(notificationParticipant);
                    }
                    break;
                case MODIFIED:
                    mail = generator.generateUpdateMailFor(notificationParticipant);
                    break;
                case DELETED:
                    mail = generator.generateDeleteMailFor(notificationParticipant);
                    break;
                default:
                    mail = null;
            }
            if (mail != null) {
                if (mail.getStateType() == null) {
                    mail.setStateType(type);
                }
                if (null != exceptions && null != mail.getMessage()) {
                    // Set exceptions
                    for (Event exception : exceptions) {
                        mail.getMessage().addException(exception);
                    }
                }
                sender.sendMail(mail, session, principal);
            }
        }
    }

    private int onBehalfOf(int calendarUser, Session session) {
        return calendarUser == session.getUserId() ? -1 : calendarUser;
    }

    private UpdateResult getExceptionMaster(CreateResult create, List<UpdateResult> updates) {
        if (CalendarUtils.isSeriesException(create.getCreatedEvent())) {
            return updates.stream().filter(u -> CalendarUtils.isSeriesMaster(u.getUpdate()) && create.getCreatedEvent().getSeriesId().equals(u.getUpdate().getId()) && u.containsAnyChangeOf(MASTER_EXCEPTION_UPDATE)).findAny().orElse(null);
        }
        return null;
    }
}
