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

package com.sun.mail.util;

import java.net.InetAddress;
import java.util.Collection;

/**
 * {@link AddressList} - Infinitely iterates over addresses.
 */
public class AddressList {

    private final Collection<InetAddress> collection;
    private InetAddress offset;

    public AddressList(Collection<InetAddress> collection) {
        this(collection, null);
    }

    public AddressList(Collection<InetAddress> collection, InetAddress offset) {
        super();
        this.collection = collection;
        this.offset = offset;
    }

    /**
     * Gets the next address.
     *
     * @return The next address
     */
    public InetAddress next() {
        if (offset != null) {
            boolean found = false;
            for (InetAddress element : collection) {
                if (element == offset) {
                    found = true;
                } else if (found) {
                    offset = element;
                    return element;
                }
            }
        }

        // Start with new iterator
        InetAddress element = collection.iterator().next();
        offset = element;
        return element;
    }

}
