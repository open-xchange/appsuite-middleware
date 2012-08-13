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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.session.Session;

/**
 * {@link OSGIFileStorageAccountManagerLookupService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class OSGIFileStorageAccountManagerLookupService implements FileStorageAccountManagerLookupService {

    /**
     * The backing list.
     */
    final Queue<FileStorageAccountManagerProvider> providers;

    /**
     * The bundle context reference.
     */
    private BundleContext bundleContext;

    /**
     * The tracker instance.
     */
    private ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker;

    final OSGIEventAdminLookup eventAdminLookup;

    private final AtomicReference<Future<Void>> serializer;

    /**
     * Initializes a new {@link OSGIFileStorageAccountManagerLookupService}.
     */
    public OSGIFileStorageAccountManagerLookupService(final OSGIEventAdminLookup eventAdminLookup) {
        super();
        serializer = new AtomicReference<Future<Void>>();
        providers = new Java7ConcurrentLinkedQueue<FileStorageAccountManagerProvider>();
        this.eventAdminLookup = eventAdminLookup;
    }

    /**
     * Starts the tracker.
     *
     * @param context The bundle context
     */
    public void start(final BundleContext context) {
        if (null == tracker) {
            tracker = new ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider>(context, FileStorageAccountManagerProvider.class, new Customizer(context));
            tracker.open();
        }
    }

    /**
     * Stops the tracker.
     */
    public void stop() {
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
    }

    @Override
    public FileStorageAccountManager getAccountManager(final String accountId, final Session session) throws OXException {
        initIfAbsent(null);

        FileStorageAccountManagerProvider candidate = null;
        FileStorageAccountManager accountManager = null;
        for (final FileStorageAccountManagerProvider provider : providers) {
            final FileStorageAccountManager cAccountManager = provider.getAccountManager(accountId, session);
            if ((null != cAccountManager) && ((null == candidate) || (provider.getRanking() > candidate.getRanking()))) {
                candidate = provider;
                accountManager = cAccountManager;
            }
        }
        if (null == accountManager) {
            return null;
        }
        return accountManager;
    }

    @Override
    public FileStorageAccountManager getAccountManagerFor(final FileStorageService service) throws OXException {
        initIfAbsent(service.getId());

        FileStorageAccountManagerProvider candidate = null;
        for (final FileStorageAccountManagerProvider provider : providers) {
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
                            if (!providers.contains(addMe)) {
                                providers.add(addMe);
                                /*
                                 * Post event
                                 */
                                final EventAdmin eventAdmin = eventAdminLookup.getEventAdmin();
                                if (null != eventAdmin) {
                                    final Dictionary<String, Object> dict = new Hashtable<String, Object>(2);
                                    dict.put(FileStorageAccountManagerProvider.PROPERTY_RANKING, Integer.valueOf(addMe.getRanking()));
                                    dict.put(FileStorageAccountManagerProvider.PROPERTY_PROVIDER, addMe);
                                    final Event event = new Event(FileStorageAccountManagerProvider.TOPIC, dict);
                                    eventAdmin.postEvent(event);
                                }
                            }
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

        private final BundleContext context;

        protected Customizer(final BundleContext context) {
            super();
            this.context = context;
        }

        @Override
        public FileStorageAccountManagerProvider addingService(final ServiceReference<FileStorageAccountManagerProvider> reference) {
            final FileStorageAccountManagerProvider service = context.getService(reference);
            {
                final FileStorageAccountManagerProvider addMe = service;
                if (!providers.contains(addMe)) {
                    providers.add(addMe);
                    /*
                     * Post event
                     */
                    final EventAdmin eventAdmin = eventAdminLookup.getEventAdmin();
                    if (null != eventAdmin) {
                        final Dictionary<String, Object> dict = new Hashtable<String, Object>(2);
                        dict.put(FileStorageAccountManagerProvider.PROPERTY_RANKING, Integer.valueOf(addMe.getRanking()));
                        dict.put(FileStorageAccountManagerProvider.PROPERTY_PROVIDER, addMe);
                        final Event event = new Event(FileStorageAccountManagerProvider.TOPIC, dict);
                        eventAdmin.postEvent(event);
                    }
                    return service;
                }
                final org.apache.commons.logging.Log logger =
                    com.openexchange.log.LogFactory.getLog(OSGIFileStorageAccountManagerLookupService.Customizer.class);
                if (logger.isWarnEnabled()) {
                    logger.warn(new StringBuilder(128).append("File storage account manager provider ").append(addMe.getClass().getSimpleName()).append(
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
                    final FileStorageAccountManagerProvider removeMe = service;
                    providers.remove(removeMe);
                } finally {
                    context.ungetService(reference);
                }
            }
        }
    } // End of Customizer class

}
