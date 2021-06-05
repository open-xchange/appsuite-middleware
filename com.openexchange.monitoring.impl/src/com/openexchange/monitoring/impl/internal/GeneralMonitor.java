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

package com.openexchange.monitoring.impl.internal;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessiondService;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GeneralMonitor implements GeneralMonitorMBean, MBeanRegistration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GeneralMonitor.class);

    private final ServiceLookup services;
    private volatile MBeanServer server;

    public GeneralMonitor(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public int getNumberOfAJAXConnections() {
        return MonitoringInfo.getNumberOfAJAXConnections();
    }

    @Override
    public int getNumberOfWebDAVUserConnections() {
        return MonitoringInfo.getNumberOfConnections(MonitoringInfo.WEBDAV_USER);
    }

    @Override
    public int getNumberOfOutlookConnections() {
        return MonitoringInfo.getNumberOfConnections(MonitoringInfo.OUTLOOK);
    }

    @Override
    public int getNumberOfSyncMLConnections() {
        return MonitoringInfo.getNumberOfConnections(MonitoringInfo.SYNCML);
    }

    @Override
    public int getNumberOfIMAPConnections() {
        return MonitoringInfo.getNumberOfConnections(MonitoringInfo.IMAP);
    }

    @Override
    public int getNumberOfIdleMailConnections() {
        return MonitoringInfo.getNumberOfConnections(MonitoringInfo.MAIL_IDLE);
    }

    @Override
    public int getNumberOfActiveSessions() {
        SessiondService sessiondService = services.getService(SessiondService.class);
        if (sessiondService != null) {
            return sessiondService.getNumberOfActiveSessions();
        }
        /*
         * No session can be active if service is missing
         */
        return 0;
    }

    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName nameArg) throws Exception {
        ObjectName name = nameArg;
        if (name == null) {
            name = new ObjectName(
                new StringBuilder(server.getDefaultDomain()).append(":name=").append(this.getClass().getName()).toString());
        }
        this.server = server;
        return name;
    }

    @Override
    public void postRegister(final Boolean registrationDone) {
        LOG.trace("postRegister() with {}", registrationDone);
    }

    @Override
    public void preDeregister() throws Exception {
        LOG.trace("preDeregister()");
    }

    @Override
    public void postDeregister() {
        LOG.trace("postDeregister()");
    }

    @Override
    public Integer getNbObjects() {
        MBeanServer server = this.server;
        if (null == server) {
            return Integer.valueOf(-1);
        }

        try {
            return Integer.valueOf((server.queryMBeans(new ObjectName("*:*"), null)).size());
        } catch (Exception e) {
            return Integer.valueOf(-1);
        }
    }

}
