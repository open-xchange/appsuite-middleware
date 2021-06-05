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

package com.openexchange.push.udp;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.push.udp.registry.PushServiceRegistry;

/**
 * Initializes the event system.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushInit {

    /**
     * Singleton.
     */
    private static final PushInit SINGLETON = new PushInit();

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushInit.class);

    private PushMulticastSocket multicast;

    private PushOutputQueue output;

    private PushChannels channels;

    private PushConfiguration config;


    public PushConfiguration getConfig() {
        return config;
    }

    private final AtomicBoolean started = new AtomicBoolean();

    /**
     * Prevent instantiation.
     */
    private PushInit() {
        super();
    }

    /**
     * @return the singleton instance.
     */
    public static PushInit getInstance() {
        return SINGLETON;
    }

    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("Duplicate push initialization.");
            return;
        }

        final ConfigurationService conf = PushServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        if (conf != null) {
            config = new PushConfigurationImpl(conf);
        }

        LOG.info("Starting Push UDP");

        if (config == null) {
            throw PushUDPExceptionCode.MISSING_CONFIG.create();
        }
        channels = new PushChannels(config);
        output = new PushOutputQueue(config, channels);

        multicast = new PushMulticastSocket(config);
    }

    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error("Duplicate push component shutdown.");
            return;
        }
        if (null != multicast) {
            multicast.close();
            multicast = null;
        }
        if (null != output) {
            output.close();
            output = null;
        }
        if (null != channels) {
            channels.shutdown();
            channels = null;
        }
        config = null;
    }
}
