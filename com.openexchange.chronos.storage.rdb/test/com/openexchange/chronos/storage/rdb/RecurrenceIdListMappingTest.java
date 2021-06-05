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

package com.openexchange.chronos.storage.rdb;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceId;

/**
 * {@link RecurrenceIdListMappingTest}
 * 
 * Tests (de-)serialization routines for properties holding recurrence identifiers in different format.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class RecurrenceIdListMappingTest {

    @Test
    public void testSerializeRecurrenceIds() {
        /*
         * UTC
         */
        RecurrenceId[] recurrenceIds = new RecurrenceId[] {
            new DefaultRecurrenceId("20180109T180000Z"),
            new DefaultRecurrenceId("20180111T180000Z")
        };
        String value = RecurrenceIdListMapping.serialize(Arrays.asList(recurrenceIds));
        SortedSet<RecurrenceId> deserializedRecurrenceIds = RecurrenceIdListMapping.deserialize(value);
        Iterator<RecurrenceId> iterator = deserializedRecurrenceIds.iterator();
        assertEquals(recurrenceIds[0], iterator.next());
        assertEquals(recurrenceIds[1], iterator.next());
        /*
         * with timezone reference, normalized
         */
        recurrenceIds = new RecurrenceId[] {
            new DefaultRecurrenceId("20180109T180000", TimeZone.getTimeZone("Asia/Tokyo")),
            new DefaultRecurrenceId("20180111T180000", TimeZone.getTimeZone("Asia/Tokyo"))
        };
        value = RecurrenceIdListMapping.serialize(Arrays.asList(recurrenceIds));
        deserializedRecurrenceIds = RecurrenceIdListMapping.deserialize(value);
        iterator = deserializedRecurrenceIds.iterator();
        assertEquals(recurrenceIds[0], iterator.next());
        assertEquals(recurrenceIds[1], iterator.next());
        /*
         * with timezone reference, mixed
         */
        recurrenceIds = new RecurrenceId[] {
            new DefaultRecurrenceId("20191209T120000", TimeZone.getTimeZone("Europe/Berlin")),
            new DefaultRecurrenceId("20191210T060000", TimeZone.getTimeZone("America/New_York")),
            new DefaultRecurrenceId("20191211T200000", TimeZone.getTimeZone("Asia/Tokyo")),
        };
        value = RecurrenceIdListMapping.serialize(Arrays.asList(recurrenceIds));
        deserializedRecurrenceIds = RecurrenceIdListMapping.deserialize(value);
        iterator = deserializedRecurrenceIds.iterator();
        assertEquals(recurrenceIds[0], iterator.next());
        assertEquals(recurrenceIds[1], iterator.next());
        assertEquals(recurrenceIds[2], iterator.next());
        /*
         * floating
         */
        recurrenceIds = new RecurrenceId[] {
            new DefaultRecurrenceId("20180109T180000", (TimeZone) null),
            new DefaultRecurrenceId("20180111T180000", (TimeZone) null)
        };
        value = RecurrenceIdListMapping.serialize(Arrays.asList(recurrenceIds));
        deserializedRecurrenceIds = RecurrenceIdListMapping.deserialize(value);
        iterator = deserializedRecurrenceIds.iterator();
        assertEquals(recurrenceIds[0], iterator.next());
        assertEquals(recurrenceIds[1], iterator.next());
        /*
         * date
         */
        recurrenceIds = new RecurrenceId[] {
            new DefaultRecurrenceId("20180109", (TimeZone) null),
            new DefaultRecurrenceId("20180111", (TimeZone) null)
        };
        value = RecurrenceIdListMapping.serialize(Arrays.asList(recurrenceIds));
        deserializedRecurrenceIds = RecurrenceIdListMapping.deserialize(value);
        iterator = deserializedRecurrenceIds.iterator();
        assertEquals(recurrenceIds[0], iterator.next());
        assertEquals(recurrenceIds[1], iterator.next());
    }

    @Test
    public void testDeserializeOldRecurrenceIds() {
        /*
         * UTC
         */
        String value = "20180109T180000Z, 20180111T180000Z";
        DateTime dateTime1 = new DateTime(TimeZone.getTimeZone("UTC"), 2018, 0, 9, 18, 0, 0);
        DateTime dateTime2 = new DateTime(TimeZone.getTimeZone("UTC"), 2018, 0, 11, 18, 0, 0);
        SortedSet<RecurrenceId> recurrenceIds = RecurrenceIdListMapping.deserialize(value);
        Iterator<RecurrenceId> iterator = recurrenceIds.iterator();
        assertEquals(dateTime1, iterator.next().getValue());
        assertEquals(dateTime2, iterator.next().getValue());
        /*
         * floating
         */
        value = "20180109T180000, 20180111T180000";
        dateTime1 = new DateTime(2018, 0, 9, 18, 0, 0);
        dateTime2 = new DateTime(2018, 0, 11, 18, 0, 0);
        recurrenceIds = RecurrenceIdListMapping.deserialize(value);
        iterator = recurrenceIds.iterator();
        assertEquals(dateTime1, iterator.next().getValue());
        assertEquals(dateTime2, iterator.next().getValue());
        /*
         * date
         */
        value = "20180109, 20180111";
        dateTime1 = new DateTime(2018, 0, 9);
        dateTime2 = new DateTime(2018, 0, 11);
        recurrenceIds = RecurrenceIdListMapping.deserialize(value);
        iterator = recurrenceIds.iterator();
        assertEquals(dateTime1, iterator.next().getValue());
        assertEquals(dateTime2, iterator.next().getValue());
    }

}
