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

import java.io.IOException;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.bcpg.PublicKeyEncSessionPacket;

/**
 * {@link RemoveRecipientPacketProcessorHandler} removes recipients from a PGP Message without the need for re-encrypting the whole message
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.guard.pgpcore.commons.PGPPacketProcessorHandler#handlePacket(com.openexchange.guard.pgpcore.commons.GuardBCPacket)
     */
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
}
