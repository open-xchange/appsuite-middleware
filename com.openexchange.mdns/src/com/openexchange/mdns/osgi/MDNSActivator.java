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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mdns.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandProvider;
import com.openexchange.mdns.MDNSService;
import com.openexchange.mdns.MDNSServiceInfo;
import com.openexchange.mdns.internal.MDNSCommandProvider;
import com.openexchange.mdns.internal.MDNSServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MDNSActivator} - The mDNS activator.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MDNSActivator extends HousekeepingActivator {

    private MDNSServiceImpl service;

    private MDNSServiceInfo serviceInfo;

    /**
     * Initializes a new {@link MDNSActivator}.
     */
    public MDNSActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(MDNSActivator.class));
        log.info("Starting bundle: com.openexchange.mdns");
        try {
            /*
             * Create mDNS service
             */
            service = new MDNSServiceImpl();
            registerService(MDNSService.class, service, null);
            registerService(CommandProvider.class, new MDNSCommandProvider(service), null);

            serviceInfo = service.registerService("com.openexchange.mdns.lookup", 1808, "open-xchange lookup service @" + getHostName());
        } catch (final Exception e) {
            log.error("Starting bundle failed: com.openexchange.mdns", e);
            throw e;
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            return "<unknown>";
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(MDNSActivator.class));
        log.info("Stopping bundle: com.openexchange.mdns");
        try {
            unregisterServices();
            if (service != null) {
                if (null != serviceInfo) {
                    service.unregisterService(serviceInfo);
                    serviceInfo = null;
                }
                service.close();
                service = null;
            }
        } catch (final Exception e) {
            log.error("Stopping bundle failed: com.openexchange.mdns", e);
            throw e;
        }
    }

}
