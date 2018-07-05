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
