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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug51918Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
@RunWith(Parameterized.class)
public class Bug51918Test extends AbstractAJAXSession {

    private static final int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
    private int hourOfDay;

    public Bug51918Test(int hour) {
        super();
        this.hourOfDay = hour;
    }

    @Parameters(name="{0}")
    public static List<Object[]> data() {
        List<Object[]> retval = new ArrayList<Object[]>();
        for (int i = 0; i <= 23; i++) {
            retval.add(new Object[] { i });
        }
        return retval;
    }

    @Test
    public void testEveryHour() throws Exception {
        Appointment app = new Appointment();
        app.setTitle("Bug 51918 Test: " + hourOfDay);
        app.setStartDate(TimeTools.D("06.03." + nextYear + " " + hourOfDay + ":00", catm.getTimezone()));
        app.setEndDate(TimeTools.D("06.03." + nextYear + " " + hourOfDay + ":30", catm.getTimezone()));
        app.setParentFolderID(catm.getPrivateFolder());
        catm.insert(app);

        Appointment before = new Appointment();
        before.setTitle("Bug 51918 Test, before");
        before.setParentFolderID(catm.getPrivateFolder());
        before.setStartDate(TimeTools.D("05.03." + nextYear + " 00:00", TimeZone.getTimeZone("UTC")));
        before.setEndDate(TimeTools.D("06.03." + nextYear + " 00:00", TimeZone.getTimeZone("UTC")));
        before.setFullTime(true);
        before.setIgnoreConflicts(false);
        before.setShownAs(Appointment.RESERVED);

        Appointment same = new Appointment();
        same.setTitle("Bug 51918 Test, same");
        same.setParentFolderID(catm.getPrivateFolder());
        same.setStartDate(TimeTools.D("06.03." + nextYear + " 00:00", TimeZone.getTimeZone("UTC")));
        same.setEndDate(TimeTools.D("07.03." + nextYear + " 00:00", TimeZone.getTimeZone("UTC")));
        same.setFullTime(true);
        same.setIgnoreConflicts(false);
        same.setShownAs(Appointment.RESERVED);

        Appointment after = new Appointment();
        after.setTitle("Bug 51918 Test, after");
        after.setParentFolderID(catm.getPrivateFolder());
        after.setStartDate(TimeTools.D("07.03." + nextYear + " 00:00", TimeZone.getTimeZone("UTC")));
        after.setEndDate(TimeTools.D("08.03." + nextYear + " 00:00", TimeZone.getTimeZone("UTC")));
        after.setFullTime(true);
        after.setIgnoreConflicts(false);
        after.setShownAs(Appointment.RESERVED);

        catm.insert(before);
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();
        if (conflicts != null && conflicts.size() != 0) {
            for (ConflictObject conflict : conflicts) {
                if (conflict.getId() == app.getObjectID()) {
                    fail("Before: Conflict at " + hourOfDay);
                }
            }
        }

        catm.insert(same);
        conflicts = catm.getLastResponse().getConflicts();
        if (conflicts == null || conflicts.size() == 0) {
            fail("Missing conflict at: " + hourOfDay);
        }
        boolean found = false;
        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == app.getObjectID()) {
                found = true;
            }
        }
        assertTrue("Missing conflict at: " + hourOfDay, found);

        catm.insert(after);
        conflicts = catm.getLastResponse().getConflicts();
        if (conflicts != null && conflicts.size() != 0) {
            for (ConflictObject conflict : conflicts) {
                if (conflict.getId() == app.getObjectID()) {
                    fail("After: Conflict at " + hourOfDay);
                }
            }
        }
    }
}
