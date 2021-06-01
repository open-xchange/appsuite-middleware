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

package com.openexchange.contact.provider.composition.impl.idmangling;

import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DelegatingContact;

/**
 * {@link IDManglingContact}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class IDManglingContact extends DelegatingContact {

    private static final long serialVersionUID = -5363513087038095570L;

    private final String newFolderId;

    /**
     * Initializes a new {@link IDManglingContact}.
     *
     * @param delegate The contact delegate
     * @param newFolderId The folder new identifier to take over
     */
    public IDManglingContact(Contact delegate, String newFolderId) {
        super(delegate);
        this.newFolderId = newFolderId;
    }

    @Override
    public void setFolderId(String folderId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsFolderId() {
        return true;
    }

    @Override
    public String getFolderId() {
        return newFolderId;
    }

    @Override
    public String toString() {
        return "IDManglingContact [newFolderId=" + newFolderId + ", delegate=" + delegate + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((newFolderId == null) ? 0 : newFolderId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IDManglingContact other = (IDManglingContact) obj;
        if (newFolderId == null) {
            if (other.newFolderId != null) {
                return false;
            }
        } else if (!newFolderId.equals(other.newFolderId)) {
            return false;
        }
        return true;
    }
}
