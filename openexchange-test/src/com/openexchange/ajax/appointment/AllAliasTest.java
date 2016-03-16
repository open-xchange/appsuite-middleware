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

package com.openexchange.ajax.appointment;

import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link AllAliasTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AllAliasTest extends AppointmentTest {

    private Appointment appointment;
    private AJAXClient client;

    /**
     * Initializes a new {@link AllAliasTest}.
     *
     * @param name
     */
    public AllAliasTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();

        appointment = new Appointment();
        appointment.setStartDate(new Date());
        appointment.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("All Alias Test Appointment");
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());

        final InsertRequest insertRequest = new InsertRequest(appointment, client.getValues().getTimeZone());
        final AppointmentInsertResponse insertResponse = client.execute(insertRequest);
        insertResponse.fillAppointment(appointment);
    }

    @Override
    protected void tearDown() throws Exception {
        final DeleteRequest deleteRequest = new DeleteRequest(appointment);
        client.execute(deleteRequest);
        super.tearDown();
    }

    public void testAllAlias() throws Exception {
        final AllRequest allAliasRequest = new AllRequest(client.getValues().getPrivateAppointmentFolder(), "all", new Date(0), new Date(
            Long.MAX_VALUE), client.getValues().getTimeZone());
        final CommonAllResponse allAliasResponse = client.execute(allAliasRequest);
        final Object[][] aliasAppointments = allAliasResponse.getArray();

        final AllRequest allRequest = new AllRequest(
            client.getValues().getPrivateAppointmentFolder(),
            new int[] { 1, 20, 207, 206, 2 },
            new Date(0),
            new Date(Long.MAX_VALUE),
            client.getValues().getTimeZone());
        final CommonAllResponse allResponse = client.execute(allRequest);
        final Object[][] appointments = allResponse.getArray();

        assertEquals("Arrays' sizes are not equal.", aliasAppointments.length, appointments.length);
        for (int i = 0; i < aliasAppointments.length; i++) {
            final Object[] o1 = aliasAppointments[i];
            final Object[] o2 = appointments[i];
            assertEquals("Objects' sizes are not equal.", o1.length, o2.length);
            for (int j = 0; j < o1.length; j++) {
                if ((o1[j] != null || o2[j] != null)) {
                    if (!(o1[j] instanceof JSONArray) && !(o2[j] instanceof JSONArray)) {
                        assertEquals("Array[" + i + "][" + j + "] not equal.", o1[j], o2[j]);
                    } else {
                        compareArrays((JSONArray) o1[j], (JSONArray) o2[j]);
                    }
                }
            }
        }
    }

    private void compareArrays(final JSONArray o1, final JSONArray o2) throws Exception {
        if (o1.length() != o2.length()) {
            fail("Arrays' sizes are not equal.");
        }
        for (int i = 0; i < o1.length(); i++) {
            if ((o1.get(i) != null || o2.get(i) != null)) {
                if (!(o1.get(i) instanceof JSONArray) && !(o2.get(i) instanceof JSONArray)) {
                    assertEquals("Array[" + i + "] not equal.", o1.get(i).toString(), o2.get(i).toString());
                } else {
                    compareArrays((JSONArray) o1.get(i), (JSONArray) o2.get(i));
                }
            }
        }
    }

}
