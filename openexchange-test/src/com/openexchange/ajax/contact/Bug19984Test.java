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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;

public class Bug19984Test extends ManagedAppointmentTest {

    public Bug19984Test() {
        super();
    }

    String ical = "BEGIN:VCALENDAR\n" + "BEGIN:VEVENT\n" + "DTSTART:20110726T183000\n" + "DTEND:20110726T200000\n" + "LOCATION;ENCODING=QUOTED-PRINTABLE:DLRG-Heim\n" + "CATEGORIES;ENCODING=QUOTED-PRINTABLE:DLRG WRD\n" + "DESCRIPTION;CHARSET=ISO-8859-1;ENCODING=QUOTED-PRINTABLE:Liebe Einsatzkr\u00e4fte,=0A=0Awir laden ein zum Wasserretter-Treff. Dieser findet alle vier Wochen statt. Neben der Einteilung f\u00fcr den Wachdienst werden auch aktuelle Themen, wie Eins\u00e4tze, abgearbeitet oder auch nur kleine Ausbildungsinhalte aus dem Bereich Fachausbildung Wasserrettung vermittelt.=0A=0AWir freuen uns daher \u00fcber eine zahlreiche Teilnahme!=0A=0AEingeladen sind alle ab Rettungsschwimmabzeichen Bronze!!!\n" + "SUMMARY;ENCODING=QUOTED-PRINTABLE:Wasserretter-Treff [OG\u00a0Hirschaid]\n" + "PRIORITY:3\n" + "END:VEVENT\n" + "END:VCALENDAR";

    @Test
    public void testIt() throws Exception {
        ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical);
        ICalImportResponse response = getClient().execute(request);
        // System.out.println(response.getData());
        assertFalse(System.getProperty("line.separator") + response.getData(), response.hasError());
    }
}
