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

package org.quartz.service.osgi;

import org.osgi.framework.ServiceRegistration;
import org.quartz.service.QuartzService;
import org.quartz.service.internal.QuartzServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link QuartzActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class QuartzActivator extends HousekeepingActivator {

    private ServiceRegistration<QuartzService> quartzServiceRegistration;
    private QuartzServiceImpl quartzServiceImpl;

    /**
     * Initializes a new {@link QuartzActivator}.
     */
    public QuartzActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuartzActivator.class);
        log.info("Starting bundle: org.quartz");
        try {
            System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
            quartzServiceImpl = new QuartzServiceImpl(getService(ConfigurationService.class));
            quartzServiceRegistration = context.registerService(QuartzService.class, quartzServiceImpl, null);
            log.info("Bundle successfully started: org.quartz");
        } catch (Exception e) {
            log.error("Failed starting bundle: org.quartz", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuartzActivator.class);
        log.info("Stopping bundle: org.quartz");
        try {
            final ServiceRegistration<QuartzService> quartzServiceRegistration = this.quartzServiceRegistration;
            if (null != quartzServiceRegistration) {
                quartzServiceRegistration.unregister();
                this.quartzServiceRegistration = null;
            }

            if (quartzServiceImpl != null) {
                quartzServiceImpl.shutdown();
            }
            log.info("Bundle successfully stopped: org.quartz");
        } catch (Exception e) {
            log.error("Failed stopping bundle: org.quartz", e);
            throw e;
        }
    }

}
