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

package com.openexchange.monitoring.internal;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.monitoring.services.MonitoringServiceRegistry;
import com.openexchange.sessiond.SessiondService;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GeneralMonitor implements GeneralMonitorMBean, MBeanRegistration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GeneralMonitor.class);

    private MBeanServer server;

    public GeneralMonitor() {
        super();
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
        final SessiondService sessiondService = MonitoringServiceRegistry.getServiceRegistry().getService(SessiondService.class);
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
        try {
            return Integer.valueOf((server.queryMBeans(new ObjectName("*:*"), null)).size());
        } catch (final Exception e) {
            return Integer.valueOf(-1);
        }
    }

}
