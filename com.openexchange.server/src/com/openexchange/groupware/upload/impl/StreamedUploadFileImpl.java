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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.upload.impl;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.groupware.upload.StreamedUploadFile;


/**
 * {@link StreamedUploadFileImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class StreamedUploadFileImpl implements StreamedUploadFile {

    private String fieldName;
    private String fileName;
    private String preparedFileName;
    private String contentType;
    private String contentId;
    private InputStream stream;

    /**
     * Initializes a new {@link StreamedUploadFileImpl}.
     */
    public StreamedUploadFileImpl() {
        super();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name
     *
     * @param fieldName The field name to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content-type
     *
     * @param contentType The content-type to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    /**
     * Sets the content-id
     *
     * @param contentId The content-id to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name
     *
     * @param fileName The file name to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getPreparedFileName() {
        if (null == preparedFileName) {
            if (null == fileName) {
                return null;
            }
            preparedFileName = fileName;
            /*
             * Try guessing the filename separator
             */
            int pos = -1;
            if ((pos = preparedFileName.lastIndexOf('\\')) != -1) {
                preparedFileName = preparedFileName.substring(pos + 1);
            } else if ((pos = preparedFileName.lastIndexOf('/')) != -1) {
                preparedFileName = preparedFileName.substring(pos + 1);
            }
            // TODO: Ensure that filename is not transfer-encoded
            // preparedFileName = CodecUtils.decode(preparedFileName,
            // ServerConfig
            // .getProperty(ServerConfig.Property.DefaultEncoding));
        }
        return preparedFileName;
    }

    @Override
    public InputStream getStream() throws IOException {
        return stream;
    }

    /**
     * Sets the stream
     *
     * @param stream The stream to set
     */
    public void setStream(InputStream stream) {
        this.stream = stream;
    }

}
