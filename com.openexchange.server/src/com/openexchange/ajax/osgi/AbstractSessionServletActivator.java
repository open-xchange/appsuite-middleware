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

package com.openexchange.ajax.osgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
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
     * Registers given Servlet.
     *
     * @param alias The Servlet's alias
     * @param servlet The Servlet instance
     * @param configKeys The Servlet's initial parameters
     */
    protected void registerSessionServlet(final String alias, final HttpServlet servlet, final String... configKeys) {
        final List<String> allKeys = new ArrayList<String>(Arrays.asList(configKeys));
        allKeys.add(Property.IP_CHECK.getPropertyName());
        allKeys.add(Property.COOKIE_HASH.getPropertyName());
        allKeys.add(Property.IP_CHECK_WHITELIST.getPropertyName());
        allKeys.add(Property.IP_MASK_V4.getPropertyName());
        allKeys.add(Property.IP_MASK_V6.getPropertyName());

        try {
            final Dictionary<String, String> initParams = new Hashtable<String, String>();
            final ConfigurationService configurationService = getService(ConfigurationService.class);
            if (configurationService != null) {
                for (final String configKey : allKeys) {
                    String property = configurationService.getProperty(configKey);
                    if (property == null) {
                        final StringBuilder sb = new StringBuilder("Missing initialization parameter \"");
                        sb.append(configKey);
                        sb.append("\". Problem during registration of servlet \"");
                        sb.append(servlet.getClass().getName());
                        sb.append("\" into the URI namespace \"");
                        sb.append(alias);
                        sb.append("\".");
                        final IllegalStateException e = new IllegalStateException(sb.toString());
                        LOG.warn("", e);
                        property = "";
                    }
                    initParams.put(configKey, property);
                }

                final String whitelistFile = configurationService.getText(SessionServlet.SESSION_WHITELIST_FILE);
                if (whitelistFile != null) {
                    initParams.put(SessionServlet.SESSION_WHITELIST_FILE, whitelistFile);
                }
            }

            registerServlet(alias, servlet, initParams, getService(HttpService.class));
        } catch (final IllegalStateException e) {
            LOG.error("", e);
        }
    }

    protected void registerServlet(final String alias, final HttpServlet servlet) {
        registerServlet(alias, servlet, getService(HttpService.class));
    }

    /**
     * final, because these services are always needed to register Session Servlets. Additional needed Services are provided by
     * <code>getAdditionalNeededServices()</code>
     */
    @Override
    protected final Class<?>[] getNeededServices() {
        final Set<Class<?>> neededServices = new HashSet<Class<?>>();
        neededServices.add(HttpService.class);
        neededServices.add(ConfigurationService.class);
        neededServices.add(CapabilityService.class);
        /*
         * Additional needed services
         */
        final Class<?>[] additionalNeededServices = getAdditionalNeededServices();
        if (additionalNeededServices != null) {
            for (final Class<?> clazz : additionalNeededServices) {
                neededServices.add(clazz);
            }
        }
        /*
         * Return
         */
        return neededServices.toArray(new Class<?>[neededServices.size()]);
    }

    /**
     * Gets additionally needed services beside <tt>HttpService</tt> & <tt>ConfigurationService</tt>.
     *
     * @return The additionally needed services
     */
    protected abstract Class<?>[] getAdditionalNeededServices();

}
