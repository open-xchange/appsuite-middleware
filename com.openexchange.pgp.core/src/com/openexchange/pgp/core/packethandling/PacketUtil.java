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

import org.bouncycastle.bcpg.ContainedPacket;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

/**
 * {@link PacketUtil}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v2.10.0
 */
public class PacketUtil {

    /**
     * Simple factory method for creating a new Packet for the given identity
     *
     * @param identity The identity which should be able to decrypt the PGP Message
     * @param sessionData The session data which get's encrypted for the given identity
     * @return The packet to add
     * @throws PGPException
     */
    public static ContainedPacket createSessionPacketForIdentitiy(PGPPublicKey identity, byte[] sessionData) throws PGPException {
        PGPKeyEncryptionMethodGenerator packetGenerator = new BcPublicKeyKeyEncryptionMethodGenerator(identity);
        return packetGenerator.generate(identity.getAlgorithm(), sessionData);
    }

}
