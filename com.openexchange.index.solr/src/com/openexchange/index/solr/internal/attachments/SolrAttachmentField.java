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

package com.openexchange.index.solr.internal.attachments;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.index.AttachmentIndexField;
import com.openexchange.index.solr.internal.SolrField;


/**
 * {@link SolrAttachmentField}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum SolrAttachmentField implements SolrField {
    
    UUID("uuid", "param1", null),
    MODULE("module", "param2", AttachmentIndexField.MODULE),
    SERVICE("service", "param3", AttachmentIndexField.SERVICE),
    ACCOUNT("account", "param4", AttachmentIndexField.ACCOUNT),
    FOLDER("folder", "param5", AttachmentIndexField.FOLDER),
    OBJECT_ID("id", "param6", AttachmentIndexField.ID),    
    FILE_NAME("file_name", "param7", AttachmentIndexField.FILE_NAME),
    FILE_SIZE("file_size", "param8", AttachmentIndexField.FILE_SIZE),
    MIME_TYPE("mime_type", "param9", AttachmentIndexField.MIME_TYPE),
    MD5_SUM("md5_sum", "param10", AttachmentIndexField.MD5_SUM),
    CONTENT("content", "param11", AttachmentIndexField.CONTENT),
    ATTACHMENT_ID("attachment_id", "param12", AttachmentIndexField.ATTACHMENT_ID);
    
    
    private static final Map<AttachmentIndexField, SolrAttachmentField> fieldMapping = new EnumMap<AttachmentIndexField, SolrAttachmentField>(AttachmentIndexField.class);
    
    private final String solrName;
    
    private final String paramName;   
    
    private final AttachmentIndexField indexField;
    
    static {
        for (SolrAttachmentField solrField : values()) {
            AttachmentIndexField field = solrField.indexField();
            if (field != null) {
                fieldMapping.put(field, solrField);
            }            
        }
    }

    private SolrAttachmentField(String solrName, String paramName, AttachmentIndexField indexField) {
        this.solrName = solrName;
        this.paramName = paramName;
        this.indexField = indexField;
    }
    
    public String solrName() {
        return solrName;
    }
    
    public String parameterName() {
        return paramName;
    }
    
    public AttachmentIndexField indexField() {
        return indexField;
    }
    
    public static SolrAttachmentField solrFieldFor(AttachmentIndexField indexField) {
        return fieldMapping.get(indexField);
    }
}
