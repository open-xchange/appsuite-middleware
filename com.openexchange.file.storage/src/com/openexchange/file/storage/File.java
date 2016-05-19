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

package com.openexchange.file.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link File} represents the meta data known about a file
 */
public interface File {

    /**
     * The default search fields: {@link Field#TITLE}, {@link Field#FILENAME}, {@link Field#DESCRIPTION}, {@link Field#URL}, {@link Field#CATEGORIES}, {@link Field#VERSION_COMMENT}
     */
    public static final Set<Field> DEFAULT_SEARCH_FIELDS = Collections.unmodifiableSet(EnumSet.of(Field.TITLE, Field.FILENAME, Field.DESCRIPTION, Field.URL, Field.CATEGORIES, Field.VERSION_COMMENT));

    String getProperty(String key);

    Set<String> getPropertyNames();

    Date getLastModified();

    void setLastModified(Date now);

    Date getCreated();

    void setCreated(Date creationDate);

    int getModifiedBy();

    void setModifiedBy(int lastEditor);

    String getFolderId();

    void setFolderId(String folderId);

    String getTitle();

    void setTitle(String title);

    String getVersion();

    void setVersion(String version);

    String getContent();

    long getFileSize();

    void setFileSize(long length);

    String getFileMIMEType();

    void setFileMIMEType(String type);

    String getFileName();

    void setFileName(String fileName);

    String getId();

    void setId(String id);

    int getCreatedBy();

    void setCreatedBy(int cretor);

    String getDescription();

    void setDescription(String description);

    String getURL();

    void setURL(String url);

    long getSequenceNumber();

    String getCategories();

    void setCategories(String categories);

    Date getLockedUntil();

    void setLockedUntil(Date lockedUntil);

    String getFileMD5Sum();

    void setFileMD5Sum(String sum);

    int getColorLabel();

    void setColorLabel(int color);

    boolean isCurrentVersion();

    void setIsCurrentVersion(boolean bool);

    String getVersionComment();

    void setVersionComment(String string);

    void setNumberOfVersions(int numberOfVersions);

    int getNumberOfVersions();

    Map<String, Object> getMeta();

    void setMeta(Map<String, Object> properties);

    /**
     * Checks whether {@link #getFileSize()} returns the exact size w/o any encodings (e.g. base64) applied.
     *
     * @return <code>true</code> for exact size; otherwise <code>false</code>
     */
    boolean isAccurateSize();

    /**
     * Sets whether the {@link #getFileSize()} returns the exact size w/o any encodings (e.g. base64) applied
     *
     * @param accurateSize <code>true</code> for exact size; otherwise <code>false</code>
     */
    void setAccurateSize(boolean accurateSize);

    /**
     * Gets the object permissions in case they are defined.
     *
     * @return A list holding additional object permissions, or <code>null</code> if not defined or not supported by the storage
     */
    List<FileStorageObjectPermission> getObjectPermissions();

    /**
     * Sets the object permissions.
     *
     * @param objectPermissions The object permissions to set, or <code>null</code> to remove previously set permissions
     */
    void setObjectPermissions(List<FileStorageObjectPermission> objectPermissions);

    /**
     * Gets a value indicating whether the item can be shared to others based on underlying storage's capabilities and the permissions of
     * the requesting user.
     *
     * @return <code>true</code> if the file is shareable, <code>false</code>, otherwise
     */
    boolean isShareable();

    /**
     * Sets the flag indicating that the item can be shared to others based on underlying storage's capabilities and the permissions of
     * the requesting user.
     *
     * @param shareable <code>true</code> if the file is shareable, <code>false</code>, otherwise
     */
    void setShareable(boolean shareable);

    File dup();

    void copyInto(File other);

    void copyFrom(File other);

    void copyInto(File other, Field...fields);

    void copyFrom(File other, Field...fields);

    Set<File.Field> differences(File other);

    boolean equals(File other, Field criterium, Field...criteria);

    /**
     * Indicates whether this file matches given pattern.
     *
     * @param pattern The pattern possibly containing wild-card characters
     * @param fields The fields to consider; if <code>null</code> {@link #DEFAULT_SEARCH_FIELDS} is used
     * @return <code>true</code> if this file matches; otherwise <code>false</code>
     */
    boolean matches(String pattern, Field... fields);

    /**
     * An enumeration of file fields.
     */
    public static enum Field {

        LAST_MODIFIED("last_modified", 5),
        CREATED("creation_date", 4),
        MODIFIED_BY("modified_by", 3),
        FOLDER_ID("folder_id", 20),
        TITLE("title", 700),
        VERSION("version", 705),
        CONTENT("content", 750),
        ID("id", 1),
        FILE_SIZE("file_size", 704),
        DESCRIPTION("description", 706),
        URL("url", 701),
        CREATED_BY("created_by", 2),
        FILENAME("filename", 702),
        FILE_MIMETYPE("file_mimetype", 703),
        SEQUENCE_NUMBER("sequence_number", 751),
        CATEGORIES("categories", 100),
        LOCKED_UNTIL("locked_until", 707),
        FILE_MD5SUM("file_md5sum", 708),
        VERSION_COMMENT("version_comment", 709),
        CURRENT_VERSION("current_version", 710),
        COLOR_LABEL("color_label", 102),
        LAST_MODIFIED_UTC("last_modified_utc", 6),
        NUMBER_OF_VERSIONS("number_of_versions", 711),
        META("meta", 23),
        OBJECT_PERMISSIONS("object_permissions", 108),
        SHAREABLE("shareable", 109)
        ;

