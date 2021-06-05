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

package com.openexchange.push.ms.osgi;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.folder.FolderService;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.management.osgi.HousekeepingManagementTracker;
import com.openexchange.ms.MsService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.ms.PushMsHandler;
import com.openexchange.push.ms.PushMsInit;
import com.openexchange.push.ms.Services;
import com.openexchange.push.ms.mbean.PushMsMBean;
import com.openexchange.push.ms.mbean.PushMsMBeanImpl;
import com.openexchange.timer.TimerService;

/**
 * {@link PushMsActivator} - OSGi bundle activator for message-based push bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushMsActivator extends HousekeepingActivator {

    /**
     * The {@link PushMsInit} reference
     */
    public static final AtomicReference<PushMsInit> INIT_REF = new AtomicReference<PushMsInit>();

    private boolean activated;

    /**
     * Initializes a new {@link PushMsActivator}.
     */
    public PushMsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { EventAdmin.class, EventFactoryService.class, ContextService.class, FolderService.class, MsService.class,
            ConfigurationService.class, CryptoService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMsActivator.class);

        {
            final ConfigurationService service = getService(ConfigurationService.class);
            if (service.getBoolProperty("com.openexchange.push.udp.pushEnabled", false)) {
                final String ls = Strings.getLineSeparator();
                final String message = "Start-up of bundle \"com.openexchange.push.ms\" denied, because UDP-based push is enabled." + ls + "Please disable it via \"com.openexchange.push.udp.pushEnabled\" option.";
                LOG.warn(message);
                // throw new BundleException(message, BundleException.ACTIVATOR_ERROR);
                return;
            }
        }
        // Proper start-up
        LOG.info("Starting bundle: com.openexchange.push.ms");
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            Services.setServiceLookup(this);
            /*
             * Start-up
             */
            final PushMsInit init = new PushMsInit();
            init.init();
            final String[] topics = new String[] { EventConstants.EVENT_TOPIC, "com/openexchange/*" };
            final Hashtable<String, Object> ht = new Hashtable<String, Object>(1);
            ht.put(EventConstants.EVENT_TOPIC, topics);
            registerService(EventHandler.class, new PushMsHandler(init.getDelayPushQueue()), ht);
            INIT_REF.set(init);
            /*
             * Service trackers
             */
            track(ManagementService.class, new HousekeepingManagementTracker(context, PushMsMBeanImpl.class.getName(), PushMsMBean.PUSH_MS_DOMAIN, new PushMsMBeanImpl()));
            track(TimerService.class);
            openTrackers();
            activated = true;
        } catch (Exception e) {
            LOG.error("Starting bundle com.openexchange.push.ms failed", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        if (!activated) {
            super.stopBundle();
            return;
        }
        final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMsActivator.class);
        LOG.info("Stopping bundle: com.openexchange.push.ms");
        try {
            final PushMsInit init = INIT_REF.get();
            if (null != init) {
                init.close();
                INIT_REF.set(null);
            }
            super.stopBundle();
            Services.setServiceLookup(null);
        } catch (Exception e) {
            LOG.error("Stopping bundle com.openexchange.push.ms failed", e);
            throw e;
        }
    }

}
