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

    /**
     * Gets the Kerberos configuration.
     *
     * @param config The configuration service to use
     * @return The Kerberos configuration
     */
    public static KerberosConfiguration configure(ConfigurationService config) {
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

        String moduleName = config.getProperty(MODULE_NAME.getName(), MODULE_NAME.getDefault());
        String userModuleName = config.getProperty(USER_MODULE_NAME.getName(), USER_MODULE_NAME.getDefault());
        configured = configured && readConfiguration(moduleName);
        return new KerberosConfiguration(configured, moduleName, userModuleName);
    }

    private static boolean readConfiguration(String serviceName) {
        Configuration config = Configuration.getConfiguration();
        AppConfigurationEntry[] entry = config.getAppConfigurationEntry(serviceName);
        return null != entry;
    }

    // -------------------------------------------------------------------------------------------------------------

    private final boolean configured;
    private final String moduleName;
    private final String userModuleName;

    private KerberosConfiguration(boolean configured, String moduleName, String userModuleName) {
        super();
        this.configured = configured;
        this.moduleName = moduleName;
        this.userModuleName = userModuleName;
    }

    /**
     * Checks if Kerberos is configured
     *
     * @return <code>true</code> if configured; otherwise <code>false</code>
     */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Gets the module name
     *
     * @return The module name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Gets the module name of the user
     *
     * @return The user's module name
     */
    public String getUserModuleName() {
        return userModuleName;
    }

}
