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

package com.openexchange.chronos.itip.handler;

import static com.openexchange.java.Autoboxing.B;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.config.ConfigurationService;

/**
 * {@link ITipHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class })
public class ITipHandlerTest {

    private ITipHandler handler = new ITipHandler(null, null);

    @Mock
    private CalendarEvent calendarEvent;

    @Mock
    private CalendarParameters calendarParameters;

    @Mock
    private ConfigurationService configurationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(ConfigurationService.class)).thenReturn(configurationService);

        expectLegacyScheduling();
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
        Mockito.when(calendarParameters.get(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.class)).thenReturn(null);

        Assert.assertTrue(handler.shouldHandle(calendarEvent));
    }

    @Test
    public void testHandle_calendarParametersDoesNotContainSuppressItipAndFalse_process() {
        Mockito.when(calendarEvent.getCalendarParameters()).thenReturn(calendarParameters);
        Mockito.when(calendarParameters.get(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.class)).thenReturn(SchedulingControl.ALL);

        Assert.assertTrue(handler.shouldHandle(calendarEvent));
    }

    @Test
    public void testHandle_calendarParametersDoesNotContainSuppressItipAndTrue_return() {
        Mockito.when(calendarEvent.getCalendarParameters()).thenReturn(calendarParameters);
        Mockito.when(calendarParameters.get(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.class)).thenReturn(SchedulingControl.NONE);

        Assert.assertFalse(handler.shouldHandle(calendarEvent));
    }

    @Test
    public void testHandle_configurationServiceMissing_return() {
        expectLegacySchedulingDisabled();
        Assert.assertFalse(handler.shouldHandle(calendarEvent));
    }

    private void expectLegacyScheduling() {
        PowerMockito.when(B(configurationService.getBoolProperty("com.openexchange.calendar.useLegacyScheduling", false))).thenReturn(Boolean.TRUE);
    }

    private void expectLegacySchedulingDisabled() {
        PowerMockito.when(B(configurationService.getBoolProperty("com.openexchange.calendar.useLegacyScheduling", false))).thenReturn(Boolean.FALSE);
    }

}
