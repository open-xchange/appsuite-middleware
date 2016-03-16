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

package com.openexchange.groupware.infostore.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.infostore.InfostoreStrings;
import com.openexchange.java.Strings;

public class Metadata {

    public static final int LAST_MODIFIED = 5;
    public static final int CREATION_DATE = 4;
    public static final int MODIFIED_BY = 3;
    public static final int FOLDER_ID = 20;
    public static final int TITLE = 700;
    public static final int VERSION = 705;
    public static final int CONTENT = 750;
    public static final int FILESTORE_LOCATION=752;
    public static final int FILENAME = 702;
    public static final int SEQUENCE_NUMBER = 751;
    public static final int ID = 1;
    public static final int FILE_SIZE = 704;
    public static final int FILE_MIMETYPE = 703;
    public static final int DESCRIPTION = 706;
    public static final int LOCKED_UNTIL = 707;
    public static final int URL = 701;
    public static final int CREATED_BY = 2;
    public static final int CATEGORIES = 100;
    public static final int FILE_MD5SUM = 708;
    public static final int VERSION_COMMENT = 709;
    public static final int CURRENT_VERSION = 710;
    public static final int NUMBER_OF_VERSIONS = 711;
    public static final int COLOR_LABEL = 102;
    public static final int LAST_MODIFIED_UTC = 6;
    public static final int META = 23;
    public static final int OBJECT_PERMISSIONS = 108;
    public static final int SHAREABLE = 109;


    public static final Metadata LAST_MODIFIED_LITERAL = new Metadata(LAST_MODIFIED, "last_modified");
    public static final Metadata CREATION_DATE_LITERAL = new Metadata(CREATION_DATE, "creation_date");
    public static final Metadata MODIFIED_BY_LITERAL = new Metadata(MODIFIED_BY, "modified_by");
    public static final Metadata FOLDER_ID_LITERAL = new Metadata(FOLDER_ID, "folder_id");
    public static final Metadata TITLE_LITERAL = new Metadata(TITLE, "title", InfostoreStrings.FIELD_TITLE);
    public static final Metadata VERSION_LITERAL = new Metadata(VERSION, "version");
    public static final Metadata CONTENT_LITERAL = new Metadata(CONTENT, "content");
    public static final Metadata ID_LITERAL = new Metadata(ID, "id");
    public static final Metadata FILE_SIZE_LITERAL = new Metadata(FILE_SIZE, "file_size");
    public static final Metadata DESCRIPTION_LITERAL = new Metadata(DESCRIPTION, "description", InfostoreStrings.FIELD_DESCRIPTION);
    public static final Metadata URL_LITERAL = new Metadata(URL, "url");
    public static final Metadata CREATED_BY_LITERAL = new Metadata(CREATED_BY, "created_by");
    public static final Metadata FILENAME_LITERAL = new Metadata(FILENAME, "filename", InfostoreStrings.FIELD_FILE_NAME);
    public static final Metadata FILE_MIMETYPE_LITERAL = new Metadata(FILE_MIMETYPE, "file_mimetype");
    public static final Metadata SEQUENCE_NUMBER_LITERAL = new Metadata(SEQUENCE_NUMBER, "sequence_number");
    public static final Metadata CATEGORIES_LITERAL = new Metadata(CATEGORIES, "categories");
    public static final Metadata LOCKED_UNTIL_LITERAL = new Metadata(LOCKED_UNTIL, "locked_until");
    public static final Metadata FILE_MD5SUM_LITERAL = new Metadata(FILE_MD5SUM, "file_md5sum");
    public static final Metadata VERSION_COMMENT_LITERAL = new Metadata(VERSION_COMMENT, "version_comment", InfostoreStrings.FIELD_VERSION_COMMENT);
    public static final Metadata CURRENT_VERSION_LITERAL = new Metadata(CURRENT_VERSION, "current_version");
    public static final Metadata COLOR_LABEL_LITERAL = new Metadata(COLOR_LABEL, "color_label");
    public static final Metadata FILESTORE_LOCATION_LITERAL = new Metadata(FILESTORE_LOCATION, "filestore_location");
    public static final Metadata LAST_MODIFIED_UTC_LITERAL = new Metadata(LAST_MODIFIED_UTC, "last_modified_utc");
    public static final Metadata NUMBER_OF_VERSIONS_LITERAL = new Metadata(NUMBER_OF_VERSIONS, "number_of_versions");
    public static final Metadata META_LITERAL = new Metadata(META, "meta");
    public static final Metadata OBJECT_PERMISSIONS_LITERAL = new Metadata(OBJECT_PERMISSIONS, "object_permissions");
    public static final Metadata SHAREABLE_LITERAL = new Metadata(SHAREABLE, "shareable");


