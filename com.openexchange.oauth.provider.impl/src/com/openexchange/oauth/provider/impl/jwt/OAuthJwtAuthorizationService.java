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

package com.openexchange.oauth.provider.impl.jwt;

import static com.openexchange.java.Autoboxing.I;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.DefaultValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link OAuthJwtAuthorizationService}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 */
public class OAuthJwtAuthorizationService implements OAuthAuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthJwtAuthorizationService.class);
    
    
    private static final String SCOPE_CLAIM_NAME = "scope";
    private static final String AUTHORIZED_PARTY_CLAIM_NAME = "azp";

    private LeanConfigurationService leanConfService;
    

    public OAuthJwtAuthorizationService(LeanConfigurationService leanConfService) {
        super();
        this.leanConfService = leanConfService;
    }

    @Override
    public ValidationResponse validateAccessToken(String accessToken) throws AuthorizationException {
        ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<SimpleSecurityContext>();

        JWKSource<SimpleSecurityContext> keySource = null;
        try {
            if (leanConfService.getBooleanProperty(OAuthJWTProperty.REMOTE_JWKS_ENABLED)) {
                keySource = getRemoteJWKS();
            } else {
                keySource = getLocalKeystore();
            }
        } catch (OXException e) {
            LOG.error("", e);
        }

        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

        JWSKeySelector<SimpleSecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);

        jwtProcessor.setJWSKeySelector(keySelector);

        DefaultValidationResponse response = new DefaultValidationResponse();
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, null);

            String issuer = leanConfService.getProperty(OAuthJWTProperty.ALLOWED_ISSUER);
            if (!issuer.isEmpty() && !claimsSet.getIssuer().equals(issuer)) {
                throw OAuthJWTExceptionCode.IVALID_ISSUER.create(claimsSet.getIssuer());
            }

            String oauthClient = claimsSet.getStringClaim(AUTHORIZED_PARTY_CLAIM_NAME);
            if (oauthClient == null) {
                throw OAuthJWTExceptionCode.UNALBLE_TO_LOAD_CLIENT.create("Clientname claim is empty");
            } else {
                response.setClientName(oauthClient);
            }

            Context ctx = resolveContext(claimsSet);
            response.setContextId(ctx.getContextId());

            int userId = resolveUser(claimsSet, ctx);
            response.setUserId(userId);

            String scope = claimsSet.getStringClaim(SCOPE_CLAIM_NAME);
            if (scope == null || scope.trim().isEmpty()) {
                throw OAuthJWTExceptionCode.UNABLE_TO_LOAD_VALID_SCOPE.create("Scope is null or empty");
            } else {
                List<String> scopes = OAuthJWTScopeHelper.resolveScopes(scope);
                response.setScope(scopes);
            }

            response.setTokenStatus(TokenStatus.VALID);
        } catch (ParseException | BadJOSEException | JOSEException | OXException e) {
            LOG.error("JWT validation failed: ", e);
        }

        return response;
    }

    private RemoteJWKSet<SimpleSecurityContext> getRemoteJWKS() throws OXException {
        try {
            String endpoint = leanConfService.getProperty(OAuthJWTProperty.JWKS_ENDPOINT);
            return new RemoteJWKSet<>(new URL(endpoint));
        } catch (MalformedURLException e) {
            throw OAuthJWTExceptionCode.JWT_VALIDATON_FAILED.create(e, "Unable to parse JWKSet URL");
        }
    }

    private JWKSource<SimpleSecurityContext> getLocalKeystore() throws OXException {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            char[] password = leanConfService.getProperty(OAuthJWTProperty.KEYSTORE_PASSWORD).toCharArray();
            keyStore.load(new FileInputStream(leanConfService.getProperty(OAuthJWTProperty.KEYSTORE_PATH)), password);

            JWKSet jwkSet = JWKSet.load(keyStore, null);
            return new ImmutableJWKSet<>(jwkSet);
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            throw OAuthJWTExceptionCode.JWT_VALIDATON_FAILED.create(e, "Unable to load local keystore");
        }
    }

    private Context resolveContext(JWTClaimsSet claimsSet) throws OXException, ParseException {
        String contextLookupParameter = leanConfService.getProperty(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM);
        if (Strings.isEmpty(contextLookupParameter)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM.name());
        }

        String contextLookup = claimsSet.getStringClaim(contextLookupParameter);
        if(contextLookup == null) {
            throw OAuthJWTExceptionCode.UNABLE_TO_PARSE_CLAIM.create(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM.getDefaultValue());
        }
        
        NamePart namePart = NamePart.of(leanConfService.getProperty(OAuthJWTProperty.CONTEXT_LOOKUP_NAME_PART));
        String contextInfo = namePart.getFrom(contextLookup, Authenticated.DEFAULT_CONTEXT_INFO);

        ContextService contextService = Services.requireService(ContextService.class);
        int contextId = contextService.getContextId(contextInfo);

        if (contextId < 0) {
            LOG.debug("Unknown context for login mapping '{}' ('{}')", contextInfo, contextLookup);
            throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(contextInfo);
        }

        LOG.debug("Resolved context {} for login mapping '{}' ('{}')", I(contextId), contextInfo, contextLookup);

        return contextService.getContext(contextId);

    }

    private int resolveUser(JWTClaimsSet claimsSet, Context context) throws OXException, ParseException {
        String userLookupParameter = leanConfService.getProperty(OAuthJWTProperty.USER_LOOKUP_CLAIM);
        if (Strings.isEmpty(userLookupParameter)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(OAuthJWTProperty.USER_LOOKUP_CLAIM.name());
        }

        String userLookup = claimsSet.getStringClaim(userLookupParameter);
        if(userLookup == null) {
            throw OAuthJWTExceptionCode.UNABLE_TO_PARSE_CLAIM.create(OAuthJWTProperty.USER_LOOKUP_CLAIM.getDefaultValue());
        }
        
        NamePart namePart = NamePart.of(leanConfService.getProperty(OAuthJWTProperty.USER_LOOKUP_NAME_PART));
        String userInfo = namePart.getFrom(userLookup, userLookup);

        UserService userService = Services.requireService(UserService.class);
        try {
            int userId = userService.getUserId(userInfo, context);
            LOG.debug("Resolved user {} in context {} for '{}' ('{}')", I(userId), I(context.getContextId()), userInfo, userLookup);
            return userId;
        } catch (OXException e) {
            if (LdapExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Unknown user in context {} for '{}' ('{}')", I(context.getContextId()), userInfo, userLookup);
                throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(userInfo);
            }

            throw e;
        }
    }
}
