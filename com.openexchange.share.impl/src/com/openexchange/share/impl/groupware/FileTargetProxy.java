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