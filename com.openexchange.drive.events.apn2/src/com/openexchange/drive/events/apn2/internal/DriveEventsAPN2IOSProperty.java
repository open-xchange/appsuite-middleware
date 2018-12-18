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

package com.openexchange.drive.events.apn2.internal;

import com.openexchange.config.lean.Property;


/**
 * {@link DriveEventsAPN2IOSProperty}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.2
 */
public enum DriveEventsAPN2IOSProperty implements Property {

    /**
     * Enables or disables push event notifications to clients using the HTTP/2 based version
     * of the Apple Push Notification service (APNS HTTP/2) for iOS devices. This requires a
     * valid configuration for the APNS certificate and keys.
     * Default: false
     */
    enabled(false),

    /**
     * Configures the apps's topic, which is typically the bundle ID of the app.
     * Default: no default
     */
    topic(null),

    /**
     * Indicates which APNS service is used when sending push notifications to iOS devices.
     * A value of "true" will use the production service, a value of "false" the sandbox service.
     * Default: true
     */
    production(true),

    /**
     * Specifies the authentication type to use for the APNS HTTP/2 push.
     * Allows the values "certificate" and "jwt".
     * - "certificates" signals to connect to APNs using provider certificates while
     * - "jwt" signals to connect to APNs using provider authentication JSON Web Token (JWT)
     * Default: "certificate"
     */
    authtype("certificate"),

    /**
     * Specifies the path to the local keystore file (PKCS #12) containing the APNS HTTP/2 certificate and keys for the iOS application.
     * Required if "com.openexchange.drive.events.apn2.enabled" is "true" and "com.openexchange.drive.events.apn2.ios.authtype"
     * is "certificate".
     * Default: no default
     */
    keystore(null),

    /**
     * Specifies the password used when creating the referenced keystore containing the certificate of the iOS application.
     * Note that blank or null passwords are in violation of the PKCS #12 specifications. Required if "com.openexchange.drive.events.apn2.enabled"
     * is "true" and "com.openexchange.drive.events.apn2.ios.authtype" is "certificate".
     * Default: no default
     */
    password(null),

    /**
     * Specifies the private key file used to connect to APNs using provider authentication JSON Web Token (JWT).
     * Required if "com.openexchange.drive.events.apn2.enabled" is "true" and "com.openexchange.drive.events.apn2.ios.authtype"
     * is "jwt".
     * Default: no default
     */
    privatekey(null),

    /**
     * Specifies the key identifier used to connect to APNs using provider authentication JSON Web Token (JWT).
     * Required if "com.openexchange.drive.events.apn2.enabled" is "true" and "com.openexchange.drive.events.apn2.ios.authtype"
     * is "jwt".
     * Default: no default
     */
    keyid(null),

    /**
     * Specifies the team identifier used to connect to APNs using provider authentication JSON Web Token (JWT).
     * Required if "com.openexchange.drive.events.apn2.enabled" is "true" and "com.openexchange.drive.events.apn2.ios.authtype"
     * is "jwt".
     * Default: no default
     */
    teamid(null)
    ;

    private static final String PREFIX = "com.openexchange.drive.events.apn2.ios.";

    private final Object defaultValue;

    private DriveEventsAPN2IOSProperty(Object defaultValue) {
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
