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

import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.monitoring.services.MonitoringServiceRegistry;
import com.openexchange.server.Initialization;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class MonitoringInit implements Initialization {

    private static final AtomicBoolean started = new AtomicBoolean();

    private static final MonitoringInit singleton = new MonitoringInit();

    private ObjectName objectName;

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MonitoringInit.class);

    /**
     * Prevent instantiation.
     */
    private MonitoringInit() {
        super();
    }

    private ObjectName getObjectName() throws MalformedObjectNameException, NullPointerException {
        if (null == objectName) {
            objectName = new ObjectName("com.openexchange.monitoring", "name", "GeneralMonitor");
        }
        return objectName;
    }

    /**
     * @return the singleton instance.
     */
    public static MonitoringInit getInstance() {
        return singleton;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws OXException {
        if (started.get()) {
            LOG.error("{} already started", MonitoringInit.class.getName());
            return;
        }
        /*
         * Create Beans and register them
         */
        final ManagementService managementAgent = MonitoringServiceRegistry.getServiceRegistry().getService(ManagementService.class);
        final GeneralMonitor generalMonitorBean = new GeneralMonitor();
        try {
            managementAgent.registerMBean(getObjectName(), generalMonitorBean);
        } catch (final MalformedObjectNameException exc) {
            LOG.error("", exc);
        } catch (final NullPointerException exc) {
            LOG.error("", exc);
        } catch (final Exception exc) {
            LOG.error("", exc);
        }
        LOG.info("JMX Monitor applied");

        started.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws OXException {
        if (!started.get()) {
            LOG.error("{} has not been started", MonitoringInit.class.getName());
            return;
        }
        final ManagementService managementAgent = MonitoringServiceRegistry.getServiceRegistry().getService(ManagementService.class);
        if (managementAgent != null) {
            try {
                managementAgent.unregisterMBean(getObjectName());
            } catch (final MalformedObjectNameException exc) {
                LOG.error("", exc);
            } catch (final NullPointerException exc) {
                LOG.error("", exc);
            } catch (final Exception exc) {
                LOG.error("", exc);
            }
            LOG.info("JMX Monitor removed");
        }
        started.set(false);
    }

    /**
     * @return <code>true</code> if monitoring has been started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started.get();
    }
}
