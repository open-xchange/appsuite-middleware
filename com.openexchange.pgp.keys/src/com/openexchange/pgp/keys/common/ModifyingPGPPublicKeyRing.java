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
            keys.add(it.next());
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
