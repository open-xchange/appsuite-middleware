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

package com.openexchange.chronos.ical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.time.TimeTools;

/**
 * {@link Bug59654Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.1
 */
public class Bug59654Test extends ICalTest {

    @Test
    public void testNewLines() throws Exception {
        /*
         * prepare event with attendee comment
         */
        Event event = new Event();
        event.setUid(UUID.randomUUID().toString());
        event.setStartDate(new DateTime(TimeTools.D("next sunday at 12:30").getTime()));
        event.setEndDate(new DateTime(TimeTools.D("next sunday at 13:30").getTime()));
        ExtendedProperties extendedProperties = new ExtendedProperties();
        List<ExtendedPropertyParameter> parameters = new ArrayList<>();
        parameters.add(new ExtendedPropertyParameter("X-CALENDARSERVER-ATTENDEE-REF", "urn:uuid:00000001-0000-1b22-00fc-c0e11e000003"));
        parameters.add(new ExtendedPropertyParameter("X-CALENDARSERVER-DTSTAMP", "20180528T121803Z"));
        String comment = "First line\nSecond Line\nThird Line";
        extendedProperties.add(new ExtendedProperty("X-CALENDARSERVER-ATTENDEE-COMMENT", comment, parameters));
        event.setExtendedProperties(extendedProperties);
        /*
         * check attendee comment after multiple import/export roundtrips
         */
        String exportedICal = exportEvent(event);
        Event importedEvent = importEvent(exportedICal);
        for (int i = 0; i < 10; i++) {
            exportedICal = exportEvent(importedEvent);
            importedEvent = importEvent(exportedICal);
        }
        ExtendedProperties importedExtendedProperties = importedEvent.getExtendedProperties();
        assertNotNull(importedExtendedProperties);
        assertEquals(extendedProperties.get("X-CALENDARSERVER-ATTENDEE-COMMENT"), importedExtendedProperties.get("X-CALENDARSERVER-ATTENDEE-COMMENT"));
    }

}
