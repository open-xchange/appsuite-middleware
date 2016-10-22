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

package com.openexchange.socketio.osgi;

import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
import com.openexchange.socketio.monitoring.SocketIOMBean;
import com.openexchange.socketio.monitoring.impl.SocketIOMBeanImpl;
import com.openexchange.socketio.server.SocketIOManager;
import com.openexchange.socketio.websocket.WsTransportConnectionRegistry;

/**
 * {@link ManagementTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ManagementTracker implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private final BundleContext context;
    private final SocketIOManager socketIoManager;
    private final WsTransportConnectionRegistry connectionRegistry;

    /**
     * Initializes a new {@link ManagementTracker}.
     */
    public ManagementTracker(SocketIOManager socketIoManager, WsTransportConnectionRegistry connectionRegistry, BundleContext context) {
        super();
        this.socketIoManager = socketIoManager;
        this.connectionRegistry = connectionRegistry;
        this.context = context;

    }

    @Override
    public ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);

        Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementTracker.class);
        try {
            ObjectName objectName = Managements.getObjectName(SocketIOMBean.class.getName(), SocketIOMBean.DOMAIN);
            managementService.registerMBean(objectName, new SocketIOMBeanImpl(socketIoManager, connectionRegistry));
            logger.info("Registered MBean {}", SocketIOMBean.class.getName());
            return managementService;
        } catch (Exception e) {
            logger.warn("Could not register MBean {}", SocketIOMBean.class.getName(), e);
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService managementService) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<ManagementService> reference, ManagementService managementService) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementTracker.class);

        if (null != managementService) {
            try {
                managementService.unregisterMBean(Managements.getObjectName(SocketIOMBean.class.getName(), SocketIOMBean.DOMAIN));
                logger.warn("Unregistered MBean {}", SocketIOMBean.class.getName());
            } catch (Exception e) {
                logger.warn("Could not un-register MBean {}", SocketIOMBean.class.getName(), e);
            }

            context.ungetService(reference);
        }
    }

}
