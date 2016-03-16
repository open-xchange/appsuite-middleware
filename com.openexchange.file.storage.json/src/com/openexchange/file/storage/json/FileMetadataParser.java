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

import java.util.ArrayList;
import java.util.List;
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
        final DefaultFile file = new DefaultFile();

        try {
        	JSONObject purged = new JSONObject(object);
        	if (purged.has("last_modified")) {
        		purged.remove("last_modified");
        	}
        	File.Field.inject(jsonHandler, file, purged);
        } catch (final RuntimeException x) {
            Throwable cause = x.getCause();
            if(cause != null) {
                if (OXException.class.isInstance(cause)) {
                    throw (OXException) cause;
                } else if (JSONException.class.isInstance(cause)) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(cause.getMessage());
                }
            }
            throw x;
        }

        return file;
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
            if(!object.has(field.getName())) {
                return md;
            }

            try {
                Object value = object.get(field.getName());

                value = process(field, value);

                field.doSwitch(set, md, value);
            } catch (final JSONException x) {
                throw new RuntimeException(x);
            } catch (OXException x) {
                throw new RuntimeException(x);
            }


            return md;
        }

        private Object process(final Field field, final Object value) throws JSONException, OXException {
            return FileMetadataFieldParser.convert(field, value);
        }
    }

    @Override
    public List<Field> getFields(final JSONObject object) {
        return File.Field.inject(new AbstractFileFieldHandler() {

            @Override
            public Object handle(final Field field, final Object... args) {
                final List<File.Field> fields = (List<File.Field>) args[0];
                if(object.has(field.getName())) {
                    fields.add(field);
                }
                return fields;
            }

        }, new ArrayList<File.Field>());
    }

}
