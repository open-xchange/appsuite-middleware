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
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.generators.ITipMailGenerator;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.tools.ITipUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
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
     * Contains the three fields that are updated if a series is updated
     */
    private final static EventField[] SERIES_UPDATE = new EventField[] { EventField.SEQUENCE };

    /**
     * Contains the three fields that are updated if a master event has a new change/delete exception
     */
    private final static EventField[] MASTER_EXCEPTION_UPDATE = new EventField[] { EventField.CHANGE_EXCEPTION_DATES };

    private NotificationMailGeneratorFactory generators;
    private MailSenderService                sender;

    public ITipHandler(NotificationMailGeneratorFactory generatorFactory, MailSenderService sender) {
        this.generators = generatorFactory;
        this.sender = sender;
    }

    @Override
    public void handle(CalendarEvent event) {
        boolean suppress = event.getCalendarParameters() != null && event.getCalendarParameters().contains(CalendarParameters.PARAMETER_SUPPRESS_ITIP) && event.getCalendarParameters().get(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.class).booleanValue();
        if (suppress) {
            return;
        }
        if (event.getAccountId() != CalendarAccount.DEFAULT_ACCOUNT.getAccountId()) {
            return;
        }

        try {
            List<CreateResult> creations = event.getCreations();
            if (creations != null && creations.size() > 0) {
                for (CreateResult create : creations) {
                    handleCreate(create, event);
                }
            }

            List<UpdateResult> updates = new LinkedList<>(event.getUpdates());
            if (updates.size() > 0) {
                for (UpdateResult update : updates) {
                    if (false == isMasterExceptionUpdate(update, creations)) {
                        handleUpdate(update, updates, event);
                    }
                }
            }

            List<DeleteResult> deletions = new LinkedList<>(event.getDeletions());
            if (deletions.size() > 0) {
                for (DeleteResult delete : deletions) {
                    handleDelete(delete, deletions, event);
                }
            }
        } catch (OXException oe) {
            LOG.error("Unable to handle CalendarEvent", oe);
        }
    }

    private void handleCreate(CreateResult create, CalendarEvent event) throws OXException {
        handle(event, State.Type.NEW, null, create.getCreatedEvent(), null);
    }

    private void handleUpdate(UpdateResult update, List<UpdateResult> updates, CalendarEvent event) throws OXException {
        List<UpdateResult> exceptions = Collections.emptyList();

        // Check for series update
        if (update.getUpdate().containsSeriesId()) {
            // Get all events of the series
            String seriesId = update.getUpdate().getSeriesId();
            List<UpdateResult> eventGroup = updates.stream().filter(u -> seriesId.equals(u.getUpdate().getSeriesId())).collect(Collectors.toList());

            // Check if there is a group to handle
            if (eventGroup.size() > 1) {
                // Check if master is present and if every update is a series update
                Optional<UpdateResult> master = eventGroup.stream().filter(u -> seriesId.equals(u.getUpdate().getId())).findFirst();
                if (master.isPresent() && CalendarUtils.isSeriesMaster(master.get().getUpdate())) {
                    UpdateResult masterUpdate = master.get();
                    if (eventGroup.stream().filter(u -> u.containsAnyChangeOf(SERIES_UPDATE)).collect(Collectors.toList()).size() == eventGroup.size()) {
                        // Series update, remove those items from the update list and the master from the exceptions
                        updates.removeAll(eventGroup);
                        eventGroup.remove(masterUpdate);

                        // Set for processing
                        update = masterUpdate;
                        exceptions = eventGroup;
                    } else {
                        Set<EventField> fields = masterUpdate.getUpdatedFields();
                        fields.remove(EventField.TIMESTAMP);
                        fields.remove(EventField.LAST_MODIFIED);
                        if(fields.isEmpty()) {
                            // Exception update, no update on master
                            updates.remove(masterUpdate);
                        }
                    }
                }
            }
        }

        // Handle update
        handle(event, State.Type.MODIFIED, update.getOriginal(), update.getUpdate(), exceptions.stream().map(UpdateResult::getUpdate).collect(Collectors.toList()));
    }

    private void handleDelete(DeleteResult delete, List<DeleteResult> deletions, CalendarEvent event) throws OXException {
        List<DeleteResult> exceptions = Collections.emptyList();

        // Check for series update
        if (delete.getOriginal().containsSeriesId()) {
            // Get all events of the series
            String seriesId = delete.getOriginal().getSeriesId();
            List<DeleteResult> eventGroup = deletions.stream().filter(u -> seriesId.equals(u.getOriginal().getSeriesId())).collect(Collectors.toList());

            // Check if there is a group to handle
            if (eventGroup.size() > 1) {
                // Check if master is present 
                Optional<DeleteResult> master = eventGroup.stream().filter(u -> seriesId.equals(u.getOriginal().getId())).findFirst();
                if (master.isPresent() && CalendarUtils.isSeriesMaster(master.get().getOriginal())) {
                    // Series update, remove those items from the update list and the master from the exceptions
                    deletions.removeAll(eventGroup);
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

        ITipMailGenerator generator = generators.create(original, update, session, onBehalfOf, principal);
        List<NotificationParticipant> recipients = generator.getRecipients();
        for (NotificationParticipant notificationParticipant : recipients) {
            NotificationMail mail;
            switch (type) {
                case NEW:
                    mail = generator.generateCreateMailFor(notificationParticipant);
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
                if (null != exceptions) {
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

    /**
     * Checks if the given {@link UpdateResult} is an update on a series master that has a new
     * event exception and checks if such a exception was created
     * 
     * @param update The {@link UpdateResult} an potentially master event
     * @param creations The {@link List} of {@link CreateResult}s
     * @return <code>true</code> if the series master got only an update on its exceptions, <code>false</code> otherwise
     */
    private boolean isMasterExceptionUpdate(UpdateResult update, List<CreateResult> creations) {
        return CalendarUtils.isSeriesMaster(update.getUpdate()) && update.containsAnyChangeOf(MASTER_EXCEPTION_UPDATE) && creations.stream().filter(c -> update.getUpdate().getId().equals(c.getCreatedEvent().getSeriesId())).findAny().isPresent();
    }
}
