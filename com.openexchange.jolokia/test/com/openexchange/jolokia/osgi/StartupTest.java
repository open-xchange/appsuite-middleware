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

package com.openexchange.jolokia.osgi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.service.http.HttpService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.jolokia.JolokiaConfig;
import com.openexchange.jolokia.osgi.CustomJolokiaBundleActivator;
import com.openexchange.osgi.DeferredActivator;

/**
 * {@link StartupTest}
 * 
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ JolokiaConfig.class})
public class StartupTest {

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private HttpService httpService;
    
    CustomJolokiaBundleActivator myActivator;

    @Before
    public void setUp() throws Exception {
        myActivator = Mockito.spy(new CustomJolokiaBundleActivator());

        // Prevent/stub logic in super.save()
        Mockito.when(((DeferredActivator) myActivator).getService(ConfigurationService.class)).thenReturn(this.configurationService);
        Mockito.when(((DeferredActivator) myActivator).getService(HttpService.class)).thenReturn(this.httpService);
        PowerMockito.mockStatic(JolokiaConfig.class);
        PowerMockito.when(JolokiaConfig.getInstance()).thenReturn(new JolokiaConfig());

        Mockito.when(this.configurationService.getBoolProperty("com.openexchange.jolokia.start", false)).thenReturn(true);
    }

    @Test
    public void testMissingConfigServiceShouldNotStart() throws Exception {
        Mockito.when(((DeferredActivator) myActivator).getService(ConfigurationService.class)).thenReturn(null);
        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testMissingHttpServiceShouldNotStart() throws Exception {
        Mockito.when(((DeferredActivator) myActivator).getService(HttpService.class)).thenReturn(null);
        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testNameAndPasswordMissingShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getBoolProperty("com.openexchange.jolokia.start", false)).thenReturn(false);

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testMissingNameShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("password");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testTooShortNameShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("password");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testMissingPasswordShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("user");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testTooShortPasswordShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("user");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testNameAndPasswordTooShortShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).stopBundle();
    }

    @Test
    public void testShouldStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("user");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("password");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(0)).stopBundle();
        myActivator.stopBundle();
    }

}
