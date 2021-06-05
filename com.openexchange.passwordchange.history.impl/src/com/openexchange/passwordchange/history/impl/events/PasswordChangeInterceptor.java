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

package com.openexchange.passwordchange.history.impl.events;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.passwordchange.history.PasswordChangeClients;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.User;
import com.openexchange.user.interceptor.AbstractUserServiceInterceptor;

/**
 * {@link PasswordChangeInterceptor} - Provisioning based password changes will call this interceptor
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeInterceptor extends AbstractUserServiceInterceptor {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeInterceptor.class);

    private final ServiceLookup services;
    final PasswordChangeRecorderRegistryService registry;

    /**
     * Initializes a new {@link PasswordChangeInterceptor}.
     *
     * @param registry The {@link PasswordChangeRecorderRegistryService}
     * @param services The {@link ServiceLookup} to get services from
     */
    public PasswordChangeInterceptor(PasswordChangeRecorderRegistryService registry, ServiceLookup services) {
        super();
        this.registry = registry;
        this.services = services;
    }

    @Override
    public int getRanking() {
        return 10;
    }

    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        // Check if password was changed
        if (null != context && null != user && null != user.getUserPassword()) {
            final int contextId = context.getContextId();
            final int userId = user.getId();

            // so password was changed..
            ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
            if (null == threadPool) {
                PasswordChangeHelper.recordChangeSafe(contextId, userId, null, PasswordChangeClients.PROVISIONING.getIdentifier(), registry);
            } else {                
                threadPool.submit(new AbstractTask<Void>() {
                    @Override
                    public Void call() {
                        PasswordChangeHelper.recordChangeSafe(contextId, userId, null, PasswordChangeClients.PROVISIONING.getIdentifier(), registry);
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public void afterDelete(Context context, User user, Contact contactData) throws OXException {
        final int contextId = context.getContextId();
        final int userId = user.getId();

        // Clear DB after deletion of user
        ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
        if (null == threadPool) {
            PasswordChangeHelper.clearSafeFor(contextId, userId, 0, registry);
        } else {            
            threadPool.submit(new AbstractTask<Void>() {
                @Override
                public Void call() {
                    PasswordChangeHelper.clearSafeFor(contextId, userId, 0, registry);
                    return null;
                }
            });
        }
    }

}
