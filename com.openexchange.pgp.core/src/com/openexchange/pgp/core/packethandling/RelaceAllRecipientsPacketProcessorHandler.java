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
import com.openexchange.pgp.core.exceptions.PGPCoreExceptionCodes;

/**
 * {@link RelaceAllRecipientsPacketProcessorHandler} Replaces all recipients with new list of recipients
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.8.4
 */
public class RelaceAllRecipientsPacketProcessorHandler implements PacketProcessorHandler {

    private final PGPPublicKey[] newIdentitiesToAdd;
    private final SecretKeyForPacketService addingIdentityService;
    private final PGPPrivateKey addingIdentity;
    private boolean alreadyProcessed;

    /**
     * Initializes a new {@link RelaceAllRecipientsPacketProcessorHandler}.
     *
     * @param addingIdentity The key of the identity who want's to add other identities
     * @param newIdentitiesToAdd The keys of the identitys which should be added to the PGP Message being able to decrypt it
     */
    public RelaceAllRecipientsPacketProcessorHandler(SecretKeyForPacketService addingIdentityService, PGPPublicKey[] newIdentitiesToAdd) {
        this.addingIdentityService = addingIdentityService;
        this.addingIdentity = null;
        this.newIdentitiesToAdd = newIdentitiesToAdd;
    }

    /**
     * Initializes a new {@link RelaceAllRecipientsPacketProcessorHandler}.
     *
     * @param addingIdentity The key of the identity who want's to add other identities
     * @param newIdentitiesToAdd The keys of the identitys which should be added to the PGP Message being able to decrypt it
     */
    public RelaceAllRecipientsPacketProcessorHandler(PGPPrivateKey addingIdentity, PGPPublicKey[] newIdentitiesToAdd) {
        this.addingIdentity = addingIdentity;
        this.addingIdentityService = null;
        this.newIdentitiesToAdd = newIdentitiesToAdd;
    }

    @Override
    public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
        Packet rawPacket = packet.getBcPacket();
        // If first pass, add all needed new recipients to the sesion header
        if (rawPacket instanceof PublicKeyEncSessionPacket) {
            if (alreadyProcessed)
             {
                return null;  // We only process the first EncSessionPacket, all the rest are removed
            }
            List<PGPPacket> ret = new ArrayList<PGPPacket>(Arrays.asList(new PGPPacket[] { }));

            //Decrypt the session data
            PublicKeyEncSessionPacket encrPacket = (PublicKeyEncSessionPacket) rawPacket;
            long keyId = encrPacket.getKeyID();
            PGPPrivateKey pKey = addingIdentity == null ? addingIdentityService.getSecretKey(keyId) : addingIdentity;
            if (pKey == null) {
                throw PGPCoreExceptionCodes.PRIVATE_KEY_NOT_FOUND.create();
            }
            byte[] symmetricSessionKey = new PGPSessionKeyExtractor().decryptSymmetricSessionKey(encrPacket, pKey);
            try {
                //Creating a new session packet for each new identity
                for (PGPPublicKey identities : newIdentitiesToAdd) {
                    ContainedPacket newPacket = PacketUtil.createSessionPacketForIdentitiy(identities, symmetricSessionKey);
                    ret.add(new PGPPacket(newPacket, null));
                }
            } finally {
                //Finally wipe the session data from memory if not used anymore
                for (int i = 0; i < symmetricSessionKey.length; i++) {
                    symmetricSessionKey[i] = 0x0;
                }
            }
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
