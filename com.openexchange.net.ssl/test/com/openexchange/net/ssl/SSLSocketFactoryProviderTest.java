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

package com.openexchange.net.ssl;

import static org.junit.Assert.assertEquals;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(SSLConfigurationService.class)).thenReturn(this.sslConfigurationService);
        PowerMockito.when(Services.getService(UserAwareSSLConfigurationService.class)).thenReturn(this.userAwareSSLConfigurationService);

        PowerMockito.mockStatic(LogProperties.class);
    }

    @Test
    public void testGetDefault_trustLevelAll_returnTrustAllFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_ALL);

        SSLSocketFactory socketFactory = SSLSocketFactoryProvider.getDefault();

        assertEquals(TrustAllSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

    @Test
    public void testGetDefault_trustLevelNoneAndNoUserInLogProperties_returnTrustedFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_NONE);
        Mockito.when(this.userAwareSSLConfigurationService.isTrustAll(Matchers.anyInt(), Matchers.anyInt())).thenReturn(Boolean.FALSE.booleanValue());

        SSLSocketFactory socketFactory = SSLSocketFactoryProvider.getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

    @Test
    public void testGetDefault_trustLevelNoneUserIdNotAvailable_returnTrustedFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_NONE);
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_USER_ID)).thenReturn("-1");

        SSLSocketFactory socketFactory = SSLSocketFactoryProvider.getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

    @Test
    public void testGetDefault_trustLevelNoneContextIdNotAvailable_returnTrustedFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_NONE);
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID)).thenReturn("-1");

        SSLSocketFactory socketFactory = SSLSocketFactoryProvider.getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }

    @Test
    public void testGetDefault_trustLevelNoneButUserWithTrustAllConfig_returnTrustAllFactory() {
        Mockito.when(this.sslConfigurationService.getTrustLevel()).thenReturn(TrustLevel.TRUST_NONE);
        Mockito.when(this.userAwareSSLConfigurationService.isTrustAll(Matchers.anyInt(), Matchers.anyInt())).thenReturn(Boolean.FALSE.booleanValue());
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_USER_ID)).thenReturn("307");
        Mockito.when(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID)).thenReturn("1");

        SSLSocketFactory socketFactory = SSLSocketFactoryProvider.getDefault();

        assertEquals(TrustedSSLSocketFactory.getDefault().getClass().getName(), socketFactory.getClass().getName());
    }
}
