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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.oauth.provider.impl.introspection;

import javax.mail.internet.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.nimbusds.jwt.util.DateUtils;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.JWTID;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.user.UserService;

import static com.openexchange.java.Autoboxing.I;

/**
 * {@link OAuthIntrospectionAuthorizationServiceTest}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Services.class, OAuthIntrospectionAuthorizationService.class})
public class OAuthIntrospectionAuthorizationServiceTest {
    
    class OAuthIntrospectionAuthorizationServiceMock extends OAuthIntrospectionAuthorizationService {
        public OAuthIntrospectionAuthorizationServiceMock(LeanConfigurationService leanConfigurationService) {
            super(leanConfigurationService);
        }
        
        @Override
        protected TokenIntrospectionSuccessResponse introspect(String accessToken) {
            TokenIntrospectionSuccessResponse introspectionSuccessResponse = new TokenIntrospectionSuccessResponse
                .Builder(true)
                .expirationTime(DateUtils.fromSecondsSinceEpoch(System.currentTimeMillis() / 1000l + 10000))
                .issueTime(DateUtils.fromSecondsSinceEpoch(System.currentTimeMillis() / 1000l))
                .jwtID(new JWTID("7115162b-047b-450c-9687-97271fbb8a45"))
                .issuer(new Issuer("http://127.0.0.1:8085/auth/realms/demo"))
                .subject(new Subject("anton@context1.ox.test"))
                .scope(new Scope("oxpim"))
                .clientID(new ClientID("contactviewer"))
                .build();
            
            return introspectionSuccessResponse;
        }
    }
    
    private OAuthIntrospectionAuthorizationServiceMock service;
        
    private String accessToken = "c1MGYwNDJiYmYxNDFkZjVkOGI0MSAgLQ";
    

    @Mock
    private ContextService contextService;
        
    @Mock
    private UserService userService;
    
    @Mock
    private LeanConfigurationService leanConfigurationService;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.requireService(LeanConfigurationService.class)).thenReturn(leanConfigurationService);  
        PowerMockito.when(Services.requireService(ContextService.class)).thenReturn(contextService);
        PowerMockito.when(Services.requireService(UserService.class)).thenReturn(userService);

        Mockito.when(leanConfigurationService.getProperty(OAuthIntrospectionProperty.CONTEXT_LOOKUP_CLAIM)).thenReturn("sub");
        Mockito.when(leanConfigurationService.getProperty(OAuthIntrospectionProperty.CONTEXT_LOOKUP_NAME_PART)).thenReturn(NamePart.DOMAIN.getConfigName());

        Mockito.when(leanConfigurationService.getProperty(OAuthIntrospectionProperty.USER_LOOKUP_CLAIM)).thenReturn("sub");
        Mockito.when(leanConfigurationService.getProperty(OAuthIntrospectionProperty.USER_LOOKUP_NAME_PART)).thenReturn(NamePart.LOCAL_PART.getConfigName());

        Mockito.when(contextService.getContext(ArgumentMatchers.anyInt())).thenReturn(new ContextImpl(1));
        Mockito.when(userService.getUserId(ArgumentMatchers.anyString(), (Context) ArgumentMatchers.any())).thenReturn(I(3));
        this.service = new OAuthIntrospectionAuthorizationServiceMock(leanConfigurationService); 
    }
    
    
    @Test
    public void testIntrospectionValidation() throws ParseException, Exception {
        ValidationResponse value = this.service.validateAccessToken(accessToken);
        
        Assert.assertEquals(value.getClientName(), "contactviewer");
        Assert.assertEquals(value.getUserId(), 3);
        Assert.assertEquals(value.getContextId(), 1);
        Assert.assertEquals(value.getTokenStatus(), TokenStatus.VALID);
    }
}
