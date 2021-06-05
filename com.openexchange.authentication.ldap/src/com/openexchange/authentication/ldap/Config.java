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

package com.openexchange.authentication.ldap;

import javax.naming.directory.SearchControls;

/**
 * {@link Config} - The configuration for the LDAP authentication bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class Config {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String uidAttribute;
        private String baseDN;
        private String ldapReturnField;
        private String searchFilter;
        private String bindDN;
        private String bindDNPassword;
        private String proxyUser;
        private String proxyDelimiter;
        private String referral;
        private String ldapScope;
        private boolean bindOnly;
        private boolean useFullLoginInfo;
        private boolean adsbind;
        private int searchScope = SearchControls.SUBTREE_SCOPE;

        Builder() {
            super();
        }

        public Builder withUidAttribute(String uidAttribute) {
            this.uidAttribute = uidAttribute;
            return this;
        }

        public Builder withBaseDN(String baseDN) {
            this.baseDN = baseDN;
            return this;
        }

        public Builder withLdapReturnField(String ldapReturnField) {
            this.ldapReturnField = ldapReturnField;
            return this;
        }

        public Builder withSearchFilter(String searchFilter) {
            this.searchFilter = searchFilter;
            return this;
        }

        public Builder withBindDN(String bindDN) {
            this.bindDN = bindDN;
            return this;
        }

        public Builder withBindDNPassword(String bindDNPassword) {
            this.bindDNPassword = bindDNPassword;
            return this;
        }

        public Builder withProxyUser(String proxyUser) {
            this.proxyUser = proxyUser;
            return this;
        }

        public Builder withProxyDelimiter(String proxyDelimiter) {
            this.proxyDelimiter = proxyDelimiter;
            return this;
        }

        public Builder withReferral(String referral) {
            this.referral = referral;
            return this;
        }

        public Builder withLdapScope(String ldapScope) {
            this.ldapScope = ldapScope;
            return this;
        }

        public Builder withBindOnly(boolean bindOnly) {
            this.bindOnly = bindOnly;
            return this;
        }

        public Builder withUseFullLoginInfo(boolean useFullLoginInfo) {
            this.useFullLoginInfo = useFullLoginInfo;
            return this;
        }

        public Builder withAdsbind(boolean adsbind) {
            this.adsbind = adsbind;
            return this;
        }

        public Builder withSearchScope(int searchScope) {
            this.searchScope = searchScope;
            return this;
        }

        public Config build() {
            return new Config(uidAttribute, baseDN, ldapReturnField, searchFilter, bindDN, bindDNPassword, proxyUser, proxyDelimiter, referral, ldapScope, bindOnly, useFullLoginInfo, adsbind, searchScope);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    public final String uidAttribute;
    public final String baseDN;
    public final String ldapReturnField;
    public final String searchFilter;
    public final String bindDN;
    public final String bindDNPassword;
    public final String proxyUser;
    public final String proxyDelimiter;
    public final String referral;
    public final String ldapScope;
    public final boolean bindOnly;
    public final boolean useFullLoginInfo;
    public final boolean adsbind;
    public final int searchScope;

    /**
     * Initializes a new {@link Config}.
     */
    Config(String uidAttribute, String baseDN, String ldapReturnField, String searchFilter, String bindDN, String bindDNPassword, String proxyUser, String proxyDelimiter, String referral, String ldapScope, boolean bindOnly, boolean useFullLoginInfo, boolean adsbind, int searchScope) {
        super();
        this.uidAttribute = uidAttribute;
        this.baseDN = baseDN;
        this.ldapReturnField = ldapReturnField;
        this.searchFilter = searchFilter;
        this.bindDN = bindDN;
        this.bindDNPassword = bindDNPassword;
        this.proxyUser = proxyUser;
        this.proxyDelimiter = proxyDelimiter;
        this.referral = referral;
        this.ldapScope = ldapScope;
        this.bindOnly = bindOnly;
        this.useFullLoginInfo = useFullLoginInfo;
        this.adsbind = adsbind;
        this.searchScope = searchScope;
    }

}
