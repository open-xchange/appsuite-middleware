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

import static com.openexchange.ajax.chronos.manager.ICalImportExportManager.assertRecurrenceID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.InfoItemExport;

/**
 * {@link ICalEventImportExportTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ICalEventImportExportTest extends AbstractImportExportTest {

    @Test
    public void testFolderEventExport() throws Exception {
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser().intValue(), "testCreateSingle"), true);
        String iCalEvent = importExportManager.exportICalFile(expectedEventData.getFolder());
        assertNotNull(iCalEvent);
        assertTrue(iCalEvent.contains("testCreateSingle"));
    }

    @Test
    public void testBatchEventExport() throws Exception {
        List<EventData> eventData = new ArrayList<>();
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser().intValue(), "testBatchEvent1"), true);
        List<InfoItemExport> itemList = new ArrayList<>();

        addInfoItemExport(itemList, expectedEventData.getFolder(), expectedEventData.getId());
        eventData.add(expectedEventData);

        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        start.setTimeInMillis(System.currentTimeMillis());
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        end.setTimeInMillis(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));

        expectedEventData = eventManager.createEvent(EventFactory.createSingleEvent(defaultUserApi.getCalUser().intValue(), "testCreateSingle", DateTimeUtil.getDateTime(start), DateTimeUtil.getDateTime(end)), true);
        addInfoItemExport(itemList, expectedEventData.getFolder(), expectedEventData.getId());
        eventData.add(expectedEventData);

        String iCalExport = importExportManager.exportICalBatchFile(itemList);
        assertNotNull(iCalExport);
        assertEventData(eventData, iCalExport);
    }

    @Test
    public void testSingleEventImport() throws Exception {

        List<EventData> eventData = parseEventData(getImportResponse(ICalImportExportManager.SINGLE_IMPORT_ICS));
        assertEquals(1, eventData.size());
        assertEquals(ICalImportExportManager.SINGLE_IMPORT_ICS_SUMMARY, eventData.get(0).getSummary());
        assertEquals(ICalImportExportManager.SINGLE_IMPORT_ICS_UID, eventData.get(0).getUid());
    }

    @Test
    public void testSeriesEventImport() throws Exception {
        List<EventData> eventData = parseEventData(getImportResponse(ICalImportExportManager.SERIES_IMPORT_ICS));
        assertEquals(1, eventData.size());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_SUMMARY, eventData.get(0).getSummary());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_UID, eventData.get(0).getUid());
    }

    @Test
    public void testICalEventRecurrenceImport() throws Exception {
        List<EventData> eventData = parseEventData(getImportResponse(ICalImportExportManager.RECURRENCE_IMPORT_ICS));
        assertFalse(eventData.isEmpty());
        for (EventData event : eventData) {
            assertEquals(ICalImportExportManager.RECURRENCE_IMPORT_ICS_SUMMARY, event.getSummary());
            assertEquals(ICalImportExportManager.RECURRENCE_IMPORT_ICS_UID, event.getUid());
            if (Strings.isNotEmpty(event.getRecurrenceId())) {
                assertRecurrenceID(ICalImportExportManager.RECURRENCE_IMPORT_ICS_RECURRENCE_ID, event.getRecurrenceId());
            }
        }
    }

    @Test
    public void testICalEventImportUIDHandling() throws Exception {
        parseEventData(getImportResponse(ICalImportExportManager.RECURRENCE_IMPORT_ICS));
        String response = importICalFile(ICalImportExportManager.RECURRENCE_IMPORT_ICS);
        assertTrue(response.contains("The appointment could not be created due to another conflicting appointment with the same unique identifier"));
    }

    @Test
    public void testICalImportExportRoundTrip() throws Exception {
        List<EventData> eventData = parseEventData(getImportResponse(ICalImportExportManager.SERIES_IMPORT_ICS));
        assertEquals(1, eventData.size());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_SUMMARY, eventData.get(0).getSummary());
        assertEquals(ICalImportExportManager.SERIES_IMPORT_ICS_UID, eventData.get(0).getUid());

        List<InfoItemExport> itemList = new ArrayList<>();
        addInfoItemExport(itemList, eventData.get(0).getFolder(), eventData.get(0).getId());

        String iCalExport = importExportManager.exportICalBatchFile(itemList);
        assertNotNull(iCalExport);
        assertEventData(eventData, iCalExport);
    }
}
