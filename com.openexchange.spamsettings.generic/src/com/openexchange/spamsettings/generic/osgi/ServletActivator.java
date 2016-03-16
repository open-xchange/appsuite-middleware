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


package com.openexchange.spamsettings.generic.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;
import com.openexchange.spamsettings.generic.service.SpamSettingService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ServletActivator extends DeferredActivator {

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletActivator.class);

    private SpamSettingsServletRegisterer servletRegisterer;

    public ServletActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, SpamSettingService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (servletRegisterer != null) {
            LOG.warn("Absent service: {}", clazz.getName());
    
            SpamSettingsServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
            servletRegisterer.registerServlet();
            SpamSettingsModulePreferences.setModule(true);
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (servletRegisterer != null) {
            LOG.info("Re-available service: {}", clazz.getName());
            servletRegisterer.unregisterServlet();
            SpamSettingsModulePreferences.setModule(false);
            SpamSettingsServiceRegistry.getServiceRegistry().removeService(clazz);
        }

    }

    @Override
    protected void startBundle() throws Exception {
        SpamSettingsServletRegisterer.PREFIX.set(getService(DispatcherPrefixService.class));
        final ServiceRegistry registry = SpamSettingsServiceRegistry.getServiceRegistry();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (final Class<?> classe : classes) {
            final Object service = getService(classe);
            if (service != null) {
                registry.addService(classe, service);
            }
        }
        servletRegisterer = new SpamSettingsServletRegisterer();
        servletRegisterer.registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {
        if (servletRegisterer != null) {
            servletRegisterer.unregisterServlet();
            servletRegisterer = null;
            SpamSettingsModulePreferences.setModule(false);
            SpamSettingsServiceRegistry.getServiceRegistry().clearRegistry();
            SpamSettingsServletRegisterer.PREFIX.set(null);
        }
    }
}
