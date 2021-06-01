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

package com.openexchange.chronos.provider;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultCalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarFolder implements CalendarFolder {

    private String id;
    private String name;
    private Boolean subscribed;
    private UsedForSync usedForSync;
    private Date lastModified;
    private List<CalendarPermission> permissions;
    private ExtendedProperties extendedProperties;
    private EnumSet<CalendarCapability> supportedCapabilites;
    private OXException accountError;

    /**
     * Initializes a new {@link DefaultCalendarFolder}.
     */
    public DefaultCalendarFolder() {
        super();
        subscribed = null;
        usedForSync = UsedForSync.DEFAULT;
    }

    /**
     * Initializes a new {@link DefaultCalendarFolder}, taking over the properties from another folder.
     *
     * @param folder The folder to copy the properties from
     */
    public DefaultCalendarFolder(CalendarFolder folder) {
        this();
        id = folder.getId();
        name = folder.getName();
        lastModified = folder.getLastModified();
        permissions = folder.getPermissions();
        extendedProperties = folder.getExtendedProperties();
        supportedCapabilites = folder.getSupportedCapabilites();
        subscribed = folder.isSubscribed();
        usedForSync = folder.getUsedForSync();
    }

    /**
     * Initializes a new {@link DefaultCalendarFolder}.
     *
     * @param id The folder identifier
     * @param name The folder name
     */
    public DefaultCalendarFolder(String id, String name) {
        this();
        this.id = id;
        this.name = name;
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
    public List<CalendarPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<CalendarPermission> permissions) {
        this.permissions = permissions;
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
    public EnumSet<CalendarCapability> getSupportedCapabilites() {
        return supportedCapabilites;
    }

    public void setSupportedCapabilites(EnumSet<CalendarCapability> supportedCapabilites) {
        this.supportedCapabilites = supportedCapabilites;
    }

    @Override
    public OXException getAccountError() {
        return accountError;
    }

    public void setAccountError(OXException accountError) {
        this.accountError = accountError;
    }

    @Override
    public String toString() {
        return "DefaultCalendarFolder [id=" + id + ", name=" + name + "]";
    }

}
