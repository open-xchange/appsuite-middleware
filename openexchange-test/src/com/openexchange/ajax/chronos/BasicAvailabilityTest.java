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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.AvailabilityData;
import com.openexchange.testing.httpclient.models.Available;
import com.openexchange.testing.httpclient.models.GetAvailabilityResponse;
import com.openexchange.testing.httpclient.modules.ChronosApi;

/**
 * {@link BasicAvailabilityTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicAvailabilityTest extends AbstractChronosTest {

    private ChronosApi chronosApi;

    @Override
    public void tearDown() throws Exception {
        // Clear the availability
        chronosApi.setAvailability(defaultUserApi.getSession(), new AvailabilityData());
        super.tearDown();
    }

    /**
     * Tests setting a single available block
     */
    @Test
    public void testSetSingleAvailableBlock() throws Exception {
        AvailabilityData availabilityData = new AvailabilityData();
        Available expected = createAvailable(System.currentTimeMillis(), System.currentTimeMillis() + 7200);
        availabilityData.addAvailableTimesItem(expected);

        chronosApi = new ChronosApi(defaultUserApi.getClient());

        // Set the availability
        chronosApi.setAvailability(defaultUserApi.getSession(), availabilityData);

        // Get the availability
        GetAvailabilityResponse availability = chronosApi.getAvailability(defaultUserApi.getSession());
        List<Available> availableBlocks = availability.getData();

        // Assert
        assertNotNull("The response payload is null", availableBlocks);
        assertFalse("The availables array is empty", availableBlocks.isEmpty());
        assertTrue("The are more than one available blocks", availableBlocks.size() == 1);
        assertAvailable(expected, availableBlocks.get(0));
    }

    /**
     * Tests setting a single available block with recurrence rule
     */
    @Test
    public void testSetSingleAvailableBlockWithRecurrenceRule() throws Exception {
        AvailabilityData availabilityData = new AvailabilityData();
        Available expected = createAvailable(System.currentTimeMillis(), System.currentTimeMillis() + 7200, "FREQ=WEEKLY;BYDAY=TH");
        availabilityData.addAvailableTimesItem(expected);

        chronosApi = new ChronosApi(defaultUserApi.getClient());

        // Set the availability
        chronosApi.setAvailability(defaultUserApi.getSession(), availabilityData);

        // Get the availability
        GetAvailabilityResponse availability = chronosApi.getAvailability(defaultUserApi.getSession());
        List<Available> availableBlocks = availability.getData();

        // Assert
        assertNotNull("The response payload is null", availableBlocks);
        assertFalse("The availables array is empty", availableBlocks.isEmpty());
        assertTrue("The are more than one available blocks", availableBlocks.size() == 1);
        assertAvailable(expected, availableBlocks.get(0));
    }

    /**
     * Tests setting multiple available blocks with recurrence rule
     */
    @Test
    public void testSetMultipleAvailableBlocks() throws Exception {
        AvailabilityData availabilityData = new AvailabilityData();
        availabilityData.addAvailableTimesItem(createAvailable(System.currentTimeMillis() + 36000, System.currentTimeMillis() + 36000 + 7200, "FREQ=WEEKLY;BYDAY=MO"));
        availabilityData.addAvailableTimesItem(createAvailable(System.currentTimeMillis(), System.currentTimeMillis() + 7200, "FREQ=WEEKLY;BYDAY=TH"));

        chronosApi = new ChronosApi(defaultUserApi.getClient());

        // Set the availability
        chronosApi.setAvailability(defaultUserApi.getSession(), availabilityData);

        // Get the availability
        GetAvailabilityResponse availability = chronosApi.getAvailability(defaultUserApi.getSession());
        List<Available> availableBlocks = availability.getData();

        // Assert
        assertNotNull("The response payload is null", availableBlocks);
        assertFalse("The availables array is empty", availableBlocks.isEmpty());
        assertAvailable(availabilityData.getAvailableTimes(), availableBlocks);
    }

    /**
     * Creates an {@link Available} block within the specified range
     * 
     * @param from The starting point in time
     * @param until The ending point in time
     * @return The {@link Available} block
     */
    private Available createAvailable(long from, long until) {
        Available available = new Available();
        available.setStart(DateTimeUtil.getDateTime(from));
        available.setEnd(DateTimeUtil.getDateTime(until));
        available.setUser(defaultUserApi.getCalUser());
        return available;
    }

    /**
     * Creates an {@link Available} block within the specified range and the specified
     * recurrence rule
     * 
     * @param from The starting point in time
     * @param until The ending point in time
     * @param rrule The recurrence rule
     * @return The {@link Available} block
     * @return The {@link Available} block
     */
    private Available createAvailable(long from, long until, String rrule) {
        Available available = createAvailable(from, until);
        available.setRrule(rrule);
        return available;
    }

    /**
     * Asserts that the specified expected {@link List} of {@link Available} blocks
     * is equal to the specified actual {@link List} of {@link Available} blocks
     * 
     * @param expected The expected {@link List}
     * @param actual The actual {@link List}
     */
    private void assertAvailable(List<Available> expected, List<Available> actual) {
        assertEquals("The amount of available blocks does not match", expected.size(), actual.size());
        for (int index = 0; index < expected.size(); index++) {
            assertAvailable(expected.get(index), actual.get(index));

        }
    }

    /**
     * Asserts that the expected {@link Available} is equal the actual {@link Available}
     * 
     * @param expected The expected {@link Available}
     * @param actual The actual {@link Available}
     */
    private void assertAvailable(Available expected, Available actual) {
        assertEquals("The recurrence rule does not match", expected.getRrule(), actual.getRrule());
        assertEquals("The start time does not match", expected.getStart(), actual.getStart());
        assertEquals("The start time does not match", expected.getEnd(), actual.getEnd());
        assertEquals("The user id does not match", expected.getUser(), actual.getUser());
    }
}
