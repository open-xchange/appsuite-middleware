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

package com.openexchange.mail.transport;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailInitialization;
import com.openexchange.mail.transport.config.TransportPropertiesInit;
import com.openexchange.server.Initialization;

/**
 * {@link TransportInitialization} - Initializes whole transport implementation and therefore provides a central point for starting/stopping
 * transport implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportInitialization implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TransportInitialization.class);

    private static final TransportInitialization instance = new TransportInitialization();

    private final AtomicBoolean started;

    /**
     * @return The singleton instance of {@link TransportInitialization}
     */
    public static TransportInitialization getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link TransportInitialization}
     */
    private TransportInitialization() {
        super();
        started = new AtomicBoolean();
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.warn("Duplicate initialization of transport module aborted.");
            return;
        }
        /*
         * Start global transport system
         */
        TransportPropertiesInit.getInstance().start();
        /*
         * TODO: Remove Simulate bundle availability
         */
        // TransportProvider.initTransportProvider();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.warn("Duplicate shut-down of transport module aborted.");
            return;
        }
        /*
         * TODO: Remove Simulate bundle disappearance
         */
        // TransportProvider.resetTransportProvider();
        /*
         * Stop global transport system
         */
        TransportPropertiesInit.getInstance().stop();
    }

    public boolean isInitialized() {
        return started.get() && MailInitialization.getInstance().isInitialized();
    }
}
