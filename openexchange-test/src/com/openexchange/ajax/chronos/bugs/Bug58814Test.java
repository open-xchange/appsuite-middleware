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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.EventFactory.Weekday;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.manager.ICalImportExportManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetType;
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

    /**
     * Create a non-full day series with no time and no timezone information on the 'until' property
     * of the recurrence frequency and change it to a full day series.
     * 
     * @throws ApiException
     * @throws ParseException
     * @throws ChronosApiException
     * @throws InvalidRecurrenceRuleException
     */
    @Test
    public void testNonFullDayToFullDay() throws ApiException, ParseException, ChronosApiException, InvalidRecurrenceRuleException {
        RecurringFrequency frequency = RecurringFrequency.WEEKLY;
        Weekday weekday = Weekday.MO;
        long now = System.currentTimeMillis();
        long untilTimestamp = now + TimeUnit.DAYS.toMillis(31);
        DateTimeData startDate = DateTimeUtil.getDateTime(now);
        DateTimeData endDate = DateTimeUtil.getDateTime(now + TimeUnit.HOURS.toMillis(2));
        DateTimeData until = DateTimeUtil.getDateTimeWithoutTimeInformation(untilTimestamp);
        // Create the non-full day event series
        EventData event = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), "Bug 58814 - testNonFullDayToFullDay", startDate, endDate, until, frequency, weekday, defaultFolderId);
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
     * @throws ParseException
     * @throws ChronosApiException
     * @throws InvalidRecurrenceRuleException
     */
    @Test
    public void testFullDayToNonFullDay() throws ApiException, ParseException, ChronosApiException {
        RecurringFrequency frequency = RecurringFrequency.WEEKLY;
        Weekday weekday = Weekday.FR;
        long now = System.currentTimeMillis();
        long untilTimestamp = now + TimeUnit.DAYS.toMillis(31);
        DateTimeData startDate = DateTimeUtil.getDateTimeWithoutTimeInformation(now);
        DateTimeData endDate = DateTimeUtil.getDateTimeWithoutTimeInformation(now + TimeUnit.HOURS.toMillis(2));
        DateTimeData until = DateTimeUtil.getDateTime(untilTimestamp);
        DateTimeData expectedUntil = DateTimeUtil.getDateTimeWithoutTimeInformation(untilTimestamp);
        // Create the full day event series
        EventData event = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), "Bug 58814 - testFullDayToNonFullDay", startDate, endDate, until, frequency, weekday, defaultFolderId);
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
        String response = importExportManager.importICalFile(defaultUserApi.getSession(), defaultFolderId, new File(asset.getAbsolutePath()), true, false);

        List<EventId> eventIds = importExportManager.parseImportJSONResponseToEventIds(response);
        eventManager.rememberEventIds(eventIds);
        List<EventData> eventData = eventManager.listEvents(eventIds);
        assertEquals(4, eventData.size());

        //TODO: assert data
    }
}
