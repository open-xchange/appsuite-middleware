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

package com.openexchange.monitoring.impl.sockets.internal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link MonitoringSocketFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MonitoringSocketFactory implements SocketImplFactory {

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);

    /**
     * Checks if <code>MonitoringSocketFactory</code> is currently enabled.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public static boolean isEnabled() {
        return ENABLED.get();
    }

    /**
     * Checks if <code>MonitoringSocketFactory</code> is currently disabled.
     *
     * @return <code>true</code> if disabled; otherwise <code>false</code>
     */
    public static boolean isDisabled() {
        return !ENABLED.get();
    }

    /**
     * Initializes the monitoring socket factory.
     *
     * @throws IOException If initialization fails
     */
    public static void initMonitoringSocketFactory() throws IOException {
        if (ENABLED.compareAndSet(false, true)) {
            SocketImplFactory socketImplFactory = new MonitoringSocketFactory();
            Socket.setSocketImplFactory(socketImplFactory);
            // ServerSocket.setSocketFactory(socketImplFactory);
        }
    }

    /**
     * Stops the monitoring socket factory.
     *
     * @throws IOException If stopping fails
     */
    public static void stopMonitoringSocketFactory() {
        if (ENABLED.compareAndSet(true, false)) {
            // Acquire field that references the socket factory
            Field addressCacheField;
            try {
                addressCacheField = Socket.class.getDeclaredField("factory");
                addressCacheField.setAccessible(true);
            } catch (Exception e) {
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MonitoringSocketFactory.class);
                logger.error("Failed to drop socket factory", e);
                return;
            }

            // Set it to null while holding lock for Socket.class
            synchronized (Socket.class) {
                try {
                    addressCacheField.set(null, null);
                } catch (Exception e) {
                    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MonitoringSocketFactory.class);
                    logger.error("Failed to drop socket factory", e);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link MonitoringSocketFactory}.
     */
    private MonitoringSocketFactory() {
        super();
    }

    @Override
    public SocketImpl createSocketImpl() {
        try {
            return new MonitoringSocketImpl();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
