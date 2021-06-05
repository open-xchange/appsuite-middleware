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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link SummaryDescriptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class SummaryDescriptionTest extends AbstractDescriptionTest {

    private static final String OLD_SUMMARY = "Old summary";
    private static final String NEW_SUMMARY = "New summary";

    /**
     * Initializes a new {@link SummaryDescriptionTest}.
     */
    public SummaryDescriptionTest() {
        super(EventField.SUMMARY, "The appointment has a new subject", () -> {
            return new SummaryDescriber();
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        describer = new SummaryDescriber();
    }

    @Test
    public void testSummary_SetNewSummary_DescriptionAvailable() {
        setSummary(null, NEW_SUMMARY);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, NEW_SUMMARY);
    }

    @Test
    public void testSummary_removeSummary_DescriptionAvailable() {
        setSummary(OLD_SUMMARY, null);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, "");
    }

    @Test
    public void testSummary_ChangeSummary_DescriptionAvailable() {
        setSummary(OLD_SUMMARY, NEW_SUMMARY);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, NEW_SUMMARY);
    }

    // -------------------- HELPERS --------------------
    private void setSummary(String originalSummary, String updatedSummary) {
        PowerMockito.when(original.getSummary()).thenReturn(originalSummary);
        PowerMockito.when(updated.getSummary()).thenReturn(updatedSummary);
    }
}
