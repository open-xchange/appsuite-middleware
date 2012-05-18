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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.mail.MailSolrIndexAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.solr.SolrCoreIdentifier;


/**
 * {@link MailSolrIndexAccessTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MailSolrIndexAccessTest extends TestCase {
    
    private static final int QUERY_ROWS = MockMailSolrIndexAccess.getQueryRows();
    
    private static final int DOCS = QUERY_ROWS * 2;
    
    
    public void testChunkLoading() throws Exception {
        Services.setServiceLookup(new ServiceLookup() {            
            @Override
            public <S> S getService(Class<? extends S> clazz) {
                return (S) new MockConfigurationService();
            }
            
            @Override
            public <S> S getOptionalService(Class<? extends S> clazz) {
                return null;
            }
        });
        
              
        /*
         * Get less than one chunk size
         */    
        MockMailSolrIndexAccess indexAccess = new MockMailSolrIndexAccess(DOCS);
        Random random = new Random();
        int len = random.nextInt(QUERY_ROWS);
        searchAndCheckLength(indexAccess, 0, len, len);
        
        /*
         * Get more than one chunk but less than index size
         */
        len = QUERY_ROWS + 11;
        searchAndCheckLength(indexAccess, 0, len, len);
        
        /*
         * Search for more than index size
         */
        len = DOCS * 2;
        searchAndCheckLength(indexAccess, 0, len, DOCS);       
        
        /*
         * Get less than one chunk size with offset > 0
         */
        int off = random.nextInt(QUERY_ROWS);
        len = random.nextInt(QUERY_ROWS);
        searchAndCheckLength(indexAccess, off, len, len);
        
        /*
         * Get more than one chunk but less than index size with offset > 0
         */
        off = random.nextInt(QUERY_ROWS);
        len = QUERY_ROWS + 11;
        searchAndCheckLength(indexAccess, off, len, len);
        
        /*
         * Search for more than index size with offset > 0
         */
        off = random.nextInt(QUERY_ROWS);
        len = DOCS * 2;
        searchAndCheckLength(indexAccess, off, len, DOCS - off);       
    }
    
    private void searchAndCheckLength(MockMailSolrIndexAccess indexAccess, int off, int len, int expected) throws Exception {        
        QueryParameters parameters = new QueryParameters.Builder(Collections.EMPTY_MAP)
            .setHandler(SearchHandler.ALL_REQUEST)
            .setType(Type.MAIL)
            .setOffset(off)
            .setLength(len)
            .build();
        IndexResult<MailMessage> result = indexAccess.query(parameters, null);
        assertTrue(result.getNumFound() == expected);
    }
    
    private static class MockMailSolrIndexAccess extends MailSolrIndexAccess {
        
        private static final List<Map<String, Object>> index = new ArrayList<Map<String,Object>>();

        
        /**
         * Initializes a new {@link MockMailSolrIndexAccess}.
         * @param identifier
         * @param triggerType
         */
        public MockMailSolrIndexAccess(int docs) {
            super(new SolrCoreIdentifier(1, 1, 1));
            for (int i = 0; i < docs; i++) {                
                index.add(Collections.singletonMap("time_" + i, (Object) System.currentTimeMillis()));
            }
        }
        
        public static int getQueryRows() {
            return QUERY_ROWS;
        }
        
        @Override
        protected QueryResponse query(SolrParams query) throws OXException {
            int start = Integer.parseInt(query.get("start"));
            int rows = Integer.parseInt(query.get("rows"));
            int end = start + rows;            
            if (start > index.size()) {
                return new MockQueryResponse(Collections.EMPTY_SET);
            }
            
            if (end > index.size()) {
                end = index.size();
            }
            
            Set<Map<String, Object>> entries = new HashSet<Map<String, Object>>(QUERY_ROWS);
            List<Map<String, Object>> subList = index.subList(start, end);
            entries.addAll(subList);            
            return new MockQueryResponse(entries);
        }
        
    }

}
