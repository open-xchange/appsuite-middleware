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

package com.openexchange.index.infostore;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.index.IndexField;


/**
 * {@link InfostoreIndexField}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum InfostoreIndexField implements IndexField {
    
    UUID(null),
    FOLDER(Metadata.FOLDER_ID_LITERAL),
    ID(Metadata.ID_LITERAL),
    CREATED_BY(Metadata.CREATED_BY_LITERAL),
    MODIFIED_BY(Metadata.MODIFIED_BY_LITERAL),
    CREATED(Metadata.CREATION_DATE_LITERAL),
    LAST_MODIFIED(Metadata.LAST_MODIFIED_LITERAL),
    TITLE(Metadata.TITLE_LITERAL),
    VERSION(Metadata.VERSION_LITERAL),
    DESCRIPTION(Metadata.DESCRIPTION_LITERAL),
    URL(Metadata.URL_LITERAL),
    SEQUENCE_NUMBER(Metadata.SEQUENCE_NUMBER_LITERAL),
    CATEGORIES(Metadata.CATEGORIES_LITERAL),
    COLOR_LABEL(Metadata.COLOR_LABEL_LITERAL),
    VERSION_COMMENT(Metadata.VERSION_COMMENT_LITERAL),
    NUMBER_OF_VERSIONS(Metadata.NUMBER_OF_VERSIONS_LITERAL),
    FILESTORE_LOCATION(Metadata.FILESTORE_LOCATION_LITERAL);

    
    
    private static final Map<Metadata, InfostoreIndexField> mapping = new HashMap<Metadata, InfostoreIndexField>();
    
    static {
        for (InfostoreIndexField field : values()) {
            Metadata metadataField = field.getMetadataField();
            if (metadataField != null) {
                mapping.put(metadataField, field);
            }            
        }
    }
    
    private final Metadata metadataField;
    
    private InfostoreIndexField(Metadata metadataField) {
        this.metadataField = metadataField;
    }
    
    public Metadata getMetadataField() {
        return metadataField;
    }
    
    public static InfostoreIndexField getByMetadateField(int metadataField) {
        return mapping.get(metadataField);
    }

}
