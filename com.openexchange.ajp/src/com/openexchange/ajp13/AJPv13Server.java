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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13;

import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.monitoring.MonitoringInfo;

/**
 * {@link AJPv13Server}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AJPv13Server {

    /**
     * <p>
     * The value 0 used in constructor <code>ServerSocket(int port, int backlog, InetAddress bindAddr)</code> causes to fall back to default
     * value for backlog
     * <p>
     * The <code>backlog</code> argument must be a positive value greater than 0. If the value passed if equal or less than 0, then the
     * default value will be assumed
     */
    protected static final int DEFAULT_BACKLOG = 0;

    private static volatile AJPv13Server instance;

    /**
     * Sets the singleton instance of AJP server to specified instance.
     *
     * @param instance The instance to use as singleton
     * @throws IllegalStateException If instance has already been set before
     */
    public static void setInstance(final AJPv13Server instance) {
        AJPv13Server tmp = AJPv13Server.instance;
        if (null == tmp) {
            synchronized (AJPv13Server.class) {
                tmp = AJPv13Server.instance;
                if (null == tmp) {
                    AJPv13Server.instance = instance;
                } else {
                    throw new IllegalStateException("AJP server instance already set");
                }
            }
        } else {
            throw new IllegalStateException("AJP server instance already set");
        }
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static AJPv13Server getInstance() {
        return AJPv13Server.instance;
    }

    /**
     * Releases the singleton instance of AJP server.
     */
    public static void releaseInstrance() {
        AJPv13Server tmp = AJPv13Server.instance;
        if (null != tmp) {
            synchronized (AJPv13Server.class) {
                tmp = AJPv13Server.instance;
                if (null != tmp) {
                    AJPv13Server.instance = null;
                }
            }
        }
    }

    /**
     * Starts the AJP server
     *
     * @throws AJPv13Exception If starting the AJP server fails
     * @throws NullPointerException If AJP server instance has not been set before
     */
    public static void startAJPServer() throws AJPv13Exception {
        instance.startServer();
    }

    /**
     * Re-Starts the AJP server
     *
     * @throws AJPv13Exception If re-starting the AJP server fails
     * @throws NullPointerException If AJP server instance has not been set before
     */
    public static void restartAJPServer() throws AJPv13Exception {
        stopAJPServer();
        startAJPServer();
    }

    /**
     * Stops the AJP server
     *
     * @throws NullPointerException If AJP server instance has not been set before
     */
    public static void stopAJPServer() {
        instance.stopServer();
    }

    /**
     * Gets the number of open AJP connections.
     *
     * @return The number of open AJP connections
     */
    public static int getNumberOfOpenAJPSockets() {
        return MonitoringInfo.getNumberOfConnections(MonitoringInfo.AJP_SOCKET);
    }

    /**
     * Increments the counter for open AJP connections.
     */
    protected static void incrementNumberOfOpenAJPSockets() {
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.AJP_SOCKET);
    }

    /**
     * Decrements the counter for open AJP connections.
     */
    public static void decrementNumberOfOpenAJPSockets() {
        MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.AJP_SOCKET);
    }

    /*-
     * ----------------------- Abstract methods -----------------------
     */

    /**
     * Initializes a new {@link AJPv13Server}.
     */
    protected AJPv13Server() {
        super();
    }

    /**
     * Starts this AJP server instance.
     *
     * @throws AJPv13Exception If starting this instance fails
     */
    protected abstract void startServer() throws AJPv13Exception;

    /**
     * Stops this AJP server instance.
     */
    protected abstract void stopServer();

    /**
     * Checks if this AJP server instance is running.
     *
     * @return <code>true</code> if this AJP server instance is running; otherwise <code>false</code>
     */
    public abstract boolean isRunning();
}
