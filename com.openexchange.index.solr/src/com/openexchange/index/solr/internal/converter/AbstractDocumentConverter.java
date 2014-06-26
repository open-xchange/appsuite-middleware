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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexField;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.config.FieldConfiguration;


/**
 * {@link AbstractDocumentConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @param <V>
 */
public abstract class AbstractDocumentConverter<V> implements SolrDocumentConverter<V> {

    protected final FieldConfiguration fieldConfig;


    /**
     * Initializes a new {@link AbstractDocumentConverter}.
     * @param fieldConfig
     */
    public AbstractDocumentConverter(FieldConfiguration fieldConfig) {
        super();
        this.fieldConfig = fieldConfig;
    }

    protected <T> T getFieldValue(IndexField indexField, SolrDocument document) throws OXException {
        if (fieldConfig.isLocalized(indexField)) {
            Set<String> solrFields = fieldConfig.getSolrFields(indexField);
            if (solrFields == null || solrFields.isEmpty()) {
                return null;
            }

            for (String field : solrFields) {
                if (document.containsKey(field)) {
                    try {
                        return (T) document.get(field);
                    } catch (final ClassCastException e) {
                        throw IndexExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected type: " + e.getMessage());
                    }
                }
            }
        } else {
            String name = fieldConfig.getRawField(indexField);
            if (name == null) {
                return null;
            }

            Object value = document.getFieldValue(name);
            if (value == null) {
                return null;
            }

            try {
                return (T) value;
            } catch (final ClassCastException e) {
                throw IndexExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected type: " + e.getMessage());
            }
        }

        return null;
    }

    protected void setFieldInDocument(SolrInputDocument inputDocument, IndexField field, Object value) {
        String fieldName = fieldConfig.getRawField(field);
        if (fieldName != null && value != null) {
            inputDocument.remove(fieldName);
            inputDocument.addField(fieldName, value);
        }
    }

    protected void addFieldInDocument(SolrInputDocument inputDocument, IndexField field, List<Object> values) {
        String fieldName = fieldConfig.getRawField(field);
        if (fieldName != null && values != null && !values.isEmpty()) {
            for (Object value : values) {
                inputDocument.addField(fieldName, value);
            }
        }
    }

    protected void addHighlighting(StandardIndexDocument<V> indexDocument, Map<String, List<String>> highlightedFields) {
        if (indexDocument != null && highlightedFields != null) {
            for (String solrField : highlightedFields.keySet()) {
                IndexField indexField = fieldConfig.getIndexField(solrField);
                if (indexField != null) {
                    List<String> highlights = highlightedFields.get(solrField);
                    indexDocument.addHighlighting(indexField, highlights);
                }
            }
        }
    }

}
