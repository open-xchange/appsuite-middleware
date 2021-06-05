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

package com.openexchange.mail.compose.mailstorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import com.drew.imaging.FileType;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.Reference;
import com.openexchange.mail.compose.AbstractCompositionSpaceImageDataSource;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link MailStorageCompositionSpaceImageDataSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorageCompositionSpaceImageDataSource extends AbstractCompositionSpaceImageDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailStorageCompositionSpaceImageDataSource.class);

    // ---------------------------------------------------------------------------------------------------------------------------

    private static final MailStorageCompositionSpaceImageDataSource INSTANCE = new MailStorageCompositionSpaceImageDataSource();

    /**
     * Returns the instance
     *
     * @return the instance
     */
    public static MailStorageCompositionSpaceImageDataSource getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private final String[] args;
    private final String alias;
    private final String registrationName;
    private volatile CompositionSpaceServiceFactory compositionSpaceServiceFactory;

    /**
     * Initializes a new {@link MailStorageCompositionSpaceImageDataSource}.
     */
    private MailStorageCompositionSpaceImageDataSource() {
        super();
        args = new String[] { "com.openexchange.mail.compose.csId", "com.openexchange.mail.compose.attachmentId" };
        alias = "/mail/compose/mailstorage/image";
        registrationName = "com.openexchange.mail.compose.mailstorage.image";
    }

    public void setServiceFactory(CompositionSpaceServiceFactory compositionSpaceServiceFactory) {
        this.compositionSpaceServiceFactory = compositionSpaceServiceFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }

        String sCompositionSpaceId = dataArguments.get(args[0]);
        if (sCompositionSpaceId == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(args[0]);
        }

        String sAttachmentId = dataArguments.get(args[1]);
        if (sAttachmentId == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(args[1]);
        }

        UUID compositionSpaceId = CompositionSpaces.parseCompositionSpaceIdIfValid(sCompositionSpaceId);
        UUID attachmentId = CompositionSpaces.parseAttachmentIdIfValid(sAttachmentId);
        if (compositionSpaceId == null || attachmentId == null) {
            LOG.warn("Requested a non-existing image attachment {} for user {} in context {}. Returning an empty image as fallback.", sAttachmentId, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            DataProperties properties = new DataProperties(4);
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, Integer.toString(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        CompositionSpaceService compositionSpaceService = getCompositionSpaceService(session);
        if (null == compositionSpaceService) {
            throw ServiceExceptionCode.absentService(CompositionSpaceService.class);
        }

        Attachment attachment = compositionSpaceService.getAttachment(compositionSpaceId, attachmentId).getAttachments().get(0);
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

    private CompositionSpaceService getCompositionSpaceService(Session session) throws OXException {
        return this.compositionSpaceServiceFactory.createServiceFor(session);
    }

    @Override
    public String[] getRequiredArguments() {
        final String[] args = new String[this.args.length];
        System.arraycopy(this.args, 0, args, 0, this.args.length);
        return args;
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
    public DataArguments generateDataArgumentsFrom(ImageLocation imageLocation) {
        final DataArguments dataArgs = new DataArguments(2);
        dataArgs.put(args[0], imageLocation.getId());
        dataArgs.put(args[1], imageLocation.getImageId());
        return dataArgs;
    }

    @Override
    public String getETag(ImageLocation imageLocation, Session session) throws OXException {
        final char delim = '#';
        final StringBuilder builder = new StringBuilder(128);
        builder.append(delim).append(imageLocation.getId());
        builder.append(delim).append(imageLocation.getImageId());
        builder.append(delim);
        return ImageUtility.getMD5(builder.toString(), "hex");
    }

}
