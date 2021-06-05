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

import java.io.IOException;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.bcpg.PublicKeyEncSessionPacket;

/**
 * {@link RemoveRecipientPacketProcessorHandler} removes recipients from a PGP Message without the need for re-encrypting the whole message
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class RemoveRecipientPacketProcessorHandler implements PacketProcessorHandler {

    private final long[] keyIdsToRemove;

    /**
     * Initializes a new {@link RemoveRecipientPacketProcessorHandler}.
     *
     * @param keyIds The IDs of the key's which should be removed from being able to decrypt the PGP Message
     */
    public RemoveRecipientPacketProcessorHandler(long[] keyIds) {
        this.keyIdsToRemove = keyIds;
    }

    /**
     * Internal method to check if a session packet matches one of the IDs which get removed
     *
     * @param session The session packet
     * @return True, if the session packet should be removed, false otherwise
     */
    private boolean shouldRemoveSession(PublicKeyEncSessionPacket session) {
        if (keyIdsToRemove != null) {
            for (long id : keyIdsToRemove) {
                if (session.getKeyID() == id) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public PGPPacket[] handlePacket(PGPPacket packet) throws IOException {
        Packet rawPacket = packet.getBcPacket();
        if (rawPacket instanceof PublicKeyEncSessionPacket) {
            if (shouldRemoveSession((PublicKeyEncSessionPacket) rawPacket)) {
                //packet will not be written back
                return null;
            }
        }
        return new PGPPacket[] { packet };
    }

    @Override
    public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
        return packetData;
    }
}
