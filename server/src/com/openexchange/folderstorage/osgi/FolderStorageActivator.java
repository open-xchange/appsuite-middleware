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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.osgi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderExceptionFactory;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.osgi.CacheFolderStorageActivator;
import com.openexchange.folderstorage.database.osgi.DatabaseFolderStorageActivator;
import com.openexchange.folderstorage.internal.ContentTypeRegistry;
import com.openexchange.folderstorage.internal.FolderServiceImpl;
import com.openexchange.folderstorage.mail.osgi.MailFolderStorageActivator;
import com.openexchange.folderstorage.virtual.osgi.VirtualFolderStorageActivator;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link FolderStorageActivator} - {@link BundleActivator Activator} for folder storage framework.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderStorageActivator implements BundleActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FolderStorageActivator.class);

    private ComponentRegistration componentRegistration;

    private List<ServiceRegistration> serviceRegistrations;

    private List<ServiceTracker> serviceTrackers;

    private List<BundleActivator> activators;

    /**
     * Initializes a new {@link FolderStorageActivator}.
     */
    public FolderStorageActivator() {
        super();
    }

    public void start(final BundleContext context) throws Exception {
        try {
            // Register error component
            componentRegistration = new ComponentRegistration(
                context,
                EnumComponent.FOLDER,
                "com.openexchange.folderstorage",
                FolderExceptionFactory.getInstance());
            // Register services
            serviceRegistrations = new ArrayList<ServiceRegistration>(4);
            // Register folder service
            serviceRegistrations.add(context.registerService(FolderService.class.getName(), new FolderServiceImpl(), null));
            serviceRegistrations.add(context.registerService(
                ContentTypeDiscoveryService.class.getName(),
                ContentTypeRegistry.getInstance(),
                null));
            // Register service trackers
            serviceTrackers = new ArrayList<ServiceTracker>(4);
            serviceTrackers.add(new ServiceTracker(context, FolderStorage.class.getName(), new FolderStorageTracker(context)));
            for (final ServiceTracker serviceTracker : serviceTrackers) {
                serviceTracker.open();
            }

            // Start other activators
            activators = new ArrayList<BundleActivator>(4);
            activators.add(new DatabaseFolderStorageActivator()); // Database impl
            activators.add(new MailFolderStorageActivator()); // Mail impl
            activators.add(new CacheFolderStorageActivator()); // Cache impl
            activators.add(new VirtualFolderStorageActivator()); // Virtual storage activator
            BundleActivator activator = null;
            for (final Iterator<BundleActivator> iter = activators.iterator(); iter.hasNext();) {
                try {
                    if (isBundleResolved(context)) {
                        if (null != activator) {
                            logFailedStartup(activator);
                        }
                        return;
                    }
                } catch (final IllegalStateException e) {
                    if (null != activator) {
                        logFailedStartup(activator);
                    }
                    return;
                }
                activator = iter.next();
                activator.start(context);
            }

            if (LOG.isInfoEnabled()) {
                final StringBuilder sb = new StringBuilder(32);
                sb.append("Bundle \"");
                sb.append(FolderStorageActivator.class.getName());
                sb.append("\" successfully started!");
                LOG.info(sb.toString());
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private static boolean isBundleResolved(final BundleContext context) {
        return Bundle.RESOLVED == context.getBundle().getState();
    }

    private static void logFailedStartup(final BundleActivator activator) {
        final StringBuilder sb = new StringBuilder(32);
        sb.append("Failed start of folder storage bundle \"");
        sb.append(activator.getClass().getName());
        sb.append("\"!");
        LOG.error(sb.toString(), new Throwable());
    }

    public void stop(final BundleContext context) throws Exception {
        try {
            // Drop activators
            if (null != activators) {
                for (final BundleActivator activator : activators) {
                    activator.stop(context);
                }
                activators.clear();
                activators = null;
            }
            // Drop service trackers
            if (null != serviceTrackers) {
                for (final ServiceTracker serviceTracker : serviceTrackers) {
                    serviceTracker.close();
                }
                serviceTrackers.clear();
                serviceTrackers = null;
            }
            // Unregister previously registered services
            if (null != serviceRegistrations) {
                for (final ServiceRegistration serviceRegistration : serviceRegistrations) {
                    serviceRegistration.unregister();
                }
                serviceRegistrations.clear();
                serviceRegistrations = null;
            }
            // Unregister previously registered component
            if (null != componentRegistration) {
                componentRegistration.unregister();
                componentRegistration = null;
            }

            if (LOG.isInfoEnabled()) {
                final StringBuilder sb = new StringBuilder(32);
                sb.append("Bundle \"");
                sb.append(FolderStorageActivator.class.getName());
                sb.append("\" successfully stopped!");
                LOG.info(sb.toString());
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}
