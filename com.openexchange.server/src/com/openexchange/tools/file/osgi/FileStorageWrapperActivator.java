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

package com.openexchange.tools.file.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;

/**
 * {@link FileStorageWrapperActivator} - The activator that wraps old legacy file storage layer around new
 * <code>com.openexchange.filestore</code> API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageWrapperActivator implements BundleActivator {

    private ServiceTracker<FileStorageService, FileStorageService> fsTracker;
    private ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        ServiceTracker<FileStorageService, FileStorageService> fsTracker = new ServiceTracker<FileStorageService, FileStorageService>(context, FileStorageService.class, new FSTrackerCustomizer(context));
        this.fsTracker = fsTracker;

        ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker = new ServiceTracker<QuotaFileStorageService, QuotaFileStorageService>(context, QuotaFileStorageService.class, new QFSTrackerCustomizer(context));
        this.qfsTracker = qfsTracker;

        fsTracker.open();
        qfsTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
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
    }

    private static class FSTrackerCustomizer implements ServiceTrackerCustomizer<FileStorageService, FileStorageService> {

        private final BundleContext context;

        public FSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public FileStorageService addingService(ServiceReference<FileStorageService> reference) {
            final FileStorageService service = context.getService(reference);
            FileStorage.setFileStorageStarter(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
            // Nothing to do here

        }

        @Override
        public void removedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
            FileStorage.setFileStorageStarter(null);
            context.ungetService(reference);
        }

    }

    private static class QFSTrackerCustomizer implements ServiceTrackerCustomizer<QuotaFileStorageService, QuotaFileStorageService> {

        private final BundleContext context;

        public QFSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public QuotaFileStorageService addingService(ServiceReference<QuotaFileStorageService> reference) {
            QuotaFileStorageService service = context.getService(reference);
            QuotaFileStorage.setQuotaFileStorageStarter(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
            // Nothing to do here

        }

        @Override
        public void removedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
            QuotaFileStorage.setQuotaFileStorageStarter(null);
            context.ungetService(reference);
        }

    }

}
