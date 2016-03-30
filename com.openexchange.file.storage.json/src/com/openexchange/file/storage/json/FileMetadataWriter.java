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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileFieldHandler;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileFieldHandler;
import com.openexchange.file.storage.json.actions.files.AJAXInfostoreRequest;
import com.openexchange.file.storage.json.osgi.FileFieldCollector;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link FileMetadataWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileMetadataWriter {

    /**
     * The logger constant.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileMetadataWriter.class);

    private final FileFieldCollector fieldCollector;

    /**
     * Initializes a new {@link FileMetadataWriter}.
     *
     * @param fieldCollector The collector for additional file fields, or <code>null</code> if not available
     */
    public FileMetadataWriter(FileFieldCollector fieldCollector) {
        super();
        this.fieldCollector = fieldCollector;
    }

    /**
     * Serializes a single file with all metadata to JSON.
     *
     * @param request The underlying infostore request
     * @param file The file to write
     * @return A JSON object holding the serialized file
     */
    public JSONObject write(final AJAXInfostoreRequest request, final File file) {
        /*
         * serialize regular fields
         */
        final JsonFieldHandler handler = new JsonFieldHandler(request);
        JSONObject jsonObject = File.Field.inject(getJsonHandler(file, handler), new JSONObject());
        /*
         * render additional fields if available
         */
        if (null != fieldCollector) {
            List<AdditionalFileField> additionalFields = fieldCollector.getFields();
            for (AdditionalFileField additionalField : additionalFields) {
                try {
                    Object value = additionalField.getValue(file, request.getSession());
                    jsonObject.put(additionalField.getColumnName(), additionalField.renderJSON(request.getRequestData(), value));
                } catch (JSONException e) {
                    LOG.error("Error writing field: {}", additionalField.getColumnName(), e);
                }
            }
        }
        return jsonObject;
    }

    /**
     * Serializes a single file with a specific field set.
     *
     * @param request The underlying infostore request
     * @param file The file to write
     * @param fields The basic file fields to write
     * @param additionalFields The column IDs of additional file fields to write; may be <code>null</code>
     * @return A JSON object holding the serialized file
     */
    public JSONObject writeSpecific(final AJAXInfostoreRequest request, final File file, final Field[] fields, final int[] additionalColumns) {
        /*
         * serialize regular fields
         */
        JSONObject jsonObject = new JSONObject();
        FileFieldHandler jsonHandler = getJsonHandler(file, new JsonFieldHandler(request));
        for (Field field : fields) {
            field.handle(jsonHandler, jsonObject);
        }

        /*
         * render additional fields if available
         */
        if (null != fieldCollector && additionalColumns != null && additionalColumns.length > 0) {
            List<AdditionalFileField> additionalFields = fieldCollector.getFields(additionalColumns);
            for (AdditionalFileField additionalField : additionalFields) {
                try {
                    Object value = additionalField.getValue(file, request.getSession());
                    jsonObject.put(additionalField.getColumnName(), additionalField.renderJSON(request.getRequestData(), value));
                } catch (JSONException e) {
                    LOG.error("Error writing field: {}", additionalField.getColumnName(), e);
                }
            }
        }
        return jsonObject;
    }

    /**
     * Serializes all files from a search iterator to JSON.
     *
     * @param request The underlying infostore request
     * @param searchIterator A search iterator for the files to write
     * @return A JSON array holding all serialized files based on the requested columns
     */
    public JSONArray write(AJAXInfostoreRequest request, SearchIterator<File> searchIterator) throws OXException {
        int[] columns = request.getRequestedColumns();
        List<Field> fields = Field.get(columns);
        try {
            if (columns.length == fields.size()) {
                /*
                 * prefer to write iteratively if only regular file fields requested
                 */
                JsonFieldHandler handler = new JsonFieldHandler(request);
                JSONArray filesArray = new JSONArray(32);
                while (searchIterator.hasNext()) {
                    filesArray.put(writeArray(handler, searchIterator.next(), fields));
                }
                return filesArray;
            } else {
                /*
                 * convert pre-loaded files to allow batch retrieval for additional fields
                 */
                return write(request, SearchIterators.asList(searchIterator));
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    /**
     * Serializes a list of files to JSON.
     *
     * @param request The underlying infostore request
     * @param files The files to write
     * @return A JSON array holding all serialized files based on the requested columns
     */
    public JSONArray write(AJAXInfostoreRequest request, List<File> files) throws OXException {
        /*
         * pre-load additional field values
         */
        int[] columns = request.getRequestedColumns();
        Map<Integer, List<Object>> additionalFieldValues = null;
        if (null != fieldCollector) {
            List<AdditionalFileField> additionalFields = fieldCollector.getFields(columns);
            if (0 < additionalFields.size()) {
                additionalFieldValues = new HashMap<Integer, List<Object>>(additionalFields.size());
                for (AdditionalFileField additionalField : additionalFields) {
                    List<Object> values = additionalField.getValues(files, request.getSession());
                    additionalFieldValues.put(Integer.valueOf(additionalField.getColumnID()), values);
                }
            }
        }
        /*
         * serialize each file to json
         */
        JsonFieldHandler handler = new JsonFieldHandler(request);
        JSONArray filesArray = new JSONArray(files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            JSONArray fileArray = new JSONArray(columns.length);
            for (int column : columns) {
                Field field = Field.get(column);
                if (null != field) {
                    fileArray.put(field.handle(handler, file));
                } else {
                    List<Object> fieldValues = null != additionalFieldValues ? additionalFieldValues.get(Integer.valueOf(column)) : null;
                    if (null != fieldValues) {
                        fileArray.put(fieldCollector.getField(column).renderJSON(request.getRequestData(), fieldValues.get(i)));
                    } else {
                        fileArray.put(JSONObject.NULL);
                    }
                }
            }
            filesArray.put(fileArray);
        }
        return filesArray;
    }

    JSONArray writeArray(JsonFieldHandler handler, File f, List<File.Field> columns) {
        JSONArray array = new JSONArray(columns.size());
        for (Field field : columns) {
            array.put(field.handle(handler, f));
        }
        return array;
    }

    private static FileFieldHandler getJsonHandler(final File file, final JsonFieldHandler fieldHandler) {
        return new AbstractFileFieldHandler() {
            @Override
            public Object handle(Field field, Object... args) {
                JSONObject jsonObject = get(0, JSONObject.class, args);
                try {
                    jsonObject.put(field.getName(), fieldHandler.handle(field, file));
                } catch (JSONException e) {
                    LOG.error("Error writing field: {}", field.getName(), e);
                }
                return jsonObject;
            }
        };
    }

}
