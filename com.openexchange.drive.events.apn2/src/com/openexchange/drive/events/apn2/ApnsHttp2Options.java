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

package com.openexchange.drive.events.apn2;

import java.io.File;
import com.openexchange.java.Strings;

/**
 * {@link ApnsHttp2Options} - Holds the (immutable) options to communicate with the Apple Push Notification System via HTTP/2.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ApnsHttp2Options {

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

    // --------------------------------------------------------------------------------------------------------

    private final AuthType authType;

    private final byte[] privateKey;
    private final String keyId;
    private final String teamId;

    private final String password;
    private final File keystore;

    private final boolean production;
    private final String topic;

    /**
     * Initializes a new immutable {@link ApnsHttp2Options} instance using a provider certificate.
     *
     * @param keystore A keystore containing the private key and the certificate signed by Apple
     * @param password The keystore's password.
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     * @param topic The app's topic, which is typically the bundle ID of the app
     */
    public ApnsHttp2Options(File keystore, String password, boolean production, String topic) {
        super();
        authType = AuthType.CERTIFICATE;
        this.keystore = keystore;
        this.password = password;
        this.production = production;
        this.topic = topic;
        this.privateKey = null;
        this.keyId = null;
        this.teamId = null;
    }

    /**
     * Initializes a new immutable {@link ApnsHttp2Options} instance using a provider JSON Web Token (JWT).
     *
     * @param privateKey The APNS authentication key
     * @param keyId The key identifier obtained from developer account
     * @param teamId The team identifier obtained from developer account
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     * @param topic The app's topic, which is typically the bundle ID of the app
     */
    public ApnsHttp2Options(byte[] privateKey, String keyId, String teamId, boolean production, String topic) {
        super();
        authType = AuthType.JWT;
        this.keystore = null;
        this.password = null;
        this.production = production;
        this.topic = topic;
        this.privateKey = privateKey;
        this.keyId = keyId;
        this.teamId = teamId;
    }

    /**
     * Gets the authentication type
     *
     * @return The authentication type
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
    public File getKeystore() {
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
     * Gets the key identifier obtained from developer account
     *
     * @return The key identifier obtained from developer account
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the team identifier obtained from developer account
     *
     * @return The team identifier obtained from developer account
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     * Gets the APNS auth key file
     *
     * @return The APNS auth key file
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * Gets the apps's topic, which is typically the bundle ID of the app.
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

}
