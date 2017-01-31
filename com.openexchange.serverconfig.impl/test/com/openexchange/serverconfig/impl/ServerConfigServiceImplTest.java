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

package com.openexchange.serverconfig.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ClientServerConfigFilter;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigServicesLookup;
import com.openexchange.serverconfig.impl.values.Languages;
import com.openexchange.test.mock.MockUtils;

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
    private final ServiceLookup serviceLookup = null;

    private List<ComputedServerConfigValueService> values = new ArrayList<>();

    private List<ClientServerConfigFilter> configFilters = new ArrayList<>();

    private final ServerConfigServicesLookup serverConfigServicesLookup = new ServerConfigServicesLookup() {

        @Override
        public List<ComputedServerConfigValueService> getComputed() {
            return getConfigValues();
        }
        @Override
        public List<ClientServerConfigFilter> getClientFilters() {
            return getConfigFilters();
        }
    };

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    private static Map<String, Object> asConfig;

    private static Map<String, Object> asConfigDefaults;

    private static Map<String, ComposedConfigProperty<String>> composedConfigPropertyMap = new HashMap<>();

    private static Properties languageProperties;

    @Mock
    private ComposedConfigProperty<String> defaultingProperty;

    @Mock
    private ComposedConfigProperty<String> cascadeProperty;

    private final ServerConfigServiceImpl serverConfigServiceImpl = new ServerConfigServiceImpl(null, null);

    @Before
    public void setUp() throws Exception {
        asConfigDefaults = (Map<String, Object>) ConfigurationServices.loadYamlFrom(getClass().getResourceAsStream("as-config-defaults.yml"));
        asConfig = (Map<String, Object>) ConfigurationServices.loadYamlFrom(getClass().getResourceAsStream("as-config.yml"));
        languageProperties = new Properties();
        languageProperties.put("en_US", "English");
        languageProperties.put("fr_FR", "Francais");

        PowerMockito.when(this.serviceLookup.getService(ConfigurationService.class)).thenReturn(this.configurationService);
        PowerMockito.when(this.configurationService.getYaml(Matchers.eq("as-config.yml"))).thenReturn(asConfig);
        PowerMockito.when(this.configurationService.getYaml(Matchers.eq("as-config-defaults.yml"))).thenReturn(asConfigDefaults);
        PowerMockito.when(this.configurationService.getPropertiesInFolder(Matchers.eq("languages/appsuite"))).thenReturn(languageProperties);


        PowerMockito.when(this.serviceLookup.getService(ConfigViewFactory.class)).thenReturn(this.configViewFactory);
        PowerMockito.when(this.configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(this.configView);
        PowerMockito.when(this.configView.all()).thenReturn(composedConfigPropertyMap);

        PowerMockito.when(this.defaultingProperty.get()).thenReturn("<as-config>");
        PowerMockito.when(this.cascadeProperty.get()).thenReturn("cascadeValue");

        composedConfigPropertyMap.put("com.openexchange.appsuite.serverConfig.pageHeaderPrefix", defaultingProperty);
        composedConfigPropertyMap.put("com.openexchange.appsuite.serverConfig.cascadeProperty", cascadeProperty);

        MockUtils.injectValueIntoPrivateField(serverConfigServiceImpl, "serviceLookup", serviceLookup);
        MockUtils.injectValueIntoPrivateField(serverConfigServiceImpl, "serverConfigServicesLookup", serverConfigServicesLookup);

    }

    public List<ComputedServerConfigValueService> getConfigValues() {
        return values;
    }

    public void setConfigValues(List<ComputedServerConfigValueService> values) {
        this.values = values;
    }

    public List<ClientServerConfigFilter> getConfigFilters() {
        return configFilters;
    }

    public void setConfigFilters(List<ClientServerConfigFilter> configFilters) {
        this.configFilters = configFilters;
    }

    @Test
    public void testGetServerConfig() throws OXException {
        ServerConfig serverConfig2 = serverConfigServiceImpl.getServerConfig("host1.mycloud.net", 1, 1);
        Map<String, Object> configMap = serverConfig2.asMap();

        //values that come from config-as-default
        configMap.get("productNameMail");
        assertEquals("OX Mail", configMap.get("productNameMail"));
        assertFalse((boolean)configMap.get("forgotPassword"));
        assertTrue((boolean)configMap.get("staySignedIn"));
        assertEquals("(c) 2015 Open-Xchange.", configMap.get("copyright"));

        //values that come from config-as and are applied because the host1 matches
        assertEquals("host1.mycloud.net", configMap.get("host"));
        assertEquals("Professional Webmail OX ", configMap.get("pageTitle"));
        assertEquals("Professional Webmail OX", configMap.get("productName"));
        assertEquals("", configMap.get("pageHeader"));
        assertEquals("", configMap.get("pageHeaderPrefix"));
        assertEquals("host1.mycloud.net", configMap.get("signinTheme"));
        assertEquals("E-Mail: cloudservice@host1-support.de", configMap.get("contact"));

        //values that come from config-as and are applied because the hostregex matches
        assertEquals("host.*\\.mycloud\\.net", configMap.get("hostRegex"));
        assertEquals("someRegexHostValue", configMap.get("someRegexHostKey"));

        /* We made sure to include this configvalue in the configcascade but we wanted to explicitely fall back to the value provided after
         * the merge of as-config-defaults and as-config via the <as-config> mechanism. So this is meant more as documentation as the key
         * is already checked above for the proper value configured for host1.
         */
        assertFalse("<as-config>".equals(configMap.get("pageHeaderPrefix")));

        //values from configCascade
        assertEquals("cascadeValue", configMap.get("Config.cascadeProperty"));
        assertEquals("cascadeValue", configMap.get("cascadeProperty"));

    }

    @Test
    public void testLooksApplicable_host_not_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, "host4.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_host_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        assertTrue(serverConfigServiceImpl.looksApplicable(host1Config, "host1.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_hostregex_not_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, "performance.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_hostregex_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertTrue(serverConfigServiceImpl.looksApplicable(host1Config, "host1.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_NoConfig() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, null));
    }

    @Test
    public void testLooksApplicable_NoData() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertFalse(serverConfigServiceImpl.looksApplicable(host1Config, null));
    }

    @Test
    public void testGetServerConfigServicesLookup() {
        assertNotNull(serverConfigServiceImpl.getServerConfigServicesLookup());
    }

    @Test
    public void testDefaultNotificationMailConfiguration() throws Exception {
        ServerConfig serverConfig = serverConfigServiceImpl.getServerConfig("nonconfiguredhost.com", -1, -1);
        NotificationMailConfig nmc = serverConfig.getNotificationMailConfig();
        assertEquals("Wrong button text color", "#ffffff", nmc.getButtonTextColor());
        assertEquals("Wrong button background color", "#3c73aa", nmc.getButtonBackgroundColor());
        assertEquals("Wrong button border color", "#356697", nmc.getButtonBorderColor());
        assertEquals("Wrong footer image name", "ox_logo_claim_blue_small.png", nmc.getFooterImage());
        assertEquals("Wrong footer text", "", nmc.getFooterText());
    }

    @Test
    public void testCustomNotificationMailConfiguration() throws Exception {
        ServerConfig serverConfig = serverConfigServiceImpl.getServerConfig("host3.mycloud.net", -1, -1);
        NotificationMailConfig nmc = serverConfig.getNotificationMailConfig();
        assertEquals("Wrong button text color", "#000000", nmc.getButtonTextColor());
        assertEquals("Wrong button background color", "#ffffff", nmc.getButtonBackgroundColor());
        assertEquals("Wrong button border color", "#000000", nmc.getButtonBorderColor());
        assertEquals("Wrong footer image name", "custom_logo.png", nmc.getFooterImage());
        assertEquals("Wrong footer text", "Footer text", nmc.getFooterText());
    }

    @Test
    public void testCustomNotificationMailConfigurationWithoutFooter() throws Exception {
        ServerConfig serverConfig = serverConfigServiceImpl.getServerConfig("host2.mycloud.net", -1, -1);
        NotificationMailConfig nmc = serverConfig.getNotificationMailConfig();
        assertEquals("Wrong button text color", "#ffffff", nmc.getButtonTextColor());
        assertEquals("Wrong button background color", "#3c73aa", nmc.getButtonBackgroundColor());
        assertEquals("Wrong button border color", "#356697", nmc.getButtonBorderColor());
        assertEquals("Wrong footer image name", null, nmc.getFooterImage());
        assertEquals("Wrong footer text", null, nmc.getFooterText());
    }

    @Test
    public void testCustomNotificationMailConfigurationWithoutFooterImage() throws Exception {
        ServerConfig serverConfig = serverConfigServiceImpl.getServerConfig("host1.mycloud.net", -1, -1);
        NotificationMailConfig nmc = serverConfig.getNotificationMailConfig();
        assertEquals("Wrong button text color", "#ffffff", nmc.getButtonTextColor());
        assertEquals("Wrong button background color", "#3c73aa", nmc.getButtonBackgroundColor());
        assertEquals("Wrong button border color", "#356697", nmc.getButtonBorderColor());
        assertEquals("Wrong footer image name", null, nmc.getFooterImage());
        assertEquals("Wrong footer text", "Footer text", nmc.getFooterText());
    }

    @Test
    public void testCustomSingleLanguageConfiguration() throws Exception {
        ComputedServerConfigValueService languages = new Languages(serviceLookup);
        setConfigValues(Collections.singletonList(languages));
        ServerConfig host1 = serverConfigServiceImpl.getServerConfig("host1.mycloud.net", 1, 1);
        List<SimpleEntry<String, String>> configLanguages = host1.getLanguages();
        // host1 is restricted to use only one language as per yaml
        assertEquals(1, configLanguages.size());
    }

    @Test
    public void testCustomAllLanguageConfiguration() throws Exception {
        ComputedServerConfigValueService languages = new Languages(serviceLookup);
        setConfigValues(Collections.singletonList(languages));
        ServerConfig host2 = serverConfigServiceImpl.getServerConfig("host2.mycloud.net", 1, 1);
        List<SimpleEntry<String, String>> configLanguages = host2.getLanguages();
        // host2 is configured to use all(2) available languages
        assertEquals(2, configLanguages.size());
    }

    @Test
    public void testMissingLanguageConfiguration() throws Exception {
        ComputedServerConfigValueService languages = new Languages(serviceLookup);
        setConfigValues(Collections.singletonList(languages));
        ServerConfig host2 = serverConfigServiceImpl.getServerConfig("host3.mycloud.net", 1, 1);
        List<SimpleEntry<String, String>> configLanguages = host2.getLanguages();
        // host3 has no language config so it should use all(2) available languages
        assertEquals(2, configLanguages.size());
    }

}
