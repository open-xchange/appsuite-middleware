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
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11996Test extends AbstractAJAXSession {

    @Test
    public void testNotMatchingStatusAndPercentComplete() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final ICalImportResponse iResponse = Executor.execute(client, new ICalImportRequest(folderId, ICAL));
        final ImportResult result = iResponse.getImports()[0];
        final int objectId = Integer.parseInt(result.getObjectId());
        try {
            final GetResponse gResponse = Executor.execute(client, new GetRequest(folderId, objectId));
            assertFalse(gResponse.hasError());
        } finally {
            Executor.execute(client, new DeleteRequest(folderId, objectId, result.getDate()));
        }
    }

    private static final String ICAL = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "PRODID:-//Minter Software//EdgeDesk 4.03 MIMEDIR//EN\n" + "METHOD:REQUEST\n" + "BEGIN:VTODO\n" + "STATUS:COMPLETED\n" + "ORGANIZER:MAILTO:david12@maxoxtest.com\n" + "ATTENDEE;RSVP=TRUE:MAILTO:david13@maxoxtest.com\n" + "DTSTART:20080116T000000Z\n" + "DUE:20080716T000100Z\n" + "TRANSP:OPAQUE\n" + "UID:167791216223452@visualmail4.webmail10\n" + "DTSTAMP:20080812T215000Z\n" + "SUMMARY:Completed task\n" + "DESCRIPTION:Only finished 75% but it's done!\n" + "PRIORITY:9\n" + "PERCENT-COMPLETE:75\n" + "BEGIN:VALARM\n" + "TRIGGER:-PT\n" + "ACTION:DISPLAY\n" + "DESCRIPTION:Reminder\n" + "END:VALARM\n" + "END:VTODO\n" + "END:VCALENDAR";
}
