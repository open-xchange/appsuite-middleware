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

package com.openexchange.oauth.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private static final OAuthScopeConfigurationService INSTANCE = new OAuthScopeConfigurationService();
    private static final String PROPERTY_PREFIX = "com.openexchange.oauth.modules.enabled.";

    private OAuthScopeConfigurationService() {
        super();
    }

    public static OAuthScopeConfigurationService getInstance() {
        return INSTANCE;
    }

    /**
     * Filters all disabled scopes from the given list of scopes for the given user and oauth api.
     * 
     * @param availableScopes All available scopes
     * @param userId The user id
     * @param ctxId The context id
     * @param oauthApiName The OAuth api name
     * @return A set of enabled and available OAuthScopes
     * @throws OXException if the configured scopes couldn't be retrieved for the given user
     */
    Set<OAuthScope> getScopes(Set<OAuthScope> availableScopes, int userId, int ctxId, String oauthApiName) throws OXException {

        Set<OAuthScope> result = new HashSet<>();

        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(userId, ctxId);
        String enabledModulesStr = view.opt(PROPERTY_PREFIX + oauthApiName, String.class, null);
        if (enabledModulesStr == null) {
            // Fallback to all enabled
            return availableScopes;
        }
        
        List<String> enabledScopes = Arrays.asList(Strings.splitByComma(enabledModulesStr));
        
        for (OAuthScope availableScope : availableScopes) {
            if (enabledScopes.contains(availableScope.getOXScope().name())) {
                result.add(availableScope);
            }
        }

        return result;

    }

}
