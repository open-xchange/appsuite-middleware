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
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.session.Session;

/**
 * {@link ICalMailPartDataSource} - The {@link MailPartDataSource} for VCard parts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ICalMailPartDataSource extends MailPartDataSource {

    public static final String PROPERTY_OWNER = "com.openexchange.conversion.owner";

    /**
     * Initializes a new {@link ICalMailPartDataSource}
     */
    public ICalMailPartDataSource() {
        super();
    }

    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
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

    private boolean isCalendar(final ContentType contentType) {
        return null != contentType && (contentType.isMimeType(MimeTypes.MIME_TEXT_ALL_CALENDAR) || contentType.startsWith(MimeTypes.MIME_APPLICATION_ICS));
    }

    private void throwException(final ContentType contentType) throws OXException {
        if (null == contentType) {
            throw DataExceptionCodes.ERROR.create("Missing header 'Content-Type' in requested mail part");
        }
        throw DataExceptionCodes.ERROR.create("Requested mail part is not an ICal: " + contentType.getBaseType());
    }

    private MailPart getMailPart(final int accountId, final String fullname, final String mailId, final String sequenceId, final Session session, DataProperties properties) throws OXException {
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
                    properties.put(PROPERTY_OWNER, Integer.toString(ownerId));
                }
            }
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
