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

package com.openexchange.pns;


/**
 * {@link KnownTransport} - The enumeration for known transports for the push notification service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum KnownTransport {

    /**
     * The transport by a Web Socket connection.
     */
    WEB_SOCKET("websocket"),
    /**
     * The transport by Apple Push Notification Service (APNS).
     */
    APNS("apn", "apns"),
    /**
     * The transport by Google Cloud Messaging service (GCM).
     */
    GCM("gcm"),
    /**
     * The transport by Windows Push Notification Services (WNS).
     */
    WNS("wns"),
    /**
     * The transport by Apple Push Notification Service (APNS) using HTTP/2.
     */
    APNS_HTTP2("apns_http2"),

    ;

    private final String transportId;
    private final String[] aliases;

    private KnownTransport(String transportId, String... aliases) {
        this.transportId = transportId;
        this.aliases = aliases;
    }

    /**
     * Gets the transport identifier.
     *
     * @return The transport identifier
     */
    public String getTransportId() {
        return transportId;
    }

    /**
     * Gets the known transport for specified identifier.
     *
     * @param transportId The transport identifier
     * @return The associated known transport or <code>null</code>
     */
    public static KnownTransport knownTransportFor(String transportId) {
        if (null != transportId) {
            for (KnownTransport knownTransport : values()) {
                // Check transport identifier
                if (transportId.equals(knownTransport.transportId)) {
                    return knownTransport;
                }

                // Check against aliases
                String[] aliases = knownTransport.aliases;
                if (null != aliases && aliases.length > 0) {
                    for (String alias : aliases) {
                        if (transportId.equals(alias)) {
                            return knownTransport;
                        }
                    }
                }
            }
        }
        return null;
    }

}
