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
    enabled(false),

    /**
     * Indicates which APNS service is used when sending push notifications to devices.
     * A value of "true" will use the production service, a value of "false" the sandbox service.
     * Default: true
     */
    production(true),

    /**
     * Specifies the path to the local keystore file (PKCS #12) containing the APNS certificate and keys for the application, e.g. "/opt/open-xchange/etc/drive-apns.p12". 
     * Required if com.openexchange.drive.events.apn.[os].enabled is "true" and the package containing the restricted drive components is not installed.
     * Default: no default
     */
    keystore(null),

    /**
     * Note that blank or null passwords are in violation of the PKCS #12 specifications. Required if "com.openexchange.drive.events.apn.[os].enabled"
     * is "true" .
     * Default: no default
     */
    password(null),
    
    /**
     * Configures the interval between queries to the APN feedback service for the subscribed devices. 
     * The value can be defined using units of measurement: "D" (=days), "W" (=weeks) and "H" (=hours). 
     * Leaving this parameter empty disables the feedback queries on this node. Since each received feedback is processed cluster-wide, only one node in the cluster should be enabled here.
     */
    feedbackQueryInterval("1D")

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
