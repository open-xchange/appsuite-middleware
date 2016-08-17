/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.contactcollector.internal;

import java.util.List;
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
    public void memorizeAddresses(List<InternetAddress> addresses, boolean incrementUseCount, Session session) {
        memorizeAddresses(addresses, incrementUseCount, session, true);
    }

    public void memorizeAddresses(List<InternetAddress> addresses, boolean incrementUseCount, Session session, boolean background) {
        MemorizerTask memorizerTask = new MemorizerTask(addresses, incrementUseCount, session);
        if (!background) {
            // Run with current thread
            MemorizerWorker.handleTask(memorizerTask, services);
            return;
        }

        // Submit...
        MemorizerWorker worker = this.worker;
        if (null == worker) {
            // Worker not initialized. Run with current thread
            MemorizerWorker.handleTask(memorizerTask, services);
            return;
        }

        try {
            worker.submit(memorizerTask);
        } catch (Exception x) {
            // Thread pool service is absent. Run with current thread
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
