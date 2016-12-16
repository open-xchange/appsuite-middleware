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

package com.openexchange.mail.oauth;

/**
 * {@link DefaultTokenInfo} - The default token info implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class DefaultTokenInfo implements TokenInfo {

    /**
     * The XOAUTH2 authentication mechanism.
     */
    public static final String AUTH_MECH_XOAUTH2 = "XOAUTH2";

    /**
     * The OAUTHBEARER authentication mechanism.
     */
    public static final String AUTH_MECH_OAUTHBEARER = "OAUTHBEARER";

    /**
     * Creates a new XOAUTH2 token info for specified token string
     *
     * @param token The token string
     * @return The XOAUTH2 token info
     */
    public static DefaultTokenInfo newXOAUTH2TokenInfoFor(String token) {
        return null == token ? null : builder().setAuthMechanism(AUTH_MECH_XOAUTH2).setToken(token).build();
    }

    /**
     * Creates a new OAUTHBEARER token info for specified token string
     *
     * @param token The token string
     * @return The OAUTHBEARER token info
     */
    public static DefaultTokenInfo newOAUTHBEARERTokenInfoFor(String token) {
        return null == token ? null : builder().setAuthMechanism(AUTH_MECH_OAUTHBEARER).setToken(token).build();
    }

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an <code>DefaultTokenInfo</code> instance */
    public static class Builder {

        private String token;
        private String authMechanism;

        Builder() {
            super();
        }

        /**
         * Sets the token
         *
         * @param token The token to set
         * @return This builder
         */
        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the authMechanism
         *
         * @param authMechanism The authMechanism to set
         * @return This builder
         */
        public Builder setAuthMechanism(String authMechanism) {
            this.authMechanism = authMechanism;
            return this;
        }

        /**
         * Creates the <code>DefaultTokenInfo</code> instance from this builder's arguments.
         *
         * @return The <code>DefaultTokenInfo</code> instance
         */
        public DefaultTokenInfo build() {
            return new DefaultTokenInfo(token, authMechanism);
        }
    }

    // ----------------------------------------------------------------

    private final String token;
    private final String authMechanism;

    /**
     * Initializes a new {@link DefaultTokenInfo}.
     */
    DefaultTokenInfo(String token, String authMechanism) {
        super();
        this.token = token;
        this.authMechanism = authMechanism;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getAuthMechanism() {
        return authMechanism;
    }

}
