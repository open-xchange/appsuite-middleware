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

package com.openexchange.share.impl.groupware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.java.Strings;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.groupware.AbstractTargetProxy;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxyType;


/**
 * {@link FileTargetProxy}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FileTargetProxy extends AbstractTargetProxy {

    private File file;
    private TargetProxyType proxyType;
    private final ShareTarget target;
    private final ShareTargetPath targetPath;

    /**
     * Initializes a new {@link FileTargetProxy}.
     *
     * @param file The file
     */
    public FileTargetProxy(File file) {
        this(file, new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId()));
    }

    /**
     * Initializes a new {@link FileTargetProxy}. The share target returned by {@link #getTarget()} will
     * be overridden by the passed one.
     *
     * @param file The file
     * @param target The target
     */
    public FileTargetProxy(File file, ShareTarget target) {
        super();
        this.file = file;
        if (file instanceof UserizedFile) {
            UserizedFile uFile = (UserizedFile) file;
            targetPath = new ShareTargetPath(FolderObject.INFOSTORE, uFile.getOriginalFolderId(), uFile.getOriginalId());
        } else {
            targetPath = new ShareTargetPath(FolderObject.INFOSTORE, file.getFolderId(), file.getId());
        }
        this.target = target;
    }

    @Override
    public String getID() {
        return target.getItem();
    }

    @Override
    public String getFolderID() {
        return target.getFolder();
    }

    @Override
    public ShareTarget getTarget() {
        return target;
    }

    @Override
    public ShareTargetPath getTargetPath() {
        return targetPath;
    }

    @Override
    public String getTitle() {
        String title = file.getFileName();
        return Strings.isNotEmpty(title) ? title : file.getTitle();
    }

    @Override
    public List<TargetPermission> getPermissions() {
        List<FileStorageObjectPermission> permissions = file.getObjectPermissions();
        if (null == permissions) {
            return Collections.emptyList();
        }
        List<TargetPermission> targetPermissions = new ArrayList<TargetPermission>(permissions.size());
        for (FileStorageObjectPermission permission : permissions) {
            targetPermissions.add(CONVERTER.convert(permission));
        }
        return targetPermissions;
    }

    @Override
    public void applyPermissions(List<TargetPermission> permissions) {
        file = new DefaultFile(file);
        file.setObjectPermissions(mergePermissions(file.getObjectPermissions(), permissions, CONVERTER));
        setModified();
    }

    @Override
    public void removePermissions(List<TargetPermission> permissions) {
        file = new DefaultFile(file);
        file.setObjectPermissions(removePermissions(file.getObjectPermissions(), permissions, CONVERTER));
        setModified();
    }

    public File getFile() {
        return file;
    }

    @Override
    public TargetProxyType getProxyType() {
        if (proxyType == null) {
            proxyType = FileTargetProxyTypeAnalyzer.analyzeType(file);
        }
        return proxyType;
    }

    @Override
    public boolean mayAdjust() {
        return file.isShareable();
    }

    @Override
    public Date getTimestamp() {
        return new Date(file.getSequenceNumber());
    }

    private static final PermissionConverter<FileStorageObjectPermission> CONVERTER = new PermissionConverter<FileStorageObjectPermission>() {

        @Override
        public int getEntity(FileStorageObjectPermission permission) {
            return permission.getEntity();
        }

        @Override
        public boolean isGroup(FileStorageObjectPermission permission) {
            return permission.isGroup();
        }

        @Override
        public boolean isSystem(FileStorageObjectPermission permission) {
            return false;
        }

        @Override
        public int getBits(FileStorageObjectPermission permission) {
            return ObjectPermission.toFolderPermissionBits(new ObjectPermission(permission.getEntity(), permission.isGroup(), permission.getPermissions()));
        }

        @Override
        public FileStorageObjectPermission convert(TargetPermission permission) {
            return new DefaultFileStorageObjectPermission(permission.getEntity(), permission.isGroup(), ObjectPermission.convertFolderPermissionBits(permission.getBits()));
        }

        @Override
        public TargetPermission convert(FileStorageObjectPermission permission) {
            return new TargetPermission(permission.getEntity(), permission.isGroup(), getBits(permission));
        }

    };

}