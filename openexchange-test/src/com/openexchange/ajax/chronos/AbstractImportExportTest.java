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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.ajax.chronos.manager.ICalImportExportManager;
import com.openexchange.java.Strings;
import com.openexchange.test.common.asset.Asset;
import com.openexchange.test.common.asset.AssetType;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.ContactListElement;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.InfoItemExport;
import com.openexchange.testing.httpclient.modules.ContactsApi;
import com.openexchange.testing.httpclient.modules.ExportApi;
import com.openexchange.testing.httpclient.modules.ImportApi;

/**
 * {@link AbstractImportExportTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class AbstractImportExportTest extends AbstractChronosTest {

    protected ICalImportExportManager importExportManager;

    protected ImportApi importApi;
    protected ExportApi exportApi;
    protected ContactsApi contactsApi;
    private Set<ContactListElement> contactsToDelete;
    private String contactsFolder = "";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        importApi = new ImportApi(getApiClient());
        exportApi = new ExportApi(getApiClient());
        contactsApi = new ContactsApi(getApiClient());

        importExportManager = new ICalImportExportManager(exportApi, importApi);
    }

    protected String getImportResponse(String fileName) throws Exception {
        return importICalFile(fileName);
    }

    protected List<EventData> parseEventData(String response) throws Exception {
        List<EventId> eventIds = importExportManager.parseImportJSONResponseToEventIds(response);
        eventManager.rememberEventIds(eventIds);
        return eventManager.listEvents(eventIds);
    }

    protected String importICalFile(String fileName) throws Exception {
        Asset asset = assetManager.getAsset(AssetType.ics, fileName);
        return importExportManager.importICalFile(defaultFolderId, new File(asset.getAbsolutePath()), Boolean.TRUE, Boolean.FALSE);
    }

    protected void createContactWithBirthdayEvent() throws Exception {
        if (Strings.isEmpty(contactsFolder)) {
            contactsFolder = getDefaultContactFolder();
        }
        rememberContact(contactsApi.createContact(createContactData()).getData().getId());
    }

    private ContactData createContactData() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        ContactData contact = new ContactData();
        contact.setFirstName("Peter");
        contact.setLastName("Paul Rubens"+UUID.randomUUID());
        contact.setBirthday(L(c.getTimeInMillis()));
        contact.setFolderId(contactsFolder);
        return contact;
    }

    protected void assertEventData(List<EventData> eventData, String iCalExport) {
        for (EventData event : eventData) {
            assertTrue(iCalExport.contains(event.getUid()));
            assertTrue(iCalExport.contains(event.getSummary()));
        }
    }

    protected void addInfoItemExport(List<InfoItemExport> itemList, String folderId, String objectId) {
        InfoItemExport item = new InfoItemExport();
        item.folderId(folderId);
        item.id(objectId);
        itemList.add(item);
    }

    protected void rememberContact(String contactId) {
        if (contactsToDelete == null) {
            contactsToDelete = new HashSet<>();
        }
        ContactListElement element = new ContactListElement();
        element.setFolder(contactsFolder);
        element.setId(contactId);
        contactsToDelete.add(element);
    }

}
