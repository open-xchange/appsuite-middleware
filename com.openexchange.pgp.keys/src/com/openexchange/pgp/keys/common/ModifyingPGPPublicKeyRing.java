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

package com.openexchange.pgp.keys.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

/**
 * Class for modifying a public key ring, adding or removing public keys
 * {@link ModifyingPGPPublicKeyRing}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.1
 */
public class ModifyingPGPPublicKeyRing {

    List<PGPPublicKey> keys;

    public ModifyingPGPPublicKeyRing () {
        keys = new ArrayList<PGPPublicKey>();
    }

    public ModifyingPGPPublicKeyRing(PGPPublicKeyRing ring) {
        Iterator<PGPPublicKey> it = ring.getPublicKeys();
        keys = new ArrayList<PGPPublicKey>();
        while (it.hasNext()) {
            keys.add((PGPPublicKey) it.next());
        }
    }

    /**
     * Remove a public key from the ring
     * @param pubKey
     * @return
     */
    public boolean removePublicKey (PGPPublicKey pubKey) {
        for (int i = 0; i < keys.size(); i++) {
            PGPPublicKey key = keys.get(i);
            if (key.getKeyID() == pubKey.getKeyID()) {
                keys.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a public key to the ring,
     * replace if duplicate
     * @param pubKey
     */
    public void addPublicKey(PGPPublicKey pubKey) {
        for (int i = 0; i < keys.size(); i++) {
            PGPPublicKey key = keys.get(i);
            if (key.getKeyID() == pubKey.getKeyID()) {
                keys.remove(i);
                break;
            }
        }
        keys.add(pubKey);
    }

    /**
     * Gets the public ring
     * Sorts keys such that master is first
     * @return
     */
    public PGPPublicKeyRing getRing() {
        ArrayList<PGPPublicKey> newList = new ArrayList<PGPPublicKey>();
        int masterIndex = -1;
        // First, find the public master key.  Needs to be the first in the keyring
        for (int i = 0; i < keys.size(); i++) {
            PGPPublicKey key = keys.get(i);
            if (key.isMasterKey()) {
                newList.add(key);
                masterIndex = i;
                break;
            }
        }
        // Add the rest
        for (int i = 0; i < keys.size(); i++) {
            if (i != masterIndex) { // We already added the master
                newList.add(keys.get(i));
            }
        }
        return new PGPPublicKeyRing(newList);
    }

}
