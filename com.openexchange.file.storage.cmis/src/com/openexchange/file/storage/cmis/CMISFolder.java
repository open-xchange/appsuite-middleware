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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cmis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;

/**
 * {@link CMISFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CMISFolder extends DefaultFileStorageFolder {

    private final int userId;

    /**
     * Initializes a new {@link CMISFolder}.
     */
    public CMISFolder(final int userId) {
        super();
        holdsFiles = true;
        b_holdsFiles = true;
        holdsFolders = true;
        b_holdsFolders = true;
        exists = true;
        subscribed = true;
        b_subscribed = true;
        this.userId = userId;
        final DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(userId);
        permissions = Collections.<FileStoragePermission> singletonList(permission);
        ownPermission = permission;
    }

    /**
     * Parses specified CMIS folder.
     *
     * @param folder The CMIS folder denoting the directory
     * @throws OXException If parsing CMIS folder property set fails
     */
    public void parseSmbFolder(final Folder folder) throws OXException {
        if (null != folder) {
            try {
                final CMISEntityMapping mapping = CMISEntityMapping.DEFAULT.get();
                if (null != mapping) {
                    final Acl acl = folder.getAcl();
                    final List<Ace> aces = acl.getAces();
                    final List<FileStoragePermission> list = new ArrayList<FileStoragePermission>(aces.size());
                    for (final Ace ace : aces) {
                        final int userId = mapping.getUserId(ace.getPrincipalId());
                        if (userId > 0) {
                            final List<String> perms = ace.getPermissions();
                            final DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
                            permission.setNoPermissions();
                            permission.setFolderPermission(FileStoragePermission.READ_FOLDER);
                            permission.setEntity(userId);
                            for (final String perm : perms) {
                                if ("read".equalsIgnoreCase(perm) || "cmis:read".equalsIgnoreCase(perm)) {
                                    permission.setReadPermission(FileStoragePermission.READ_ALL_OBJECTS);
                                } else if ("write".equalsIgnoreCase(perm) || "cmis:write".equalsIgnoreCase(perm)) {
                                    permission.setWritePermission(FileStoragePermission.READ_ALL_OBJECTS);
                                    permission.setDeletePermission(FileStoragePermission.DELETE_ALL_OBJECTS);
                                    permission.setFolderPermission(FileStoragePermission.CREATE_SUB_FOLDERS);
                                } else if ("all".equalsIgnoreCase(perm) || "cmis:all".equalsIgnoreCase(perm)) {
                                    permission.setMaxPermissions();
                                }
                            }
                            list.add(permission);
                            if (this.userId == userId) {
                                ownPermission = permission;
                            }
                        }
                    }
                    permissions = list;
                }
                id = folder.getId();
                {
                    final String p = folder.getParentId();
                    if (null == p) {
                        rootFolder = true;
                        id = FileStorageFolder.ROOT_FULLNAME;
                        parentId = null;
                    } else {
                        rootFolder = false;
                        parentId = p;
                    }
                    b_rootFolder = true;
                }
                creationDate = new Date(folder.getCreationDate().getTimeInMillis());
                lastModifiedDate = new Date(folder.getLastModificationDate().getTimeInMillis());
                name = folder.getName();
                /*
                 * Iterate headers
                 */
                final List<Property<?>> properties = folder.getProperties();
                if (null != properties) {
                    final Map<String, Object> props = new HashMap<String, Object>(properties.size());
                    for (final Property<?> entry : properties) {
                        final List<String> values = entry.getValue();
                        if (null != values && 1 == values.size()) {
                            props.put(entry.getLocalName(), values.get(0));
                        }
                    }
                    if (!props.isEmpty()) {
                        setProperties(props);
                    }
                }
            } catch (final RuntimeException e) {
                throw CMISExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

}
