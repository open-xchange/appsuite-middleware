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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.rest.client.httpclient.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MonitoringRegistry}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class MonitoringRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringRegistry.class);

    private static final MonitoringRegistry INSTANCE = new MonitoringRegistry();

    private final ConcurrentMap<String, AtomicInteger> registeredClients;

    private MonitoringRegistry() {
        super();
        registeredClients = new ConcurrentHashMap<>();
    }

    public static MonitoringRegistry getInstance() {
        return INSTANCE;
    }

    public MonitoringId registerInstance(String clientName) {
        AtomicInteger existing = registeredClients.putIfAbsent(clientName, new AtomicInteger(1));
        if (existing == null) {
            log("HTTP client with name '{}' was instantiated for the first time.");
            return new MonitoringId(clientName, 1);
        }

        int instances = existing.incrementAndGet();
        log("HTTP client with name '{}' was instantiated {} times!"
            + "Connection pool monitoring will only reflect the most recent instance!", clientName, instances);
        return new MonitoringId(clientName, instances);
    }

    public void unregisterInstance(MonitoringId id) {
        AtomicInteger existing = registeredClients.get(id.getClientName());
        if (existing == null) {
            LOG.warn("HTTP client name was not registered for duplicate check!");
            return;
        }

        registeredClients.computeIfPresent(id.getClientName(), (k, v) -> v.decrementAndGet() == 0 ? null : v);
    }

    public boolean hasInstance(MonitoringId monitoringId) {
        AtomicInteger existing = registeredClients.get(monitoringId.getClientName());
        if (existing == null) {
            return false;
        }

        try {
            if (existing.get() >= monitoringId.getInstanceId()) {
                return true;
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        return false;
    }

    private static void log(String message, Object... args) {
        if (args == null) {
            args = new Object[0];
        }

        if (LOG.isDebugEnabled()) {
            // log stacktrace in debug mode to be able to trace initial instantiation
            Object[] largs = new Object[args.length + 1];
            System.arraycopy(args, 0, largs, 0, args.length);
            largs[largs.length - 1] = new Exception();
            LOG.info(message, largs);
        } else {
            LOG.info(message, args);
        }
    }

}
