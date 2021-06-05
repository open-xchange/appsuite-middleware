/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.passwordchange.history.impl.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.impl.PasswordChangeRecorderRegistryServiceImpl;
import com.openexchange.passwordchange.history.impl.RdbPasswordChangeRecorder;
import com.openexchange.passwordchange.history.impl.events.PasswordChangeEventListener;
import com.openexchange.passwordchange.history.impl.events.PasswordChangeInterceptor;
import com.openexchange.passwordchange.history.impl.groupware.PasswordChangeHistoryConvertToUtf8mb4;
import com.openexchange.passwordchange.history.impl.groupware.PasswordChangeHistoryCreateTableTask;
import com.openexchange.passwordchange.history.impl.groupware.PasswordHistoryDeleteListener;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;
import com.openexchange.user.interceptor.UserServiceInterceptor;

/**
 *
 * {@link PasswordChangeHistoryImplActivator} - Activator for PasswordChangeHistory bundle
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class PasswordChangeHistoryImplActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHistoryImplActivator.class);

    /**
     * Initializes a new {@link PasswordChangeHistoryImplActivator}
     */
    public PasswordChangeHistoryImplActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, UserService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting PasswordChangeHistory bundle");

        // Register a password change registry
        PasswordChangeRecorderRegistryServiceImpl registry = new PasswordChangeRecorderRegistryServiceImpl(context, getService(ConfigViewFactory.class), getService(UserService.class));
        PasswordChangeRecorder defaultRecorder = new RdbPasswordChangeRecorder(this);
        registry.register(defaultRecorder);
        track(PasswordChangeRecorder.class, registry);
        trackService(ThreadPoolService.class);
        trackService(DatabaseService.class);
        openTrackers();
        registerService(PasswordChangeRecorderRegistryService.class, registry);

        // Register event for password change
        PasswordChangeEventListener eventHandler = new PasswordChangeEventListener(registry);
        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(EventConstants.EVENT_TOPIC, eventHandler.getTopic());
        registerService(EventHandler.class, eventHandler, properties);

        // Register interceptor
        registerService(UserServiceInterceptor.class, new PasswordChangeInterceptor(registry, this));

        // Register DeleteListener to handle user/context deletions
        registerService(DeleteListener.class, new PasswordHistoryDeleteListener(registry, getService(UserService.class)));

        // Register UpdateTask and CreateTableService
        final PasswordChangeHistoryCreateTableTask createTableTask = new PasswordChangeHistoryCreateTableTask();
        registerService(CreateTableService.class, createTableTask);
        registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<? extends UpdateTaskV2> getUpdateTasks() {
                final List<UpdateTaskV2> tasks = new ArrayList<UpdateTaskV2>(1);
                tasks.add(createTableTask);
                tasks.add(new PasswordChangeHistoryConvertToUtf8mb4());
                return tasks;
            }
        });
    }
}
