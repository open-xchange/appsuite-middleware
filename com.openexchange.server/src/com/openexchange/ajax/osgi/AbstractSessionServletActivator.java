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

package com.openexchange.ajax.osgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig.Property;

/**
 * {@link AbstractSessionServletActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractSessionServletActivator extends AbstractServletActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractSessionServletActivator.class);

    /**
     * Initializes a new {@link AbstractSessionServletActivator}.
     */
    protected AbstractSessionServletActivator() {
        super();
    }

    /**
     * Registers given Servlet instance.
     *
     * @param alias The Servlet's alias
     * @param servlet The Servlet instance
     * @param configKeys The Servlet's initial parameters
     */
    protected void registerSessionServlet(final String alias, final HttpServlet servlet, final String... configKeys) {
        try {
            // Determine keys to read from config service
            List<String> allKeys = null == configKeys || configKeys.length == 0 ? new ArrayList<String>(6) : new ArrayList<String>(Arrays.asList(configKeys));
            allKeys.add(Property.COOKIE_HASH.getPropertyName());
            // allKeys.add(Property.IP_CHECK.getPropertyName());           --> IP check mechanism may also be specified through "com.openexchange.ipcheck.mode"
            // allKeys.add(Property.IP_CHECK_WHITELIST.getPropertyName()); --> Now initialized in IPCheckServiceImpl.newConfigurationFor(Session)
            // allKeys.add(Property.IP_MASK_V4.getPropertyName());         --> Now initialized in IPCheckServiceImpl.newConfigurationFor(Session)
            // allKeys.add(Property.IP_MASK_V6.getPropertyName());         --> Now initialized in IPCheckServiceImpl.newConfigurationFor(Session)

            // Fill Servlet's init parameters with keys' values
            Dictionary<String, String> initParams = createInitParameters(allKeys, alias, servlet);

            // Register Servlet instance using HttpService
            registerServlet(alias, servlet, initParams, getService(HttpService.class));
        } catch (IllegalStateException e) {
            LOG.error("", e);
        }
    }

    private Dictionary<String, String> createInitParameters(List<String> keys, String alias, HttpServlet servlet) {
        ConfigurationService configurationService = getService(ConfigurationService.class);
        if (configurationService == null) {
            return new Hashtable<String, String>(0);
        }

        Dictionary<String, String> initParams = new Hashtable<String, String>(keys.size());
        for (String configKey : keys) {
            String property = configurationService.getProperty(configKey);
            if (property == null) {
                LOG.warn("Missing initialization parameter \"{}\". Problem during registration of servlet \"{}\" using the URI namespace \"{}\".", configKey, servlet.getClass().getName(), alias, new IllegalStateException("Missing initialization parameter"));
                property = "";
            }
            initParams.put(configKey, property);
        }

        String whitelistFile = configurationService.getText(SessionServlet.SESSION_WHITELIST_FILE);
        if (whitelistFile != null) {
            initParams.put(SessionServlet.SESSION_WHITELIST_FILE, whitelistFile);
        }

        return initParams;
    }

    /**
     * Registers given Servlet instance using <code>HttpService</code>.
     *
     * @param alias The alias
     * @param servlet The Servlet instance
     */
    protected void registerServlet(final String alias, final HttpServlet servlet) {
        registerServlet(alias, servlet, getService(HttpService.class));
    }

    /**
     * final, because these services are always needed to register Session Servlets. Additional needed Services are provided by
     * <code>getAdditionalNeededServices()</code>
     */
    @Override
    protected final Class<?>[] getNeededServices() {
        Set<Class<?>> neededServices = new LinkedHashSet<Class<?>>(6);
        neededServices.add(HttpService.class);
        neededServices.add(ConfigurationService.class);
        neededServices.add(CapabilityService.class);

        // Add additional needed services (if any)
        Class<?>[] additionalNeededServices = getAdditionalNeededServices();
        if (additionalNeededServices != null) {
            for (Class<?> clazz : additionalNeededServices) {
                neededServices.add(clazz);
            }
        }

        return neededServices.toArray(new Class<?>[neededServices.size()]);
    }

    /**
     * Gets additionally needed services beside <tt>HttpService</tt>, <tt>ConfigurationService</tt> and <tt>CapabilityService</tt>.
     *
     * @return The additionally needed services
     */
    protected abstract Class<?>[] getAdditionalNeededServices();

}
