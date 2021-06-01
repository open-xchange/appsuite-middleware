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
                String name = MimeMessageUtility.getSubject(message);
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
            } catch (RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

}
