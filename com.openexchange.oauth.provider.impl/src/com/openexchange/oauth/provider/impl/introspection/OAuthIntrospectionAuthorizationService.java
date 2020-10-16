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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jose.proc.SecurityContext;
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
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.impl.AbstractAuthorizationService;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTClaimVerifier;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTScopeService;

/**
 * {@link OAuthIntrospectionAuthorizationService}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since 7.10.5
 */
public class OAuthIntrospectionAuthorizationService extends AbstractAuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthIntrospectionAuthorizationService.class);

    private LeanConfigurationService leanConfService;
    private final LoadingCache<String, TokenIntrospectionResponse> cache;

    /**
     * Initializes a new {@link OAuthIntrospectionAuthorizationService}.
     * 
     * @param leanConfService the {@link LeanConfigurationService}
     * @param scopeService the {@link OAuthJWTScopeService}
     */
    public OAuthIntrospectionAuthorizationService(LeanConfigurationService leanConfService, OAuthJWTScopeService scopeService) {
        super(scopeService);
        this.leanConfService = leanConfService;
        this.cache = Caffeine.newBuilder().expireAfter(new Expiry<String, TokenIntrospectionResponse>() {

            @Override
            public long expireAfterCreate(String key, TokenIntrospectionResponse resp, long currentTime) {
                long TTL;
                if (resp.indicatesSuccess()) {
                    long expiration = ((TokenIntrospectionSuccessResponse) resp).getExpirationTime().getTime();
                    long current = System.currentTimeMillis();
                    
                    //The response contains the "exp" parameter (expiration), the response
                    //MUST NOT be cached beyond the time indicated therein - RFC 7662   
                    TTL = expiration - current;
                } else {
                    TTL = TimeUnit.MILLISECONDS.convert(30L, TimeUnit.SECONDS);
                }

                return TimeUnit.MILLISECONDS.toNanos(TTL);
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

        TokenIntrospectionResponse introspectionResponse = cache.get(accessToken);
        if (introspectionResponse.indicatesSuccess()) {
            TokenIntrospectionSuccessResponse introspectionSuccessResponse = (TokenIntrospectionSuccessResponse) introspectionResponse;
            if (!introspectionSuccessResponse.isActive()) {
                throw new AuthorizationException("Provided access token isn't active");
            }
            try {
                JWTClaimsSet claimsSet = parseResponseToClaimSet(introspectionSuccessResponse);
                return createValidationReponse(claimsSet);
            } catch (OXException | java.text.ParseException | BadJWTException e) {
                LOG.error("Provided access token can't be processed", e);
                throw new AuthorizationException(e);
            }
        }
        throw new AuthorizationException("The passed access token is invalid");
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
        OAuthJWTClaimVerifier<SecurityContext> claimVerifier = new OAuthJWTClaimVerifier<SecurityContext>(Arrays.asList(""));
        JWTClaimsSet claimsSet = JWTClaimsSet.parse(introspectionSuccessResponse.toJSONObject());
        claimVerifier.verify(claimsSet);
        return claimsSet;
    }

    /**
     * Introspect and verify that the provided token is active.
     *
     * @param accessToken the token to introspect
     * @return {@link TokenIntrospectionSuccessResponse} containing the token's attributes
     * @throws AuthorizationException
     */
    public TokenIntrospectionResponse introspect(String accessToken) throws AuthorizationException {
        try {
            return makeRequest(accessToken);
        } catch (OXException e) {
            throw new AuthorizationException("Introspection failed", e);
        }
    }

    /**
     * Send the introspection request and retrieve the response.
     *
     * @param httpRequest the {@link HTTPRequest}
     * @return the {@link HTTPResponse}
     * @throws OXException
     */
    private TokenIntrospectionResponse makeRequest(String accessToken) throws OXException {
        HTTPRequest httpRequest = buildRequest(accessToken);
        try {
            HTTPResponse httpResponse = httpRequest.send();
            return parseResponse(httpResponse);
        } catch (IOException e) {
            throw OAuthIntrospectionExceptionCode.VALIDATON_FAILED.create(e, "Unable to send introspection request");
        }
    }

    /**
     * Parses a token introspection response from the specified {@link HTTPResponse}.
     *
     * @param response the {@link HTTPResponse} from introspection request.
     * @return Either {@link TokenIntrospectionSuccessResponse} or {@link TokenIntrospectionErrorResponse}.
     * @throws OXException
     */
    private TokenIntrospectionResponse parseResponse(HTTPResponse response) throws OXException {
        try {
            return TokenIntrospectionResponse.parse(response);
        } catch (ParseException e) {
            throw OAuthIntrospectionExceptionCode.VALIDATON_FAILED.create(e, "Unable to parse introspection request");
        }
    }

    /**
     * Creates an introspection request based on the received access token and the given configuration.
     *
     * @param accessToken the received access token
     * @return configured {@link HTTPRequest}
     * @throws OXException
     */
    private HTTPRequest buildRequest(String accessToken) throws OXException {
        Token token = new BearerAccessToken(accessToken);
        
        ClientSecretBasic clientSecretBasic = null;
        boolean basicAuthEnabled = leanConfService.getBooleanProperty(OAuthIntrospectionProperty.BASIC_AUTH_ENABLED);
        if (basicAuthEnabled) {
            clientSecretBasic = createClientSecretBasic();
        }

        URI introspectionEndpoint = getIntrospectionEndpoint();

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
     * @throws OXException
     */
    private URI getIntrospectionEndpoint() throws OXException {
        String introspectionEndpoint = leanConfService.getProperty(OAuthIntrospectionProperty.ENDPOINT);
        if (!Strings.isEmpty(introspectionEndpoint)) {
            try {
                return new URI(leanConfService.getProperty(OAuthIntrospectionProperty.ENDPOINT));
            } catch (URISyntaxException e) {
                throw OAuthIntrospectionExceptionCode.VALIDATON_FAILED.create(e, "Unable to parse introspection endpoint");
            }
        }
        throw OAuthIntrospectionExceptionCode.VALIDATON_FAILED.create("Unable to parse introspection endpoint: Endpoint is null or empty");
    }

    /**
     * Create client secret basic.
     *
     * @return the client secret basic authentication
     * @throws OXException
     */
    private ClientSecretBasic createClientSecretBasic() throws OXException {
        String clientID = leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_ID);
        String secret = leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_SECRET);
        if (clientID.isEmpty() || secret.isEmpty()) {
            throw OAuthIntrospectionExceptionCode.UNABLE_TO_LOAD_CLIENT_CREDENTIALS.create();
        }
        return new ClientSecretBasic(new ClientID(clientID), new Secret(secret));
    }

    @Override
    protected String getContextLookupClaimname() {
        return leanConfService.getProperty(OAuthIntrospectionProperty.CONTEXT_LOOKUP_CLAIM);
    }

    @Override
    protected String getContextLookupNamePart() {
        return leanConfService.getProperty(OAuthIntrospectionProperty.CONTEXT_LOOKUP_NAME_PART);
    }

    @Override
    protected String getUserLookupClaimname() {
        return leanConfService.getProperty(OAuthIntrospectionProperty.USER_LOOKUP_CLAIM);
    }

    @Override
    protected String getUserNameLookupPart() {
        return leanConfService.getProperty(OAuthIntrospectionProperty.USER_LOOKUP_NAME_PART);
    }
}
