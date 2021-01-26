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

package com.openexchange.folder.json;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folder.json.writer.FolderWriter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderPath;
import com.openexchange.folderstorage.ImmutablePermission;
import com.openexchange.folderstorage.ImmutablePermission.Builder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.UserizedFolderImpl;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link FolderConverter}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class FolderConverter implements ResultConverter {

    public FolderConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "folder";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object resultObject = result.getResultObject();
        if (!(resultObject instanceof FileStorageFolder)) {
            throw new UnsupportedOperationException("Unknown result object");
        }

        FileStorageFolder fileStorageFolder = (FileStorageFolder) resultObject;
        UserizedFolder userizedFolder;
        {
            Object optDelegate = fileStorageFolder.getDelegate();
            if (optDelegate instanceof UserizedFolder) {
                userizedFolder = (UserizedFolder) optDelegate;
            } else {
                userizedFolder = new UserizedFolderImpl(convertFileStorageFolder2FolderstorageFolder(fileStorageFolder), session, session.getUser(), session.getContext());
            }
        }
        resultObject = FolderWriter.writeSingle2Object(requestData, null, userizedFolder, Constants.ADDITIONAL_FOLDER_FIELD_LIST);
        result.setResultObject(resultObject);
    }

    private Folder convertFileStorageFolder2FolderstorageFolder(FileStorageFolder fileStorageFolder) {
        return new FileStorageFolderImplementation(fileStorageFolder);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class FileStorageFolderImplementation implements Folder {

        private static final long serialVersionUID = 8565331942511382772L;

        private transient final FileStorageFolder fileStorageFolder;

        /**
         * Initializes a new {@link FileStorageFolderImplementation}.
         *
         * @param fileStorageFolder The file storage folder used as delegate
         */
        FileStorageFolderImplementation(FileStorageFolder fileStorageFolder) {
            this.fileStorageFolder = fileStorageFolder;
        }

        @Override
        public void setUnread(int unread) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setType(Type type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTreeID(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTotal(int total) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSupportedCapabilities(Set<String> capabilities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSummary(String summary) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSubscribedSubfolders(boolean subscribedSubfolders) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSubscribed(boolean subscribed) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSubfolderIDs(String[] subfolderIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPermissions(Permission[] permissions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setParentID(String parentId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginPath(FolderPath originPath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setNewID(String newId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setNew(int nu) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setModifiedBy(int modifiedBy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMeta(Map<String, Object> meta) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLastModified(Date lastModified) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setID(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDeleted(int deleted) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDefaultType(int defaultType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDefault(boolean deefault) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCreationDate(Date creationDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCreatedBy(int createdBy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setContentType(ContentType contentType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCapabilities(int capabilities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBits(int bits) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAccountID(String accountId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public boolean isSubscribed() {
            return fileStorageFolder.isSubscribed();
        }

        @Override
        public boolean isGlobalID() {
            return true;
        }

        @Override
        public boolean isDefault() {
            return fileStorageFolder.isDefaultFolder();
        }

        @Override
        public boolean isCacheable() {
            return false;
        }

        @Override
        public boolean hasSubscribedSubfolders() {
            return fileStorageFolder.hasSubscribedSubfolders();
        }

        @Override
        public int getUnread() {
            return 0;
        }

        @Override
        public Type getType() {
            return PrivateType.getInstance();
        }

        @Override
        public String getTreeID() {
            return "0";
        }

        @Override
        public int getTotal() {
            return fileStorageFolder.getFileCount();
        }

        @Override
        public Set<String> getSupportedCapabilities() {
            return fileStorageFolder.getCapabilities();
        }

        @Override
        public String getSummary() {
            return null;
        }

        @Override
        public String[] getSubfolderIDs() {
            return new String[0];
        }

        @Override
        public Permission[] getPermissions() {
            List<FileStoragePermission> permissions = fileStorageFolder.getPermissions();
            Permission[] p = new Permission[permissions.size()];
            for (int i = 0; i < permissions.size(); i++) {
                FileStoragePermission fp = permissions.get(i);
                Builder builder = ImmutablePermission.builder();
                builder.setAdmin(fp.isAdmin())
                .setDeletePermission(fp.getDeletePermission())
                .setEntity(fp.getEntity())
                .setFolderPermission(fp.getFolderPermission())
                .setFolderPermissionType(fp.getPermissionLegator())
                .setGroup(fp.isGroup())
                .setReadPermission(fp.getReadPermission())
                .setSystem(0)
                .setWritePermission(fp.getWritePermission());
                p[i] = builder.build();
            }
            return p;
        }

        @Override
        public String getParentID() {
            return fileStorageFolder.getParentId();
        }

        @Override
        public FolderPath getOriginPath() {
            return null;
        }

        @Override
        public String getNewID() {
            return null;
        }

        @Override
        public int getNew() {
            return 0;
        }

        @Override
        public String getName() {
            return fileStorageFolder.getName();
        }

        @Override
        public int getModifiedBy() {
            return fileStorageFolder.getModifiedBy();
        }

        @Override
        public Map<String, Object> getMeta() {
            return fileStorageFolder.getMeta();
        }

        @Override
        public String getLocalizedName(Locale locale) {
            return fileStorageFolder.getLocalizedName(locale);
        }

        @Override
        public Date getLastModified() {
            return fileStorageFolder.getLastModifiedDate();
        }

        @Override
        public String getID() {
            return fileStorageFolder.getId();
        }

        @Override
        public int getDeleted() {
            return 0;
        }

        @Override
        public int getDefaultType() {
            return 0;
        }

        @Override
        public Date getCreationDate() {
            return fileStorageFolder.getCreationDate();
        }

        @Override
        public int getCreatedBy() {
            return fileStorageFolder.getCreatedBy();
        }

        @Override
        public ContentType getContentType() {
            return InfostoreContentType.getInstance();
        }

        @Override
        public int getCapabilities() {
            return -1;
        }

        @Override
        public int getBits() {
            FileStoragePermission perm = fileStorageFolder.getOwnPermission();
            return Permissions.createPermissionBits(perm.getFolderPermission(), perm.getReadPermission(), perm.getWritePermission(), perm.getDeletePermission(), perm.isAdmin());
        }

        @Override
        public String getAccountID() {
            return null;
        }

        @Override
        public Folder clone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityInfo getCreatedFrom() {
            return fileStorageFolder.getCreatedFrom();
        }

        @Override
        public void setCreatedFrom(EntityInfo createdFrom) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityInfo getModifiedFrom() {
            return fileStorageFolder.getModifiedFrom();
        }

        @Override
        public void setModifiedFrom(EntityInfo modifiedFrom) {
            throw new UnsupportedOperationException();
        }
    } // End of class FileStorageFolderImplementation

}
