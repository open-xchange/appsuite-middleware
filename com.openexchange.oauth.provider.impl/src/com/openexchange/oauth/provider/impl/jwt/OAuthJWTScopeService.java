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
