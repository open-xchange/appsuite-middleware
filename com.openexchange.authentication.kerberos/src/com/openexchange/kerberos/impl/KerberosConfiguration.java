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

import static com.openexchange.kerberos.impl.ConfigurationProperty.DEBUG;
import static com.openexchange.kerberos.impl.ConfigurationProperty.JAAS_CONF;
import static com.openexchange.kerberos.impl.ConfigurationProperty.KRB5_CONF;
import static com.openexchange.kerberos.impl.ConfigurationProperty.MODULE_NAME;
import static com.openexchange.kerberos.impl.ConfigurationProperty.USER_MODULE_NAME;
import java.io.File;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;

/**
 * Configures the Kerberos component
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class KerberosConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KerberosConfiguration.class);

    private static String moduleName;
    private static String userModuleName;

    public static boolean configure(ConfigurationService config) {
        boolean configured = true;

        System.setProperty("sun.security.krb5.debug", config.getProperty(DEBUG.getName(), DEBUG.getDefault()));

        final String krb5ConfPath = config.getProperty(KRB5_CONF.getName(), KRB5_CONF.getDefault());
        final File krb5Conf = new File(krb5ConfPath);
        if (krb5Conf.exists() && krb5Conf.isFile() && krb5Conf.canRead()) {
            System.setProperty("java.security.krb5.conf", krb5ConfPath);
        } else {
            LOG.error("Cannot read krb5.conf configuration file stated to be here: \"{}\".", krb5ConfPath);
            configured = false;
        }

        final String jaasConfPath = config.getProperty(JAAS_CONF.getName(), JAAS_CONF.getDefault());
        final File jaasConf = new File(jaasConfPath);
        if (jaasConf.exists() && jaasConf.isFile() && jaasConf.canRead()) {
            System.setProperty("java.security.auth.login.config", jaasConfPath);
        } else {
            LOG.error("Cannot read JAAS configuration file state to be here: \"{}\".", jaasConfPath);
            configured = false;
        }

//        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        moduleName = config.getProperty(MODULE_NAME.getName(), MODULE_NAME.getDefault());
        userModuleName = config.getProperty(USER_MODULE_NAME.getName(), USER_MODULE_NAME.getDefault());
        configured = configured && readConfiguration(moduleName);
        return configured;
    }

    private static boolean readConfiguration(String serviceName) {
        Configuration config = Configuration.getConfiguration();
        AppConfigurationEntry[] entry = config.getAppConfigurationEntry(serviceName);
        return null != entry;
    }

    public static String getModuleName() {
        return moduleName;
    }

    public static String getUserModuleName() {
        return userModuleName;
    }

    private KerberosConfiguration() {
        super();
    }
}
