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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.owncloud;

import java.net.MalformedURLException;
import java.net.URL;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.file.storage.webdav.AbstractWebDAVFolderAccess;
import com.openexchange.file.storage.webdav.WebDAVFileStorageConstants;
import com.openexchange.webdav.client.WebDAVClient;

/**
 * {@link OwnCloudFolderAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class OwnCloudFolderAccess extends AbstractWebDAVFolderAccess implements UserCreatedFileStorageFolderAccess {

    private final String rootUrl;

    /**
     * Initializes a new {@link NextCloudFolderAccessa}.
     */
    protected OwnCloudFolderAccess(@NonNull WebDAVClient webdavClient, @NonNull OwnCloudAccountAccess accountAccess) throws OXException {
        super(webdavClient, accountAccess);
        if (account.getConfiguration().containsKey(WebDAVFileStorageConstants.WEBDAV_URL)) {
            rootUrl = (String) account.getConfiguration().get(WebDAVFileStorageConstants.WEBDAV_URL);
        } else {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(WebDAVFileStorageConstants.ID, WebDAVFileStorageConstants.WEBDAV_URL);
        }
    }

    /**
     * Gets the path to the root folder
     *
     * @return The path to the root folder
     * @throws OXException in case the root url is invalid
     */
    public String getRootFolderId() throws OXException {
        try {
            return new URL(rootUrl).getPath();
        } catch (MalformedURLException e) {
            throw FileStorageExceptionCodes.INVALID_URL.create(e, e.getMessage());
        }
    }

}
