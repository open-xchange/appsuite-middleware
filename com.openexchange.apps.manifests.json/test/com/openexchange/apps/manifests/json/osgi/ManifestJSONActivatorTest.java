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
 *    Matchers.any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After Matchers.any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT Matchers.any WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.apps.manifests.json.osgi;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.ServiceProvider;
import com.openexchange.osgi.SimpleServiceProvider;
import com.openexchange.test.mock.InjectionFieldConstants;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.test.mock.assertion.ServiceMockActivatorAsserter;


/**
 * Unit tests for {@link ManifestJSONActivator}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
@RunWith(PowerMockRunner.class)
public class ManifestJSONActivatorTest {

    /**
     * Instance to test
     */
    @InjectMocks
    private ManifestJSONActivator manifestJSONActivator = null;

    /**
     * A temporary folder that could be used by each mock.
     */
    @Rule
    protected TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private BundleContext bundleContext;

    @Mock
    private Bundle bundle;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.manifestJSONActivator = new ManifestJSONActivator();

        // SERVICES
        PowerMockito.when(configurationService.getProperty(Matchers.anyString())).thenReturn("theStringPropertyValue");
        PowerMockito.when(configurationService.getPropertiesInFolder(Matchers.anyString())).thenReturn(this.properties);

        ConcurrentMap<Class<?>, ServiceProvider<?>> services = new ConcurrentHashMap<Class<?>, ServiceProvider<?>>();
        services.putIfAbsent(ConfigurationService.class, new SimpleServiceProvider<Object>(configurationService));
        MockUtils.injectValueIntoPrivateField(this.manifestJSONActivator, InjectionFieldConstants.SERVICES, services);

        // CONTEXT
        Mockito.when(this.bundleContext.getBundle()).thenReturn(this.bundle);
        Mockito.when(this.bundle.getVersion()).thenReturn(new Version(1, 1, 1));
        MockUtils.injectValueIntoPrivateField(this.manifestJSONActivator, InjectionFieldConstants.CONTEXT, bundleContext);
    }

    @Test
    public void testStartBundle_EverythingFine_AllServicesRegistered() throws Exception {
        this.manifestJSONActivator.startBundle();

        ServiceMockActivatorAsserter.verifyAllServicesRegistered(this.manifestJSONActivator, 1);
    }

    @Test
    public void testStartBundle_EverythingFine_AllTrackersRegistered() throws Exception {
        this.manifestJSONActivator.startBundle();

        ServiceMockActivatorAsserter.verifyAllServiceTrackersRegistered(this.manifestJSONActivator, 4);
    }
}
