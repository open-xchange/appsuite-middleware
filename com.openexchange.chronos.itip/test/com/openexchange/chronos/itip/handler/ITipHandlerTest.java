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

package com.openexchange.chronos.itip.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.java.Autoboxing;

/**
 * {@link ITipHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ITipHandlerTest {

    private ITipHandler handler = new ITipHandler(null, null);

    @Mock
    private CalendarEvent calendarEvent;

    @Mock
    private CalendarParameters calendarParameters;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandle_eventNull_return() {
        Assert.assertFalse(handler.shouldHandle(null));
    }

    @Test
    public void testHandle_calendarParametersNull_process() {
        Mockito.when(calendarEvent.getCalendarParameters()).thenReturn(null);

        Assert.assertTrue(handler.shouldHandle(calendarEvent));
    }

    @Test
    public void testHandle_calendarParametersDoesNotContainSuppressItip_process() {
        Mockito.when(calendarEvent.getCalendarParameters()).thenReturn(calendarParameters);
        Mockito.when(Autoboxing.B(calendarParameters.contains(CalendarParameters.PARAMETER_SUPPRESS_ITIP))).thenReturn(Boolean.FALSE);

        Assert.assertTrue(handler.shouldHandle(calendarEvent));
    }

    @Test
    public void testHandle_calendarParametersDoesNotContainSuppressItipAndFalse_process() {
        Mockito.when(calendarEvent.getCalendarParameters()).thenReturn(calendarParameters);
        Mockito.when(Autoboxing.B(calendarParameters.contains(CalendarParameters.PARAMETER_SUPPRESS_ITIP))).thenReturn(Boolean.TRUE);
        Mockito.when(calendarParameters.get(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.class)).thenReturn(Boolean.FALSE);

        Assert.assertTrue(handler.shouldHandle(calendarEvent));
    }

    @Test
    public void testHandle_calendarParametersDoesNotContainSuppressItipAndTrue_return() {
        Mockito.when(calendarEvent.getCalendarParameters()).thenReturn(calendarParameters);
        Mockito.when(Autoboxing.B(calendarParameters.contains(CalendarParameters.PARAMETER_SUPPRESS_ITIP))).thenReturn(Boolean.TRUE);
        Mockito.when(calendarParameters.get(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.class)).thenReturn(Boolean.TRUE);

        Assert.assertFalse(handler.shouldHandle(calendarEvent));
    }

}
