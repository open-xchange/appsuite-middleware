/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.dav.mixins;

import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link ShareAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class ShareAccess extends SingleXMLPropertyMixin {

    /**
     * Used to indicate that the resource is not shared.  This is the default, which means that if the DAV:share-access is omitted, this value is implied.
     */
    public static final String NOT_SHARED = "not-shared";

    /**
     * Used to indicate that the resource is owned by the current user and is being shared by them.
     */
    public static final String SHARED_OWNER = "shared-owner";

    /**
     * Used to indicate that the resource is shared, and the current instance is the 'shared instance' which has read-write access.
     */
    public static final String READ_WRITE = "read-write";

    /**
     * Used to indicate that the resource is shared, and the current instance is the 'shared instance', and only read access is provided.
     */
    public static final String READ = "read";

    private final CommonFolderCollection<?> collection;

    /**
     * Initializes a new {@link ShareAccess}.
     *
     * @param collection The collection
     */
    public ShareAccess(CommonFolderCollection<?> collection) {
        super(DAVProtocol.DAV_NS.getURI(), "share-access");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        UserizedFolder folder = collection.getFolder();
        Permission[] permissions = folder.getPermissions();
        if (null != permissions && 1 < permissions.length) {
            Permission ownPermission = folder.getOwnPermission();
            if (ownPermission.isAdmin()) {
                return SHARED_OWNER;
            } else if (Permission.WRITE_OWN_OBJECTS < ownPermission.getWritePermission()) {
                return READ_WRITE;
            } else {
                return READ;
            }
        }
        return NOT_SHARED;
    }

}
