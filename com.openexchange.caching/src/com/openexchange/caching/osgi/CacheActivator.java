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

package com.openexchange.caching.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.caching.CacheInformationMBean;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.DefaultCacheKeyService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.internal.AbstractCache;
import com.openexchange.caching.internal.JCSCacheInformation;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link CacheActivator} - The {@link DeferredActivator} implementation for cache bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CacheActivator.class);

    static volatile CacheService cacheService;

    /**
     * Gets the cacheService
     *
     * @return The cacheService or <code>null</code>
     */
    public static CacheService getCacheService() {
        return cacheService;
    }

    private volatile ObjectName objectName;

    /**
     * Initializes a new {@link CacheActivator}.
     */
    public CacheActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, CacheEventService.class };
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
        final ConfigurationService service = getService(ConfigurationService.class);
        JCSCacheServiceInit.getInstance().start(service);
        JCSCacheServiceInit.getInstance().setCacheEventService(getService(CacheEventService.class));
        registerService(CacheKeyService.class, new DefaultCacheKeyService());
        /*
         * Register service
         */
        if (service.getBoolProperty("com.openexchange.caching.jcs.enabled", true)) {
            final Dictionary<String, Object> dictionary = new Hashtable<String, Object>(2);
            dictionary.put("name", "oxcache");
            dictionary.put(Constants.SERVICE_RANKING, Integer.valueOf(10));
            final JCSCacheService jcsCacheService = JCSCacheService.getInstance();
            registerService(CacheService.class, jcsCacheService, dictionary);
            cacheService = jcsCacheService;
        } else {
            LOG.info("\n\n\tDefault cache service implementation has been disabled.\n");
            track(CacheService.class, new SimpleRegistryListener<CacheService>() {

                @Override
                public void added(ServiceReference<CacheService> ref, CacheService service) {
                    cacheService = service;
                }

                @Override
                public void removed(ServiceReference<CacheService> ref, CacheService service) {
                    cacheService = null;
                }
            });
        }
        final class ServiceTrackerCustomizerImpl implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

            private final BundleContext bundleContext;

            public ServiceTrackerCustomizerImpl(final BundleContext bundleContext) {
                super();
                this.bundleContext = bundleContext;
            }

            @Override
            public ManagementService addingService(final ServiceReference<ManagementService> reference) {
                final ManagementService management = bundleContext.getService(reference);
                registerCacheMBean(management);
                return management;
            }

            @Override
            public void modifiedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
                // Nothing to do.
            }

            @Override
            public void removedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
                final ManagementService management = service;
                unregisterCacheMBean(management);
                bundleContext.ungetService(reference);
            }
        }
        track(ManagementService.class, new ServiceTrackerCustomizerImpl(context));
        track(EventAdmin.class, new SimpleRegistryListener<EventAdmin>() {

            @Override
            public void added(final ServiceReference<EventAdmin> ref, final EventAdmin service) {
                AbstractCache.setEventAdmin(service);
            }

            @Override
            public void removed(final ServiceReference<EventAdmin> ref, final EventAdmin service) {
                AbstractCache.setEventAdmin(null);
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() {
        cacheService = null;
        cleanUp();
        /*
         * Stop cache
         */
        final JCSCacheServiceInit instance = JCSCacheServiceInit.getInstance();
        if (null != instance) {
            instance.stop();
        }
        JCSCacheServiceInit.releaseInstance();
    }

    void registerCacheMBean(final ManagementService management) {
        ObjectName objectName = this.objectName;
        if (objectName == null) {
            try {
                objectName = getObjectName(JCSCacheInformation.class.getName(), CacheInformationMBean.CACHE_DOMAIN);
                this.objectName = objectName;
                management.registerMBean(objectName, new JCSCacheInformation());
            } catch (final MalformedObjectNameException e) {
                LOG.error("", e);
            } catch (final NotCompliantMBeanException e) {
                LOG.error("", e);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    void unregisterCacheMBean(final ManagementService management) {
        final ObjectName objectName = this.objectName;
        if (objectName != null) {
            try {
                management.unregisterMBean(objectName);
            } catch (final OXException e) {
                LOG.error("", e);
            } finally {
                this.objectName = null;
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
