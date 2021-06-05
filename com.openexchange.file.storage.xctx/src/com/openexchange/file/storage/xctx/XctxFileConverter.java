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

package com.openexchange.file.storage.xctx;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.infostore.FileConverter;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.session.Session;

/**
 * {@link XctxFileConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFileConverter extends FileConverter {

    private final Session guestSession;
    private final EntityHelper entityHelper;
    
    /**
     * Initializes a new {@link XctxFileConverter}.
     * 
     * @param accountAccess The parent account access
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     */
    public XctxFileConverter(XctxAccountAccess accountAccess, Session guestSession) {
        super();
        this.guestSession = guestSession;
        this.entityHelper = new EntityHelper(accountAccess);
    }

    @Override
    public File getFile(DocumentMetadata metadata) {
        if (null == metadata) {
            return null;
        }
        /*
         * get file with entities under perspective of remote guest session in foreign context
         */
        DefaultFile file = new DefaultFile(super.getFile(metadata));
        /*
         * qualify remote entities for usage in local session in storage account's context & erase ambiguous numerical identifiers
         */
        file.setCreatedFrom(entityHelper.mangleRemoteEntity(file.getCreatedFrom()));
        file.setCreatedBy(0);
        file.setModifiedFrom(entityHelper.mangleRemoteEntity(file.getModifiedFrom()));
        file.setModifiedBy(0);
        /*
         * enhance & qualify remote entities in object permissions for usage in local session in storage account's context
         */
        List<FileStorageObjectPermission> objectPermissions = entityHelper.addObjectPermissionEntityInfos(guestSession, file.getObjectPermissions());
        file.setObjectPermissions(entityHelper.mangleRemoteObjectPermissions(objectPermissions));
        /*
         * assume not shareable by default
         */
        file.setShareable(false);
        return file;
    }

    @Override
    public DocumentMetadata getMetadata(File file) throws OXException {
        /*
         * get metadata with entities under perspective of local session in storage account's context
         */
        DocumentMetadata metadata = super.getMetadata(file);
        /*
         * restore previously mangled entities for context of guest session
         */
        EntityInfo remoteCreatedFrom = entityHelper.unmangleLocalEntity(metadata.getCreatedFrom());
        metadata.setCreatedFrom(remoteCreatedFrom);
        metadata.setCreatedBy(null != remoteCreatedFrom ? remoteCreatedFrom.getEntity() : 0);
        EntityInfo remoteModifiedFrom = entityHelper.unmangleLocalEntity(metadata.getModifiedFrom());
        metadata.setModifiedFrom(remoteModifiedFrom);
        metadata.setModifiedBy(null != remoteModifiedFrom ? remoteModifiedFrom.getEntity() : 0);
        /*
         * restore previously mangled entities in object permissions for context of guest session
         */
        file.setObjectPermissions(entityHelper.unmangleLocalObjectPermissions(file.getObjectPermissions()));
        return metadata;
    }

}