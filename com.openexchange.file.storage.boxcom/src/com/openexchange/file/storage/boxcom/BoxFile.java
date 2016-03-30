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

package com.openexchange.file.storage.boxcom;

import java.text.ParseException;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import com.box.boxjavalibv2.dao.BoxSharedLink;
import com.box.boxjavalibv2.utils.ISO8601DateParser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.Strings;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link BoxFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoxFile extends DefaultFile {

    /**
     * Initializes a new {@link BoxFile}.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     */
    public BoxFile(String folderId, String id, int userId, String rootFolderId) {
        super();
        setFolderId(isRootFolder(folderId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : folderId);
        setCreatedBy(userId);
        setModifiedBy(userId);
        setId(id);
        setFileName(id);
        setVersion(FileStorageFileAccess.CURRENT_VERSION);
        setNumberOfVersions(1);
        setIsCurrentVersion(true);
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return "0".equals(id) || rootFolderId.equals(id);
    }

    @Override
    public String toString() {
        final String url = getURL();
        return url == null ? super.toString() : url;
    }

    /**
     * Parses specified Box file.
     *
     * @param file The Box file
     * @throws OXException If parsing Box file fails
     * @return This Box file
     */
    public BoxFile parseBoxFile(com.box.boxjavalibv2.dao.BoxFile file) throws OXException {
        return parseBoxFile(file, null);
    }

    /**
     * Parses specified Box file.
     *
     * @param file The Box file
     * @param fields The fields to consider
     * @throws OXException If parsing Box file fails
     * @return This Box file with property set applied
     */
    public BoxFile parseBoxFile(com.box.boxjavalibv2.dao.BoxFile file, List<Field> fields) throws OXException {
        if (null != file) {
            try {
                final String name = file.getName();
                setTitle(name);
                setFileName(name);
                final Set<Field> set = null == fields || fields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(fields);

                if (set.contains(Field.CREATED)) {
                    Date parsed = parseISO8601(file.getCreatedAt());
                    if (parsed != null) {
                        setCreated(parsed);
                    }
                }
                if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
                    Date parsed = parseISO8601(file.getModifiedAt());
                    if (parsed != null) {
                        setLastModified(parsed);
                    }
                }
                if (set.contains(Field.FILE_MIMETYPE)) {
                    MimeTypeMap map = Services.getService(MimeTypeMap.class);
                    String contentType = null == map ? "application/octet-stream" : map.getContentType(name);
                    setFileMIMEType(contentType);
                }
                if (set.contains(Field.FILE_SIZE)) {
                    Double size = file.getSize();
                    if (null != size) {
                        setFileSize(size.longValue());
                    }
                }
                if (set.contains(Field.URL)) {
                    BoxSharedLink sharedLink = file.getSharedLink();
                    if (null != sharedLink) {
                        setURL(sharedLink.getDownloadUrl());
                    }
                }
                if (set.contains(Field.COLOR_LABEL)) {
                    setColorLabel(0);
                }
                if (set.contains(Field.CATEGORIES)) {
                    String[] tags = file.getTags();
                    if (null != tags) {
                        setCategories(Strings.concat(", ", tags));
                    }
                }
                if (set.contains(Field.DESCRIPTION)) {
                    setDescription(file.getDescription());
                }
                if (set.contains(Field.VERSION_COMMENT)) {
                    setVersionComment(null);
                }
                if (set.contains(Field.LOCKED_UNTIL)) {
                    if (file.getLock() != null) {
                        Date parsed = parseISO8601(file.getLock().getExpiresAt());
                        if (parsed != null) {
                            setLockedUntil(parsed);
                        } else {
                            setLockedUntil(new Date(Long.MAX_VALUE)); //indefinite
                        }
                    }
                }
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    /**
     * Parses an ISO8601 formatted date into a {@link java.util.Date} object.
     *
     * @param date The ISO8601 formatted date
     * @return The date
     */
    private Date parseISO8601(String date) {
        if (null != date) {
            try {
                return ISO8601DateParser.parse(date);
            } catch (ParseException e) {
                Logger logger = org.slf4j.LoggerFactory.getLogger(BoxFile.class);
                logger.warn("Could not parse date from: {}", date, e);
            }
        }
        return null;
    }
}
