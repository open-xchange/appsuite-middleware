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
import com.openexchange.ajax.chronos.factory.RRuleFactory;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * 
 * {@link Bug13447Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug13447Test extends AbstractChronosTest {

    public Bug13447Test() {
        super();
    }

    @Test
    public void testBug13447() throws Exception {
        
        EventData series = EventFactory.createSeriesEvent(getCalendaruser(), "Bug13447Test", 5, folderId);
        EventData createEvent = eventManager.createEvent(series, true);
        
        createEvent.setRrule(null);
        EventData single = eventManager.updateEvent(createEvent, false, false);
        
        single.setRrule(RRuleFactory.getFrequencyWithOccurenceLimit(RecurringFrequency.DAILY, 3));
        eventManager.updateEvent(single, false, false);
    }

}
