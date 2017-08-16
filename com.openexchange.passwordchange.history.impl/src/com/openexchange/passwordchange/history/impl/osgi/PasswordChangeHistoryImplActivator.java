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
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.impl.PasswordChangeRecorderRegistryServiceImpl;
import com.openexchange.passwordchange.history.impl.RdbPasswordChangeRecorder;
import com.openexchange.passwordchange.history.impl.events.PasswordChangeEventListener;
import com.openexchange.passwordchange.history.impl.events.PasswordChangeInterceptor;
import com.openexchange.passwordchange.history.impl.groupware.PasswordChangeHistoryCreateTableTask;
import com.openexchange.passwordchange.history.impl.groupware.PasswordHistoryDeleteListener;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;
import com.openexchange.user.UserServiceInterceptor;

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

        // Register UpdateTask
        registerService(CreateTableService.class, new PasswordChangeHistoryCreateTableTask());
        registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<? extends UpdateTaskV2> getUpdateTasks() {
                final List<UpdateTaskV2> tasks = new ArrayList<UpdateTaskV2>(1);
                tasks.add(new PasswordChangeHistoryCreateTableTask());
                return tasks;
            }
        });
    }
}
