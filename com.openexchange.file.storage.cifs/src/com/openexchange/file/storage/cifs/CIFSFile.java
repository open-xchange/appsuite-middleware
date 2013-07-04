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

package com.openexchange.file.storage.cifs;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link CIFSFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSFile extends DefaultFile {

    private static final String URL_SPEC = CIFSConstants.URL_SPEC;

    /**
     * Initializes a new {@link CIFSFile}.
     *
     * @param folderId The folder identifier; e.g. "smb://my.smb-server.net/d/files/"
     * @param id The file identifier; e.g. "document.pdf"
     * @param userId The user identifier
     */
    public CIFSFile(final String folderId, final String id, final int userId) {
        super();
        setFolderId(folderId);
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
     * Parses specified SMB file.
     *
     * @param smbFile The SMB file
     * @throws OXException If parsing SMB file fails
     * @return This CIFS file
     */
    public CIFSFile parseSmbFile(final SmbFile smbFile) throws OXException {
        return parseSmbFile(smbFile, null);
    }

    /**
     * Parses specified SMB file.
     *
     * @param smbFile The SMB file
     * @param fields The fields to consider
     * @throws OXException If parsing SMB file fails
     * @return This CIFS file with property set applied
     */
    public CIFSFile parseSmbFile(final SmbFile smbFile, final List<Field> fields) throws OXException {
        if (null != smbFile) {
            try {
                {
                    final String p = smbFile.getParent();
                    if (URL_SPEC.equals(p)) {
                        setFolderId(FileStorageFolder.ROOT_FULLNAME);
                    } else {
                        setFolderId(Utils.checkFolderId(p));
                    }
                }
                final Set<Field> set = null == fields || fields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(fields);
                if (set.contains(Field.CREATED)) {
                    setCreated(new Date(smbFile.createTime()));
                }
                if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
                    setLastModified(new Date(smbFile.getIfModifiedSince()));
                }
                final String name = smbFile.getName();
                setTitle(name);
                setFileName(name);
                if (set.contains(Field.FILE_MIMETYPE)) {
                    String contentType = smbFile.getContentType();
                    if (isEmpty(contentType)) {
                        final MimeTypeMap map = CIFSServices.getService(MimeTypeMap.class);
                        contentType = map.getContentType(name);
                    }
                    setFileMIMEType(contentType);
                }
                if (set.contains(Field.FILE_SIZE)) {
                    setFileSize(smbFile.getContentLength());
                }
                if (set.contains(Field.URL)) {
                    setURL(smbFile.getPath());
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
            } catch (final SmbException e) {
                throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
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
