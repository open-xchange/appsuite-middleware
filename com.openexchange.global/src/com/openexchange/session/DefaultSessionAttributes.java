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
