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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;

/**
 * {@link OAuthJWTScopeService} - Provides scope-mapping from external Authorization Server scopes to internal MW scopes
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public class OAuthJWTScopeService implements Reloadable {

    private static final String SCOPE_PREFIX = "com.openexchange.oauth.provider.scope.";
    private final LeanConfigurationService leanConfigurationService;

    private Map<String, List<String>> configuredScopes;

    /**
     * Initializes a new {@link OAuthJWTScopeService}.
     *
     * @param leanConfigurationService
     */
    public OAuthJWTScopeService(LeanConfigurationService leanConfigurationService) {
        this.leanConfigurationService = leanConfigurationService;
        this.configuredScopes = loadProperties();
    }

    /**
     * Processes incoming scopes that may have to be replaced by internal middleware scopes.
     *
     * @param externalScopes - String representation of the received scopes.
     * @return resolvedScopes - List of processed and substituted scopes that can be interpreted by the corresponding OAuthAction
     */
    public List<String> getInternalScopes(String externalScopes) {
        return getInternalScopes(parse(externalScopes));
    }

    /**
     * Processes incoming scopes that may have to be replaced by internal middleware scopes.
     *
     * @param externalScopes - String representation of the received scopes
     * @return resolvedScopes - List of processed and substituted scopes that can be interpreted by the corresponding OAuthAction
     */
    public List<String> getInternalScopes(List<String> externalScopes) {
        if (configuredScopes.isEmpty()) {
            return externalScopes;
        }

        List<String> resolvedScopes = new ArrayList<String>();
        for (String scope : externalScopes) {
            if (configuredScopes.containsKey(scope)) {
                List<String> scopeProperty = configuredScopes.get(scope);
                if (!scopeProperty.isEmpty()) {
                    resolvedScopes.addAll(scopeProperty);
                }
            } else {
                resolvedScopes.add(scope);
            }
        }

        return resolvedScopes;
    }

    /**
     * Using a prefix, all configured external scopes are loaded with their internal scope mapping.
     * To make the later mapping easier, this prefix is then removed from each key of the returned map.
     * E.g. mail = [read_mail, write_mail]
     *
     * @return Preprocessed map of external scopes and their internal representation
     */
    private synchronized Map<String, List<String>> loadProperties() {
        Map<String, String> configuredScopes = leanConfigurationService.getProperties((k, v) -> k.startsWith(SCOPE_PREFIX));

        Map<String, List<String>> processedScopes = new HashMap<String, List<String>>();
        for (Map.Entry<String, String> entry : configuredScopes.entrySet()) {
            String processedKey = entry.getKey().substring(SCOPE_PREFIX.length());
            processedScopes.put(processedKey, parse(entry.getValue()));
        }

        return processedScopes;
    }

    /**
     * Splits given scopes string by space or comma.
     *
     * @param scopes the string to split
     * @return the split string
     */
    private static List<String> parse(final String scopes) {
        if (scopes == null) {
            return null;
        }

        if (scopes.trim().isEmpty()) {
            return null;
        }

        List<String> scope = new ArrayList<>();

        // OAuth specifies space as delimiter, also support comma (old draft)
        StringTokenizer st = new StringTokenizer(scopes, " ,");

        while (st.hasMoreTokens()) {
            scope.add(st.nextToken());
        }

        return scope;
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.oauth.provider.scope.*");
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        this.configuredScopes = loadProperties();
    }
}
