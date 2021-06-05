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

package com.openexchange.events.remote.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.events.remote.internal.EventBridge;
import com.openexchange.events.remote.internal.RemoteSession;
import com.openexchange.events.remote.internal.Services;
import com.openexchange.ms.MsService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link RemoteEventsActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RemoteEventsActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link RemoteEventsActivator}.
     */
    public RemoteEventsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CryptoService.class, ConfigurationService.class, EventAdmin.class, MsService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(RemoteEventsActivator.class);
        logger.info("starting bundle: \"com.openexchange.remote.events\"");
        Services.set(this);
        /*
         * setup remote session obfuscation
         */
        ConfigurationService configService = getService(ConfigurationService.class);
        RemoteSession.OBFUSCATION_KEY.set(configService.getProperty("com.openexchange.sessiond.encryptionKey",
            "auw948cz,spdfgibcsp9e8ri+<#qawcghgifzign7c6gnrns9oysoeivn"));
        /*
         * setup event bridge
         */
        EventBridge eventBridge = new EventBridge(getService(EventAdmin.class), getService(MsService.class));
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, EventBridge.TOPIC_PREFIX  + '*');
        registerService(EventHandler.class, eventBridge, serviceProperties);
    }

    @Override
    protected void stopBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(RemoteEventsActivator.class);
        logger.info("stopping bundle: \"com.openexchange.remote.events\"");
        RemoteSession.OBFUSCATION_KEY.set(null);
        Services.set(null);
        super.stopBundle();
    }

}
