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

package com.openexchange.groupware.upload.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.groupware.upload.UploadFile;

/**
 * {@link UploadFileImpl} - Represents an uploaded file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UploadFileImpl implements UploadFile {

    private String fieldName;
    private String fileName;
    private String preparedFileName;
    private File tmpFile;
    private String contentType;
    private String contentId;
    private long size;

    /**
     * Initializes a new {@link UploadFileImpl}.
     */
    public UploadFileImpl() {
        super();
    }

    @Override
    public InputStream openStream() throws IOException {
        if (null == tmpFile) {
            throw new IOException("No such file");
        }
        return new FileInputStream(tmpFile);
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getFileName() {
        return fileName;
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
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public File getTmpFile() {
        return tmpFile;
    }

    @Override
    public void setTmpFile(final File tmpFile) {
        this.tmpFile = tmpFile;
    }

    @Override
    public String toString() {
        String fileName = getPreparedFileName();
        if (fileName != null) {
            return fileName;
        } else if (fieldName != null) {
            return fieldName;
        }
        return super.toString();
    }

}
