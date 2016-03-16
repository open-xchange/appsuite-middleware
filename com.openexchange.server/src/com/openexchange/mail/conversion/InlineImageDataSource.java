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

import static com.openexchange.mail.mime.utils.MimeMessageUtility.shouldRetry;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.io.InputStream;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.session.Session;

/**
 * {@link InlineImageDataSource} - A {@link DataSource} for image parts inside a mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InlineImageDataSource implements ImageDataSource {

    private static final InlineImageDataSource INSTANCE = new InlineImageDataSource();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static InlineImageDataSource getInstance() {
        return INSTANCE;
    }

    private static final long EXPIRES = ImageDataSource.YEAR_IN_MILLIS * 50;

    /**
     * Common required arguments for uniquely determining a mail part:
     * <ul>
     * <li>com.openexchange.mail.conversion.fullname</li>
     * <li>com.openexchange.mail.conversion.mailid</li>
     * <li>com.openexchange.mail.conversion.cid</li>
     * </ul>
     */
    private static final String[] ARGS = {
        "com.openexchange.mail.conversion.fullname", "com.openexchange.mail.conversion.mailid", "com.openexchange.mail.conversion.cid" };

    private static final Class<?>[] TYPES = { InputStream.class };

    /**
     * Initializes a new {@link InlineImageDataSource}
     */
    private InlineImageDataSource() {
        super();
    }

    private MailPart getImagePart(final int accountId, final String fullname, final String mailId, final String cid, final Session session) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            return loadImagePart(fullname, mailId, cid, mailAccess);
        } catch (final OXException e) {
            if ((null != mailAccess) && shouldRetry(e)) {
                // Re-connect
                mailAccess = MailAccess.reconnect(mailAccess);
                return loadImagePart(fullname, mailId, cid, mailAccess);
            }
            throw e;
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    private MailPart loadImagePart(final String fullname, final String mailId, final String cid, final MailAccess<?, ?> mailAccess) throws OXException {
        final MailPart imagePart = mailAccess.getMessageStorage().getImageAttachment(fullname, mailId, cid);
        if (null == imagePart) {
            return null;
        }
        ContentType contentType = imagePart.containsContentType() ? imagePart.getContentType() : ContentType.DEFAULT_CONTENT_TYPE;
        if (!contentType.startsWith("image/") && !MimeType2ExtMap.getContentType(imagePart.getFileName(), "text/plain").startsWith("image/")) {
            // Does not seem to be an image
            return null;
        }
        imagePart.loadContent();
        return imagePart;
    }

    @Override
    public String generateUrl(final ImageLocation imageLocation, final Session session) {
        final StringBuilder sb = new StringBuilder(64);
        /*
         * Nothing special...
         */
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        return sb.toString();
    }

    @Override
    public DataArguments generateDataArgumentsFrom(final ImageLocation imageLocation) {
        final DataArguments dataArguments = new DataArguments(3);
        dataArguments.put(ARGS[0], imageLocation.getFolder());
        dataArguments.put(ARGS[1], imageLocation.getId());
        dataArguments.put(ARGS[2], imageLocation.getImageId());
        return dataArguments;
    }

    @Override
    public ImageLocation parseUrl(final String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

    @Override
    public long getExpires() {
        return EXPIRES;
    }

    @Override
    public String getETag(final ImageLocation imageLocation, final Session session) {
        final char delim = '#';
        final StringBuilder builder = new StringBuilder(128);
        builder.append(delim).append(imageLocation.getFolder());
        builder.append(delim).append(imageLocation.getId());
        builder.append(delim).append(imageLocation.getImageId());
        // builder.append(delim).append(session.getUserId());
        // builder.append(delim).append(session.getContextId());
        builder.append(delim);
        return ImageUtility.getMD5(builder.toString(), "hex");
    }

    /**
     * Common required arguments for uniquely determining a mail part:
     * <ul>
     * <li>com.openexchange.mail.conversion.fullname</li>
     * <li>com.openexchange.mail.conversion.mailid</li>
     * <li>com.openexchange.mail.conversion.cid</li>
     * </ul>
     */
    @Override
    public String[] getRequiredArguments() {
        return ARGS;
    }

    @Override
    public Class<?>[] getTypes() {
        return TYPES;
    }

    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        final MailPart mailPart;
        {
            final String fullnameArgument = dataArguments.get(ARGS[0]);
            if (null == fullnameArgument) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
            }
            final FullnameArgument arg = prepareMailFolderParam(fullnameArgument);
            final String fullname = arg.getFullname();
            final String mailId = dataArguments.get(ARGS[1]);
            if (null == mailId) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[1]);
            }
            final String cid = dataArguments.get(ARGS[2]);
            if (null == cid) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[2]);
            }
            mailPart = getImagePart(arg.getAccountId(), fullname, mailId, cid, session);
            if (null == mailPart) {
                throw MailExceptionCode.IMAGE_ATTACHMENT_NOT_FOUND.create(cid, mailId, fullname);
            }
            final ContentType contentType = mailPart.getContentType();
            if (contentType == null) {
                throw DataExceptionCodes.ERROR.create("Missing header 'Content-Type' in requested mail part");
            }
            final String fileName = mailPart.getFileName();
            if (!contentType.isMimeType(MimeTypes.MIME_IMAGE_ALL)) {
                /*
                 * Either general purpose "application/octet-stream" or check by file name
                 */
                if (null == fileName) {
                    if (!contentType.startsWith(MimeTypes.MIME_APPL_OCTET)) {
                        throw DataExceptionCodes.ERROR.create("Requested mail part is not an image: " + contentType.getBaseType());
                    }
                } else {
                    final String byFileName = MimeType2ExtMap.getContentType(fileName);
                    if (ContentType.isMimeType(byFileName, MimeTypes.MIME_IMAGE_ALL)) {
                        /*
                         * File name indicates an image/* content type
                         */
                        contentType.setBaseType(byFileName);
                    } else {
                        if (!contentType.isMimeType(MimeTypes.MIME_APPL_OCTET)) {
                            throw DataExceptionCodes.ERROR.create("Requested mail part is not an image: " + contentType.getBaseType());
                        }
                    }
                }
            } else if (null != fileName) {
                final String byFileName = MimeType2ExtMap.getContentType(fileName);
                if (ContentType.isMimeType(byFileName, MimeTypes.MIME_IMAGE_ALL) && !contentType.startsWith(byFileName)) {
                    /*
                     * File name indicates an image/* content type and Content-Type indicates a different one than determined by file name
                     */
                    contentType.setBaseType(byFileName);
                }
            }
            final DataProperties properties = new DataProperties(8);
            properties.put(DataProperties.PROPERTY_FOLDER_ID, fullnameArgument);
            properties.put(DataProperties.PROPERTY_ID, mailId);
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType.getBaseType());
            final String charset = contentType.getCharsetParameter();
            if (charset != null) {
                properties.put(DataProperties.PROPERTY_CHARSET, charset);
            }
            properties.put(DataProperties.PROPERTY_SIZE, Long.toString(mailPart.getSize()));
            properties.put(DataProperties.PROPERTY_NAME, fileName);
            return new SimpleData<D>((D) mailPart.getInputStream(), properties);
        }
    }

    private static final String REGISTRATION_NAME = "com.openexchange.mail.image";

    @Override
    public String getRegistrationName() {
        return REGISTRATION_NAME;
    }

    private static final String ALIAS = "/mail/picture";

    @Override
    public String getAlias() {
        return ALIAS;
    }

}

