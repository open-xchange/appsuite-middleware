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

package com.openexchange.config.cascade.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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
