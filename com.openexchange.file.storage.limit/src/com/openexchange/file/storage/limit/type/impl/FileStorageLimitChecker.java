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

package com.openexchange.file.storage.limit.type.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.limit.LimitFile;
import com.openexchange.file.storage.limit.exceptions.FileLimitExceptionCodes;
import com.openexchange.file.storage.limit.osgi.Services;
import com.openexchange.groupware.infostore.InfostoreConfig;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.session.Session;

/**
 * {@link FileStorageLimitChecker}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class FileStorageLimitChecker extends AbstractCombinedTypeLimitChecker {

    @Override
    public String getType() {
        return "filestorage";
    }

    @Override
    public List<OXException> check(Session session, String folderId, List<LimitFile> files) throws OXException {
        IDBasedFolderAccessFactory folderFactory = Services.getService(IDBasedFolderAccessFactory.class, true);
        IDBasedFolderAccess folderAccess = folderFactory.createAccess(session);
        if (!folderAccess.exists(folderId)) {
            throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
        }

        return getExceededLimits(folderId, files, folderAccess);
    }

    private List<OXException> getExceededLimits(String folderId, List<LimitFile> files, IDBasedFolderAccess folderAccess) throws OXException {
        List<OXException> exceededLimits = new ArrayList<>();
        Quota fileQuota = folderAccess.getFileQuota(folderId);
        if (fileQuota != null && fileQuota.getLimit() > 0 && fileQuota.getUsage() + files.size() > fileQuota.getLimit()) {
            exceededLimits.add(FileLimitExceptionCodes.TOO_MANY_FILES.create(fileQuota.getLimit()));
        }

        Quota storageQuota = folderAccess.getStorageQuota(folderId);
        long fileTotalSize = files.stream().collect(Collectors.summingLong(LimitFile::getSize));
        if (storageQuota != null) {
            if (storageQuota.getLimit() == 0) {
                exceededLimits.add(FileLimitExceptionCodes.NOT_ALLOWED.create(folderId));
            } else if (storageQuota.getLimit() > 0 && storageQuota.getUsage() + fileTotalSize > storageQuota.getLimit()) {
                exceededLimits.add(FileLimitExceptionCodes.STORAGE_QUOTA_EXCEEDED.create(UploadUtility.getSize(fileTotalSize, 2, false, true), UploadUtility.getSize(storageQuota.getLimit() - storageQuota.getUsage(), 2, false, true)));
            }
        }

        List<OXException> checkMaxUploadSizePerFile = checkMaxUploadSizePerFile(files);
        if (!checkMaxUploadSizePerFile.isEmpty()) {
            exceededLimits.addAll(checkMaxUploadSizePerFile);
        }
        return exceededLimits;
    }

    @Override
    protected long getMaxUploadSizePerModule() {
        return InfostoreConfig.getMaxUploadSize();
    }
}
