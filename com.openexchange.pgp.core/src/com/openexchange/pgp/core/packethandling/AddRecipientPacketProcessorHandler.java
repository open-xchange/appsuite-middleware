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

package com.openexchange.pgp.core.packethandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.bcpg.ContainedPacket;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.bcpg.PublicKeyEncSessionPacket;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import com.openexchange.pgp.core.PGPSessionDecrypter;

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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.guard.pgpcore.commons.PacketProcessorHandler#handlePacket(com.openexchange.guard.pgpcore.commons.GuardBCPacket)
     */
    @Override
    public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
        Packet rawPacket = packet.getBcPacket();
        if (!alreadyProcessed && rawPacket instanceof PublicKeyEncSessionPacket) {
            List<PGPPacket> ret = new ArrayList<PGPPacket>(Arrays.asList(new PGPPacket[] { packet /* keep the original packet */}));

            //Decrypt the session data
            byte[] symmetricSessionKey = new PGPSessionDecrypter().decryptSymmetricSessionKey((PublicKeyEncSessionPacket) rawPacket, addingIdentity);
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

    /* (non-Javadoc)
     * @see com.openexchange.pgp.core.packethandling.PacketProcessorHandler#modifyPacketData(com.openexchange.pgp.core.packethandling.PGPPacket, byte[])
     */
    @Override
    public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
        return packetData;
    }
}
