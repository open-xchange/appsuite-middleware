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

package com.openexchange.file.storage.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileFieldHandler;
import com.openexchange.file.storage.FileFieldSwitcher;

/**
 * {@link FileFieldHandling}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileFieldHandling {

    public static FileFieldHandler toHandler(final FileFieldSwitcher switcher) {
        return new SwitcherHandler(switcher);
    }

    public static FileFieldSwitcher toSwitcher(final FileFieldHandler handler) {
        return new HandlerSwitcher(handler);
    }

    public static void copy(final File orig, final File dest, final File.Field... fields) {
        final FileFieldGet get = new FileFieldGet();
        final FileFieldSet set = new FileFieldSet();

        for (final Field field : fields) {
            field.doSwitch(set, dest, field.doSwitch(get, orig));
        }
    }

    public static void copy(final File orig, final File dest) {
        copy(orig, dest, Field.values());
    }

    public static File dup(final File orig) {
        final DefaultFile copy = new DefaultFile();
        copy(orig, copy);
        return copy;
    }

    public static Map<String, Object> toMap(final File file, final File.Field... fields) {
        final Map<String, Object> map = new HashMap<String, Object>();
        final FileFieldGet get = new FileFieldGet();

        for (final Field field : fields) {
            final Object value = field.doSwitch(get, file);
            map.put(field.getName(), value);
        }

        return map;
    }

    public static Map<String, Object> toMap(final File file) {
        return toMap(file, Field.values());
    }

    public static String toString(final File file) {
        return toMap(file).toString();
    }

    public static void fromMap(final Map<String, Object> map, final File file, final List<Field> foundFields) {
        final FileFieldSet set = new FileFieldSet();

        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final Field field = Field.get(entry.getKey());
            foundFields.add(field);
            field.doSwitch(set, file, entry.getValue());
        }
    }

    public static void fromMap(final Map<String, Object> map, final File file) {
        fromMap(map, file, new ArrayList<Field>(map.size()));
    }

    public static List<Object> toList(final File file, final File.Field... fields) {
        final FileFieldGet get = new FileFieldGet();
        final List<Object> list = new ArrayList<Object>(fields.length);
        for (final Field field : fields) {
            list.add(field.doSwitch(get, file));
        }
        return list;
    }

    private static class SwitcherHandler implements FileFieldHandler {

        private final FileFieldSwitcher switcher;

        public SwitcherHandler(final FileFieldSwitcher switcher) {
            this.switcher = switcher;
        }

        @Override
        public Object handle(final Field field, final Object... args) {
            return field.doSwitch(switcher, args);
        }
    }

    private static class HandlerSwitcher implements FileFieldSwitcher {

        private final FileFieldHandler handler;

        public HandlerSwitcher(final FileFieldHandler handler) {
            this.handler = handler;
        }

        @Override
        public Object categories(final Object... args) {
            return handler.handle(Field.CATEGORIES, args);
        }

        @Override
        public Object colorLabel(final Object... args) {
            return handler.handle(Field.COLOR_LABEL, args);
        }

        @Override
        public Object content(final Object... args) {
            return handler.handle(Field.CONTENT, args);
        }

        @Override
        public Object created(final Object... args) {
            return handler.handle(Field.CREATED, args);
        }

        @Override
        public Object createdBy(final Object... args) {
            return handler.handle(Field.CREATED_BY, args);
        }

        @Override
        public Object currentVersion(final Object... args) {
            return handler.handle(Field.CURRENT_VERSION, args);
        }

        @Override
        public Object description(final Object... args) {
            return handler.handle(Field.DESCRIPTION, args);
        }

        @Override
        public Object fileMd5sum(final Object... args) {
            return handler.handle(Field.FILE_MD5SUM, args);
        }

        @Override
        public Object fileMimetype(final Object... args) {
            return handler.handle(Field.FILE_MIMETYPE, args);
        }

        @Override
        public Object fileSize(final Object... args) {
            return handler.handle(Field.FILE_SIZE, args);
        }

        @Override
        public Object filename(final Object... args) {
            return handler.handle(Field.FILENAME, args);
        }

        @Override
        public Object folderId(final Object... args) {
            return handler.handle(Field.FOLDER_ID, args);
        }

        @Override
        public Object id(final Object... args) {
            return handler.handle(Field.ID, args);
        }

        @Override
        public Object lastModified(final Object... args) {
            return handler.handle(Field.LAST_MODIFIED, args);
        }

        @Override
        public Object lastModifiedUtc(final Object... args) {
            return handler.handle(Field.LAST_MODIFIED_UTC, args);
        }

        @Override
        public Object lockedUntil(final Object... args) {
            return handler.handle(Field.LOCKED_UNTIL, args);
        }

        @Override
        public Object modifiedBy(final Object... args) {
            return handler.handle(Field.MODIFIED_BY, args);
        }

        @Override
        public Object numberOfVersions(final Object... args) {
            return handler.handle(Field.NUMBER_OF_VERSIONS, args);
        }

        @Override
        public Object sequenceNumber(final Object... args) {
            return handler.handle(Field.SEQUENCE_NUMBER, args);
        }

        @Override
        public Object title(final Object... args) {
            return handler.handle(Field.TITLE, args);
        }

        @Override
        public Object url(final Object... args) {
            return handler.handle(Field.URL, args);
        }

        @Override
        public Object version(final Object... args) {
            return handler.handle(Field.VERSION, args);
        }

        @Override
        public Object versionComment(final Object... args) {
            return handler.handle(Field.VERSION_COMMENT, args);
        }

        @Override
        public Object meta(Object... args) {
            return handler.handle(Field.META, args);
        }

        @Override
        public Object objectPermissions(Object... args) {
            return handler.handle(Field.OBJECT_PERMISSIONS, args);
        }

        @Override
        public Object shareable(Object... args) {
            return handler.handle(Field.SHAREABLE, args);
        }

    }
}
