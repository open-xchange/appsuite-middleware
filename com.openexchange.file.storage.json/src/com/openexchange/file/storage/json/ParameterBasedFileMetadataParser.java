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

package com.openexchange.file.storage.json;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;

/**
 * {@link ParameterBasedFileMetadataParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ParameterBasedFileMetadataParser {

    private final static ParameterBasedFileMetadataParser INSTANCE = new ParameterBasedFileMetadataParser();

    /**
     * Initializes a new {@link ParameterBasedFileMetadataParser}.
     */
    private ParameterBasedFileMetadataParser() {
        super();
    }

    public final static ParameterBasedFileMetadataParser getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves a {@link File} based on the parameters of the given request
     *
     * @param request The {@link AJAXRequestData} to parse
     * @return The {@link File}
     * @throws OXException in case the file coulnd't be parsed
     */
    public File parse(AJAXRequestData request) throws OXException {
        File result = new DefaultFile();
        result.setFolderId(request.getParameter(File.Field.FOLDER_ID.getName()));
        result.setTitle(request.getParameter(File.Field.TITLE.getName()));
        result.setFileName(request.getParameter(File.Field.FILENAME.getName()));
        result.setFileMIMEType(request.getParameter(File.Field.FILE_MIMETYPE.getName()));

        // Disallow to manually set weird MIME type
        {
            String cts = result.getFileMIMEType();
            try {
                ContentType contentType = new ContentType(cts);
                if (contentType.contains("multipart/") || contentType.containsBoundaryParameter()) {
                    // deny weird MIME types
                    throw FileStorageExceptionCodes.DENIED_MIME_TYPE.create();
                }
            } catch (Exception e) {
                // MIME type could not be safely parsed
                throw FileStorageExceptionCodes.DENIED_MIME_TYPE.create(e, e.getMessage());
            }
        }

        result.setFileMD5Sum(request.getParameter(File.Field.FILE_MD5SUM.getName()));
        result.setDescription(request.getParameter(File.Field.DESCRIPTION.getName()));
        result.setId(request.getParameter(File.Field.ID.getName()));
        result.setURL(request.getParameter(File.Field.URL.getName()));
        result.setVersion(request.getParameter(File.Field.VERSION.getName()));
        result.setVersionComment(request.getParameter(File.Field.VERSION_COMMENT.getName()));
        result.setCategories(request.getParameter(File.Field.CATEGORIES.getName()));
        String colorString = request.getParameter(File.Field.COLOR_LABEL.getName());
        if(Strings.isNotEmpty(colorString)) {
            result.setColorLabel(Integer.valueOf(colorString));
        }
        String sizeString = request.getParameter(File.Field.FILE_SIZE.getName());
        if (Strings.isNotEmpty(sizeString)) {
            result.setFileSize(Long.valueOf(sizeString));
        }
        return result;
    }

}
