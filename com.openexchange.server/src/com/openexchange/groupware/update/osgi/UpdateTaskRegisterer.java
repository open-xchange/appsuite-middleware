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

package com.openexchange.groupware.update.osgi;

import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * Registers update tasks if the {@link DatabaseService} becomes available. This allows writing update tasks not coupled to the
 * {@link ServerServiceRegistry}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class UpdateTaskRegisterer implements ServiceTrackerCustomizer<DatabaseService, DatabaseService> {

    private final BundleContext context;
    private ServiceRegistration<UpdateTaskProviderService> registration;

    public UpdateTaskRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DatabaseService addingService(ServiceReference<DatabaseService> reference) {
        final DatabaseService service = context.getService(reference);
        registration = context.registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {
            @Override
            public Collection<? extends UpdateTaskV2> getUpdateTasks() {
                return createTasks(service);
            }
        }, null);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        registration.unregister();
        context.ungetService(reference);
    }

    /**
     * Overwrite this method and return your update tasks to register by instantiating them. Pass the {@link DatabaseService} to the
     * constructor if your update tasks.
     * @param service the database service discovered by this service tracker customizer.
     * @return update tasks to be registered.
     */
    protected abstract Collection<? extends UpdateTaskV2> createTasks(DatabaseService service);
}
