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

package com.openexchange.index.solr.internal.infostore;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.infostore.index.InfostoreIndexField;
import com.openexchange.index.solr.internal.SolrField;

/**
 * {@link SolrInfostoreField}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum SolrInfostoreField implements SolrField {

    UUID("uuid", InfostoreIndexField.UUID, "param1"),
    FILESTORE_LOCATION("filestore_location", InfostoreIndexField.FILESTORE_LOCATION, "param2"),
    NUMBER_OF_VERSIONS("number_of_versions", InfostoreIndexField.NUMBER_OF_VERSIONS, "param3"),
    FOLDER("folder_id", InfostoreIndexField.FOLDER, "param4"),
    ID("id", InfostoreIndexField.ID, "param5"),
    CREATED_BY("created_by", InfostoreIndexField.CREATED_BY, "param6"),
    MODIFIED_BY("modified_by", InfostoreIndexField.MODIFIED_BY, "param7"),
    CREATED("creation_date", InfostoreIndexField.CREATED, "param8"),
    LAST_MODIFIED("last_modified", InfostoreIndexField.LAST_MODIFIED, "param9"),
    TITLE("title", InfostoreIndexField.TITLE, "param10"),
    VERSION("version", InfostoreIndexField.VERSION, "param11"),
    DESCRIPTION("description", InfostoreIndexField.DESCRIPTION, "param15"),
    URL("url", InfostoreIndexField.URL, "param16"),
    SEQUENCE_NUMBER("sequence_number", InfostoreIndexField.SEQUENCE_NUMBER, "param17"),
    CATEGORIES("categories", InfostoreIndexField.CATEGORIES, "param18"),
    COLOR_LABEL("color_label", InfostoreIndexField.COLOR_LABEL, "param19"),
    VERSION_COMMENT("version_comment", InfostoreIndexField.VERSION_COMMENT, "param20");
    

    private String solrName;

    private InfostoreIndexField indexField;

    private String parameterName;
    
    private static final Map<String, SolrInfostoreField> solrNameMapping = new HashMap<String, SolrInfostoreField>();
    
    private static final Map<InfostoreIndexField, SolrInfostoreField> fieldMapping = new EnumMap<InfostoreIndexField, SolrInfostoreField>(InfostoreIndexField.class);
    
    private static final Set<InfostoreIndexField> indexedFields = EnumSet.noneOf(InfostoreIndexField.class);
    
    static {
        for (SolrInfostoreField field : values()) {
            String name = field.solrName();
            if (name != null) {
                solrNameMapping.put(name, field);
                fieldMapping.put(field.indexField(), field);
                indexedFields.add(field.indexField());
            }
        }
    }
    
    public static SolrInfostoreField getBySolrName(String solrName) {
        return solrNameMapping.get(solrName);
    }
    
    public static SolrInfostoreField getByIndexField(InfostoreIndexField indexField) {
        return fieldMapping.get(indexField);
    }
    
    public static Set<InfostoreIndexField> getIndexedFields() {        
        return Collections.unmodifiableSet(indexedFields);
    }
    
    public static String[] solrNamesFor(Set<SolrInfostoreField> solrFields) {
        String[] names = new String[solrFields.size()];
        int i = 0;
        for (SolrInfostoreField field : solrFields) {
            names[i++] = field.solrName();
        }
        
        return names;
    }

    private SolrInfostoreField(String solrName, InfostoreIndexField indexField, String parameterName) {
        this.solrName = solrName;
        this.indexField = indexField;
        this.parameterName = parameterName;
    }

    @Override
    public String solrName() {
        return solrName;
    }

    @Override
    public InfostoreIndexField indexField() {
        return indexField;
    }

    @Override
    public String parameterName() {
        return parameterName;
    }
}
