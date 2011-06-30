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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.file.storage.AbstractFileFieldHandler;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.meta.FileFieldGet;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link FileMetadataWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileMetadataWriter {
    private static final Log LOG = LogFactory.getLog(FileMetadataWriter.class);
    
    private static final JSONHandler JSON = new JSONHandler();
    
    public JSONArray write(SearchIterator<File> files, List<File.Field> columns, TimeZone timeZone) throws AbstractOXException {
        JSONArray array = new JSONArray();
        while (files.hasNext()) {
            array.put(writeArray(files.next(), columns, timeZone));
        }
        return array;
    }

    public JSONArray writeArray(File f, List<File.Field> columns, TimeZone tz) {
        JSONArray array = new JSONArray();
        for (Field field : columns) {
            array.put( writeAttribute(f, field, tz));
        }
        return array;
    }
    
    
    private Object writeAttribute(File f, Field field, TimeZone tz) {
        return field.handle(JSON, f, tz);
    }


    public static class JSONHandler extends AbstractFileFieldHandler {
        private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

        private FileFieldGet get = new FileFieldGet();

        public Object handle(Field field, Object... args) {
            Object value = field.doSwitch(get, args);
            if(value == null && field == File.Field.LOCKED_UNTIL) {
                return 0;
            }
            if(Date.class.isInstance(value)) {
                Date d = (Date) value;
                TimeZone tz = get(1, TimeZone.class, args);
                if(field == Field.LAST_MODIFIED_UTC) {
                    tz = UTC;
                }
                if(field == File.Field.LOCKED_UNTIL && (d == null || d.getTime() <= System.currentTimeMillis())) {
                    return 0;
                } 
                return writeDate((Date) value, tz);
            }
            
            switch(field) {
            case CATEGORIES: return handleCategories((String) value);
            default: // do nothing;
            }
            
            return value;
        }

        private Object writeDate(Date date, TimeZone tz) {
            final int offset = (tz == null) ? 0 : tz.getOffset(date.getTime());
            long time = date.getTime()+offset;
            // Happens on infinite locks.
            if(time < 0) {
                time = Long.MAX_VALUE;
            }
            return time;
        }

        private JSONArray handleCategories(String value) {
            if(value == null) {
                return null;
            }
            String[] strings = value.split("\\s*,\\s*");
            JSONArray array = new JSONArray();
            for (String string : strings) {
                array.put(string);
            }
            
            return array;
        }
        
    }


    public JSONObject write(final File file, final TimeZone timezone) {
        return File.Field.inject(new AbstractFileFieldHandler() {

            public Object handle(Field field, Object... args) {
                JSONObject o = get(0, JSONObject.class, args);
                try {
                    o.put(field.getName(), JSON.handle(field, file, timezone));
                } catch (JSONException e) {
                    LOG.error("Error writing field: "+field.getName()+": "+e.getMessage(), e);
                }
                return o;
            }
            
        }, new JSONObject());
    }
}
