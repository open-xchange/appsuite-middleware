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

package com.openexchange.sessiond.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigRegistry;
import com.openexchange.sessiond.osgi.Services;

/**
 * {@link SessiondInit} - Initializes sessiond service
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessiondInit implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessiondInit.class);

    private static final SessiondInit singleton = new SessiondInit();

    public static SessiondInit getInstance() {
        return singleton;
    }

    // ---------------------------------------------------------------------------------------------

    private final AtomicBoolean started = new AtomicBoolean();

    @Override
    public void start() throws OXException {
        if (started.get()) {
            LOG.error("{} started", SessiondInit.class.getName());
            return;
        }
        LOG.info("Parse Sessiond properties");

        final ConfigurationService conf = Services.getService(ConfigurationService.class);
        if (conf != null) {
            LOG.info("Starting Sessiond");
            SessiondConfigInterface config = new SessiondConfigImpl(conf);
            UserTypeSessiondConfigRegistry registry = new UserTypeSessiondConfigRegistry(conf);
            SessionHandler.init(config, registry);
            started.set(true);
        }
    }

    @Override
    public void stop() throws OXException {
        if (!started.get()) {
            LOG.error("{} has not been started", SessiondInit.class.getName());
            return;
        }
        SessionHandler.close();
        started.set(false);
    }

    /**
     * Checks if {@link SessiondInit} is started
     *
     * @return <code>true</code> if {@link SessiondInit} is started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started.get();
    }
}
