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
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
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

    private String subject = "anton@context1.ox.test";
    private String issuer = "https://example.com";
    private String scopeClaimname = "scope";
    private String scope = "oxpim";
    private String authorizedPartyClaimname = "azp";
    private String authorizedParty = "contactviewer";

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

        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.JWKS_ENDPOINT)).thenReturn(OAuthJWTProperty.JWKS_ENDPOINT.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.KEYSTORE_PATH)).thenReturn(OAuthJWTProperty.KEYSTORE_PATH.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.KEYSTORE_PASSWORD)).thenReturn(OAuthJWTProperty.KEYSTORE_PASSWORD.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.ALLOWED_ISSUER)).thenReturn(issuer);

        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM)).thenReturn(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.CONTEXT_LOOKUP_NAME_PART)).thenReturn(NamePart.DOMAIN.getConfigName());

        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.USER_LOOKUP_CLAIM)).thenReturn(OAuthJWTProperty.USER_LOOKUP_CLAIM.getDefaultValue().toString());
        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.USER_LOOKUP_NAME_PART)).thenReturn(NamePart.LOCAL_PART.getConfigName());

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

    @Test(expected = AuthorizationException.class)
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

        spy.validateAccessToken(jwt);
    }

    @Test(expected = AuthorizationException.class)
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

        spy.validateAccessToken(jwt);
    }

    @Test(expected = AuthorizationException.class)
    public void testJWTValidationForMissingScopes() throws Exception {
        Mockito.when(leanConfigurationService.getProperty(OAuthJWTProperty.ALLOWED_ISSUER)).thenReturn("https://example.com");

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

        spy.validateAccessToken(jwt);
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
