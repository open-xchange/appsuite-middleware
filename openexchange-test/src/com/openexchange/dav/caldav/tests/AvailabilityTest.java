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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link AvailabilityTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailabilityTest extends CalDAVTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        OptionsMethod options = null;
        try {
            options = new OptionsMethod(getBaseUri());
            assertEquals("unexpected http status", StatusCodes.SC_OK, super.getWebDAVClient().executeMethod(options));
            assertResponseHeaders(new String[] { "calendar-availability" }, "DAV", options);
        } catch (AssertionError e) {
            throw new AssumptionViolatedException("\"calendar-availability\" not supported.", e);
        } finally {
            release(options);
        }
    }

    /**
     * Simple test that simulates the creation of an availability block
     * with a single available slot on the client and the immediate sync
     * on the server.
     */
    @Test
    public void setAvailabilityFromClient() throws Exception {
        String uid = randomUID();
        String summary = "test";
        String location = "testcity";
        Date start = TimeTools.D("tomorrow at 3pm");
        Date end = TimeTools.D("tomorrow at 4pm");

        // Generate and set
        String iCal = generateVAvailability(start, end, uid, summary, location);
        assertEquals("response code wrong", 207, propPatchICal(iCal));

        // Get from client and assert
        List<ICalResource> iCalResource = propFind("calendar-availability");
        assertNotNull("The expected availability resource is null", iCalResource);
        assertEquals("Expected only one availability resource", 1, iCalResource.size());

        ICalResource resource = iCalResource.get(0);
        List<Component> availabilities = resource.getAvailabilities();
        assertNotNull("The availabilities list is null", availabilities);
        assertEquals("Expected only one availability block", 1, availabilities.size());

        Component availability = availabilities.get(0);
        assertEquals("Expected one sub-component", 1, availability.getComponents().size());
        assertEquals("The uid property does not match", uid, availability.getProperty("UID").getValue());
        assertEquals("The summary property does not match", summary, availability.getProperty("SUMMARY").getValue());
        assertEquals("The location property does not match", location, availability.getProperty("LOCATION").getValue());

        Component available = availability.getComponents().get(0);
        assertEquals("The start date does not match", start, TimeTools.D(available.getProperty("DTSTART").getValue(), TimeZone.getTimeZone("Europe/Berlin")));
        assertEquals("The end date does not match", end, TimeTools.D(available.getProperty("DTEND").getValue(), TimeZone.getTimeZone("Europe/Berlin")));
    }
}
