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
import com.openexchange.management.ManagementService;
import com.openexchange.ms.MsService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.ms.PushMsHandler;
import com.openexchange.push.ms.PushMsInit;
import com.openexchange.push.ms.Services;
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

    private volatile boolean activated;

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
    protected void startBundle() throws Exception {
        final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMsActivator.class);

        {
            final ConfigurationService service = getService(ConfigurationService.class);
            if (service.getBoolProperty("com.openexchange.push.udp.pushEnabled", false)) {
                final String ls = System.getProperty("line.separator");
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
            track(ManagementService.class, new ManagementRegisterer(context));
            track(TimerService.class);
            openTrackers();
            activated = true;
        } catch (final Exception e) {
            LOG.error("Starting bundle com.openexchange.push.ms failed", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
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
        } catch (final Exception e) {
            LOG.error("Stopping bundle com.openexchange.push.ms failed", e);
            throw e;
        }
    }

}
