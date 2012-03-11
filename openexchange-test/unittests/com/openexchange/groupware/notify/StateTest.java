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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
package com.openexchange.groupware.notify;

import com.openexchange.mail.usersetting.UserSettingMail;
import junit.framework.TestCase;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class StateTest extends TestCase {

    public void testAppModificationUserNeverGetsAnEmail() {
        UserSettingMail settings = null;
        int owner = 12;
        int modificationUser = 11;
        int participant = 11;

        State state = new AppointmentState(null, null, null);

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));
    }

    public void testTaskModificationUserNeverGetsAnEmail() {
        UserSettingMail settings = null;
        int owner = 12;
        int modificationUser = 11;
        int participant = 11;

        State state = new TaskState(null, null, null);

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));
    }

    public void testNewAppRespectsNewModifiedDeletedFlag() {
        assertRespectsNewModifiedDeletedFlag(
                new AppointmentState(null, null, State.Type.NEW)
        );
    }

    public void testModifiedAppRespecstNewModifiedDeletedFlag() {
        assertRespectsNewModifiedDeletedFlag(
                new AppointmentState(null, null, State.Type.MODIFIED)
        );
    }

    public void testDeletedAppRespecstNewModifiedDeletedFlag() {
        assertRespectsNewModifiedDeletedFlag(
                new AppointmentState(null, null, State.Type.DELETED)
        );
    }

    public void testNewTaskRespectsNewModifiedDeletedFlag() {
        assertRespectsNewModifiedDeletedFlag(
                new TaskState(null, null, State.Type.NEW)
        );
    }

    public void testModifiedTaskRespecstNewModifiedDeletedFlag() {
        assertRespectsNewModifiedDeletedFlag(
                new TaskState(null, null, State.Type.MODIFIED)
        );
    }

    public void testDeletedTaskRespecstNewModifiedDeletedFlag() {
        assertRespectsNewModifiedDeletedFlag(
                new TaskState(null, null, State.Type.DELETED)
        );
    }

    private void assertRespectsNewModifiedDeletedFlag(AppointmentState state) {
        UserSettingMail settings = new UserSettingMail(-1,-1);
        settings.setNotifyAppointments(false);

        int owner = 12;
        int modificationUser = 11;
        int participant = 13;

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));

        settings.setNotifyAppointments(true);

        assertTrue(state.sendMail(settings, owner, participant, modificationUser));
    }

    private void assertRespectsNewModifiedDeletedFlag(TaskState state) {
        UserSettingMail settings = new UserSettingMail(-1,-1);
        settings.setNotifyTasks(false);

        int owner = 12;
        int modificationUser = 11;
        int participant = 13;

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));

        settings.setNotifyTasks(true);

        assertTrue(state.sendMail(settings, owner, participant, modificationUser));
    }

    public void testAppAcceptRespectsOwnerFlagForOwner() {
        assertRespectsOwnerFlag(
            new AppointmentState(null, null, State.Type.ACCEPTED)
        );
    }

    public void testAppDeclineRespectsOwnerFlagForOwner() {
        assertRespectsOwnerFlag(
            new AppointmentState(null, null, State.Type.DECLINED)
        );
    }

    public void testAppTentativeRespectsOwnerFlagForOwner() {
        assertRespectsOwnerFlag(
            new AppointmentState(null, null, State.Type.TENTATIVELY_ACCEPTED)
        );
    }

    public void testTaskAcceptRespectsOwnerFlagForOwner() {
        assertRespectsOwnerFlag(
            new TaskState(null, null, State.Type.ACCEPTED)
        );
    }

    public void testTaskDeclineRespectsOwnerFlagForOwner() {
        assertRespectsOwnerFlag(
            new TaskState(null, null, State.Type.DECLINED)
        );
    }

    public void testTaskTentativeRespectsOwnerFlagForOwner() {
        assertRespectsOwnerFlag(
            new TaskState(null, null, State.Type.TENTATIVELY_ACCEPTED)
        );
    }

    private void assertRespectsOwnerFlag(AppointmentState state) {
        UserSettingMail settings = new UserSettingMail(-1,-1);
        settings.setNotifyAppointmentsConfirmOwner(false);

        int owner = 12;
        int modificationUser = 11;
        int participant = 12;

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));

        settings.setNotifyAppointmentsConfirmOwner(true);

        assertTrue(state.sendMail(settings, owner, participant, modificationUser));
    }

    private void assertRespectsOwnerFlag(TaskState state) {
        UserSettingMail settings = new UserSettingMail(-1,-1);
        settings.setNotifyTasksConfirmOwner(false);

        int owner = 12;
        int modificationUser = 11;
        int participant = 12;

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));

        settings.setNotifyTasksConfirmOwner(true);

        assertTrue(state.sendMail(settings, owner, participant, modificationUser));
    }

    public void testAppAcceptRespectsParticipantFlagForParticipant() {
        assertRespectsParticipantFlagForParticipant(
            new AppointmentState(null, null, State.Type.ACCEPTED)
        );
    }

    public void testAppDeclineRespectsParticipantFlagForParticipant() {
        assertRespectsParticipantFlagForParticipant(
            new AppointmentState(null, null, State.Type.DECLINED)
        );
    }

    public void testAppTentativeRespectsParticipantFlagForParticipant() {
        assertRespectsParticipantFlagForParticipant(
            new AppointmentState(null, null, State.Type.TENTATIVELY_ACCEPTED)
        );
    }

    public void testTaskAcceptRespectsParticipantFlagForParticipant() {
        assertRespectsParticipantFlagForParticipant(
            new TaskState(null, null, State.Type.ACCEPTED)
        );
    }

    public void testTaskDeclineRespectsParticipantFlagForParticipant() {
        assertRespectsParticipantFlagForParticipant(
            new TaskState(null, null, State.Type.DECLINED)
        );
    }

    public void testTaskTentativeRespectsParticipantFlagForParticipant() {
        assertRespectsParticipantFlagForParticipant(
            new TaskState(null, null, State.Type.TENTATIVELY_ACCEPTED)
        );
    }

    private void assertRespectsParticipantFlagForParticipant(AppointmentState state) {
        UserSettingMail settings = new UserSettingMail(-1,-1);
        settings.setNotifyAppointmentsConfirmParticipant(false);

        int owner = 12;
        int modificationUser = 11;
        int participant = 13;

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));

        settings.setNotifyAppointmentsConfirmParticipant(true);

        assertTrue(state.sendMail(settings, owner, participant, modificationUser));
    }

    private void assertRespectsParticipantFlagForParticipant(TaskState state) {
        UserSettingMail settings = new UserSettingMail(-1,-1);
        settings.setNotifyTasksConfirmParticipant(false);

        int owner = 12;
        int modificationUser = 11;
        int participant = 13;

        assertFalse(state.sendMail(settings, owner, participant, modificationUser));

        settings.setNotifyTasksConfirmParticipant(true);

        assertTrue(state.sendMail(settings, owner, participant, modificationUser));
    }

    public void testAppReminderChangeNeverTriggersNotification() {
        State state = new AppointmentState(null,null, State.Type.REMINDER);
        assertFalse(state.sendMail(null, 12, 13,14));
    }

    public void testTaskReminderChangeNeverTriggersNotification() {
        State state = new TaskState(null,null, State.Type.REMINDER);
        assertFalse(state.sendMail(null, 12, 13,14));
    }


}
