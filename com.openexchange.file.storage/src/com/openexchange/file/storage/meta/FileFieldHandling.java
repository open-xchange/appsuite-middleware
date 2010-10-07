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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.file.storage.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileFieldHandler;
import com.openexchange.file.storage.FileFieldSwitcher;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;

/**
 * {@link FileFieldHandling}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileFieldHandling {

    public static FileFieldHandler toHandler(FileFieldSwitcher switcher) {
        return new SwitcherHandler(switcher);
    }

    public static FileFieldSwitcher toSwitcher(FileFieldHandler handler) {
        return new HandlerSwitcher(handler);
    }

    public static void copy(File orig, File dest, File.Field... fields) {
        FileFieldGet get = new FileFieldGet();
        FileFieldSet set = new FileFieldSet();

        for (Field field : fields) {
            field.doSwitch(set, dest, field.doSwitch(get, orig));
        }
    }

    public static void copy(File orig, File dest) {
        copy(orig, dest, Field.values());
    }

    public static File dup(File orig) {
        DefaultFile copy = new DefaultFile();
        copy(orig, copy);
        return copy;
    }

    public static Map<String, Object> toMap(File file, File.Field... fields) {
        Map<String, Object> map = new HashMap<String, Object>();
        FileFieldGet get = new FileFieldGet();

        for (Field field : fields) {
            Object value = field.doSwitch(get, file);
            map.put(field.getName(), value);
        }

        return map;
    }

    public static Map<String, Object> toMap(File file) {
        return toMap(file, Field.values());
    }

    public static String toString(File file) {
        return toMap(file).toString();
    }

    public static void fromMap(Map<String, Object> map, File file, List<Field> foundFields) {
        FileFieldSet set = new FileFieldSet();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Field field = Field.get(entry.getKey());
            foundFields.add(field);
            field.doSwitch(set, file, entry.getValue());
        }
    }
    
    public static void fromMap(Map<String, Object> map, File file) {
        fromMap(map, file, new ArrayList<Field>(map.size()));
    }

    public static List<Object> toList(File file, File.Field... fields) {
        FileFieldGet get = new FileFieldGet();
        List<Object> list = new ArrayList<Object>(fields.length);
        for (Field field : fields) {
            list.add(field.doSwitch(get, file));
        }
        return list;
    }

    private static class SwitcherHandler implements FileFieldHandler {

        private FileFieldSwitcher switcher;

        public SwitcherHandler(FileFieldSwitcher switcher) {
            this.switcher = switcher;
        }

        public Object handle(Field field, Object... args) {
            return field.doSwitch(switcher, args);
        }
    }

    private static class HandlerSwitcher implements FileFieldSwitcher {

        private FileFieldHandler handler;

        public HandlerSwitcher(FileFieldHandler handler) {
            this.handler = handler;
        }

        public Object categories(Object... args) {
            return handler.handle(Field.CATEGORIES, args);
        }

        public Object colorLabel(Object... args) {
            return handler.handle(Field.COLOR_LABEL, args);
        }

        public Object content(Object... args) {
            return handler.handle(Field.CONTENT, args);
        }

        public Object created(Object... args) {
            return handler.handle(Field.CREATED, args);
        }

        public Object createdBy(Object... args) {
            return handler.handle(Field.CREATED_BY, args);
        }

        public Object currentVersion(Object... args) {
            return handler.handle(Field.CURRENT_VERSION, args);
        }

        public Object description(Object... args) {
            return handler.handle(Field.DESCRIPTION, args);
        }

        public Object fileMd5sum(Object... args) {
            return handler.handle(Field.FILE_MD5SUM, args);
        }

        public Object fileMimetype(Object... args) {
            return handler.handle(Field.FILE_MIMETYPE, args);
        }

        public Object fileSize(Object... args) {
            return handler.handle(Field.FILE_SIZE, args);
        }

        public Object filename(Object... args) {
            return handler.handle(Field.FILENAME, args);
        }

        public Object folderId(Object... args) {
            return handler.handle(Field.FOLDER_ID, args);
        }

        public Object id(Object... args) {
            return handler.handle(Field.ID, args);
        }

        public Object lastModified(Object... args) {
            return handler.handle(Field.LAST_MODIFIED, args);
        }

        public Object lastModifiedUtc(Object... args) {
            return handler.handle(Field.LAST_MODIFIED_UTC, args);
        }

        public Object lockedUntil(Object... args) {
            return handler.handle(Field.LOCKED_UNTIL, args);
        }

        public Object modifiedBy(Object... args) {
            return handler.handle(Field.MODIFIED_BY, args);
        }

        public Object numberOfVersions(Object... args) {
            return handler.handle(Field.NUMBER_OF_VERSIONS, args);
        }

        public Object sequenceNumber(Object... args) {
            return handler.handle(Field.SEQUENCE_NUMBER, args);
        }

        public Object title(Object... args) {
            return handler.handle(Field.TITLE, args);
        }

        public Object url(Object... args) {
            return handler.handle(Field.URL, args);
        }

        public Object version(Object... args) {
            return handler.handle(Field.VERSION, args);
        }

        public Object versionComment(Object... args) {
            return handler.handle(Field.VERSION_COMMENT, args);
        }
    }
}
