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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.monitoring.sockets.internal;

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
