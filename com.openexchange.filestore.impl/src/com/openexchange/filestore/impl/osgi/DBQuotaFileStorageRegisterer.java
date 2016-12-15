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

package com.openexchange.filestore.impl.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.filestore.QuotaBackendService;
import com.openexchange.filestore.impl.DBQuotaFileStorageService;
import com.openexchange.filestore.impl.groupware.QuotaModePreferenceItem;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;

/**
 * {@link DBQuotaFileStorageRegisterer} - Registers the {@link QuotaFileStorageService} service if all required services are available.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DBQuotaFileStorageRegisterer implements ServiceTrackerCustomizer<FileStorageService, FileStorageService> {

    private final BundleContext context;
    private final Lock lock = new ReentrantLock();

    private final QuotaFileStorageListenerTracker listenerTracker;
    private final RankingAwareNearRegistryServiceTracker<QuotaBackendService> backendServices;

    private List<ServiceRegistration<?>> registrations;
    boolean isRegistered = false;

    /**
     * Initializes a new {@link DBQuotaFileStorageRegisterer}.
     *
     * @param context The bundle context
     */
    public DBQuotaFileStorageRegisterer(RankingAwareNearRegistryServiceTracker<QuotaBackendService> backendServices, QuotaFileStorageListenerTracker listenerTracker, BundleContext context) {
        super();
        this.backendServices = backendServices;
        this.listenerTracker = listenerTracker;
        this.context = context;
    }

    @Override
    public FileStorageService addingService(ServiceReference<FileStorageService> reference) {
        FileStorageService service = context.getService(reference);

        lock.lock();
        try {
            boolean needsRegistration = false;
            if (false == isRegistered) {
                needsRegistration = true;
                isRegistered = true;
            }
            if (needsRegistration) {
                List<ServiceRegistration<?>> registrations = new ArrayList<>(4);
                this.registrations = registrations;

                QuotaFileStorageService qfss = new DBQuotaFileStorageService(backendServices, listenerTracker, service);
                registrations.add(context.registerService(QuotaFileStorageService.class, qfss, null));

                QuotaModePreferenceItem item = new QuotaModePreferenceItem(qfss);
                registrations.add(context.registerService(PreferencesItemService.class, item, null));
                registrations.add(context.registerService(ConfigTreeEquivalent.class, item, null));

                return service;
            }
        } finally {
            lock.unlock();
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
        lock.lock();
        try {
            boolean needsUnregistration = false;
            List<ServiceRegistration<?>> regs = registrations;
            if (isRegistered) {
                registrations = null;
                needsUnregistration = true;
                isRegistered = false;
            }
            if (needsUnregistration) {
                for (ServiceRegistration<?> reg : regs) {
                    reg.unregister();
                }
            }
            context.ungetService(reference);
        } finally {
            lock.unlock();
        }
    }

}
