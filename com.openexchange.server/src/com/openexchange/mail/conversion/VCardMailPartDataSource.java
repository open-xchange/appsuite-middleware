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
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.session.Session;

/**
 * {@link VCardMailPartDataSource} - The {@link MailPartDataSource} for VCard parts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VCardMailPartDataSource extends MailPartDataSource {

    /**
     * Initializes a new {@link VCardMailPartDataSource}
     */
    public VCardMailPartDataSource() {
        super();
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
            mailPart = getMailPart(arg.getAccountId(), fullname, mailId, sequenceId, session);
            final ContentType contentType = mailPart.getContentType();
            if (contentType == null) {
                throw DataExceptionCodes.ERROR.create("Missing header 'Content-Type' in requested mail part");
            }
            if (!contentType.isMimeType(MimeTypes.MIME_TEXT_ALL_CARD) && !contentType.startsWith("text/directory")) {
                throw DataExceptionCodes.ERROR.create("Requested mail part is not a VCard: " + contentType.getBaseType());
            }
            final DataProperties properties = new DataProperties();
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType.getBaseType());
            final String charset = contentType.getCharsetParameter();
            if (charset == null) {
                properties.put(DataProperties.PROPERTY_CHARSET, MailProperties.getInstance().getDefaultMimeCharset());
            } else {
                properties.put(DataProperties.PROPERTY_CHARSET, charset);
            }
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(mailPart.getSize()));
            properties.put(DataProperties.PROPERTY_NAME, mailPart.getFileName());
            return new SimpleData<D>((D) mailPart.getInputStream(), properties);
        }
    }

}
