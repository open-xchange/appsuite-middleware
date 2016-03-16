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

package com.openexchange.ajax.appointment.recurrence;

import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * OX uses different ways of limiting/ending a series: You can use an ending date or you can define a number of occurrences. This
 * information needs to be kept (you cannot just convert one into the other), because changing this series might have different
 * implications, eg.: If you move a daily series two days closer to the end date, you might lose two occurrences - if you move a series with
 * 7 occurrences, moving does not matter.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForDifferentWaysOfEndingASeries extends ManagedAppointmentTest {

    public TestsForDifferentWaysOfEndingASeries(String name) {
        super(name);
    }

    public void testShouldNotSetUntilIfOccurrencesIsUsed() throws Exception {
        Appointment app = generateDailyAppointment();
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_COUNT, 7);
        changes.put(Appointment.RECURRENCE_TYPE, app.get(Appointment.RECURRENCE_TYPE));
        changes.put(Appointment.INTERVAL, app.get(Appointment.INTERVAL));

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.UNTIL, null);

        positiveAssertionOnCreate.check(app, changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    public void testShouldNotSetOccurrencesIfUntilIsUsed() throws Exception {
        Appointment app = generateDailyAppointment();
        Changes changes = new Changes();
        changes.put(Appointment.UNTIL, D("7/1/2008 00:00"));
        changes.put(Appointment.RECURRENCE_TYPE, app.get(Appointment.RECURRENCE_TYPE));
        changes.put(Appointment.INTERVAL, app.get(Appointment.INTERVAL));

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.RECURRENCE_COUNT, null);

        positiveAssertionOnCreate.check(app, changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }


    public void testShouldNotSetOccurrencesIfNothingIsSet() throws Exception {
        Appointment app = generateDailyAppointment();
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, app.get(Appointment.RECURRENCE_TYPE));
        changes.put(Appointment.INTERVAL, app.get(Appointment.INTERVAL));

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.RECURRENCE_COUNT, null);
        expectations.put(Appointment.UNTIL, null);

        positiveAssertionOnCreate.check(app, changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }
}
