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

package com.openexchange.calendar.itip.performers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipDingeMacher;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.generators.ITipMailGenerator;
import com.openexchange.calendar.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.sender.MailSenderService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.session.Session;


/**
 * {@link AbstrakterDingeMacher}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstrakterDingeMacher implements ITipDingeMacher {

    protected ITipIntegrationUtility util;
    private final MailSenderService sender;
    private final ITipMailGeneratorFactory mailGenerators;

    public AbstrakterDingeMacher(final ITipIntegrationUtility util, final MailSenderService mailSender,  final ITipMailGeneratorFactory mailGenerators) {
        super();
        this.util = util;
        this.sender = mailSender;
        this.mailGenerators = mailGenerators;
    }

    protected Appointment determineOriginalAppointment(final ITipChange change, final Map<String, CalendarDataObject> processed, final Session session) throws OXException {
        Appointment currentAppointment = change.getCurrentAppointment();
        if (currentAppointment == null || currentAppointment.getObjectID() <= 0) {
            if (change.isException()) {
                currentAppointment = change.getMasterAppointment();
                if (currentAppointment == null || currentAppointment.getObjectID() <= 0) {
                    currentAppointment = processed.get(change.getNewAppointment().getUid());
                    if (currentAppointment == null) {
                    	currentAppointment = util.loadAppointment(change.getNewAppointment(), session);
                    }
                }
            }
        }
        return currentAppointment;
    }

    protected void ensureFolderId(final Appointment appointment, final Session session) throws OXException {
        if (appointment.containsParentFolderID() && appointment.getParentFolderID() > 0) {
            return;
        }
        final int privateCalendarFolderId = util.getPrivateCalendarFolderId(session);
        appointment.setParentFolderID(privateCalendarFolderId);

    }

    protected void writeMail(final ITipAction action, Appointment original, final Appointment appointment, final Session session, int owner) throws OXException {
        switch (action) {
        case COUNTER:
            return;
        default: //Continue normally
        }
        final Appointment filled = fillup(original, appointment);
        original = constructOriginalForMail(action, original, filled, session, owner);

        final ITipMailGenerator generator = mailGenerators.create(original, filled, session, owner);
        switch (action) {
        case CREATE:
            if (!generator.userIsTheOrganizer()) {
                return;
            }
            List<NotificationParticipant> recipients = generator.getRecipients();
            for (final NotificationParticipant p : recipients) {
                final NotificationMail mail = generator.generateCreateExceptionMailFor(p);
                if (mail != null) {
                    sender.sendMail(mail, session);
                }
            }
            break;
        case UPDATE:
        	if (!generator.userIsTheOrganizer()) {
                return;
            }
            recipients = generator.getRecipients();
        	for (final NotificationParticipant p : recipients) {
                final NotificationMail mail = generator.generateUpdateMailFor(p);
                if (mail != null ) {
                    sender.sendMail(mail, session);
                }
            }
        	break;
        case DECLINECOUNTER:
            recipients = generator.getRecipients();
            for (final NotificationParticipant p : recipients) {
                final NotificationMail mail = generator.generateDeclineCounterMailFor(p);
                if (mail != null) {
                    sender.sendMail(mail, session);
                }
            }
        	break;
        case SEND_APPOINTMENT:
            recipients = generator.getRecipients();
            for (final NotificationParticipant p : recipients) {
                final NotificationMail mail = generator.generateCreateMailFor(p);
                if (mail != null) {
                    sender.sendMail(mail, session);
                }
            }
        	break;
        case REFRESH:
            recipients = generator.getRecipients();
            for (final NotificationParticipant p : recipients) {
                final NotificationMail mail = generator.generateRefreshMailFor(p);
                if (mail != null) {
                    sender.sendMail(mail, session);
                }
            }
        	break;
        default:
            recipients = generator.getRecipients();
            for (final NotificationParticipant p : recipients) {
                final NotificationMail mail = generator.generateUpdateMailFor(p);
                if (mail != null) {
                    sender.sendMail(mail, session);
                }
            }
        }
    }

    private Appointment constructOriginalForMail(final ITipAction action, final Appointment original, final Appointment appointment, final Session session, int owner) {
        switch (action) {
        case ACCEPT: case ACCEPT_AND_IGNORE_CONFLICTS: case ACCEPT_AND_REPLACE: case ACCEPT_PARTY_CRASHER: case DECLINE: case TENTATIVE: return constructFakeOriginal(appointment, session, owner);
        default: return original;
        }
    }

    private Appointment constructFakeOriginal(final Appointment appointment, final Session session, int owner) {
        final Appointment clone = appointment.clone();
        final Participant[] participants = clone.getParticipants();
        final List<Participant> changed = new ArrayList<Participant>();
        for (final Participant participant : participants) {
            if (participant instanceof UserParticipant) {
                UserParticipant up = (UserParticipant) participant;
                if (up.getIdentifier() == owner) {
                    up = new UserParticipant(up.getIdentifier());

                    up.setConfirm(ConfirmStatus.NONE.getId());
                }
                changed.add(up);
            } else {
                changed.add(participant);
            }
        }

        clone.setParticipants(changed);

        final UserParticipant[] users = clone.getUsers();
        final List<UserParticipant> changedUsers = new ArrayList<UserParticipant>();
        for (UserParticipant up : users) {
            if (up.getIdentifier() == owner) {
                up = new UserParticipant(up.getIdentifier());
                up.setConfirm(ConfirmStatus.NONE.getId());
            }
            changedUsers.add(up);
        }
        clone.setUsers(changedUsers);

        return clone;
    }

    private Appointment fillup(final Appointment original, final Appointment appointment) {
        if (original == null) {
            return appointment;
        }
        final AppointmentDiff diff = AppointmentDiff.compare(original, appointment);

        final Appointment copy = original.clone();
        final List<FieldUpdate> updates = diff.getUpdates();
        for (final FieldUpdate fieldUpdate : updates) {
            copy.set(fieldUpdate.getFieldNumber(), fieldUpdate.getNewValue());
        }

        return copy;
    }

    protected int getOwner(Session session, ITipAnalysis analysis, Appointment appointment) {
        int owner = session.getUserId();
        if (analysis.getMessage().getOwner() > 0) {
            owner = analysis.getMessage().getOwner();
        } else if (appointment.getPrincipalId() > 0) {
            owner = appointment.getPrincipalId();
        }
        return owner;
    }

}
