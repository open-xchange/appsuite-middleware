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

package com.openexchange.file.storage.mail;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.mail.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mime.MimeTypeMap;
import com.sun.mail.imap.IMAPMessage;

/**
 * {@link MailDriveFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveFile extends DefaultFile {

    /**
     * Parses an instance of <code>MailDriveFile</code> from specified IMAP message.
     *
     * @param message The backing IMAP message representing the attachment
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     * @param rootFolderId The identifier of the root folder
     * @return This parsed Mail Drive file <b>or <code>null</code> if specified IMAP message represents an invalid attachment</b>
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    public static MailDriveFile parse(IMAPMessage message, String folderId, String id, int userId, String rootFolderId) throws MessagingException, OXException {
        return parse(message, folderId, id, userId, rootFolderId, null);
    }

    /**
     * Parses an instance of <code>MailDriveFile</code> from specified IMAP message.
     *
     * @param message The backing IMAP message representing the attachment
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     * @param rootFolderId The identifier of the root folder
     * @param fields The fields to consider
     * @return This parsed Mail Drive file <b>or <code>null</code> if specified IMAP message represents an invalid attachment</b>
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    public static MailDriveFile parse(IMAPMessage message, String folderId, String id, int userId, String rootFolderId, List<Field> fields) throws MessagingException, OXException {
        return new MailDriveFile(folderId, id, userId, rootFolderId).parseMessage(message, fields);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private MailMetadata metadata;

    /**
     * Initializes a new {@link MailDriveFile}.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     * @param rootFolderId The identifier of the root folder
     */
    private MailDriveFile(String folderId, String id, int userId, String rootFolderId) {
        super();
        setFolderId(isRootFolder(folderId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : folderId);
        setCreatedBy(userId);
        setModifiedBy(userId);
        setId(id);
        setFileName(id);
        setVersion(FileStorageFileAccess.CURRENT_VERSION);
        setIsCurrentVersion(true);
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return "".equals(id) || rootFolderId.equals(id);
    }

    /**
     * Gets the mail metadata for this file.
     *
     * @return The mail metadata, or <code>null</code> if not yet parsed
     */
    public MailMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        final String url = getURL();
        return url == null ? super.toString() : url;
    }

    /**
     * Parses specified Mail Drive file.
     *
     * @param message The backing IMAP message representing the attachment
     * @param fields The fields to consider
     * @return This Mail Drive file with property set applied <b>or <code>null</code> if specified IMAP message represents an invalid attachment</b>
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If parsing Mail Drive file fails
     */
    private MailDriveFile parseMessage(IMAPMessage message, List<Field> fields) throws MessagingException, OXException {
        if (null != message) {
            try {
                String name = MimeMessageConverter.getSubject(message);
                if (Strings.isEmpty(name)) {
                    return null;
                }
                setTitle(name);
                setFileName(name);
                final Set<Field> set = null == fields || fields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(fields);

                if (set.contains(Field.CREATED)) {
                    Date createdAt = message.getReceivedDate();
                    if (null != createdAt) {
                        setCreated(createdAt);
                    }
                }
                if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
                    Date modifiedAt = message.getReceivedDate();
                    if (null != modifiedAt) {
                        setLastModified(modifiedAt);
                    }
                }
                if (set.contains(Field.FILE_MIMETYPE)) {
                    String contentType;
                    {
                        String[] tmp = message.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                        if ((tmp != null) && (tmp.length > 0)) {
                            contentType = MimeMessageUtility.decodeMultiEncodedHeader(tmp[0]);
                        } else {
                            contentType = MimeTypes.MIME_DEFAULT;
                        }
                    }
                    if (Strings.isEmpty(contentType)) {
                        MimeTypeMap map = Services.getService(MimeTypeMap.class);
                        contentType = map.getContentType(name);
                    }
                    if (null != contentType) {
                        int pos = contentType.indexOf(';');
                        if (pos > 0) {
                            contentType = contentType.substring(0, pos);
                        }
                        contentType = contentType.trim();
                    }
                    setFileMIMEType(contentType);
                }
                if (set.contains(Field.FILE_SIZE)) {
                    long size = message.getSize();
                    if (size >= 0) {
                        setFileSize(size);
                        setAccurateSize(false);
                    }
                }
                if (set.contains(Field.URL)) {
                    setURL(null);
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

                // Prepare additional metadata
                this.metadata = new MailMetadata(message);
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

}
