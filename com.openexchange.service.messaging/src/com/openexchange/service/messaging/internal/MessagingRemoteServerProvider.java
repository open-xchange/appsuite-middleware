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

package com.openexchange.service.messaging.internal;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.mdns.MDNSService;
import com.openexchange.mdns.MDNSServiceEntry;
import com.openexchange.mdns.MDNSServiceInfo;

/**
 * {@link MessagingRemoteServerProvider} - Provides the addresses to remote messaging servers either dynamically looked-up or statically
 * configured.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingRemoteServerProvider extends ServiceTracker<MDNSService, MDNSService> {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingRemoteServerProvider.class));

    private static volatile MessagingRemoteServerProvider instance;

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MessagingRemoteServerProvider getInstance() {
        return instance;
    }

    /**
     * Initializes the instance.
     *
     * @param context The bundle context
     */
    public static void initInstance(final BundleContext context) {
        if (null == instance) {
            synchronized (MessagingConfig.class) {
                if (null == instance) {
                    instance = new MessagingRemoteServerProvider(context);
                }
            }
        }
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        if (null != instance) {
            synchronized (MessagingConfig.class) {
                if (null != instance) {
                    instance = null;
                }
            }
        }
    }

    /*-
     * --------------------- Member section ------------------------
     */

    private final BundleContext context;

    private final AtomicReference<MDNSService> mdnsServiceRef;

    private MDNSServiceInfo serviceInfo;

    private MessagingRemoteServerProvider(final BundleContext context) {
        super(context, MDNSService.class.getName(), null);
        this.context = context;
        mdnsServiceRef = new AtomicReference<MDNSService>();
    }

    @Override
    public MDNSService addingService(final ServiceReference<MDNSService> reference) {
        final MDNSService service = context.getService(reference);
        if (mdnsServiceRef.compareAndSet(null, service)) {
            try {
                serviceInfo =
                    service.registerService(Constants.MDNS_SERVICE_ID, MessagingConfig.getInstance().getListenerPort(), new StringBuilder(
                        "open-xchange messaging service @").append(getHostName()).toString());
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        context.ungetService(reference);
        return null;
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            return "<unknown>";
        }
    }

    @Override
    public void removedService(final ServiceReference<MDNSService> reference, final MDNSService service) {
        if (null != service) {
            final MDNSService mdnsService = service;
            if (mdnsServiceRef.compareAndSet(mdnsService, null)) {
                try {
                    mdnsService.unregisterService(serviceInfo);
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
                serviceInfo = null;
            }
            context.ungetService(reference);
        }
    }

    @Override
    public void modifiedService(final ServiceReference<MDNSService> reference, final MDNSService service) {
        // Nope
    }

    /**
     * Gets remote messaging servers.
     *
     * @return
     * @throws OXException
     */
    public List<InetSocketAddress> getRemoteMessagingServers() throws OXException {
        final MessagingConfig config = MessagingConfig.getInstance();
        if (config.isMdnsEnabled()) {
            final MDNSService mdnsService = mdnsServiceRef.get();
            if (null != mdnsService) {
                try {
                    final List<MDNSServiceEntry> entries = mdnsService.listByService(Constants.MDNS_SERVICE_ID);
                    if (entries.isEmpty()) {
                        return Collections.<InetSocketAddress> emptyList();
                    }
                    final List<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>(entries.size());
                    for (final MDNSServiceEntry entry : entries) {
                        for (final InetAddress inetAddress : entry.getAddresses()) {
                            addrs.add(new InetSocketAddress(inetAddress, entry.getPort()));
                        }
                    }
                    return addrs;
                } catch (final OXException e) {
                    throw new OXException(e);
                }
            }
        }
        /*
         * Either mDNS is disabled or service not available. Then return the statically configured remote messaging servers.
         */
        return config.getRemoteMessagingServers();
    }

}
