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

package com.openexchange.hostname.ldap.osgi;

import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.hostname.ldap.LDAPHostnameCache;
import com.openexchange.hostname.ldap.LDAPHostnameService;
import com.openexchange.hostname.ldap.configuration.LDAPHostnameProperties;
import com.openexchange.hostname.ldap.configuration.Property;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * The activator for <i>"com.openexchange.hostname.ldap"</i> bundle
 */
public class Activator extends HousekeepingActivator {

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CacheService.class, ConfigurationService.class, SSLSocketFactoryProvider.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            ConfigurationService configService = getService(ConfigurationService.class);
            checkConfiguration(configService);
            activateCaching(configService, getService(CacheService.class));
            LDAPHostnameCache.getInstance().outputSettings();

            // Register hostname service to modify hostnames in direct links, this will also initialize the cache class
            registerService(HostnameService.class, new LDAPHostnameService(this), null);
        } catch (Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        // Stop hostname service
        try {
            deactivateCaching();
            super.stopBundle();
            Services.setServiceLookup(null);
        } catch (Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

    private void activateCaching(ConfigurationService configService, CacheService cacheService) throws OXException {
        final String cacheConfigFile = LDAPHostnameProperties.getProperty(configService, Property.cache_config_file);
        cacheService.loadConfiguration(cacheConfigFile.trim());
    }

    private void checkConfiguration(ConfigurationService configService) throws OXException {
        LDAPHostnameProperties.check(configService, Property.values(), LDAPHostnameCache.REGION_NAME);
    }

    private void deactivateCaching() {
        CacheService cacheService = getService(CacheService.class);
        if (null != cacheService) {
            try {
                cacheService.freeCache(LDAPHostnameCache.REGION_NAME);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }

    }

}
