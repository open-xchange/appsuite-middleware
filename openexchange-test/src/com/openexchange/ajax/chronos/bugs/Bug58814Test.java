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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.EventFactory.Weekday;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.manager.ICalImportExportManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.test.common.asset.Asset;
import com.openexchange.test.common.asset.AssetType;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.modules.ExportApi;
import com.openexchange.testing.httpclient.modules.ImportApi;

/**
 * {@link Bug58814Test} - Tests for <a href="https://bugs.open-xchange.com/show_bug.cgi?id=58814">Bug 58814</a>
 * - .ics import fails with 'The specified recurrence rule is invalid'
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class Bug58814Test extends AbstractChronosTest {

    /**
     * Initialises a new {@link Bug58814Test}.
     */
    public Bug58814Test() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        eventManager.setIgnoreConflicts(true);
    }

    /**
     * Create a non-full day series with no time and no timezone information on the 'until' property
     * of the recurrence frequency and change it to a full day series.
     *
     * @throws ApiException
     * @throws ParseException
     * @throws ChronosApiException
     */
    @Test
    public void testNonFullDayToFullDay() throws ApiException, ParseException, ChronosApiException {
        RecurringFrequency frequency = RecurringFrequency.WEEKLY;
        Weekday weekday = Weekday.MO;
        long now = System.currentTimeMillis();
        long untilTimestamp = now + TimeUnit.DAYS.toMillis(31);
        DateTimeData startDate = DateTimeUtil.getDateTime(now);
        DateTimeData endDate = DateTimeUtil.getDateTime(now + TimeUnit.HOURS.toMillis(2));
        DateTimeData until = DateTimeUtil.getDateTimeWithoutTimeInformation(untilTimestamp);
        // Create the non-full day event series
        EventData event = EventFactory.createSeriesEvent(getCalendaruser(), "Bug 58814 - testNonFullDayToFullDay", startDate, endDate, until, frequency, weekday, defaultFolderId);
        EventData createdEvent = eventManager.createEvent(event);

        // Ensure that the 'until' part was successfully adjusted to match the 'start' part type
        String expectedRR = "FREQ=" + frequency.name() + ";BYDAY=" + weekday.name() + ";UNTIL=" + until.getValue() + "T000000Z";
        String actualRR = createdEvent.getRrule();
        Assert.assertEquals(expectedRR, actualRR);

        // Switch to full day event (timezone and hour information absent)
        createdEvent.setStartDate(DateTimeUtil.stripTimeInformation(createdEvent.getStartDate()));
        createdEvent.setEndDate(DateTimeUtil.stripTimeInformation(createdEvent.getEndDate()));
        EventData updatedEvent = eventManager.updateEvent(createdEvent);

        // Ensure that the 'until' part was successfully adjusted to match the 'start' part type
        expectedRR = "FREQ=" + frequency.name() + ";BYDAY=" + weekday.name() + ";UNTIL=" + until.getValue();
        actualRR = updatedEvent.getRrule();
        Assert.assertEquals(expectedRR, actualRR);
    }

    /**
     * Create a full day series and change it to a non-full day series.
     *
     * @throws ApiException
     * @throws ChronosApiException
     */
    @Test
    public void testFullDayToNonFullDay() throws ApiException, ChronosApiException {
        RecurringFrequency frequency = RecurringFrequency.WEEKLY;
        Weekday weekday = Weekday.FR;
        long now = System.currentTimeMillis();
        long untilTimestamp = now + TimeUnit.DAYS.toMillis(31);
        DateTimeData startDate = DateTimeUtil.getDateTimeWithoutTimeInformation(now);
        DateTimeData endDate = DateTimeUtil.getDateTimeWithoutTimeInformation(now + TimeUnit.HOURS.toMillis(2));
        DateTimeData until = DateTimeUtil.getDateTime(untilTimestamp);
        DateTimeData expectedUntil = DateTimeUtil.getDateTimeWithoutTimeInformation(untilTimestamp);
        // Create the full day event series
        EventData event = EventFactory.createSeriesEvent(getCalendaruser(), "Bug 58814 - testFullDayToNonFullDay", startDate, endDate, until, frequency, weekday, defaultFolderId);
        EventData createdEvent = eventManager.createEvent(event);

        // Ensure that the 'until' part was successfully adjusted to match the 'start' part type
        String expectedRR = "FREQ=" + frequency.name() + ";BYDAY=" + weekday.name() + ";UNTIL=" + expectedUntil.getValue();
        String actualRR = createdEvent.getRrule();
        Assert.assertEquals(expectedRR, actualRR);

        // Switch to non-full day event (timezone and hour information present)
        createdEvent.setStartDate(DateTimeUtil.getDateTime(now));
        createdEvent.setEndDate(DateTimeUtil.getDateTime(now + TimeUnit.HOURS.toMillis(2)));
        EventData updatedEvent = eventManager.updateEvent(createdEvent);

        // Ensure that the 'until' part was successfully adjusted to match the 'start' part type
        expectedRR = "FREQ=" + frequency.name() + ";BYDAY=" + weekday.name() + ";UNTIL=" + expectedUntil.getValue() + "T000000Z";
        actualRR = updatedEvent.getRrule();
        Assert.assertEquals(expectedRR, actualRR);
    }

    /**
     * Test <code>.ics</code> file import with invalid RFC start/end/until combos
     */
    @Test
    public void testSeriesEventImport() throws Exception {
        ImportApi importApi = new ImportApi(getApiClient());
        ExportApi exportApi = new ExportApi(getApiClient());
        ICalImportExportManager importExportManager = new ICalImportExportManager(exportApi, importApi);

        Asset asset = assetManager.getAsset(AssetType.ics, "bug58814.ics");
        String response = importExportManager.importICalFile(defaultFolderId, new File(asset.getAbsolutePath()), Boolean.TRUE, Boolean.FALSE);

        List<EventId> eventIds = importExportManager.parseImportJSONResponseToEventIds(response);
        eventManager.rememberEventIds(eventIds);
        List<EventData> eventDataList = eventManager.listEvents(eventIds);
        assertEquals("Expected 4 events but response was: " + response, 4, eventDataList.size());

        EventData eventData = eventDataList.get(0);
        assertEquals("Test Bug 58814 - Recurrence Check No-Full-Day", eventData.getSummary());
        assertEquals(DateTimeUtil.getDateTimeData("20140604T170000", "America/New_York"), eventData.getStartDate());
        assertEquals(DateTimeUtil.getDateTimeData("20140604T200000", "America/New_York"), eventData.getEndDate());
        assertEquals("FREQ=WEEKLY;UNTIL=20140924T210000Z;BYDAY=WE", eventData.getRrule());

        eventData = eventDataList.get(1);
        assertEquals("Test Bug 58814 - Recurrence Check Full-Day", eventData.getSummary());
        assertEquals(DateTimeUtil.getDateTimeData("20100826", null), eventData.getStartDate());
        assertEquals(DateTimeUtil.getDateTimeData("20100827", null), eventData.getEndDate());
        assertEquals("FREQ=DAILY;WKST=SU;UNTIL=20100905", eventData.getRrule());

        eventData = eventDataList.get(2);
        assertEquals("Test Bug 58814 - Recurrence Check Start/End Full-Day, Recurrence Until No-Full-Day", eventData.getSummary());
        assertEquals(DateTimeUtil.getDateTimeData("20081208", null), eventData.getStartDate());
        assertEquals(DateTimeUtil.getDateTimeData("20081209", null), eventData.getEndDate());
        assertEquals("FREQ=DAILY;UNTIL=20081214", eventData.getRrule());

        eventData = eventDataList.get(3);
        assertEquals("Test Bug 58814 - Recurrence Check Start/End No-Full-Day, Recurrence Until Full-Day", eventData.getSummary());
        assertEquals(DateTimeUtil.getDateTimeData("20100406T133000", "America/New_York"), eventData.getStartDate());
        assertEquals(DateTimeUtil.getDateTimeData("20100406T143000", "America/New_York"), eventData.getEndDate());
        assertEquals("FREQ=DAILY;WKST=SU;UNTIL=20100421T000000Z", eventData.getRrule());
    }
}
