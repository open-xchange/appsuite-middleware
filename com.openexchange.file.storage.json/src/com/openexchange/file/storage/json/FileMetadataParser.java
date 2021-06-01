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
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileFieldHandler;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.meta.FileFieldSet;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileMetadataParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileMetadataParser implements FileMetadataParserService{


    private static final FileMetadataParser instance = new FileMetadataParser();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static FileMetadataParser getInstance() {
        return instance;
    }

    private final JSONParserHandler jsonHandler;

    private FileMetadataParser() {
        super();
        jsonHandler = new JSONParserHandler();
    }

    @Override
    public File parse(final JSONObject object) throws OXException {
        return parse(object, null);
    }

    public File parse(JSONObject object, TimeZone timeZone) throws OXException {
        if (null == object) {
            return null;
        }
        try {
            return File.Field.inject(jsonHandler, new DefaultFile(), object, timeZone);
        } catch (RuntimeException x) {
            Throwable cause = x.getCause();
            if (cause != null) {
                if (OXException.class.isInstance(cause)) {
                    throw (OXException) cause;
                } else if (JSONException.class.isInstance(cause)) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(cause.getMessage());
                }
            }
            throw x;
        }
    }

    private static final class JSONParserHandler extends AbstractFileFieldHandler {

        private final FileFieldSet set;

        protected JSONParserHandler() {
            super();
            set = new FileFieldSet();
        }

        @Override
        public Object handle(final Field field, final Object... args) {
            final File md = md(args);
            final JSONObject object = get(1, JSONObject.class, args);
            if (!object.has(field.getName())) {
                return md;
            }
            TimeZone timeZone = get(2, TimeZone.class, args);
            try {
                Object value = object.get(field.getName());

                value = process(field, value, timeZone);

                field.doSwitch(set, md, value);
            } catch (JSONException x) {
                throw new RuntimeException(x);
            } catch (OXException x) {
                throw new RuntimeException(x);
            }


            return md;
        }

        private Object process(final Field field, final Object value, TimeZone timeZone) throws JSONException, OXException {
            return FileMetadataFieldParser.convert(field, value, timeZone);
        }
    }

    @Override
    public List<Field> getFields(final JSONObject object) {
        return File.Field.inject(new AbstractFileFieldHandler() {

            @Override
            public Object handle(final Field field, final Object... args) {
                final List<File.Field> fields = (List<File.Field>) args[0];
                if (object.has(field.getName())) {
                    fields.add(field);
                }
                return fields;
            }

        }, new ArrayList<File.Field>());
    }

}
