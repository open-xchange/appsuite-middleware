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

package com.openexchange.imap;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.services.Services;


/**
 * {@link Bug30843Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, ConfigurationService.class })
public class Bug30843Test {

    private ConfigurationService configService;

    /**
     * Initializes a new {@link Bug30843Test}.
     */
    public Bug30843Test() {
        super();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.configService = PowerMockito.mock(ConfigurationService.class);
        PowerMockito.when(configService.getProperty(ArgumentMatchers.anyString())).thenReturn("");
        PowerMockito.when(configService.getProperty(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn("");
        PowerMockito.when(I(configService.getIntProperty(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))).thenReturn(I(0));
        PowerMockito.when(configService.getProperty("com.openexchange.imap.imapAuthEnc", "UTF-8")).thenReturn("UTF-8");
        PowerMockito.when(configService.getProperty("com.openexchange.imap.ssl.protocols", "SSLv3 TLSv1")).thenReturn("SSLv3 TLSv1");
        PowerMockito.when(configService.propertyNames()).thenReturn(new HashSet<String>().iterator());
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(ConfigurationService.class)).thenReturn(configService);
    }

     @Test
     public void testIMAPProperties_getSSLProtocols() throws Exception {
        IMAPProperties props = IMAPProperties.getInstance();
        props.loadProperties();
        String sslProtocols = props.getSSLProtocols();
        assertEquals("Wrong value loaded from ConfigurationService", "SSLv3 TLSv1", sslProtocols);
    }

}
