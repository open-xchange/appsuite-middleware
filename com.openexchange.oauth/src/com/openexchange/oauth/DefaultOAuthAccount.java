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

package com.openexchange.oauth;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.oauth.scope.OAuthScope;

/**
 * {@link DefaultOAuthAccount}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DefaultOAuthAccount extends DefaultOAuthToken implements OAuthAccount {

    private int id;
    private String displayName;
    private OAuthServiceMetaData metaData;
    private Set<OAuthScope> enabledScopes;
    private boolean enabledScopesSet;
    private String userIdentity;

    /**
     * Initializes a new {@link DefaultOAuthAccount}.
     */
    public DefaultOAuthAccount() {
        super();
        enabledScopes = Collections.emptySet();
        enabledScopesSet = false;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public OAuthServiceMetaData getMetaData() {
        return metaData;
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the display name
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the meta data
     *
     * @param metaData The meta data to set
     */
    public void setMetaData(final OAuthServiceMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        final String delim = ", ";
        return new StringBuilder(64).append("( id = ").append(this.id).append(delim).append("displayName = ").append(this.displayName).append(delim).append("metaData = ").append(this.metaData).append(delim).append(" )").toString();
    }

    @Override
    public API getAPI() {
        return metaData.getAPI();
    }

    @Override
    public Set<OAuthScope> getEnabledScopes() {
        return enabledScopes;
    }

    /**
     * Adds specified scope to the set of enabled of enabled scopes for this OAuth account
     *
     * @param enabledScope The scope to add
     */
    public void addEnabledScope(OAuthScope enabledScope) {
        if (null != enabledScope) {
            Set<OAuthScope> enabledScopes = new LinkedHashSet<>(this.enabledScopes);
            enabledScopes.add(enabledScope);
            this.enabledScopes = ImmutableSet.copyOf(enabledScopes);
            this.enabledScopesSet = true;
        }
    }

    /**
     * Sets the enabled scopes
     *
     * @param enabledScopes The enabled scopes to set
     */
    public void setEnabledScopes(Set<OAuthScope> enabledScopes) {
        this.enabledScopes = null == enabledScopes ? Collections.<OAuthScope> emptySet() : ImmutableSet.copyOf(enabledScopes);
        this.enabledScopesSet = true;
    }

    /**
     * Checks whether enabled scopes are set in this instance.
     *
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean isEnabledScopesSet() {
        return enabledScopesSet;
    }

    @Override
    public String getUserIdentity() {
        return userIdentity;
    }

    /**
     * Sets the userIdentity
     *
     * @param userIdentity The userIdentity to set
     */
    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }
}
