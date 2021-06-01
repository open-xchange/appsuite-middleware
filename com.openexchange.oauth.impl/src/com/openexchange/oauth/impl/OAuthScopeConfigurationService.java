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

package com.openexchange.oauth.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.impl.services.Services;
import com.openexchange.oauth.scope.OAuthScope;

/**
 * {@link OAuthScopeConfigurationService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class OAuthScopeConfigurationService {

    private static final String PROPERTY_PREFIX = "com.openexchange.oauth.modules.enabled.";

    private static final OAuthScopeConfigurationService INSTANCE = new OAuthScopeConfigurationService();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static OAuthScopeConfigurationService getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------

    private OAuthScopeConfigurationService() {
        super();
    }

    /**
     * Filters all disabled scopes from the given list of scopes for the given user and OAuth API.
     *
     * @param availableScopes All available scopes
     * @param userId The user identifier
     * @param ctxId The context identifier
     * @param oauthApiName The OAuth API name;: e.g. <code>"google"</code>
     * @return A set of enabled and available OAuthScopes
     * @throws OXException If the configured scopes couldn't be retrieved for the given user
     */
    Set<OAuthScope> getScopes(Set<OAuthScope> availableScopes, int userId, int ctxId, String oauthApiName) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(userId, ctxId);

        ComposedConfigProperty<String> property = view.property(PROPERTY_PREFIX + oauthApiName, String.class);

        if (false == property.isDefined()) {
            // Fall-back to all enabled
            return availableScopes;
        }

        String enabledModulesStr = property.get();
        if (Strings.isEmpty(enabledModulesStr)) {
            // Defined, but none set
            return Collections.emptySet();
        }

        String[] tokens = Strings.splitByComma(enabledModulesStr);
        if (null == tokens || tokens.length <= 0) {
            // Defined, but none set
            return Collections.emptySet();
        }

        Set<String> enabledScopes = new LinkedHashSet<>(Arrays.asList(tokens));
        ImmutableSet.Builder<OAuthScope> result = ImmutableSet.builder();
        for (OAuthScope availableScope : availableScopes) {
            if (enabledScopes.contains(availableScope.getOXScope().name())) {
                result.add(availableScope);
            }
        }
        return result.build();
    }

}
