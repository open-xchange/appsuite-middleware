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

package com.openexchange.contactcollector.internal;

import java.util.Collection;
import javax.mail.internet.InternetAddress;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link ContactCollectorServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactCollectorServiceImpl implements ContactCollectorService {

    /** This service's ranking */
    public static final Integer RANKING = Integer.valueOf(0);

    private final ServiceLookup services;
    private volatile MemorizerWorker worker;

    /**
     * Initializes a new {@link ContactCollectorServiceImpl}.
     */
    public ContactCollectorServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void memorizeAddresses(Collection<InternetAddress> addresses, boolean incrementUseCount, Session session) {
        memorizeAddresses(addresses, incrementUseCount, session, true);
    }

    public void memorizeAddresses(Collection<InternetAddress> addresses, boolean incrementUseCount, Session session, boolean background) {
        MemorizerTask memorizerTask = new MemorizerTask(addresses, incrementUseCount, session);
        if (!background) {
            // Run with current thread
            ContactCleanUp.performContactCleanUp(session, services);
            MemorizerWorker.handleTask(memorizerTask, services);
            return;
        }

        // Submit...
        MemorizerWorker worker = this.worker;
        if (null == worker) {
            // Worker not initialized. Run with current thread
            ContactCleanUp.performContactCleanUp(session, services);
            MemorizerWorker.handleTask(memorizerTask, services);
            return;
        }

        try {
            worker.submit(memorizerTask);
        } catch (Exception x) {
            // Thread pool service is absent. Run with current thread
            ContactCleanUp.performContactCleanUp(session, services);
            MemorizerWorker.handleTask(memorizerTask, services);
        }
    }

    /**
     * Starts this contact collector service implementation.
     *
     * @throws OXException If a needed service is missing
     */
    public void start() throws OXException {
        AliasesProvider.getInstance().start();
        worker = new MemorizerWorker(services);
    }

    /**
     * Stops this contact collector service implementation.
     */
    public void stop() {
        MemorizerWorker worker = this.worker;
        if (null != worker) {
            this.worker = null;
            worker.close();
        }
        AliasesProvider.getInstance().stop();
    }
}
