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

package com.openexchange.caldav.mixins;

import com.google.common.xml.XmlEscapers;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.java.Strings;
import com.openexchange.user.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link Invite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Invite extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Invite.class);

    private final FolderCollection<?> collection;
    private final GroupwareCaldavFactory factory;

    /**
     * Initializes a new {@link Invite}.
     *
     * @param factory The CalDAV factory
     * @param collection The collection
     */
    public Invite(GroupwareCaldavFactory factory, FolderCollection<?> collection) {
        super(CaldavProtocol.CALENDARSERVER_NS.getURI(), "invite");
        this.factory = factory;
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (null == collection || null == collection.getFolder() || null == collection.getFolder().getPermissions() ||
            null == collection.getFolder().getSupportedCapabilities() || false == collection.getFolder().getSupportedCapabilities().contains("permissions")) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        Permission[] permissions = collection.getFolder().getPermissions();
        for (Permission permission : permissions) {
            try {
                if (permission.isAdmin()) {
                    stringBuilder.append("<CS:organizer>").append(getEntityElements(permission)).append("</CS:organizer>");
                } else if (impliesReadPermissions(permission)) {
                    stringBuilder.append("<CS:user>").append(getEntityElements(permission)).append("<CS:invite-accepted/>")
                        .append("<CS:access><CS:read");
                    if (impliesReadWritePermissions(permission)) {
                        stringBuilder.append("-write");
                    }
                    stringBuilder.append("/></CS:access></CS:user>");
                }
            } catch (OXException e) {
                LOG.warn("error resolving permission entity from '{}'", collection.getFolder(), e);
            }
        }
        return stringBuilder.toString();
    }

    private String getEntityElements(Permission permission) throws OXException {
        String commonName;
        String uri;
        if (permission.isGroup()) {
            uri = PrincipalURL.forGroup(permission.getEntity(), factory.getConfigViewFactory());
            Group group = factory.requireService(GroupService.class).getGroup(factory.getContext(), permission.getEntity());
            commonName = " + " + group.getDisplayName();
        } else {
            uri = PrincipalURL.forUser(permission.getEntity(), factory.getConfigViewFactory());
            User user = factory.resolveUser(permission.getEntity());
            commonName = user.getDisplayName();
            if (Strings.isEmpty(commonName)) {
                commonName = user.getMail();
                if (Strings.isEmpty(commonName)) {
                    commonName = "User " + user.getId();
                }
            }
        }
        boolean mailtoPrefix = true; // bug #44264
        return new StringBuilder()
            .append("<D:href>").append(mailtoPrefix ? "mailto:" : "").append(XmlEscapers.xmlContentEscaper().escape(uri)).append("</D:href>")
            .append("<CS:common-name>").append(XmlEscapers.xmlContentEscaper().escape(commonName)).append("</CS:common-name>")
        .toString();
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
