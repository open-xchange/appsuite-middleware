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

import static com.openexchange.mail.json.writer.MessageWriter.getAddressesAsArray;
import static com.openexchange.mail.mime.converters.MimeMessageConverter.getAddressHeader;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.mail.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MailFolderUtility;
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
     * Initializes a new {@link MailDriveFile}.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     */
    public MailDriveFile(String folderId, String id, int userId, String rootFolderId) {
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

    @Override
    public String toString() {
        final String url = getURL();
        return url == null ? super.toString() : url;
    }

    /**
     * Parses specified Mail Drive file.
     *
     * @param message The backing IMAP message representing the attachment
     * @return This Mail Drive file
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If parsing message fails
     */
    public MailDriveFile parseMessage(IMAPMessage message) throws MessagingException, OXException {
        return parseMessage(message, null);
    }

    /**
     * Parses specified Mail Drive file.
     *
     * @param message The backing IMAP message representing the attachment
     * @param fields The fields to consider
     * @return This Mail Drive file with property set applied
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If parsing Mail Drive file fails
     */
    public MailDriveFile parseMessage(IMAPMessage message, List<Field> fields) throws MessagingException, OXException {
        if (null != message) {
            try {
                final String name = message.getSubject();
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
                    String contentType = message.getContentType();
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

                // Compose "meta" field
                Map<String, Object> meta = new HashMap<String, Object>(2);
                meta.put("mail", mailMetadata(message));
                setMeta(meta);
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    private Map<String, Object> mailMetadata(IMAPMessage message) throws MessagingException {
        Map<String, Object> map = new LinkedHashMap<String, Object>(6);
        {
            String originalSubject = MimeMessageUtility.getHeader("X-Original-Subject", null, message);
            map.put("subject", null == originalSubject ? JSONObject.NULL : MimeMessageUtility.decodeMultiEncodedHeader(originalSubject));
        }
        {
            Long origUid = (Long) message.getItem("X-REAL-UID");
            map.put("id", null == origUid ? JSONObject.NULL : origUid.toString());
        }
        {
            String origFolder = (String) message.getItem("X-MAILBOX");
            map.put("folder", null == origFolder ? JSONObject.NULL : MailFolderUtility.prepareFullname(0, origFolder));
        }
        {
            InternetAddress[] fromHeaders = getAddressHeader("From", message);
            map.put("from", fromHeaders == null || fromHeaders.length == 0 ? JSONObject.NULL : getAddressesAsArray(fromHeaders).asList());
        }
        {
            InternetAddress[] toHeaders = getAddressHeader("To", message);
            map.put("to", toHeaders == null || toHeaders.length == 0 ? JSONObject.NULL : getAddressesAsArray(toHeaders).asList());
        }
        return map;
    }

}
