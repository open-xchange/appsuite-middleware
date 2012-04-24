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

package com.openexchange.mq.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mq.MQConstants;
import com.openexchange.mq.MQServerStartup;
import com.openexchange.mq.MQService;
import com.openexchange.mq.hornetq.HornetQServerStartup;
import com.openexchange.mq.serviceLookup.MQServiceLookup;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MQActivator} - The {@link BundleActivator activator} for Message Queue bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MQActivator extends HousekeepingActivator {

    private volatile MQServerStartup serverStartup;

    /**
     * Initializes a new {@link MQActivator}.
     */
    public MQActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(MQActivator.class));
        log.info("Starting bundle: " + MQConstants.BUNDLE_SYMBOLIC_NAME);
        try {
            MQServiceLookup.setServiceLookup(this);
            // Start MQ server
            final MQServerStartup serverStartup = new HornetQServerStartup();
            serverStartup.start();
            this.serverStartup = serverStartup;
            // Register service(s)
            final MQService service = serverStartup.getService();
            addService(MQService.class, service);
            MQServiceLookup.setMQService(service);
            MQService.SERVICE_REFERENCE.set(service);
            registerService(MQService.class, service);

            // --------- Test service ----------
            // new com.openexchange.mq.example.MQJmsQueueExample(service).test();
            // new com.openexchange.mq.example.MQJmsQueueExample2(service).test();
            // new com.openexchange.mq.example.MQJmsTopicExample(service).test();
            // new com.openexchange.mq.example.MQJmsPriorizedQueueExample(service).test();
        } catch (final Exception e) {
            log.error("Error starting bundle: " + MQConstants.BUNDLE_SYMBOLIC_NAME, e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(MQActivator.class));
        log.info("Stopping bundle: " + MQConstants.BUNDLE_SYMBOLIC_NAME);
        try {
            final MQServerStartup serverStartup = this.serverStartup;
            if (null != serverStartup) {
                serverStartup.stop();
                this.serverStartup = null;
            }
            removeService(MQService.class);
            MQServiceLookup.setServiceLookup(null);
            MQServiceLookup.setMQService(null);
            MQService.SERVICE_REFERENCE.set(null);
            super.stopBundle();
        } catch (final Exception e) {
            log.error("Error stopping bundle: " + MQConstants.BUNDLE_SYMBOLIC_NAME, e);
            throw e;
        }
    }

}
