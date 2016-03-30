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

package com.openexchange.file.storage.infostore.internal;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;


/**
 * {@link FieldMapping}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FieldMapping {

    private static final Map<File.Field, Metadata> field2metadata = new EnumMap<File.Field, Metadata>(File.Field.class);

    static {
        field2metadata.put(File.Field.LAST_MODIFIED, Metadata.LAST_MODIFIED_LITERAL);
        field2metadata.put(File.Field.CREATED, Metadata.CREATION_DATE_LITERAL);
        field2metadata.put(File.Field.MODIFIED_BY, Metadata.MODIFIED_BY_LITERAL);
        field2metadata.put(File.Field.FOLDER_ID, Metadata.FOLDER_ID_LITERAL);
        field2metadata.put(File.Field.TITLE, Metadata.TITLE_LITERAL);
        field2metadata.put(File.Field.VERSION, Metadata.VERSION_LITERAL);
        field2metadata.put(File.Field.CONTENT, Metadata.CONTENT_LITERAL);
        field2metadata.put(File.Field.ID, Metadata.ID_LITERAL);
        field2metadata.put(File.Field.FILE_SIZE, Metadata.FILE_SIZE_LITERAL);
        field2metadata.put(File.Field.DESCRIPTION, Metadata.DESCRIPTION_LITERAL);
        field2metadata.put(File.Field.URL, Metadata.URL_LITERAL);
        field2metadata.put(File.Field.CREATED_BY, Metadata.CREATED_BY_LITERAL);
        field2metadata.put(File.Field.FILENAME, Metadata.FILENAME_LITERAL);
        field2metadata.put(File.Field.FILE_MIMETYPE, Metadata.FILE_MIMETYPE_LITERAL);
        field2metadata.put(File.Field.SEQUENCE_NUMBER, Metadata.SEQUENCE_NUMBER_LITERAL);
        field2metadata.put(File.Field.CATEGORIES, Metadata.CATEGORIES_LITERAL);
        field2metadata.put(File.Field.LOCKED_UNTIL, Metadata.LOCKED_UNTIL_LITERAL);
        field2metadata.put(File.Field.FILE_MD5SUM, Metadata.FILE_MD5SUM_LITERAL);
        field2metadata.put(File.Field.VERSION_COMMENT, Metadata.VERSION_COMMENT_LITERAL);
        field2metadata.put(File.Field.CURRENT_VERSION, Metadata.CURRENT_VERSION_LITERAL);
        field2metadata.put(File.Field.COLOR_LABEL, Metadata.COLOR_LABEL_LITERAL);
        field2metadata.put(File.Field.LAST_MODIFIED_UTC, Metadata.LAST_MODIFIED_UTC_LITERAL);
        field2metadata.put(File.Field.NUMBER_OF_VERSIONS, Metadata.NUMBER_OF_VERSIONS_LITERAL);
        field2metadata.put(File.Field.META, Metadata.META_LITERAL);
        field2metadata.put(File.Field.OBJECT_PERMISSIONS, Metadata.OBJECT_PERMISSIONS_LITERAL);
        field2metadata.put(File.Field.SHAREABLE, Metadata.SHAREABLE_LITERAL);
    }

    public static Metadata getMatching(File.Field field) {
        return field2metadata.get(field);
    }

    public static Metadata[] getMatching(List<File.Field> fields) {
        Metadata[] retval = new Metadata[fields.size()];
        for(int i = 0; i < retval.length; i++) {
            retval[i] = getMatching(fields.get(i));
        }
        return retval;
    }

    public static int getSortDirection(SortDirection order) {
        return SortDirection.DESC.equals(order) ? InfostoreFacade.DESC : InfostoreFacade.ASC;
    }

}
