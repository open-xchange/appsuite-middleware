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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import org.junit.Test;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.task.ManagedTaskTest;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link Bug8654Test_TaskImport}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class Bug8654Test_TaskImport extends ManagedTaskTest {

    final String ical =
        "BEGIN:VCALENDAR\n"
        + "VERSION:2.0\n"
        + "X-WR-CALNAME:Test\n"
        + "PRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\n"
        + "X-WR-RELCALID:F1D0AAC4-A28F-41E1-9FA8-83546CE7D7B8\n"
        + "X-WR-TIMEZONE:Europe/Berlin\n" + "CALSCALE:GREGORIAN\n"
        + "METHOD:PUBLISH\n"
        + "BEGIN:VTIMEZONE\n"
        + "TZID:Europe/Berlin\n"
        + "LAST-MODIFIED:20070801T101420Z\n"
        + "BEGIN:DAYLIGHT\n"
        + "DTSTART:20070325T010000\n"
        + "TZOFFSETTO:+0200\n"
        + "TZOFFSETFROM:+0000\n"
        + "TZNAME:CEST\n"
        + "END:DAYLIGHT\n"
        + "BEGIN:STANDARD\n"
        + "DTSTART:20071028T030000\n"
        + "TZOFFSETTO:+0100\n"
        + "TZOFFSETFROM:+0200\n"
        + "TZNAME:CET\n"
        + "END:STANDARD\n"
        + "END:VTIMEZONE\n"
        + "BEGIN:VTODO\n"
        + "PRIORITY:5\n"
        + "DTSTAMP:20070801T101401Z\n"
        + "UID:C037CF41-BB61-4BF8-A77A-459D2B754A32\n"
        + "SEQUENCE:4\n"
        + "URL;VALUE=URI:http://www.open-xchange.com\n"
        + "DTSTART;TZID=Europe/Berlin:20070801T000000\n"
        + "SUMMARY:Teste Task Import von OX\n"
        + "DESCRIPTION:Test\\, ob die Aufgaben auch ankommen.\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    @Test
    public void testTaskImport() throws Exception {
        final ICalImportRequest request = new ICalImportRequest(folderID, new ByteArrayInputStream(ical.toString().getBytes(com.openexchange.java.Charsets.UTF_8)), false);
        ICalImportResponse response = getClient().execute(request);
        assertEquals(1, response.getImports().length);

        String objectId = response.getImports()[0].getObjectId();
        Task task = ttm.getTaskFromServer(folderID, Integer.parseInt(objectId));
        assertNotNull(task);
        assertEquals("Teste Task Import von OX", task.getTitle());
    }

}
