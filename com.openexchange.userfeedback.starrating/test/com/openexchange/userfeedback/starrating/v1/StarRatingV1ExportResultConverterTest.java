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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.userfeedback.starrating.v1;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.userfeedback.ExportResult;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.Feedback;
import com.openexchange.userfeedback.FeedbackMetaData;

/**
 * {@link StarRatingV1ExportResultConverterTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class StarRatingV1ExportResultConverterTest {

    private Feedback feedback;

    Map<String, String> config = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        FeedbackMetaData metaData = FeedbackMetaData.builder().setCtxId(0).setDate(System.currentTimeMillis()).setLoginName("loginName").setServerVersion("serverVersion").build();
        feedback = Feedback.builder(metaData).build();

        config.put("delimiter", ",");
    }

    private static final String FEEDBACK_BACKSLASH_R_BACKSLASH_N = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"this text doesn't matter.\nThe 'rn' should not be converted to rnn!\nAll of them\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testCreateFeedback_feedbackContainsBackslashRBackslashN_leaveItAsItIs() throws JSONException, IOException {
        feedback.setContent(new JSONObject(FEEDBACK_BACKSLASH_R_BACKSLASH_N));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();

        String commentFromResult = getCommentFromResult(result);
        assertEquals("this text doesn\'t matter.\r\nThe \'rn\' should not be converted to rnn!\r\nAll of them", commentFromResult);
    }

    private static final String FEEDBACK_MULTIPLE_BACKSLASH_R_BACKSLASH_N = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"this text doesn't matter.\r\n\r\n\r\nThe 'rn' should not be converted to rnn!\r\nAll of them\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testCreateFeedback_feedbackContainsMultipleBackslashRBackslashN_leaveItAsItIs() throws JSONException, IOException {
        feedback.setContent(new JSONObject(FEEDBACK_MULTIPLE_BACKSLASH_R_BACKSLASH_N));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();

        String commentFromResult = getCommentFromResult(result);
        assertEquals("this text doesn\'t matter.\r\n\r\n\r\nThe \'rn\' should not be converted to rnn!\r\nAll of them", commentFromResult);

    }

    private static final String FEEDBACK_BACKSLASH_N = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"this text doesn't matter.\nBut the 'n' should be converted to rn!\nAll of them\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testCreateFeedback_feedbackContainsBackslashN_convertToBackslashRBackslashN() throws JSONException, IOException {
        feedback.setContent(new JSONObject(FEEDBACK_BACKSLASH_N));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();

        String commentFromResult = getCommentFromResult(result);
        assertEquals("this text doesn\'t matter.\r\nBut the \'n\' should be converted to rn!\r\nAll of them", commentFromResult);
    }

    private static final String FEEDBACK_MULTIPLE_BACKSLASH_N = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"this text doesn't matter.\n\n\nBut the multiple 'n's should be converted to rn!\n\nAll of them\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testCreateFeedback_feedbackContainsMultipleBackslashNs_convertToBackslashRBackslashNs() throws JSONException, IOException {
        feedback.setContent(new JSONObject(FEEDBACK_MULTIPLE_BACKSLASH_N));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);

        assertEquals("this text doesn't matter.\r\n\r\n\r\nBut the multiple 'n's should be converted to rn!\r\n\r\nAll of them", commentFromResult);
    }

    private static final String FEEDBACK_MIXED_BACKSLASH_R_BACKSLASH_N = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"this text doesn't matter.\nBut the 'n' should be converted to rn!\n\r\nAll of them\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testCreateFeedback_feedbackContainsMixedControlChars_convertToBackslashRBackslashNs() throws JSONException, IOException {
        feedback.setContent(new JSONObject(FEEDBACK_MIXED_BACKSLASH_R_BACKSLASH_N));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();

        String commentFromResult = getCommentFromResult(result);
        assertEquals("this text doesn\'t matter.\r\nBut the \'n\' should be converted to rn!\r\n\r\nAll of them", commentFromResult);
    }

    private static final String BUG_52995_FEEDBACK = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"=cmd|' /C calc'!A0\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug52995_avoidPipeAndSingleQuotes() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_52995_FEEDBACK));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("'=cmd\\|' /C calc'!A0", commentFromResult);
    }

    private static final String BUG_52995_FEEDBACK_1 = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"@cmd|' /C calc'!A0\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug52995_1_avoidPipeAndSingleQuotes() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_52995_FEEDBACK_1));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("'@cmd\\|' /C calc'!A0", commentFromResult);
    }

    private static final String BUG_52995_FEEDBACK_2 = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"|cmd|' /C calc'!A0\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug52995_2_avoidPipeAndSingleQuotes() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_52995_FEEDBACK_2));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("'|cmd\\|' /C calc'!A0", commentFromResult);
    }

    private static final String BUG_52995_FEEDBACK_3 = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"~cmd|' /C calc'!A0\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug52995_3_avoidPipeAndSingleQuotes() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_52995_FEEDBACK_3));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("~cmd\\|' /C calc'!A0", commentFromResult);
    }

    private static final String BUG_52995_FEEDBACK_4 = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"+cmd|' /C calc'!A0\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug52995_4_avoidPipeAndSingleQuotes() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_52995_FEEDBACK_4));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("'+cmd\\|' /C calc'!A0", commentFromResult);
    }

    private static final String BUG_52995_FEEDBACK_5 = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"-cmd|' /C calc'!A0\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug52995_5_avoidPipeAndSingleQuotes() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_52995_FEEDBACK_5));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("'-cmd\\|' /C calc'!A0", commentFromResult);
    }

    private static final String BUG_54533_FEEDBACK = "{\"score\":\"3\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"ich \n\nhabe\n\nmehrere\n\nzeilenumbr\u00FCche\n\n!!!\",\"operating_system\":\"Mac OS X 10_13_1\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36\",\"screen_resolution\":\"1680x1050\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug54533() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_54533_FEEDBACK));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("ich \r\n\r\nhabe\r\n\r\nmehrere\r\n\r\nzeilenumbr\u00FCche\r\n\r\n!!!", commentFromResult);
    }

    private static final String BUG_56021_FEEDBACK_1 = "{\"score\":\"2\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"Also ich sag ja immer: \\\"OX ist super! ;)\\\"\",\"operating_system\":\"Mac OS X 10_13_0\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36\",\"screen_resolution\":\"1440x900\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug56021_1() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_56021_FEEDBACK_1));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("Also ich sag ja immer: 'OX ist super! ;)'", commentFromResult);
    }

    private static final String BUG_56021_FEEDBACK_2 = "{\"score\":\"2\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"\\\";xyz\\\"\",\"operating_system\":\"Mac OS X 10_13_0\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36\",\"screen_resolution\":\"1440x900\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testBug56021_2() throws JSONException, IOException {
        feedback.setContent(new JSONObject(BUG_56021_FEEDBACK_2));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("';xyz'", commentFromResult);
    }

    private static final String SPECIAL_CHARS_1 = "{\"score\":\"2\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"\\\\ one more nonsensical feedback \\\\dkjfdla\n\n\\\\dkfjsl\",\"operating_system\":\"Mac OS X 10_13_0\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36\",\"screen_resolution\":\"1440x900\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testSpecialChars1() throws JSONException, IOException {
        feedback.setContent(new JSONObject(SPECIAL_CHARS_1));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("\\ one more nonsensical feedback \\dkjfdla\r\n\r\n\\dkfjsl", commentFromResult);
    }

    private static final String SPECIAL_CHARS_2 = "{\"score\":\"2\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"\u0060\u0060\u0060\u0060\u0060\n\ndsfkaljd\",\"operating_system\":\"Mac OS X 10_13_0\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36\",\"screen_resolution\":\"1440x900\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testSpecialChars2() throws JSONException, IOException {
        feedback.setContent(new JSONObject(SPECIAL_CHARS_2));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("\u0060\u0060\u0060\u0060\u0060\r\n\r\ndsfkaljd", commentFromResult);
    }

    private static final String SPECIAL_CHARS_3 = "{\"score\":\"2\",\"user\":\"tHeHaSHedUSER\",\"app\":\"mail\",\"entry_point\":\"mail\",\"comment\":\"'mal schauen was hier passiert ''''''''\n\n\u0060\u0060\u0060\n\ntoll\",\"operating_system\":\"Mac OS X 10_13_0\",\"browser\":\"Chrome\",\"browser_version\":\"62\",\"user_agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36\",\"screen_resolution\":\"1440x900\",\"language\":\"en_US\",\"date\":\"11.11.2011\",\"client_version\":\"7.10.0-Rev0\",\"server_version\":\"7.10.0-Rev111\"}";

    @Test
    public void testSpecialChars3() throws JSONException, IOException {
        feedback.setContent(new JSONObject(SPECIAL_CHARS_3));
        StarRatingV1ExportResultConverter converter = new StarRatingV1ExportResultConverter(Collections.singletonList(feedback), config);

        ExportResult exportResult = converter.get(ExportType.CSV);

        Object result = exportResult.getResult();
        String commentFromResult = getCommentFromResult(result);
        assertEquals("'mal schauen was hier passiert ''''''''\r\n\r\n\u0060\u0060\u0060\r\n\r\ntoll", commentFromResult);
    }

    private String getCommentFromResult(Object result) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withHeader("Date", "Score", "Comment", "App", "Entry Point", "Operating System", "Browser", "Browser Version", "User Agent", "Screen Resolution", "Language", "User", "Server Version", "Client Version").withQuoteMode(QuoteMode.ALL);
        try (CSVParser csvFileParser = new CSVParser(new InputStreamReader((InputStream) result, StandardCharsets.UTF_8), csvFileFormat)) {
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            CSVRecord record = csvRecords.get(1); // get second row (ignore header)
            return record.get(2); // get third column (comment)
        }
    }
}
