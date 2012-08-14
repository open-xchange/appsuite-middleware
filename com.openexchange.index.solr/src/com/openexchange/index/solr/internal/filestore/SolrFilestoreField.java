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

package com.openexchange.index.solr.internal.filestore;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.index.filestore.FilestoreIndexField;
import com.openexchange.index.solr.internal.SolrField;

/**
 * {@link SolrFilestoreField}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum SolrFilestoreField implements SolrField {

    UUID("uuid", FilestoreIndexField.UUID, "param1"),
    ACCOUNT("account", FilestoreIndexField.ACCOUNT, "param2"),
    SERVICE("service", FilestoreIndexField.SERVICE, "param3"),
    FOLDER("folder_id", FilestoreIndexField.FOLDER, "param4"),
    ID("id", FilestoreIndexField.ID, "param5"),
    CREATED_BY("created_by", FilestoreIndexField.CREATED_BY, "param6"),
    MODIFIED_BY("modified_by", FilestoreIndexField.MODIFIED_BY, "param7"),
    CREATED("creation_date", FilestoreIndexField.CREATED, "param8"),
    LAST_MODIFIED("last_modified", FilestoreIndexField.LAST_MODIFIED, "param9"),
    TITLE("title", FilestoreIndexField.TITLE, "param10"),
    VERSION("version", FilestoreIndexField.VERSION, "param11"),
    FILE_SIZE("file_size", FilestoreIndexField.FILE_SIZE, "param12"),
    MIME_TYPE("mime_type", FilestoreIndexField.MIME_TYPE, "param13"),
    FILE_NAME("file_name", FilestoreIndexField.FILE_NAME, "param14"),
    DESCRIPTION("description", FilestoreIndexField.DESCRIPTION, "param15"),
    URL("url", FilestoreIndexField.URL, "param16"),
    SEQUENCE_NUMBER("sequence_number", FilestoreIndexField.SEQUENCE_NUMBER, "param17"),
    CATEGORIES("categories", FilestoreIndexField.CATEGORIES, "param18"),
    COLOR_LABEL("color_label", FilestoreIndexField.COLOR_LABEL, "param19"),
    VERSION_COMMENT("version_comment", FilestoreIndexField.VERSION_COMMENT, "param20"),
    CONTENT("content", FilestoreIndexField.CONTENT, "param21"),
    MD5_SUM("md5_sum", FilestoreIndexField.MD5_SUM, "param22");
    

    private String solrName;

    private FilestoreIndexField indexField;

    private String parameterName;
    
    private static final Map<String, SolrFilestoreField> solrNameMapping = new HashMap<String, SolrFilestoreField>();
    
    private static final Map<FilestoreIndexField, SolrFilestoreField> fieldMapping = new EnumMap<FilestoreIndexField, SolrFilestoreField>(FilestoreIndexField.class);
    
    private static final Set<SolrFilestoreField> indexedFields = EnumSet.noneOf(SolrFilestoreField.class);
    
    static {
        for (SolrFilestoreField field : values()) {
            String name = field.solrName();
            if (name != null) {
                solrNameMapping.put(name, field);
                fieldMapping.put(field.indexField(), field);
                indexedFields.add(field);
            }
        }
    }
    
    public static SolrFilestoreField getBySolrName(String solrName) {
        return solrNameMapping.get(solrName);
    }
    
    public static SolrFilestoreField getByIndexField(FilestoreIndexField indexField) {
        return fieldMapping.get(indexField);
    }
    
    public static Collection<SolrFilestoreField> getIndexedFields() {
        return Collections.unmodifiableCollection(solrNameMapping.values());
    }
    
    public static String[] solrNamesFor(Set<SolrFilestoreField> solrFields) {
        String[] names = new String[solrFields.size()];
        int i = 0;
        for (SolrFilestoreField field : solrFields) {
            names[i++] = field.solrName();
        }
        
        return names;
    }

    private SolrFilestoreField(String solrName, FilestoreIndexField indexField, String parameterName) {
        this.solrName = solrName;
        this.indexField = indexField;
        this.parameterName = parameterName;
    }

    public String solrName() {
        return solrName;
    }

    public FilestoreIndexField indexField() {
        return indexField;
    }

    public String parameterName() {
        return parameterName;
    }
}
