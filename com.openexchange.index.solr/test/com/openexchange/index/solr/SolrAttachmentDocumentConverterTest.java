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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.AttachmentIndexField;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.attachments.SolrAttachmentDocumentConverter;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.config.XMLBasedFieldConfiguration;
import com.openexchange.junit.Assert;
import com.openexchange.server.ServiceLookup;
import com.openexchange.textxtraction.TextXtractService;


/**
 * {@link SolrAttachmentDocumentConverterTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrAttachmentDocumentConverterTest {

    private static final String SOLR_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
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
    		"    <requestHandler name=\"simpleSearch\" class=\"solr.SearchHandler\">\n" +
    		"        <lst name=\"invariants\">\n" +
    		"            <str name=\"defType\">edismax</str>\n" +
    		"            <str name=\"echoParams\">explicit</str>\n" +
    		"            <float name=\"tie\">0.01</float>\n" +
    		"            <str name=\"qf\">\n" +
    		"                file_name^10.0 content_gen^5.0 content_de^5.0 content_en^5.0 content_fr^5.0 content_it^5.0\n" +
    		"            </str>\n" +
    		"        </lst>\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"fl\">*</str> <!-- Envelope Felder -->\n" +
    		"        </lst>\n" +
    		"\n" +
    		"        <arr name=\"components\">\n" +
    		"            <str>query</str>\n" +
    		"        </arr>\n" +
    		"    </requestHandler>\n" +
    		"\n" +
    		"    <requestHandler name=\"allSearch\" class=\"solr.SearchHandler\">\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"fl\">*</str> <!-- Envelope Felder -->\n" +
    		"            <str name=\"df\">folder</str>\n" +
    		"            <str name=\"q.op\">OR</str>\n" +
    		"        </lst>\n" +
    		"\n" +
    		"        <arr name=\"components\">\n" +
    		"            <str>query</str>\n" +
    		"        </arr>\n" +
    		"    </requestHandler>\n" +
    		"    \n" +
    		"    <requestHandler name=\"getSearch\" class=\"solr.SearchHandler\">\n" +
    		"        <arr name=\"components\">\n" +
    		"            <str>searchFieldSupplier</str>\n" +
    		"            <str>query</str>\n" +
    		"        </arr>\n" +
    		"    </requestHandler>\n" +
    		"    \n" +
    		"    <searchComponent name=\"searchFieldSupplier\" class=\"de.kippdata.cria.solrext.searchcomponents.SearchFieldSupplier\">\n" +
    		"        <str name=\"sf\">uuid</str>\n" +
    		"    </searchComponent>\n" +
    		"\n" +
    		"    <requestHandler name=\"customSearch\" class=\"solr.SearchHandler\">\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"fl\">*</str>\n" +
    		"            <str name=\"q.op\">OR</str>\n" +
    		"        </lst>\n" +
    		"\n" +
    		"        <arr name=\"components\">\n" +
    		"            <str>query</str>\n" +
    		"        </arr>\n" +
    		"    </requestHandler>\n" +
    		"    \n" +
    		"    <requestHandler name=\"personsAndTopics\" class=\"solr.SearchHandler\">\n" +
    		"        <lst name=\"invariants\">\n" +
    		"            <str name=\"defType\">edismax</str>\n" +
    		"            <str name=\"echoParams\">explicit</str>\n" +
    		"            <float name=\"tie\">0.01</float>\n" +
    		"            <str name=\"qf\">\n" +
    		"                file_name^100 content_gen^50 content_en^50 content_de^50 content_fr^50 content_it^50\n" +
    		"            </str>\n" +
    		"            <str name=\"fq\">module:19</str>\n" +
    		"            <str name=\"hl\">true</str>\n" +
    		"            <str name=\"hl.snippets\">1</str>\n" +
    		"            <str name=\"hl.usePhraseHighlighter\">true</str>\n" +
    		"            <str name=\"hl.requireFieldMatch\">true</str>\n" +
    		"        </lst>\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"fl\">uuid file_name content_gen content_en content_de content_fr content_it</str>\n" +
    		"        </lst>\n" +
    		"\n" +
    		"        <arr name=\"components\">\n" +
    		"            <str>query</str>\n" +
    		"            <str>highlight</str>\n" +
    		"        </arr>\n" +
    		"    </requestHandler>\n" +
    		"\n" +
    		"    <requestHandler name=\"/update\" class=\"solr.XmlUpdateRequestHandler\">\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"update.chain\">langid</str>\n" +
    		"        </lst>\n" +
    		"    </requestHandler>\n" +
    		"    <updateRequestProcessorChain name=\"langid\">\n" +
    		"        <processor\n" +
    		"            class=\"org.apache.solr.update.processor.TikaLanguageIdentifierUpdateProcessorFactory\">\n" +
    		"            <bool name=\"langid\">true</bool>\n" +
    		"            <str name=\"langid.fl\">content</str>\n" +
    		"            <str name=\"langid.whitelist\">de,en,fr,it</str>\n" +
    		"            <str name=\"langid.langField\">locale</str>\n" +
    		"            <bool name=\"langid.map\">true</bool>\n" +
    		"            <str name=\"langid.threshold\">0.2</str>\n" +
    		"            <str name=\"langid.fallback\">gen</str>\n" +
    		"        </processor>\n" +
    		"        <processor class=\"solr.LogUpdateProcessorFactory\" />\n" +
    		"        <processor class=\"solr.RunUpdateProcessorFactory\" />\n" +
    		"    </updateRequestProcessorChain>\n" +
    		"    \n" +
    		"    <requestHandler name=\"/update/javabin\" class=\"solr.BinaryUpdateRequestHandler\" />\n" +
    		"</config>";

    private static final String SCHEMA = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
    		"<schema name=\"OX Schema Attachments\" version=\"1.5\">\n" +
    		"    <types>\n" +
    		"        <fieldType name=\"string\" class=\"solr.StrField\" sortMissingLast=\"true\" />\n" +
    		"        <fieldType name=\"long\" class=\"solr.LongField\" />\n" +
    		"        <fieldType name=\"int\" class=\"solr.IntField\" />\n" +
    		"\n" +
    		"        <!-- German -->\n" +
    		"        <fieldType name=\"text_de\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n" +
    		"            <analyzer type=\"index\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_en.txt\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.GermanNormalizationFilterFactory\" />\n" +
    		"                <filter class=\"solr.GermanLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"            <analyzer type=\"query\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.SynonymFilterFactory\" synonyms=\"${confDir:/opt/open-xchange/solr/conf}/synonyms.txt\" ignoreCase=\"true\" expand=\"true\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_en.txt\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.GermanNormalizationFilterFactory\" />\n" +
    		"                <filter class=\"solr.GermanLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"\n" +
    		"        <!-- English -->\n" +
    		"        <fieldType name=\"text_en\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n" +
    		"            <analyzer type=\"index\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_en.txt\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.EnglishPossessiveFilterFactory\" />\n" +
    		"                <filter class=\"solr.KeywordMarkerFilterFactory\" protected=\"${confDir:/opt/open-xchange/solr/conf}/protwords.txt\" />\n" +
    		"                <filter class=\"solr.PorterStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"            <analyzer type=\"query\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.SynonymFilterFactory\" synonyms=\"${confDir:/opt/open-xchange/solr/conf}/synonyms.txt\" ignoreCase=\"true\" expand=\"true\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_en.txt\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.EnglishPossessiveFilterFactory\" />\n" +
    		"                <filter class=\"solr.KeywordMarkerFilterFactory\" protected=\"${confDir:/opt/open-xchange/solr/conf}/protwords.txt\" />\n" +
    		"                <filter class=\"solr.PorterStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"\n" +
    		"        <!-- French -->\n" +
    		"        <fieldType name=\"text_fr\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n" +
    		"            <analyzer type=\"index\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <!-- removes l', etc -->\n" +
    		"                <filter class=\"solr.ElisionFilterFactory\" ignoreCase=\"true\" articles=\"${confDir:/opt/open-xchange/solr/conf}/contractions_fr.txt\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_fr.txt\" format=\"snowball\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.FrenchLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"            <analyzer type=\"query\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.SynonymFilterFactory\" synonyms=\"${confDir:/opt/open-xchange/solr/conf}/synonyms.txt\" ignoreCase=\"true\" expand=\"true\" />\n" +
    		"                <!-- removes l', etc -->\n" +
    		"                <filter class=\"solr.ElisionFilterFactory\" ignoreCase=\"true\" articles=\"${confDir:/opt/open-xchange/solr/conf}/contractions_fr.txt\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_fr.txt\" format=\"snowball\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.FrenchLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"\n" +
    		"        <!-- Italian -->\n" +
    		"        <fieldType name=\"text_it\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n" +
    		"            <analyzer type=\"index\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <!-- removes l', etc -->\n" +
    		"                <filter class=\"solr.ElisionFilterFactory\" ignoreCase=\"true\" articles=\"${confDir:/opt/open-xchange/solr/conf}/contractions_it.txt\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_it.txt\" format=\"snowball\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.ItalianLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"            <analyzer type=\"query\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.SynonymFilterFactory\" synonyms=\"${confDir:/opt/open-xchange/solr/conf}/synonyms.txt\" ignoreCase=\"true\" expand=\"true\" />\n" +
    		"                <!-- removes l', etc -->\n" +
    		"                <filter class=\"solr.ElisionFilterFactory\" ignoreCase=\"true\" articles=\"${confDir:/opt/open-xchange/solr/conf}/contractions_it.txt\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_it.txt\" format=\"snowball\"\n" +
    		"                    enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.ItalianLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"\n" +
    		"        <fieldType name=\"text\" class=\"solr.TextField\" positionIncrementGap=\"100\" omitNorms=\"false\">\n" +
    		"            <analyzer type=\"index\">\n" +
    		"                <!-- StandardTokenizer zur Bereinigung von Interpunktion -->\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.WordDelimiterFilterFactory\" generateWordParts=\"1\" generateNumberParts=\"1\" catenateWords=\"0\" catenateNumbers=\"1\"\n" +
    		"                    catenateAll=\"0\" splitOnCaseChange=\"1\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.RemoveDuplicatesTokenFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"            <analyzer type=\"query\">\n" +
    		"                <tokenizer class=\"solr.WhitespaceTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.WordDelimiterFilterFactory\" generateWordParts=\"1\" generateNumberParts=\"1\" catenateWords=\"0\" catenateNumbers=\"1\"\n" +
    		"                    catenateAll=\"0\" splitOnCaseChange=\"1\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.SynonymFilterFactory\" synonyms=\"${confDir:/opt/open-xchange/solr/conf}/synonyms.txt\" ignoreCase=\"true\" expand=\"true\" />\n" +
    		"                <filter class=\"solr.RemoveDuplicatesTokenFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"\n" +
    		"        <fieldType name=\"file_name\" class=\"solr.TextField\" positionIncrementGap=\"100\" omitNorms=\"false\">\n" +
    		"            <analyzer>\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.WordDelimiterFilterFactory\" generateWordParts=\"1\" generateNumberParts=\"1\" catenateWords=\"0\" catenateNumbers=\"0\"\n" +
    		"                    catenateAll=\"0\" splitOnCaseChange=\"1\" preserveOriginal=\"1\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.LengthFilterFactory\" min=\"2\" max=\"30\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"    </types>\n" +
    		"\n" +
    		"    <fields>\n" +
    		"        <field name=\"uuid\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.UUID\" />\n" +
    		"        <field name=\"module\" type=\"int\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.MODULE\" />\n" +
    		"        <field name=\"account\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.ACCOUNT\" />\n" +
    		"        <field name=\"folder\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.FOLDER\" />\n" +
    		"        <field name=\"id\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.OBJECT_ID\" />\n" +
    		"        <field name=\"attachment_id\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.ATTACHMENT_ID\" />\n" +
    		"        <field name=\"file_name\" type=\"file_name\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.FILE_NAME\" />\n" +
    		"        <field name=\"file_size\" type=\"long\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.FILE_SIZE\" />\n" +
    		"        <field name=\"mime_type\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.MIME_TYPE\" />\n" +
    		"        <field name=\"md5_sum\" type=\"string\" stored=\"true\" indexed=\"false\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.MD5_SUM\" />\n" +
    		"\n" +
    		"        <field name=\"content\" type=\"text\" stored=\"false\" indexed=\"false\" oxIndexField=\"com.openexchange.groupware.attach.index.AttachmentIndexField.CONTENT\" />\n" +
    		"        <field name=\"content_gen\" type=\"text\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_de\" type=\"text_de\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_en\" type=\"text_en\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_fr\" type=\"text_fr\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_it\" type=\"text_it\" stored=\"false\" indexed=\"true\" />\n" +
    		"\n" +
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
    public void testDocument2Attachment() throws Exception {
        SolrAttachmentDocumentConverter converter = new SolrAttachmentDocumentConverter(config);
        SolrDocument document = new SolrDocument();
        document.setField("uuid", UUID.randomUUID().toString());
        document.setField("module", Types.EMAIL);
        document.setField("account", "0");
        document.setField("folder", "INBOX");
        document.setField("id", "1234");
        document.setField("attachment_id", "1");
        document.setField("file_name", "some_attachment.pdf");
        document.setField("file_size", 9876543210L);
        document.setField("mime_type", "application/pdf");
        document.setField("md5_sum", "fasdfsdfgsdgfsd89787897dfs90");

        IndexDocument<Attachment> indexDocument = converter.convert(document);
        Attachment attachment = indexDocument.getObject();
        Assert.assertEquals("Wrong value module", document.getFieldValue("module"), attachment.getModule());
        Assert.assertEquals("Wrong value account", document.getFieldValue("account"), attachment.getAccount());
        Assert.assertEquals("Wrong value folder", document.getFieldValue("folder"), attachment.getFolder());
        Assert.assertEquals("Wrong value id", document.getFieldValue("id"), attachment.getObjectId());
        Assert.assertEquals("Wrong value attachment_id", document.getFieldValue("attachment_id"), attachment.getAttachmentId());
        Assert.assertEquals("Wrong value file_name", document.getFieldValue("file_name"), attachment.getFileName());
        Assert.assertEquals("Wrong value file_size", document.getFieldValue("file_size"), attachment.getFileSize());
        Assert.assertEquals("Wrong value mime_type", document.getFieldValue("mime_type"), attachment.getMimeType());
        Assert.assertEquals("Wrong value md5_sum", document.getFieldValue("md5_sum"), attachment.getMd5Sum());
    }

    @Test
    public void testAttachment2Document() throws Exception {
        SolrAttachmentDocumentConverter converter = new SolrAttachmentDocumentConverter(config);
        Services.setServiceLookup(new ServiceLookup() {
            @Override
            public <S> S getService(Class<? extends S> clazz) {
                if (clazz.equals(TextXtractService.class)) {
                    return (S) new TextXtractService() {
                        @Override
                        public String extractFromResource(String resource, String optMimeType) throws OXException {
                            return null;
                        }

                        @Override
                        public String extractFrom(String content, String optMimeType) throws OXException {
                            return null;
                        }

                        @Override
                        public String extractFrom(InputStream inputStream, String optMimeType) throws OXException {
                            return "This is the content";
                        }
                    };
                }

                return null;
            }

            @Override
            public <S> S getOptionalService(Class<? extends S> clazz) {
                return null;
            }
        });

        Attachment attachment = new Attachment();
        attachment.setModule(Types.EMAIL);
        attachment.setAccount("0");
        attachment.setFolder("INBOX");
        attachment.setObjectId("1234");
        attachment.setAttachmentId("1");
        attachment.setFileName("some_attachment.pdf");
        attachment.setFileSize(9876543210L);
        attachment.setMimeType("application/pdf");
        attachment.setMd5Sum("fasdfsdfgsdgfsd89787897dfs90");
        attachment.setContent(new EmptyInputStream());

        SolrInputDocument document = converter.convert(1, 2, new StandardIndexDocument<Attachment>(null, attachment));
        Assert.assertEquals("Wrong value uuid", document.getFieldValue("uuid"), AttachmentUUID.newUUID(1, 2, attachment).toString());
        Assert.assertEquals("Wrong value module", document.getFieldValue("module"), attachment.getModule());
        Assert.assertEquals("Wrong value account", document.getFieldValue("account"), attachment.getAccount());
        Assert.assertEquals("Wrong value folder", document.getFieldValue("folder"), attachment.getFolder());
        Assert.assertEquals("Wrong value id", document.getFieldValue("id"), attachment.getObjectId());
        Assert.assertEquals("Wrong value attachment_id", document.getFieldValue("attachment_id"), attachment.getAttachmentId());
        Assert.assertEquals("Wrong value file_name", document.getFieldValue("file_name"), attachment.getFileName());
        Assert.assertEquals("Wrong value file_size", document.getFieldValue("file_size"), attachment.getFileSize());
        Assert.assertEquals("Wrong value mime_type", document.getFieldValue("mime_type"), attachment.getMimeType());
        Assert.assertEquals("Wrong value md5_sum", document.getFieldValue("md5_sum"), attachment.getMd5Sum());
        Assert.assertEquals("Wrong value content", document.getFieldValue("content"), "This is the content");
    }

    @Test
    public void testHighlighting() throws Exception {
        SolrAttachmentDocumentConverter converter = new SolrAttachmentDocumentConverter(config);
        Map<String, List<String>> highlights = new HashMap<String, List<String>>();
        List<String> content_genHighlights = new ArrayList<String>();
        content_genHighlights.add("blubb1");
        List<String> content_deHighlights = new ArrayList<String>();
        content_deHighlights.add("blubb2");
        List<String> content_nonExistsHighlights = new ArrayList<String>(); // Must not appear in result
        content_nonExistsHighlights.add("murks");
        List<String> file_nameHighlights = new ArrayList<String>();
        file_nameHighlights.add("blubb3");
        highlights.put("content_gen", content_genHighlights);
        highlights.put("content_de", content_deHighlights);
        highlights.put("content_nonExists", content_nonExistsHighlights);
        highlights.put("file_name", file_nameHighlights);

        IndexDocument<Attachment> indexDocument = converter.convert(new SolrDocument(), highlights);
        Map<IndexField, List<String>> highlighting = indexDocument.getHighlighting();
        Assert.assertNotNull("highlighting was null", highlighting);
        Assert.assertEquals("Wrong size", 2, highlighting.size());

        List<String> fileNames = highlighting.get(AttachmentIndexField.FILE_NAME);
        Assert.assertEquals("Wrong size", 1, fileNames.size());
        Assert.assertTrue("Missing value", fileNames.contains("blubb3"));

        List<String> contents = highlighting.get(AttachmentIndexField.CONTENT);
        Assert.assertEquals("Wrong size", 2, contents.size());
        Assert.assertTrue("Missing value", contents.contains("blubb1"));
        Assert.assertTrue("Missing value", contents.contains("blubb2"));
    }

    @Test
    public void testNullValues() throws Exception {
        SolrAttachmentDocumentConverter converter = new SolrAttachmentDocumentConverter(config);
        IndexDocument<Attachment> indexDocument = converter.convert(new SolrDocument(), null);
        Assert.assertNotNull(indexDocument);
        Assert.assertNotNull(indexDocument.getObject());
        Assert.assertNull(indexDocument.getHighlighting());

        /*
         * Conversion with minimum value set
         */
        Attachment attachment = new Attachment();
        attachment.setFolder("INBOX");
        attachment.setObjectId("1234");
        SolrInputDocument inputDocument = converter.convert(1, 2, new StandardIndexDocument<Attachment>(null, attachment));
        Assert.assertNotNull(inputDocument);
        Assert.assertNotNull(inputDocument.getFieldValue("uuid"));
    }

    private static final class EmptyInputStream extends InputStream {

        public EmptyInputStream() {
            super();
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

    }

}
