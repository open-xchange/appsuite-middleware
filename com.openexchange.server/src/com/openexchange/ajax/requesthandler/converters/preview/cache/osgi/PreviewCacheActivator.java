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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.converters.preview.cache.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.converters.preview.AbstractPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.cache.FileStorePreviewCacheImpl;
import com.openexchange.ajax.requesthandler.converters.preview.cache.PreviewCacheMBean;
import com.openexchange.ajax.requesthandler.converters.preview.cache.PreviewCacheMBeanImpl;
import com.openexchange.ajax.requesthandler.converters.preview.cache.RdbPreviewCacheImpl;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.AddRefIdForPreviewCacheTable;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheCreateTableService;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheCreateTableTask;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheDeleteListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.preview.cache.PreviewCache;


/**
 * {@link PreviewCacheActivator} - Activator for preview document cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PreviewCacheActivator extends HousekeepingActivator {

    private volatile ObjectName objectName;

    /**
     * Initializes a new {@link PreviewCacheActivator}.
     */
    public PreviewCacheActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Trackers
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
        openTrackers();
        // Init service
        final PreviewCache cache;
        final EventHandler eventHandler;
        {
            final ConfigurationService configurationService = getService(ConfigurationService.class);
            final String type = configurationService.getProperty("com.openexchange.preview.cache.type", "FS").trim();
            if ("DB".equalsIgnoreCase(type)) {
                final RdbPreviewCacheImpl rdbPreviewCacheImpl = new RdbPreviewCacheImpl();
                cache = rdbPreviewCacheImpl;
                eventHandler = rdbPreviewCacheImpl;
            } else {
                final boolean quotaAware = configurationService.getBoolProperty("com.openexchange.preview.cache.quotaAware", false);
                final FileStorePreviewCacheImpl fileStorePreviewCache = new FileStorePreviewCacheImpl(quotaAware);
                cache = fileStorePreviewCache;
                eventHandler = fileStorePreviewCache;
            }
        }
        PreviewCacheMBeanImpl.CACHE_REF.set(cache);
        // Register stuff
        registerService(PreviewCache.class, cache);
        {
            final Dictionary<String, Object> d = new Hashtable<String, Object>(1);
            d.put(EventConstants.EVENT_TOPIC, new String[] { FileStorageEventConstants.UPDATE_TOPIC, FileStorageEventConstants.DELETE_TOPIC });
            registerService(EventHandler.class, eventHandler, d);
        }
        AbstractPreviewResultConverter.setPreviewCache(cache);
        /*
         * Register update task, create table job and delete listener
         */
        registerService(CreateTableService.class, new PreviewCacheCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new PreviewCacheCreateTableTask(), new AddRefIdForPreviewCacheTable()));
        registerService(DeleteListener.class, new PreviewCacheDeleteListener());
    }

    @Override
    protected void stopBundle() throws Exception {
        AbstractPreviewResultConverter.setPreviewCache(null);
        PreviewCacheMBeanImpl.CACHE_REF.set(null);
        super.stopBundle();
    }

    void registerCacheMBean(final ManagementService management) {
        ObjectName objectName = this.objectName;
        if (objectName == null) {
            try {
                objectName = getObjectName(PreviewCacheMBean.class.getName(), "com.openexchange.preview.cache");
                this.objectName = objectName;
                management.registerMBean(objectName, new PreviewCacheMBeanImpl());
            } catch (final MalformedObjectNameException e) {
                final Log LOG = com.openexchange.log.Log.loggerFor(PreviewCacheActivator.class);
                LOG.error(e.getMessage(), e);
            } catch (final NotCompliantMBeanException e) {
                final Log LOG = com.openexchange.log.Log.loggerFor(PreviewCacheActivator.class);
                LOG.error(e.getMessage(), e);
            } catch (final OXException e) {
                final Log LOG = com.openexchange.log.Log.loggerFor(PreviewCacheActivator.class);
                LOG.error(e.getMessage(), e);
            }
        }
    }

    void unregisterCacheMBean(final ManagementService management) {
        final ObjectName objectName = this.objectName;
        if (objectName != null) {
            try {
                management.unregisterMBean(objectName);
            } catch (final OXException e) {
                final Log LOG = com.openexchange.log.Log.loggerFor(PreviewCacheActivator.class);
                LOG.error(e.getMessage(), e);
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
