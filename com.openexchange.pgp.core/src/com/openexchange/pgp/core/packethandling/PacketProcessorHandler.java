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

package com.openexchange.pgp.core.packethandling;

/**
 * {@link PacketProcessorHandler} gets called while processing a PGP Message and is able to remove or modify a packet
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public interface PacketProcessorHandler {

    /**
     * Handles a PGP packet while processing a PGP message
     *
     * @param packet The packet to handle
     * @return A set of packets which should be written back to the message instead of the given packet,
     *         or an empty set or null to remove the given packet from the message
     * @throws Exception
     */
    public PGPPacket[] handlePacket(PGPPacket packet) throws Exception;

    /**
     *
     * Handles the raw packet data right before writing them back to the message.
     *
     * @param packet the {@link PGPPacket} owning the packetData
     * @param packetData The raw packet data
     * @return The data which will be written back to the message.
     */
    public byte[] handlePacketData(PGPPacket packet, byte[] packetData);
}
