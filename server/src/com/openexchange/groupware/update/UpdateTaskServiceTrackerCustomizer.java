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

package com.openexchange.groupware.update;

import java.util.Collection;
import java.util.Iterator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link UpdateTaskServiceTrackerCustomizer} - The {@link ServiceTrackerCustomizer service tracker customizer} for update tasks.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskServiceTrackerCustomizer implements ServiceTrackerCustomizer {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UpdateTaskServiceTrackerCustomizer.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link UpdateTaskServiceTrackerCustomizer}.
     * 
     * @param context The bundle context
     */
    public UpdateTaskServiceTrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    public Object addingService(final ServiceReference reference) {
        final Object addedService = context.getService(reference);
        final UpdateTaskRegistry registry = UpdateTaskRegistry.getInstance();
        if (null != registry) {
            // Get provider's collection
            final Collection<UpdateTask> collection = ((UpdateTaskProviderService) addedService).getUpdateTasks();
            boolean error = false;
            final int size = collection.size();
            final Iterator<UpdateTask> iter = collection.iterator();
            for (int i = 0; !error && i < size; i++) {
                final UpdateTask task = iter.next();
                if (!registry.addUpdateTask(task)) {
                    LOG.error(new StringBuilder().append("Update task \"").append(task.getClass().getName()).append(
                        "\" could not be registered."), new Throwable());
                    error = true;
                }
            }
            if (!error) {
                // Everything worked fine
                return addedService;
            }
            // Rollback
            for (final UpdateTask task : collection) {
                registry.removeUpdateTask(task);
            }
        }
        // Nothing to track, return null
        context.ungetService(reference);
        return null;
    }

    public void modifiedService(final ServiceReference reference, final Object service) {
        // Nothing to do
    }

    public void removedService(final ServiceReference reference, final Object service) {
        if (null != service) {
            try {
                final UpdateTaskRegistry registry = UpdateTaskRegistry.getInstance();
                if (null != registry) {
                    final UpdateTaskProviderService providerService = (UpdateTaskProviderService) service;
                    final Collection<UpdateTask> collection = providerService.getUpdateTasks();
                    for (final UpdateTask task : collection) {
                        registry.removeUpdateTask(task);
                    }
                }
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
