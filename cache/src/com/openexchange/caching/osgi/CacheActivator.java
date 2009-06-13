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

package com.openexchange.caching.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.caching.CacheInformationMBean;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.internal.JCSCacheInformation;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.management.ManagementException;
import com.openexchange.management.ManagementService;
import com.openexchange.server.osgiservice.DeferredActivator;

/**
 * {@link CacheActivator} - The {@link DeferredActivator} implementation for cache bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(CacheActivator.class);

    private final Dictionary<String, String> dictionary;

    private ServiceRegistration serviceRegistration;

    private ObjectName objectName;

    private ServiceTracker tracker;

    /**
     * Initializes a new {@link CacheActivator}.
     */
    public CacheActivator() {
        super();
        dictionary = new Hashtable<String, String>();
        dictionary.put("name", "oxcache");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (ConfigurationService.class.equals(clazz)) {
            JCSCacheServiceInit.getInstance().setConfigurationService(null);
        }
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        /*
         * TODO: Reconfigure with newly available configuration service?
         */
        if (ConfigurationService.class.equals(clazz)) {
            JCSCacheServiceInit.getInstance().setConfigurationService(getService(ConfigurationService.class));
            JCSCacheServiceInit.getInstance().reconfigureByPropertyFile();
        }
    }

    @Override
    protected void startBundle() throws Exception {
        JCSCacheServiceInit.initInstance();
        JCSCacheServiceInit.getInstance().start(getService(ConfigurationService.class));
        /*
         * Register service
         */
        serviceRegistration = context.registerService(CacheService.class.getName(), JCSCacheService.getInstance(), dictionary);
        final class ServiceTrackerCustomizerImpl implements ServiceTrackerCustomizer {

            private final BundleContext bundleContext;

            public ServiceTrackerCustomizerImpl(final BundleContext bundleContext) {
                super();
                this.bundleContext = bundleContext;
            }

            public Object addingService(final ServiceReference reference) {
                final ManagementService management = (ManagementService) bundleContext.getService(reference);
                registerCacheMBean(management);
                return management;
            }

            public void modifiedService(final ServiceReference reference, final Object service) {
                // Nothing to do.
            }

            public void removedService(final ServiceReference reference, final Object service) {
                final ManagementService management = (ManagementService) service;
                unregisterCacheMBean(management);
                bundleContext.ungetService(reference);
            }
        }
        tracker = new ServiceTracker(context, ManagementService.class.getName(), new ServiceTrackerCustomizerImpl(context));
        tracker.open();
    }

    @Override
    protected void stopBundle() {
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
        /*
         * Stop cache
         */
        JCSCacheServiceInit.getInstance().stop();
        JCSCacheServiceInit.releaseInstance();
    }

    void registerCacheMBean(final ManagementService management) {
        if (objectName == null) {
            try {
                objectName = getObjectName(JCSCacheInformation.class.getName(), CacheInformationMBean.CACHE_DOMAIN);
                management.registerMBean(objectName, new JCSCacheInformation());
            } catch (final MalformedObjectNameException e) {
                LOG.error(e.getMessage(), e);
            } catch (final NotCompliantMBeanException e) {
                LOG.error(e.getMessage(), e);
            } catch (final ManagementException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    void unregisterCacheMBean(final ManagementService management) {
        if (objectName != null) {
            try {
                management.unregisterMBean(objectName);
            } catch (final ManagementException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                objectName = null;
            }
        }
    }

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name and domain name.
     * 
     * @param className The class name to use as object name
     * @param domain The domain name
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     */
    private static ObjectName getObjectName(final String className, final String domain) throws MalformedObjectNameException {
        final int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", pos == -1 ? className : className.substring(pos + 1));
    }

}
