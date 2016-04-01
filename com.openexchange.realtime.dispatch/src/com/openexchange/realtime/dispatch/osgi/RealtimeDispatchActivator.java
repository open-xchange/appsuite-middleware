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

package com.openexchange.realtime.dispatch.osgi;

import java.util.Collection;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.impl.LocalMessageDispatcherImpl;
import com.openexchange.realtime.dispatch.management.ManagementHouseKeeper;

public class RealtimeDispatchActivator extends HousekeepingActivator {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RealtimeDispatchActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ManagementService.class, EventAdmin.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    /*
     * Register the MessageDispatcher as Service and listen for new Channels being added to the OSGi service registry. When new Channels are
     * added/removed to/from the service registry inform the MessageDispatcher about it.
     */
    @Override
    protected void startBundle() throws Exception {
        RealtimeServiceRegistry.SERVICES.set(this);
        ManagementHouseKeeper managementHouseKeeper = ManagementHouseKeeper.getInstance();
        managementHouseKeeper.initialize(this);

        final LocalMessageDispatcher dispatcher = new LocalMessageDispatcherImpl();
        registerService(LocalMessageDispatcher.class, dispatcher);
        Collection<RealtimeJanitor> realtimeJanitors = RealtimeJanitors.getInstance().getJanitors();
        for (RealtimeJanitor realtimeJanitor : realtimeJanitors) {
            registerService(RealtimeJanitor.class, realtimeJanitor, realtimeJanitor.getServiceProperties());
        }

        track(Channel.class, new SimpleRegistryListener<Channel>() {

            @Override
            public void added(final ServiceReference<Channel> ref, final Channel service) {
                dispatcher.addChannel(service);
            }

            @Override
            public void removed(final ServiceReference<Channel> ref, final Channel service) {
                dispatcher.removeChannel(service);
            }
        });

        try {
            managementHouseKeeper.exposeManagementObjects();
        } catch (OXException oxe) {
            LOG.error("Failed to expose ManagementObjects", oxe);
        }
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        RealtimeJanitors.getInstance().cleanup();
        ManagementHouseKeeper.getInstance().cleanup();
        RealtimeServiceRegistry.SERVICES.set(null);
        super.stopBundle();
    }

}
