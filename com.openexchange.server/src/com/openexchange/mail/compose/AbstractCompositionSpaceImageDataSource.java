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

package com.openexchange.mail.compose;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.drew.imaging.FileType;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.Reference;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.session.Session;


/**
 * {@link AbstractCompositionSpaceImageDataSource} - The abstract image data source for composition space image resources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public abstract class AbstractCompositionSpaceImageDataSource implements ImageDataSource {

    protected static final String MIMETYPE_APPLICATION_OCTETSTREAM = "application/octet-stream";

    protected static final long EXPIRES = ImageDataSource.YEAR_IN_MILLIS * 50;

    private final ContentType unknownContentType;

    /**
     * Initializes a new {@link AbstractCompositionSpaceImageDataSource}.
     */
    protected AbstractCompositionSpaceImageDataSource() {
        super();
        ContentType ct = new ContentType();
        ct.setPrimaryType("image");
        ct.setSubType("unknown");
        unknownContentType = ct;
    }

    /**
     * Determines the MIME type of given attachment.
     *
     * @param attachment The attachment
     * @param fileTypeRef The file type reference
     * @return The MIME type
     * @throws IOException If an I/O error occurs
     */
    protected ContentType determineContentType(Attachment attachment, Reference<FileType> fileTypeRef) throws IOException {
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
        } catch (@SuppressWarnings("unused") OXException e) {
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

    /**
     * Determines the file name for given attachment.
     *
     * @param attachment The attachment
     * @param contentType The attachment's MIME type
     * @param fileTypeRef The file type reference
     * @param createIfMissing Whether to create a file name if missing
     * @return The file name or <code>null</code>
     * @throws OXException If attachment data cannot be obtained
     * @throws IOException If an I/O error occurs
     */
    protected String determineFileName(Attachment attachment, ContentType contentType, Reference<FileType> fileTypeRef, boolean createIfMissing) throws OXException, IOException {
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
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public ImageLocation parseUrl(String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public String generateUrl(ImageLocation imageLocation, Session session) throws OXException {
        StringBuilder sb = new StringBuilder(64);
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        return sb.toString();
    }

    @Override
    public long getExpires() {
        return EXPIRES;
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

}
