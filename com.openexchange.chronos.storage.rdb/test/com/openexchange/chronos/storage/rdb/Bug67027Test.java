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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.sql.ResultSet;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.storage.rdb.legacy.EventMapper;
import com.openexchange.groupware.tools.mappings.database.DbMultiMapping;

/**
 * {@link Bug67027Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug67027Test {

    @Test
    public void testOrganizerFromResultSet() throws Exception {
        /*
         * mock resultset yielding bogus organizer/principal data
         */
        String[] columnLabels = new String[] { "organizer", "organizerId", "principal", "principalId" };
        String storedOrganizer = "otto@example.com";
        int storedOrganizerId = 71;
        String storedPrincipal = "otto@example.com";
        int storedPrincipalId = 0;
        ResultSet resultSet = PowerMockito.mock(ResultSet.class);
        PowerMockito.when(resultSet.getString(columnLabels[0])).thenReturn(storedOrganizer);
        PowerMockito.when(I(resultSet.getInt(columnLabels[1]))).thenReturn(I(storedOrganizerId));
        PowerMockito.when(resultSet.getString(columnLabels[2])).thenReturn(storedPrincipal);
        PowerMockito.when(I(resultSet.getInt(columnLabels[3]))).thenReturn(I(storedPrincipalId));
        /*
         * read from resultset & assign organizer in event
         */
        Event event = new Event();
        DbMultiMapping<? extends Object, Event> dbMapping = (DbMultiMapping<? extends Object, Event>) EventMapper.getInstance().get(EventField.ORGANIZER);
        dbMapping.set(resultSet, event, columnLabels);
        /*
         * verify organizer
         */
        Organizer organizer = event.getOrganizer();
        assertNotNull(organizer);
        assertEquals(organizer.getUri(), CalendarUtils.getURI(storedOrganizer));
        assertEquals(organizer.getEntity(), storedOrganizerId);
        assertNull(organizer.getSentBy());
    }
}
