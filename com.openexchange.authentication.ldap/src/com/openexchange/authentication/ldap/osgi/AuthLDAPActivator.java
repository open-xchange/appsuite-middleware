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

package com.openexchange.authentication.ldap.osgi;

import java.util.Properties;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.ldap.LDAPAuthentication;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AuthLDAPActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AuthLDAPActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AuthLDAPActivator.class);

    /**
     * Initializes a new {@link AuthLDAPActivator}.
     */
    public AuthLDAPActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, SSLSocketFactoryProvider.class };
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception if the authentication class can not be initialized.
     */
    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting ldap authentication service.");

        final ConfigurationService config = getService(ConfigurationService.class);
        final Properties props = config.getFile("ldapauth.properties");

        LDAPAuthentication impl = new LDAPAuthentication(props, this);
        registerService(AuthenticationService.class, impl, null);
        registerService(Reloadable.class, impl, null);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping ldap authentication service.");
        super.stopBundle();
    }
}
