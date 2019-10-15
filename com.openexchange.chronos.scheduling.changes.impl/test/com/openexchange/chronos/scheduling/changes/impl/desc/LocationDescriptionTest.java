/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.scheduling.changes.impl.desc;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.service.EventUpdate;

/**
 * {@link LocationDescriptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class LocationDescriptionTest {

    private static final String FORMAT = "text";

    private LocationDescriber describer;

    @Mock
    private EventUpdate eventUpdate;

    @Mock
    private Set<EventField> fields;

    @Mock
    private Event original;

    @Mock
    private Event updated;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        describer = new LocationDescriber();
        PowerMockito.when(eventUpdate.getOriginal()).thenReturn(original);
        PowerMockito.when(eventUpdate.getUpdate()).thenReturn(updated);
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
