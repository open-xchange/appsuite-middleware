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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.contactcollector.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.contactcollector.folder.ContactCollectorFolderCreator;
import com.openexchange.contactcollector.internal.ContactCollectorServiceImpl;
import com.openexchange.contactcollector.preferences.ContactCollectEnabled;
import com.openexchange.contactcollector.preferences.ContactCollectFolder;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link BundleActivator Activator} for contact collector.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Activator.class);

    private ServiceRegistration registryCollector;

    private ServiceRegistration registryPrefItemFolder;

    private ServiceRegistration registryPrefItemEnabled;

    private ServiceRegistration registryFolderCreator;

    private ContactCollectorServiceImpl collectorInstance;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        /*
         * (Re-)Initialize service registry with available services
         */
        {
            final ServiceRegistry registry = ServiceRegistry.getInstance();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (int i = 0; i < classes.length; i++) {
                final Object service = getService(classes[i]);
                if (null != service) {
                    registry.addService(classes[i], service);
                }
            }
        }

        collectorInstance = new ContactCollectorServiceImpl();
        collectorInstance.start();
        registryFolderCreator = context.registerService(LoginHandlerService.class.getName(), new ContactCollectorFolderCreator(), null);
        registryCollector = context.registerService(ContactCollectorService.class.getName(), collectorInstance, null);
        registryPrefItemFolder = context.registerService(PreferencesItemService.class.getName(), new ContactCollectFolder(), null);
        registryPrefItemEnabled = context.registerService(PreferencesItemService.class.getName(), new ContactCollectEnabled(), null);
    }

    @Override
    public void stopBundle() throws Exception {
        registryPrefItemEnabled.unregister();
        registryPrefItemFolder.unregister();
        registryCollector.unregister();
        registryFolderCreator.unregister();
        try {
            collectorInstance.stop();
        } catch (final InterruptedException e) {
            LOG.error("Contact collector shut-down interrupted", e);
        } finally {
            collectorInstance = null;
        }
        /*
         * Clear service registry
         */
        ServiceRegistry.getInstance().clearRegistry();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class, UserConfigurationService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        ServiceRegistry.getInstance().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        ServiceRegistry.getInstance().removeService(clazz);
    }

}
