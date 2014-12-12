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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.QuotaFileStorageService;


/**
 * {@link FileStorages} - Utility class for file storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class FileStorages {

    /**
     * Initializes a new {@link FileStorages}.
     */
    private FileStorages() {
        super();
    }

    // -------------------------------------------------------------------------------------------------------------------------

    private static final AtomicReference<QuotaFileStorageService> QFS_REF = new AtomicReference<QuotaFileStorageService>();

    /**
     * Sets the quota-aware file storage service.
     *
     * @param qfsService The quota-aware file storage service
     */
    public static void setQuotaFileStorageService(QuotaFileStorageService qfsService) {
        QFS_REF.set(qfsService);
    }

    /**
     * Gets the quota-aware file storage service
     *
     * @return The quota-aware file storage service or <code>null</code> if absent
     */
    public static QuotaFileStorageService getQuotaFileStorageService() {
        return QFS_REF.get();
    }

    // -------------------------------------------------------------------------------------------------------------------------

    private static final AtomicReference<FileStorageService> FS_REF = new AtomicReference<FileStorageService>();

    /**
     * Sets the file storage service.
     *
     * @param fsService The file storage service
     */
    public static void setFileStorageService(FileStorageService fsService) {
        FS_REF.set(fsService);
    }

    /**
     * Gets the file storage service
     *
     * @return The file storage service or <code>null</code> if absent
     */
    public static FileStorageService getFileStorageService() {
        return FS_REF.get();
    }

    // -------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the context-related appendix for a file storage's base URI.
     *
     * @param contextId The context identifier
     * @return The context-related appendix for a file storage's base URI
     */
    public static String getContextAppendix(int contextId) {
        return new StringBuilder(16).append(contextId).append("_ctx_store").toString();
    }

    /**
     * Gets the user-related appendix for a file storage's base URI.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The user-related appendix for a file storage's base URI
     */
    public static String getUserAppendix(int userId, int contextId) {
        return new StringBuilder(16).append(contextId).append("_ctx_").append(userId).append("_user_store").toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the fully qualifying URI for given context.
     *
     * @param contextId The context identifier
     * @param baseUri The file storage's base URI
     * @return The fully qualifying URI
     */
    public static URI getFullyQualifyingUriForContext(int contextId, URI baseUri) {
        try {
            return new URI(baseUri.getScheme(), baseUri.getAuthority(), baseUri.getPath() + '/' + getContextAppendix(contextId), baseUri.getQuery(), baseUri.getFragment());
        } catch (URISyntaxException e) {
            // Cannot occur
            return null;
        }
    }

    /**
     * Gets the fully qualifying URI for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param baseUri The file storage's base URI
     * @return The fully qualifying URI
     */
    public static URI getFullyQualifyingUriForUser(int userId, int contextId, URI baseUri) {
        try {
            return new URI(baseUri.getScheme(), baseUri.getAuthority(), baseUri.getPath() + '/' + getUserAppendix(userId, contextId), baseUri.getQuery(), baseUri.getFragment());
        } catch (URISyntaxException e) {
            // Cannot occur
            return null;
        }
    }

}
