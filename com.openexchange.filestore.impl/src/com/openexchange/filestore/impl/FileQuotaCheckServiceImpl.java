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

package com.openexchange.filestore.impl;

import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileQuotaCheckExceptionCodes;
import com.openexchange.file.storage.Quota;
import com.openexchange.filestore.FileQuotaCheckService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.infostore.InfostoreConfig;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link FileQuotaCheckServiceImpl}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public class FileQuotaCheckServiceImpl implements FileQuotaCheckService {

    /**
     * Initializes a new {@link FileQuotaCheckServiceImpl}.
     */
    public FileQuotaCheckServiceImpl() {
    }

    @Override
    public boolean completeQuotaCheck(int userId, int contextId, List<File> quotaFiles, Quota fileQuota, Quota storageQuota, long accumulatedFileQuota, long accumulatedStorageQuota) throws OXException {
        if (!calculateFileMaxUploadSize(accumulatedStorageQuota)) {
            throw FileQuotaCheckExceptionCodes.MAX_UPLOAD_CHECK_MSG.create();
        }
        if (!calculateQuotaPerUser(userId, contextId, accumulatedStorageQuota)
            || !calculateQuota(fileQuota.getLimit(), fileQuota.getUsage(), accumulatedFileQuota)
            || !calculateQuota(storageQuota.getLimit(), storageQuota.getUsage(), accumulatedStorageQuota)) {
            throw FileQuotaCheckExceptionCodes.QUOTA_CHECK_MSG.create();
        }
        return true;
    }

    @Override
    public boolean checkQuota(long limit, long usage, long accumulatedStorageQuota) throws OXException {
        return calculateQuota(limit, usage, accumulatedStorageQuota);
    }

    @Override
    public boolean checkFileMaxUploadSize(long accumulatedStorageQuota) throws OXException {
        return calculateFileMaxUploadSize(accumulatedStorageQuota);
    }

    @Override
    public boolean checkStorageQuotaPerUser(int userId, int contextId, long accumulatedStorageQuota) throws OXException {
        return calculateQuotaPerUser(userId, contextId, accumulatedStorageQuota);
    }

    private boolean calculateQuotaPerUser(int userId, int contextId, long accumulatedStorageQuota) throws OXException {
        QuotaFileStorage storage = getFileStorage(userId, contextId);
        return calculateQuota(storage.getQuota(), storage.getUsage(), accumulatedStorageQuota);
    }

    /**
     * Compares the quota of all uploaded files with the max upload size
     *
     * @param accumulatedStorageQuota The quota of all uploaded files.
     * @return boolean, true if the check passed.
     */
    private boolean calculateFileMaxUploadSize(long accumulatedStorageQuota) {
        long tmpMaxUploadSize = InfostoreConfig.getMaxUploadSize();
        if (0 > tmpMaxUploadSize) {
            tmpMaxUploadSize = sysconfMaxUpload();
            if (0 > tmpMaxUploadSize) {
                return true;
            }
        }
        return (tmpMaxUploadSize > accumulatedStorageQuota) || (0 == tmpMaxUploadSize) ? true : false;
    }

    /**
     * Compares the quota of all uploaded files with the actual quota
     *
     * @param limit The limit of the dedicated quota.
     * @param usage The actual usage of the dedicated quota.
     * @param accumulatedQuota The quota of all uploaded files.
     * @return
     */
    private boolean calculateQuota(long limit, long usage, long accumulatedQuota) {
        if (0 < limit) {
            if ((usage + accumulatedQuota) > limit) {
                return false;
            }
        }
        return true;
    }

    private long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if (null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

    private QuotaFileStorage getFileStorage(int userId, int contextId) throws OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }
        return storageService.getQuotaFileStorage(userId, contextId, Info.drive());
    }

}
