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

package com.openexchange.contact.vcard.storage.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;
import com.openexchange.contact.vcard.storage.VCardStorageExceptionCodes;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.java.Strings;
import com.openexchange.tools.file.SaveFileAction;

/**
 * {@link DefaultVCardStorageService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultVCardStorageService implements VCardStorageService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultVCardStorageService.class);

    /**
     * Initializes a new {@link DefaultVCardStorageService}.
     */
    public DefaultVCardStorageService() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String saveVCard(InputStream file, int contextId) throws OXException {
        Validate.notNull(file, "VCard InputStream might not be null!");

        SaveFileAction action = createFileAction(file, contextId);
        action.perform();
        String fileStorageID = action.getFileStorageID();
        return fileStorageID;
    }

    protected SaveFileAction createFileAction(InputStream file,int contextId) throws OXException {
        return new SaveFileAction(getFileStorage(contextId), file, -1, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getVCard(String identifier, int contextId) throws OXException {
        try {
            if (Strings.isEmpty(identifier)) {
                LOG.warn("Identifier to get VCard for is null. Cannot return VCard.");
                return null;
            }

            QuotaFileStorage fileStorage = getFileStorage(contextId);
            InputStream vCard = fileStorage.getFile(identifier);
            return vCard;
        } catch (OXException e) {
            if (FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                LOG.error(e.getMessage());
                return null;
            }
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteVCard(String identifier, int contextId) throws OXException {
        if (Strings.isEmpty(identifier)) {
            LOG.warn("Identifier for removing stored VCard not available.");
            return false;
        }

        QuotaFileStorage fileStorage = getFileStorage(contextId);
        return fileStorage.deleteFile(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> neededCapabilities() {
        return Collections.singletonList("com.openexchange.capability.filestore");
    }

    /**
     * Returns the {@link FileStorage} assigned to the given context
     *
     * @param contextId the context identifier to return {@link FileStorage} for
     * @return {@link FileStorage} for the given context
     * @throws OXException
     */
    protected QuotaFileStorage getFileStorage(int contextId) throws OXException {
        try {
            QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
            if (null == storageService) {
                throw VCardStorageExceptionCodes.FILESTORE_DOWN.create();
            }
            return storageService.getQuotaFileStorage(contextId, Info.general());
        } catch (OXException e) {
            throw VCardStorageExceptionCodes.FILESTORE_DOWN.create(e);
        }
    }
}
