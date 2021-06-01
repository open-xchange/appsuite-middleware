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

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11724Test extends AbstractAJAXSession {

    /**
     * Checks if a whole day appointment is imported properly.
     */
    @Test
    public void testWholeDayAppointment() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final ICalImportResponse iResponse = Executor.execute(client, new ICalImportRequest(folderId, ICAL));
        final ImportResult result = iResponse.getImports()[0];
        final int objectId = Integer.parseInt(result.getObjectId());
        try {
            final GetResponse gResponse = Executor.execute(client, new GetRequest(folderId, objectId));
            assertFalse(gResponse.hasError());
        } finally {
            Executor.execute(client, new DeleteRequest(objectId, folderId, result.getDate()));
        }
    }

    private static final String ICAL = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "PRODID:OPEN-XCHANGE\n" + "BEGIN:VEVENT\n" + "CLASS:PUBLIC\n" + "CREATED:20080728T200752Z\n" + "DESCRIPTION:fasel\n" + "DTSTART;VALUE=DATE:20080728\n" + "LAST-MODIFIED:20080728T200755Z\n" + "ORGANIZER:mailto:user3@oxtest41.de\n" + "DTSTAMP:20080728T200853Z\n" + "SUMMARY:ganztag\n" + "TRANSP:OPAQUE\n" + "UID:21@localhost\n" + "END:VEVENT\n" + "END:VCALENDAR\n";
}