        private final int number;

        private final String name;

        private Field(final String name, final int number) {
            this.number = number;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getNumber() {
            return number;
        }

        public Object doSwitch(final FileFieldSwitcher switcher, final Object... args) {
            switch (this) {
            case LAST_MODIFIED:
                return switcher.lastModified(args);
            case CREATED:
                return switcher.created(args);
            case MODIFIED_BY:
                return switcher.modifiedBy(args);
            case FOLDER_ID:
                return switcher.folderId(args);
            case TITLE:
                return switcher.title(args);
            case VERSION:
                return switcher.version(args);
            case CONTENT:
                return switcher.content(args);
            case ID:
                return switcher.id(args);
            case FILE_SIZE:
                return switcher.fileSize(args);
            case DESCRIPTION:
                return switcher.description(args);
            case URL:
                return switcher.url(args);
            case CREATED_BY:
                return switcher.createdBy(args);
            case FILENAME:
                return switcher.filename(args);
            case FILE_MIMETYPE:
                return switcher.fileMimetype(args);
            case SEQUENCE_NUMBER:
                return switcher.sequenceNumber(args);
            case CATEGORIES:
                return switcher.categories(args);
            case LOCKED_UNTIL:
                return switcher.lockedUntil(args);
            case FILE_MD5SUM:
                return switcher.fileMd5sum(args);
            case VERSION_COMMENT:
                return switcher.versionComment(args);
            case CURRENT_VERSION:
                return switcher.currentVersion(args);
            case COLOR_LABEL:
                return switcher.colorLabel(args);
            case LAST_MODIFIED_UTC:
                return switcher.lastModifiedUtc(args);
            case NUMBER_OF_VERSIONS:
                return switcher.numberOfVersions(args);
            case META:
                return switcher.meta(args);
            case OBJECT_PERMISSIONS:
                return switcher.objectPermissions(args);
            case SHAREABLE:
                return switcher.shareable(args);
            default:
                throw new IllegalArgumentException("Don't know field: " + getName());
            }
        }

        public static List<Object> forAllFields(final FileFieldSwitcher switcher, final Object... args) {
            final List<Object> retval = new ArrayList<Object>(values().length);
            for (final Field field : values()) {
                retval.add(field.doSwitch(switcher, args));
            }
            return retval;
        }

        public static <T> T inject(final FileFieldSwitcher switcher, T arg, final Object... args) {
            final Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = arg;
            System.arraycopy(args, 0, newArgs, 1, args.length);
            for (final Field field : values()) {
                arg = (T) field.doSwitch(switcher, args);
            }
            return arg;
        }

        public Object handle(final FileFieldHandler handler, final Object... args) {
            return handler.handle(this, args);
        }

        public static List<Object> forAllFields(final FileFieldHandler handler, final Object... args) {
            final List<Object> retval = new ArrayList<Object>(values().length);
            for (final Field field : values()) {
                retval.add(field.handle(handler, args));
            }
            return retval;
        }

        public static <T> T inject(final FileFieldHandler handler, T arg, final Object... args) {
            final Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = arg;
            System.arraycopy(args, 0, newArgs, 1, args.length);
            for (final Field field : values()) {
                arg = (T) field.handle(handler, newArgs);
            }
            return arg;
        }

        private static final Map<String, Field> byName = new HashMap<String, Field>();
        static {
            for (final Field field : values()) {
                byName.put(field.getName(), field);
            }
        }

        private static final Map<Integer, Field> byNumber = new HashMap<Integer, Field>();

        static {
            for (final Field field : values()) {
                byNumber.put(Integer.valueOf(field.getNumber()), field);
            }
        }

        public static Field get(final String key) {
            if(key == null) {
                return null;
            }
            final Field field = byName.get(key);
            if(field != null) {
                 return field;
            }
            try {
                final int number = Integer.parseInt(key);
                return byNumber.get(Integer.valueOf(number));
            } catch (final NumberFormatException x) {
                return null;
            }
        }

        public static List<Field> get(final Collection<String> keys) {
            if (keys == null) {
                return Collections.emptyList();
            }
            final List<Field> retval = new ArrayList<Field>(keys.size());
            for (final String key : keys) {
                retval.add(get(key));
            }
            return retval;
        }

        public static Field get(final int number) {
            return byNumber.get(number);
        }

        public static List<Field> get(final int[] numbers) {
            final List<Field> fields = new ArrayList<Field>(numbers.length);
            for (int number : numbers) {
                Field field = byNumber.get(number);
                if (field != null) {
                    fields.add(field);
                }
            }
            return fields;
        }

    }
}
