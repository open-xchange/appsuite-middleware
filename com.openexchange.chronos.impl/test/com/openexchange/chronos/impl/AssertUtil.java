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

package com.openexchange.chronos.impl;

import static org.junit.Assert.assertEquals;
import java.util.List;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.FreeBusyTime;

/**
 * {@link AssertUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class AssertUtil {

    /**
     * Asserts that the expected {@link List} of {@link FreeBusyTime} is equal the actual {@link List}
     * of {@link FreeBusyTime}s
     * 
     * @param expected the expected list
     * @param actual the actual list
     */
    static void assertFreeBusyTimes(List<FreeBusyTime> expected, List<FreeBusyTime> actual) {
        assertEquals("The amount of the free/busy times does not match", expected.size(), actual.size());
        for (int index = 0; index < actual.size(); index++) {
            assertFreeBusyTime(expected.get(index), actual.get(index));
        }
    }

    /**
     * Asserts that the expected {@link FreeBusyTime} is equal the actual {@link FreeBusyTime}
     * 
     * @param expected The expected {@link FreeBusyTime}
     * @param actual The actual {@link FreeBusyTime}
     */
    static void assertFreeBusyTime(FreeBusyTime expected, FreeBusyTime actual) {
        assertEquals(expected.getFbType(), actual.getFbType());
        assertEquals(expected.getStartTime(), actual.getStartTime());
        assertEquals(expected.getEndTime(), actual.getEndTime());
    }

    /**
     * Asserts that the expected {@link List} of {@link Available}s is equal to the actual {@link List}
     * of {@link Available}s
     * 
     * @param expected The expected list
     * @param actual The actual list
     */
    static void assertAvailableBlocks(List<Available> expected, List<Available> actual) {
        assertEquals("The amount of the available blocks does not match", expected.size(), actual.size());
        for (int index = 0; index < actual.size(); index++) {
            assertAvailable(expected.get(index), actual.get(index));
        }
    }

    /**
     * Asserts that the expected {@link Available} is equal to the actual {@link Available}
     * 
     * @param expected The expected {@link Available}
     * @param actual The actual {@link Available}
     */
    static void assertAvailable(Available expected, Available actual) {
        //assertEquals("The summary does not match", expected.getSummary(), actual.getSummary());
        assertEquals("The start time does not match", expected.getStartTime(), actual.getStartTime());
        assertEquals("The end time does not match", expected.getEndTime(), actual.getEndTime());

    }
}
