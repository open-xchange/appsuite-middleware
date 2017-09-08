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
