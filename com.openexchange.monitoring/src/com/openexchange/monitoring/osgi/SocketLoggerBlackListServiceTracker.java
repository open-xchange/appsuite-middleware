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

package com.openexchange.monitoring.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.monitoring.sockets.SocketLoggerRegistryService;
import com.openexchange.monitoring.sockets.exceptions.BlackListException;

/**
 * {@link SocketLoggerBlackListServiceTracker} - Tracks for SocketLoggerRegistryService appearance and adds specified logger name to its
 * black-list. Logger name gets removed from black-list on disappearance respectively.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public final class SocketLoggerBlackListServiceTracker extends AbstractSocketLoggerRegistryServiceTracker {

    /**
     * Initializes a new {@link SocketLoggerBlackListServiceTracker}.
     *
     * @param loggerName The name of the logger that is supposed to be added to black-list
     * @param context The bundle context that should be used by this tracker
     */
    public SocketLoggerBlackListServiceTracker(String loggerName, BundleContext context) {
        super(context, loggerName);
    }

    @Override
    protected void onRegistryAppeared(SocketLoggerRegistryService registryService, String loggerName) throws BlackListException {
        registryService.blacklistLogger(loggerName);
    }

    @Override
    protected void onRegistryDisappearing(SocketLoggerRegistryService registryService, String loggerName) {
        registryService.unblacklistLoggerFor(loggerName);
    }

}
