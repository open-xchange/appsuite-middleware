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

package com.openexchange.logging.osgi;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.logging.mbean.LogbackConfiguration;
import com.openexchange.logging.mbean.LogbackConfigurationMBean;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Ioannis Chouklis</a>
 */
public class Activator extends HousekeepingActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ObjectName logbackConfObjName;

    private LogbackConfigurationMBean logbackConfMBean;

    @Override
    protected Class<?>[] getNeededServices() {
        // Nothing to do
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        new com.openexchange.logging.internal.LogbackCorrector().correct();

        registerLoggingConfigurationMBean();
    }

    /**
     * Register the LoggingConfigurationMBean
     */
    private void registerLoggingConfigurationMBean() {
        try {
            logbackConfObjName = new ObjectName(LogbackConfigurationMBean.DOMAIN, LogbackConfigurationMBean.KEY, LogbackConfigurationMBean.VALUE);
            logbackConfMBean = new LogbackConfiguration();
            track(ManagementService.class, new SimpleRegistryListener<ManagementService>() {

                @Override
                public void added(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.registerMBean(logbackConfObjName, logbackConfMBean);
                    } catch (OXException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                @Override
                public void removed(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.unregisterMBean(logbackConfObjName);
                    } catch (OXException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            });

        } catch (MalformedObjectNameException e) {
            logger.error(e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
        } catch (NotCompliantMBeanException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
