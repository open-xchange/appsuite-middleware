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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link LocationDescriptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class LocationDescriptionTest extends AbstractDescriptionTestMocking {

    private LocationDescriber describer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        describer = new LocationDescriber();
    }

    @Test
    public void testLocation_newGeo_DecriptionAvailable() {
        containsGeo();
        
        setGeo(null, new double[] { 3.5, 4.5 });

        Description description = describer.describe(eventUpdate);
        assertThat("Not matching size", I(description.getChangedFields().size()), is((I(1))));
        assertThat("Wrong field", description.getChangedFields().get(0), is((EventField.GEO)));

        String message = description.getSentences().get(0).getMessage(FORMAT, null, null, null);
        assertThat("Not matching size", I(description.getSentences().size()), is((I(1))));
        assertTrue(message.startsWith("The appointment takes place in a new location"));
        assertTrue(message.contains("3.5"));
    }

    @Test
    public void testLocation_newLocation_DecriptionAvailable() {
        containsLocation();
        
        String updatedLocation = "Olpe";
        setLocation(null, updatedLocation);

        Description description = describer.describe(eventUpdate);
        assertThat("Not matching size", I(description.getChangedFields().size()), is((I(1))));
        assertThat("Wrong field", description.getChangedFields().get(0), is((EventField.LOCATION)));

        String message = description.getSentences().get(0).getMessage(FORMAT, null, null, null);
        assertThat("Not matching size", I(description.getSentences().size()), is((I(1))));
        assertTrue(message.startsWith("The appointment takes place in a new location"));
        assertTrue(message.contains(updatedLocation));

    }
    
    @Test
    public void testLocation_completlyNew_DecriptionsAvailable() {
        containsGeo();
        containsLocation();
        
        String updatedLocation = "Olpe";
        setGeo(null, new double[] { 3.5, 4.5 });
        setLocation(null, updatedLocation);
        
        Description description = describer.describe(eventUpdate);
        assertThat("Not matching size", I(description.getChangedFields().size()), is((I(2))));
        assertTrue(description.getChangedFields().contains(EventField.GEO));
        assertTrue(description.getChangedFields().contains(EventField.LOCATION));
        
        String message = description.getSentences().get(0).getMessage(FORMAT, null, null, null);
        assertThat("Not matching size", I(description.getSentences().size()), is((I(1))));
        assertTrue(message.startsWith("The appointment takes place in a new location"));
        assertTrue(message.contains("3.5"));
        assertTrue(message.contains(updatedLocation));
    }
    
    @Test
    public void testLocation_updatedLocations_DecriptionsAvailable() {
        containsGeo();
        containsLocation();
        
        String updatedLocation = "Olpe";
        setGeo(new double[] { 1.0, 1.0 }, new double[] { 3.5, 4.5 });
        setLocation("Walachei", updatedLocation);
        
        Description description = describer.describe(eventUpdate);
        assertThat("Not matching size", I(description.getChangedFields().size()), is((I(2))));
        assertTrue(description.getChangedFields().contains(EventField.GEO));
        assertTrue(description.getChangedFields().contains(EventField.LOCATION));
        
        String message = description.getSentences().get(0).getMessage(FORMAT, null, null, null);
        assertThat("Not matching size", I(description.getSentences().size()), is((I(1))));
        assertTrue(message.startsWith("The appointment takes place in a new location"));
        assertTrue(message.contains("3.5"));
        assertTrue(message.contains(updatedLocation));
    }

    // -------------------- HELPERS --------------------

    private void containsGeo() {
        PowerMockito.when(eventUpdate.getUpdatedFields()).thenReturn(fields);
        PowerMockito.when(B(fields.contains(EventField.GEO))).thenReturn(Boolean.TRUE);
    }

    private void setGeo(double[] originalGeo, double[] updatedGeo) {
        PowerMockito.when(original.getGeo()).thenReturn(originalGeo);
        PowerMockito.when(updated.getGeo()).thenReturn(updatedGeo);
    }

    private void containsLocation() {
        PowerMockito.when(eventUpdate.getUpdatedFields()).thenReturn(fields);
        PowerMockito.when(B(fields.contains(EventField.LOCATION))).thenReturn(Boolean.TRUE);
    }

    private void setLocation(String originalLocation, String updatedLocation) {
        PowerMockito.when(original.getLocation()).thenReturn(originalLocation);
        PowerMockito.when(updated.getLocation()).thenReturn(updatedLocation);
    }
}
