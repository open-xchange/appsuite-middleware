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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractImportExportTest;
import com.openexchange.ajax.chronos.manager.ICalImportExportManager;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.InfoItemExport;

/**
 * {@link MWB2Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.3
 */
public class MWB2Test extends AbstractImportExportTest {

    @Test
    public void testFloatingEvent() throws Exception {
        /*
        BEGIN:VCALENDAR
        VERSION:2.0
        METHOD:PUBLISH
        PRODID:Data::ICal 0.23
        BEGIN:VEVENT
        CATEGORIES:Travel\, Flight
        CLASS:PUBLIC
        DESCRIPTION:From: Cologne-Bonn
        DTEND:20191103T182500
        DTSTAMP:20191021T140425Z
        DTSTART:20191103T171500
        LOCATION:From Cologne-Bonn
        PRIORITY:5
        SEQUENCE:0
        SUMMARY:Flight to Berlin-Tegel
        TRANSP:OPAQUE
        UID:12345abcdef_CGNTXL
        END:VEVENT
        END:VCALENDAR
         */

        List<EventData> eventData = parseEventData(getImportResponse(ICalImportExportManager.FLOATING_ICS));
        assertEquals(1, eventData.size());

        EventData event = eventData.get(0);
        assertNull("No timezone expected.", event.getStartDate().getTzid());
        assertNull("No timezone expectedt.", event.getEndDate().getTzid());

        List<InfoItemExport> itemList = new ArrayList<>();
        addInfoItemExport(itemList, eventData.get(0).getFolder(), eventData.get(0).getId());

        String iCalExport = importExportManager.exportICalBatchFile(itemList);
        assertNotNull(iCalExport);
        assertEventData(eventData, iCalExport);

        assertTrue("Missing correct dtstart", iCalExport.contains("DTSTART:20191103T171500\r\n"));
        assertTrue("Missing correct dtend", iCalExport.contains("DTEND:20191103T182500\r\n"));
    }
}
