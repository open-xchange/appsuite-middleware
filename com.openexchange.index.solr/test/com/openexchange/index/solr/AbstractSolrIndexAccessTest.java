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

package com.openexchange.index.solr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.index.FacetParameters;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.solr.SolrCoreIdentifier;

/**
 * {@link AbstractSolrIndexAccessTest}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AbstractSolrIndexAccessTest extends AbstractSolrIndexAccess<Void> {

    /**
     * Initializes a new {@link AbstractSolrIndexAccessTest}.
     * @param identifier
     */
    public AbstractSolrIndexAccessTest() {
        super(new SolrCoreIdentifier(1, 1, 1));
    }

    @Test
    public void testQueryBuilder() throws Exception {
        String field1 = "field1";
        String field2 = "field2";
        String value1 = "value1";
        Set<String> value2 = new HashSet<String>();
        value2.add("value2");
        value2.add("value3");
        
        String q1 = buildQueryString(field1, value1);
        Assert.assertEquals("Queries were not equal.", "(field1:\"value1\")", q1);
        
        String q2 = buildQueryStringWithOr(field2, value2);
        Assert.assertTrue("Queries were not equal.", q2.equals("((field2:\"value2\") OR (field2:\"value3\"))") || q2.equals("((field2:\"value3\") OR (field2:\"value2\"))"));

        String query = catenateQueriesWithAnd(q1, q2);
        Assert.assertTrue("Wrong query.", query.contains(q1) && query.contains(q2) && query.contains(" AND "));
        Assert.assertEquals("Wrong query size.", q1.length() + q2.length() + "( AND )".length(), query.length());
        
        Assert.assertNull(buildQueryString("abs", null));
        Assert.assertNull(buildQueryString(null, "abc"));
        Assert.assertNull(buildQueryStringWithOr("abs", null));
        Assert.assertNull(buildQueryStringWithOr(null, Collections.singleton("abc")));
        Assert.assertNull(buildQueryStringWithOr("abc", null));
        Assert.assertNull(catenateQueriesWithAnd(null));
        Assert.assertNull(catenateQueriesWithAnd(null, null));
    }

    @Override
    public boolean isIndexed(String accountId, String folderId) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<? extends IndexField> getIndexedFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addEnvelopeData(IndexDocument<Void> document) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addEnvelopeData(Collection<IndexDocument<Void>> documents) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addContent(IndexDocument<Void> document, boolean full) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addContent(Collection<IndexDocument<Void>> documents, boolean full) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAttachments(IndexDocument<Void> document, boolean full) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAttachments(Collection<IndexDocument<Void>> documents, boolean full) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteById(String id) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public IndexResult<Void> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexResult<Void> query(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
