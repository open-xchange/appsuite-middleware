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

package com.openexchange.index.solr.internal;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * {@link ConfigurationTest} - You must set the property SOLR_BUNDLE_DIR before launching this test. Example:
 * -DSOLR_BUNDLE_DIR=/home/user/git/backend/com.openexchange.solr
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigurationTest {

    @Test
    public void testCoreStartUp() throws Exception {
        String bundleDir = System.getProperty("SOLR_BUNDLE_DIR");
        String confDir = bundleDir + File.separatorChar + "solr" + File.separatorChar + "conf";
        String libDir = bundleDir + File.separatorChar + "solr" + File.separatorChar + "lib";
        String schemaFile = confDir + File.separatorChar + "schemas" + File.separatorChar + "mail_v1.xml";
        String configFile = confDir + File.separatorChar + "solrconfig-mail.xml";
        CoreContainer coreContainer = new CoreContainer("/tmp");
        coreContainer.load("/tmp", new InputSource(new FileInputStream(confDir + File.separatorChar + "solr.xml")));

        String coreName = "testCore";
        File coreStoreDir = File.createTempFile("test-core-dir", null);
        coreStoreDir.delete();
        coreStoreDir.mkdir();
        try {
            File dataDir = new File(coreStoreDir, coreName);
            dataDir.mkdir();
            Properties properties = new Properties();
            properties.put("data.dir", dataDir.getAbsolutePath());
            properties.put("logDir", "/tmp");
            properties.put("confDir", confDir);
            properties.put("libDir", libDir);
            CoreDescriptor coreDescriptor = new CoreDescriptor(coreContainer, coreName, coreStoreDir.getAbsolutePath());
            coreDescriptor.setDataDir(dataDir.getAbsolutePath());
            coreDescriptor.setSchemaName(schemaFile);
            coreDescriptor.setConfigName(configFile);
            coreDescriptor.setCoreProperties(properties);
            SolrCore embeddedSolrCore = coreContainer.create(coreDescriptor);
            coreContainer.register(coreName, embeddedSolrCore, false);

            indexAndSearch(coreContainer, coreName);
            embeddedSolrCore.close();
        } finally {
            coreContainer.shutdown();
            FileUtils.deleteDirectory(coreStoreDir);
        }
    }

    private void indexAndSearch(CoreContainer coreContainer, String coreName) throws Exception {
        EmbeddedSolrServer solr = new EmbeddedSolrServer(coreContainer, coreName);
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("uuid", "Mail1");
        doc.setField("from", "some.body@some-where.com");
        doc.setField("to", "anybody.else@some-where.com");
        doc.setField("subject", "This is the mail you want to index!");
        doc.setField("content", "And here you see the content...");

        solr.add(doc);
        solr.commit();

        SolrQuery query = new SolrQuery("mail");
        query.setQueryType("simpleSearch");
        QueryResponse response = solr.query(query);
        Assert.assertEquals(1L, response.getResults().getNumFound());
    }

}
