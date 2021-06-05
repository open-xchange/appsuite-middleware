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

package com.openexchange.file.storage.limit.type.impl;

import static com.openexchange.java.Autoboxing.L;
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
            exceededLimits.add(FileLimitExceptionCodes.TOO_MANY_FILES.create(L(fileQuota.getLimit())));
        }

        Quota storageQuota = folderAccess.getStorageQuota(folderId);
        long fileTotalSize = files.stream().collect(Collectors.summingLong(LimitFile::getSize)).longValue();
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
