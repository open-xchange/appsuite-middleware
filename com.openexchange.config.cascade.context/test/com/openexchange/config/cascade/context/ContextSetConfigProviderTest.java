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

package com.openexchange.config.cascade.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link ContextSetConfigProviderTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ContextSetConfigProviderTest {

    private static final String TESTPROP = "com.openexchange.testprop";

    @Mock
    private ContextService contexts;

    @Mock
    private Context context;

    @Mock
    private ConfigurationService config;

    @Mock
    private UserPermissionService userPermissions;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private ConfigViewFactory configViews;

    private ContextSetConfigProvider configProvider;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(TESTPROP, "testvalue");
        properties.put("withTags", "ucInfostore");
        Map<String, Object> yamlFiles = Collections.<String, Object>singletonMap(
            "/opt/open-xchange/etc/contextSets/test.yml",
            Collections.<String, Object>singletonMap("configname", properties));

        when(config.getYamlInFolder(anyString())).thenReturn(yamlFiles);
        when(userPermissions.getUserPermissionBits(anyInt(), (Context) any())).thenReturn( new UserPermissionBits(UserPermissionBits.INFOSTORE, 1, context));

        SimpleServiceLookup services = new SimpleServiceLookup();
        services.add(ContextService.class, contexts);
        services.add(ConfigurationService.class, config);
        services.add(UserPermissionService.class, userPermissions);
        services.add(ConfigViewFactory.class, configViews);
        configProvider = new ContextSetConfigProvider(services);
    }

     @Test
     public void testPropertyBoundToUserPermission() throws Exception {
        ContextImpl contextImpl = new ContextImpl(1);
        BasicProperty property = configProvider.get(TESTPROP, contextImpl, 1);
        assertTrue(property.isDefined());
        assertEquals("testvalue", property.get());
    }

}
