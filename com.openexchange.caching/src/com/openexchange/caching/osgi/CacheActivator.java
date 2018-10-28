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
import javax.management.ObjectName;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
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
import com.openexchange.management.ManagementService;
import com.openexchange.management.osgi.HousekeepingManagementTracker;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link CacheActivator} - The {@link DeferredActivator} implementation for cache bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheActivator extends HousekeepingActivator {

    private ObjectName objectName;

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
        final JCSCacheService jcsCacheService = JCSCacheService.getInstance();
        {
            final Dictionary<String, Object> dictionary = new Hashtable<String, Object>(2);
            dictionary.put("name", "oxcache");
            dictionary.put(Constants.SERVICE_RANKING, Integer.valueOf(10));
            registerService(CacheService.class, jcsCacheService, dictionary);
        }

        track(ManagementService.class, new HousekeepingManagementTracker(context, JCSCacheInformation.class.getName(), CacheInformationMBean.CACHE_DOMAIN, new JCSCacheInformation(jcsCacheService)));
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
    protected void stopBundle() throws Exception {
        super.stopBundle();
        /*
         * Stop cache
         */
        final JCSCacheServiceInit instance = JCSCacheServiceInit.getInstance();
        if (null != instance) {
            instance.stop();
        }
        JCSCacheServiceInit.releaseInstance();
    }
}
