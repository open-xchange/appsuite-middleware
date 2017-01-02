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
        return new SaveFileAction(getFileStorage(contextId), file, -1, false);
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
        } catch (final OXException e) {
            throw VCardStorageExceptionCodes.FILESTORE_DOWN.create(e);
        }
    }
}
