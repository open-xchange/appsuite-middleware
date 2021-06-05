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

package com.openexchange.ajax.appointment;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link ChangeTimeZoneTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class ChangeTimeZoneTest extends AbstractAJAXSession {

    private final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private Appointment app;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        app = new Appointment();
        app.setTitle("ChangeTimeZoneTest");
        app.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        app.setStartDate(D("01.04.2015 08:00", UTC));
        app.setEndDate(D("01.04.2015 09:00", UTC));
        app.setTimezone("Europe/Berlin");

        catm.insert(app);
    }

    @Test
    public void testSimpleTimeZoneChange() throws Exception {
        app.setLastModified(new Date(Long.MAX_VALUE));
        app.setTimezone("US/Eastern");
        catm.update(app);

        Appointment loaded = catm.get(getClient().getValues().getPrivateAppointmentFolder(), app.getObjectID());
        assertEquals("Wrong tmezone.", "US/Eastern", loaded.getTimezone());
        assertTrue(true);
    }
}
