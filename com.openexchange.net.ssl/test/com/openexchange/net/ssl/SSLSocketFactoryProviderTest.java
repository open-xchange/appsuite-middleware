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

package com.openexchange.net.ssl;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.log.LogProperties;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.TrustLevel;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.net.ssl.internal.DefaultSSLSocketFactoryProvider;
import com.openexchange.net.ssl.osgi.Services;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link SSLSocketFactoryProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({ Services.class, LogProperties.class })
public class SSLSocketFactoryProviderTest {

    @Mock
    private SSLConfigurationService sslConfigurationService;

    @Mock
    private UserAwareSSLConfigurationService userAwareSSLConfigurationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(SSLConfigurationService.class)).thenReturn(this.sslConfigurationService);
        PowerMockito.when(Services.getService(UserAwareSSLConfigurationService.class)).thenReturn(this.userAwareSSLConfigurationService);

        PowerMockito.mockStatic(LogProperties.class);

        TrustedSSLSocketFactory.init();
    }

     @Test
     public void testGetDefault_trustLevelAll_returnTrustAllFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_ALL);

        SSLSocketFactory socketFactory = new DefaultSSLSocketFactoryProvider(sslConfigurationService).getDefault();

        assertEquals(TrustAllSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

     @Test
     public void testGetDefault_trustLevelRestrictedAndNoUserInLogProperties_returnTrustedFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_RESTRICTED);
        Mockito.when(B(this.userAwareSSLConfigurationService.isTrustAll(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))).thenReturn(Boolean.FALSE);

        SSLSocketFactory socketFactory = new DefaultSSLSocketFactoryProvider(sslConfigurationService).getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

     @Test
     public void testGetDefault_trustLevelRestrictedUserIdNotAvailable_returnTrustedFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_RESTRICTED);
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_USER_ID)).thenReturn("-1");

        SSLSocketFactory socketFactory = new DefaultSSLSocketFactoryProvider(sslConfigurationService).getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

     @Test
     public void testGetDefault_trustLevelRestrictedContextIdNotAvailable_returnTrustedFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_RESTRICTED);
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID)).thenReturn("-1");

        SSLSocketFactory socketFactory = new DefaultSSLSocketFactoryProvider(sslConfigurationService).getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

     @Test
     public void testGetDefault_trustLevelRestrictedButUserWithTrustAllConfig_returnTrustAllFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_RESTRICTED);
        Mockito.when(B(this.userAwareSSLConfigurationService.isTrustAll(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))).thenReturn(Boolean.FALSE);
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_USER_ID)).thenReturn("307");
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID)).thenReturn("1");

        SSLSocketFactory socketFactory = new DefaultSSLSocketFactoryProvider(sslConfigurationService).getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }
}
