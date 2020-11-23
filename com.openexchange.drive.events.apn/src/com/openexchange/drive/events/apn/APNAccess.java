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

package com.openexchange.drive.events.apn;

import java.util.Arrays;
import com.openexchange.java.Strings;

/**
 * {@link APNAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class APNAccess {

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
    private final String stringKeystore;
    private final byte[] bytesKeystore;

    private final byte[] privateKeyBytes;
    private final String privateKeyString;
    private final String keyId;
    private final String teamId;

    private final boolean production;
    private final String topic;

    private int hash = 0;

    private APNAccess(AuthType type, String keystore, byte[] keystoreBytes, String password, String privateKey, byte[] privateKeyBytes, String keyId, String teamId, String topic, boolean production) {
        super();
        this.authType = type;
        this.stringKeystore = keystore;
        this.bytesKeystore = keystoreBytes;
        this.password = password;
        this.privateKeyString = privateKey;
        this.privateKeyBytes = privateKeyBytes;
        this.keyId = keyId;
        this.teamId = teamId;
        this.topic = topic;
        this.production = production;
    }

    /**
     * Initializes a new {@link APNAccess}.
     *
     * @param keystore The path to the keystore file containing the private key and the certificate signed by Apple
     * @param password The keystore's password.
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     */
    public APNAccess(String keystore, String password, boolean production, String topic) {
        this(AuthType.CERTIFICATE, keystore, null, password, null, null, null, null, topic, production);
    }

    /**
     * Initializes a new {@link APNAccess}.
     *
     * @param keystore The binaries of the keystore containing the private key and the certificate signed by Apple
     * @param password The keystore's password.
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     */
    public APNAccess(byte[] keystore, String password, boolean production, String topic) {
        this(AuthType.CERTIFICATE, null, keystore, password, null, null, null, null, topic, production);
    }

    /**
     * Initializes a new {@link APNAccess}.
     *
     * @param privateKey The APNS authentication key
     * @param keyId The key identifier obtained from developer account
     * @param teamId The team identifier obtained from developer account
     * @param topic The app's topic, which is typically the bundle ID of the app
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     */
    public APNAccess(byte[] privateKey, String keyId, String teamId, String topic, boolean production) {
        this(AuthType.JWT, null, null, null, null, privateKey, keyId, teamId, topic, production);
    }

    /**
     * Initializes a new {@link APNAccess}.
     *
     * @param privateKey A file containing the APNS authentication key
     * @param keyId The key identifier obtained from developer account
     * @param teamId The team identifier obtained from developer account
     * @param topic The app's topic, which is typically the bundle ID of the app
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     */
    public APNAccess(String privateKey, String keyId, String teamId, String topic, boolean production) {
        this(AuthType.JWT, null, null, null, privateKey, null, keyId, teamId, topic, production);
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
        return null == stringKeystore ? bytesKeystore : stringKeystore;
    }

    /**
     * Gets the private key
     *
     * @return The private key
     */
    public Object getPrivateKey() {
        return null == privateKeyString ? privateKeyBytes : privateKeyString;
    }

    /**
     * Gets the key identifier
     *
     * @return The key identifier
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the team identifier
     *
     * @return The team identifier
     */
    public String getTeamId() {
        return teamId;
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

    @Override
    public int hashCode() {
        int result = hash; // Does not need to be thread-safe
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + (AuthType.CERTIFICATE.equals(authType) ? 1321 : 1327);
            result = prime * result + (production ? 1231 : 1237);
            result = prime * result + ((topic == null) ? 0 : topic.hashCode());
            result = prime * result + ((password == null) ? 0 : password.hashCode());
            result = prime * result + ((stringKeystore == null) ? 0 : stringKeystore.hashCode());
            result = prime * result + ((privateKeyString == null) ? 0 : privateKeyString.hashCode());
            result = prime * result + Arrays.hashCode(bytesKeystore);
            result = prime * result + Arrays.hashCode(privateKeyBytes);
            result = prime * result + ((keyId == null) ? 0 : keyId.hashCode());
            result = prime * result + ((teamId == null) ? 0 : teamId.hashCode());
            this.hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof APNAccess)) {
            return false;
        }
        APNAccess other = (APNAccess) obj;
        if (false == authType.equals(other.authType)) {
            return false;
        }
        if (production != other.production) {
            return false;
        }
        if (null == topic) {
            if (null != topic) {
                return false;
            }
        } else if (!topic.equals(other.topic)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (stringKeystore == null) {
            if (other.stringKeystore != null) {
                return false;
            }
        } else if (!stringKeystore.equals(other.stringKeystore)) {
            return false;
        }
        if (privateKeyString == null) {
            if (other.privateKeyString != null) {
                return false;
            }
        } else if (!privateKeyString.equals(other.privateKeyString)) {
            return false;
        }
        if (!Arrays.equals(bytesKeystore, other.bytesKeystore)) {
            return false;
        }
        if (!Arrays.equals(privateKeyBytes, other.privateKeyBytes)) {
            return false;
        }
        if (null == keyId) {
            if (null != other.keyId) {
                return false;
            }
        } else if (!keyId.equals(other.keyId)) {
            return false;
        }
        if (null == teamId) {
            if (null != other.teamId) {
                return false;
            }
        } else if (!teamId.equals(other.teamId)) {
            return false;
        }
        return true;
    }

}
