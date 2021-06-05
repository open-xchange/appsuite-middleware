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

import static com.openexchange.java.CharsetDetector.detectCharset;
import static com.openexchange.mail.utils.MessageUtility.readStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessageRemovedException;
import javax.mail.Part;
import org.slf4j.Logger;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;

/**
 * {@link ThresholdFileHolderMailPart} - The mail part backed by a file holder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ThresholdFileHolderMailPart extends MailPart implements ComposedMailPart {

    private static final long serialVersionUID = 1406440848691751504L;

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ThresholdFileHolderMailPart.class);
    }

    private final transient ThresholdFileHolder fileHolder;
    private final transient DataSource dataSource;
    private transient Object cachedContent;

    /**
     * Initializes a new {@link ThresholdFileHolderMailPart}.
     *
     * @param attachmentDescription The attachment description
     * @param fileHolder The attachment data as a {@link ThresholdFileHolder} instance
     * @throws OXException If initialization fails
     */
    public ThresholdFileHolderMailPart(AttachmentDescription attachmentDescription, ThresholdFileHolder fileHolder) throws OXException {
        super();
        boolean error = true;
        try {
            String preparedFileName = attachmentDescription.getName();
            ContentType contentType = determineContentType(attachmentDescription, preparedFileName, fileHolder);
            fileHolder.setContentType(contentType.getBaseType());

            determineContentId(attachmentDescription);
            setFileName(preparedFileName);
            setSize(attachmentDescription.getSize());
            ContentDisposition cd = new ContentDisposition();
            cd.setDisposition(Part.ATTACHMENT);
            cd.setFilenameParameter(getFileName());
            setContentDisposition(cd);
            this.fileHolder = fileHolder;
            dataSource = new AttachmentDescriptionDataSource(attachmentDescription, fileHolder);
            error = false;
        } finally {
            if (error) {
                Streams.close(fileHolder);
            }
        }
    }

    private void determineContentId(AttachmentDescription attachmentDescription) {
        ContentId contentId = attachmentDescription.getContentId();
        if (contentId != null) {
            setContentId(contentId.getContentIdForHeader());
        }
    }

    private ContentType determineContentType(AttachmentDescription attachmentDescription, String preparedFileName, ThresholdFileHolder fileHolder) throws OXException {
        try {
            setContentType(prepareContentType(attachmentDescription.getMimeType(), preparedFileName));
        } catch (@SuppressWarnings("unused") OXException e) {
            // Retry with guess by file name
            setContentType(MimeType2ExtMap.getContentType(preparedFileName));
        }

        ContentType contentType = getContentType();
        if (contentType.startsWith(TEXT)) {
            if (contentType.getCharsetParameter() == null || "GB18030".equalsIgnoreCase(contentType.getCharsetParameter())) {
                // Examine stream data
                try {
                    contentType.setCharsetParameter(CharsetDetector.detectCharset(fileHolder.getStream()));
                    setContentType(contentType);
                } catch (Exception e) {
                    LoggerHolder.LOG.debug("Failed to examine stream data", e);
                }
            }
        } else if (contentType.startsWith("application/force")) {
            contentType.setBaseType(MimeType2ExtMap.getContentType(preparedFileName));
            setContentType(contentType);
        }

        return getContentType();
    }

    private static String prepareContentType(String contentType, String preparedFileName) {
        if (null == contentType || contentType.length() == 0) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        final String retval;
        {
            if (0 == contentType.indexOf('"')) {
                int mlen = contentType.length() - 1;
                if (mlen == contentType.lastIndexOf('"')) {
                    retval = contentType.substring(1, mlen);
                } else {
                    retval = contentType;
                }
            } else {
                retval = contentType;
            }
        }
        if ("multipart/form-data".equalsIgnoreCase(retval)) {
            return MimeType2ExtMap.getContentType(preparedFileName);
        }
        return contentType;
    }

    private static final String TEXT = "text/";

    private DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Object getContent() throws OXException {
        if (cachedContent != null) {
            return cachedContent;
        }
        if (getContentType().startsWith(TEXT)) {
            String charset = getContentType().getCharsetParameter();
            if (charset == null) {
                charset = detectCharset(fileHolder.getStream());
                LoggerHolder.LOG.debug("Uploaded file contains textual content but does not specify a charset. Assumed charset is: {}", charset);
            }

            InputStream fis = null;
            try {
                fis = fileHolder.getStream();
                cachedContent = readStream(fis, charset);
            } catch (FileNotFoundException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            } catch (IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(fis);
            }
            return cachedContent;
        }
        return null;
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return new DataHandler(getDataSource());
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return null;
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return fileHolder.getStream();
    }

    @Override
    public void loadContent() {
        // Nothing to do
    }

    @Override
    public void prepareForCaching() {
        // Nothing to do
    }

    @Override
    public ComposedPartType getType() {
        return ComposedMailPart.ComposedPartType.FILE;
    }

}
