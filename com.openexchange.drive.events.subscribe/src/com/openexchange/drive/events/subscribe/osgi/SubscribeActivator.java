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

package com.openexchange.drive.events.subscribe.osgi;

import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.internal.SubscribeServiceLookup;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsAddModeColumnTask;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsAddUuidColumnTask;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsCreateTableService;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsCreateTableTask;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsDeleteListener;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsMakeUuidPrimaryTask;
import com.openexchange.drive.events.subscribe.rdb.RdbSubscriptionStore;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link SubscribeActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SubscribeActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SubscribeActivator.class);

    /**
     * Initializes a new {@link SubscribeActivator}.
     */
    public SubscribeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: {}", context.getBundle().getSymbolicName());
        SubscribeServiceLookup.set(this);
        registerService(CreateTableService.class, new DriveEventSubscriptionsCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveEventSubscriptionsCreateTableTask()));
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveEventSubscriptionsAddUuidColumnTask()));
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveEventSubscriptionsMakeUuidPrimaryTask()));
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveEventSubscriptionsAddModeColumnTask()));
        registerService(DeleteListener.class, new DriveEventSubscriptionsDeleteListener());
        registerService(DriveSubscriptionStore.class, new RdbSubscriptionStore());
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        SubscribeServiceLookup.set(null);
        super.stopBundle();
    }

}
