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

package com.openexchange.oauth.provider.impl;

import static com.openexchange.java.Autoboxing.I;
import java.text.ParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.jwt.JWTClaimsSet;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.spi.DefaultValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTClaimVerifier;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTExceptionCode;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTScopeService;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link AbstractClaimSetAuthorizationService}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since 7.10.5
 */
public abstract class AbstractClaimSetAuthorizationService implements OAuthAuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractClaimSetAuthorizationService.class);

    private final OAuthJWTScopeService scopeService;
    protected final LeanConfigurationService leanConfService;

    /**
     * Initializes a new {@link AbstractClaimSetAuthorizationService}.
     *
     * @param scopeService
     */
    protected AbstractClaimSetAuthorizationService(LeanConfigurationService leanConfService, OAuthJWTScopeService scopeService) {
        this.scopeService = scopeService;
        this.leanConfService = leanConfService;
    }


    /**
     * Creates a {@link ValidationResponse} from a given {@link JWTClaimsSet}.
     *
     * @param claimsSet
     * @return {@link DefaultValidationResponse}
     * @throws ParseException
     * @throws OXException
     */
    protected DefaultValidationResponse createValidationReponse(JWTClaimsSet claimsSet) throws ParseException, OXException {
        DefaultValidationResponse response = new DefaultValidationResponse();

        response.setClientName(claimsSet.getStringClaim(OAuthJWTClaimVerifier.AUTHORIZED_PARTY_CLAIM_NAME));

        Context ctx = resolveContext(claimsSet);
        response.setContextId(ctx.getContextId());

        int userId = resolveUser(claimsSet, ctx);
        response.setUserId(userId);

        List<String> scopes = scopeService.getInternalScopes(claimsSet.getStringClaim(OAuthJWTClaimVerifier.SCOPE_CLAIM_NAME));
        response.setScope(scopes);

        response.setTokenStatus(TokenStatus.VALID);

        return response;
    }


    /**
     * Determines the {@link Context} of a user for which a {@link JWTClaimsSet} has been obtained.
     * The corresponding {@link Context} is resolved from configured claim (default = "sub").
     *
     * @param claimsSet contains all claims
     * @return the resolved {@link Context}.
     * @throws OXException
     * @throws ParseException
     */
    private Context resolveContext(JWTClaimsSet claimsSet) throws OXException, ParseException {

        String contextLookupParameter = getContextLookupClaimname();
        if (Strings.isEmpty(contextLookupParameter)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(contextLookupParameter);
        }

        String contextLookup = claimsSet.getStringClaim(contextLookupParameter);
        if (contextLookup == null) {
            throw OAuthJWTExceptionCode.UNABLE_TO_PARSE_CLAIM.create(contextLookup);
        }

        NamePart namePart = NamePart.of(getContextLookupNamePart());
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
     * Determines the user ID for which a {@link JWTClaimsSet} has been obtained.
     * The corresponding user is resolved from configured claim (default = "sub").
     *
     * @param claimsSet contains all claims
     * @param context context of the user
     * @return the resolved user
     * @throws OXException
     * @throws ParseException
     */
    private int resolveUser(JWTClaimsSet claimsSet, Context context) throws OXException, ParseException {
        String userLookupParameter = getUserLookupClaimname();
        if (Strings.isEmpty(userLookupParameter)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(userLookupParameter);
        }

        String userLookup = claimsSet.getStringClaim(userLookupParameter);
        if (userLookup == null) {
            throw OAuthJWTExceptionCode.UNABLE_TO_PARSE_CLAIM.create(userLookup);
        }

        NamePart namePart = NamePart.of(getUserNameLookupPart());
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
     * Get context lookup claimname.
     *
     * @return the context lookup claimname
     */
    protected String getContextLookupClaimname() {
        return leanConfService.getProperty(OAuthProviderProperties.CONTEXT_LOOKUP_CLAIM);
    }

    /**
     * Get context lookup {@link NamePart}.
     *
     * @return the context lookup {@link NamePart}
     */
    protected String getContextLookupNamePart() {
        return leanConfService.getProperty(OAuthProviderProperties.CONTEXT_LOOKUP_NAME_PART);
    }

    /**
     * Get user lookup claimname.
     *
     * @return the user lookup claim name
     */
    protected String getUserLookupClaimname() {
        return leanConfService.getProperty(OAuthProviderProperties.USER_LOOKUP_CLAIM);
    }

    /**
     * Get user lookup {@link NamePart}.
     *
     * @return the user lookup {@link NamePart}
     */
    protected String getUserNameLookupPart() {
        return leanConfService.getProperty(OAuthProviderProperties.USER_LOOKUP_NAME_PART);
    }




}
