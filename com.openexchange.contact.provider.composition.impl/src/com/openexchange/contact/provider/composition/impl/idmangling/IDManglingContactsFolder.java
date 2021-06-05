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

import java.util.Date;
import java.util.List;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsPermission;
import com.openexchange.contact.common.ExtendedProperties;
import com.openexchange.contact.common.UsedForSync;
import com.openexchange.exception.OXException;

/**
 * {@link IDManglingContactsFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class IDManglingContactsFolder implements ContactsFolder {

    protected final String newId;
    private final ContactsFolder delegate;

    /**
     * Initializes a new {@link IDManglingContactsFolder}.
     *
     * @param delegate The contacts folder delegate
     * @param newId The new identifier to hide the delegate's one
     */
    public IDManglingContactsFolder(ContactsFolder delegate, String newId) {
        super();
        this.delegate = delegate;
        this.newId = newId;
    }

    @Override
    public String getId() {
        return newId;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Boolean isSubscribed() {
        return delegate.isSubscribed();
    }

    @Override
    public Date getLastModified() {
        return delegate.getLastModified();
    }

    @Override
    public List<ContactsPermission> getPermissions() {
        return delegate.getPermissions();
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return delegate.getExtendedProperties();
    }

    @Override
    public OXException getAccountError() {
        return delegate.getAccountError();
    }

    @Override
    public String toString() {
        return "IDManglingContactsFolder [newId=" + newId + ", delegate=" + delegate + "]";
    }

    @Override
    public UsedForSync getUsedForSync() {
        return delegate.getUsedForSync();
    }
}
