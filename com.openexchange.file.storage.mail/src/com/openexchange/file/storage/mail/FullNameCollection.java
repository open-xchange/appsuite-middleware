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

package com.openexchange.file.storage.mail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.file.storage.mail.FullName.Type;

/**
 * {@link FullNameCollection}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class FullNameCollection implements Iterable<FullName> {

    /** The full name of the virtual attachment folder containing all attachments */
    public final String fullNameAll;

    /** The full name of the virtual attachment folder containing received attachments */
    public final String fullNameReceived;

    /** The full name of the virtual attachment folder containing sent attachments */
    public final String fullNameSent;

    /**
     * Initializes a new {@link FullNameCollection}.
     *
     * @param fullNameAll The full name of the virtual attachment folder containing all attachments
     * @param fullNameReceived The full name of the virtual attachment folder containing received attachments
     * @param fullNameSent The full name of the virtual attachment folder containing sent attachments
     */
    public FullNameCollection(String fullNameAll, String fullNameReceived, String fullNameSent) {
        super();
        this.fullNameAll = fullNameAll;
        this.fullNameReceived = fullNameReceived;
        this.fullNameSent = fullNameSent;
    }

    @Override
    public Iterator<FullName> iterator() {
        return asList().iterator();
    }

    /**
     * Gets the full name for specified type.
     *
     * @param type The type
     * @return The associated full name or <code>null</code>
     */
    public FullName getFullNameFor(Type type) {
        if (null == type) {
            return null;
        }

        switch (type) {
            case ALL:
                return null == fullNameAll ? null : new FullName(fullNameAll, Type.ALL);
            case RECEIVED:
                return null == fullNameReceived ? null : new FullName(fullNameReceived, Type.RECEIVED);
            case SENT:
                return null == fullNameSent ? null : new FullName(fullNameSent, Type.SENT);
            default:
                return null;
        }
    }

    /**
     * Gets the {@link List} view for this collection.
     *
     * @return The list
     */
    public List<FullName> asList() {
        List<FullName> fullNames = new ArrayList<FullName>(3);
        if (null != fullNameAll) {
            fullNames.add(new FullName(fullNameAll, Type.ALL));
        }
        if (null != fullNameReceived) {
            fullNames.add(new FullName(fullNameReceived, Type.RECEIVED));
        }
        if (null != fullNameSent) {
            fullNames.add(new FullName(fullNameSent, Type.SENT));
        }
        return fullNames;
    }

}
