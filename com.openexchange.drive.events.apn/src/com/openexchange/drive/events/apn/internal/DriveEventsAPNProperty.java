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

package com.openexchange.drive.events.apn.internal;

import com.openexchange.config.lean.Property;


/**
 *
 * {@link DriveEventsAPNProperty}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public enum DriveEventsAPNProperty implements Property {

    /**
     * Enables or disables push event notifications to clients using the Apple Push Notification service (APNS).
     * This requires a valid configuration for the APNS certificate and keys, or install the restricted components packages for drive.
     * Default: false
     */
    enabled(Boolean.FALSE),

    /**
     * Specifies the authentication type to use for the APNS HTTP/2 push.
     * Allows the values "certificate" and "jwt".
     * - "certificates" signals to connect to APNs using provider certificates while
     * - "jwt" signals to connect to APNs using provider authentication JSON Web Token (JWT)
     * Default: "certificate"
     */
    authtype("certificate"),

    /**
     * Specifies the path to the local keystore file (PKCS #12) containing the APNS certificate and keys for the application, e.g. "/opt/open-xchange/etc/drive-apns.p12".
     * Required if com.openexchange.drive.events.apn.[os].enabled is "true" and "com.openexchange.drive.events.apn.[os].authtype" is "certificate" and the package containing 
     * the restricted drive components is not installed.
     * Default: no default
     */
    keystore(null),

    /**
     * Note that blank or null passwords are in violation of the PKCS #12 specifications. Required if "com.openexchange.drive.events.apn.[os].enabled"
     * is "true" and "com.openexchange.drive.events.apn.[os].authtype" is "certificate"
     * Default: no default
     */
    password(null),

    /**
     * Specifies the private key file used to connect to APNs using provider authentication JSON Web Token (JWT).
     * Required if "com.openexchange.drive.events.apn.[os].enabled" is "true" and "com.openexchange.drive.events.apn.[os].authtype"
     * is "jwt".
     * Default: no default
     */
    privatekey(null),

    /**
     * Specifies the key identifier used to connect to APNs using provider authentication JSON Web Token (JWT).
     * Required if "com.openexchange.drive.events.apn.[os].enabled" is "true" and "com.openexchange.drive.events.apn.[os].authtype"
     * is "jwt".
     * Default: no default
     */
    keyid(null),

    /**
     * Specifies the team identifier used to connect to APNs using provider authentication JSON Web Token (JWT).
     * Required if "com.openexchange.drive.events.apn.[os].enabled" is "true" and "com.openexchange.drive.events.apn.[os].authtype"
     * is "jwt".
     * Default: no default
     */
    teamid(null),

    /**
     * Specifies the topic to use for OX Drive push notifications. Topic is the app's bundleId
     * Default: no default
     */
    topic(null),

    /**
     * Indicates which APNS service is used when sending push notifications to devices.
     * A value of "true" will use the production service, a value of "false" the sandbox service.
     * Default: true
     */
    production(Boolean.TRUE),

    ;

    public static final String FRAGMENT_FILE_NAME = "drive.properties";
    public static final String OPTIONAL_FIELD = "os";
    private static final String PREFIX = "com.openexchange.drive.events.apn.[os].";

    private final Object defaultValue;

    private DriveEventsAPNProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
