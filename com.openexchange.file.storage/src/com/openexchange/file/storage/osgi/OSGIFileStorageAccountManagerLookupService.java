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

package com.openexchange.file.storage.osgi;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;

/**
 * {@link OSGIFileStorageAccountManagerLookupService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class OSGIFileStorageAccountManagerLookupService implements FileStorageAccountManagerLookupService {

    /**
     * Dummy value.
     */
    protected static final Object PRESENT = new Object();

    /**
     * The backing queue.
     */
    protected final ConcurrentMap<FileStorageAccountManagerProvider, Object> providers;

    /**
     * The bundle context reference.
     */
    protected volatile BundleContext bundleContext;

    /**
     * The tracker instance.
     */
    private volatile ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker;

    /**
     * Used to "serialize" initialization attempts.
     */
    private final AtomicReference<Future<Void>> serializer;

    /**
     * Initializes a new {@link OSGIFileStorageAccountManagerLookupService}.
     */
    public OSGIFileStorageAccountManagerLookupService() {
        super();
        serializer = new AtomicReference<Future<Void>>();
        providers = new ConcurrentHashMap<FileStorageAccountManagerProvider, Object>(8);
    }

    /**
     * Starts the tracker.
     *
     * @param context The bundle context
     */
    public void start(final BundleContext context) {
        this.bundleContext = context;
        if (null == tracker) {
            final ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker = new ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider>(context, FileStorageAccountManagerProvider.class, new Customizer());
            tracker.open();
            this.tracker = tracker;
        }
    }

    /**
     * Stops the tracker.
     */
    public void stop() {
        final ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }
        this.bundleContext = null;
    }

    private static final String PARAM_DEFAULT_ACCOUNT = "file.storage.defaultAccount";

    @Override
    public FileStorageAccountManager getAccountManager(final String accountId, final Session session) throws OXException {
        initIfAbsent(null);

        final String paramName = PARAM_DEFAULT_ACCOUNT + '@' + accountId;
        FileStorageAccountManager accountManager = (FileStorageAccountManager) session.getParameter(paramName);
        if (null == accountManager) {
            FileStorageAccountManagerProvider candidate = null;
            for (final FileStorageAccountManagerProvider provider : providers.keySet()) {
                if ((null == candidate) || (provider.getRanking() > candidate.getRanking())) {
                    final FileStorageAccountManager cAccountManager = provider.getAccountManager(accountId, session);
                    if (null != cAccountManager) {
                        candidate = provider;
                        accountManager = cAccountManager;
                    }
                }
            }
            if (null == accountManager) {
                return null;
            }
            session.setParameter(paramName, accountManager);
        }
        return accountManager;
    }

    @Override
    public FileStorageAccountManager getAccountManagerFor(final FileStorageService service) throws OXException {
        initIfAbsent(service.getId());

        FileStorageAccountManagerProvider candidate = null;
        for (final FileStorageAccountManagerProvider provider : providers.keySet()) {
            if (provider.supports(service) && ((null == candidate) || (provider.getRanking() > candidate.getRanking()))) {
                candidate = provider;
            }
        }
        if (null == candidate) {
            throw FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.create(service.getId());
        }
        return candidate.getAccountManagerFor(service);
    }

    private void initIfAbsent(final String serviceId) throws OXException {
        Future<Void> future = serializer.get();
        if (null == future) {
            final BundleContext bundleContext = this.bundleContext;
            final FutureTask<Void> ft = new FutureTask<Void>(new Callable<Void>() {

                @Override
                public Void call() throws OXException {
                    if (null == bundleContext) {
                        if (null == serviceId) {
                            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("Missing bundle context.");
                        }
                        throw FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.create(serviceId);
                    }
                    try {
                        final Collection<ServiceReference<FileStorageAccountManagerProvider>> references = bundleContext.getServiceReferences(FileStorageAccountManagerProvider.class, null);
                        for (final ServiceReference<FileStorageAccountManagerProvider> reference : references) {
                            final FileStorageAccountManagerProvider addMe = bundleContext.getService(reference);
                            providers.putIfAbsent(addMe, PRESENT);
                        }
                    } catch (final InvalidSyntaxException e) {
                        if (null == serviceId) {
                            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                        }
                        throw FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.create(e, serviceId);
                    } catch (final RuntimeException e) {
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                    return null;
                }
            });
            if (serializer.compareAndSet(null, ft)) {
                ft.run();
                future = ft;
            } else {
                future = serializer.get();
            }
        }
        try {
            future.get();
        } catch (final InterruptedException e) {
            // Keep interrupted flag
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    private final class Customizer implements ServiceTrackerCustomizer<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> {

        protected Customizer() {
            super();
        }

        @Override
        public FileStorageAccountManagerProvider addingService(final ServiceReference<FileStorageAccountManagerProvider> reference) {
            final BundleContext context = bundleContext;
            final FileStorageAccountManagerProvider service = context.getService(reference);
            {
                if (null == providers.putIfAbsent(service, PRESENT)) {
                    return service;
                }
                final Log logger = LogFactory.getLog(OSGIFileStorageAccountManagerLookupService.Customizer.class);
                if (logger.isWarnEnabled()) {
                    logger.warn(new StringBuilder(128).append("File storage account manager provider ").append(service.getClass().getSimpleName()).append(
                        " could not be added. Provider is already present.").toString());
                }
            }
            /*
             * Adding to registry failed
             */
            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(final ServiceReference<FileStorageAccountManagerProvider> reference, final FileStorageAccountManagerProvider service) {
            // Nothing to do
        }

        @Override
        public void removedService(final ServiceReference<FileStorageAccountManagerProvider> reference, final FileStorageAccountManagerProvider service) {
            if (null != service) {
                try {
                    providers.remove(service);
                } finally {
                    bundleContext.ungetService(reference);
                }
            }
        }
    } // End of Customizer class

}
