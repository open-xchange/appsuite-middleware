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
