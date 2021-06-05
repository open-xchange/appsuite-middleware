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

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.java.Strings;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link Invite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class Invite extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Invite.class);

    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link Invite}.
     *
     * @param collection The collection
     */
    public Invite(FolderCollection<?> collection) {
        super(DAVProtocol.DAV_NS.getURI(), "invite");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (null == collection || null == collection.getFolder() || null == collection.getFolder().getPermissions()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        Permission[] permissions = collection.getFolder().getPermissions();
        if (PublicType.getInstance().equals(collection.getFolder().getType())) {

        } else {
            for (Permission permission : permissions) {
                if (permission.isAdmin()) {
                    continue; // don't include "owner"
                }
                try {
                    stringBuilder.append(getEntityElements(permission));
                } catch (OXException e) {
                    LOG.warn("error resolving permission entity from '{}'", collection.getFolder(), e);
                }
            }
        }
        return stringBuilder.toString();
    }

    private String getEntityElements(Permission permission) throws OXException {
        DAVFactory factory = collection.getFactory();
        ConfigViewFactory configViewFactory = factory.getServiceSafe(ConfigViewFactory.class);
        String commonName;
        String uri;
        if (permission.isGroup()) {
            uri = PrincipalURL.forGroup(permission.getEntity(), configViewFactory);
            Group group = factory.requireService(GroupService.class).getGroup(factory.getContext(), permission.getEntity());
            commonName = " + " + group.getDisplayName();
        } else {
            uri = PrincipalURL.forUser(permission.getEntity(), configViewFactory);
            User user = factory.requireService(UserService.class).getUser(permission.getEntity(), factory.getContext());
            commonName = user.getDisplayName();
            if (Strings.isEmpty(commonName)) {
                commonName = user.getMail();
                if (Strings.isEmpty(commonName)) {
                    commonName = "User " + user.getId();
                }
            }
        }
        String shareAccess;
        if (impliesReadWritePermissions(permission)) {
            shareAccess = "read-write";
        } else if (impliesReadPermissions(permission)) {
            shareAccess = "read";
        } else {
            shareAccess = "no-access";
        }
        return new StringBuilder().append("<D:sharee>")
            .append("<D:href>").append(uri).append("</D:href>")
            .append("<D:invite-accepted />")
            .append("<D:share-access>").append(shareAccess).append("</D:share-access>")
//            .append("<CS:common-name>").append(commonName).append("</CS:common-name>")
        .append("</D:sharee>").toString();
    }

    /**
     * Gets a value indicating whether the supplied permission implies (at least) a simplified CalDAV "read" access level.
     *
     * @param permission The permission to check
     * @return <code>true</code> if "read" permissions can be assumed, <code>false</code>, otherwise
     */
    private static boolean impliesReadPermissions(Permission permission) {
        return null != permission &&
            permission.getFolderPermission() >= Permission.READ_FOLDER &&
            permission.getReadPermission() >= Permission.READ_OWN_OBJECTS
        ;
    }

    /**
     * Gets a value indicating whether the supplied permission implies (at least) a simplified CalDAV "read-write" access level.
     *
     * @param permission The permission to check
     * @return <code>true</code> if "read-write" permissions can be assumed, <code>false</code>, otherwise
     */
    private static boolean impliesReadWritePermissions(Permission permission) {
        return null != permission &&
            permission.getFolderPermission() >= Permission.CREATE_OBJECTS_IN_FOLDER &&
            permission.getWritePermission() >= Permission.WRITE_OWN_OBJECTS &&
            permission.getDeletePermission() >= Permission.DELETE_OWN_OBJECTS &&
            permission.getReadPermission() >= Permission.READ_OWN_OBJECTS
        ;
    }

}
