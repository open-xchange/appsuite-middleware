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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link CMISFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CMISFolderAccess extends AbstractCMISAccess implements FileStorageFolderAccess {

    private final CMISAccountAccess accountAccess;

    /**
     * Initializes a new {@link CMISFolderAccess}.
     */
    public CMISFolderAccess(final String rootUrl, final org.apache.chemistry.opencmis.client.api.Session cmisSession, final FileStorageAccount account, final Session session, final CMISAccountAccess accountAccess) {
        super(rootUrl, cmisSession, account, session);
        this.accountAccess = accountAccess;
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderId)) {
            return true;
        }
        try {
            if ((FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId))) {
                // Root folder always exists
                return true;
            }
            final CmisObject object = cmisSession.getObject(cmisSession.createObjectId(folderId));
            return (null != object) && ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId());
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        try {
            /*
             * Check
             */
            final ObjectId folderObjectId;
            final CmisObject object;
            final boolean root = (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId));
            if (root) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            if (!root) {
                return convertFolder(folderObjectId, (Folder) object);                
            }
            final CMISFolder folder = convertFolder(folderObjectId, (Folder) object);
            folder.setRootFolder(true);
            folder.setId(FileStorageFolder.ROOT_FULLNAME);
            return folder;
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    private CMISFolder convertFolder(final ObjectId folderObjectId, final Folder folder) throws OXException {
        /*
         * Check sub resources
         */
        final ItemIterable<CmisObject> children = folder.getChildren();
        boolean hasSubdir = false;
        int fileCount = 0;
        for (final CmisObject sub : children) {
            final String typeId = sub.getType().getId();
            if (ObjectType.FOLDER_BASETYPE_ID.equals(typeId)) {
                hasSubdir = true;
            } else if (ObjectType.DOCUMENT_BASETYPE_ID.equals(typeId)) {
                fileCount++;
            }
        }
        final Set<Action> allowableActions = folder.getAllowableActions().getAllowableActions();
        if (allowableActions.contains(Action.CAN_GET_ACL)) {
            try {
                cmisSession.getAcl(folderObjectId, true);
            } catch (final CmisNotSupportedException e) {
                // ACL must not be obtained
            }
        }
        //cmisSession.get
        /*
         * Convert to a folder
         */
        final CMISFolder cmisFolder = new CMISFolder(session.getUserId());
        cmisFolder.parseSmbFolder(folder);
        cmisFolder.setFileCount(fileCount);
        cmisFolder.setSubfolders(hasSubdir);
        cmisFolder.setSubscribedSubfolders(hasSubdir);
        /*
         * TODO: Set capabilities
         */
        return cmisFolder;
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentId, final boolean all) throws OXException {
        try {
            /*
             * Check
             */
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(parentId) || rootUrl.equals(parentId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(parentId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(parentId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(parentId);
            }
            final Folder folder = (Folder) object;
            /*
             * Check sub resources
             */
            final ItemIterable<CmisObject> children = folder.getChildren();
            final List<FileStorageFolder> subs = new LinkedList<FileStorageFolder>();
            for (final CmisObject sub : children) {
                if (ObjectType.FOLDER_BASETYPE_ID.equals(sub.getType().getId())) {
                    subs.add(convertFolder(cmisSession.createObjectId(sub.getId()), (Folder) sub));
                }
            }
            /*
             * Return
             */
            return subs.toArray(new FileStorageFolder[subs.size()]);
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(rootUrl);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        try {
            final String parentId = toCreate.getParentId();
            final ObjectId folderObjectId;
            if (FileStorageFolder.ROOT_FULLNAME.equals(parentId) || rootUrl.equals(parentId)) {
                final CmisObject object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(parentId);
            }
            final List<FileStoragePermission> permissions = toCreate.getPermissions();
            final List<Ace> aces;
            if (null == permissions) {
                aces = Collections.emptyList();
            } else {
                final CMISEntityMapping mapping = CMISEntityMapping.DEFAULT.get();
                if (null == mapping) {
                    aces = Collections.emptyList();
                } else {
                    aces = new ArrayList<Ace>(permissions.size());
                    for (final FileStoragePermission permission : permissions) {
                        final String cmisId = mapping.getCmisId(permission.getEntity());
                        if (null != cmisId) {
                            final AccessControlEntryImpl ace = new AccessControlEntryImpl();
                            ace.setPrincipal(new AccessControlPrincipalDataImpl(cmisId));
                            if (permission.isAdmin()) {
                                ace.setPermissions(Collections.singletonList("cmis:all"));
                            } else {
                                final List<String> rights = new LinkedList<String>();
                                if (permission.getReadPermission() > FileStoragePermission.NO_PERMISSIONS) {
                                    rights.add("cmis:read");
                                }
                                if (permission.getWritePermission() > FileStoragePermission.NO_PERMISSIONS) {
                                    rights.add("cmis:write");
                                }
                            }
                            aces.add(ace);
                        }
                    }
                }
            }
            final Map<String, Object> properties = new HashMap<String, Object>(2);
            properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.FOLDER_BASETYPE_ID);
            properties.put(PropertyIds.NAME, toCreate.getName());
            properties.put(PropertyIds.CREATED_BY, accountAccess.getUser());
            return cmisSession.createFolder(properties, folderObjectId, Collections.<Policy> emptyList(), aces, Collections.<Ace> emptyList()).getId();
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String updateFolder(final String folderId, final FileStorageFolder toUpdate) throws OXException {
        try {
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                throw CMISExceptionCodes.UPDATE_DENIED.create(folderId);
            }
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder folder = (Folder) object;
            // Update properties
            final Map<String, Object> properties = toUpdate.getProperties();
            if (null != properties && !properties.isEmpty()) {
                folder.updateProperties(properties, true);
            }
            // Update ACL
            final List<FileStoragePermission> permissions = toUpdate.getPermissions();
            final CMISEntityMapping mapping = CMISEntityMapping.DEFAULT.get();
            if (null != mapping && null != permissions && !permissions.isEmpty()) {
                cmisSession.getAcl(folderObjectId, true);
                final Acl acl = folder.getAcl();
                if (null != acl) {
                    // Remove existing
                    folder.removeAcl(acl.getAces(), AclPropagation.PROPAGATE);
                    // Apply new ones
                    final List<Ace> aces = new ArrayList<Ace>(permissions.size());
                    for (final FileStoragePermission permission : permissions) {
                        final String cmisId = mapping.getCmisId(permission.getEntity());
                        if (null != cmisId) {
                            final AccessControlEntryImpl ace = new AccessControlEntryImpl();
                            ace.setPrincipal(new AccessControlPrincipalDataImpl(cmisId));
                            if (permission.isAdmin()) {
                                ace.setPermissions(Collections.singletonList("cmis:all"));
                            } else {
                                final List<String> rights = new LinkedList<String>();
                                if (permission.getReadPermission() > FileStoragePermission.NO_PERMISSIONS) {
                                    rights.add("cmis:read");
                                }
                                if (permission.getWritePermission() > FileStoragePermission.NO_PERMISSIONS) {
                                    rights.add("cmis:write");
                                }
                            }
                            aces.add(ace);
                        }
                    }
                    folder.addAcl(aces, AclPropagation.PROPAGATE);
                }
            }
            return folderId;
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        try {
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                throw CMISExceptionCodes.UPDATE_DENIED.create(folderId);
            }
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final ObjectId newParentObjectId;
            if (FileStorageFolder.ROOT_FULLNAME.equals(newParentId) || rootUrl.equals(newParentId)) {
                final CmisObject robject = cmisSession.getRootFolder();
                newParentObjectId = cmisSession.createObjectId(robject.getId());
            } else {
                newParentObjectId = cmisSession.createObjectId(newParentId);
            }
            final Folder folder = (Folder) object;
            final FileableCmisObject result = folder.move(cmisSession.createObjectId(folder.getParentId()), newParentObjectId);
            return result.getId();
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        try {
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                throw CMISExceptionCodes.UPDATE_DENIED.create(folderId);
            }
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder folder = (Folder) object;
            // rename folder
            final Map<String,String> newNameProps = new HashMap<String, String>();
            newNameProps.put(PropertyIds.NAME, newName);
            final CmisObject updated = folder.updateProperties(newNameProps);
            return updated.getId();
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        return deleteFolder(folderId, true);
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        try {
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                throw CMISExceptionCodes.DELETE_DENIED.create(folderId);
            }
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                return folderId;
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder folder = (Folder) object;
            /*
             * Delete
             */
            folder.deleteTree(true, UnfileObject.DELETE, false);
            /*
             * Return
             */
            return folderId;
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        clearFolder(folderId, true);
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        clearFolder0(folderId);
    }

    private void clearFolder0(final String folderId) throws OXException {
        try {
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                throw CMISExceptionCodes.UPDATE_DENIED.create(folderId);
            }
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder folder = (Folder) object;
            final ItemIterable<CmisObject> children = folder.getChildren();
            for (final CmisObject sub : children) {
                if (ObjectType.DOCUMENT_BASETYPE_ID.equals(sub.getType())) {
                    sub.delete(true);
                }
            }
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
            return new FileStorageFolder[0];
        }
        try {
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<FileStorageFolder> list = new ArrayList<FileStorageFolder>();
            FileStorageFolder f = convertFolder(folderObjectId, (Folder) object);
            do {
                list.add(f);
                f = getFolder(f.getParentId());
            } while (!FileStorageFolder.ROOT_FULLNAME.equals(f.getParentId()) && !rootUrl.equals(f.getParentId()));

            return list.toArray(new FileStorageFolder[list.size()]);
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return Quota.getUnlimitedQuota(Quota.Type.STORAGE);
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return Quota.getUnlimitedQuota(Quota.Type.FILE);
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        final Quota[] ret = new Quota[types.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Quota.getUnlimitedQuota(types[i]);
        }
        return ret;
    }

}
