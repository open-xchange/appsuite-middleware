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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.infostore.SolrDocumentMetadata;
import com.openexchange.index.solr.internal.infostore.SolrInfostoreIndexAccess;


/**
 * {@link SolrFilestoreIndexAccessTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrFilestoreIndexAccessTest extends TestCase {
    
    private SolrInfostoreIndexAccess indexAccess;
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        indexAccess = new MockSolrFilestoreIndexAccess();
    }

    public void testAddDocument() throws Exception {
        DocumentMetadata file = new SolrDocumentMetadata();
        file.setCategories("Ene mene muh");
        file.setColorLabel(3);
        file.setCreationDate(new GregorianCalendar(2005, 3, 12, 17, 24, 43).getTime());
        file.setCreatedBy(5);
        file.setDescription("This is the description");
        file.setFileMD5Sum("234345645mlml4k5");
        file.setFileMIMEType("text/html");
        file.setFileName("A_file_name.html");
        file.setFileSize(33456L);
        file.setFolderId(1234L);
        file.setId(4352);
        file.setLastModified(new Date());
        file.setModifiedBy(16);
        file.setTitle("I am the title, man...");
        file.setURL("http://some.where");
        file.setVersion(26);
        file.setVersionComment("Version comment...");
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IndexConstants.ACCOUNT, "sada689");
        StandardIndexDocument<DocumentMetadata> document = new StandardIndexDocument<DocumentMetadata>(file);
        document.setProperties(parameters);
        indexAccess.addEnvelopeData(document);
        
        QueryParameters query = new QueryParameters.Builder(parameters).setHandler(SearchHandler.ALL_REQUEST).build();
        IndexResult<DocumentMetadata> result = indexAccess.query(query, null);
        assertTrue("Wrong result size", result.getNumFound() == 1);
        DocumentMetadata reloaded = result.getResults().get(0).getObject();
        // FIXME: calc differences
//        Set<DocumentMetadata> differences = file.differences(reloaded);        
//        assertTrue("There were differences.", differences.size() == 0);
        fail("FIXME!");
    }
    
}
