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

package com.openexchange.kerberos.impl;

/**
 * Enumerates the possible configuration properties for the Kerberos authentication module.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ConfigurationProperty {

    /**
     * Path to the JAAS configuration file.
     */
    JAAS_CONF("java.security.auth.login.config", "/opt/open-xchange/etc/kerberosLogin.conf"),
    /**
     * Wether Suns Kerberos implementation should write debugging information or not.
     */
    DEBUG("sun.security.krb5.debug", "false"),
    /**
     * Path to the krb5.conf configuration file.
     */
    KRB5_CONF("java.security.krb5.conf", "/opt/open-xchange/etc/krb5.conf"),
    /**
     * Name of the module in the Java authentication and authorization configuration file used when a Kerberos forwarding ticket is sent by
     * the browser.
     */
    MODULE_NAME("com.openexchange.kerberos.moduleName", "Open-Xchange"),
    /**
     * Name of the module in the Java authentication and authorization configuration file used for username and password authentication on
     * the normal frontend login screen.
     */
    USER_MODULE_NAME("com.openexchange.kerberos.userModuleName", "Open-Xchange-User-Auth");

    private final String propertyName;

    private final String defaultValue;

    private ConfigurationProperty(final String propertyName, final String defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return propertyName;
    }

    public String getDefault() {
        return defaultValue;
    }
}
