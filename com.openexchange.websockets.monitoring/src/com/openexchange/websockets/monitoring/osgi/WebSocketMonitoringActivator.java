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

package com.openexchange.websockets.monitoring.osgi;

import javax.management.ObjectName;
import org.slf4j.Logger;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.websockets.WebSocketService;
import com.openexchange.websockets.monitoring.WebSocketMBean;
import com.openexchange.websockets.monitoring.impl.WebSocketMBeanImpl;


/**
 * {@link WebSocketMonitoringActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketMonitoringActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link WebSocketMonitoringActivator}.
     */
    public WebSocketMonitoringActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ManagementService.class, WebSocketService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMonitoringActivator.class);

        ManagementService managementService = getService(ManagementService.class);
        try {
            ObjectName objectName = Managements.getObjectName(WebSocketMBean.class.getName(), WebSocketMBean.DOMAIN);
            managementService.registerMBean(objectName, new WebSocketMBeanImpl(getService(WebSocketService.class)));
            logger.warn("Registered MBean {}", WebSocketMBean.class.getName());
        } catch (Exception e) {
            logger.warn("Could not register MBean {}", WebSocketMBean.class.getName(), e);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMonitoringActivator.class);

        ManagementService managementService = getService(ManagementService.class);
        if (null != managementService) {
            try {
                managementService.unregisterMBean(Managements.getObjectName(WebSocketMBean.class.getName(), WebSocketMBean.DOMAIN));
                logger.warn("Unregistered MBean {}", WebSocketMBean.class.getName());
            } catch (Exception e) {
                logger.warn("Could not un-register MBean {}", WebSocketMBean.class.getName(), e);
            }
        }

        super.stopBundle();
    }

}
