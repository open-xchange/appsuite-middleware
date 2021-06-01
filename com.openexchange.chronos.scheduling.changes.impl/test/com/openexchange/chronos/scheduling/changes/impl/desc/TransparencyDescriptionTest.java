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
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link TransparencyDescriptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class TransparencyDescriptionTest extends AbstractDescriptionTest {

    /**
     * Initializes a new {@link TransparencyDescriptionTest}.
     */
    public TransparencyDescriptionTest() {
        super(EventField.TRANSP, "The appointment will now be shown as", () -> {
            return new TransparencyDescriber();
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        describer = new TransparencyDescriber();
    }

    @Test
    public void testTransp_SetOPAQUE_DescriptionAvailable() {
        setTransp(null, TimeTransparency.OPAQUE);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, "Reserved");
    }

    @Test
    public void testTransp_SetTransperant_DescriptionAvailable() {
        setTransp(null, TimeTransparency.TRANSPARENT);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, "Free");
    }

    @Test
    public void testTransp_ChangeToTransperant_DescriptionAvailable() {
        setTransp(TimeTransparency.OPAQUE, TimeTransparency.TRANSPARENT);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, "Free");
    }

    // -------------------- HELPERS --------------------
    private void setTransp(Transp originalTransp, Transp updatedTransp) {
        PowerMockito.when(original.getTransp()).thenReturn(originalTransp);
        PowerMockito.when(updated.getTransp()).thenReturn(updatedTransp);
    }
}
