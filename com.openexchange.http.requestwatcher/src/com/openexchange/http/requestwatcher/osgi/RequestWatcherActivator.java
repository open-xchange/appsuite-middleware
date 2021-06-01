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

package com.openexchange.http.requestwatcher.osgi;

import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.http.requestwatcher.internal.RequestWatcherServiceImpl;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

public class RequestWatcherActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link RequestWatcherActivator}.
     */
    public RequestWatcherActivator() {
        super();
    }

    /** The request watcher instance */
    private RequestWatcherServiceImpl requestWatcher;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, TimerService.class };
    }

    @Override
    protected synchronized void startBundle() throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(RequestWatcherActivator.class);
        log.info("Starting request watcher.");

        RequestWatcherServiceImpl requestWatcher = new RequestWatcherServiceImpl(getService(ConfigurationService.class), getService(TimerService.class));
        this.requestWatcher = requestWatcher;
        registerService(RequestWatcherService.class, requestWatcher);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        Logger log = org.slf4j.LoggerFactory.getLogger(RequestWatcherActivator.class);
        log.info("Stopping request watcher.");

        // Stop the Watcher
        RequestWatcherServiceImpl requestWatcher = this.requestWatcher;
        if (null != requestWatcher) {
            requestWatcher.stopWatching();
            this.requestWatcher = null;
        }

        super.stopBundle();
    }

}
