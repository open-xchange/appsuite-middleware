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

package com.openexchange.dav.mixins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.dav.Privilege;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;

/**
 * {@link CurrentUserPrivilegeSet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class CurrentUserPrivilegeSet implements PropertyMixin {

    private final List<Privilege> privileges;

    /**
     * Initializes a new {@link CurrentUserPrivilegeSet}.
     *
     * @param permission The underlying folder permissions
     */
    public CurrentUserPrivilegeSet(Permission permission) {
        this(permission, false);
    }

    /**
     * Initializes a new {@link CurrentUserPrivilegeSet}.
     *
     * @param permission The underlying folder permissions
     * @param allowWriteProperties <code>true</code> to add the {@link #WRITE_PROPERTIES}-privilege regardless of the underlying
     *            permission, <code>false</code>, otherwise
     */
    public CurrentUserPrivilegeSet(Permission permission, boolean allowWriteProperties) {
        super();
        this.privileges = Privilege.getApplying(permission, true);
    }

    public CurrentUserPrivilegeSet(Privilege...privileges) {
        super();
        this.privileges = Arrays.asList(privileges);
    }

    @Override
    public List<WebdavProperty> getAllProperties() throws OXException {
        return Collections.emptyList();
    }

    @Override
    public WebdavProperty getProperty(String namespace, String name) throws OXException {
        if (namespace.equals(Protocol.DAV_NS.getURI()) && name.equals("current-user-privilege-set")) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            property.setXML(true);
            StringBuilder xml = new StringBuilder();
            for (Privilege priv : privileges) {
                xml.append("<D:privilege>").append("<D:").append(priv.getName()).append(" />").append("</D:privilege>");
            }
            property.setValue(xml.toString());

            return property;
        }
        return null;
    }
}
