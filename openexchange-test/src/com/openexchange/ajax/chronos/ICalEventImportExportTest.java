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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ICalImportExportManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetType;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.InfoItemExport;

/**
 * {@link ICalEventImportExportTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ICalEventImportExportTest extends AbstractChronosTest {

    private List<EventData> getEventData(String fileName) throws Exception {
        String response = importICalFile(fileName);
        List<EventId> eventIds = importExportManager.parseImportJSONResponseToEventIds(response);
        eventManager.rememberEventIds(eventIds);
        return eventManager.listEvents(eventIds);
    }

    private String importICalFile(String fileName) throws Exception {
        Asset asset = assetManager.getAsset(AssetType.ics, fileName);
        return importExportManager.importICalFile(defaultUserApi.getSession(), folderId, new File(asset.getAbsolutePath()), true, false);
    }

    @Test
    public void testFolderEventExport() throws Exception {
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testCreateSingle"));
        String iCalEvent = importExportManager.exportICalFile(defaultUserApi.getSession(), expectedEventData.getFolder());
        assertNotNull(iCalEvent);
        assertTrue(iCalEvent.contains("testCreateSingle"));
    }

    @Test
    public void testBatchEventExport() throws Exception {
        List<EventData> eventData = new ArrayList<>();
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testBatchEvent1"));
        List<InfoItemExport> itemList = new ArrayList<>();

        addInfoItemExport(itemList, expectedEventData.getFolder(), expectedEventData.getId());
        eventData.add(expectedEventData);

        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.setTimeInMillis(System.currentTimeMillis());
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        end.setTimeInMillis(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));

        expectedEventData = eventManager.createEvent(EventFactory.createSingleEvent(defaultUserApi.getCalUser(), "testCreateSingle", DateTimeUtil.getDateTime(start), DateTimeUtil.getDateTime(end)));
        addInfoItemExport(itemList, expectedEventData.getFolder(), expectedEventData.getId());
        eventData.add(expectedEventData);

        String iCalExport = importExportManager.exportICalBatchFile(defaultUserApi.getSession(), itemList);
        assertNotNull(iCalExport);
        assertEventData(eventData, iCalExport);
    }


    private void addInfoItemExport(List<InfoItemExport> itemList, String folderId, String objectId) {
        InfoItemExport item = new InfoItemExport();
        item.folderId(folderId);
        item.id(objectId);
        itemList.add(item);
    }

    @Test
    public void testSingleEventImport() throws Exception {
        List<EventData> eventData = getEventData(ICalImportExportManager.SINGLE_IMPORT_ICS);
        assertEquals(1, eventData.size());
        assertEquals(ICalImportExportManager.SINGLE_IMPORT_ICS_SUMMARY, eventData.get(0).getSummary());
        assertEquals(ICalImportExportManager.SINGLE_IMPORT_ICS_UID, eventData.get(0).getUid());
    }

    @Test
    public void testSeriesEventImport() throws Exception {
        List<EventData> eventData = getEventData(ICalImportExportManager.SERIES_IMPORT_ICS);
        assertEquals(1, eventData.size());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_SUMMARY, eventData.get(0).getSummary());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_UID, eventData.get(0).getUid());
    }

    @Test//TODO fix
    public void testICalEventRecurrenceImport() throws Exception {
        List<EventData> eventData = getEventData(ICalImportExportManager.RECURRENCE_IMPORT_ICS);
        assertFalse(eventData.isEmpty());
        for (EventData event : eventData) {
            assertEquals(ICalImportExportManager.RECURRENCE_IMPORT_ICS_SUMMARY, event.getSummary());
            assertEquals(ICalImportExportManager.RECURRENCE_IMPORT_ICS_UID, event.getUid());
            if (!Strings.isEmpty(event.getRecurrenceId())) {
                assertEquals(ICalImportExportManager.RECURRENCE_IMPORT_ICS_RECURRENCE_ID, event.getRecurrenceId());
            }
        }
    }

    @Test
    public void testICalEventImportUIDHandling() throws Exception {
        getEventData(ICalImportExportManager.RECURRENCE_IMPORT_ICS);
        String response = importICalFile(ICalImportExportManager.RECURRENCE_IMPORT_ICS);
        assertTrue(response.contains("The event could not be created due to another conflicting event with the same unique identifier"));
    }

    @Test
    public void testICalImportExportRoundTrip() throws Exception {
        List<EventData> eventData = getEventData(ICalImportExportManager.SERIES_IMPORT_ICS);
        assertEquals(1, eventData.size());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_SUMMARY, eventData.get(0).getSummary());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_UID, eventData.get(0).getUid());

        List<InfoItemExport> itemList = new ArrayList<>();
        addInfoItemExport(itemList, eventData.get(0).getFolder(), eventData.get(0).getId());

        String iCalExport = importExportManager.exportICalBatchFile(defaultUserApi.getSession(), itemList);
        assertNotNull(iCalExport);
        assertEventData(eventData, iCalExport);
    }

    private void assertEventData(List<EventData> eventData, String iCalExport) {
        for (EventData event : eventData) {
            assertTrue(iCalExport.contains(event.getUid()));
            assertTrue(iCalExport.contains(event.getSummary()));
        }
    }
}