    public static final Metadata[] VALUES_ARRAY = new Metadata[]{
        LAST_MODIFIED_LITERAL,
        CREATION_DATE_LITERAL,
        MODIFIED_BY_LITERAL,
        FOLDER_ID_LITERAL,
        TITLE_LITERAL,
        VERSION_LITERAL,
        CONTENT_LITERAL,
        ID_LITERAL,
        FILE_SIZE_LITERAL,
        DESCRIPTION_LITERAL,
        URL_LITERAL,
        CREATED_BY_LITERAL,
        FILENAME_LITERAL,
        FILE_MIMETYPE_LITERAL,
        SEQUENCE_NUMBER_LITERAL,
        CATEGORIES_LITERAL,
        LOCKED_UNTIL_LITERAL,
        FILE_MD5SUM_LITERAL,
        VERSION_COMMENT_LITERAL,
        CURRENT_VERSION_LITERAL,
        COLOR_LABEL_LITERAL,
        FILESTORE_LOCATION_LITERAL,
        LAST_MODIFIED_UTC_LITERAL,
        NUMBER_OF_VERSIONS_LITERAL,
        META_LITERAL,
        OBJECT_PERMISSIONS_LITERAL,
        SHAREABLE_LITERAL
    };

    public static final Metadata[] HTTPAPI_VALUES_ARRAY = new Metadata[]{
        LAST_MODIFIED_LITERAL,
        CREATION_DATE_LITERAL,
        MODIFIED_BY_LITERAL,
        FOLDER_ID_LITERAL,
        TITLE_LITERAL,
        VERSION_LITERAL,
        CONTENT_LITERAL,
        ID_LITERAL,
        FILE_SIZE_LITERAL,
        DESCRIPTION_LITERAL,
        URL_LITERAL,
        CREATED_BY_LITERAL,
        FILENAME_LITERAL,
        FILE_MIMETYPE_LITERAL,
        CATEGORIES_LITERAL,
        LOCKED_UNTIL_LITERAL,
        FILE_MD5SUM_LITERAL,
        VERSION_COMMENT_LITERAL,
        CURRENT_VERSION_LITERAL,
        COLOR_LABEL_LITERAL,
        LAST_MODIFIED_UTC_LITERAL,
        NUMBER_OF_VERSIONS_LITERAL,
        META_LITERAL,
        OBJECT_PERMISSIONS_LITERAL,
        SHAREABLE_LITERAL
    };

