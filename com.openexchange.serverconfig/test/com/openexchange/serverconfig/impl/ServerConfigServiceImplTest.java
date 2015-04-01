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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.serverconfig.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ho.yaml.Yaml;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.serverconfig.ServerConfigMatcherService;
import com.openexchange.serverconfig.ServerConfigServicesLookup;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ServerConfigServiceImplTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
@RunWith(PowerMockRunner.class)
@SuppressWarnings("unchecked")
public class ServerConfigServiceImplTest {

    @Mock
    private ServiceLookup serviceLookup = null;

    private List<ServerConfigMatcherService> matchers = new ArrayList<>();

    private List<ComputedServerConfigValueService> values = new ArrayList<>();

    private ServerConfigServicesLookup serverConfigServicesLookup = new ServerConfigServicesLookup() {

        @Override
        public List<ServerConfigMatcherService> getMatchers() {
            return getConfigMatchers();
        }

        @Override
        public List<ComputedServerConfigValueService> getComputed() {
            return getConfigValues();
        }
    };

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    ServerSession serverSession;

    @Mock
    private AJAXRequestData ajaxRequestData;

    private static Map<String, Object> asConfig;

    private static Map<String, Object> asConfigDefaults;
    
    private static Map<String, ComposedConfigProperty<String>> composedConfigPropertyMap = new HashMap<>();
    
    @Mock
    private ComposedConfigProperty<String> defaultingProperty;

    @Mock
    private ComposedConfigProperty<String> cascadeProperty;

    private ServerConfigServiceImpl serverConfigServiceImpl = new ServerConfigServiceImpl(null, null);

