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

package com.openexchange.groupware.contexts.impl;

import com.openexchange.context.ContextService;
import com.openexchange.context.internal.ContextServiceImpl;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * This class contains the initialization for contexts.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ContextInit implements Initialization {

    private static final ContextInit singleton = new ContextInit();

    /**
     * Prevent instantiation.
     */
    private ContextInit() {
        super();
    }

    /**
     * @return the singleton instance.
     */
    public static ContextInit getInstance() {
        return singleton;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws OXException {
        ContextStorage.start();
        ServerServiceRegistry.getInstance().addService(ContextService.class, new ContextServiceImpl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws OXException {
        ServerServiceRegistry.getInstance().removeService(ContextService.class);
        ContextStorage.stop();
    }
}
