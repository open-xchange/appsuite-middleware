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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.validator.routines.UrlValidator;
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
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.DefaultValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.AbstractClaimSetAuthorizationService;
import com.openexchange.oauth.provider.impl.OAuthProviderProperties;

/**
 * {@link OAuthJwtAuthorizationService} - Service provider Interface that validates and parses incoming access tokens that are JWT.
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public class OAuthJwtAuthorizationService extends AbstractClaimSetAuthorizationService implements ForcedReloadable {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthJwtAuthorizationService.class);

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    /**
     * Initializes a new {@link OAuthJwtAuthorizationService}.
     *
     * @param leanConfService
     * @param scopeService
     * @throws OXException
     */
    public OAuthJwtAuthorizationService(LeanConfigurationService leanConfService, OAuthJWTScopeService scopeService) throws OXException {
        super(leanConfService, scopeService);
        this.jwtProcessor = createJWTProcessor();
    }

    @Override
    public ValidationResponse validateAccessToken(String accessToken) throws AuthorizationException {
        DefaultValidationResponse validationResponse = new DefaultValidationResponse();
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, null);
            validationResponse = createValidationReponse(claimsSet);
        } catch (ParseException e) {
            LOG.debug("Unable to parse the received token.", e);
            validationResponse.setTokenStatus(TokenStatus.MALFORMED);
        } catch (BadJOSEException e) {
            LOG.debug("The received token is rejected, i.e. the token has expired or the issuer is unknown.", e);
            validationResponse.setTokenStatus(TokenStatus.INVALID);
        } catch (JOSEException e) {
            LOG.debug("Internal processing exception is encountered.", e);
            validationResponse.setTokenStatus(TokenStatus.UNKNOWN);
        } catch (Exception e) {
            LOG.debug("The validation of the received token failed with an unexpected error.", e);
            throw new AuthorizationException("The validation of the received token failed with an unexpected error", e);
        }
        LOG.debug("Processed JWT with token status: {}", validationResponse.getTokenStatus());
        return validationResponse;
    }

    /**
     * Depending on configuration this method gets a {@link JWKSource} from remote or a local JSON file.
     *
     * @return the loaded JWKSource.
     * @throws OXException
     */
    JWKSource<SecurityContext> getKeySource() throws OXException {
        String jwksUri = leanConfService.getProperty(OAuthJWTProperty.JWKS_URI);
        if (Strings.isNotEmpty(jwksUri)) {
            String[] schemes = { "http", "https", "file" };
            UrlValidator validator = new UrlValidator(schemes);

            if (validator.isValid(jwksUri)) {
                try {
                    URI uri = new URI(jwksUri);
                    if ("file".equals(uri.getScheme())) {
                        return getJWKSFromJson(uri);
                    }
                    return getRemoteJWKS(uri);
                } catch (URISyntaxException e) {
                    LOG.error("Unable to load JWKs, because of malformed JWKs URI", e);
                }
            }
            throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create("The OAuthJwtAuthorizationService could not be initialized because the specified JWKs URI (" + OAuthJWTProperty.JWKS_URI.getFQPropertyName() + ") is invalid");
        }
        throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create("The OAuthJwtAuthorizationService could not be initialized because no source for loading a JWKs (" + OAuthJWTProperty.JWKS_URI.getFQPropertyName() + ") has been specified");
    }

    /**
     * Fetches signature keys from configured JWKS endpoint used for validation
     *
     * @param jwksUri The configured uri of the jwks endpoint
     * @return the retrieved {@link RemoteJWKSet}
     * @throws OXException
     */
    private RemoteJWKSet<SecurityContext> getRemoteJWKS(URI jwksUri) throws OXException {
        try {
            return new RemoteJWKSet<>(jwksUri.toURL());
        } catch (MalformedURLException e) {
            throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create(e, "Unable to parse JWKSet URL");
        }
    }

    /**
     * Loads a JWK set from a local file
     *
     * @param jwksUri The configured uri to the local file
     * @return the loaded {@link JWKSet}
     * @throws OXException
     */
    private JWKSource<SecurityContext> getJWKSFromJson(URI jwksUri) throws OXException {
        try {
            File file = new File(jwksUri);
            if (file.exists() == false) {
                throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create("Unable to load JWK set from a local file. The file '{}' doesn't exist.", file.getAbsolutePath());
            }
            JWKSet localKeys = JWKSet.load(file);
            return new ImmutableJWKSet<>(localKeys);
        } catch (IOException | ParseException e) {
            throw OAuthJWTExceptionCode.SERVICE_CONFIGURATION_FAILED.create(e, "Unable to load JWK set from a local file");
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
        List<String> allowedIssuers = Arrays.asList(Strings.splitByComma(leanConfService.getProperty(OAuthProviderProperties.ALLOWED_ISSUER)));
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
        try {
            this.jwtProcessor = createJWTProcessor();
        } catch (OXException e) {
            LOG.error("Reload of OAuth JWT properties failed: {}", e.getMessage(), e);
        }
    }

}
