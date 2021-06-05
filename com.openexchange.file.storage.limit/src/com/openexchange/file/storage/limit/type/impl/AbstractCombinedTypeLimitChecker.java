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
import java.util.Collections;
import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.LimitFile;
import com.openexchange.file.storage.limit.exceptions.FileLimitExceptionCodes;
import com.openexchange.file.storage.limit.type.TypeLimitChecker;
import com.openexchange.groupware.upload.impl.UploadUtility;

/**
 * {@link AbstractCombinedTypeLimitChecker}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public abstract class AbstractCombinedTypeLimitChecker implements TypeLimitChecker {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCombinedTypeLimitChecker.class);

    /**
     * Checks the upload size limit per file for the given {@link List} and return exceeded limits.
     *
     * @param files The files to check
     * @return {@link List} containing already exceeded limits.
     */
    protected List<OXException> checkMaxUploadSizePerFile(List<LimitFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        List<OXException> exceededLimits = new ArrayList<>();
        long maxUploadSize = getMaxUploadSize();
        if (maxUploadSize > 0) {
            files.stream().filter(file -> (file.getSize() > maxUploadSize)).forEach(file -> exceededLimits.add(FileLimitExceptionCodes.FILE_QUOTA_PER_REQUEST_EXCEEDED.create(file.getName(), UploadUtility.getSize(file.getSize(), 2, false, true), UploadUtility.getSize(maxUploadSize, 2, false, true))));
        }
        return exceededLimits;
    }

    /**
     * Returns the configured upload size limit based on the given module. If the module configuration is set to <0 the server configuration will be used.
     *
     * @see #getMaxUploadSizePerModule()
     * @return long with the upload size limit
     */
    protected long getMaxUploadSize() {
        long maxUploadSizePerModule = getMaxUploadSizePerModule();
        if (maxUploadSizePerModule < 0) {
            maxUploadSizePerModule = getServerMaxUploadSize();
        }
        return maxUploadSizePerModule;

    }

    /**
     * Returns the configured upload size limit for the current module.
     *
     * @return long with the upload size limit for the current module
     */
    protected abstract long getMaxUploadSizePerModule();

    protected long getServerMaxUploadSize() {
        long result;
        try {
            result = ServerConfig.getLong(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE).longValue();
        } catch (OXException e) {
            result = 0l;
            LOG.error("", e);
        }
        return result;
    }
}
