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
 * {@link DescriptionDescriptionTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class DescriptionDescriptionTest extends AbstractDescriptionTest {

    private static final String OLD_DESCRIPTION = "Old description";
    private static final String NEW_DESCRIPTION = "New description";

    /**
     * Initializes a new {@link DescriptionDescriptionTest}.
     */
    public DescriptionDescriptionTest() {
        super(EventField.DESCRIPTION, "The appointment description has changed", () -> {
            return new DescriptionDescriber();
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        describer = new DescriptionDescriber();
    }

    @Test
    public void testDescription_SetNewDescription_DescriptionAvailable() {
        setDescription(null, NEW_DESCRIPTION);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description);
    }

    @Test
    public void testDescription_removeDescription_DescriptionAvailable() {
        setDescription(OLD_DESCRIPTION, null);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description);
    }

    @Test
    public void testDescription_ChangeDescription_DescriptionAvailable() {
        setDescription(OLD_DESCRIPTION, NEW_DESCRIPTION);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description);
    }

    // -------------------- HELPERS --------------------
    private void setDescription(String originalDescription, String updatedDescription) {
        PowerMockito.when(original.getDescription()).thenReturn(originalDescription);
        PowerMockito.when(updated.getDescription()).thenReturn(updatedDescription);
    }
}
