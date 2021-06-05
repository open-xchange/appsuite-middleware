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

package com.openexchange.oauth.provider.impl.jwt;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.OAuthProviderProperties;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link OAuthJwtAuthorizationServiceTest}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since 7.10.5
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, LeanConfigurationService.class, OAuthJwtAuthorizationService.class })
public class OAuthJwtAuthorizationServiceTest {

    private final String subject = "anton@context1.ox.test";
    private final String issuer = "https://example.com";
    private final String scopeClaimname = "scope";
    private final String scope = "oxpim";
    private final String authorizedPartyClaimname = "azp";
    private final String authorizedParty = "contactviewer";

    private OAuthJwtAuthorizationService spy;

    private OAuthJWTScopeService scopeService;

    private JWK jwk;

    private KeyPair keyPair;

    @Mock
    private LeanConfigurationService leanConfigurationService;

    @Mock
    private ContextService contextService;

    @Mock
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.requireService(LeanConfigurationService.class)).thenReturn(leanConfigurationService);
        PowerMockito.when(Services.requireService(ContextService.class)).thenReturn(contextService);
        PowerMockito.when(Services.requireService(UserService.class)).thenReturn(userService);

        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.JWKS_URI)).thenReturn(OAuthJWTProperty.JWKS_URI.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.ALLOWED_ISSUER)).thenReturn(issuer);

        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.CONTEXT_LOOKUP_CLAIM)).thenReturn(OAuthProviderProperties.CONTEXT_LOOKUP_CLAIM.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.CONTEXT_LOOKUP_NAME_PART)).thenReturn(NamePart.DOMAIN.getConfigName());

        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.USER_LOOKUP_CLAIM)).thenReturn(OAuthProviderProperties.USER_LOOKUP_CLAIM.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.USER_LOOKUP_NAME_PART)).thenReturn(NamePart.LOCAL_PART.getConfigName());

        Mockito.when(contextService.getContext(ArgumentMatchers.anyInt())).thenReturn(new ContextImpl(1));
        Mockito.when(I(userService.getUserId(ArgumentMatchers.anyString(), (Context) ArgumentMatchers.any()))).thenReturn(I(3));

        this.scopeService = new OAuthJWTScopeService(leanConfigurationService);
        this.keyPair = generateKeyPair();
        this.jwk = generateJWK(keyPair);

        MemberModifier.stub(MemberMatcher.method(OAuthJwtAuthorizationService.class, "getKeySource")).toReturn(new ImmutableJWKSet<>(new JWKSet(jwk)));
        this.spy = new OAuthJwtAuthorizationService(leanConfigurationService, scopeService);

    }

    @Test
    public void testJwtValidationForValidToken() throws Exception {
        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(keyPair.getPrivate());

        // Prepare JWT with claims set
        // @formatter:off
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer(issuer)
            .claim(authorizedPartyClaimname, "testClient")
            .claim(scopeClaimname, scope)
            .expirationTime(new Date(new Date().getTime() + 60 * 1000))
            .build();
        // @formatter:on

        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build(), claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        String jwt = signedJWT.serialize();

        ValidationResponse value = spy.validateAccessToken(jwt);

        assertEquals(value.getTokenStatus(), TokenStatus.VALID);
    }

    @Test
    public void testJWTValidationForExpiredToken() throws Exception {
        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(keyPair.getPrivate());

        // Prepare JWT with claims set
        // @formatter:off
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer(issuer)
            .claim(authorizedPartyClaimname, authorizedParty)
            .claim(scopeClaimname, scope)
            .expirationTime(new Date(new Date().getTime() - 60 * 1000))
            .build();
        // @formatter:on

        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build(), claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        String jwt = signedJWT.serialize();

        ValidationResponse value = spy.validateAccessToken(jwt);
        assertEquals(value.getTokenStatus(), TokenStatus.INVALID);
    }

    @Test
    public void testJWTValidationForUnexpectedTokenIssuer() throws Exception {
        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(keyPair.getPrivate());

        // Prepare JWT with claims set
        // @formatter:off
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer("https://example.de")
            .claim(authorizedPartyClaimname, authorizedParty)
            .claim(scopeClaimname, scope)
            .expirationTime(new Date(new Date().getTime() + 60 * 1000))
            .build();
        // @formatter:on

        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build(), claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        String jwt = signedJWT.serialize();

        ValidationResponse value = spy.validateAccessToken(jwt);
        assertEquals(value.getTokenStatus(), TokenStatus.INVALID);
    }

    @Test
    public void testJWTValidationForMissingScopes() throws Exception {
        Mockito.when(leanConfigurationService.getProperty(OAuthProviderProperties.ALLOWED_ISSUER)).thenReturn("https://example.com");

        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(keyPair.getPrivate());

        // Prepare JWT with claims set
        // @formatter:off
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer(issuer)
            .claim(authorizedPartyClaimname, authorizedParty)
            .claim(scopeClaimname, "")
            .expirationTime(new Date(new Date().getTime() + 60 * 1000))
            .build();
        // @formatter:on

        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build(), claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        String jwt = signedJWT.serialize();

        ValidationResponse value = spy.validateAccessToken(jwt);
        assertEquals(value.getTokenStatus(), TokenStatus.INVALID);
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();

        return keyPair;
    }

    private JWK generateJWK(KeyPair keyPair) {
        // @formatter:off
        JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate())
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .build();
        // @formatter:on
        return jwk;
    }
}
