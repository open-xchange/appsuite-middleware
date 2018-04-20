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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.impl.performer.UpdatesPerformer.getResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.AbstractCombineTest;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

/**
 * {@link UpdatesPerformerTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdatesPerformerTest extends AbstractCombineTest {

    private List<Event> deletedEvents;
    private List<Event> newAndModifiedEvents;

    @Before
    public void setup() throws OXException {
        deletedEvents = new ArrayList<Event>();
        for (int i = 1; i <= 100; i++) {
            Event event = new Event();
            event.setId(String.valueOf(i));
            event.setFolderId("30");
            event.setTimestamp(i);
            event.setUid(UUIDs.getUnformattedStringFromRandom());
            deletedEvents.add(event);
        }
        newAndModifiedEvents = new ArrayList<Event>();
        for (int i = 1; i <= 100; i++) {
            Event event = new Event();
            event.setId(String.valueOf(i));
            event.setFolderId("30");
            event.setTimestamp(i);
            event.setUid(UUIDs.getUnformattedStringFromRandom());
            newAndModifiedEvents.add(event);
        }
    }

    @Test
    public void testLimitedResults1() throws Exception {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 0), 100, 100, 100);
    }

    @Test
    public void testLimitedResults2() throws Exception {
        assertUpdatesResult(getResult(newAndModifiedEvents, null, 0), 100, -1, 100);
    }

    @Test
    public void testLimitedResults3() throws Exception {
        assertUpdatesResult(getResult(null, deletedEvents, 0), -1, 100, 100);
    }

    @Test
    public void testLimitedResults4() throws Exception {
        assertUpdatesResult(getResult(null, null, 0), -1, -1, 0);
    }

    @Test
    public void testLimitedResults5() throws Exception {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 200), 100, 100, 100);
    }

    @Test
    public void testLimitedResults6() throws Exception {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 100), 50, 50, 50);
    }

    @Test
    public void testLimitedResults7() throws Exception {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 25), 13, 12, 13);
    }

    @Test
    public void testLimitedResults8() throws Exception {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 1), 1, 0, 1);
    }

    @Test
    public void testLimitedResults9() throws Exception {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 400), 100, 100, 100);
    }

    private void assertUpdatesResult(UpdatesResult result, int expectedNewAndModifiedSize, int expectedDeletedSize, long expectedTimestamp) {
        assertNotNull(result);
        if (-1 == expectedNewAndModifiedSize) {
            assertNull(result.getNewAndModifiedEvents());
        } else {
            assertNotNull(result.getNewAndModifiedEvents());
            assertEquals(expectedNewAndModifiedSize, result.getNewAndModifiedEvents().size());
        }
        if (-1 == expectedDeletedSize) {
            assertNull(result.getDeletedEvents());
        } else {
            assertNotNull(result.getDeletedEvents());
            assertEquals(expectedDeletedSize, result.getDeletedEvents().size());
        }
        assertEquals(expectedTimestamp, result.getTimestamp());
    }

}
