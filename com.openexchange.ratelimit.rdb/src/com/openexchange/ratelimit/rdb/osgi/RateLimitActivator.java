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

package com.openexchange.ratelimit.rdb.osgi;

import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.ratelimit.RateLimiterFactory;
import com.openexchange.ratelimit.rdb.impl.RateLimiterFactoryImpl;
import com.openexchange.ratelimit.rdb.impl.groupware.RateLimitCreateTableService;
import com.openexchange.ratelimit.rdb.impl.groupware.RateLimitCreateTableUpdateTask;
import com.openexchange.ratelimit.rdb.impl.groupware.RateLimitDeleteListener;

/**
 * {@link RateLimitActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class RateLimitActivator extends HousekeepingActivator{

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {DBProvider.class, ContextService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        RateLimitCreateTableService rateLimitCreateTableService = new RateLimitCreateTableService();
        registerService(CreateTableService.class, rateLimitCreateTableService);
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new RateLimitCreateTableUpdateTask(rateLimitCreateTableService)));
        registerService(RateLimiterFactory.class, new RateLimiterFactoryImpl(this));
        registerService(DeleteListener.class, new RateLimitDeleteListener());
    }

}
