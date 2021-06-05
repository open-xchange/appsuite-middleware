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
import com.openexchange.ajax.UserTest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.resource.actions.ResourceDeleteRequest;
import com.openexchange.ajax.resource.actions.ResourceNewRequest;
import com.openexchange.ajax.resource.actions.ResourceNewResponse;
import com.openexchange.ajax.user.actions.SearchRequest;
import com.openexchange.ajax.user.actions.SearchResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.resource.Resource;
import com.openexchange.user.User;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11868Test extends AbstractAJAXSession {

    /**
     * Checks if a whole day appointment is imported properly.
     */
    @Test
    public void testWholeDayAppointment() throws Throwable {
        final AJAXClient client = getClient();
        String ical;
        {
            final ContactSearchObject search = new ContactSearchObject();
            search.setPattern("*");
            search.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
            final SearchRequest request = new SearchRequest(search, UserTest.CONTACT_FIELDS);
            final SearchResponse response = Executor.execute(client, request);
            final User[] user = response.getUser();
            ical = ICAL.replace("@email1@", user[0].getMail()).replace("@email2@", user[1].getMail()).replace("@email3@", user[2].getMail());
        }
        final Resource resource = new Resource();
        {
            final long different = System.currentTimeMillis();
            resource.setDisplayName("Bug 11868 test resource" + different);
            resource.setSimpleName("Bug 11868 test resource " + different);
            resource.setMail("bug11868testresource" + different + "@example.org");
            final ResourceNewResponse response = Executor.execute(client, new ResourceNewRequest(resource));
            resource.setIdentifier(response.getID());
            resource.setLastModified(response.getTimestamp());
            ical = ical.replace("@resource1@", resource.getDisplayName());
        }
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final ICalImportResponse iResponse = Executor.execute(client, new ICalImportRequest(folderId, ical));
        final ImportResult result = iResponse.getImports()[0];
        final int objectId = Integer.parseInt(result.getObjectId());
        try {
            final GetResponse gResponse = Executor.execute(client, new GetRequest(folderId, objectId));
            assertFalse(gResponse.hasError());
        } finally {
            Executor.execute(client, new DeleteRequest(objectId, folderId, result.getDate()));
            Executor.execute(client, new ResourceDeleteRequest(resource));
        }
    }

    private static final String ICAL = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "PRODID:OPEN-XCHANGE\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20080807T103300Z\n" + "SUMMARY:Hosting Call\n" + "DESCRIPTION:Recreated due to several reasons\n" + "DTSTART:20080225T130000Z\n" + "DTEND:20080225T140000Z\n" + "CLASS:PUBLIC\n" + "LOCATION:TelCo1\n" + "TRANSP:OPAQUE\n" + "ATTENDEE:MAILTO:@email1@\n" + "ATTENDEE:MAILTO:@email2@\n" + "ATTENDEE:MAILTO:@email3@\n" + "RESOURCES:@resource1@\n" + "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO\n" + "END:VEVENT\n" + "END:VCALENDAR\n";
}
