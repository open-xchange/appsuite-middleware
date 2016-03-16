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

package com.openexchange.mail.mime.dataobjects;

import static com.openexchange.java.CharsetDetector.detectCharset;
import static com.openexchange.mail.utils.MessageUtility.readStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessageRemovedException;
import javax.mail.Part;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.datasource.FileDataSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;

/**
 * {@link MimeFileMailPart} - A {@link MailPart} implementation that keeps a reference to a temporary created file that shall be added as an
 * attachment later.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MimeFileMailPart extends MailPart {

    private static final long serialVersionUID = 257902073011243269L;

    private static final transient org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(MimeFileMailPart.class);

    private final File file;

    private transient DataSource dataSource;

    private transient Object cachedContent;

    /**
     * Initializes a new {@link MimeFileMailPart}
     *
     * @param fileDataSource The file data source
     * @throws OXException If upload file's content type cannot be parsed
     */
    protected MimeFileMailPart(final com.openexchange.mail.mime.datasource.FileDataSource fileDataSource) throws OXException {
        super();
        this.file = fileDataSource.getFile();
        final String preparedFileName = fileDataSource.getName();
        setContentType(prepareContentType(fileDataSource.getContentType(), preparedFileName));
        setFileName(preparedFileName);
        setSize(fileDataSource.getFile().length());
        final ContentDisposition cd = new ContentDisposition();
        cd.setDisposition(Part.ATTACHMENT);
        cd.setFilenameParameter(getFileName());
        setContentDisposition(cd);
        try {
            if (getContentType().getCharsetParameter() == null && getContentType().startsWith(TEXT)) {
                /*
                 * Guess charset for textual attachment
                 */
                final String cs = detectCharset(new FileInputStream(file));
                getContentType().setCharsetParameter(cs);
            }
            this.dataSource = fileDataSource;
        } catch (final IOException e) {
            LOG.error("", e);
            dataSource = new MessageDataSource(new byte[0], MimeTypes.MIME_APPL_OCTET);
        }
    }

    private static String prepareContentType(final String contentType, final String preparedFileName) {
        if (null == contentType || contentType.length() == 0) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        final String retval;
        {
            final int mlen = contentType.length() - 1;
            if (0 == contentType.indexOf('"') && mlen == contentType.lastIndexOf('"')) {
                retval = contentType.substring(1, mlen);
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
                if (getContentType().getCharsetParameter() == null && getContentType().startsWith(TEXT)) {
                    /*
                     * Guess charset for textual attachment
                     */
                    final String cs = detectCharset(new FileInputStream(file));
                    getContentType().setCharsetParameter(cs);
                }
                dataSource = new FileDataSource(file, getContentType().toString());
            } catch (final IOException e) {
                LOG.error("", e);
                dataSource = new MessageDataSource(new byte[0], MimeTypes.MIME_APPL_OCTET);
            }
        }
        return dataSource;
    }

    /**
     * Gets the upload file associated with this mail part
     *
     * @return The upload file associated with this mail part
     */
    public File getFile() {
        return file;
    }

    @Override
    public Object getContent() throws OXException {
        if (cachedContent != null) {
            return cachedContent;
        }
        if (getContentType().isMimeType(MimeTypes.MIME_TEXT_ALL)) {
            String charset = getContentType().getCharsetParameter();
            if (charset == null) {
                try {
                    charset = detectCharset(new FileInputStream(file));
                    LOG.debug("Uploaded file contains textual content but does not specify a charset. Assumed charset is: {}", charset);
                } catch (final FileNotFoundException e) {
                    throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                }
            }
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                cachedContent = readStream(fis, charset);
            } catch (final FileNotFoundException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            } catch (final IOException e) {
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
        try {
            return new FileInputStream(file);
        } catch (final FileNotFoundException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void loadContent() {
        // Nothing to do
    }

    @Override
    public void prepareForCaching() {
        // Nothing to do
    }

}
