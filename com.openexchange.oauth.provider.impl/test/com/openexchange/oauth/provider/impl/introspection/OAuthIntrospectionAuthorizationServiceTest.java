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

package com.openexchange.oauth.provider.impl.introspection;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import java.util.Date;
import javax.mail.internet.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.nimbusds.jwt.util.DateUtils;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.JWTID;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.OAuthProviderProperties;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTScopeService;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link OAuthIntrospectionAuthorizationServiceTest}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since 7.10.5
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, OAuthIntrospectionAuthorizationService.class })
public class OAuthIntrospectionAuthorizationServiceTest {

    private final String accessToken = "c1MGYwNDJiYmYxNDFkZjVkOGI0MSAgLQ";

    private final Date expirationTime = DateUtils.fromSecondsSinceEpoch(System.currentTimeMillis() / 1000l + 10000);
    private final Date issueTime = DateUtils.fromSecondsSinceEpoch(System.currentTimeMillis() / 1000l);
    private final JWTID jwtid = new JWTID("7115162b-047b-450c-9687-97271fbb8a45");
    private final Issuer issuer = new Issuer("http://127.0.0.1:8085/auth/realms/demo");
    private final Subject subject = new Subject("anton@context1.ox.test");
    private final Scope scope = new Scope("oxpim");

    private OAuthIntrospectionAuthorizationService service;

    private OAuthJWTScopeService scopeService;


    @Mock
    private ContextService contextService;

    @Mock
    private UserService userService;

    @Mock
    private LeanConfigurationService leanConfigurationService;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Services.class);

        PowerMockito.when(Services.requireService(LeanConfigurationService.class)).thenReturn(leanConfigurationService);
        PowerMockito.when(Services.requireService(ContextService.class)).thenReturn(contextService);
        PowerMockito.when(Services.requireService(UserService.class)).thenReturn(userService);

        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.CONTEXT_LOOKUP_CLAIM)).thenReturn(OAuthProviderProperties.CONTEXT_LOOKUP_CLAIM.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.CONTEXT_LOOKUP_NAME_PART)).thenReturn(NamePart.DOMAIN.getConfigName());

        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.USER_LOOKUP_CLAIM)).thenReturn(OAuthProviderProperties.USER_LOOKUP_CLAIM.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.USER_LOOKUP_NAME_PART)).thenReturn(NamePart.LOCAL_PART.getConfigName());

        Mockito.when(contextService.getContext(ArgumentMatchers.anyInt())).thenReturn(new ContextImpl(1));
        Mockito.when(I(userService.getUserId(ArgumentMatchers.anyString(), (Context) ArgumentMatchers.any()))).thenReturn(I(3));

        this.scopeService = new OAuthJWTScopeService(leanConfigurationService);
        this.service = new OAuthIntrospectionAuthorizationService(leanConfigurationService, scopeService);
    }


    /**
     * Tests the process of an introspection using a given access token.
     * Finally, it is checked whether the entries of the {@link TokenIntrospectionSuccessResponse} are translated correctly and set in the {@link ValidationResponse}.
     *
     * @throws ParseException
     * @throws Exception
     */
    @Test
    public void testIntrospectionValidation() throws Exception {
     // @formatter:off
        TokenIntrospectionSuccessResponse introspectionSuccessResponse = new TokenIntrospectionSuccessResponse
            .Builder(true)
            .expirationTime(expirationTime)
            .issueTime(issueTime)
            .jwtID(jwtid)
            .issuer(issuer)
            .subject(subject)
            .scope(scope)
            .parameter("azp", "contactviewer")
            .build();
     // @formatter:on

        PowerMockito.stub(PowerMockito.method(OAuthIntrospectionAuthorizationService.class, "introspect", String.class)).toReturn(introspectionSuccessResponse);

        ValidationResponse value = service.validateAccessToken(accessToken);

        Assert.assertEquals(value.getClientName(), "contactviewer");
        Assert.assertEquals(value.getUserId(), 3);
        Assert.assertEquals(value.getContextId(), 1);
        Assert.assertEquals(value.getTokenStatus(), TokenStatus.VALID);
    }

    /**
     * This test checks if the handling of an inactive token is executed correctly.
     *
     * @throws Exception
     */
    @Test
    public void testIntrospectionOnInactiveToken() throws Exception {
     // @formatter:off
        TokenIntrospectionSuccessResponse introspectionSuccessResponse = new TokenIntrospectionSuccessResponse
            .Builder(false)
            .expirationTime(expirationTime)
            .issueTime(issueTime)
            .jwtID(jwtid)
            .issuer(issuer)
            .subject(subject)
            .scope(scope)
            .parameter("azp", "contactviewer")
            .build();
     // @formatter:on

        PowerMockito.stub(PowerMockito.method(OAuthIntrospectionAuthorizationService.class, "makeRequest", String.class)).toReturn(introspectionSuccessResponse);

        ValidationResponse value = service.validateAccessToken(accessToken);
        assertEquals(value.getTokenStatus(), TokenStatus.EXPIRED);
    }

    /**
     * This test checks if the handling of a token which indicates no success ({@link TokenIntrospectionErrorResponse}) is done correctly.
     *
     * @throws Exception
     */
    @Test(expected = AuthorizationException.class)
    public void testIntrospectionOnErrorResponse() throws Exception {
        TokenIntrospectionResponse introspectionErrorResponse = new TokenIntrospectionErrorResponse(new ErrorObject(""));

        PowerMockito.stub(PowerMockito.method(OAuthIntrospectionAuthorizationService.class, "makeRequest", String.class)).toReturn(introspectionErrorResponse);

        service.validateAccessToken(accessToken);
    }
}
