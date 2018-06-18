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
    public void setUp() throws Exception {
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
