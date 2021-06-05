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

package com.openexchange.ratelimit.rdb.impl;

import com.openexchange.context.ContextService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.ratelimit.RateLimiterFactory;
import com.openexchange.ratelimit.Rate;
import com.openexchange.ratelimit.RateLimiter;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RateLimiterFactoryImpl} is a {@link RateLimiterFactory} which creates {@link RateLimiter} which uses the database to provide rate limiting.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class RateLimiterFactoryImpl implements RateLimiterFactory{

    private final ServiceLookup services;

    /**
     * Initializes a new {@link RateLimiterFactoryImpl}.
     */
    public RateLimiterFactoryImpl(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public RateLimiter createLimiter(String id, Rate rate, int userId, int ctxId) throws OXException {
        ContextService contextService = services.getServiceSafe(ContextService.class);
        Context context = contextService.getContext(ctxId);
        DBProvider dbProvider = services.getServiceSafe(DBProvider.class);
        return new RateLimiterImpl(id, userId, context, rate.getAmount(), rate.getTimeframe(), dbProvider);
    }

}
