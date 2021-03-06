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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.AssertUtil;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link AbstractAlarmTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(BlockJUnit4ClassRunner.class)
abstract class AbstractAlarmTest extends AbstractChronosTest {

    /**
     * Gets and checks if the specified event exists and if it contains the expected amount of alarms
     *
     * @param expectedEventData The event to get and check
     * @param amountOfExpectedAlarms The excepted amount of alarms
     * @return The actual EventData
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    EventData getAndAssertAlarms(EventData expectedEventData, int amountOfExpectedAlarms, String folderId) throws ApiException, ChronosApiException {
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        assertNotNull(actualEventData.getAlarms());
        assertEquals(amountOfExpectedAlarms, actualEventData.getAlarms().size());
        return actualEventData;
    }
}
