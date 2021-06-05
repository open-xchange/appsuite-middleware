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

package com.openexchange.groupware.impl.id;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link IDGeneratorInit}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IDGeneratorInit implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IDGeneratorInit.class);

    private final AtomicBoolean started;

    /**
     * Initializes a new {@link IDGeneratorInit}.
     */
    public IDGeneratorInit() {
        super();
        started = new AtomicBoolean();
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        // Simulate bundle start
        ServerServiceRegistry.getInstance().addService(IDGeneratorService.class, new IDGeneratorServiceImpl());
        LOG.info("IDGeneratorService successfully injected to server service registry");
    }

    @Override
    public void stop() throws OXException {
        if (!started.compareAndSet(true, false)) {
            return;
        }
        // Simulate bundle stop
        ServerServiceRegistry.getInstance().removeService(IDGeneratorService.class);
    }

}
