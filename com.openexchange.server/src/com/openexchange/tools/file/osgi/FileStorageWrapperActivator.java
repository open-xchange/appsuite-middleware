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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.external.FileStorageFactory;
import com.openexchange.tools.file.external.QuotaFileStorageFactory;

public class FileStorageWrapperActivator implements BundleActivator {

    private ServiceTracker<FileStorageFactory,FileStorageFactory> FSTracker;

    private ServiceTracker<QuotaFileStorageFactory,QuotaFileStorageFactory> QFSTracker;

    @Override
    public void start(final BundleContext context) throws Exception {
        FSTracker = new ServiceTracker<FileStorageFactory,FileStorageFactory>(context, FileStorageFactory.class, new FSTrackerCustomizer(context));
        QFSTracker = new ServiceTracker<QuotaFileStorageFactory,QuotaFileStorageFactory>(context, QuotaFileStorageFactory.class, new QFSTrackerCustomizer(context));
        FSTracker.open();
        QFSTracker.open();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        FSTracker.close();
        QFSTracker.close();
    }

    private static class FSTrackerCustomizer implements ServiceTrackerCustomizer<FileStorageFactory,FileStorageFactory> {

        private final BundleContext context;

        public FSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public FileStorageFactory addingService(final ServiceReference<FileStorageFactory> reference) {
            final FileStorageFactory service = context.getService(reference);
            FileStorage.setFileStorageStarter(service);
            return service;
        }

        @Override
        public void modifiedService(final ServiceReference<FileStorageFactory> reference, final FileStorageFactory service) {
            // Nothing to do here

        }

        @Override
        public void removedService(final ServiceReference<FileStorageFactory> reference, final FileStorageFactory service) {
            FileStorage.setFileStorageStarter(null);
            context.ungetService(reference);
        }

    }

    private static class QFSTrackerCustomizer implements ServiceTrackerCustomizer<QuotaFileStorageFactory,QuotaFileStorageFactory> {

        private final BundleContext context;

        public QFSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public QuotaFileStorageFactory addingService(final ServiceReference<QuotaFileStorageFactory> reference) {
            final QuotaFileStorageFactory service = context.getService(reference);
            QuotaFileStorage.setQuotaFileStorageStarter(service);
            return service;
        }

        @Override
        public void modifiedService(final ServiceReference<QuotaFileStorageFactory> reference, final QuotaFileStorageFactory service) {
            // Nothing to do here

        }

        @Override
        public void removedService(final ServiceReference<QuotaFileStorageFactory> reference, final QuotaFileStorageFactory service) {
            QuotaFileStorage.setQuotaFileStorageStarter(null);
            context.ungetService(reference);
        }

    }

}
