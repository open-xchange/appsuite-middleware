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

import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.groupware.infostore.index.InfostoreIndexField;
import com.openexchange.index.IndexField;
import com.openexchange.index.solr.internal.FieldMapper;
import com.openexchange.index.solr.internal.SolrField;


/**
 * {@link InfostoreFieldMapper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreFieldMapper implements FieldMapper {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(InfostoreFieldMapper.class);
    
    private static final InfostoreFieldMapper INSTANCE = new InfostoreFieldMapper();
    
    private final Map<InfostoreIndexField, SolrInfostoreField> fieldMapping;
    
    
    private InfostoreFieldMapper() {
        super();
        fieldMapping = new EnumMap<InfostoreIndexField, SolrInfostoreField>(InfostoreIndexField.class);
        for (SolrInfostoreField solrField : SolrInfostoreField.values()) {
            InfostoreIndexField indexField = solrField.indexField();
            if (indexField != null) {
                fieldMapping.put(indexField, solrField);
            }
        }
    }
    
    public static InfostoreFieldMapper getInstance() {
        return INSTANCE;
    }
    
    @Override
    public SolrField solrFieldFor(IndexField indexField) {
        if (indexField == null) {
            return null;
        }
        
        if (!(indexField instanceof InfostoreIndexField)) {
            LOG.warn("Parameter 'indexField' must be of type " + InfostoreIndexField.class.getName() + "!");
            return null;
        }
        
        return fieldMapping.get(indexField);
    }

}
