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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAttributes;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.calendar.itip.sender.MailSenderService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.session.Session;


/**
 * {@link UpdatePerformer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdatePerformer extends AbstrakterDingeMacher {


    public UpdatePerformer(ITipIntegrationUtility util, MailSenderService sender, ITipMailGeneratorFactory generators) {
        super(util, sender, generators);
    }

    @Override
    public Collection<ITipAction> getSupportedActions() {
        return EnumSet.of(ITipAction.ACCEPT, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.ACCEPT_PARTY_CRASHER, ITipAction.ACCEPT_AND_REPLACE, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.UPDATE, ITipAction.CREATE, ITipAction.COUNTER);
    }

    @Override
    public List<Appointment> perform(ITipAction action, ITipAnalysis analysis, Session session, ITipAttributes attributes) throws OXException {
        List<ITipChange> changes = analysis.getChanges();
        List<Appointment> result = new ArrayList<Appointment>(changes.size());

        Map<String, CalendarDataObject> processed = new HashMap<String, CalendarDataObject>();

        for (ITipChange change : changes) {

            CalendarDataObject appointment = change.getNewAppointment();
            if (appointment == null) {
            	continue;
            }
            appointment.setNotification(true);
            int owner = session.getUserId();
            if (analysis.getMessage().getOwner() > 0) {
                owner = analysis.getMessage().getOwner();
            } else if (appointment.getPrincipalId() > 0) {
                owner = appointment.getPrincipalId();
            }
            ensureParticipant(appointment, change.getCurrentAppointment(), action, owner, attributes);
            Appointment original = determineOriginalAppointment(change, processed, session);
            Appointment forMail = appointment;
            if (original != null) {
                updateAppointment(original, appointment, session);
            } else {
                ensureFolderId(appointment, session);
                createAppointment(appointment, session);
                forMail = util.reloadAppointment(appointment, session);
            }

            if (appointment != null && !change.isException()) {
                processed.put(appointment.getUid(), appointment);
            }

            writeMail(action, original, forMail, session, owner);
            result.add(appointment);
        }

        return result;
    }



    private void updateAppointment(Appointment original, CalendarDataObject appointment, Session session) throws OXException {
        AppointmentDiff appointmentDiff = AppointmentDiff.compare(original, appointment);
        CalendarDataObject update = new CalendarDataObject();
        List<FieldUpdate> updates = appointmentDiff.getUpdates();
        boolean write = false;
        for (FieldUpdate fieldUpdate : updates) {
            if (fieldUpdate.getFieldNumber() != CalendarObject.CONFIRMATIONS) {
                update.set(fieldUpdate.getFieldNumber(), fieldUpdate.getNewValue());
                write = true;
            }
        }

        update.setParentFolderID(original.getParentFolderID());
        update.setObjectID(original.getObjectID());

        if (!original.containsRecurrencePosition() && !original.containsRecurrenceDatePosition()) {
            if (appointment.containsRecurrencePosition()) {
                update.setRecurrencePosition(appointment.getRecurrencePosition());
            } else if (appointment.containsRecurrenceDatePosition()) {
                update.setRecurrenceDatePosition(appointment.getRecurrenceDatePosition());
            }
        } else {
            if (original.containsRecurrencePosition()) {
                update.setRecurrencePosition(original.getRecurrencePosition());
            } else if (original.containsRecurrenceDatePosition()) {
                update.setRecurrenceDatePosition(original.getRecurrenceDatePosition());
            }
        }

        CalendarDataObject clone = update.clone();
        if (write) {
            util.updateAppointment(clone, session, original.getLastModified());
            original.setLastModified(clone.getLastModified());
        }

        saveConfirmations(session, appointmentDiff, clone);

        appointment.setObjectID(clone.getObjectID());
        appointment.setParentFolderID(clone.getParentFolderID());

        if (update.containsRecurrencePosition()) {
            appointment.setRecurrencePosition(update.getRecurrencePosition());
        }
    }

    private void saveConfirmations(Session session, AppointmentDiff appointmentDiff, Appointment update) throws OXException {
        if (appointmentDiff.anyFieldChangedOf("confirmations")) {
            FieldUpdate fieldUpdate = appointmentDiff.getUpdateFor("confirmations");
            Difference extraInfo = (Difference) fieldUpdate.getExtraInfo();
            List<Change> changed = extraInfo.getChanged();
            for (Change change : changed) {
                ConfirmationChange confirmationChange = (ConfirmationChange) change;
                util.changeConfirmationForExternalParticipant(update, confirmationChange, session);
            }

        }
    }

    private void createAppointment(CalendarDataObject appointment, Session session) throws OXException {
        util.createAppointment(appointment, session);
        // Update Confirmations
        Appointment reloaded = util.reloadAppointment(appointment, session);
        AppointmentDiff appointmentDiff = AppointmentDiff.compare(reloaded, appointment);
        saveConfirmations(session, appointmentDiff, reloaded);
    }


    private void ensureParticipant(CalendarDataObject appointment, Appointment currentAppointment, ITipAction action, int owner, ITipAttributes attributes) {
        int confirm = CalendarObject.NONE;
        switch (action) {
        case ACCEPT: case ACCEPT_AND_IGNORE_CONFLICTS: case CREATE: confirm = CalendarObject.ACCEPT; break;
        case DECLINE: confirm = CalendarObject.DECLINE; break;
        case TENTATIVE: confirm = CalendarObject.TENTATIVE; break;
        case UPDATE: confirm = getCurrentConfirmation(currentAppointment, owner); break;
        default: confirm = -1;
        }
        String message = null;
        if (attributes != null && attributes.getConfirmationMessage() != null && !attributes.getConfirmationMessage().trim().equals("")) {
            message = attributes.getConfirmationMessage();
        } else {
            message = getCurrentMessage(currentAppointment, owner);
        }
        Participant[] participants = appointment.getParticipants();
        boolean found = false;
        if (null != participants) {
            for (Participant participant : participants) {
                if (participant instanceof UserParticipant) {
                    UserParticipant up = (UserParticipant) participant;
                    if (up.getIdentifier() == owner) {
                        found = true;
                        if (confirm != -1) {
                            up.setConfirm(confirm);
                        }
                        if (message != null) {
                            up.setConfirmMessage(message);
                        }
                    }
                }
            }
        }

        if (!found) {
            UserParticipant up = new UserParticipant(owner);
            if (confirm != -1) {
                up.setConfirm(confirm);
            }
            if (message != null) {
                up.setConfirmMessage(message);
            }
            Participant[] tmp = appointment.getParticipants();
            List<Participant> participantList = (null == tmp) ? new ArrayList<Participant>(1) : new ArrayList<Participant>(Arrays.asList(tmp));
            participantList.add(up);
            appointment.setParticipants(participantList);
        }

        found = false;
        UserParticipant[] users = appointment.getUsers();
        if (users != null) {
            for (UserParticipant userParticipant : users) {
                if (userParticipant.getIdentifier() == owner) {
                    found = true;
                    if (confirm != -1) {
                        userParticipant.setConfirm(confirm);
                    }
                    if (message != null) {
                        userParticipant.setConfirmMessage(message);
                    }
                }
            }
        }

        if (!found) {
            UserParticipant up = new UserParticipant(owner);
            if (confirm != -1) {
                up.setConfirm(confirm);
            }
            if (message != null) {
                up.setConfirmMessage(message);
            }
            UserParticipant[] tmp = appointment.getUsers();
            List<UserParticipant> participantList = (tmp == null) ? new ArrayList<UserParticipant>(1) : new ArrayList<UserParticipant>(Arrays.asList(tmp));
            participantList.add(up);
            appointment.setUsers(participantList);
        }
    }

    private String getCurrentMessage(Appointment currentAppointment, int userId) {
        if (currentAppointment == null || currentAppointment.getUsers() == null || currentAppointment.getUsers().length == 0) {
            return null;
        }

        for (UserParticipant up : currentAppointment.getUsers()) {
            if (up.getIdentifier() == userId) {
                if (up.containsConfirmMessage()) {
                    return up.getConfirmMessage();
                }
            }
        }
        return null;
    }

    private int getCurrentConfirmation(Appointment currentAppointment, int userId) {
        if (currentAppointment == null || currentAppointment.getUsers() == null || currentAppointment.getUsers().length == 0) {
            return -1;
        }

        for (UserParticipant up : currentAppointment.getUsers()) {
            if (up.getIdentifier() == userId) {
                if (up.containsConfirm()) {
                    return up.getConfirm();
                }
            }
        }
        return -1;
    }
}
