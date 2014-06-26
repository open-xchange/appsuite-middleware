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

package com.openexchange.index.solr;

import static com.openexchange.index.solr.internal.LuceneQueryTools.buildQueryString;
import static com.openexchange.index.solr.internal.LuceneQueryTools.buildQueryStringWithOr;
import static com.openexchange.index.solr.internal.LuceneQueryTools.catenateQueriesWithAnd;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.StrField;
import org.junit.Test;
import com.openexchange.index.solr.internal.querybuilder.BuilderException;
import com.openexchange.index.solr.internal.querybuilder.SimpleQueryBuilder;



/**
 * {@link SimpleQueryBuilderTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SimpleQueryBuilderTest extends SimpleQueryBuilder {

    /**
     * Initializes a new {@link SimpleQueryBuilderTest}.
     * @param configPath
     * @param moduleField
     * @param accountField
     * @param folderField
     * @param fieldMapper
     * @throws BuilderException
     */
    public SimpleQueryBuilderTest() {
        super();
    }

    @Test
    public void testFilterQueries() throws Exception {
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
        Assert.assertNull(catenateQueriesWithAnd((String[]) null));
        Assert.assertNull(catenateQueriesWithAnd(null, null));
    }
    
    @Test
    public void testBug24918() throws Exception {
        String folderName = "Apstiprin\u0101ts \"ham";
        String folderQuery = buildQueryString("full_name", folderName);
        String accountQuery = buildQueryString("account", 0);
        String queryString = catenateQueriesWithAnd(accountQuery, folderQuery);
        
        FieldType strField = new StrField();
        Analyzer indexAnalyzer = strField.getAnalyzer();
        Analyzer queryAnalyzer = strField.getQueryAnalyzer();

        MemoryIndex index = new MemoryIndex();
        index.addField("account", "0", indexAnalyzer);
        index.addField("full_name", folderName, indexAnalyzer);
        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "full_name", queryAnalyzer);
        float score = index.search(parser.parse(queryString));
        Assert.assertTrue("Query '" + queryString + "' did not match.", score > 0.0f);
        System.out.println("indexData=" + index.toString());
    }

}