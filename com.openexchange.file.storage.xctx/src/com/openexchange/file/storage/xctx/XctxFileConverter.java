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
 *    trademarks of the OX Software GmbH group of companies.
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