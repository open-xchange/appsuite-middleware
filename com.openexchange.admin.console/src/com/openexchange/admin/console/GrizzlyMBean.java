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

package com.openexchange.admin.console;

/**
 * {@link GrizzlyMBean} Enum of MBeans we are interested in. Each containing the ObjectName and the attributes to query.
 *
 * @author <a href="mailto:marc .arens@open-xchange.com">Marc Arens</a>
 */
enum GrizzlyMBean {
    HTTPCODECFILTER("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=HttpCodecFilter,name=HttpCodecFilter", new String[] {
        "total-bytes-written", "total-bytes-received", "http-codec-error-count" }),
    HTTPSERVERFILTER("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=HttpServerFilter,name=HttpServerFilter", new String[] {
        "current-suspended-request-count", "requests-cancelled-count", "requests-completed-count", "requests-received-count",
        "requests-timed-out-count" }),
    KEEPALIVE("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=KeepAlive,name=Keep-Alive", new String[] {
        "hits-count", "idle-timeout-seconds", "live-connections-count", "max-requests-count", "refuses-count", "timeouts-count" }),
    NETWORKLISTENER("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer],type=NetworkListener,name=NetworkListener[http-listener]", new String[] {
        "host", "port", "secure", "started", "paused", "chunking-enabled", "max-http-header-size", "max-pending-bytes" }),
    TCPNIOTRANSPORT("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=TCPNIOTransport,name=Transport", new String[] {
        "bound-addresses", "bytes-read", "bytes-written", "client-connect-timeout-millis", "client-socket-so-timeout", "last-error",
        "open-connections-count", "total-connections-count", "read-buffer-size", "selector-threads-count", "thread-pool-type",
        "server-socket-so-timeout", "socket-keep-alive", "socket-linger", "state", "write-buffer-size" });

    private final String objectName;

    private final String[] attributes;

    /**
     * Initializes a new {@link GrizzlyMBean}.
     *
     * @param objectName The object name needed to query for this MBean
     * @param attributes The attributes of the MBean we are interested in.
     */
    GrizzlyMBean(final String objectName, final String[] attributes) {
        this.objectName = objectName;
        this.attributes = attributes;
    }

    /**
     * Gets the object name of the MBean we are interested in.
     *
     * @return The object name
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Gets the attributes of the MBean we are interested in.
     *
     * @return The attributes
     */
    public String[] getAttributes() {
        return attributes;
    }
}
