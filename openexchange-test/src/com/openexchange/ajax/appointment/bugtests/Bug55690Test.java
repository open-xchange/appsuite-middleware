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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertFalse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug55690Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug55690Test extends AbstractAJAXSession {

    private AJAXClient client;
    private AJAXClient client2;
    private CalendarTestManager catm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();
        client2 = getClient2();
        catm2 = new CalendarTestManager(client2);
    }

    @Test
    public void testBug() throws Exception {
        FolderObject sharedFolder = ftm.generateSharedFolder("Bug 53714 Folder " + System.currentTimeMillis(), FolderObject.CALENDAR, client.getValues().getPrivateAppointmentFolder(), client.getValues().getUserId());
        ftm.insertFolderOnServer(sharedFolder);
        Appointment appointment = new Appointment();
        appointment.setTitle("Bug 53714 test");
        appointment.setStartDate(D("01.06.2017 08:00"));
        appointment.setEndDate(D("01.06.2017 08:00"));
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        appointment.setParticipants(new Participant[] { new UserParticipant(getClient().getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        catm2.insert(appointment);

        Appointment loadForUpdate = catm.get(client.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        loadForUpdate.setParentFolderID(sharedFolder.getObjectID());
        catm.update(client.getValues().getPrivateAppointmentFolder(), loadForUpdate);

        ftm.deleteFolderOnServer(sharedFolder);
        assertFalse("No exception expected", ftm.getLastResponse().hasWarnings() || ftm.getLastResponse().hasError());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            catm2.cleanUp();
            ftm.cleanUp();
        } finally {
            super.tearDown();
        }
    }
}
