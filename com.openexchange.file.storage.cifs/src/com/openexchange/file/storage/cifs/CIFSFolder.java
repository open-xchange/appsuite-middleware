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

package com.openexchange.file.storage.cifs;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;

/**
 * {@link CIFSFolder}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSFolder extends DefaultFileStorageFolder implements TypeAware {

    // private static final String URL_SPEC = CIFSConstants.URL_SPEC;
    private final String rootUrl;

    private FileStorageFolderType type;

    /**
     * Initializes a new {@link CIFSFolder}.
     */
    public CIFSFolder(final int userId, final String rootUrl) {
        super();
        type = FileStorageFolderType.NONE;
        this.rootUrl = rootUrl;
        holdsFiles = true;
        b_holdsFiles = true;
        holdsFolders = true;
        b_holdsFolders = true;
        exists = true;
        subscribed = true;
        b_subscribed = true;
        final DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(userId);
        permissions = Collections.<FileStoragePermission> singletonList(permission);
        ownPermission = permission;
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    /**
     * Sets the type.
     * 
     * @param type The type to set
     * @return This folder with type applied
     */
    public CIFSFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }

    /**
     * Parses specified CIFS/SMB file.
     * 
     * @param smbFile The CIFS/SMB file denoting the directory
     * @throws OXException If parsing CIFS/SMB file property set fails
     */
    public void parseSmbFolder(final SmbFile smbFile) throws OXException {
        if (null != smbFile) {
            try {
                final String path = smbFile.getPath();
                id = Utils.checkFolderId(path);
                {
                    if (rootUrl.equals(path)) {
                        rootFolder = true;
                        id = FileStorageFolder.ROOT_FULLNAME;
                        parentId = null;
                    } else {
                        rootFolder = false;
                        final String sParent = Utils.checkFolderId(smbFile.getParent());
                        parentId = rootUrl.equals(sParent) ? FileStorageFolder.ROOT_FULLNAME : sParent;
                    }
                    b_rootFolder = true;
                }
                creationDate = new Date(smbFile.createTime());
                lastModifiedDate = new Date(smbFile.getIfModifiedSince());
                final String name = smbFile.getName();
                this.name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
                /*
                 * Iterate headers
                 */
                final Map<String, List<String>> headerFields = smbFile.getHeaderFields();
                if (null != headerFields) {
                    final Map<String, Object> props = new HashMap<String, Object>(headerFields.size());
                    for (final Entry<String, List<String>> entry : headerFields.entrySet()) {
                        final List<String> values = entry.getValue();
                        if (null != values && 1 == values.size()) {
                            props.put(entry.getKey(), values.get(0));
                        }
                    }
                    if (!props.isEmpty()) {
                        setProperties(props);
                    }
                }
            } catch (final SmbException e) {
                throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
            }
        }
    }

}
