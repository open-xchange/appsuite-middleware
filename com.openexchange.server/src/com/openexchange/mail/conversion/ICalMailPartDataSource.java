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
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link ICalMailPartDataSource} - The {@link MailPartDataSource} for VCard parts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ICalMailPartDataSource extends MailPartDataSource {

    public static final String PROPERTY_OWNER = "com.openexchange.conversion.owner";
    private ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link ICalMailPartDataSource}
     */
    public ICalMailPartDataSource() {
        super();
    }

    /**
     * Sets the ServiceLookup
     *
     * @param serviceLookup The service lookup
     * @return this
     */
    public ICalMailPartDataSource setServiceLookup(ServiceLookup serviceLookup) {
        this.serviceLookup = serviceLookup;
        return this;
    }

    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        final MailPart mailPart;
        {
            final FullnameArgument arg = prepareMailFolderParam(dataArguments.get(ARGS[0]));
            if (null == arg) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
            }

            final String fullname = arg.getFullname();
            final String mailId = dataArguments.get(ARGS[1]);
            final String sequenceId = dataArguments.get(ARGS[2]);
            final DataProperties properties = new DataProperties();
            mailPart = getMailPart(arg.getAccountId(), fullname, mailId, sequenceId, session, properties);
            ContentType contentType = mailPart.getContentType();
            if (!isCalendar(contentType)) {
                final String fileName = mailPart.getFileName();
                if (com.openexchange.java.Strings.isEmpty(fileName)) {
                    throwException(contentType);
                }
                final String contentTypeByFileName = MimeType2ExtMap.getContentType(fileName);
                if (MimeTypes.MIME_APPL_OCTET.equalsIgnoreCase(contentTypeByFileName)) {
                    throwException(contentType);
                }
                final ContentType tmp = new ContentType(contentTypeByFileName);
                if (!isCalendar(tmp)) {
                    throwException(contentType);
                }
                if (null == contentType) {
                    contentType = tmp;
                } else {
                    contentType.setBaseType(tmp.getBaseType());
                }
            }
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType.getBaseType());
            final String cs = contentType.getCharsetParameter();
            properties.put(DataProperties.PROPERTY_CHARSET, com.openexchange.java.Strings.isEmpty(cs) ? MailProperties.getInstance().getDefaultMimeCharset() : cs);
            properties.put(DataProperties.PROPERTY_SIZE, Long.toString(mailPart.getSize()));
            properties.put(DataProperties.PROPERTY_NAME, mailPart.getFileName());
            return new SimpleData<D>((D) mailPart.getInputStream(), properties);
        }
    }

    private boolean isCalendar(ContentType contentType) {
        return null != contentType && (contentType.isMimeType(MimeTypes.MIME_TEXT_ALL_CALENDAR) || contentType.startsWith(MimeTypes.MIME_APPLICATION_ICS));
    }

    private void throwException(ContentType contentType) throws OXException {
        if (null == contentType) {
            throw DataExceptionCodes.ERROR.create("Missing header 'Content-Type' in requested mail part");
        }
        throw DataExceptionCodes.ERROR.create("Requested mail part is not an ICal: " + contentType.getBaseType());
    }

    private MailPart getMailPart(int accountId, String fullname, String mailId, String sequenceId, Session session, DataProperties properties) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            if (serviceLookup != null) {
                CryptographicAwareMailAccessFactory cryptoMailAccessFactory = serviceLookup.getOptionalService(CryptographicAwareMailAccessFactory.class);
                if (cryptoMailAccessFactory != null) {
                    mailAccess = cryptoMailAccessFactory.createAccess((MailAccess<IMailFolderStorage, IMailMessageStorage>) mailAccess, session, null);
                }
            }
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
                    properties.put(PROPERTY_OWNER, Integer.toString(ownerId));
                }
            }
            /*
             * Load header
             */
            MailMessage message = mailAccess.getMessageStorage().getMessage(fullname, mailId, false);
            String[] header = message.getHeader("FROM");
            if (null != header && 1 == header.length) {
                properties.put("from", header[0]);
            }

            /*
             * Load iCal
             */
            final MailPart mailPart = mailAccess.getMessageStorage().getAttachment(fullname, mailId, sequenceId);
            if (null == mailPart) {
                throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(sequenceId, mailId, fullname);
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
