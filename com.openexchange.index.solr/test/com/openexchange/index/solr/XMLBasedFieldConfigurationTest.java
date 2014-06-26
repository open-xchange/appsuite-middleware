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

import java.io.File;
import java.io.FileWriter;
import java.util.Set;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.index.IndexField;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.config.XMLBasedFieldConfiguration;
import com.openexchange.mail.index.MailIndexField;


/**
 * {@link XMLBasedFieldConfigurationTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class XMLBasedFieldConfigurationTest {

    private static final String SOLR_CONFIG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
    		"<config>\n" +
    		"    <abortOnConfigurationError>\n" +
    		"        ${solr.abortOnConfigurationError:true}\n" +
    		"    </abortOnConfigurationError>\n" +
    		"\n" +
    		"    <luceneMatchVersion>LUCENE_36</luceneMatchVersion>\n" +
    		"\n" +
    		"    <dataDir>${solr.core.dataDir}</dataDir>\n" +
    		"    <lib dir=\"${libDir:/opt/open-xchange/solr/lib}\" />\n" +
    		"\n" +
    		"    <configIndex>\n" +
    		"        <useCompoundFile>false</useCompoundFile>\n" +
    		"        <ramBufferSizeMB>32</ramBufferSizeMB>\n" +
    		"        <mergeFactor>10</mergeFactor>\n" +
    		"        <maxFieldLength>10000</maxFieldLength>\n" +
    		"        <unlockOnStartup>true</unlockOnStartup>\n" +
    		"        <reopenReaders>true</reopenReaders>\n" +
    		"        <writeLockTimeout>1000</writeLockTimeout>\n" +
    		"        <commitLockTimeout>10000</commitLockTimeout>\n" +
    		"        <lockType>simple</lockType>\n" +
    		"        <deletionPolicy class=\"solr.SolrDeletionPolicy\">\n" +
    		"            <str name=\"keepOptimizedOnly\">false</str>\n" +
    		"            <str name=\"maxCommitsToKeep\">1</str>\n" +
    		"        </deletionPolicy>\n" +
    		"    </configIndex>\n" +
    		"\n" +
    		"    <updateHandler class=\"solr.DirectUpdateHandler2\" />\n" +
    		"\n" +
    		"    <query>\n" +
    		"        <maxBooleanClauses>1024</maxBooleanClauses>\n" +
    		"        <filterCache class=\"solr.FastLRUCache\" size=\"512\" initialSize=\"512\" autowarmCount=\"0\" />\n" +
    		"        <queryResultCache class=\"solr.LRUCache\" size=\"512\" initialSize=\"512\" autowarmCount=\"0\" />\n" +
    		"        <documentCache class=\"solr.LRUCache\" size=\"512\" initialSize=\"512\" autowarmCount=\"0\" />\n" +
    		"        <enableLazyFieldLoading>true</enableLazyFieldLoading>\n" +
    		"        <queryResultWindowSize>20</queryResultWindowSize>\n" +
    		"        <queryResultMaxDocsCached>200</queryResultMaxDocsCached>\n" +
    		"        <listener event=\"newSearcher\" class=\"solr.QuerySenderListener\">\n" +
    		"            <arr name=\"queries\"></arr>\n" +
    		"        </listener>\n" +
    		"        <useColdSearcher>false</useColdSearcher>\n" +
    		"        <maxWarmingSearchers>5</maxWarmingSearchers>\n" +
    		"    </query>\n" +
    		"\n" +
    		"    <requestDispatcher handleSelect=\"true\">\n" +
    		"        <requestParsers enableRemoteStreaming=\"false\" multipartUploadLimitInKB=\"2048000\" />\n" +
    		"        <httpCaching never304=\"true\" />\n" +
    		"    </requestDispatcher>\n" +
    		"\n" +
    		"    <!-- For debugging purposes only -->\n" +
    		"    <requestHandler name=\"standard\" class=\"solr.SearchHandler\" default=\"false\">\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"echoParams\">explicit</str>\n" +
    		"        </lst>\n" +
    		"    </requestHandler>\n" +
    		"\n" +
    		"    <updateRequestProcessorChain name=\"langid\">\n" +
    		"        <processor class=\"org.apache.solr.update.processor.TikaLanguageIdentifierUpdateProcessorFactory\">\n" +
    		"            <bool name=\"langid\">true</bool>\n" +
    		"            <str name=\"langid.fl\">subject,content</str>\n" +
    		"            <str name=\"langid.whitelist\">de,en,fr,it</str>\n" +
    		"            <str name=\"langid.langField\">locale</str>\n" +
    		"            <bool name=\"langid.map\">true</bool>\n" +
    		"            <str name=\"langid.threshold\">0.2</str>\n" +
    		"            <str name=\"langid.fallback\">gen</str>\n" +
    		"        </processor>\n" +
    		"        <processor class=\"solr.LogUpdateProcessorFactory\" />\n" +
    		"        <processor class=\"solr.RunUpdateProcessorFactory\" />\n" +
    		"    </updateRequestProcessorChain>\n" +
    		"</config>";

    private static final String SCHEMA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
    		"<schema name=\"OX Test Schema\" version=\"1.5\">\n" +
    		"    <types>\n" +
    		"        <fieldType name=\"string\" class=\"solr.StrField\" sortMissingLast=\"true\" omitNorms=\"true\" />\n" +
    		"        <fieldType name=\"int\" class=\"solr.IntField\" omitNorms=\"true\" />\n" +
    		"    </types>\n" +
    		"\n" +
    		"    <fields>\n" +
    		"        <!-- Common Fields -->\n" +
    		"        <field name=\"uuid\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.UUID\" />\n" +
    		"\n" +
    		"        <field name=\"subject\" type=\"text\" stored=\"false\" indexed=\"false\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.SUBJECT\" />\n" +
    		"        <field name=\"subject_gen\" type=\"text\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_de\" type=\"text_de\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_en\" type=\"text_en\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_fr\" type=\"text_fr\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_it\" type=\"text_it\" stored=\"true\" indexed=\"true\" />\n" +
    		"        \n" +
    		"        <field name=\"no_enum\" type=\"text\" stored=\"false\" indexed=\"true\" oxIndexField=\"com.openexchange.index.solr.NoEnum.VALUE\" /> \n" +
    		"        \n" +
    		"        <field name=\"locale\" type=\"string\" stored=\"true\" indexed=\"false\" />\n" +
    		"    </fields>\n" +
    		"\n" +
    		"    <uniqueKey>uuid</uniqueKey>\n" +
    		"    <solrQueryParser defaultOperator=\"OR\" />\n" +
    		"    <defaultSearchField>uuid</defaultSearchField>\n" +
    		"</schema>\n" +
    		"";

    private static FieldConfiguration config;

    private static File configFile;

    private static File schemaFile;

    @BeforeClass
    public static void setUp() throws Exception {
        configFile = File.createTempFile("solrTestConfig", "xml");
        schemaFile = File.createTempFile("solrTestSchema", "xml");

        FileWriter fw = new FileWriter(configFile);
        fw.write(SOLR_CONFIG);
        fw.flush();
        fw.close();

        fw = new FileWriter(schemaFile);
        fw.write(SCHEMA);
        fw.flush();
        fw.close();

        config = new XMLBasedFieldConfiguration(configFile.getAbsolutePath(), schemaFile.getAbsolutePath());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (configFile != null) {
            configFile.delete();
        }

        if (schemaFile != null) {
            schemaFile.delete();
        }
    }

    @Test
    public void testGetIndexedFields() throws Exception {
        Set<? extends IndexField> indexedFields = config.getIndexedFields();
        Assert.assertEquals("Wrong number of indexed fields", 2, indexedFields.size());
        Assert.assertTrue("Missing field uuid", indexedFields.contains(MailIndexField.UUID));
        Assert.assertTrue("Missing field subject", indexedFields.contains(MailIndexField.SUBJECT));
    }

    @Test
    public void testIsLocalized() throws Exception {
        Assert.assertTrue(config.isLocalized(MailIndexField.SUBJECT));
        Assert.assertFalse(config.isLocalized(MailIndexField.UUID));
    }

    @Test
    public void testGetSolrFields() throws Exception {
        Set<String> solrFields = config.getSolrFields(MailIndexField.UUID);
        Assert.assertTrue(solrFields.size() == 1);
        Assert.assertEquals("uuid", solrFields.iterator().next());

        solrFields = config.getSolrFields(MailIndexField.SUBJECT);
        Assert.assertTrue(solrFields.size() == 5);
        Assert.assertTrue(solrFields.contains("subject_gen"));
        Assert.assertTrue(solrFields.contains("subject_en"));
        Assert.assertTrue(solrFields.contains("subject_de"));
        Assert.assertTrue(solrFields.contains("subject_it"));
        Assert.assertTrue(solrFields.contains("subject_fr"));
    }

    @Test
    public void testGetUUIDField() throws Exception {
        Assert.assertEquals("uuid", config.getUUIDField());
    }

    @Test
    public void testGetIndexField() throws Exception {
        Assert.assertEquals(MailIndexField.SUBJECT, config.getIndexField("subject_gen"));
        Assert.assertEquals(MailIndexField.SUBJECT, config.getIndexField("subject_en"));
        Assert.assertEquals(MailIndexField.SUBJECT, config.getIndexField("subject_de"));
        Assert.assertEquals(MailIndexField.SUBJECT, config.getIndexField("subject_it"));
        Assert.assertEquals(MailIndexField.SUBJECT, config.getIndexField("subject_fr"));

        Assert.assertEquals(MailIndexField.UUID, config.getIndexField("uuid"));
    }

    @Test
    public void testGetRawField() throws Exception {
        Assert.assertEquals("uuid", config.getRawField(MailIndexField.UUID));
        Assert.assertEquals("subject", config.getRawField(MailIndexField.SUBJECT));
    }

}
