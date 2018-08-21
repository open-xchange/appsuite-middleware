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

package com.openexchange.mail.compose.impl.attachment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import com.drew.imaging.FileType;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.Reference;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.CompositonSpaces;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link AttachmentImageDataSource}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AttachmentImageDataSource implements ImageDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentImageDataSource.class);

    private static final String MIMETYPE_APPLICATION_OCTETSTREAM = "application/octet-stream";

    private static final long EXPIRES = ImageDataSource.YEAR_IN_MILLIS * 50;

    // ---------------------------------------------------------------------------------------------------------------------------

    private static final AttachmentImageDataSource INSTANCE = new AttachmentImageDataSource();

    /**
     * Returns the instance
     *
     * @return the instance
     */
    public static AttachmentImageDataSource getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private volatile AttachmentStorageService attachmentStorageService;
    private final ContentType unknownContentType;
    private final String[] args;
    private final String alias;
    private final String registrationName;

    /**
     * Initializes a new {@link AttachmentImageDataSource}.
     */
    private AttachmentImageDataSource() {
        super();
        args = new String[] { "com.openexchange.mail.compose.id" };
        alias = "/mail/compose/image";
        registrationName = AttachmentStorage.IMAGE_REGISTRATION_NAME;
        ContentType ct = new ContentType();
        ct.setPrimaryType("image");
        ct.setSubType("unknown");
        unknownContentType = ct;
    }

    /**
     * Applies the service
     *
     * @param attachmentStorageService The service
     */
    public void setService(AttachmentStorageService attachmentStorageService) {
        this.attachmentStorageService = attachmentStorageService;
    }

    /**
     * Gets the attachment storage.
     *
     * @param serverSession The server session
     * @return The attachment storage
     * @throws OXException If appropriate attachment storage cannot be returned
     */
    private AttachmentStorage getAttachmentStorage(Session serverSession) throws OXException {
        AttachmentStorageService attachmentStorageService = this.attachmentStorageService;
        if (null == attachmentStorageService) {
            return null;
        }
        return attachmentStorageService.getAttachmentStorageFor(serverSession);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }

        String sAttachmentId = dataArguments.get(args[0]);
        if (sAttachmentId == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(args[0]);
        }

        AttachmentStorage attachmentStorage = getAttachmentStorage(session);
        if (null == attachmentStorage) {
            throw ServiceExceptionCode.absentService(AttachmentStorage.class);
        }

        UUID attachmentId = CompositonSpaces.parseAttachmentIdIfValid(sAttachmentId);
        if (null == attachmentId) {
            LOG.warn("Requested a non-existing image attachment {} for user {} in context {}. Returning an empty image as fallback.", sAttachmentId, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            DataProperties properties = new DataProperties(4);
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, Integer.toString(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        Attachment attachment = attachmentStorage.getAttachment(attachmentId, session);
        if (null == attachment) {
            LOG.warn("Requested a non-existing image attachment {} for user {} in context {}. Returning an empty image as fallback.", sAttachmentId, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            DataProperties properties = new DataProperties(4);
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, Integer.toString(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        try {
            Reference<FileType> fileTypeRef = new Reference<>();
            ContentType contentType = determineContentType(attachment, fileTypeRef);
            String fileName = determineFileName(attachment, contentType, fileTypeRef, false);

            DataProperties properties = new DataProperties(4);
            properties.put(DataProperties.PROPERTY_ID, sAttachmentId);
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType.toString());
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(attachment.getSize()));
            properties.put(DataProperties.PROPERTY_NAME, fileName);
            return new SimpleData<D>((D) (attachment.getData()), properties);
        } catch (IOException e) {
            throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private ContentType determineContentType(Attachment attachment, Reference<FileType> fileTypeRef) throws IOException {
        try {
            String contentType = attachment.getMimeType();
            ContentType ct;
            if (Strings.isNotEmpty(contentType) && false == (ct = new ContentType(contentType)).startsWith(MIMETYPE_APPLICATION_OCTETSTREAM)) {
                return ct;
            }

            FileType fileType = detectFileType(attachment);
            fileTypeRef.setValue(fileType);
            if (FileType.Unknown == fileType) {
                return unknownContentType;
            }

            String mimeType = fileType.getMimeType();
            return Strings.isEmpty(mimeType) ? unknownContentType : new ContentType(mimeType);
        } catch (OXException e) {
            // Parsing MIME type failed
            return unknownContentType;
        }
    }

    private FileType detectFileType(Attachment attachment) throws OXException, IOException {
        InputStream in = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            in = attachment.getData();
            bufferedInputStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 64);
            FileType fileType = com.drew.imaging.FileTypeDetector.detectFileType(bufferedInputStream);
            return null == fileType ? FileType.Unknown : fileType;
        } finally {
            Streams.close(bufferedInputStream, in);
        }
    }

    private String determineFileName(Attachment attachment, ContentType contentType, Reference<FileType> fileTypeRef, boolean createIfMissing) throws OXException, IOException {
        String name = attachment.getName();
        if (Strings.isNotEmpty(name)) {
            return name;
        }

        String fileName = contentType.getNameParameter();
        if (Strings.isNotEmpty(fileName) && !"null".equalsIgnoreCase(fileName)) {
            return fileName;
        }

        if (false == createIfMissing) {
            return null;
        }

        // Create a file name...
        FileType fileType = fileTypeRef.getValue();
        if (null == fileType) {
            fileType = detectFileType(attachment);
            fileTypeRef.setValue(fileType);
        }

        String commonExtension = fileType.getCommonExtension();
        if (Strings.isEmpty(commonExtension)) {
            return "image.dat";
        }

        return commonExtension.charAt(0) == '.' ? "image" + commonExtension : "image." + commonExtension;
    }

    @Override
    public String[] getRequiredArguments() {
        final String[] args = new String[this.args.length];
        System.arraycopy(this.args, 0, args, 0, this.args.length);
        return args;
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public String getRegistrationName() {
        return registrationName;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public ImageLocation parseUrl(String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public DataArguments generateDataArgumentsFrom(ImageLocation imageLocation) {
        final DataArguments dataArgs = new DataArguments(2);
        dataArgs.put(args[0], imageLocation.getImageId());
        return dataArgs;
    }

    @Override
    public String generateUrl(ImageLocation imageLocation, Session session) throws OXException {
        final StringBuilder sb = new StringBuilder(64);
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        return sb.toString();
    }

    @Override
    public long getExpires() {
        return EXPIRES;
    }

    @Override
    public String getETag(ImageLocation imageLocation, Session session) throws OXException {
        final char delim = '#';
        final StringBuilder builder = new StringBuilder(128);
        builder.append(delim).append(imageLocation.getImageId());
        builder.append(delim);
        return ImageUtility.getMD5(builder.toString(), "hex");
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

}
