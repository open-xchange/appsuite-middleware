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
