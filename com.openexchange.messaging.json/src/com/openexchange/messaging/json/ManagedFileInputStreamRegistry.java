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

package com.openexchange.messaging.json;

import java.io.IOException;
import java.io.InputStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;

/**
 * {@link ManagedFileInputStreamRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedFileInputStreamRegistry implements MessagingInputStreamRegistry {

    private static volatile ManagedFileInputStreamRegistry instance = null;

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ManagedFileInputStreamRegistry getInstance() {
        ManagedFileInputStreamRegistry tmp = instance;
        if (null == tmp) {
            synchronized (ManagedFileInputStreamRegistry.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new ManagedFileInputStreamRegistry();
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        instance = null;
    }

    protected volatile ManagedFileManagement fileManagement;

    private volatile ServiceTracker<ManagedFileManagement, ManagedFileManagement> tracker;

    /**
     * Initializes a new {@link ManagedFileInputStreamRegistry}.
     */
    public ManagedFileInputStreamRegistry() {
        super();
    }

    /**
     * Starts this registry.
     *
     * @param context The bundle context used to track needed service
     */
    public void start(final BundleContext context) {
        if (null != tracker) {
            return;
        }
        tracker =
            new ServiceTracker<ManagedFileManagement, ManagedFileManagement>(
                context,
                ManagedFileManagement.class,
                new ServiceTrackerCustomizer<ManagedFileManagement, ManagedFileManagement>() {

                    @Override
                    public ManagedFileManagement addingService(final ServiceReference<ManagedFileManagement> reference) {
                        final ManagedFileManagement service = context.getService(reference);
                        fileManagement = service;
                        return service;
                    }

                    @Override
                    public void modifiedService(final ServiceReference<ManagedFileManagement> reference, final ManagedFileManagement service) {
                        // Mope
                    }

                    @Override
                    public void removedService(final ServiceReference<ManagedFileManagement> reference, final ManagedFileManagement service) {
                        fileManagement = null;
                        context.ungetService(reference);
                    }
                });
        tracker.open();
    }

    /**
     * Stops this registry orderly.
     */
    public void stop() {
        fileManagement = null;
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
    }

    @Override
    public Object getRegistryEntry(final Object id) throws OXException {
        return fileManagement.getByID(id.toString());
    }

    @Override
    public InputStream get(final Object id) throws OXException, IOException {
        final ManagedFile managedFile = fileManagement.getByID(id.toString());
        if (null == managedFile) {
            throw new IOException("No managed file associated with id: " + id);
        }
        return managedFile.getInputStream();
    }

}
