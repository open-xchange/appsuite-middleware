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

package com.openexchange.contact.common;

import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultContactsFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class DefaultContactsFolder implements ContactsFolder {

    private String id;
    private String name;
    private Boolean subscribed;
    private UsedForSync usedForSync;
    private Date lastModified;
    private ExtendedProperties extendedProperties;
    private OXException accountError;
    private List<ContactsPermission> permissions;

    /**
     * Initializes a new {@link DefaultContactsFolder}.
     */
    public DefaultContactsFolder() {
        super();
        subscribed = null;
        usedForSync = UsedForSync.DEFAULT;
    }

    /**
     * Initializes a new {@link DefaultContactsFolder}, taking over the properties from another folder.
     *
     * @param folder The folder to copy the properties from
     */
    public DefaultContactsFolder(ContactsFolder folder) {
        this();
        id = folder.getId();
        name = folder.getName();
        lastModified = folder.getLastModified();
        extendedProperties = folder.getExtendedProperties();
        subscribed = folder.isSubscribed();
        usedForSync = folder.getUsedForSync();
        permissions = folder.getPermissions();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public UsedForSync getUsedForSync() {
        return usedForSync;
    }

    public void setUsedForSync(UsedForSync usedForSync) {
        this.usedForSync = usedForSync;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(ExtendedProperties extendedProperties) {
        this.extendedProperties = extendedProperties;
    }

    @Override
    public OXException getAccountError() {
        return accountError;
    }

    public void setAccountError(OXException accountError) {
        this.accountError = accountError;
    }

    public void setPermissions(List<ContactsPermission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public List<ContactsPermission> getPermissions() {
        return permissions;
    }
}
