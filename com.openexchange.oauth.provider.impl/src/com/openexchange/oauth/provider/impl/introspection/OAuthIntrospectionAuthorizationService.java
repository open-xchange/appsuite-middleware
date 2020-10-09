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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jwt.util.DateUtils;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Token;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
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
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTScopeService;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link OAuthIntrospectionAuthorizationService}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 */
public class OAuthIntrospectionAuthorizationService implements OAuthAuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthIntrospectionAuthorizationService.class);    

    private LeanConfigurationService leanConfService;
    private OAuthJWTScopeService scopeService;
    private final LoadingCache<String, TokenIntrospectionSuccessResponse> cache;

    public OAuthIntrospectionAuthorizationService(LeanConfigurationService leanConfService, OAuthJWTScopeService scopeService) {
        super();
        this.leanConfService = leanConfService;
        this.scopeService = scopeService;
        this.cache = Caffeine.newBuilder().expireAfter(new Expiry<String, TokenIntrospectionSuccessResponse>() {

            public long expireAfterCreate(String key, TokenIntrospectionSuccessResponse resp, long currentTime) {
                long expirationTime = DateUtils.toSecondsSinceEpoch(resp.getExpirationTime());
                long current = System.currentTimeMillis() / 1000l;

                //The response contains the "exp" parameter (expiration), the response
                //MUST NOT be cached beyond the time indicated therein - RFC 7662   
                long TTL = expirationTime - current;
                return TimeUnit.SECONDS.toSeconds(TTL);
            }

            public long expireAfterUpdate(String key, TokenIntrospectionSuccessResponse resp, long currentTime, long currentDuration) {
                return currentDuration;
            }

            public long expireAfterRead(String key, TokenIntrospectionSuccessResponse resp, long currentTime, long currentDuration) {
                return currentDuration;
            }
        }).build(key -> introspect(key));

    }

    @Override
    public ValidationResponse validateAccessToken(String accessToken) throws AuthorizationException {

        TokenIntrospectionSuccessResponse introspectionSuccessResponse = cache.get(accessToken);
        DefaultValidationResponse validationResponse = new DefaultValidationResponse();

        try {
            Map<String, Object> claims = parseClaims(introspectionSuccessResponse);

            String contextLookupParameter = leanConfService.getProperty(OAuthIntrospectionProperty.CONTEXT_LOOKUP_CLAIM);
            String contextLookup = claims.get(contextLookupParameter).toString();
            NamePart contextNamePart = NamePart.of(leanConfService.getProperty(OAuthIntrospectionProperty.CONTEXT_LOOKUP_NAME_PART));
            String contextInfo = contextNamePart.getFrom(contextLookup, Authenticated.DEFAULT_CONTEXT_INFO);
            Context context = resolveContext(contextInfo);
            validationResponse.setContextId(context.getContextId());

            String userLookupParameter = leanConfService.getProperty(OAuthIntrospectionProperty.USER_LOOKUP_CLAIM);
            String userLookup = claims.get(userLookupParameter).toString();
            NamePart userNamePart = NamePart.of(leanConfService.getProperty(OAuthIntrospectionProperty.USER_LOOKUP_NAME_PART));
            String userInfo = userNamePart.getFrom(userLookup, userLookup);
            int userId = resolveUser(context, userInfo);
            validationResponse.setUserId(userId);

            String oauthClient = claims.get(OAuthIntrospectionConstants.CLIENT_ID).toString();
            validationResponse.setClientName(oauthClient);

            List<String> scopes = (List<String>) claims.get(OAuthIntrospectionConstants.SCOPE);

            List<String> resolvedScopes = scopeService.getInternalScopes(scopes);
            validationResponse.setScope(resolvedScopes);

            validationResponse.setTokenStatus(TokenStatus.VALID);
        } catch (OXException e) {
            LOG.error("Provided token can't be processed", e);
        }

        return validationResponse;
    } 

    protected TokenIntrospectionSuccessResponse introspect(String accessToken) throws AuthorizationException {

        TokenIntrospectionResponse introspectionResponse = null;
        try {
            HTTPRequest request =  buildIntrospectionRequest(accessToken);

            HTTPResponse response = makeIntrospectionRequest(request);

            introspectionResponse = parseResponse(response);
        } catch (OXException e) {
            LOG.error("Introspection failed", e);
        }

        TokenIntrospectionSuccessResponse introspectionSuccessResponse = checkSuccess(introspectionResponse);

        if (!introspectionSuccessResponse.isActive()) {
            throw new AuthorizationException("Provided token isn't active");
        }

        return introspectionSuccessResponse;
    }

    private HTTPRequest buildIntrospectionRequest(String accessToken) throws OXException {
        Token token = new BearerAccessToken(accessToken);
        
        ClientSecretBasic clientSecretBasic = null;
        Boolean basicAuthEnabled = leanConfService.getBooleanProperty(OAuthIntrospectionProperty.BASIC_AUTH_ENABLED);
        if (basicAuthEnabled) {
            if(leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_ID) == null || leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_SECRET) == null) {
                throw OAuthIntrospectionExceptionCode.UNABLE_TO_LOAD_CLIENT_CREDENTIALS.create();
            }
            
            ClientID clientID = new ClientID(leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_ID));
            Secret secret = new Secret(leanConfService.getProperty(OAuthIntrospectionProperty.CLIENT_SECRET));
            clientSecretBasic = new ClientSecretBasic(clientID, secret);
        }

        URI introspectionEndpoint = null;
        try {
            introspectionEndpoint = new URI(leanConfService.getProperty(OAuthIntrospectionProperty.ENDPOINT));
        } catch (URISyntaxException e) {
            throw OAuthIntrospectionExceptionCode.VALIDATON_FAILED.create(e, "Unable to parse introspection endpoint");
        }

        TokenIntrospectionRequest request = null;
        if (basicAuthEnabled) {
            request = new TokenIntrospectionRequest(introspectionEndpoint, clientSecretBasic, token);
        }else {
            request = new TokenIntrospectionRequest(introspectionEndpoint, token);
        }
        
        return request.toHTTPRequest();
    }

    private HTTPResponse makeIntrospectionRequest(HTTPRequest httpRequest) throws OXException {
        try {
            return httpRequest.send();
        } catch (IOException e) {
            throw OAuthIntrospectionExceptionCode.VALIDATON_FAILED.create(e, "Unable to send introspection request");
        }
    }

    private TokenIntrospectionResponse parseResponse(HTTPResponse response) throws OXException {
        try {
            return TokenIntrospectionResponse.parse(response);
        } catch (ParseException e) {
            throw OAuthIntrospectionExceptionCode.VALIDATON_FAILED.create(e, "Unable to parse introspection request");
        }
    }

    private TokenIntrospectionSuccessResponse checkSuccess(TokenIntrospectionResponse introspectionResponse) {
        if (introspectionResponse.indicatesSuccess()) {
            LOG.error("Failed introspection");
        }
        return (TokenIntrospectionSuccessResponse) introspectionResponse;
    }

    private Map<String, Object> parseClaims(TokenIntrospectionSuccessResponse response) throws OXException {

        Map<String, Object> claims = new HashMap<String, Object>();

        if (response.getClientID() != null) {
            claims.put(OAuthIntrospectionConstants.CLIENT_ID, response.getClientID().getValue());
        }

        if (response.getUsername() != null) {
            claims.put(OAuthIntrospectionConstants.USERNAME, response.getUsername());
        }

        if (response.getTokenType() != null) {
            claims.put(OAuthIntrospectionConstants.TOKEN_TYPE, response.getTokenType().getValue());
        }

        if (response.getExpirationTime() != null) {
            claims.put(OAuthIntrospectionConstants.EXPIRATION_TIME, DateUtils.toSecondsSinceEpoch(response.getExpirationTime()));
        }

        if (response.getIssueTime() != null) {
            claims.put(OAuthIntrospectionConstants.ISSUE_TIME, DateUtils.toSecondsSinceEpoch(response.getIssueTime()));
        }

        if (response.getNotBeforeTime() != null) {
            claims.put(OAuthIntrospectionConstants.NOT_BEFORE, DateUtils.toSecondsSinceEpoch(response.getNotBeforeTime()));
        }

        if (response.getSubject() != null) {
            claims.put(OAuthIntrospectionConstants.SUBJECT, response.getSubject().getValue());
        }

        if (response.getAudience() != null) {
            claims.put(OAuthIntrospectionConstants.AUDIENCE, Audience.toStringList(response.getAudience()));
        }

        if (response.getIssuer() != null) {
            claims.put(OAuthIntrospectionConstants.ISSUER, response.getIssuer().getValue());
        }

        if (response.getJWTID() != null) {
            claims.put(OAuthIntrospectionConstants.JWTID, response.getJWTID().getValue());
        }

        if (response.getScope() != null) {
            List<String> scopes = Collections.unmodifiableList(response.getScope().toStringList());
            claims.put(OAuthIntrospectionConstants.SCOPE, scopes);
        }

        JSONObject jsonResponse = response.toJSONObject();

        String contextLookupParameter = leanConfService.getProperty(OAuthIntrospectionProperty.CONTEXT_LOOKUP_CLAIM);
        if (Strings.isEmpty(contextLookupParameter)) {
            throw OAuthIntrospectionExceptionCode.UNABLE_TO_LOAD_USERINFO.create("Unable to get a valid context claim: " + OAuthIntrospectionProperty.CONTEXT_LOOKUP_CLAIM);
        }
        String contextLookup = getCustomClaim(jsonResponse, contextLookupParameter);
        claims.put(contextLookupParameter, contextLookup);

        String userLookupParameter = leanConfService.getProperty(OAuthIntrospectionProperty.USER_LOOKUP_CLAIM);
        if (Strings.isEmpty(userLookupParameter)) {
            throw OAuthIntrospectionExceptionCode.UNABLE_TO_LOAD_USERINFO.create("Unable to get a valid user claim: " + OAuthIntrospectionProperty.CONTEXT_LOOKUP_CLAIM);
        }
        String userLookup = getCustomClaim(jsonResponse, userLookupParameter);
        claims.put(userLookupParameter, userLookup);

        return claims;
    }

    private Context resolveContext(String contextInfo) throws OXException {
        ContextService contextService = Services.requireService(ContextService.class);
        int contextId = contextService.getContextId(contextInfo);

        if (contextId < 0) {
            LOG.debug("Unknown context for login mapping '{}'", contextInfo);
            throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(contextInfo);
        }

        LOG.debug("Resolved context {} for login mapping '{}'", I(contextId), contextInfo);

        return contextService.getContext(contextId);
    }

    private int resolveUser(Context context, String userInfo) throws OXException {
        UserService userService = Services.requireService(UserService.class);
        try {
            int userId = userService.getUserId(userInfo, context);
            LOG.debug("Resolved user {} in context {} for '{}' ", I(userId), I(context.getContextId()), userInfo);
            return userId;
        } catch (OXException e) {
            if (LdapExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Unknown user in context {} for '{}' ", I(context.getContextId()), userInfo);
                throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(userInfo);
            }

            throw e;
        }
    }

    private String getCustomClaim(JSONObject params, String claimName) throws OXException {
        try {
            return JSONObjectUtils.getString(params, claimName);
        } catch (ParseException e) {
            throw OAuthIntrospectionExceptionCode.UNABLE_TO_PARSE_CLAIM.create(e, claimName);
        }
    }
}
