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

package com.openexchange.service.indexing.impl.osgi;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.smal.SmalAccessService;
import com.openexchange.management.ManagementService;
import com.openexchange.mq.MQService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.IndexingServiceMBean;
import com.openexchange.service.indexing.impl.CompositeServiceLookup;
import com.openexchange.service.indexing.impl.IndexingServiceImpl;
import com.openexchange.service.indexing.impl.IndexingServiceInit;
import com.openexchange.service.indexing.impl.IndexingServiceMBeanImpl;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link IndexingServiceActivator} - The activator for indexing service.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexingServiceActivator extends HousekeepingActivator {

    private volatile IndexingServiceInit serviceInit;

    /**
     * Initializes a new {@link IndexingServiceActivator}.
     */
    public IndexingServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, MQService.class, ThreadPoolService.class, DatabaseService.class, MailService.class,
            SmalAccessService.class, IndexFacadeService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingServiceActivator.class));
        log.info("Starting bundle: com.openexchange.service.indexing.impl");
        try {
            final CompositeServiceLookup compositeServiceLookup = new CompositeServiceLookup(context);
            compositeServiceLookup.addAll(getNeededServices(), this);
            Services.setServiceLookup(compositeServiceLookup);
            /*
             * IndexingService initialization
             */
            final int maxConcurrentJobs = 8;
            final IndexingServiceInit serviceInit = new IndexingServiceInit(maxConcurrentJobs, this);
            serviceInit.init();
            /*
             * Start receiving jobs? --> indexing-service.properties
             */
            {
                final ConfigurationService service = getService(ConfigurationService.class);
                final boolean startReceiver = service.getBoolProperty("com.openexchange.service.indexing.startReceiver", true);
                if (startReceiver) {
                    serviceInit.initReceiver();
                }
            }
            this.serviceInit = serviceInit;
            /*
             * Register service
             */
            final IndexingServiceImpl indexingService = new IndexingServiceImpl(serviceInit);
            registerService(IndexingService.class, indexingService);
            addService(IndexingService.class, indexingService);
            compositeServiceLookup.addIfAbsent(IndexingService.class, new ServiceLookup() {

                @Override
                public <S> S getService(final Class<? extends S> clazz) {
                    if (!IndexingService.class.equals(clazz)) {
                        throw new IllegalStateException("Invalid class: " + clazz.getName());
                    }
                    @SuppressWarnings("unchecked")
                    final S ret = (S) indexingService;
                    return ret;
                }

                @Override
                public <S> S getOptionalService(final Class<? extends S> clazz) {
                    return getService(clazz);
                }
            });
            /*
             * Service tracker(s)
             */
            final ObjectName objectName = new ObjectName(IndexingServiceMBean.DOMAIN, "name", "Indexing Service MBean");
            // trackService(ManagementService.class);
            track(ManagementService.class, new SimpleRegistryListener<ManagementService>() {

                @Override
                public void added(final ServiceReference<ManagementService> ref, final ManagementService service) {
                    try {
                        service.registerMBean(objectName, new IndexingServiceMBeanImpl());
                    } catch (final NotCompliantMBeanException e) {
                        log.error(e.getMessage(), e);
                    } catch (final OXException e) {
                        log.error(e.getMessage(), e);
                    }
                }

                @Override
                public void removed(final ServiceReference<ManagementService> ref, final ManagementService service) {
                    try {
                        service.unregisterMBean(objectName);
                    } catch (final OXException e) {
                        log.error(e.getMessage(), e);
                    }
                }

            });
            openTrackers();

            /*-
             * ------------------- Test ---------------------
             */
            // serviceInit.getSender().sendJobMessage(new EchoIndexJob("Echo..."));
        } catch (final Exception e) {
            log.error("Error starting bundle: com.openexchange.service.indexing.impl", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingServiceActivator.class));
        log.info("Stopping bundle: com.openexchange.service.indexing.impl");
        try {
            /*
             * Unregister service
             */
            unregisterServices();
            final CompositeServiceLookup lookup = Services.getServiceLookup();
            if (null != lookup) {
                lookup.close();
                Services.setServiceLookup(null);
            }
            /*
             * IndexingService shut-down
             */
            final IndexingServiceInit serviceInit = this.serviceInit;
            if (null != serviceInit) {
                serviceInit.drop();
                this.serviceInit = null;
            }
            /*
             * Perform rest
             */
            super.stopBundle();
        } catch (final Exception e) {
            log.error("Error stopping bundle: com.openexchange.service.indexing.impl", e);
            throw e;
        } finally {
            Services.setServiceLookup(null);
        }
    }

}
