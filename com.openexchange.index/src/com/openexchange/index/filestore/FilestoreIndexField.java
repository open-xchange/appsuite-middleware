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

package com.openexchange.index.filestore;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.index.IndexField;
import com.openexchange.file.storage.File.Field;


/**
 * {@link FilestoreIndexField}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum FilestoreIndexField implements IndexField {
    
    UUID(null),
    ACCOUNT(null),
    SERVICE(null),
    FOLDER(Field.FOLDER_ID),
    ID(Field.ID),
    CREATED_BY(Field.CREATED_BY),
    MODIFIED_BY(Field.MODIFIED_BY),
    CREATED(Field.CREATED),
    LAST_MODIFIED(Field.LAST_MODIFIED),
    TITLE(Field.TITLE),
    VERSION(Field.VERSION),
    FILE_SIZE(Field.FILE_SIZE),
    MIME_TYPE(Field.FILE_MIMETYPE),
    FILE_NAME(Field.FILENAME),
    DESCRIPTION(Field.DESCRIPTION),
    URL(Field.URL),
    SEQUENCE_NUMBER(Field.SEQUENCE_NUMBER),
    CATEGORIES(Field.CATEGORIES),
    COLOR_LABEL(Field.COLOR_LABEL),
    VERSION_COMMENT(Field.VERSION_COMMENT),
    CONTENT(Field.CONTENT),
    MD5_SUM(Field.FILE_MD5SUM);
    
    
    private static final Map<Field, FilestoreIndexField> mapping = new EnumMap<Field, FilestoreIndexField>(Field.class);
    
    static {
        for (FilestoreIndexField field : values()) {
            mapping.put(field.getFileField(), field);
        }
    }
    
    private Field field;
    
    private FilestoreIndexField(Field field) {
        this.field = field;
    }
    
    public Field getFileField() {
        return field;
    }
    
    public static FilestoreIndexField getByFileField(Field field) {
        return mapping.get(field);
    }

}
