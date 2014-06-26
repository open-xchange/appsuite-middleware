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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import junit.framework.Assert;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.config.XMLBasedFieldConfiguration;
import com.openexchange.index.solr.internal.mail.SolrMailDocumentConverter;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;


/**
 * {@link SolrMailDocumentConverterTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrMailDocumentConverterTest {

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
    		"                content_gen^1.0 content_de^1.0 content_en^1.0 content_fr^1.0 content_it^1.0 subject_gen^3.0 subject_de^3.0 subject_en^3.0 subject_fr^3.0 subject_it^3.0 from^2.0 to^2.0\n" +
    		"                cc^1.0 bcc^1.0\n" +
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
    		"            <str name=\"df\">full_name</str>\n" +
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
    		"            <str>query</str>\n" +
    		"        </arr>\n" +
    		"    </requestHandler>\n" +
    		"\n" +
    		"    <requestHandler name=\"customSearch\" class=\"solr.SearchHandler\">\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"fl\">*</str>\n" +
    		"            <!-- <str name=\"df\">full_name</str> -->\n" +
    		"            <str name=\"q.op\">OR</str>\n" +
    		"        </lst>\n" +
    		"\n" +
    		"        <!-- Use this only if \"components\" is not specified to use the defaults -->\n" +
    		"        <!--\n" +
    		"            <arr name=\"first-components\">\n" +
    		"            <str>queryModifier</str>\n" +
    		"            </arr>\n" +
    		"        -->\n" +
    		"        <arr name=\"components\">\n" +
    		"            <str>query</str>\n" +
    		"            <!-- Uncomment for debugging purposes only -->\n" +
    		"            <!--\n" +
    		"                <str>debug</str>\n" +
    		"            -->\n" +
    		"        </arr>\n" +
    		"    </requestHandler>\n" +
    		"    \n" +
    		"    <requestHandler name=\"personsAndTopics\" class=\"solr.SearchHandler\">\n" +
    		"        <lst name=\"invariants\">\n" +
    		"            <str name=\"defType\">edismax</str>\n" +
    		"            <str name=\"echoParams\">explicit</str>\n" +
    		"            <float name=\"tie\">0.01</float>\n" +
    		"            <str name=\"qf\">\n" +
    		"                from^100.0 to^100.0 cc^50.0 bcc^5.0 subject_gen^100.0 subject_en^100.0 subject_de^100.0 subject_fr^100.0 subject_it^100.0\n" +
    		"            </str>\n" +
    		"            <str name=\"hl\">true</str>\n" +
    		"            <str name=\"hl.snippets\">1</str>\n" +
    		"            <str name=\"hl.usePhraseHighlighter\">true</str>\n" +
    		"            <str name=\"hl.requireFieldMatch\">true</str>\n" +
    		"        </lst>\n" +
    		"        <lst name=\"defaults\">\n" +
    		"            <str name=\"fl\">uuid from to cc bcc subject_gen subject_en subject_de subject_fr subject_it</str>\n" +
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
    		"\n" +
    		"    <requestHandler name=\"/update/javabin\" class=\"solr.BinaryUpdateRequestHandler\" />\n" +
    		"\n" +
    		"</config>";

    private static final String SCHEMA = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
    		"<schema name=\"OX Schema Mail\" version=\"1.5\">\n" +
    		"    <types>\n" +
    		"        <fieldType name=\"boolean\" class=\"solr.BoolField\" omitNorms=\"true\" />\n" +
    		"        <fieldType name=\"string\" class=\"solr.StrField\" sortMissingLast=\"true\" omitNorms=\"true\" />\n" +
    		"        <fieldType name=\"long\" class=\"solr.LongField\" omitNorms=\"true\" />\n" +
    		"        <fieldType name=\"int\" class=\"solr.IntField\" omitNorms=\"true\" />\n" +
    		"        \n" +
    		"        <fieldType name=\"addr\" class=\"solr.TextField\">\n" +
    		"            <analyzer>\n" +
    		"                <tokenizer class=\"solr.KeywordTokenizerFactory\" />\n" +
    		"                <filter class=\"de.kippdata.solrext.analyzer.InternetAddressCleanupFilterFactory\" />\n" +
    		"                <filter class=\"solr.WordDelimiterFilterFactory\" catenateWords=\"0\" catenateNumbers=\"0\" catenateAll=\"0\" splitOnCaseChange=\"0\"\n" +
    		"                    splitOnNumerics=\"0\" types=\"${confDir:/opt/open-xchange/solr/conf}/wdf.txt\" />\n" +
    		"                <filter class=\"de.kippdata.solrext.analyzer.DomainSkipFilterFactory\" domains=\"de com co.uk net fr it\" />\n" +
    		"                <filter class=\"solr.WordDelimiterFilterFactory\" catenateWords=\"0\" catenateNumbers=\"0\" catenateAll=\"0\" splitOnCaseChange=\"0\"\n" +
    		"                    splitOnNumerics=\"0\" />\n" +
    		"                <filter class=\"solr.LengthFilterFactory\" min=\"2\" max=\"30\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"        \n" +
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
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />                \n" +
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
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_it.txt\" format=\"snowball\" enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.ItalianLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"            <analyzer type=\"query\">\n" +
    		"                <tokenizer class=\"solr.StandardTokenizerFactory\" />\n" +
    		"                <filter class=\"solr.SynonymFilterFactory\" synonyms=\"${confDir:/opt/open-xchange/solr/conf}/synonyms.txt\" ignoreCase=\"true\" expand=\"true\" />\n" +
    		"                <!-- removes l', etc -->\n" +
    		"                <filter class=\"solr.ElisionFilterFactory\" ignoreCase=\"true\" articles=\"${confDir:/opt/open-xchange/solr/conf}/contractions_it.txt\" />\n" +
    		"                <filter class=\"solr.LowerCaseFilterFactory\" />\n" +
    		"                <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"${confDir:/opt/open-xchange/solr/conf}/stopwords_it.txt\" format=\"snowball\" enablePositionIncrements=\"true\" />\n" +
    		"                <filter class=\"solr.ItalianLightStemFilterFactory\" />\n" +
    		"            </analyzer>\n" +
    		"        </fieldType>\n" +
    		"\n" +
    		"        <fieldType name=\"text\" class=\"solr.TextField\" positionIncrementGap=\"100\" omitNorms=\"false\">\n" +
    		"            <analyzer type=\"index\">\n" +
    		"                <tokenizer class=\"solr.WhitespaceTokenizerFactory\" />\n" +
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
    		"    </types>\n" +
    		"\n" +
    		"    <fields>\n" +
    		"        <!-- Common Fields -->\n" +
    		"        <field name=\"uuid\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.UUID\" />\n" +
    		"        <field name=\"timestamp\" type=\"long\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.TIMESTAMP\" />\n" +
    		"        <field name=\"account\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.ACCOUNT\" />\n" +
    		"        <field name=\"full_name\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FULL_NAME\" />\n" +
    		"        <field name=\"id\" type=\"string\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.ID\" />\n" +
    		"        <field name=\"color_label\" type=\"int\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.COLOR_LABEL\" />\n" +
    		"        <field name=\"attachment\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.ATTACHMENT\" />\n" +
    		"\n" +
    		"        <!-- Long fields -->\n" +
    		"        <field name=\"received_date\" type=\"long\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.RECEIVED_DATE\" />\n" +
    		"        <field name=\"sent_date\" type=\"long\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.SENT_DATE\" />\n" +
    		"        <field name=\"size\" type=\"long\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.SIZE\" />\n" +
    		"\n" +
    		"        <!-- Flag fields -->\n" +
    		"        <field name=\"flag_answered\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_ANSWERED\" />\n" +
    		"        <field name=\"flag_deleted\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_DELETED\" />\n" +
    		"        <field name=\"flag_draft\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_DRAFT\" />\n" +
    		"        <field name=\"flag_flagged\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_FLAGGED\" />\n" +
    		"        <field name=\"flag_recent\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_RECENT\" />\n" +
    		"        <field name=\"flag_seen\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_SEEN\" />\n" +
    		"        <field name=\"flag_user\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_USER\" />\n" +
    		"        <field name=\"flag_spam\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_SPAM\" />\n" +
    		"        <field name=\"flag_forwarded\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_FORWARDED\" />\n" +
    		"        <field name=\"flag_read_ack\" type=\"boolean\" stored=\"true\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FLAG_READ_ACK\" />\n" +
    		"        <field name=\"user_flags\" type=\"string\" stored=\"true\" indexed=\"true\" multiValued=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.USER_FLAGS\" />\n" +
    		"        \n" +
    		"        <field name=\"content_flag\" type=\"boolean\" stored=\"false\" indexed=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.CONTENT_FLAG\" />\n" +
    		"\n" +
    		"        <field name=\"from\" type=\"addr\" stored=\"true\" indexed=\"true\" multiValued=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.FROM\" />\n" +
    		"        <field name=\"to\" type=\"addr\" stored=\"true\" indexed=\"true\" multiValued=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.TO\" />\n" +
    		"        <field name=\"cc\" type=\"addr\" stored=\"true\" indexed=\"true\" multiValued=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.CC\" />\n" +
    		"        <field name=\"bcc\" type=\"addr\" stored=\"true\" indexed=\"true\" multiValued=\"true\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.BCC\" />\n" +
    		"\n" +
    		"        <field name=\"subject\" type=\"text\" stored=\"false\" indexed=\"false\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.SUBJECT\" />\n" +
    		"        <field name=\"subject_gen\" type=\"text\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_de\" type=\"text_de\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_en\" type=\"text_en\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_fr\" type=\"text_fr\" stored=\"true\" indexed=\"true\" />\n" +
    		"        <field name=\"subject_it\" type=\"text_it\" stored=\"true\" indexed=\"true\" />\n" +
    		"        \n" +
    		"        <field name=\"content\" type=\"text\" stored=\"false\" indexed=\"false\" oxIndexField=\"com.openexchange.mail.index.MailIndexField.CONTENT\" />\n" +
    		"        <field name=\"content_gen\" type=\"text\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_de\" type=\"text_de\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_en\" type=\"text_en\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_fr\" type=\"text_fr\" stored=\"false\" indexed=\"true\" />\n" +
    		"        <field name=\"content_it\" type=\"text_it\" stored=\"false\" indexed=\"true\" /> \n" +
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
    public void testSolrDocument2MailMessage() throws Exception {
        SolrMailDocumentConverter converter = new SolrMailDocumentConverter(config);
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.addField("account", "0");
        solrDocument.addField("full_name", "INBOX");
        solrDocument.addField("id", "1234");
        solrDocument.addField("color_label", 1);
        solrDocument.addField("attachment", true);
        solrDocument.addField("received_date", System.currentTimeMillis());
        solrDocument.addField("sent_date", System.currentTimeMillis());
        solrDocument.addField("size", 9876543210L);
        solrDocument.addField("flag_answered", true);
        solrDocument.addField("flag_deleted", true);
        solrDocument.addField("flag_draft", true);
        solrDocument.addField("flag_flagged", true);
        solrDocument.addField("flag_recent", true);
        solrDocument.addField("flag_seen", true);
        solrDocument.addField("flag_user", true);
        solrDocument.addField("flag_spam", true);
        solrDocument.addField("flag_forwarded", true);
        solrDocument.addField("flag_read_ack", true);
        solrDocument.addField("user_flags", Arrays.asList(new String[] {"abc", "def"}));
        solrDocument.addField("from", Arrays.asList(new String[] {"abc.def@gmail.com", "def.ghi@gmx.de"}));
        solrDocument.addField("to", Arrays.asList(new String[] {"ghi.jkl@gmail.com", "jkl.mno@gmx.de"}));
        solrDocument.addField("cc", Arrays.asList(new String[] {"mno.pqr@gmail.com", "pqr.stu@gmx.de"}));
        solrDocument.addField("bcc", Arrays.asList(new String[] {"stu.vwx@gmail.com", "vwx.yz@gmx.de"}));
        solrDocument.addField("subject_fr", "This is the subject!");

        IndexDocument<MailMessage> document = converter.convert(solrDocument);
        MailMessage mail = document.getObject();
        Assert.assertEquals("Wrong value for field account",  solrDocument.getFieldValue("account") , String.valueOf(mail.getAccountId()));
        Assert.assertEquals("Wrong value for field full_name",  solrDocument.getFieldValue("full_name") , mail.getFolder());
        Assert.assertEquals("Wrong value for field id",  solrDocument.getFieldValue("id") , mail.getMailId());
        Assert.assertEquals("Wrong value for field color_label",  solrDocument.getFieldValue("color_label") , mail.getColorLabel());
        Assert.assertEquals("Wrong value for field attachment",  solrDocument.getFieldValue("attachment") , mail.hasAttachment());
        Assert.assertEquals("Wrong value for field received_date",  new Date((Long) solrDocument.getFieldValue("received_date")), mail.getReceivedDate());
        Assert.assertEquals("Wrong value for field sent_date",  new Date((Long) solrDocument.getFieldValue("sent_date")) , mail.getSentDate());
        Assert.assertEquals("Wrong value for field size",  solrDocument.getFieldValue("size") , mail.getSize());

        int flags = mail.getFlags();
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_FLAGGED) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0));
        Assert.assertTrue("Wrong value for flag", Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0));
        List<String> userFlags = (List<String>) solrDocument.getFieldValue("user_flags");
        String[] mailUserFlags = mail.getUserFlags();
        for (int i = 0; i < mailUserFlags.length; i++) {
            String muf = mailUserFlags[i];
            Assert.assertEquals("Wrong flag value", userFlags.get(i), muf);
        }

        checkAddresses((List<Object>) solrDocument.getFieldValue("from") , mail.getFrom());
        checkAddresses((List<Object>) solrDocument.getFieldValue("to") , mail.getTo());
        checkAddresses((List<Object>) solrDocument.getFieldValue("cc") , mail.getCc());
        checkAddresses((List<Object>) solrDocument.getFieldValue("bcc") , mail.getBcc());

        Assert.assertEquals("Wrong value for field subject_fr",  solrDocument.getFieldValue("subject_fr") , mail.getSubject());
    }

    @Test
    public void testMailMessage2InputDocument() throws Exception {
        SolrMailDocumentConverter converter = new SolrMailDocumentConverter(config);
        MailMessage mail = new IDMailMessage("1234", "INBOX");
        mail.setAccountId(2);
        mail.addFrom(new QuotedInternetAddress("abc.def@gmail.com"));
        mail.addFrom(new PlainTextAddress("def.ghi@gmail.com"));
        mail.addTo(new QuotedInternetAddress("ghi.jkl@gmail.com"));
        mail.addTo(new PlainTextAddress("jkl.mno@gmail.com"));
        mail.addCc(new QuotedInternetAddress("mno.pqr@gmail.com"));
        mail.addCc(new PlainTextAddress("pqr.stu@gmail.com"));
        mail.addBcc(new QuotedInternetAddress("stu.vwx@gmail.com"));
        mail.addBcc(new PlainTextAddress("vwx.yz@gmail.com"));
        mail.setColorLabel(5);
        mail.setHasAttachment(true);
        mail.setReceivedDate(new Date());
        mail.setSentDate(new Date());
        mail.setSize(9876543210L);
        mail.setFlags(1023);
        mail.addUserFlags(new String[] {"these", "are", "some", "flags"});
        mail.setSubject("This is the subject");
        ContentAwareMailMessage toConvert = new ContentAwareMailMessage("This is the body", mail);

        SolrInputDocument document = converter.convert(1, 2, new StandardIndexDocument<MailMessage>(null, toConvert));
        Assert.assertEquals("Wrong value uuid", MailUUID.newUUID(1, 2, mail.getAccountId(), mail.getFolder(), mail.getMailId()).toString(), document.getFieldValue("uuid"));
        Assert.assertEquals("Wrong value folder", mail.getFolder(), document.getFieldValue("full_name"));
        Assert.assertEquals("Wrong value id", mail.getMailId(), document.getFieldValue("id"));
        Assert.assertEquals("Wrong value account", String.valueOf(mail.getAccountId()), document.getFieldValue("account"));
        checkAddresses((List<Object>) document.getFieldValues("from"), mail.getFrom());
        checkAddresses((List<Object>) document.getFieldValues("to"), mail.getTo());
        checkAddresses((List<Object>) document.getFieldValues("cc"), mail.getCc());
        checkAddresses((List<Object>) document.getFieldValues("bcc"), mail.getBcc());
        Assert.assertEquals("Wrong value color label", mail.getColorLabel(), document.getFieldValue("color_label"));
        Assert.assertEquals("Wrong value attachments", mail.hasAttachment(), document.getFieldValue("attachment"));
        Assert.assertEquals("Wrong value received date", mail.getReceivedDate().getTime(), document.getFieldValue("received_date"));
        Assert.assertEquals("Wrong value sent date", mail.getSentDate().getTime(), document.getFieldValue("sent_date"));
        Assert.assertEquals("Wrong value size", mail.getSize(), document.getFieldValue("size"));

        Assert.assertEquals("Wrong value flag_answered", true, document.getFieldValue("flag_answered"));
        Assert.assertEquals("Wrong value flag_deleted", true, document.getFieldValue("flag_deleted"));
        Assert.assertEquals("Wrong value flag_draft", true, document.getFieldValue("flag_draft"));
        Assert.assertEquals("Wrong value flag_flagged", true, document.getFieldValue("flag_flagged"));
        Assert.assertEquals("Wrong value flag_recent", true, document.getFieldValue("flag_recent"));
        Assert.assertEquals("Wrong value flag_seen", true, document.getFieldValue("flag_seen"));
        Assert.assertEquals("Wrong value flag_user", true, document.getFieldValue("flag_user"));
        Assert.assertEquals("Wrong value flag_spam", true, document.getFieldValue("flag_spam"));
        Assert.assertEquals("Wrong value flag_forwarded", true, document.getFieldValue("flag_forwarded"));
        Assert.assertEquals("Wrong value flag_read_ack", true, document.getFieldValue("flag_read_ack"));
        String[] userFlags = mail.getUserFlags();
        List<Object> ufl = (List<Object>) document.getFieldValues("user_flags");
        for (int i = 0; i < userFlags.length; i++) {
            Assert.assertEquals(userFlags[i], ufl.get(i));
        }

        Assert.assertEquals("Wrong value subject", mail.getSubject(), document.getFieldValue("subject"));
        Assert.assertEquals("Wrong value content", toConvert.getPrimaryContent(), document.getFieldValue("content"));
    }

    @Test
    public void testHighlighting() throws Exception {
        SolrMailDocumentConverter converter = new SolrMailDocumentConverter(config);
        Map<String, List<String>> highlights = new HashMap<String, List<String>>();
        List<String> subject_genHighlights = new ArrayList<String>();
        subject_genHighlights.add("blubb1");
        List<String> subject_deHighlights = new ArrayList<String>();
        subject_deHighlights.add("blubb2");
        List<String> subject_nonExists = new ArrayList<String>(); // This must not appear in the converted document
        subject_nonExists.add("abc");
        List<String> fromHighlights = new ArrayList<String>();
        fromHighlights.add("blubb3");
        highlights.put("subject_gen", subject_genHighlights);
        highlights.put("subject_de", subject_deHighlights);
        highlights.put("subject_nonExists", subject_nonExists);
        highlights.put("from", fromHighlights);

        IndexDocument<MailMessage> indexDocument = converter.convert(new SolrDocument(), highlights);
        Map<IndexField, List<String>> highlighting = indexDocument.getHighlighting();
        Assert.assertNotNull("highlighting was null", highlighting);
        Assert.assertEquals("Wrong size", 2, highlighting.size());

        List<String> subjects = highlighting.get(MailIndexField.SUBJECT);
        Assert.assertEquals("Wrong size", 2, subjects.size());
        Assert.assertTrue("Missing value", subjects.contains("blubb1"));
        Assert.assertTrue("Missing value", subjects.contains("blubb2"));

        List<String> from = highlighting.get(MailIndexField.FROM);
        Assert.assertEquals("Wrong size", 1, from.size());
        Assert.assertTrue("Missing value", from.contains("blubb3"));
    }

    @Test
    public void testNullValues() throws Exception {
        SolrMailDocumentConverter converter = new SolrMailDocumentConverter(config);
        IndexDocument<MailMessage> indexDocument = converter.convert(new SolrDocument(), null);
        Assert.assertNotNull(indexDocument);
        Assert.assertNotNull(indexDocument.getObject());
        Assert.assertNull(indexDocument.getHighlighting());

        MailMessage mail = new IDMailMessage();
        ContentAwareMailMessage toConvert = new ContentAwareMailMessage("This is the body", mail);
        SolrInputDocument inputDocument = converter.convert(1, 2, new StandardIndexDocument<MailMessage>(null, toConvert));
        Assert.assertNotNull(inputDocument);
        Assert.assertNotNull(inputDocument.getFieldValue("uuid"));
    }

    private void checkAddresses(List<Object> plainText, InternetAddress[] addrs) {
        Assert.assertEquals("Wrong array size", plainText.size(), addrs.length);
        for (int i = 0; i < addrs.length; i++) {
            InternetAddress addr = addrs[i];
            Assert.assertEquals("Wrong value", plainText.get(i), addr.toString());
        }
    }
}
