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

package com.openexchange.jolokia.osgi;

import static com.openexchange.java.Autoboxing.B;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.service.http.HttpService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.jolokia.JolokiaConfig;
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
    public void setUp() {
        myActivator = Mockito.spy(new CustomJolokiaBundleActivator());

        // Prevent/stub logic in super.save()
        Mockito.when(((DeferredActivator) myActivator).getService(ConfigurationService.class)).thenReturn(this.configurationService);
        Mockito.when(((DeferredActivator) myActivator).getService(HttpService.class)).thenReturn(this.httpService);

        Mockito.when(B(this.configurationService.getBoolProperty("com.openexchange.jolokia.start", false))).thenReturn(B(true));
    }

     @Test
     public void testMissingConfigServiceShouldNotStart() throws Exception {
        Mockito.when(((DeferredActivator) myActivator).getService(ConfigurationService.class)).thenReturn(null);
        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testMissingHttpServiceShouldNotStart() throws Exception {
        Mockito.when(((DeferredActivator) myActivator).getService(HttpService.class)).thenReturn(null);
        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testNameAndPasswordMissingShouldNotStart() throws Exception {
        Mockito.when(B(this.configurationService.getBoolProperty("com.openexchange.jolokia.start", false))).thenReturn(B(false));

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testMissingNameShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("password");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testTooShortNameShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("password");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testMissingPasswordShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("user");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testTooShortPasswordShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("user");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testNameAndPasswordTooShortShouldNotStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(1)).notStarted();
    }

     @Test
     public void testShouldStart() throws Exception {
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.user", "")).thenReturn("user");
        Mockito.when(this.configurationService.getProperty("com.openexchange.jolokia.password", "")).thenReturn("password");

        myActivator.startBundle();
        Mockito.verify(myActivator, Mockito.times(0)).notStarted();
        myActivator.stopBundle();
    }

}