    @Before
    public void setUp() throws Exception {
        asConfigDefaults = (Map<String, Object>) Yaml.load(getClass().getResourceAsStream("as-config-defaults.yml"));
        asConfig = (Map<String, Object>) Yaml.load(getClass().getResourceAsStream("as-config.yml"));

        PowerMockito.when(this.serviceLookup.getService(ConfigurationService.class)).thenReturn(this.configurationService);
        PowerMockito.when(this.configurationService.getYaml(Matchers.eq("as-config.yml"))).thenReturn(asConfig);
        PowerMockito.when(this.configurationService.getYaml(Matchers.eq("as-config-defaults.yml"))).thenReturn(asConfigDefaults);

        PowerMockito.when(this.serviceLookup.getService(ConfigViewFactory.class)).thenReturn(this.configViewFactory);
        PowerMockito.when(this.configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(this.configView);
        PowerMockito.when(this.configView.all()).thenReturn(composedConfigPropertyMap);
        
        PowerMockito.when(this.defaultingProperty.get()).thenReturn("<as-config>");
        PowerMockito.when(this.cascadeProperty.get()).thenReturn("cascadeValue");
        
        composedConfigPropertyMap.put("com.openexchange.appsuite.serverConfig.pageHeaderPrefix", defaultingProperty);
        composedConfigPropertyMap.put("com.openexchange.appsuite.serverConfig.cascadeProperty", cascadeProperty);

        PowerMockito.when(this.serverSession.isAnonymous()).thenReturn(false);
        PowerMockito.when(this.serverSession.getUserId()).thenReturn(1);
        PowerMockito.when(this.serverSession.getContextId()).thenReturn(1);

        MockUtils.injectValueIntoPrivateField(serverConfigServiceImpl, "serviceLookup", serviceLookup);
        MockUtils.injectValueIntoPrivateField(serverConfigServiceImpl, "serverConfigServicesLookup", serverConfigServicesLookup);

    }

    public List<ServerConfigMatcherService> getConfigMatchers() {
        return matchers;
    }

    public void setConfigMatchers(List<ServerConfigMatcherService> matchers) {
        this.matchers = matchers;
    }

    public List<ComputedServerConfigValueService> getConfigValues() {
        return values;
    }

    public void setConfigValues(List<ComputedServerConfigValueService> values) {
        this.values = values;
    }

    @Test
    public void testGetServerConfig() throws OXException, JSONException {
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("host1.mycloud.net");
        JSONObject serverConfig = serverConfigServiceImpl.getServerConfig(ajaxRequestData, serverSession);
        System.out.println(serverConfig);

        //values that come from config-as-default
        assertEquals("OX Mail", serverConfig.getString("productNameMail"));
        assertFalse(serverConfig.getBoolean("forgotPassword"));
        assertTrue(serverConfig.getBoolean("staySignedIn"));
        assertEquals("(c) 2015 Open-Xchange.", serverConfig.getString("copyright"));

        //values that come from config-as and are applied because the host1 matches
        assertEquals("host1.mycloud.net", serverConfig.getString("host"));
        assertEquals("Professional Webmail OX ", serverConfig.getString("pageTitle"));
        assertEquals("Professional Webmail OX", serverConfig.getString("productName"));
        assertEquals("", serverConfig.getString("pageHeader"));
        assertEquals("", serverConfig.getString("pageHeaderPrefix"));
        assertEquals("host1.mycloud.net", serverConfig.getString("signinTheme"));
        assertEquals("E-Mail: cloudservice@host1-support.de", serverConfig.getString("contact"));

        //values that come from config-as and are applied because the hostregex matches
        assertEquals("host.*\\.mycloud\\.net", serverConfig.getString("hostRegex"));
        assertEquals("someRegexHostValue", serverConfig.getString("someRegexHostKey"));
        
        /* We made sure to include this configvalue in the configcascade but we wanted to explicitely fall back to the value provided after
         * the merge of as-config-defaults and as-config via the <as-config> mechanism. So this is meant more as documentation as the key
         * is already checked above for the proper value configured for host1.
         */
        assertFalse("<as-config>".equals(serverConfig.getString("pageHeaderPrefix")));
        
        //values from configCascade
        assertEquals("cascadeValue", serverConfig.getString("Config.cascadeProperty"));
        assertEquals("cascadeValue", serverConfig.getString("cascadeProperty"));

    }

    @Test
    public void testLooksApplicable_host_not_matching() throws OXException {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("host4.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, ajaxRequestData, null));
    }

    @Test
    public void testLooksApplicable_host_matching() throws OXException {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("host1.mycloud.net");
        assertTrue(serverConfigServiceImpl.looksApplicable(host1Config, ajaxRequestData, null));
    }

    @Test
    public void testLooksApplicable_hostregex_not_matching() throws OXException {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("performance.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, ajaxRequestData, null));
    }

    @Test
    public void testLooksApplicable_hostregex_matching() throws OXException {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("host1.mycloud.net");
        assertTrue(serverConfigServiceImpl.looksApplicable(host1Config, ajaxRequestData, null));
    }

    @Test
    public void testLooksApplicable_matcher_not_matching() throws OXException {

        ServerConfigMatcherService noneMatcher = new ServerConfigMatcherService() {

            @Override
            public boolean looksApplicable(Map<String, Object> config, AJAXRequestData request, ServerSession session) {
                return false;
            }
        };
        setConfigMatchers(Collections.singletonList(noneMatcher));

        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("unknown.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, ajaxRequestData, null));
    }

    @Test
    public void testLooksApplicable_matcher_matching() throws OXException {
        ServerConfigMatcherService allMatcher = new ServerConfigMatcherService() {

            @Override
            public boolean looksApplicable(Map<String, Object> config, AJAXRequestData request, ServerSession session) {
                return true;
            }
        };
        setConfigMatchers(Collections.singletonList(allMatcher));

        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("unknown.mycloud.net");
        assertTrue(serverConfigServiceImpl.looksApplicable(host1Config, ajaxRequestData, null));
    }

    @Test
    public void testLooksApplicable_NoConfig() throws OXException {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("host1.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, null, null));

        Mockito.when(ajaxRequestData.getHostname()).thenReturn(null);
        assertFalse(serverConfigServiceImpl.looksApplicable(null, ajaxRequestData, null));
    }

    @Test
    public void testLooksApplicable_NoData() throws OXException {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        Mockito.when(ajaxRequestData.getHostname()).thenReturn("host1.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, null, null));

        Mockito.when(ajaxRequestData.getHostname()).thenReturn(null);
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, ajaxRequestData, null));
    }

    @Test
    public void testGetServerConfigServicesLookup() {
        assertNotNull(serverConfigServiceImpl.getServerConfigServicesLookup());
    }

}
