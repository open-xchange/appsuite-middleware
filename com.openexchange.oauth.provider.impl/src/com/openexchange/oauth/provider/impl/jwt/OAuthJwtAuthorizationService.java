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
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStoreException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.PasswordLookup;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.NamePart;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.java.ConfigAwareKeyStore;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.DefaultValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link OAuthJwtAuthorizationService} - Service provider Interface that validates and parses incoming access tokens that are JWT.
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public class OAuthJwtAuthorizationService implements OAuthAuthorizationService, ForcedReloadable {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthJwtAuthorizationService.class);

    private LeanConfigurationService leanConfService;
    private OAuthJWTScopeService scopeService;
    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private ConfigAwareKeyStore configAwareKeystore;

    /**
     * Initializes a new {@link OAuthJwtAuthorizationService}.
     * 
     * @param leanConfService
     * @param scopeService
     */
    public OAuthJwtAuthorizationService(LeanConfigurationService leanConfService, OAuthJWTScopeService scopeService) {
        super();
        this.leanConfService = leanConfService;
        this.scopeService = scopeService;
        this.jwtProcessor = createJWTProcessor();
    }

    @Override
    public ValidationResponse validateAccessToken(String accessToken) throws AuthorizationException {

        DefaultValidationResponse response = new DefaultValidationResponse();
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, null);

            response.setClientName(claimsSet.getStringClaim(OAuthJWTClaimVerifier.AUTHORIZED_PARTY_CLAIM_NAME));

            Context ctx = resolveContext(claimsSet);
            response.setContextId(ctx.getContextId());

            int userId = resolveUser(claimsSet, ctx);
            response.setUserId(userId);

            List<String> scopes = scopeService.getInternalScopes(claimsSet.getStringClaim(OAuthJWTClaimVerifier.SCOPE_CLAIM_NAME));
            response.setScope(scopes);

            response.setTokenStatus(TokenStatus.VALID);
        } catch (ParseException | BadJOSEException | JOSEException | OXException e) {
            throw new AuthorizationException(e);
        }

        return response;
    }

    /**
     * Depending on configuration this method gets a {@link JWKSource} from remote or a locally populated keystore.
     *
     * @return the loaded JWKSource.
     */
    JWKSource<SecurityContext> getKeySource() {
        try {
            if (!leanConfService.getProperty(OAuthJWTProperty.JWKS_ENDPOINT).isEmpty()) {
                return getRemoteJWKS();
            }
            return getLocalKeystore();
        } catch (OXException e) {
            LOG.error("", e);
        }
        return null;
    }

    /**
     * Fetches signature keys from configured JWKS endpoint used for validation
     *
     * @return the retrieved {@link RemoteJWKSet}
     * @throws OXException
     */
    private RemoteJWKSet<SecurityContext> getRemoteJWKS() throws OXException {
        try {
            String endpoint = leanConfService.getProperty(OAuthJWTProperty.JWKS_ENDPOINT);
            return new RemoteJWKSet<>(new URL(endpoint));
        } catch (MalformedURLException e) {
            throw OAuthJWTExceptionCode.JWT_VALIDATON_FAILED.create(e, "Unable to parse JWKSet URL");
        }
    }

    /**
     * Loads signature keys from locally populated KeyStore used for validation.
     *
     * @return the loaded {@link JWKSource}
     * @throws OXException
     */
    private JWKSource<SecurityContext> getLocalKeystore() throws OXException {
        try {
            if (configAwareKeystore == null) {
                configAwareKeystore = new ConfigAwareKeyStore(OAuthJWTProperty.KEYSTORE_PATH.getFQPropertyName(), OAuthJWTProperty.KEYSTORE_PASSWORD.getFQPropertyName(), OAuthJWTProperty.KEYSTORE_TYPE.getFQPropertyName());
            }

            String keystorePath = leanConfService.getProperty(OAuthJWTProperty.KEYSTORE_PATH);
            String keyStorePassword = leanConfService.getProperty(OAuthJWTProperty.KEYSTORE_PASSWORD);

            Properties properties = new Properties();
            properties.put(OAuthJWTProperty.KEYSTORE_PATH.getFQPropertyName(), keystorePath);
            properties.put(OAuthJWTProperty.KEYSTORE_PASSWORD.getFQPropertyName(), keyStorePassword);

            configAwareKeystore.reloadStore(properties);

            JWKSet jwkSet = JWKSet.load(configAwareKeystore.getKeyStore(), new PasswordLookup() {

                @Override
                public char[] lookupPassword(String name) {
                    return keyStorePassword.toCharArray();
                }
            });
            return new ImmutableJWKSet<>(jwkSet);
        } catch (KeyStoreException | FileNotFoundException e) {
            throw OAuthJWTExceptionCode.JWT_VALIDATON_FAILED.create(e, "Unable to load local keystore");
        }
    }

    /**
     * Determines the {@link Context} of a user for which a JWT has been obtained.
     * The corresponding {@link Context} is resolved from configured claim (default = "sub").
     * 
     * @param claimsSet - contains all claims of the obtained JWT.
     * @return the resolved context.
     * @throws OXException
     * @throws ParseException
     */
    private Context resolveContext(JWTClaimsSet claimsSet) throws OXException, ParseException {
        String contextLookupParameter = leanConfService.getProperty(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM);
        if (Strings.isEmpty(contextLookupParameter)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM.name());
        }

        String contextLookup = claimsSet.getStringClaim(contextLookupParameter);
        if (contextLookup == null) {
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

    /**
     * Determines the user ID for which a JWT has been obtained.
     * The corresponding user is resolved from configured claim (default = "sub").
     *
     * @param claimsSet - contains all claims of the obtained JWT.
     * @param context - context of the user.
     * @return the resolved user.
     * @throws OXException
     * @throws ParseException
     */
    private int resolveUser(JWTClaimsSet claimsSet, Context context) throws OXException, ParseException {
        String userLookupParameter = leanConfService.getProperty(OAuthJWTProperty.USER_LOOKUP_CLAIM);
        if (Strings.isEmpty(userLookupParameter)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(OAuthJWTProperty.USER_LOOKUP_CLAIM.name());
        }

        String userLookup = claimsSet.getStringClaim(userLookupParameter);
        if (userLookup == null) {
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

    /**
     * 
     * Creates and configures {@link ConfigurableJWTProcessor} with a custom {@link OAuthJWTClaimVerifier} and {@link JWSKeySelector}.
     *
     * @return the configured {@link ConfigurableJWTProcessor}
     */
    private ConfigurableJWTProcessor<SecurityContext> createJWTProcessor() {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<SecurityContext>();

        jwtProcessor.setJWTClaimsSetVerifier(createClaimVerifier());
        jwtProcessor.setJWSKeySelector(createJWSKeySelector());
        return jwtProcessor;
    }

    /**
     * Creates a new {@link OAuthJWTClaimVerifier} and configures the allowed issuers.
     *
     * @return {@link OAuthJWTClaimVerifier}
     */
    private OAuthJWTClaimVerifier<SecurityContext> createClaimVerifier() {
        List<String> allowedIssuers = Arrays.asList(Strings.splitByComma(leanConfService.getProperty(OAuthJWTProperty.ALLOWED_ISSUER)));
        OAuthJWTClaimVerifier<SecurityContext> claimVerifier = new OAuthJWTClaimVerifier<SecurityContext>(allowedIssuers);
        return claimVerifier;
    }

    /**
     * Creates new {@link JWSKeySelector}.
     *
     * @return JWSKeySelector
     */
    private JWSKeySelector<SecurityContext> createJWSKeySelector() {
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWKSource<SecurityContext> keySource = getKeySource();
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
        return keySelector;
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        JWSKeySelector<SecurityContext> reloadedKeySelector = createJWSKeySelector();
        jwtProcessor.setJWSKeySelector(reloadedKeySelector);
    }
}
