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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.filestore.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.filestore.FileStorage2ContextsResolver;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorageService;


/**
 * {@link FileStorageActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class FileStorageActivator implements BundleActivator {

    private volatile ServiceTracker<FileStorageService, FileStorageService> fsTracker;
    private volatile ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker;
    private volatile ServiceTracker<FileStorage2ContextsResolver, FileStorage2ContextsResolver> resolverTracker;

    /**
     * Initializes a new {@link FileStorageActivator}.
     */
    public FileStorageActivator() {
        super();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        try {
            ServiceTracker<FileStorageService, FileStorageService> fsTracker = new ServiceTracker<FileStorageService, FileStorageService>(context, FileStorageService.class, new FSTrackerCustomizer(context));
            this.fsTracker = fsTracker;

            ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker = new ServiceTracker<QuotaFileStorageService, QuotaFileStorageService>(context, QuotaFileStorageService.class, new QFSTrackerCustomizer(context));
            this.qfsTracker = qfsTracker;

            ServiceTracker<FileStorage2ContextsResolver, FileStorage2ContextsResolver> resolverTracker = new ServiceTracker<FileStorage2ContextsResolver, FileStorage2ContextsResolver>(context, FileStorage2ContextsResolver.class, new ResolverTrackerCustomizer(context));
            this.resolverTracker = resolverTracker;

            fsTracker.open();
            qfsTracker.open();
            resolverTracker.open();
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(FileStorageActivator.class);
            logger.error("Failed to start {}", context.getBundle().getSymbolicName(), e);
            throw e;
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            ServiceTracker<FileStorageService, FileStorageService> fsTracker = this.fsTracker;
            if (null != fsTracker) {
                fsTracker.close();
                this.fsTracker = null;
            }

            ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker = this.qfsTracker;
            if (null != qfsTracker) {
                qfsTracker.close();
                this.qfsTracker = null;
            }

            ServiceTracker<FileStorage2ContextsResolver, FileStorage2ContextsResolver> resolverTracker = this.resolverTracker;
            if (null != resolverTracker) {
                resolverTracker.close();
                this.resolverTracker = null;
            }
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(FileStorageActivator.class);
            logger.error("Failed to stop {}", context.getBundle().getSymbolicName(), e);
            throw e;
        }
    }

    private static class FSTrackerCustomizer implements ServiceTrackerCustomizer<FileStorageService, FileStorageService> {

        private final BundleContext context;

        FSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public FileStorageService addingService(ServiceReference<FileStorageService> reference) {
            final FileStorageService service = context.getService(reference);
            FileStorages.setFileStorageService(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
            // Nothing to do here

        }

        @Override
        public void removedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
            FileStorages.setFileStorageService(null);
            context.ungetService(reference);
        }

    }

    private static class QFSTrackerCustomizer implements ServiceTrackerCustomizer<QuotaFileStorageService, QuotaFileStorageService> {

        private final BundleContext context;

        QFSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public QuotaFileStorageService addingService(ServiceReference<QuotaFileStorageService> reference) {
            QuotaFileStorageService service = context.getService(reference);
            FileStorages.setQuotaFileStorageService(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
            // Nothing to do here

        }

        @Override
        public void removedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
            FileStorages.setQuotaFileStorageService(null);
            context.ungetService(reference);
        }

    }

    private static class ResolverTrackerCustomizer implements ServiceTrackerCustomizer<FileStorage2ContextsResolver, FileStorage2ContextsResolver> {

        private final BundleContext context;

        ResolverTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public FileStorage2ContextsResolver addingService(ServiceReference<FileStorage2ContextsResolver> reference) {
            FileStorage2ContextsResolver service = context.getService(reference);
            FileStorages.setFileStorage2ContextsResolver(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<FileStorage2ContextsResolver> reference, FileStorage2ContextsResolver service) {
            // Nothing to do here

        }

        @Override
        public void removedService(ServiceReference<FileStorage2ContextsResolver> reference, FileStorage2ContextsResolver service) {
            FileStorages.setFileStorage2ContextsResolver(null);
            context.ungetService(reference);
        }

    }

}
