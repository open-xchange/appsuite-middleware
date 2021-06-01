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

package com.openexchange.chronos.itip.analyzers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;

/**
 * {@link UpdateITipAnalyzerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class UpdateITipAnalyzerTest {

    @InjectMocks
    private UpdateITipAnalyzer updateITipAnalyzer;

    @Mock
    private Event original;

    @Mock
    private Event update = new Event();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(original.getAttendees()).thenReturn(null);
    }

    @Test
    public void testAddResourcesToUpdate_originalNull_doNothing() {
        updateITipAnalyzer.addResourcesToUpdate(null, update);

        Mockito.verify(original, Mockito.never()).getAttendees();
    }

    @Test
    public void testAddResourcesToUpdate_updateNull_doNothing() {
        updateITipAnalyzer.addResourcesToUpdate(original, null);

        Mockito.verify(original, Mockito.never()).getAttendees();
    }

    @Test
    public void testAddResourcesToUpdate_attendeesNullInOriginal_doNothing() {
        updateITipAnalyzer.addResourcesToUpdate(original, update);

        Mockito.verify(original, Mockito.atLeast(1)).getAttendees();
        Mockito.verify(update, Mockito.never()).getAttendees();
    }

    @Test
    public void testAddResourcesToUpdate_noAttendeesInOriginal_doNothing() {
        Mockito.when(original.getAttendees()).thenReturn(Collections.emptyList());

        updateITipAnalyzer.addResourcesToUpdate(original, update);

        Mockito.verify(original, Mockito.atLeast(1)).getAttendees();
        Mockito.verify(update, Mockito.never()).getAttendees();
    }

    @Test // bug 57785
    public void testAddResourcesToUpdate_attendeesInOriginalButNoCU_doNothing() {
        Mockito.when(original.getAttendees()).thenReturn(Arrays.asList(new Attendee()));
        Event lUpdate = new Event();

        updateITipAnalyzer.addResourcesToUpdate(original, lUpdate);

        Mockito.verify(original, Mockito.atLeast(1)).getAttendees();
        assertNull(lUpdate.getAttendees());
    }

    @Test
    public void testAddResourcesToUpdate_wrongCalendarUserType_doNothing() {
        Attendee attendee = new Attendee();
        attendee.setCuType(CalendarUserType.ROOM);
        Mockito.when(original.getAttendees()).thenReturn(Arrays.asList(attendee));
        Event lUpdate = new Event();

        updateITipAnalyzer.addResourcesToUpdate(original, lUpdate);

        Mockito.verify(original, Mockito.atLeast(1)).getAttendees();
        assertNull(lUpdate.getAttendees());
    }

    @Test
    public void testAddResourcesToUpdate_addAttendee() {
        Attendee attendee = Mockito.mock(Attendee.class);
        Mockito.when(attendee.getCuType()).thenReturn(CalendarUserType.RESOURCE);
        Mockito.when(original.getAttendees()).thenReturn(Arrays.asList(attendee));
        List<Attendee> emptyList = new ArrayList<>(1);
        Mockito.when(update.getAttendees()).thenReturn(emptyList);

        updateITipAnalyzer.addResourcesToUpdate(original, update);

        Mockito.verify(original, Mockito.atLeast(1)).getAttendees();
        assertEquals(1, emptyList.size());
    }

    @Test
    public void testAddResourcesToUpdate_attendeesFromUpdateNull() {
        Attendee attendee = Mockito.mock(Attendee.class);
        Mockito.when(attendee.getCuType()).thenReturn(CalendarUserType.RESOURCE);
        Mockito.when(original.getAttendees()).thenReturn(Arrays.asList(attendee));
        Event lUpdate = new Event();

        updateITipAnalyzer.addResourcesToUpdate(original, lUpdate);

        Mockito.verify(original, Mockito.atLeast(1)).getAttendees();
        assertEquals(1, lUpdate.getAttendees().size());
    }

    @Test
    public void testAddResourcesToUpdate_attendeesFromUpdateEmpty() {
        Attendee attendee = Mockito.mock(Attendee.class);
        Mockito.when(attendee.getCuType()).thenReturn(CalendarUserType.RESOURCE);
        Mockito.when(original.getAttendees()).thenReturn(Arrays.asList(attendee));
        Event lUpdate = new Event();
        lUpdate.setAttendees(new ArrayList<>());

        updateITipAnalyzer.addResourcesToUpdate(original, lUpdate);

        Mockito.verify(original, Mockito.atLeast(1)).getAttendees();
        assertEquals(1, lUpdate.getAttendees().size());
    }

}
