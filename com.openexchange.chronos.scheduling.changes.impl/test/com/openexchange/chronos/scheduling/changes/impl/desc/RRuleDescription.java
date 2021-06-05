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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RRuleDescription}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class RRuleDescription extends AbstractDescriptionTest {

    private static final String DAILY_COUNT = "FREQ=DAILY;COUNT=10";
    private static final String DAILY = "FREQ=DAILY";

    @Mock
    private static ServiceLookup services;

    /**
     * Initializes a new {@link RRuleDescription}.
     */
    public RRuleDescription() {
        super(EventField.RECURRENCE_RULE, "The appointment's recurrence rule has changed to", () -> {
            return new RRuleDescriber(services);
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.spy(CalendarUtils.class);
        describer = new RRuleDescriber(services);
    }

    @Test
    public void testSummary_SetNewSummary_DescriptionAvailable() {
        setRRule(null, DAILY);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, "Every 1 day, No end");
    }

//    @Test
//    public void testSummary_removeSummary_DescriptionAvailable() {
//        setRRule(DAILY_COUNT, null);
//
//        Description description = describer.describe(eventUpdate);
//        testDescription(description);
//        checkMessage(description, "daily");
//    }

    @Test
    public void testSummary_ChangeSummary_DescriptionAvailable() {
        setRRule(DAILY, DAILY_COUNT);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, "Every 1 day, Occurs 10 times");
    }

    // -------------------- HELPERS --------------------

    private void setRRule(String originalRR, String updatedRR) {
        PowerMockito.when(original.getRecurrenceRule()).thenReturn(originalRR);
        PowerMockito.when(updated.getRecurrenceRule()).thenReturn(updatedRR);
    }

}
