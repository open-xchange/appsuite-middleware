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

package com.openexchange.mail.compose.impl;

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
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDataSource;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.datasource.MessageDataSource;

/**
 * {@link AttachmentMailPart} - The mail part backed by an attachment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentMailPart extends MailPart implements ComposedMailPart {

    private static final long serialVersionUID = 1406440848691751504L;

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentMailPart.class);
    }

    private final transient Attachment attachment;
    private transient DataSource dataSource;
    private transient Object cachedContent;

    /**
     * Initializes a new {@link AttachmentMailPart}.
     *
     * @param attachment The attachment
     * @throws OXException If initialization fails
     */
    public AttachmentMailPart(Attachment attachment) throws OXException {
        super();
        this.attachment = attachment;
        String preparedFileName = attachment.getName();
        try {
            setContentType(prepareContentType(attachment.getMimeType(), preparedFileName));
        } catch (@SuppressWarnings("unused") OXException e) {
            // Retry with guess by file name
            setContentType(MimeType2ExtMap.getContentType(preparedFileName));
        }
        {
            final ContentType contentType = getContentType();
            if (contentType.startsWith(TEXT) && "GB18030".equalsIgnoreCase(contentType.getCharsetParameter())) {
                InputStream in = null;
                try {
                    in = attachment.getData();
                    contentType.setCharsetParameter(CharsetDetector.detectCharset(in));
                    setContentType(contentType);
                } catch (@SuppressWarnings("unused") Exception e) {
                    // Ignore
                } finally {
                    Streams.close(in);
                }
            } else if (contentType.startsWith("application/force")) {
                contentType.setBaseType(MimeType2ExtMap.getContentType(preparedFileName));
                setContentType(contentType);
            }
        }
        {
            ContentId contentId = attachment.getContentIdAsObject();
            if (contentId != null) {
                setContentId(contentId.getContentIdForHeader());
            }
        }
        setFileName(preparedFileName);
        setSize(attachment.getSize());
        final ContentDisposition cd = new ContentDisposition();
        cd.setDisposition(Attachment.ContentDisposition.INLINE == attachment.getContentDisposition() ? Part.INLINE : Part.ATTACHMENT);
        cd.setFilenameParameter(getFileName());
        setContentDisposition(cd);
    }

    /**
     * Gets the attachment
     *
     * @return The attachment
     */
    public Attachment getAttachment() {
        return attachment;
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
        /*
         * Lazy creation
         */
        if (null == dataSource) {
            try {
                ContentType contentType = getContentType();
                if (contentType.getCharsetParameter() == null && contentType.startsWith(TEXT)) {
                    /*
                     * Guess charset for textual attachment
                     */
                    String cs = detectCharset(attachment.getData());
                    contentType.setCharsetParameter(cs);
                    LoggerHolder.LOG.debug("Uploaded file contains textual content but does not specify a charset. Assumed charset is: {}", cs);
                }
                dataSource = new AttachmentDataSource(attachment, contentType.toString());
            } catch (Exception e) {
                LoggerHolder.LOG.error("", e);
                dataSource = new MessageDataSource(new byte[0], MimeTypes.MIME_APPL_OCTET);
            }
        }
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
                charset = detectCharset(attachment.getData());
                LoggerHolder.LOG.debug("Uploaded file contains textual content but does not specify a charset. Assumed charset is: {}", charset);
            }

            InputStream fis = null;
            try {
                fis = attachment.getData();
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
        return attachment.getData();
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
        AttachmentOrigin origin = attachment.getOrigin();
        if (origin == null) {
            return ComposedMailPart.ComposedPartType.FILE;
        }

        switch (origin) {
            case UPLOAD:
                return ComposedMailPart.ComposedPartType.FILE;
            case CONTACT:
                return ComposedMailPart.ComposedPartType.DOCUMENT;
            case DRIVE:
                return ComposedMailPart.ComposedPartType.DOCUMENT;
            case MAIL:
                return ComposedMailPart.ComposedPartType.REFERENCE;
            case VCARD:
                return ComposedMailPart.ComposedPartType.DOCUMENT;
            default:
                break;
        }

        return ComposedMailPart.ComposedPartType.FILE;
    }

}
