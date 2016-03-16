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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.jolokia.clt;

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
