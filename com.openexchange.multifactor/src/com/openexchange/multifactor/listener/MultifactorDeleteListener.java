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

package com.openexchange.multifactor.listener;

import java.sql.Connection;
import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorRequest;

/**
 * {@link MultifactorDeleteListener} handles delete events for user and contexts
 * Deletes multifactor devices for removed users and contexts
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorDeleteListener implements DeleteListener {

    private final MultifactorProviderRegistry registry;

    public MultifactorDeleteListener(MultifactorProviderRegistry registry) {
        this.registry = registry;
    }

    /**
     * Perform deletion of multi-factor devices for a user after the user has been deleted
     *
     * @param contextId Id of the context
     * @param userId Id of the user
     * @throws OXException
     */
    private void deleteUser(int contextId, int userId) throws OXException {
        final MultifactorRequest multifactorRequest = new MultifactorRequest(contextId, userId, null, null, null);
        final Collection<MultifactorProvider> providers = registry.getProviders(multifactorRequest);
        for (final MultifactorProvider provider : providers) {
            provider.deleteRegistrations(contextId, userId);
        }
    }

    /**
     * Delete all multi-factor devices for a context after deletion.
     *
     * @param contextId  Id of the context
     * @throws OXException
     */
    private void deleteContext(int contextId) throws OXException {
        final MultifactorRequest multifactorRequest = new MultifactorRequest(contextId, 0, null, null, null);
        final Collection<MultifactorProvider> providers = registry.getProviders(multifactorRequest);
        for (final MultifactorProvider provider : providers) {
            provider.deleteRegistrations(contextId);
        }
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        try {
            if (event.getType() == DeleteEvent.TYPE_USER) {
                deleteUser(event.getContext().getContextId(), event.getId());
                return;
            }
            if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
                deleteContext(event.getContext().getContextId());
            }
        } catch (Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }
}
