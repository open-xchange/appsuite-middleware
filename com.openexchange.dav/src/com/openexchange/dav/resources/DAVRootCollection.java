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

package com.openexchange.dav.resources;

import static com.openexchange.dav.DAVTools.getExternalPath;
import java.util.Date;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.mixins.ACL;
import com.openexchange.dav.mixins.ACLRestrictions;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.dav.mixins.SupportedPrivilegeSet;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.group.GroupStorage;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link DAVRootCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class DAVRootCollection extends DAVCollection {

    private static final Permission[] ROOT_PERMISSIONS = {
        new BasicPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, true, Permissions.createPermissionBits(
            Permission.READ_FOLDER, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, false))
    };

    private final String displayName;

    /**
     * Initializes a new {@link DAVRootCollection}.
     *
     * @param factory The underlying factory
     * @param displayName The display name to use
     */
    protected DAVRootCollection(DAVFactory factory, String displayName) {
        super(factory, new WebdavPath(getExternalPath(factory.getService(ConfigViewFactory.class), null)));
        this.displayName = displayName;
        ConfigViewFactory configViewFactory = factory.getService(ConfigViewFactory.class);
        includeProperties(
            new CurrentUserPrincipal(factory), new SupportedPrivilegeSet(), new ACL(ROOT_PERMISSIONS, configViewFactory), new ACLRestrictions()
        );
    }

    @Override
    public Permission[] getPermissions() {
        return ROOT_PERMISSIONS;
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return new Date(0L);
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return new Date(0L);
    }

    @Override
    public String getSyncToken() throws WebdavProtocolException {
        Date lastModified = getLastModified();
        return null == lastModified ? "0" : String.valueOf(lastModified.getTime());
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return displayName;
    }

}
