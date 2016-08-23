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

package com.openexchange.file.storage.dropbox;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.WebAuthSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.dropbox.access.DropboxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractDropboxAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractDropboxAccess {

    /** Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource. */
    protected static final int SC_CONFLICT = 409;

    protected final DropboxOAuthAccess dropboxOAuthAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected final DropboxAPI<WebAuthSession> dropboxAPI;

    /**
     * Initialises a new {@link AbstractDropboxAccess}.
     *
     * @throws OXException
     */
    @SuppressWarnings("unchecked")
    protected AbstractDropboxAccess(final DropboxOAuthAccess dropboxOAuthAccess, final FileStorageAccount account, final Session session) throws OXException {
        super();
        this.dropboxOAuthAccess = dropboxOAuthAccess;
        this.account = account;
        this.session = session;
        // Other fields
        this.dropboxAPI = dropboxOAuthAccess.<DropboxAPI<WebAuthSession>> getClient().client;
    }

    /**
     * Handles given server error.
     *
     * @param id The file/folder identifier
     * @param e The server exception
     * @return The handled exception
     */
    protected OXException handleServerError(final String id, final DropboxServerException e) {
        if (null != id && 404 == e.error) {
            return FileStorageExceptionCodes.NOT_FOUND.create(e, DropboxConstants.ID, id);
        }
        com.dropbox.client2.exception.DropboxServerException.Error body = e.body;
        int error = e.error;
        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(error), null == body.userError ? body.error : body.userError);
    }

    /**
     * Gets the (dropbox-)path for specified folder identifier.
     *
     * @param folderId The folder identifier
     * @return The associated path
     */
    protected static String toPath(final String folderId) {
        if (null == folderId) {
            return null;
        }
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? "/" : folderId;
    }

    /**
     * Gets the (dropbox-)path for the specified folder- and file-identifier.
     *
     * @param folderId The folder identifier
     * @param fileId The file identifier
     * @return The associated path
     */
    protected static String toPath(String folderId, String fileId) {
        String parentPath = toPath(folderId);
        return parentPath.endsWith("/") ? parentPath + fileId : parentPath + '/' + fileId;
    }

    /**
     * Gets the identifier for specified path.
     *
     * @param path The path
     * @return The associated identifier
     */
    protected static String toId(final String path) {
        if (null == path) {
            return null;
        }
        return "/".equals(path) ? FileStorageFolder.ROOT_FULLNAME : path;
    }

    /**
     * URL-encodes specified string.
     *
     * @param string The string
     * @return The URL-encoded string
     */
    protected static String encode(final String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return string;
        }
    }

}
