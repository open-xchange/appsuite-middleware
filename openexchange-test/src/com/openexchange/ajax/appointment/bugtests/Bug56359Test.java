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

package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.ConfirmRequest;
import com.openexchange.ajax.appointment.action.ConfirmResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;

/**
 * {@link Bug56359Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug56359Test extends AppointmentTest {

    /**
     * Initializes a new {@link Bug56359Test}.
     */
    public Bug56359Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * reset default folder permissions prior test execution
         */
        catm.resetDefaultFolderPermissions();
    }

    @Test
    public void testConfirmForeignAppointment() throws Exception {
        /*
         * as user A, create a new appointment in the user's default folder
         */
        Appointment appointment = createAppointmentObject("Bug56359Test");
        appointment.setIgnoreConflicts(true);
        appointment = catm.insert(appointment);
        /*
         * as user B, try and add an external user via 'confirm' action (in user b's personal calendar folder)
         */
        int folderId = getClient2().getValues().getPrivateAppointmentFolder();
        int objectId = appointment.getObjectID();
        ConfirmResponse confirmResponse = getClient2().execute(new ConfirmRequest(
            folderId, objectId, Appointment.ACCEPT, "", "test@example.com", appointment.getLastModified(), false));
        assertTrue("No errors in confirm response", confirmResponse.hasError());
        assertEquals("Unexpected error in confirm response", "APP-0059", confirmResponse.getException().getErrorCode());
        /*
         * verify that participant was not added
         */
        appointment = catm.get(appointment);
        for (Participant participant : appointment.getParticipants()) {
            assertNotEquals("External participant was added", "test@example.com", participant.getEmailAddress());
        }
    }

}
