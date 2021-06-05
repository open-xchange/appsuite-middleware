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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.LimitFile;
import com.openexchange.file.storage.limit.exceptions.FileLimitExceptionCodes;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.session.Session;

/**
 * {@link PIMLimitChecker}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class PIMLimitChecker extends AbstractCombinedTypeLimitChecker {

    @Override
    public String getType() {
        return "pim";
    }

    @Override
    public List<OXException> check(Session session, String folderId, List<LimitFile> files) throws OXException {
        List<OXException> exceededLimits = new ArrayList<>();

        long fileTotalSize = files.stream().collect(Collectors.summingLong(LimitFile::getSize)).longValue();
        QuotaFileStorage fileStorage = getFileStorage(session.getContextId());
        if (fileStorage != null) {
            long quota = fileStorage.getQuota();
            if (quota == 0) {
                exceededLimits.add(FileLimitExceptionCodes.NOT_ALLOWED.create(folderId));
            } else if (quota > 0 && fileStorage.getUsage() + fileTotalSize > quota) {
                exceededLimits.add(FileLimitExceptionCodes.STORAGE_QUOTA_EXCEEDED.create(UploadUtility.getSize(fileTotalSize, 2, false, true), UploadUtility.getSize(quota - fileStorage.getUsage(), 2, false, true)));
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
        return AttachmentConfig.getMaxUploadSize();
    }

    private QuotaFileStorage getFileStorage(int contextId) throws OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            throw AttachmentExceptionCodes.FILESTORE_DOWN.create();
        }
        return storageService.getQuotaFileStorage(contextId, Info.general());
    }
}
