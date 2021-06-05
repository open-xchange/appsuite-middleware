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

package com.openexchange.chronos.provider.composition.impl.idmangling;

import java.util.Date;
import java.util.Map;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link IDManglingAccountAwareGroupwareFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingAccountAwareGroupwareFolder extends IDManglingAccountAwareFolder implements GroupwareCalendarFolder {

    protected final String newParentId;
    @SuppressWarnings("hiding")
    protected final GroupwareCalendarFolder delegate;

    /**
     * Initializes a new {@link IDManglingAccountAwareGroupwareFolder}.
     *
     * @param delegate The calendar folder delegate
     * @param account The underlying calendar account
     * @param newId The new identifier to hide the delegate's one
     * @param newParentId The new parent identifier to hide the delegate's one
     */
    public IDManglingAccountAwareGroupwareFolder(GroupwareCalendarFolder delegate, CalendarAccount account, String newId, String newParentId) {
        super(delegate, account, newId);
        this.delegate = delegate;
        this.newParentId = newParentId;
    }

    @Override
    public String getParentId() {
        return newParentId;
    }

    @Override
    public boolean isDefaultFolder() {
        return delegate.isDefaultFolder();
    }

    @Override
    public EntityInfo getModifiedFrom() {
        return delegate.getModifiedFrom();
    }

    @Override
    public EntityInfo getCreatedFrom() {
        return delegate.getCreatedFrom();
    }

    @Override
    public Date getCreationDate() {
        return delegate.getCreationDate();
    }

    @Override
    public GroupwareFolderType getType() {
        return delegate.getType();
    }

    @Override
    public Map<String, Object> getMeta() {
        return delegate.getMeta();
    }

    @Override
    public String toString() {
        return "IDManglingGroupwareFolder [newId=" + newId + ", newParentId=" + newParentId + ", delegate=" + delegate + "]";
    }

}
