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

package com.openexchange.filestore.limit.type.impl;

import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.filestore.limit.exceptions.LimitExceptionCodes;
import com.openexchange.filestore.limit.type.TypeLimitService;
import com.openexchange.groupware.upload.impl.UploadUtility;

/**
 * {@link AbstractCombinedTypeLimitService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public abstract class AbstractCombinedTypeLimitService implements TypeLimitService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCombinedTypeLimitService.class);

    /**
     * Checks the upload size limit per file for the given {@link List} and adds exceeded limits to the provided {@link List} of {@link OXException}s
     * 
     * @param files The files to check
     * @param exceededLimits {@link List} containing already exceeded limits. Exceeded limits for the current type will be added
     */
    protected void checkMaxUploadSizePerFile(List<File> files, List<OXException> exceededLimits) {
        if (files == null || files.isEmpty()) {
            return; 
        }
        if (exceededLimits == null) {
            LOG.warn("Provided list of exceptions is null. Return without checking upload size per file.");
            return;
        }
        long maxUploadSize = getMaxUploadSize();
        if (maxUploadSize > 0) {
            files.stream().filter(file -> (file.getFileSize() > maxUploadSize)).forEach(file -> exceededLimits.add(LimitExceptionCodes.FILE_QUOTA_PER_REQUEST_EXCEEDED.create(file.getFileName(), UploadUtility.getSize(file.getFileSize(), 2, false, true), UploadUtility.getSize(maxUploadSize, 2, false, true))));
        }
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
            result = ServerConfig.getLong(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        } catch (OXException e) {
            result = 0l;
            LOG.error("", e);
        }
        return result;
    }
}
