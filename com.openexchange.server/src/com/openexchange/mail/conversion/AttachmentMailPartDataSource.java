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

package com.openexchange.mail.conversion;

import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.io.InputStream;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.session.Session;

/**
 * {@link AttachmentMailPartDataSource} - The {@link MailPartDataSource} for additional attachments on iCal mails
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class AttachmentMailPartDataSource extends MailPartDataSource {

    public static final String PROPERTY_CID = "com.openexchange.mail.conversion.cid";

    /**
     * Initializes a new {@link AttachmentMailPartDataSource}.
     *
     */
    public AttachmentMailPartDataSource() {
        super();
    }

    @SuppressWarnings("unchecked") // checked within method
    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }

        // Get parameters
        final FullnameArgument arg = prepareMailFolderParam(dataArguments.get(ARGS[0]));
        if (null == arg) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
        }
        String fullname = arg.getFullname();
        String mailId = dataArguments.get(ARGS[1]);
        String cid = dataArguments.get(PROPERTY_CID);
        DataProperties properties = new DataProperties();

        // Get the mail with its attachments
        MailPart mail = getMail(arg.getAccountId(), fullname, mailId, session, properties);
        if (null != mail.getContentType() && mail.getContentType().startsWith("multipart/")) {
            for (int i = 0; i < mail.getEnclosedCount(); i++) {
                MailPart part = mail.getEnclosedMailPart(i);
                ContentType contentType = part.getContentType();
                // Skip calendar files
                if (null != contentType /*&& false == contentType.isMimeType(MimeTypes.MIME_TEXT_ALL_CALENDAR)*/) {
                    String contentId = part.getContentId();
                    if (Strings.isNotEmpty(contentId) && cid.equals(contentId)) {
                        // Found attachment, set additional data for the stream
                        String cs = contentType.getCharsetParameter();
                        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType.getBaseType());
                        properties.put(DataProperties.PROPERTY_CHARSET, com.openexchange.java.Strings.isEmpty(cs) ? MailProperties.getInstance().getDefaultMimeCharset() : cs);
                        properties.put(DataProperties.PROPERTY_CHARSET, com.openexchange.java.Strings.isEmpty(cs) ? MailProperties.getInstance().getDefaultMimeCharset() : cs);
                        properties.put(DataProperties.PROPERTY_SIZE, Long.toString(part.getSize()));
                        properties.put(DataProperties.PROPERTY_NAME, part.getFileName());
                        return new SimpleData<D>((D) part.getInputStream(), properties);
                    }
                }
            }
        }
        throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(cid, mailId, fullname);
    }

    private MailPart getMail(int accountId, String fullname, String mailId, Session session, DataProperties properties) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            MailFolder folder = mailAccess.getFolderStorage().getFolder(fullname);
            if (folder.isShared()) {
                MailPermission[] permissions = folder.getPermissions();
                boolean found = false;
                boolean foundMoreThanOne = false;
                int ownerId = 0;
                for (MailPermission perm : permissions) {
                    if (perm.isFolderAdmin() && !perm.isGroupPermission()) {
                        if (found) {
                            foundMoreThanOne = true;
                        }
                        ownerId = perm.getEntity();
                        found = true;
                    }
                }
                if (found && !foundMoreThanOne && ownerId > 0) {
                    properties.put(ICalMailPartDataSource.PROPERTY_OWNER, Integer.toString(ownerId));
                }
            }
            final MailPart mailPart = mailAccess.getMessageStorage().getMessage(fullname, mailId, false);
            if (null == mailPart) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(mailId, fullname);
            }
            mailPart.loadContent();
            return mailPart;
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

}
