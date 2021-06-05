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

package com.openexchange.file.storage.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ParameterBasedFileMetadataParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ParameterBasedFileMetadataParser {

    private final static ParameterBasedFileMetadataParser INSTANCE = new ParameterBasedFileMetadataParser();

    //@formatter:off
    private static final List<File.Field> POSSIBLE_FIELDS = Arrays.asList(Field.FOLDER_ID, Field.TITLE, Field.FILENAME, Field.FILE_MIMETYPE, Field.CREATED, Field.LAST_MODIFIED,
        Field.FILE_MD5SUM, Field.DESCRIPTION, Field.ID, Field.VERSION, Field.VERSION_COMMENT, Field.CATEGORIES, Field.COLOR_LABEL, Field.FILE_SIZE, Field.URL);
    //@formatter:on

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
     * @param timezone The user/client timezone to consider
     * @return The {@link File}
     * @throws OXException in case the file coulnd't be parsed
     */
    public File parse(AJAXRequestData request, TimeZone timezone) throws OXException {
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
        if (Strings.isNotEmpty(colorString)) {
            result.setColorLabel(Integer.parseInt(colorString));
        }
        String sizeString = request.getParameter(File.Field.FILE_SIZE.getName());
        if (Strings.isNotEmpty(sizeString)) {
            result.setFileSize(Long.parseLong(sizeString));
        }
        Date created = parseDateParameter(request, File.Field.CREATED.getName(), timezone);
        if (null != created) {
            result.setCreated(created);
        }
        Date lastModified = parseDateParameter(request, File.Field.LAST_MODIFIED.getName(), timezone);
        if (null != lastModified) {
            result.setLastModified(lastModified);
        }
        return result;
    }

    /**
     * Gets the fields from the specified {@link AJAXRequestData}
     *
     * @param request the {@link AJAXRequestData}
     * @return A {@link List} with the set {@link Field}s
     */
    public List<File.Field> getFields(AJAXRequestData request) {
        ArrayList<File.Field> result = new ArrayList<>();
        for (Field field : POSSIBLE_FIELDS) {
            if (request.containsParameter(field.getName())) {
                result.add(field);
            }
        }
        return result;
    }

    private static Date parseDateParameter(AJAXRequestData request, String parameterName, TimeZone timezone) throws OXException {
        String value = request.getParameter(parameterName);
        if (Strings.isEmpty(value)) {
            return null;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, value);
        }
        if (null != timezone) {
            timestamp -= timezone.getOffset(timestamp);
        }
        return new Date(timestamp);
    }

}
