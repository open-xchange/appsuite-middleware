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
import static org.junit.Assert.assertNotNull;
import java.util.Collections;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.Conference;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.time.TimeTools;

/**
 * {@link MWB864Test}
 *
 * It's not possible to create appointment with conference with PIN
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class MWB864Test extends AbstractChronosTest {

    @Test
    public void testCreateConference() throws Exception {
        /*
         * generate event with conference and tel: URI
         */
        String conferenceUri = "tel:+4995518738302,,503676#,,598377#";
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        EventData eventData = new EventData();
        eventData.setFolder(folderId);
        eventData.setSummary("MWB864Test");
        eventData.setStartDate(DateTimeUtil.getDateTime(timeZone.getID(), TimeTools.D("Next monday at 10 am", timeZone).getTime()));
        eventData.setEndDate(DateTimeUtil.getDateTime(timeZone.getID(), TimeTools.D("Next monday at 11 am", timeZone).getTime()));
        Conference conference = new Conference();
        conference.setUri(conferenceUri);
        conference.setFeatures(Collections.singletonList("PHONE"));
        conference.setLabel("Cloud PBX conference");
        eventData.setConferences(Collections.singletonList(conference));
        /*
         * try and create event
         */
        EventData createdEvent = eventManager.createEvent(eventData, true);
        /*
         * reload event data and check stored conference
         */
        createdEvent = eventManager.getEvent(folderId, createdEvent.getId());
        assertNotNull(createdEvent.getConferences());
        assertEquals(1, createdEvent.getConferences().size());
        Conference createdConference = createdEvent.getConferences().get(0);
        assertEquals(conferenceUri, createdConference.getUri());
    }
}
