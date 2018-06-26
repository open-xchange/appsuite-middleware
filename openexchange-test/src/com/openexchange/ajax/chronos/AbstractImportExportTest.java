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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.ajax.chronos.manager.ICalImportExportManager;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetType;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactData;
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
        return importExportManager.importICalFile(defaultUserApi.getSession(), defaultFolderId, new File(asset.getAbsolutePath()), true, false);
    }

    protected void createContactWithBirthdayEvent(String session) throws ApiException {
        contactsApi.createContact(session, createContactData());
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
        contact.setSecondName("Paul"+UUID.randomUUID());
        contact.setBirthday(c.getTimeInMillis());

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

}
