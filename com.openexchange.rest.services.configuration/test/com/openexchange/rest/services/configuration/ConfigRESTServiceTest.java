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

package com.openexchange.rest.services.configuration;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.MockingServiceLookup;

/**
 * {@link ConfigRESTServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigRESTServiceTest {

    private MockingServiceLookup services = null;
    private ConfigView configView = null;
    private ConfigurationRESTService configService = null;

    @Before
    public void setup() throws OXException {
        services = new MockingServiceLookup();
        configView = mock(ConfigView.class);
        configService = new ConfigurationRESTService(services);

        ConfigViewFactory mock = services.mock(ConfigViewFactory.class);
        when(mock.getView(12, 42)).thenReturn(configView);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRetrieveProperty() throws OXException, JSONException {

        ComposedConfigProperty<String> prop = mock(ComposedConfigProperty.class);
        when(B(prop.isDefined())).thenReturn(B(true));
        when(prop.get()).thenReturn("myValue");

        when(configView.property("myProperty", String.class)).thenReturn(prop);

        Object property = configService.getProperty("myProperty", 42, 12);

        JSONObject result = (JSONObject) property;
        assertEquals(1, result.length());
        assertEquals("myValue", result.getString("myProperty"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void retrieveUnknownProperty() throws OXException {
        ComposedConfigProperty<String> prop = mock(ComposedConfigProperty.class);
        when(B(prop.isDefined())).thenReturn(B(false));

        when(configView.property("myProperty", String.class)).thenReturn(prop);
        try {
            configService.getProperty("myProperty", 42, 12);
            fail("Should have halted prematurely");
        } catch (NotFoundException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRetrieveAllPropertiesWithACertainPrefix() throws OXException, JSONException {
        ComposedConfigProperty<String> prop1 = mock(ComposedConfigProperty.class);
        when(prop1.get()).thenReturn("v1");

        ComposedConfigProperty<String> prop2 = mock(ComposedConfigProperty.class);
        when(prop2.get()).thenReturn("v2");

        ComposedConfigProperty<String> prop3 = mock(ComposedConfigProperty.class);
        when(prop3.get()).thenReturn("v3");

        ComposedConfigProperty<String> prop4 = mock(ComposedConfigProperty.class);
        when(prop4.get()).thenReturn("v4");

        ComposedConfigProperty<String> prop5 = mock(ComposedConfigProperty.class);
        when(prop5.get()).thenReturn("v5");

        Map<String, ComposedConfigProperty<String>> map = new HashMap<String, ComposedConfigProperty<String>>();
        map.put("com.openexchange.prefix.p1", prop1);
        map.put("com.openexchange.prefix.p2", prop2);

        map.put("com.openexchange.otherPrefix.p3", prop3);
        map.put("com.openexchange.otherPrefix.p4", prop4);

        map.put("com.openexchange.yetAnotherPrefix.p5", prop5);

        when(configView.all()).thenReturn(map);

        JSONObject object = configService.getWithPrefix("com.openexchange.prefix", 42, 12);
        assertEquals(2, object.length());
        assertEquals("v1", object.getString("com.openexchange.prefix.p1"));
        assertEquals("v2", object.getString("com.openexchange.prefix.p2"));

    }
}
