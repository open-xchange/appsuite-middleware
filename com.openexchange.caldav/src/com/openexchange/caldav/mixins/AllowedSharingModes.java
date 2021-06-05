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

import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link AllowedSharingModes}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AllowedSharingModes extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AllowedSharingModes.class);

    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link AllowedSharingModes}.
     *
     * @param collection The folder collection
     */
    public AllowedSharingModes(FolderCollection<?> collection) {
        super(CaldavProtocol.CALENDARSERVER_NS.getURI(), "allowed-sharing-modes");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (supportsPermissions() && hasFullSharedFolderAccess()) {
            return "<can-be-shared/><can-be-published/>";
        }
        return "<never-shared/><never-publish/>";
    }

    private boolean supportsPermissions() {
        return null != collection.getFolder() && null != collection.getFolder().getSupportedCapabilities() &&
            collection.getFolder().getSupportedCapabilities().contains("permissions");
    }

    private boolean hasFullSharedFolderAccess() {
        try {
            UserPermissionBits permissionBits = ServerSessionAdapter.valueOf(collection.getFactory().getSession()).getUserPermissionBits();
            return permissionBits.hasFullSharedFolderAccess();
        } catch (OXException e) {
            LOG.warn("Error checking user permission bits", e);
        }
        return false;
    }

}
