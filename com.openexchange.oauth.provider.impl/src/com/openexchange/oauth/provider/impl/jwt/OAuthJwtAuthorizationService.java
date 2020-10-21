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
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConfigAwareKeyStore;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.impl.AbstractAuthorizationService;

/**
 * {@link OAuthJwtAuthorizationService} - Service provider Interface that validates and parses incoming access tokens that are JWT.
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public class OAuthJwtAuthorizationService extends AbstractAuthorizationService implements ForcedReloadable {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthJwtAuthorizationService.class);

    private final LeanConfigurationService leanConfService;
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private ConfigAwareKeyStore configAwareKeystore;

    /**
     * Initializes a new {@link OAuthJwtAuthorizationService}.
     *
     * @param leanConfService
     * @param scopeService
     * @throws OXException
     */
    public OAuthJwtAuthorizationService(LeanConfigurationService leanConfService, OAuthJWTScopeService scopeService) throws OXException {
        super(scopeService);
        this.leanConfService = leanConfService;
        this.jwtProcessor = createJWTProcessor();
    }

    @Override
    public ValidationResponse validateAccessToken(String accessToken) throws AuthorizationException {
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, null);
            return createValidationReponse(claimsSet);
        } catch (ParseException | BadJOSEException | JOSEException | OXException e) {
            LOG.debug(e.getMessage());
            throw new AuthorizationException(e);
        }
    }

    /**
     * Depending on configuration this method gets a {@link JWKSource} from remote or a locally populated keystore.
     *
     * @return the loaded JWKSource.
     * @throws OXException
     */
    JWKSource<SecurityContext> getKeySource() throws OXException {
        if (Strings.isNotEmpty(leanConfService.getProperty(OAuthJWTProperty.JWKS_ENDPOINT))) {
            return getRemoteJWKS();
        }
        return getLocalKeystore();
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
            throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create(e, "Unable to parse JWKSet URL");
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
            
            if(Strings.isEmpty(keystorePath)) {
                throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create("Missing required property: " + OAuthJWTProperty.KEYSTORE_PATH.getFQPropertyName());
            }
            
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
            throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create(e, "Unable to load local keystore");
        }
    }

    /**
     *
     * Creates and configures {@link ConfigurableJWTProcessor} with a custom {@link OAuthJWTClaimVerifier} and {@link JWSKeySelector}.
     *
     * @return the configured {@link ConfigurableJWTProcessor}
     * @throws OXException
     */
    private ConfigurableJWTProcessor<SecurityContext> createJWTProcessor() throws OXException {
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
     * @throws OXException
     */
    private JWSKeySelector<SecurityContext> createJWSKeySelector() throws OXException {
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWKSource<SecurityContext> keySource = getKeySource();
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
        return keySelector;
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        JWSKeySelector<SecurityContext> reloadedKeySelector;
        try {
            reloadedKeySelector = createJWSKeySelector();
            jwtProcessor.setJWSKeySelector(reloadedKeySelector);
        } catch (OXException e) {
            LOG.error("Reload of OAuth JWT properties failed: ", e.getMessage());
        }
    }

    @Override
    protected String getContextLookupClaimname() {
        return leanConfService.getProperty(OAuthJWTProperty.CONTEXT_LOOKUP_CLAIM);
    }

    @Override
    protected String getContextLookupNamePart() {
        return leanConfService.getProperty(OAuthJWTProperty.CONTEXT_LOOKUP_NAME_PART);
    }

    @Override
    protected String getUserLookupClaimname() {
        return leanConfService.getProperty(OAuthJWTProperty.USER_LOOKUP_CLAIM);
    }

    @Override
    protected String getUserNameLookupPart() {
        return leanConfService.getProperty(OAuthJWTProperty.USER_LOOKUP_NAME_PART);
    }
}
