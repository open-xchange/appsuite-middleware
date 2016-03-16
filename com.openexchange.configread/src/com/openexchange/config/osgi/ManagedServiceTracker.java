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

package com.openexchange.config.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;


/**
 * {@link ManagedServiceTracker} - Tracks {@link ManagedService} instances and applies certain configuration to them.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public final class ManagedServiceTracker extends ServiceTracker<ManagedService, ManagedService> {

    private final ConfigurationService configService;

    /**
     * Initializes a new {@link ManagedServiceTracker}.
     */
    public ManagedServiceTracker(final BundleContext context, ConfigurationService configService) {
        super(context, ManagedService.class, null);
        this.configService = configService;
    }

    @Override
    public ManagedService addingService(final ServiceReference<ManagedService> reference) {
        boolean serviceObtained = false;
        try {
            if ("org.apache.felix.webconsole.internal.servlet.OsgiManager".equals(reference.getProperty(Constants.SERVICE_PID))) {
                final ManagedService service = super.addingService(reference);
                serviceObtained = true;

                configureWebConsole(service, configService);

                return service;
            }
        } catch (final ConfigurationException e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagedServiceTracker.class);
            log.warn("Cannot configure Apache Felix Web Console", e);
        } catch (final RuntimeException e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagedServiceTracker.class);
            log.warn("Cannot configure Apache Felix Web Console", e);
        }
        if (serviceObtained) {
            context.ungetService(reference);
        }
        return null;
    }

    /**
     * Configures the Web Console.
     *
     * @param service The associated managed service
     * @param configService The config service
     * @throws ConfigurationException If configuration fails
     */
    public static void configureWebConsole(final ManagedService service, ConfigurationService configService) throws ConfigurationException {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put("manager.root", configService.getProperty("com.openexchange.webconsole.servletPath", "/servlet/console"));
        properties.put("username", configService.getProperty("com.openexchange.webconsole.username", "open-xchange"));
        properties.put("password", configService.getProperty("com.openexchange.webconsole.password", "secret"));
        properties.put("realm", configService.getProperty("com.openexchange.webconsole.realm", "Open-Xchange Management Console"));
        service.updated(properties);
    }

}
