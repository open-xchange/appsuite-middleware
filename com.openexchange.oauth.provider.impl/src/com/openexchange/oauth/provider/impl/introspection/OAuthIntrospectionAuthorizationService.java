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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Token;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.DefaultValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.AbstractClaimSetAuthorizationService;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTClaimVerifier;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTScopeService;

/**
 * {@link OAuthIntrospectionAuthorizationService}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since 7.10.5
 */
public class OAuthIntrospectionAuthorizationService extends AbstractClaimSetAuthorizationService implements Reloadable {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthIntrospectionAuthorizationService.class);

    private final LoadingCache<String, TokenIntrospectionResponse> cache;

    /**
     * Initializes a new {@link OAuthIntrospectionAuthorizationService}.
     *
     * @param leanConfService the {@link LeanConfigurationService}
     * @param scopeService the {@link OAuthJWTScopeService}
     */
    public OAuthIntrospectionAuthorizationService(LeanConfigurationService leanConfService, OAuthJWTScopeService scopeService) {
        super(leanConfService, scopeService);
        this.cache = Caffeine.newBuilder().expireAfter(new Expiry<String, TokenIntrospectionResponse>() {

            @Override
            public long expireAfterCreate(String key, TokenIntrospectionResponse resp, long currentTime) {
                long TTL;
                if (resp.indicatesSuccess() && ((TokenIntrospectionSuccessResponse)resp).isActive()) {
                    long expiration = ((TokenIntrospectionSuccessResponse) resp).getExpirationTime().getTime();
                    long current = System.currentTimeMillis();

                    //The response contains the "exp" parameter (expiration), the response
                    //MUST NOT be cached beyond the time indicated therein - RFC 7662
                    TTL = expiration - current;
                    return TimeUnit.MILLISECONDS.toNanos(TTL);
                }

                // Store unsuccessful responses for 30 seconds
                return TimeUnit.SECONDS.toNanos(30);
            }

            @Override
            public long expireAfterUpdate(String key, TokenIntrospectionResponse resp, long currentTime, long currentDuration) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(String key, TokenIntrospectionResponse resp, long currentTime, long currentDuration) {
                return currentDuration;
            }
        }).build(key -> introspect(key));

    }

    @Override
    public ValidationResponse validateAccessToken(String accessToken) throws AuthorizationException {
        TokenIntrospectionResponse introspectionResponse = null;
        try {
            introspectionResponse = cache.get(accessToken);
        } catch (CompletionException e) {
            if (e.getCause() instanceof AuthorizationException) {
                throw (AuthorizationException) e.getCause();
            }
            throw new AuthorizationException(e.getCause());
        }
        if (null != introspectionResponse && introspectionResponse.indicatesSuccess()) {
            DefaultValidationResponse validationResponse = new DefaultValidationResponse();
            TokenIntrospectionSuccessResponse introspectionSuccessResponse = (TokenIntrospectionSuccessResponse) introspectionResponse;
            if (!introspectionSuccessResponse.isActive()) {
                validationResponse.setTokenStatus(TokenStatus.EXPIRED);
                return validationResponse;
            }
            try {
                JWTClaimsSet claimsSet = parseResponseToClaimSet(introspectionSuccessResponse);
                validationResponse = createValidationReponse(claimsSet);
            } catch (BadJWTException e) {
                LOG.debug("Bad JSON Web Token (JWT)", e);
                validationResponse.setTokenStatus(TokenStatus.INVALID);
            } catch (java.text.ParseException e) {
                LOG.debug("Parsing of JSON Web Token (JWT) failed", e);
                validationResponse.setTokenStatus(TokenStatus.MALFORMED);
            } catch (Exception e) {
                LOG.debug("Token introspection failed with unexpected error", e);
                throw new AuthorizationException("Token introspection failed with unexpected error", e);
            }

            LOG.debug("Processed response from introspection request with token status: {}", validationResponse.getTokenStatus());
            return validationResponse;
        }
        if(null != introspectionResponse) {
            HTTPResponse httpResponse = introspectionResponse.toHTTPResponse();
            LOG.debug("Token introspection failed with response error: {} {}: {}", I(httpResponse.getStatusCode()), httpResponse.getStatusMessage(), httpResponse.getContent());
        } else {
            LOG.debug("Token introspection response was null");
        }
        throw new AuthorizationException("Token introspection failed with response error");
    }

    /**
     * Parse the received {@link TokenIntrospectionSuccessResponse} to a {@link JWTClaimsSet} for further processing.
     *
     * @param introspectionSuccessResponse
     * @return the parsed {@link JWTClaimsSet}
     * @throws java.text.ParseException
     * @throws BadJWTException
     */
    private JWTClaimsSet parseResponseToClaimSet(TokenIntrospectionSuccessResponse introspectionSuccessResponse) throws java.text.ParseException, BadJWTException {
        JWTClaimsSet claimsSet = JWTClaimsSet.parse(introspectionSuccessResponse.toJSONObject());
        OAuthJWTClaimVerifier.DEFAULT_VERIFIER.verify(claimsSet, null);
        return claimsSet;
    }

    /**
     * Introspect and verify that the provided token is active.
     *
     * @param accessToken the token to introspect
     * @return {@link TokenIntrospectionResponse} containing the token's attributes
     * @throws AuthorizationException
     */
    public TokenIntrospectionResponse introspect(String accessToken) throws AuthorizationException {
        try {
            return makeRequest(accessToken);
        } catch (Exception e) {
            throw new AuthorizationException("Token introspection request failed", e);
        }
    }

    /**
     * Send the introspection request and retrieve the response.
     *
     * @param accessToken the access token to inspect
     * @return the {@link TokenIntrospectionResponse}
     * @throws AuthorizationException
     */
    private TokenIntrospectionResponse makeRequest(String accessToken) throws AuthorizationException {
        HTTPRequest httpRequest = buildRequest(accessToken);
        try {
            HTTPResponse httpResponse = httpRequest.send();
            return parseResponse(httpResponse);
        } catch (IOException e) {
            throw new AuthorizationException("Unable to send introspection request", e);
        }
    }

    /**
     * Parses a token introspection response from the specified {@link HTTPResponse}.
     *
     * @param response the {@link HTTPResponse} from introspection request.
     * @return Either {@link TokenIntrospectionSuccessResponse} or {@link TokenIntrospectionErrorResponse}.
     * @throws AuthorizationException in case of a parsing error
     */
    private TokenIntrospectionResponse parseResponse(HTTPResponse response) throws AuthorizationException {
        try {
            return TokenIntrospectionResponse.parse(response);
        } catch (ParseException e) {
            throw new AuthorizationException("Unable to parse introspection response", e);
        }
    }

    /**
     * Creates an introspection request based on the received access token and the given configuration.
     *
     * @param accessToken the received access token
     * @return configured {@link HTTPRequest}
     * @throws AuthorizationException
     */
    private HTTPRequest buildRequest(String accessToken) throws AuthorizationException {
        Token token = new BearerAccessToken(accessToken);

        URI introspectionEndpoint = getIntrospectionEndpoint();

        ClientSecretBasic clientSecretBasic = null;
        boolean basicAuthEnabled = leanConfService.getBooleanProperty(OAuthIntrospectionProperty.BASIC_AUTH_ENABLED);
        if (basicAuthEnabled) {
            clientSecretBasic = createClientSecretBasic();
        }

        TokenIntrospectionRequest request;
        if (basicAuthEnabled) {
            request = new TokenIntrospectionRequest(introspectionEndpoint, clientSecretBasic, token);
        }else {
            request = new TokenIntrospectionRequest(introspectionEndpoint, token);
        }

        return request.toHTTPRequest();
    }

    /**
     * Get the introspection endpoint.
     *
     * @return the introspection endpoint URI
     * @throws AuthorizationException
     */
    private URI getIntrospectionEndpoint() throws AuthorizationException {
        String introspectionEndpoint = leanConfService.getProperty(OAuthIntrospectionProperty.ENDPOINT);
        if (Strings.isNotEmpty(introspectionEndpoint)) {
            try {
                return new URI(leanConfService.getProperty(OAuthIntrospectionProperty.ENDPOINT));
            } catch (URISyntaxException e) {
                throw new AuthorizationException("Unable to parse introspection endpoint", e);
            }
        }
        LOG.debug("The mandatory property " + OAuthIntrospectionProperty.ENDPOINT.getFQPropertyName() + " is empty");
        throw new AuthorizationException("Token introspection failed because of internal errors: Endpoint is null  or empty");
    }

    /**
     * Create client secret basic.
     *
     * @return the client secret basic authentication
     * @throws AuthorizationException
     */
    private ClientSecretBasic createClientSecretBasic() throws AuthorizationException {
        String clientID = leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_ID);
        String secret = leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_SECRET);
        if (Strings.isEmpty(clientID) || Strings.isEmpty(secret)) {
            throw new AuthorizationException("Token introspection failed because of internal errors: Unable to load credentials for introsprection basic auth");
        }
        return new ClientSecretBasic(new ClientID(clientID), new Secret(secret));
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(
            OAuthIntrospectionProperty.ENDPOINT.getFQPropertyName(),
            OAuthIntrospectionProperty.BASIC_AUTH_ENABLED.getFQPropertyName(),
            OAuthIntrospectionProperty.CLIENT_ID.getFQPropertyName(),
            OAuthIntrospectionProperty.CLIENT_SECRET.getFQPropertyName()
       );
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        LOG.info("Reloading configuration for OAuthIntrospectionAuthorizationService");
        this.cache.invalidateAll();

        //Make sure everything is configured
        if(Strings.isEmpty(leanConfService.getProperty(OAuthIntrospectionProperty.ENDPOINT))) {
            LOG.error("Error reloading introspection configuration: Introspection endpoint is not configured properly.");
        }

        boolean basicAuthEnabled = leanConfService.getBooleanProperty(OAuthIntrospectionProperty.BASIC_AUTH_ENABLED);
        if(basicAuthEnabled) {
            if(Strings.isEmpty(leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_ID))) {
                LOG.error("Error reloading introspection configuration: Basic auth is enabled but client id is empty.");
            }
            if(Strings.isEmpty(leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_SECRET))) {
                LOG.error("Error reloading introspection configuration: Basic auth is enabled but client secret is empty.");
            }
        }
    }
}
