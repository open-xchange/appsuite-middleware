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

package com.openexchange.session;

/**
 * {@link DefaultSessionAttributes} - The default implementation of {@link SessionAttributes}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DefaultSessionAttributes implements SessionAttributes {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>DefaultSessionAttributes</code> */
    public static class Builder {

        private SessionAttribute<String> localIp;
        private SessionAttribute<String> client;
        private SessionAttribute<String> hash;
        private SessionAttribute<String> userAgent;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the local IP address.
         *
         * @param localIp The local IP address to set
         * @return This builder
         */
        public Builder withLocalIp(String localIp) {
            this.localIp = SessionAttribute.valueOf(localIp);
            return this;
        }

        /**
         * Sets the client identifier.
         *
         * @param client The client identifier to set
         * @return This builder
         */
        public Builder withClient(String client) {
            this.client = SessionAttribute.valueOf(client);
            return this;
        }

        /**
         * Sets the hash identifier.
         *
         * @param hash The hash identifier to set
         * @return This builder
         */
        public Builder withHash(String hash) {
            this.hash = SessionAttribute.valueOf(hash);
            return this;
        }

        /**
         * Sets the User-Agent identifier.
         *
         * @param userAgent The User-Agent identifier to set
         * @return This builder
         */
        public Builder withUserAgent(String userAgent) {
            this.userAgent = SessionAttribute.valueOf(userAgent);
            return this;
        }

        /**
         * Creates the appropriate instance of <code>DefaultSessionAttributes</code> from this builder's attributes.
         *
         * @return The instance of <code>DefaultSessionAttributes</code>
         */
        public DefaultSessionAttributes build() {
            return new DefaultSessionAttributes(localIp, client, hash, userAgent);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final SessionAttribute<String> localIp;
    private final SessionAttribute<String> client;
    private final SessionAttribute<String> hash;
    private final SessionAttribute<String> userAgent;

    /**
     * Initializes a new {@link DefaultSessionAttributes}.
     */
    DefaultSessionAttributes(SessionAttribute<String> localIp, SessionAttribute<String> client, SessionAttribute<String> hash, SessionAttribute<String> userAgent) {
        super();
        this.localIp = localIp == null ? SessionAttribute.unset() : localIp;
        this.client = client == null ? SessionAttribute.unset() : client;
        this.hash = hash == null ? SessionAttribute.unset() : hash;
        this.userAgent = userAgent == null ? SessionAttribute.unset() : userAgent;
    }

    @Override
    public SessionAttribute<String> getLocalIp() {
        return localIp;
    }

    @Override
    public SessionAttribute<String> getClient() {
        return client;
    }

    @Override
    public SessionAttribute<String> getHash() {
        return hash;
    }

    @Override
    public SessionAttribute<String> getUserAgent() {
        return userAgent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("localIp=").append(localIp.get()).append(", ");
        sb.append("client=").append(client.get()).append(", ");
        sb.append("hash=").append(hash.get()).append(", ");
        sb.append("userAgent=").append(userAgent.get());
        sb.append("]");
        return sb.toString();
    }

}
