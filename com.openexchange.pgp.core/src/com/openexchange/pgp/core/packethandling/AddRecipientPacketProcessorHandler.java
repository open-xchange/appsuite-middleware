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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.bcpg.ContainedPacket;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.bcpg.PublicKeyEncSessionPacket;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import com.openexchange.pgp.core.PGPSessionKeyExtractor;

/**
 * {@link AddRecipientPacketProcessorHandler} adds new recipients to a PGP Message without the need to re-encrypt the whole Message
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class AddRecipientPacketProcessorHandler implements PacketProcessorHandler {

    private final PGPPublicKey[] identitiesToAdd;
    private final PGPPrivateKey addingIdentity;
    private boolean alreadyProcessed;

    /**
     * Initializes a new {@link AddRecipientPacketProcessorHandler}.
     *
     * @param addingIdentity The key of the identity who want's to add other identities
     * @param identitiesToAdd The keys of the identity which should be added to the PGP Message being able to decrypt it
     */
    public AddRecipientPacketProcessorHandler(PGPPrivateKey addingIdentity, PGPPublicKey[] identitiesToAdd) {
        this.addingIdentity = addingIdentity;
        this.identitiesToAdd = identitiesToAdd;
    }

    @Override
    public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
        Packet rawPacket = packet.getBcPacket();
        if (!alreadyProcessed && rawPacket instanceof PublicKeyEncSessionPacket) {
            List<PGPPacket> ret = new ArrayList<PGPPacket>(Arrays.asList(new PGPPacket[] { packet /* keep the original packet */}));

            //Decrypt the session data
            byte[] symmetricSessionKey = new PGPSessionKeyExtractor().decryptSymmetricSessionKey((PublicKeyEncSessionPacket) rawPacket, addingIdentity);
            try {
                //Creating a new session packet for each new identity
                for (PGPPublicKey identities : identitiesToAdd) {
                    ContainedPacket newPacket = PacketUtil.createSessionPacketForIdentitiy(identities, symmetricSessionKey);
                    ret.add(new PGPPacket(newPacket, null));
                }
            } finally {
                //Finally wipe the session data from memory if not used anymore
                for (int i = 0; i < symmetricSessionKey.length; i++) {
                    symmetricSessionKey[i] = 0x0;
                }
            }

            //also keeping the original recipient
            ret.add(packet);

            //Mark as done;
            //because it could be possible that the PGP Message contains more than one PublicKeyEncSessionPacket and this handler is called multiple times
            alreadyProcessed = true;
            return ret.toArray(new PGPPacket[ret.size()]);
        }
        return new PGPPacket[] { packet };
    }

    @Override
    public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
        return packetData;
    }
}
