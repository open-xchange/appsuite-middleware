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

package com.openexchange.config.cascade.context;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractContextBasedConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractContextBasedConfigProvider implements ConfigProviderService {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractContextBasedConfigProvider}.
     * @param contexts
     */
    protected AbstractContextBasedConfigProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public BasicProperty get(String propertyName, int contextId, int userId) throws OXException {
        if (contextId == NO_CONTEXT) {
            return NO_PROPERTY;
        }

        ContextService contextService = services.getService(ContextService.class);
        if (contextService == null) {
            return NO_PROPERTY;
        }

        try {
            return get(propertyName, contextService.getContext(contextId, UpdateBehavior.DENY_UPDATE), userId);
        } catch (OXException e) {
            if (false == e.equalsCode(2, "CTX")) {
                throw e;
            }

            // "CTX-0002" --> No such context
            return NO_PROPERTY;
        }
    }

    @Override
    public Collection<String> getAllPropertyNames(int contextId, int userId) throws OXException {
        if (contextId == NO_CONTEXT) {
            return Collections.emptyList();
        }

        ContextService contextService = services.getService(ContextService.class);
        if (contextService == null) {
            return Collections.emptyList();
        }

        try {
            return getAllPropertyNamesFor(contextService.getContext(contextId, UpdateBehavior.DENY_UPDATE), userId);
        } catch (OXException e) {
            if (false == e.equalsCode(2, "CTX")) {
                throw e;
            }

            // "CTX-0002" --> No such context
            return Collections.emptyList();
        }
    }

    /**
     * Gets all available property names
     *
     * @param context The associated context
     * @param optUser The optional user or <code>null</code> (if not specified)
     * @return All available property names
     * @throws OXException If property names cannot be returned
     */
    protected abstract Collection<String> getAllPropertyNamesFor(Context context, int userId) throws OXException;

    /**
     * Gets the denoted property
     *
     * @param propertyName The property name
     * @param context The associated context
     * @param user The identifier for the associated user
     * @return The property
     * @throws OXException If property cannot be returned
     */
    protected abstract BasicProperty get(String propertyName, Context context, int user) throws OXException;

}
