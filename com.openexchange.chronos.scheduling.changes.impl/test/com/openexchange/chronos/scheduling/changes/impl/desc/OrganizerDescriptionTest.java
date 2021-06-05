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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link OrganizerDescriptionTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class OrganizerDescriptionTest extends AbstractDescriptionTest {

    private Organizer oldOrganizer;
    private Organizer newOrganizer;

    /**
     * Initializes a new {@link OrganizerDescriptionTest}.
     */
    public OrganizerDescriptionTest() {
        super(EventField.ORGANIZER, "The organizer of the appointment has changed", () -> {
            return new OrganizerDescriber();
        });
    }


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        describer = new OrganizerDescriber();
        oldOrganizer = new Organizer();
        oldOrganizer.setEMail("old@organizer.com");
        newOrganizer = new Organizer();
        newOrganizer.setEMail("new@organizer.com");
    }

    @Test
    public void testOrganizer_SetNewOrganizer_DescriptionAvailable() {
        setOrganizer(null, newOrganizer);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, newOrganizer.getEMail());
    }

    /**
     * Tests that no {@link NullPointerException} occurs when the organizer is removed.
     */
    @Test
    public void testOrganizer_removeOrganizer_DescriptionAvailable() {
        setOrganizer(oldOrganizer, null);

        Description description = describer.describe(eventUpdate);
        assertNotNull(description);
        assertTrue(description.getSentences().isEmpty());
    }

    @Test
    public void testOrganizer_ChangeOrganizer_DescriptionAvailable() {
        setOrganizer(oldOrganizer, newOrganizer);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, newOrganizer.getEMail());
    }

    // -------------------- HELPERS --------------------
    private void setOrganizer(Organizer originalOrganizer, Organizer updatedOrganizer) {
        PowerMockito.when(original.getOrganizer()).thenReturn(originalOrganizer);
        PowerMockito.when(updated.getOrganizer()).thenReturn(updatedOrganizer);
    }
}
