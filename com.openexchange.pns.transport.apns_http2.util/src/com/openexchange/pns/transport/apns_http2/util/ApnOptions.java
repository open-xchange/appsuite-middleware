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

package com.openexchange.pns.transport.apns_http2.util;

import com.openexchange.java.Strings;

/**
 * {@link ApnOptions} - Holds the (immutable) options to communicate with the Apple Push Notification System.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ApnOptions {

    /** The authentication types supported by APNs */
    public static enum AuthType {

        /** Connect to APNs using provider certificates */
        CERTIFICATE("certificate"),
        /** Connect to APNs using provider authentication JSON Web Token (JWT) */
        JWT("jwt"),
        ;

        private final String id;

        private AuthType(String id) {
            this.id = id;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the authentication type for specified identifier.
         *
         * @param id The identifier
         * @return The authentication type or <code>null</code>
         */
        public static AuthType authTypeFor(String id) {
            if (Strings.isEmpty(id)) {
                return null;
            }

            for (AuthType authType : AuthType.values()) {
                if (authType.id.equalsIgnoreCase(id)) {
                    return authType;
                }
            }
            return null;
        }
    }

    private final AuthType authType;
    private final String password;
    private final Object keystore;
    private final boolean production;
    private final String topic;
    private final String clientId;
    private final Object privateKey;
    private final String keyId;
    private final String teamId;

    private ApnOptions(AuthType authType, Object keystore, String password, Object privateKey, String keyId, String teamId, String topic, boolean production, String clientId) {
        super();
        this.authType = authType;
        this.keystore = keystore;
        this.password = password;
        this.privateKey = privateKey;
        this.keyId = keyId;
        this.teamId = teamId;
        this.topic = topic;
        this.production = production;
        this.clientId = clientId;
    }

    /**
     * Initializes a new immutable {@link ApnOptions} instance.
     *
     * @param keystore A keystore containing the private key and the certificate signed by Apple.<br>
     *                 The following formats can be used:
     *                 <ul>
     *                 <li><code>java.io.File</code></li>
     *                 <li><code>java.io.InputStream</code></li>
     *                 <li><code>byte[]</code></li>
     *                 <li><code>java.security.KeyStore</code></li>
     *                 <li><code>java.lang.String</code> for a file path</li>
     *                 </ul>
     * @param password The keystore's password.
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     * @param topic The push topic, should be the app's bundle identifier
     * @param clientId The client identifier
     */
    public ApnOptions(Object keystore, String password, boolean production, String topic, String clientId) {
        this(AuthType.CERTIFICATE, keystore, password, null, null, null, topic, production, clientId);
    }

    /**
     * Initializes a new immutable {@link ApnOptions} instance.
     *
     * @param privateKey The APNS authentication key signed by Apple.<br>
     *                 The following formats can be used:
     *                 <ul>
     *                 <li><code>java.io.File</code></li>
     *                 <li><code>java.io.InputStream</code></li>
     *                 <li><code>byte[]</code></li>
     *                 <li><code>java.lang.String</code> for a file path</li>
     *                 </ul>
     * @param keyId The key identifier obtained from developer account
     * @param teamId The team identifier obtained from developer account
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     * @param topic The push topic, should be the app's bundle identifier
     * @param clientId The client identifier
     */
    public ApnOptions(Object privateKey, String keyId, String teamId, boolean production, String topic, String clientId) {
        this(AuthType.JWT, null, null, privateKey, keyId, teamId, topic, production, clientId);
    }

    /**
     * Gets the {@link AuthType}
     *
     * @return The auth type
     */
    public AuthType getAuthType() {
        return authType;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the keystore
     *
     * @return The keystore
     */
    public Object getKeystore() {
        return keystore;
    }

    /**
     * Gets the production
     *
     * @return The production
     */
    public boolean isProduction() {
        return production;
    }

    /**
     * Gets the topic
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the private key
     *
     * @return The private key
     */
    public Object getPrivateKey() {
        return privateKey;
    }

    /**
     * Gets the key identifier
     *
     * @return The key identifier
     */
    public String keyId() {
        return keyId;
    }

    /**
     * Gets the team identifier
     *
     * @return The team identifier
     */
    public String teamId() {
        return teamId;
    }

}
