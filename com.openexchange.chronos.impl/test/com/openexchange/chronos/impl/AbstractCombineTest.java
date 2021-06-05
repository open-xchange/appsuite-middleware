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

import static com.openexchange.java.Autoboxing.I;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.junit.Before;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractCombineTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractCombineTest {

    protected CalendarAvailabilityStorage storage;
    protected CalendarSession session;
    protected List<Available> available;

    @Before
    public void setUp() throws OXException {
        available = new ArrayList<>();

        // Mock the session
        session = mock(CalendarSession.class);
        when(I(session.getUserId())).thenReturn(I(1));
        when(session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class)).thenReturn(TimeZone.getTimeZone("Europe/Berlin"));

        // Mock the storage
        storage = mock(CalendarAvailabilityStorage.class);
        when(storage.loadAvailable(1)).thenReturn(available);
    }
}
