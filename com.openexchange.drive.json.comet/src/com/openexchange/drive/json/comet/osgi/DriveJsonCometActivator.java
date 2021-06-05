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

package com.openexchange.drive.json.comet.osgi;

import org.glassfish.grizzly.comet.CometContext;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.json.LongPollingListenerFactory;
import com.openexchange.drive.json.comet.internal.CometListenerFactory;
import com.openexchange.http.grizzly.service.comet.CometContextService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link DriveJsonCometActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveJsonCometActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveJsonCometActivator.class);
    private static final String TOPIC = "/drive/listen";

    /**
     * Initializes a new {@link DriveJsonCometActivator}.
     */
    public DriveJsonCometActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, CometContextService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.drive.json.comet");
        long expirationDelay = getService(ConfigurationService.class).getIntProperty("com.openexchange.drive.listenTimeout", 90 * 1000);
        CometContext<DriveEvent> cometContext = getService(CometContextService.class).register(TOPIC);
        cometContext.setExpirationDelay(expirationDelay);
        registerService(LongPollingListenerFactory.class, new CometListenerFactory(cometContext));
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.drive.json.comet");
        try {
            CometContextService cometContextService = getService(CometContextService.class);
            if (null != cometContextService) {
                cometContextService.deregister(TOPIC);
            }
        } catch (RuntimeException e) {
            LOG.warn("Error unregistering comet context", e);
        }
        super.stopBundle();
    }

}
