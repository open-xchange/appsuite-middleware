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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.push.mq.osgi;

import static com.openexchange.push.mq.registry.PushMQServiceRegistry.getServiceRegistry;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.context.ContextService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.folder.FolderService;
import com.openexchange.management.ManagementService;
import com.openexchange.mq.MQService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.push.mq.PushMQHandler;
import com.openexchange.push.mq.PushMQInit;
import com.openexchange.timer.TimerService;

/**
 * {@link PushMQActivator} - OSGi bundle activator for push mq
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PushMQActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PushMQActivator.class));

    private PushMQInit init;

    /**
     * Initializes a new {@link PushMQActivator}.
     */
    public PushMQActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            EventAdmin.class, EventFactoryService.class, ContextService.class, FolderService.class, MQService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.push.mq");
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            final ServiceRegistry registry = getServiceRegistry();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (final Class<?> classe : classes) {
                final Object service = getService(classe);
                if (null != service) {
                    registry.addService(classe, service);
                }
            }
            /*
             * Start-up
             */
            init = new PushMQInit();
            init.init();
            final String[] topics = new String[] { EventConstants.EVENT_TOPIC, "com/openexchange/*" };
            final Hashtable<String, Object> ht = new Hashtable<String, Object>(1);
            ht.put(EventConstants.EVENT_TOPIC, topics);
            registerService(EventHandler.class, new PushMQHandler(init.getPublisher()), ht);
            /*
             * Service trackers
             */
            track(ManagementService.class, new ManagementRegisterer(context));
            track(EventFactoryService.class);
            track(TimerService.class);
            openTrackers();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.push.mq");
        try {
            init.close();
            ServiceRegistry registry = getServiceRegistry();
            if (registry != null) {
                registry.clearRegistry();
            }
            unregisterServices();
            closeTrackers();
            cleanUp();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}
