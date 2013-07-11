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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileFieldHandler;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.meta.FileFieldGet;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link FileMetadataWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileMetadataWriter {

    /**
     * The logger constant.
     */
    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FileMetadataWriter.class));

    /**
     * The {@link JSONHandler} constant.
     */
    protected static final JSONHandler JSON = new JSONHandler();

    public JSONArray write(final SearchIterator<File> files, final List<File.Field> columns, final TimeZone timeZone) throws OXException {
        final JSONArray array = new JSONArray(32);
        while (files.hasNext()) {
            array.put(writeArray(files.next(), columns, timeZone));
        }
        files.close();
        return array;
    }

    public JSONArray writeArray(final File f, final List<File.Field> columns, final TimeZone tz) {
        final JSONArray array = new JSONArray(columns.size());
        for (final Field field : columns) {
            array.put( writeAttribute(f, field, tz));
        }
        return array;
    }


    private Object writeAttribute(final File f, final Field field, final TimeZone tz) {
        return field.handle(JSON, f, tz);
    }


    private static class JSONHandler extends AbstractFileFieldHandler {

        private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

        private final FileFieldGet get = new FileFieldGet();

        protected JSONHandler() {
            super();
        }

        @Override
        public Object handle(final Field field, final Object... args) {
            final Object value = field.doSwitch(get, args);
            if (File.Field.FILE_MIMETYPE == field) {
                if (null == value) {
                    return value;
                }
                final String ct = value.toString();
                if (ct.indexOf(';') <= 0) {
                    return value;
                }
                try {
                    return ContentType.getBaseType(ct);
                } catch (final OXException e) {
                    return value;
                }
            }
            if ((value == null) && (field == File.Field.LOCKED_UNTIL)) {
                return Integer.valueOf(0);
            }
            if (Date.class.isInstance(value)) {
                final Date d = (Date) value;
                TimeZone tz = get(1, TimeZone.class, args);
                if (field == Field.LAST_MODIFIED_UTC) {
                    tz = UTC;
                }
                if (field == File.Field.LOCKED_UNTIL && (d == null || d.getTime() <= System.currentTimeMillis())) {
                    return Integer.valueOf(0);
                }
                return writeDate((Date) value, tz);
            }

            switch (field) {
            case CATEGORIES:
                return handleCategories((String) value);
            case META:
                try {
                    if (value == null) {
                        return null;
                    }
                    return JSONCoercion.coerceToJSON(value);
                } catch (JSONException e) {
                    LOG.error(e.getMessage(), e);
                    return null;
                }
            default: // do nothing;
            }

            return value;
        }

        private Object writeDate(final Date date, final TimeZone tz) {
            final int offset = (tz == null) ? 0 : tz.getOffset(date.getTime());
            long time = date.getTime() + offset;
            // Happens on infinite locks.
            if (time < 0) {
                time = Long.MAX_VALUE;
            }
            return Long.valueOf(time);
        }

        private JSONArray handleCategories(final String value) {
            if (value == null) {
                return null;
            }
            final String[] strings = Strings.splitByComma(value);
            final JSONArray array = new JSONArray();
            for (final String string : strings) {
                array.put(string);
            }

            return array;
        }

    }

    public JSONObject write(final File file, final TimeZone timezone) {
        return File.Field.inject(new AbstractFileFieldHandler() {

            @Override
            public Object handle(final Field field, final Object... args) {
                final JSONObject o = get(0, JSONObject.class, args);
                try {
                    o.put(field.getName(), JSON.handle(field, file, timezone));
                } catch (final JSONException e) {
                    LOG.error("Error writing field: "+field.getName()+": "+e.getMessage(), e);
                }
                return o;
            }

        }, new JSONObject());
    }

}
