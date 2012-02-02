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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.tools.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.DeferredRegistryRegistration;


/**
 * {@link ServletRegistration}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ServletRegistration extends DeferredRegistryRegistration<HttpService, HttpServlet> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ServletRegistration.class));

    private static final Class<?>[] NO_SERVICES = new Class<?>[0];
    private static final Class<?>[] CONFIGURATION_SERVICE = new Class<?>[]{ConfigurationService.class};

    private final String alias;

    private final List<String> configKeys;

    private boolean registered;

    public ServletRegistration(final BundleContext context, final HttpServlet item, final String alias, final String...configKeys) {
        this(context, item, alias, ((configKeys == null || configKeys.length == 0) ? new ArrayList<String>() : Arrays.asList(configKeys)));
    }

    public ServletRegistration(final BundleContext context, final HttpServlet item, final String alias, final List<String> configKeys) {
        super(context, HttpService.class, item, ((configKeys == null || configKeys.isEmpty()) ? NO_SERVICES : CONFIGURATION_SERVICE));
        this.alias = alias;
        this.configKeys = configKeys;
        if(alias == null) {
            throw new IllegalArgumentException("The alias must not be null");
        }
        open();
    }

    @Override
    public void register(final HttpService registry, final HttpServlet item) {
        try {
            final Dictionary<String, String> initParams = new Hashtable<String, String>();
            final ConfigurationService configurationService = getService(ConfigurationService.class);
            if (configurationService != null) {
                for(final String configKey : configKeys) {
                    final String property = configurationService.getProperty(configKey);
                    if (null == property) {
                        /*
                         * Missing initialization parameter
                         */
                        throw new IllegalStateException(new StringBuilder("Missing initialization parameter \"").append(configKey).append(
                            "\". Aborting registration of servlet \"").append(item.getClass().getName()).append(
                            "\" into the URI namespace \"").append(alias).append("\".").toString());
                    }
                    initParams.put(configKey, property);
                }
            }
            customizeInitParams(initParams);
            registry.registerServlet(alias, item, initParams, null);
            registered = true;
        } catch (final ServletException e) {
            LOG.error(e.getMessage(), e);
        } catch (final NamespaceException e) {
            LOG.error(e.getMessage(), e);
        } catch (final IllegalStateException e) {
            LOG.error(e.getMessage(), e);
        }
    }


    protected void customizeInitParams(final Dictionary<String, String> initParams) {
        // Can be overridden
    }

    @Override
    public void unregister(HttpService registry, HttpServlet item) {
        if (registered && registry != null) {
            registry.unregister(alias);
        }
        registered = false;
    }


}
