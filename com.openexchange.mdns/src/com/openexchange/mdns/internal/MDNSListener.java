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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mdns.internal;

import static com.openexchange.java.util.UUIDs.fromUnformattedString;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.mdns.MDNSServiceEntry;

/**
 * {@link MDNSListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MDNSListener implements javax.jmdns.ServiceListener {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MDNSListener.class));

    private static final boolean INFO = LOG.isInfoEnabled();

    private final ConcurrentMap<String, ConcurrentMap<UUID, MDNSServiceEntry>> map;

    private final MDNSReregisterer mdnsReregisterer;

    /**
     * Initializes a new {@link MDNSListener}.
     */
    public MDNSListener(final ConcurrentMap<String, ConcurrentMap<UUID, MDNSServiceEntry>> map, final MDNSReregisterer mdnsReregisterer) {
        super();
        this.map = map;
        this.mdnsReregisterer = mdnsReregisterer;
    }

    @Override
    public void serviceAdded(final javax.jmdns.ServiceEvent event) {
        try {
            event.getDNS().requestServiceInfo(event.getType(), event.getName());
        } catch (final Exception e) {
            LOG.error(new StringBuilder(64).append("Resolving added service \"").append(event.getName()).append("\" failed: ").append(
                e.getMessage()).toString(), e);
        }
    }

    @Override
    public void serviceRemoved(final javax.jmdns.ServiceEvent event) {
        final String n = event.getName();
        final int pos = n.indexOf('/');
        if (pos <= 0) {
            LOG.error("Illegal service name: " + n);
            return;
        }
        final UUID id = fromUnformattedString(n.substring(0, pos));
        final String serviceId = n.substring(pos + 1);
        final ConcurrentMap<UUID, MDNSServiceEntry> inner = map.get(serviceId);
        if (null == inner) {
            return;
        }
        final MDNSServiceEntry entry = inner.remove(id);
        if (null != entry) {
            mdnsReregisterer.serviceRemoved(serviceId, entry);
        }
        if (inner.isEmpty()) {
            map.remove(serviceId);
        }
        if (INFO) {
            LOG.info(new StringBuilder(64).append("Removed tracked service: ").append(n).toString());
        }
    }

    @Override
    public void serviceResolved(final javax.jmdns.ServiceEvent event) {
        final javax.jmdns.ServiceInfo info = event.getInfo();
        final UUID id;
        final String serviceId;
        {
            final String n = event.getName();
            final int pos = n.indexOf('/');
            if (pos <= 0) {
                LOG.error("Illegal service name: " + n);
                return;
            }
            id = fromUnformattedString(n.substring(0, pos));
            serviceId = n.substring(pos + 1);
            if (mdnsReregisterer.contains(id, serviceId)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(64).append("Ignoring self-added service: ").append(info).toString());
                }
                return;
            }
        }
        /*
         * Add newly detected service
         */
        final MDNSServiceEntryImpl entry =
            new MDNSServiceEntryImpl(new InetAddress[] { info.getInetAddress() } /*info.getInetAddresses()*/, info.getPort(), id, serviceId, info.toString(), Constants.SERVICE_TYPE);
        ConcurrentMap<UUID, MDNSServiceEntry> inner = map.get(serviceId);
        if (null == inner) {
            final ConcurrentMap<UUID, MDNSServiceEntry> newInner = new ConcurrentHashMap<UUID, MDNSServiceEntry>();
            inner = map.putIfAbsent(serviceId, newInner);
            if (null == inner) {
                inner = newInner;
            }
        }
        MDNSServiceEntry prev = inner.putIfAbsent(id, entry);
        if (null != prev) {
            /*
             * Already a service bound to id. Check equality.
             */
            if (prev.equals(entry)) {
                /*
                 * Equal service; ignore it
                 */
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder("Duplicate service: ").append(entry).toString());
                }
            } else {
                /*
                 * A service update
                 */
                synchronized (inner) {
                    prev = inner.get(id);
                    if (!prev.equals(entry)) {
                        inner.put(id, entry);
                        mdnsReregisterer.serviceAdded(serviceId, entry);
                        if (INFO) {
                            LOG.info(new StringBuilder(64).append("Updated new service: ").append(entry).toString());
                        }
                    } else if (INFO) {
                        LOG.info(new StringBuilder("Duplicate service discovered: ").append(entry).toString());
                    }
                }
            }
        } else {
            if (INFO) {
                LOG.info(new StringBuilder(64).append("Detected new service: ").append(entry).toString());
            }
            /*
             * Added a newly discovered service. Re-register own services to re-publish them.
             */
            mdnsReregisterer.reregisterServices();
            mdnsReregisterer.serviceAdded(serviceId, entry);
        }
    }
}
