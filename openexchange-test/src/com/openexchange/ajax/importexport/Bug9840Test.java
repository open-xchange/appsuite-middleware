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
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug9840Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     *
     * @param name test name
     */
    public Bug9840Test() {
        super();
    }

    @Test
    public void testConversionErrorOnBYMONTH() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final ICalImportResponse iResponse = Executor.execute(client, new ICalImportRequest(folderId, ICAL, false));
        final ImportResult result = iResponse.getImports()[0];
        assertTrue("BYMONTH recurrence pattern not detected as error.", result.hasError());
        final OXException exception = result.getException();
        final CalendarExceptionCodes code = CalendarExceptionCodes.INVALID_RRULE;
        assertEquals(code.getNumber(), exception.getCode());
        assertEquals(code.getCategory(), exception.getCategory());
    }

    private static final String ICAL = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "BEGIN:VEVENT\n" + "SUMMARY:Everyday in January, for 3 years\n" + "DTSTART:20070101T090000\n" + "DURATION:PT30M\n" + "RRULE:FREQ=DAILY;UNTIL=20100131T090000Z;BYMONTH=1\n" + "DESCRIPTION:==> (2007 9:00 AM)January 1-31\n" + " (2008 9:00 AM)January 1-31\n" + "  (2009 9:00 AM)January 1-31\n" + "END:VEVENT\n" + "END:VCALENDAR\n";
}
