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

package com.openexchange.filestore;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.Quota;

/**
 * {@link FileQuotaCheckService}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public interface FileQuotaCheckService {

    /**
     * Performs a full check for all quotas
     *
     * @param userId The userId.
     * @param contextId The contextId.
     * @param quotaFiles The uploaded files to check.
     * @param fileQuota The file quota.
     * @param storageQuota The storage quota.
     * @param accumulatedFileQuota The overall used file quota of the file upload.
     * @param accumulatedStorageQuota The overall used storage quota of the file upload.
     * @throws OXException If the quota check fails.
     */
    boolean completeQuotaCheck(int userId, int contextId, List<File> quotaFiles, Quota fileQuota, Quota storageQuota, long accumulatedFileQuota, long accumulatedStorageQuota) throws OXException;

    /**
     * Checks, if the upload violates the current storage or file quota
     *
     * @param limit The current limit of the storage or file quota.
     * @param usage The current usage of the storage or file quota.
     * @param accumulatedStorageQuota The overall used storage quota of the file upload.
     * @return boolean true, if the quota check was successful.
     * @throws OXException If the quota check fails.
     */
    boolean checkQuota(long limit, long usage, long accumulatedStorageQuota) throws OXException;

    /**
     * Checks, if the upload violates the max upload size
     *
     * @param accumulatedStorageQuota The overall used file quota of the file upload.
     * @return boolean true, if the quota check was successful.
     * @throws OXException If the quota check fails.
     */
    boolean checkFileMaxUploadSize(long accumulatedStorageQuota) throws OXException;

    /**
     * Checks, if the upload violates the current storage quota of the specific user
     *
     * @param userId The userId.
     * @param contextId The  contextId.
     * @param accumulatedStorageQuota The overall used file quota of the file upload.
     * @return boolean true, if the quota check was successful.
     * @throws OXException If the quota check fails.
     */
    boolean checkStorageQuotaPerUser(int userId, int contextId, long accumulatedStorageQuota) throws OXException;

}
