/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.tryagain.TryAgain;

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
            retval.add(new Object[] { I(i) });
        }
        return retval;
    }

    @SuppressWarnings("null")
    @Test
    @TryAgain
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
