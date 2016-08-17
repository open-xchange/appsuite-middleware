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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.rest.services.configuration;

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
import com.openexchange.rest.services.configuration.ConfigurationRESTService;
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
        when(prop.isDefined()).thenReturn(true);
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
        when(prop.isDefined()).thenReturn(false);

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
