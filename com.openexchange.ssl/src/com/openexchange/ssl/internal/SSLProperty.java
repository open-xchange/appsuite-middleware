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

package com.openexchange.ssl.internal;

/**
 * {@link SSLProperty}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public enum SSLProperty {

    /* The general switch if all other configuration should be considered. If set to false, all connections will be trusted */
    SECURE_CONNECTIONS_ENABLED(SSLProperty.SECURE_CONNECTIONS_KEY, true),

    /* Enables logging SSL details. Have a look at http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/ReadDebug.html for more details. */
    SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED(SSLProperty.SECURE_CONNECTIONS_DEBUG_LOGS_KEY, false),

    HOSTNAME_VERIFICATION_ENABLED(SSLProperty.HOSTNAME_VERIFICATION_ENABLED_KEY, true),

    DEFAULT_TRUSTSTORE_ENABLED(SSLProperty.DEFAULT_TRUSTSTORE_ENABLED_KEY, true),

    CUSTOM_TRUSTSTORE_ENABLED(SSLProperty.CUSTOM_TRUSTSTORE_ENABLED_KEY, false),

    CUSTOM_TRUSTSTORE_LOCATION(SSLProperty.CUSTOM_TRUSTSTORE_PATH_KEY, SSLProperty.CUSTOM_TRUSTSTORE_PATH_DEFAULT),

    CUSTOM_TRUSTSTORE_PASSWORD(SSLProperty.CUSTOM_TRUSTSTORE_PASSWORD_KEY, SSLProperty.CUSTOM_TRUSTSTORE_PASSWORD_DEFAULT),

    ;

    static final String SECURE_CONNECTIONS_KEY = "com.openexchange.ssl.only";

    static final String SECURE_CONNECTIONS_DEBUG_LOGS_KEY = "com.openexchange.ssl.debug.logs";

    static final String HOSTNAME_VERIFICATION_ENABLED_KEY = "com.openexchange.ssl.hostname.verification.enabled";

    static final String DEFAULT_TRUSTSTORE_ENABLED_KEY = "com.openexchange.ssl.default.truststore.enabled";

    static final String CUSTOM_TRUSTSTORE_ENABLED_KEY = "com.openexchange.ssl.custom.truststore.enabled";

    static final String CUSTOM_TRUSTSTORE_PATH_KEY = "com.openexchange.ssl.custom.truststore.path";

    static final String CUSTOM_TRUSTSTORE_PATH_DEFAULT = "";

    static final String CUSTOM_TRUSTSTORE_PASSWORD_KEY = "com.openexchange.ssl.custom.truststore.password";

    static final String CUSTOM_TRUSTSTORE_PASSWORD_DEFAULT = "";

    private final String propertyName;

    private String defaultValue;

    private boolean defaultBoolValue;

    private SSLProperty(final String propertyName, final String defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    private SSLProperty(final String propertyName, final boolean defaultBoolValue) {
        this.propertyName = propertyName;
        this.defaultBoolValue = defaultBoolValue;
    }

    public String getName() {
        return propertyName;
    }

    public String getDefault() {
        return defaultValue;
    }

    public boolean getDefaultBoolean() {
        return defaultBoolValue;
    }
}
