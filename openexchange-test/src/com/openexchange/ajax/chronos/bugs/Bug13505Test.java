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

import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.EventFactory.Weekday;
import com.openexchange.ajax.chronos.factory.RRuleFactory;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug13505Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug13505Test extends AbstractChronosTest {

    public Bug13505Test() {
        super();
    }

    @Test
    public void testBug13505() throws Exception {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testBug13505", folderId);
        event.setRrule(RRuleFactory.getFrequencyWithOccurenceLimit(RecurringFrequency.WEEKLY, 3, Weekday.MO));
        EventData createEvent = eventManager.createEvent(event, true);

        createEvent.setRrule(RRuleFactory.getFrequencyWithOccurenceLimit(RecurringFrequency.MONTHLY, 3));
        eventManager.updateEvent(createEvent, false, false);
    }

}
