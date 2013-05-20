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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link DropboxFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxFile extends DefaultFile {

    /**
     * Initializes a new {@link DropboxFile}.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     */
    public DropboxFile(final String folderId, final String id, final int userId) {
        super();
        setFolderId("/".equals(folderId) ? FileStorageFolder.ROOT_FULLNAME : folderId);
        setCreatedBy(userId);
        setModifiedBy(userId);
        setId(id);
        setFileName(id);
        setVersion(FileStorageFileAccess.CURRENT_VERSION);
        setIsCurrentVersion(true);
    }

    @Override
    public String toString() {
        final String url = getURL();
        return url == null ? super.toString() : url;
    }

    /**
     * Parses specified Dropbox file entry.
     *
     * @param entry The Dropbox file entry
     * @throws OXException If parsing SMB file fails
     * @return This Dropbox file
     */
    public DropboxFile parseDropboxFile(final Entry entry) throws OXException {
        return parseDropboxFile(entry, null);
    }

    /**
     * Parses specified Dropbox file entry.
     *
     * @param entry The Dropbox file entry
     * @param fields The fields to consider
     * @throws OXException If parsing SMB file fails
     * @return This Dropbox file with property set applied
     */
    public DropboxFile parseDropboxFile(final Entry entry, final List<Field> fields) throws OXException {
        if (null != entry && !entry.isDir) {
            try {
                setId(entry.path);
                {
                    final String p = entry.parentPath();
                    if ("/".equals(p)) {
                        setFolderId(FileStorageFolder.ROOT_FULLNAME);
                    } else {
                        setFolderId(p);
                    }
                }
                final String name = entry.fileName();
                setTitle(name);
                setFileName(name);
                setVersion(entry.rev);
                final Set<Field> set = null == fields || fields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(fields);
                {
                    final Date date = RESTUtility.parseDate(entry.modified);
                    if (set.contains(Field.CREATED)) {
                        setCreated(new Date(date.getTime()));
                    }
                    if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
                        setLastModified(new Date(date.getTime()));
                    }
                }
                if (set.contains(Field.FILE_MIMETYPE)) {
                    String contentType = entry.mimeType;
                    if (isEmpty(contentType)) {
                        final MimeTypeMap map = DropboxServices.getService(MimeTypeMap.class);
                        contentType = map.getContentType(name);
                    }
                    setFileMIMEType(contentType);
                }
                if (set.contains(Field.FILE_SIZE)) {
                    setFileSize(entry.bytes);
                }
                if (set.contains(Field.URL)) {
                    setURL(entry.path);
                }
                if (set.contains(Field.COLOR_LABEL)) {
                    setColorLabel(0);
                }
                if (set.contains(Field.CATEGORIES)) {
                    setCategories(null);
                }
                if (set.contains(Field.DESCRIPTION)) {
                    setDescription(null);
                }
                if (set.contains(Field.VERSION_COMMENT)) {
                    setVersionComment(null);
                }
            } catch (final RuntimeException e) {
                throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }
}
