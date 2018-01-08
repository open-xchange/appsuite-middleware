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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.CalendarUser;
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
 * @since v7.10.0
 */
public class ITipHandler implements CalendarHandler {

    Logger LOG = LoggerFactory.getLogger(ITipHandler.class);

    private NotificationMailGeneratorFactory generators;
    private MailSenderService sender;

    public ITipHandler(NotificationMailGeneratorFactory generatorFactory, MailSenderService sender) {
        this.generators = generatorFactory;
        this.sender = sender;
    }

    @Override
    public void handle(CalendarEvent event) {
        Boolean suppress = event.getCalendarParameters() != null && event.getCalendarParameters().contains(CalendarParameters.PARAMETER_SUPPRESS_ITIP) && event.getCalendarParameters().get(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.class);
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

            List<UpdateResult> updates = event.getUpdates();
            if (updates != null && updates.size() > 0) {
                for (UpdateResult update : updates) {
                    handleUpdate(update, event);
                }
            }

            List<DeleteResult> deletions = event.getDeletions();
            if (deletions != null && deletions.size() > 0) {
                for (DeleteResult delete : deletions) {
                    handleDelete(delete, event);
                }
            }
        } catch (OXException oe) {
            LOG.error("Unable to handle CalendarEvent", oe);
        }
    }

    private void handleCreate(CreateResult create, CalendarEvent event) throws OXException {
        Session session = event.getSession();
        int onBehalfOf = onBehalfOf(event.getCalendarUser(), session);
        CalendarUser principal = ITipUtils.getPrincipal(event.getCalendarParameters());

        ITipMailGenerator generator = generators.create(null, create.getCreatedEvent(), session, onBehalfOf, principal);
        List<NotificationParticipant> recipients = generator.getRecipients();
        for (NotificationParticipant notificationParticipant : recipients) {
            NotificationMail mail = generator.generateCreateMailFor(notificationParticipant);
            if (mail != null) {
                if (mail.getStateType() == null) {
                    mail.setStateType(State.Type.NEW);
                }
                sender.sendMail(mail, session, principal);
            }
        }

    }

    private void handleDelete(DeleteResult delete, CalendarEvent event) throws OXException {
        Session session = event.getSession();
        int onBehalfOf = onBehalfOf(event.getCalendarUser(), session);
        CalendarUser principal = ITipUtils.getPrincipal(event.getCalendarParameters());

        ITipMailGenerator generator = generators.create(null, delete.getOriginal(), session, onBehalfOf, principal);
        List<NotificationParticipant> recipients = generator.getRecipients();
        for (final NotificationParticipant notificationParticipant : recipients) {
            final NotificationMail mail = generator.generateDeleteMailFor(notificationParticipant);
            if (mail != null) {
                if (mail.getStateType() == null) {
                    mail.setStateType(State.Type.DELETED);
                }
                sender.sendMail(mail, session, principal);
            }
        }
    }

    private void handleUpdate(UpdateResult update, CalendarEvent event) throws OXException {
        Session session = event.getSession();
        int onBehalfOf = onBehalfOf(event.getCalendarUser(), session);
        CalendarUser principal = ITipUtils.getPrincipal(event.getCalendarParameters());

        ITipMailGenerator generator = generators.create(update.getOriginal(), update.getUpdate(), session, onBehalfOf, principal);
        List<NotificationParticipant> recipients = generator.getRecipients();
        for (NotificationParticipant notificationParticipant : recipients) {
            NotificationMail mail;
            mail = generator.generateUpdateMailFor(notificationParticipant);
            if (mail != null) {
                if (mail.getStateType() == null) {
                    mail.setStateType(State.Type.MODIFIED);
                }
                sender.sendMail(mail, session, principal);
            }
        }
    }

    private int onBehalfOf(int calendarUser, Session session) {
        return calendarUser == session.getUserId() ? -1 : calendarUser;
    }
}