    public static final List<Metadata> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));
    public static final List<Metadata> HTTPAPI_VALUES = Collections.unmodifiableList(Arrays.asList(HTTPAPI_VALUES_ARRAY));

    private final String name;
    private final String displayName;
    private final int id;

    private Metadata(int id, String name) {
        this(id, name, null);
    }

    private Metadata(int id, String name, String displayName) {
        super();
        this.name = name;
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return Strings.toUpperCase(name);
    }

    public String getName(){
        return name;
    }

    public int getId(){
        return id;
    }

    /**
     * Gets the optional display name
     *
     * @return The display name or <code>null</code>
     */
    public String getDisplayName() {
        return displayName;
    }

    public static Metadata get(final int id){
        switch(id){
        case LAST_MODIFIED : return LAST_MODIFIED_LITERAL;
        case CREATION_DATE : return CREATION_DATE_LITERAL;
        case MODIFIED_BY: return MODIFIED_BY_LITERAL;
        case FOLDER_ID: return FOLDER_ID_LITERAL;
        case TITLE: return TITLE_LITERAL;
        case VERSION: return VERSION_LITERAL;
        case CONTENT: return CONTENT_LITERAL;
        case ID: return ID_LITERAL;
        case FILE_SIZE: return FILE_SIZE_LITERAL;
        case DESCRIPTION : return DESCRIPTION_LITERAL;
        case URL : return URL_LITERAL;
        case CREATED_BY : return CREATED_BY_LITERAL;
        case FILENAME: return FILENAME_LITERAL;
        case FILE_MIMETYPE: return FILE_MIMETYPE_LITERAL;
        case SEQUENCE_NUMBER: return SEQUENCE_NUMBER_LITERAL;
        case CATEGORIES: return CATEGORIES_LITERAL;
        case LOCKED_UNTIL : return LOCKED_UNTIL_LITERAL;
        case FILE_MD5SUM : return FILE_MD5SUM_LITERAL;
        case VERSION_COMMENT: return VERSION_COMMENT_LITERAL;
        case CURRENT_VERSION: return CURRENT_VERSION_LITERAL;
        case COLOR_LABEL: return COLOR_LABEL_LITERAL;
        case FILESTORE_LOCATION : return FILESTORE_LOCATION_LITERAL;
        case LAST_MODIFIED_UTC : return LAST_MODIFIED_UTC_LITERAL;
        case NUMBER_OF_VERSIONS : return NUMBER_OF_VERSIONS_LITERAL;
        case META : return META_LITERAL;
        case OBJECT_PERMISSIONS: return OBJECT_PERMISSIONS_LITERAL;
        case SHAREABLE: return SHAREABLE_LITERAL;
        default : return null;
        }
    }

    /**
     * Gets the metadata for given identifier
     *
     * @param s The identifier
     * @return The metadata or <code>null</code>
     */
    public static Metadata get(String s) {
        if (null == s) {
            return null;
        }
        for (Metadata metadata : VALUES) {
            if (metadata.getName().equals(s)) {
                return metadata;
            }
        }
        return null;
    }

    public Object doSwitch(final MetadataSwitcher switcher){
        switch(id){
        case LAST_MODIFIED : return switcher.lastModified();
        case CREATION_DATE : return switcher.creationDate();
        case MODIFIED_BY: return switcher.modifiedBy();
        case FOLDER_ID: return switcher.folderId();
        case TITLE: return switcher.title();
        case VERSION: return switcher.version();
        case CONTENT: return switcher.content();
        case ID: return switcher.id();
        case FILE_SIZE: return switcher.fileSize();
        case DESCRIPTION : return switcher.description();
        case URL : return switcher.url();
        case CREATED_BY : return switcher.createdBy();
        case FILENAME: return switcher.fileName();
        case FILE_MIMETYPE: return switcher.fileMIMEType();
        case SEQUENCE_NUMBER: return switcher.sequenceNumber();
        case CATEGORIES : return switcher.categories();
        case LOCKED_UNTIL: return switcher.lockedUntil();
        case FILE_MD5SUM: return switcher.fileMD5Sum();
        case VERSION_COMMENT: return switcher.versionComment();
        case CURRENT_VERSION: return switcher.currentVersion();
        case COLOR_LABEL: return switcher.colorLabel();
        case FILESTORE_LOCATION : return switcher.filestoreLocation();
        case LAST_MODIFIED_UTC : return switcher.lastModifiedUTC();
        case NUMBER_OF_VERSIONS : return switcher.numberOfVersions();
        case META : return switcher.meta();
        case OBJECT_PERMISSIONS: return switcher.objectPermissions();
        case SHAREABLE: return switcher.shareable();
        default : return null;
        }
    }

    public static AttachmentField getAttachmentField(final Metadata attachmentCompatible) {
        switch(attachmentCompatible.getId()) {
        case FILENAME : return AttachmentField.FILENAME_LITERAL;
        case FILE_SIZE : return AttachmentField.FILE_SIZE_LITERAL;
        case FILE_MIMETYPE : return AttachmentField.FILE_MIMETYPE_LITERAL;
        case TITLE : return AttachmentField.FILENAME_LITERAL;
        case DESCRIPTION : return AttachmentField.COMMENT_LITERAL;
        default : return null;
        }
    }

    public static int[] columns(Metadata[] metadata) {
        int[] columns = new int[metadata.length];
        for (int i = 0; i < metadata.length; i++) {
            Metadata m = metadata[i];
            columns[i] = m.getId();
        }

        return columns;
    }

}
